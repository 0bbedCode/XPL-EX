package eu.faircode.xlua.x.xlua.settings;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

public class SettingsFactory {
    private static final String TAG = LibUtil.generateTag(SettingsFactory.class);
    private static final String TAG_PARSE_SETTINGS = LibUtil.generateTag(SettingsFactory.class, "parseSettings");
    private static final String TAG_PARSE_SETTING = LibUtil.generateTag(SettingsFactory.class, "parseSetting");
    private static final String TAG_FINISH = LibUtil.generateTag(SettingsFactory.class, "finish");
    private static final String TAG_ENSURE_CONTAINER = LibUtil.generateTag(SettingsFactory.class, "ensureContainerIsCreated");

    /*
        [1] Invoke "getSettings" Command return "List<SettingPacket>"
        [2] Pass result to here, Organize them into Containers etc

            some.cool.setting[1,2]
                some.cool.setting.1
                some.cool.setting.2

        Look into Name Transformation, for some reason it renames stuff like "some.cool.setting.1" to "some.cool.setting.one"
        Ew it looks ugly, so ensure it does not remap the Numeric ending
        It shows up in the Database with its Numeric Value, just Renamed from NameInformation object
        Take that back it does rename the numeric ending but for the "Nice Name"

        Used Shared Registry here to determine INIT state of a Setting to Container

     */


    //private final SharedRegistry sharedRegistry = new SharedRegistry();
    //private final WeakHashMap<String, SettingsGroup> groups = new WeakHashMap<>();

    private final WeakHashMap<String, SettingsContainer> containers = new WeakHashMap<>();
    private final WeakHashMap<String, SettingHolder> settings = new WeakHashMap<>();

    private final WeakHashMap<String, SettingsContainer> containerMap = new WeakHashMap<>();

    private final WeakHashMap<String, SettingHolder> limboSettings = new WeakHashMap<>();
    private final WeakHashMap<String, SettingHolder> awaitingContainer = new WeakHashMap<>();

    public List<SettingsContainer> getContainers() { return new ArrayList<>(containers.values()); }

    public void parseSettings(List<SettingPacket> settings) {
        if(!ListUtil.isValid(settings)) {
            Log.e(TAG_PARSE_SETTINGS, "Error Input Settings List is Null or Empty! Count=" + ListUtil.size(settings) + " Stack=" + RuntimeUtils.getStackTraceSafeString(new Throwable()));
            return;
        }

        if(DebugUtil.isDebug())
            Log.d(TAG_PARSE_SETTINGS, "Parsing List of Setting Packets! Count=" + settings.size());

        for(SettingPacket setting : settings)
            parseSetting(setting);

        if(DebugUtil.isDebug())
            Log.d(TAG_PARSE_SETTINGS, Str.fm("Finished parsing List of Setting Packets! Original Count=[%s] Containers Count [%s] Settings Count [%s] Limbo Count [%s]", settings.size(), containers.size(), this.settings.size(), limboSettings.size()));
    }


    public void parseSetting(SettingPacket setting) {
        if(setting == null || Str.isEmpty(setting.name)) {
            Log.e(TAG_PARSE_SETTING, Str.fm("Critical error, Name or Setting Packet object is Null or Empty! Setting=[%s] Containers Count [%s] Settings Count [%s] Limbo Count [%s]", Str.noNL(Str.toStringOrNull(setting)), containers.size(), settings.size(), limboSettings.size()));
            return;
        }

        NameInformation nameInformation = NameInformation.create(setting.name);
        if(nameInformation.kind == NameInformationKind.UNKNOWN) {
            Log.e(TAG_PARSE_SETTING, "Critical error Name Information was Unknown, Name=" + setting.name);
            return;
        }

        if(settings.containsKey(setting.name))
            Log.w(TAG_PARSE_SETTING, Str.fm("Critical Warning! Some how Setting [%s] that is now being Parsed exists in the already Parsed Cache List! Final cache list is for COMPLETE settings!", setting.name));

        SettingsContainer container = ensureContainerIsCreated(nameInformation);
        if(container == null) {
            //Most likely this is a Child Setting in need of its Container, but its Container was not found so put into a awaiting list
            //if(limboSettings.containsKey(nameInformation.name))
            //    Log.w(TAG_PARSE_SETTING, Str.fm("Critical Warning! Some how Setting [%s] is in Limbo ? yet it lacks a Container [%s] Kind [%s] ? ", nameInformation.name, nameInformation.getContainerName(), nameInformation.kind.name()));
            SettingHolder holder = limboSettings.remove(setting.name);
            if(holder == null)
                holder = new SettingHolder(nameInformation, setting.value, setting.description);

            awaitingContainer.put(setting.name, holder);
            if(DebugUtil.isDebug())
                Log.d(TAG_PARSE_SETTING, Str.fm("Setting [%s] is lacking a Container (it requires a container) Pushed to await list! Await Count [%s] Kind [%s]", nameInformation.name, awaitingContainer.size(), nameInformation.kind.name()));

        } else {
            if(!nameInformation.hasChildren()) {
                if(nameInformation.kind == NameInformationKind.SINGLE_NO_PARENT) {
                    //Setting is just a Single Setting, No Parent No Children like "android.device.brand" so it will be a Container it self
                    SettingHolder holder = new SettingHolder(nameInformation, setting.value, setting.description);
                    settings.put(setting.name, holder);
                    container.consumeSingleSetting(holder);
                    if(DebugUtil.isDebug())
                        Log.d(TAG_PARSE_SETTING, Str.fm("Pushed Single (no parent no children) Setting [%s] to Settings Cache List, Count [%s], Setting=[%s]", nameInformation.name, settings.size(), Str.noNL(Str.toStringOrNull(holder))));
                } else {
                    //It would be in limbo because the Container is already initialized and now is waiting to populate it's child settings
                    //So check limbo if it exists, if so remove it from limbo, ensure its updated to its container then push it to completed settings list
                    //Basically if Container is Not Null it should have already been put in limbo
                    SettingHolder holder = limboSettings.remove(setting.name);
                    if(holder == null) {
                        Log.w(TAG_PARSE_SETTING, Str.fm("Weird Error, Setting [%s] Does not exist in Limbo yet the container [%s] was already created ? Limbo Count [%s] Container Children [%s]", setting.name, container.getName(), limboSettings.size(), Str.joinList(container.getAllNames())));
                        return;
                    }

                    container.updateChild(holder, setting);
                    settings.put(setting.name, holder);
                    if(DebugUtil.isDebug())
                        Log.d(TAG_PARSE_SETTING, Str.fm("Found child setting [%s] in Limbo ! Populated setting [%s] Container Name [%s] Limbo Count [%s]", setting.name, Str.noNL(Str.toStringOrNull(holder)), container.getName(), limboSettings.size()));
                }
            } else {
                if(DebugUtil.isDebug())
                    Log.d(TAG_PARSE_SETTING, Str.fm("Parsing Setting [%s] Container Children Names! Names=[%s]", setting.name, Str.joinList(nameInformation.childrenNames())));

                for(NameInformation childNameInformation : nameInformation.getChildrenNames()) {
                    SettingHolder holder = awaitingContainer.remove(childNameInformation.name);
                    if(holder == null) {
                        holder = new SettingHolder(childNameInformation, null, setting.description);
                        limboSettings.put(childNameInformation.name, holder);
                        if(DebugUtil.isDebug())
                            Log.d(TAG_PARSE_SETTING, Str.fm("Child Setting [%s] for Parent [%s] is not in the Awaiting list, assuming it still has not been parsed or does not exist as a value! Pushed to Limbo... Limbo Count [%s]", childNameInformation.name, setting.name, limboSettings.size()));

                    } else {
                        container.updateChild(holder, null);
                        settings.put(childNameInformation.name, holder);
                        if(DebugUtil.isDebug())
                            Log.d(TAG_PARSE_SETTING, Str.fm("Child Setting [%s] was found awaiting its Container [%s]", childNameInformation.name, container.getName()));
                    }
                }
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG_PARSE_SETTING, Str.fm("Finished Parsing Setting [%s]! Name Information [%s], Containers Count [%s] Settings Count [%s] Awaiting Containers Count [%s] Limbo Count [%s]", setting.name, Str.noNL(Str.toStringOrNull(nameInformation)), containers.size(), settings.size(), awaitingContainer.size(), limboSettings.size()));
    }

    public SettingsContainer ensureContainerIsCreated(NameInformation nameInformation) {
        if(nameInformation == null || nameInformation.kind == NameInformationKind.UNKNOWN) {
            Log.e(TAG_ENSURE_CONTAINER, "Error ensuring Container is Created for Name information! Name Info=" + Str.toStringOrNull(nameInformation));
            return null;
        }

        if(nameInformation.kind != NameInformationKind.CHILD_HAS_PARENT) {
            String containerName = nameInformation.getContainerName();
            SettingsContainer container = containers.get(containerName);
            if(container == null) {
                if(DebugUtil.isDebug())
                    Log.d(TAG_ENSURE_CONTAINER, Str.fm("Creating Container for Setting [%s] Container Name [%s]", nameInformation.name, containerName));

                container = new SettingsContainer(nameInformation, containerName);
                containers.put(containerName, container);

                //containerMap.put(nameInformation.name, container);  //We don't need to do this FYI ... if we have too then the parsing logic code is bad !
                if(nameInformation.hasChildren())
                    for(String childName : nameInformation.childrenNames())
                        containerMap.put(childName, container);             //Append Children to map, so we can help children to its parent
            }

            return container;
        }

        //For numeric Ending Settings like "cool.setting.1"
        SettingsContainer mappedContainer = containerMap.get(nameInformation.name);
        if(mappedContainer == null) {
            if(DebugUtil.isDebug())
                Log.d(TAG_ENSURE_CONTAINER, Str.fm("Child Setting [%s] expecting a Container / Parent lacks a Parent! Awaiting Count [%s]", nameInformation.name, awaitingContainer.size()));
        }

        return mappedContainer;
    }

    public void finish() {
        if(DebugUtil.isDebug())
            Log.d(TAG_FINISH, Str.fm("Cleaning up / Finalizing internal Containers & Settings List! Containers Count [%s] Settings Count [%s] Awaiting Count [%s] Limbo Count [%s]", containers.size(), settings.size(), awaitingContainer.size(), limboSettings.size()));

        if(!limboSettings.isEmpty()) {
            for(Map.Entry<String, SettingHolder> entry : limboSettings.entrySet()) {
                String name = entry.getKey();
                SettingHolder holder = entry.getValue();
                if(Str.isEmpty(name) || holder == null || Str.isEmpty(holder.getName())) {
                    Log.e(TAG_FINISH, Str.fm("Critical Error! Some how a Setting made it into Cache with either Key or Value or Name being Null or Empty! Name [%s] Holder= [%s]", Str.toStringOrNull(name), Str.noNL(Str.toStringOrNull(holder))));
                    continue;
                }

                NameInformation nameInformation = holder.getNameInformation();
                if(nameInformation == null) {
                    Log.e(TAG_FINISH, Str.fm("Critical Error! Some how Name information for the Setting is Null! Name [%s] Holder [%s]", name, Str.noNL(Str.toStringOrNull(holder))));
                    continue;
                }

                if(nameInformation.hasChildren()) {
                    Log.w(TAG_FINISH, Str.fm("Weird, Setting in Setting cache List is identified as a Parent Container ? why is it in the Settings list... Name [%s] Container Name [%s] Kind [%s] Info=%s", name, nameInformation.getContainerName(), nameInformation.kind.name(), Str.toStringOrNull(nameInformation)));
                    continue;
                }

                SettingsContainer container = containerMap.get(name);
                if(container == null) {
                    //This can be the Case if the Setting ends with a Numeric Number like "soc.cpu.instruction.set.64"
                    if(DebugUtil.isDebug())
                        Log.w(TAG_FINISH, Str.fm("Found a Setting [%s] in Limbo without a Container! It will be assumed its a Single Setting (no container no children) that ends with a Numeric chars! Creating its own Container! Info=", name, Str.toStringOrNull(nameInformation)));

                    nameInformation.kind = NameInformationKind.SINGLE_NO_PARENT;
                    nameInformation.index = 0;
                    container = new SettingsContainer(holder);

                    containers.put(name, container);
                    containerMap.put(name, container);
                    settings.put(name, holder);
                    continue;
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG_FINISH, Str.fm("Ensuring Child Setting [%s] in Limbo is linked with its Parent Container [%s] Name Info [%s]. It has no actual value hence why it's in limbo!", name, container.getName(), Str.noNL(Str.toStringOrNull(nameInformation))));

                container.updateChild(holder, null);
                settings.put(name, holder);
            }

            limboSettings.clear();
        }

        if(DebugUtil.isDebug())
            Log.d(TAG_FINISH, Str.fm("Finished pairing Limbo Settings to its Containers! Settings Count [%s] now Checking awaiting Container List (should be empty) Count=[%s]", settings.size(), awaitingContainer.size()));

        if(!awaitingContainer.isEmpty()) {
            if(DebugUtil.isDebug())
                Log.w(TAG_FINISH, Str.fm("Warning, Awaiting Container list is not Empty! Count [%s] Somehow it's Containers were never initialized ? Containers Count [%s] Settings Count [%s]", awaitingContainer.size(), containers.size(), settings.size()));

            for(Map.Entry<String, SettingHolder> entry : awaitingContainer.entrySet()) {
                String name = entry.getKey();
                SettingHolder holder = entry.getValue();

                SettingsContainer container = containerMap.get(name);
                if(DebugUtil.isDebug())
                    Log.w(TAG_FINISH, Str.fm("Error no Container Setting [%s] Holder [%s] Container if got [%s] ", name, Str.noNL(Str.toStringOrNull(holder)), Str.noNL(Str.toStringOrNull(container))));

                if(container != null) {
                    container.updateChild(holder, null);
                    settings.put(name, holder);
                }
            }

            awaitingContainer.clear();
        }

        if(DebugUtil.isDebug())
            Log.d(TAG_FINISH, Str.fm("Finished Parsing Settings into Containers etc! Container Count [%s] Settings Count [%s]", containers.size(), settings.size()));
    }
}























