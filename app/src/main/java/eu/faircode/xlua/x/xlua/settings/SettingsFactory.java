package eu.faircode.xlua.x.xlua.settings;

import android.content.Context;
import android.os.Process;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetHooksCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetSettingsExCommand;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.database.wrappers.GlobalDatabaseResolver;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

public class SettingsFactory {
    private static final String TAG = LibUtil.generateTag(SettingsFactory.class);

    public static SettingsFactory create() { return new SettingsFactory(); }

    private final WeakHashMap<String, SettingsContainer> containers = new WeakHashMap<>();
    private final WeakHashMap<String, SettingHolder> settings = new WeakHashMap<>();

    public List<SettingsContainer> getContainers() { return new ArrayList<>(containers.values()); }

    public static final List<String> BAD_PREFIX_NAMES = Arrays.asList("intercept.", "java.", "qemu.", "hide.");
    public static final List<String> BAD_NAMES = Arrays.asList(
            "account.user.serial",
            "account.user.name",
            "lac.cid",
            "value.meid",
            "value.imei",
            "value.email",
            "lac,cid",
            "apps.sync.times.bool",
            "cpu.cpuid",
            "file.last.modified.offset",
            "file.time.access.offset",
            "file.time.created.offset",
            "file.time.modify.offset",
            "google.client.id.base",
            "google.client.id.base.name",
            "google.gms.version",
            "hide.emulator.bool",
            "hide.root.bool",
            "analytics.firebase.instance.id",
            "unique.netd.secret.key",
            "unique.gsm.sim.serial",
            "settings.xiaomi.op.security.uuid");

    public SettingsFactory initialize(Context context, UserClientAppContext userContext, boolean joinSettingsFromHooks) {
        try {
            if(ObjectUtils.anyNull(context, userContext))
                throw new Exception("One of the Given Params Appear Null!");

            List<SettingPacket> settings = GetSettingsExCommand.get(context, true, userContext.appUid, userContext.appPackageName, GetSettingsExCommand.FLAG_ONE);
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Initializing Settings, from a Count of (%s) Join Settings From Hooks (%s)",
                        ListUtil.size(settings),
                        joinSettingsFromHooks));

            parseSettings(
                    joinSettingsFromHooks ?
                    joinHookDefinedSettings(context, settings, userContext) :
                    settings);
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Finish Initializing Settings from a Count of (%s), Now Total Settings Parsed (%s) with Total Container Count (%s)",
                        ListUtil.size(settings),
                        this.settings.size(),
                        this.containers.size()));

            return this;
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to Initialize Settings Factory! Context Is Null [%s] User App Context Is Null [%s] Settings Count (%s) Containers Count (%s) Error=%s",
                    ObjectUtils.objectValidity(context),
                    ObjectUtils.objectValidity(userContext),
                    settings.size(),
                    containers.size(),
                    e));

            return this;
        }
    }


    public static boolean isBad(String settingName) {
        String lowered = Str.toLowerCase(settingName);
        if(BAD_NAMES.contains(lowered))
            return true;

        for(String bad : BAD_PREFIX_NAMES) {
            if(lowered.startsWith(bad))
                return true;
        }

        return false;
    }

    public void parseSettings(List<SettingPacket> settings) {
        if(!ListUtil.isValid(settings)) {
            Log.e(TAG,
                    Str.fm("Failed Parsing Settings! Input Settings Were Null or Empty! Count=%s Stack=%s",
                    ListUtil.size(settings),
                    RuntimeUtils.getStackTraceSafeString(new Exception())));
            return;
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Parsing (%s) Setting Packets! Good Luck...", ListUtil.size(settings)));

        int unknown = 0;
        Map<String, Pair<SettingPacket, NameInformation>> parents = new HashMap<>();
        Map<String, Pair<SettingPacket, NameInformation>> children = new HashMap<>();
        Map<String, Pair<SettingPacket, NameInformation>> single = new HashMap<>();
        for(SettingPacket setting : settings) {
            NameInformation nameInformation = NameInformation.create(setting.name);
            if(isBad(setting.name))
                continue;

            switch (nameInformation.kind) {
                case PARENT_HAS_CHILDREN_IS_CONTAINER:
                    appendMap(parents, setting, nameInformation);
                    break;
                case SINGLE_NO_PARENT:
                    appendMap(single, setting, nameInformation);
                    break;
                case CHILD_HAS_PARENT:
                    appendMap(children, setting, nameInformation);
                    break;
                default:
                    unknown++;
                    Log.e(TAG, Str.fm("Failed to Identify Name Kind for (%s) NameInformation Name (%s). Total Count So far Parsed for Parents (%s) Children (%s) Singles (%s) Unknown Count=%s",
                            setting.name,
                            nameInformation.name,
                            parents.size(),
                            children.size(),
                            single.size(),
                            unknown));
                            break;
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Sorted Settings from Count (%s) to (%s) Parents (%s) Children (%) Singles and Unknown (%s). Now Organizing Parents first!",
                    settings.size(),
                    parents.size(),
                    children.size(),
                    single.size(),
                    unknown));

        if(!parents.isEmpty()) {
            for(String name : new ArrayList<>(parents.keySet())) {
                Pair<SettingPacket, NameInformation> pair = parents.get(name);
                if(pair == null || (pair.first == null || pair.second == null)) {
                    if (DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Parent Pair Information for [%s] Is Null or Bad Pairs! Parent Count=%s Children Count=%s Single Count=%s",
                                name,
                                parents.size(),
                                children.size(),
                                single.size()));
                    continue;
                }

                SettingPacket packet = pair.first;
                NameInformation nameInformation = pair.second;

                String containerName = nameInformation.getContainerName();
                SettingsContainer container = new SettingsContainer(nameInformation, containerName, false);
                container.ensureDescription(packet.description);
                containers.put(containerName, container);

                if(single.containsKey(containerName)) {
                    Pair<SettingPacket, NameInformation> singlePair = single.remove(containerName);
                    if(singlePair != null && (singlePair.first != null)) {
                        container.ensureDescription(singlePair.first.description);
                        if(DebugUtil.isDebug())
                            Log.d(TAG, Str.fm("Found a Single Setting [%s][%s] from [%s] Parent Name, Ensured the Descriptions are Merged if not! Total Single Count (%s) & Removed the Single",
                                    containerName,
                                    name,
                                    packet.name,
                                    single.size()));
                    }
                }

                List<NameInformation> childrenInformation = nameInformation.childrenNameInformation;
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Parsing Parent Container [%s][%s][%s] Children Information Count (%s) with a Total of [%s] limbo Children, Total Settings Count (%s), Total Containers Count (%s)",
                            containerName,
                            packet.name,
                            name,
                            ListUtil.size(childrenInformation),
                            children.size(),
                            this.settings.size(),
                            containers.size()));

                if(ListUtil.isValid(childrenInformation)) {
                    for(NameInformation childInformation : childrenInformation) {
                        Pair<SettingPacket, NameInformation> childPair = children.remove(childInformation.name);
                        if(childPair == null) {
                            SettingHolder childHolder = new SettingHolder(childInformation, null, packet.description);
                            container.pushChild(childHolder, false);
                            this.settings.put(childHolder.getName(), childHolder);
                            if(DebugUtil.isDebug())
                                Log.d(TAG, Str.fm("Child [%s][%s] under Index [%s] for Parent Container [%s][%s][%s] did Not Exist in Children List and was Created! Total Children Count (%s) Total Settings Count (%s)",
                                        childHolder.getName(),
                                        childInformation.name,
                                        childHolder.getIndex(),
                                        containerName,
                                        packet.name,
                                        name,
                                        children.size(),
                                        this.settings.size()));
                        } else {
                            SettingPacket childPacket = childPair.first;
                            SettingHolder childHolder = new SettingHolder(childInformation, childPacket.value, childPacket.description);
                            container.pushChild(childHolder, true);
                            this.settings.put(childPacket.name, childHolder);
                            if(DebugUtil.isDebug())
                                Log.d(TAG, Str.fm("Child Packet [%s][%s] existed for Parent Container [%s][%s][%s] Child Index [%s] and was Removed from Children List! Total Children Count (%s) Total Settings Count (%s)",
                                        childPacket.name,
                                        childHolder.getName(),
                                        containerName,
                                        packet.name,
                                        name,
                                        childHolder.getIndex(),
                                        children.size(),
                                        this.settings.size()));
                        }
                    }
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Finish Initializing and Creating Container [%s][%s][%s] From Parent [%s][%s] Total Container Count (%s) Total Children Count (%s) Total Settings Count (%s)",
                            container.getName(),
                            containerName,
                            name,
                            packet.name,
                            containers.size(),
                            children.size(),
                            this.settings.size()));
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Finished Parsing Initializing (%s) Containers from (%s) Settings to a Total of (%s) Settings with a Remaining of (%s) Child Settings and (%s) Single Settings! Cleaning Up!",
                    this.containers.size(),
                    settings.size(),
                    this.settings.size(),
                    children.size(),
                    single.size()));

        if(!single.isEmpty()) {
            for(String singleName : new ArrayList<>(single.keySet())) {
                if(!Str.isEmpty(singleName)) {
                    Pair<SettingPacket, NameInformation> singlePair = single.remove(singleName);
                    if(singlePair == null || (singlePair.first == null || singlePair.second == null)) {
                        Log.w(TAG, Str.fm("Single (%s) has Invalid Pair Entry (%s) or Items in the Pair Entry Appear Invalid!. Single Count (%s) Settings Count (%s) Containers Count (%s)",
                                singleName,
                                ObjectUtils.objectValidity(singlePair),
                                single.size(),
                                containers.size()));
                        continue;
                    }

                    SettingPacket packet = singlePair.first;
                    NameInformation nameInformation = singlePair.second;
                    if(nameInformation.kind == NameInformationKind.UNKNOWN) {
                        Log.w(TAG, Str.fm("Bad Single Setting (%s) Packet (%s) Name Information (%s), Appears Unknown Name Information!",
                                singleName,
                                packet.name,
                                nameInformation.name));
                        continue;
                    }

                    String containerName = nameInformation.getContainerName();
                    SettingHolder holder = this.settings.get(containerName);
                    if(holder == null) {
                        holder = new SettingHolder(nameInformation, packet.value, packet.description);
                        this.settings.put(containerName, holder);
                    } else {
                        Log.e(TAG, Str.fm("Error! Some how Setting Holder [%s] Exists from Container Name [%s] with Packet Name [%s] and NameInformation Name [%s] Single Name [%s] Single Count (%s) Settings Count (%s) Container Count (%s)",
                                holder.getName(),
                                containerName,
                                packet.name,
                                nameInformation.name,
                                singleName,
                                single.size(),
                                this.settings.size(),
                                containers.size()));
                        continue;
                    }

                    SettingsContainer container = this.containers.get(containerName);
                    if(container == null) {
                        container = new SettingsContainer(holder);
                        this.containers.put(containerName, container);
                    } else {
                        Log.e(TAG, Str.fm("Error! Some how Setting Holder [%s] Container (%s) with Packet Name [%s] and NameInformation Name [%s] Single Name [%s] Single Count (%s) Settings Count (%s) Container Count (%s)",
                                holder.getName(),
                                containerName,
                                packet.name,
                                nameInformation.name,
                                singleName,
                                single.size(),
                                this.settings.size(),
                                containers.size()));
                        continue;
                    }

                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Pushed Singe Setting! Name [%s] Packet Name [%s] Container Name [%s] Name Information Name [%s]. Total Settings Count (%s) Single Count (%s) Container Count (%s)",
                                singleName,
                                packet.name,
                                containerName,
                                nameInformation.name,
                                this.settings.size(),
                                single.size(),
                                this.containers.size()));
                }
            }
        }

        //Now Finally Remove Settings, or ReMap them
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Finished Parsing (%s) Settings ending in a Total of (%s) Settings Parsed, with (%s) Containers. The Remaining Children Count (%s) with a Remaining Single Count (%s) Remaining (%s) Parents! Unknown Settings (%s)",
                    settings.size(),
                    this.settings.size(),
                    this.containers.size(),
                    children.size(),
                    single.size(),
                    parents.size(),
                    unknown));
    }

    public void appendMap(Map<String, Pair<SettingPacket, NameInformation>> map, SettingPacket packet, NameInformation nameInformation) {
        if(map != null && packet != null && nameInformation != null && nameInformation.kind != NameInformationKind.UNKNOWN) {
            Pair<SettingPacket, NameInformation> pair = map.get(packet.name);
            if(pair != null) {
                if(pair.first.consume(packet))
                    map.put(packet.name, Pair.create(packet, nameInformation));
            } else {
                map.put(packet.name, Pair.create(packet, nameInformation));
            }
        }
    }

    public List<SettingPacket> joinHookDefinedSettings(Context context, List<SettingPacket> original, UserClientAppContext app) {
        if(context == null || original == null || app == null)
            return ListUtil.emptyList();

        LinkedHashMap<String, SettingPacket> current = new LinkedHashMap<>();
        if(ListUtil.isValid(original)) {
            for(SettingPacket setting : original) {
                if(setting != null && !Str.isEmpty(setting.name)) {
                    current.put(setting.name, setting);
                }
            }
        }

        //ToDo: Try to tie this into the core for hooks & settings
        List<XHook> all = GetHooksCommand.getHooks(context, true, true);
        List<String> collections = GetSettingExCommand.getCollections(context, Process.myUid());
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Getting Hooks Settings, " + "Database Settings Count=%s Current Settings Count=%s  Hooks Count=%s Collections Count=%s Collections=[%s]",
                    ListUtil.size(original),
                    current.size(),
                    ListUtil.size(all),
                    ListUtil.size(collections),
                    Str.joinList(collections)));

        if(ListUtil.isValid(all)) {
            for(XHook hook : all) {
                TryRun.silent(() -> {
                    //ToDO: sync this with the assignments part ?
                    if(isGood(hook, collections, app.appPackageName)) {
                        for(String setting : hook.settings) {
                            if(Str.isEmpty(setting))
                                continue;

                            //Properly resolve the name for the setting hooks
                            //This is a patch, in reality we need to actually update the hooks etc
                            //Also need to update system so the containers house all the subjects
                            //Right now if it has "cell.item" it will contain "cell.item" and "cell.item.2" but "cell.item.1" will be its own "[1]"
                            String trimmed = GlobalDatabaseResolver.resolveName(context, Str.trimOriginal(setting));
                            if(Str.isEmpty(trimmed))
                                continue;

                            if(isBad(trimmed))
                                continue;

                            SettingPacket packet = current.get(trimmed);
                            if(packet == null) {
                                packet = SettingPacket.create(trimmed, null, app, ActionPacket.create(ActionFlag.PUSH, false));
                                current.put(packet.name, packet);
                                if(DebugUtil.isDebug())
                                    Log.d(TAG, Str.fm("Setting [%s][%s] from Hook [%s] is not Defined in the Current List of Existing Settings. Created and now Appended to List Count (%s)",
                                            setting,
                                            trimmed,
                                            hook.getObjectId(),
                                            current.size()));
                            }
                        }
                    }
                });
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Finished Mapping Settings Defined in Hooks!, Database Settings Count=%s Current Settings Count=%s  Hooks Count=%s Collections Count=%s Collections=[%s]",
                    ListUtil.size(original),
                    current.size(),
                    ListUtil.size(all),
                    ListUtil.size(collections),
                    Str.joinList(collections)));

        return ListUtil.copyToArrayList(current.values());
    }



    //ToDo: Put this somewhere clever nice, utils maybe ?
    public static boolean isGood(XHook hook, List<String> collections, String pkgName) {
        if(hook == null)
            return false;

        String id = hook.getObjectId();
        if(Str.isEmpty(id))
            return false;

        if(!hook.isAvailable(pkgName, collections)) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Hook is not Available (1): " + id);

            return false;
        }

        if(Str.isEmpty(hook.group))
            return false;

        if(hook.group.toLowerCase().startsWith("intercept.")) {
            return false;
        }

        return ListUtil.isValid(hook.settings);
        //return ArrayUtils.isValid(hook.getSettings());
    }

    public static boolean isGoodSetting(SettingPacket packet) {
        if(packet  == null || Str.isEmpty(packet.name))
            return false;

        String lowName = packet.name.toLowerCase().trim();
        if(BAD_NAMES.contains(lowName))
            return false;

        for(String w : BAD_PREFIX_NAMES) {
            if(lowName.startsWith(w))
                return false;
        }

        return true;
    }

}























