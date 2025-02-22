package eu.faircode.xlua.x.xlua.repos;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.ui.core.FilterRequest;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;
import eu.faircode.xlua.x.xlua.settings.SettingsFactory;
import eu.faircode.xlua.x.xlua.commands.query.GetSettingsExCommand;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

public class SettingsRepository implements IXLuaRepo<SettingsContainer> {
    private static final String TAG = LibUtil.generateTag(SettingsRepository.class);

    public static final IXLuaRepo<SettingsContainer> INSTANCE = new SettingsRepository();

    @Override
    public List<SettingsContainer> get() { return Collections.emptyList(); }

    /*
        ToDO: Soon not yet, repo system should tangle with the shared universal app shit object
                        It can even store the Shared Registries etc
                        IRepo<TObject, TPacket>
                        The object can help us by storing more specific flags data args etc
                        We can also mass invoke handlers that would need an update etc..
                        Can also be useful when trying to share update objects
                        and when "filtering".....
                            IAppCarrier
                                XApp app;
                                ...
                                setApp(XApp app);
                                XApp getApp()
     */

    @Override
    public List<SettingsContainer> get(Context context, UserClientAppContext userContext) {
        List<SettingPacket> settings = GetSettingsExCommand.get(context, true, userContext.appUid, userContext.appPackageName, GetSettingsExCommand.FLAG_ONE);
        if(DebugUtil.isDebug())
            Log.d(TAG, "Settings Count from Command=" + ListUtil.size(settings));

        if(!ListUtil.isValid(settings))
            return ListUtil.emptyList();

        //ToDO: Build settings factory to do as it says
        //          So in theory, most of this bullshit at least (GetSettingsExCommand) should be done in the Factory!
        //          Goal is also to make these portable but usable any where Service or Client
        //          So Perhaps we can use "factory" MAYBE as a repo
        //          Hmm
        //          Perhaps it can handle a lot like keeping things up to date in containers etc
        //          Make a nice one way in linear control system so in theory I can add more it will handle duplicates etc

        SettingsFactory factory = new SettingsFactory();
        List<SettingPacket> all = factory.joinHookDefinedSettings(context, settings, userContext);
        if(!ListUtil.isValid(all))
            all = new ArrayList<>(settings);

        if(DebugUtil.isDebug())
            Log.d(TAG, "Settings Count from Joining from Command and Hooks=" + ListUtil.size(all) + " Original Count: " + ListUtil.size(settings));

        factory.parseSettings(all);
        factory.finish();
        List<SettingsContainer> containers = factory.getContainers();
        if(DebugUtil.isDebug())
            Log.d(TAG, "Got all Setting Containers from repo, Settings Original Count=" + ListUtil.size(settings) + " All Settings Count=" + ListUtil.size(all) + " Container Count=" + ListUtil.size(containers));

        return containers;
    }

    @Override
    public List<SettingsContainer> filterAndSort(List<SettingsContainer> items, FilterRequest request) {
        if(!ObjectUtils.anyNull(items, request))
            return ListUtil.emptyList();

        Comparator<SettingsContainer> comparator = getComparator(request.getOrderOrDefault("name"), request.isReversed);
        List<SettingsContainer> containers = new ArrayList<>();
        for(SettingsContainer container : items)
            if(isMatchingCriteria(container, request))
                containers.add(container);

        Collections.sort(containers, comparator);
        if(DebugUtil.isDebug())
            Log.d(TAG, "Filtered and Sorted through Settings, Original Size=" + ListUtil.size(items) + " New Size=" + ListUtil.size(containers) + " Request=" + Str.toStringOrNull(request));

        return containers;
    }

    public static boolean isMatchingCriteria(SettingsContainer container, FilterRequest request) {
        if(!container.getContainerName().toLowerCase().contains(request.getQueryOrDefault(container.getContainerName()).toLowerCase()))
            return false;
        //Make this into a irreplaceable type from IDiffFace ??
        if(request.hasFilterTags()) {
            for(String filter : request.filterTags) {
                switch (filter) {
                    case "enabled":
                        if(!container.isValid()) return false;
                        break;
                }
            }
        }

        return true;
    }

    public static Comparator<SettingsContainer> getComparator(String sortBy, boolean isReverse) {
        Comparator<SettingsContainer> comparator;
        switch (sortBy) {
            default:
                comparator = new Comparator<SettingsContainer>() {
                    @Override
                    public int compare(SettingsContainer a1, SettingsContainer a2) { return String.CASE_INSENSITIVE_ORDER.compare(a1.getContainerName(), a2.getContainerName()); }
                };
                break;
        }

        if (isReverse) {
            final Comparator<SettingsContainer> finalComparator = comparator;
            comparator = new Comparator<SettingsContainer>() {
                @Override
                public int compare(SettingsContainer a1, SettingsContainer a2) { return finalComparator.compare(a2, a1); }
            };
        }

        return comparator;
    }
}
