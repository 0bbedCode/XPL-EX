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
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;
import eu.faircode.xlua.x.xlua.settings.SettingsGroup;

public class SettingsGroupRepository implements IXLuaRepo<SettingsGroup> {
    private static final String TAG = LibUtil.generateTag(SettingsGroupRepository.class);

    public static final IXLuaRepo<SettingsGroup> INSTANCE = new SettingsGroupRepository();

    @Override
    public List<SettingsGroup> get() {
        return Collections.emptyList();
    }

    @Override
    public List<SettingsGroup> get(Context context, UserClientAppContext userContext) {
        return SettingsGroup.categorizeIntoGroups(SettingsRepository.INSTANCE.get(context, userContext));
    }

    @Override
    public List<SettingsGroup> filterAndSort(List<SettingsGroup> items, FilterRequest request) {
        if(ObjectUtils.anyNull(items, request)) {
            Log.e(TAG, "Input has Null or Bad Args!");
            return ListUtil.emptyList();
        }

        Comparator<SettingsGroup> comparator = getComparator(request.getOrderOrDefault("name"), request.isReversed);

        List<SettingsGroup> queryGroups = new ArrayList<>();
        if(!request.isEmptyOrClearQuery()) {
            for(SettingsGroup group : items)
                if( isMatchingCriteria(group, request))
                    queryGroups.add(group);
        } else {
            queryGroups.addAll(items);
        }


        Collections.sort(queryGroups, comparator);
        if(DebugUtil.isDebug())
            Log.d(TAG, "Filtered and Sorted through Settings groups, Original Size=" + ListUtil.size(items) + " Filtered Size=" + ListUtil.size(queryGroups) + " Request=" + Str.toStringOrNull(request));

        return queryGroups;
    }

    public static boolean isMatchingCriteria(SettingsGroup group, FilterRequest request) {
        //Make this more advance
        String qLow = request.query.toLowerCase();
        if(group.getGroupName().toLowerCase().contains(qLow))
            return true;

        for(SettingsContainer container : group.getContainers()) {
            if(container.getContainerName().toLowerCase().contains(qLow))
                return true;

            if(container.getDescription() != null && container.getDescription().toLowerCase().contains(qLow))
                return true;

            for(SettingHolder setting : container.getSettings())
                if(setting.getName().toLowerCase().contains(qLow))
                    return true;
        }

        return false;
    }



    public static Comparator<SettingsGroup> getComparator(String sortBy, boolean isReverse) {
        Comparator<SettingsGroup> comparator;
        switch (sortBy) {
            default:
                comparator = new Comparator<SettingsGroup>() {
                    @Override
                    public int compare(SettingsGroup a1, SettingsGroup a2) { return String.CASE_INSENSITIVE_ORDER.compare(a1.getGroupName(), a2.getGroupName()); }
                };
                break;
        }

        if (isReverse) {
            final Comparator<SettingsGroup> finalComparator = comparator;
            comparator = new Comparator<SettingsGroup>() {
                @Override
                public int compare(SettingsGroup a1, SettingsGroup a2) { return finalComparator.compare(a2, a1); }
            };
        }

        return comparator;
    }
}
