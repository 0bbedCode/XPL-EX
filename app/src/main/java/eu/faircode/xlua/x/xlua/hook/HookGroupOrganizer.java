package eu.faircode.xlua.x.xlua.hook;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.ui.GroupHelper;
import eu.faircode.xlua.ui.HookWarnings;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.xlua.commands.query.GetHooksCommand;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

public class HookGroupOrganizer {
    private static final String TAG = "XLua.HookGroupOrganizer";

    public final Map<String, HookGroup> groups = new WeakHashMap<>();

    public List<XLuaHook> getHooksForSettings(List<String> setting_names) {
        List<String> lower_names = ListUtil.toLowerCase(setting_names);
        List<XLuaHook> hooks = new ArrayList<>();
        if(!ListUtil.isValid(lower_names) || groups.isEmpty())
            return hooks;

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Getting Hooks for Settings=[%s] UID=%s  PKG=%s", Str.joinList(lower_names), "null", "null"));

        for(Map.Entry<String, HookGroup> entry : groups.entrySet()) {
            //String groupName = entry.getKey();
            HookGroup group = entry.getValue();
            for(XLuaHook hook : group.hooks) {
                String[] hook_settings = hook.getSettings();
                boolean found = false;
                if(ArrayUtils.isValid(hook_settings)) {
                    for(String hook_setting : hook_settings) {
                        //String cleaned = Str.trimEx(hook_setting.toLowerCase());
                        if(TextUtils.isEmpty(hook_setting))
                            continue;

                        String lowered = hook_setting.toLowerCase();
                        if(lower_names.contains(lowered)) {
                            hooks.add(hook);
                            if(DebugUtil.isDebug())
                                Log.d(TAG, Str.fm("Found Possible Hook to Setting by Settings! Setting Count=%s Hook Name=%s Hook ID=%s  UID=%s  Package Name=%s", setting_names.size(), hook.getName(), hook.getId(), "null", "null"));

                            found = true;
                            break;
                        }
                    }
                }

                if(!found && !TextUtils.isEmpty(hook.getLuaScript())) {
                    String script = hook.getLuaScript().toLowerCase();
                    for(String setting_name : lower_names) {
                        String format_1 = "\"" + setting_name + "\"";
                        String format_2 = "'" + setting_name + "'";
                        if(script.contains(format_1) || script.contains(format_2)) {
                            hooks.add(hook);
                            if(DebugUtil.isDebug())
                                Log.d(TAG, Str.fm("Found Possible Hook to Setting by Lua Script! Setting Name=%s Hook Name=%s Hook ID=%s  UID=%s  Package Name=%s", setting_name, hook.getName(), hook.getId(), "null", "null"));

                            break;
                        }
                    }
                }
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Finished Filtering the Hooks for App UID:%s  Package Name=%s   Hooks Count=%s", "null", "null", hooks.size()));

        return hooks;
    }

    public void initGroups(List<XLuaHook> hooks, Context context) {
        groups.clear();
        for(XLuaHook hook : hooks) {
            HookGroup group = groups.get(hook.getGroup());
            if(group == null) {
                group = new HookGroup();
                groups.put(hook.getGroup(), group);

                Resources resources = context.getResources();
                String name = hook.getGroup().toLowerCase().replaceAll("[^a-z]", "_");
                group.resourceId = resources.getIdentifier("group_" + name, "string", context.getPackageName());
                group.name = hook.getGroup();
                group.title = (group.resourceId > 0 ? resources.getString(group.resourceId) : hook.getGroup());
                group.groupId = GroupHelper.getGroupId(group.name);
                group.hasWarning = HookWarnings.hasWarning(context, group.name);

                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Created Group for Hook:%s  Group Name=%s UID=%s Package Name=%s", hook.getId(), hook.getGroup(), "null", "null"));
            }

            group.hooks.add(hook);
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Initializing the Hook Groups Usage Stats for App=%s  Total Hook Count=%s  Group Count=%s", "null", ListUtil.size(hooks), groups.size()));

    }

    public void collectApp(AppXpPacket app, List<XLuaHook> hooks, Context context, SharedRegistry viewRegistry) {
        initGroups(hooks, context);
        //Init assignments
        if(viewRegistry != null) {
            for(AssignmentPacket assignment : app.assignments) {
                if (assignment == null) {
                    Log.e(TAG, "NULL");
                    continue;
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG, "St=" + Str.toStringOrNull(assignment));

                //Clear old ? if old is cached but not updated ??
                viewRegistry.setChecked(SharedRegistry.STATE_TAG_HOOKS, assignment.hook, true);
            }
        }

        /*for(String groupId : groups.keySet()) {
            for(AssignmentPacket assignment : app.assignments) {
                if(assignment == null) {
                    Log.e(TAG, "NULL");
                    continue;
                }

                Log.d(TAG, "St=" + Str.toStringOrNull(assignment));
                viewRegistry.setChecked(ViewStateRegistry.STATE_TAG_HOOKS, assignment.hook, true);
                //if(groupId.equalsIgnoreCase(assignment.gr))
                //if(viewRegistry != null)
                //    viewRegistry.setChecked(ViewStateRegistry.STATE_TAG_HOOKS, assignment.hookObj.getId(), has);

                //String g = !TextUtils.isEmpty(assignment.hook);

                boolean has = assignment.hookObj.getGroup().equals(groupId);
                if(has) {
                    Group group = groups.get(groupId);
                    if(group == null)
                        continue;

                    if (assignment.exception != null)
                        group.exception = true;
                    if (assignment.installed >= 0)
                        group.installed++;
                    if (assignment.hookObj.isOptional())
                        group.optional++;
                    if (assignment.restricted)
                        group.used = Math.max(group.used, assignment.used);

                    group.assigned++;
                }

                if(viewRegistry != null)
                    viewRegistry.setChecked(ViewStateRegistry.STATE_TAG_HOOKS, assignment.hookObj.getId(), has);
            }
        }*/
    }

    public static List<String> getHookIdsFromSettingPackets(Context context, List<SettingPacket> settings) {
        List<String> hooks = new ArrayList<>();
        HookGroupOrganizer organizer = new HookGroupOrganizer();

        List<XLuaHook> allHooks =  GetHooksCommand.getHooks(context, true, false);
        organizer.initGroups(allHooks, context);
        List<String> allSettings = new ArrayList<>();
        for(SettingPacket setting : settings)
            if(!allSettings.contains(setting.name))
                allSettings.add(setting.name);

        if(DebugUtil.isDebug())
            Log.d(TAG, "Getting Hooks Ids from Settings, Settings Count=" + ListUtil.size(settings) + " Hooks Count=" + ListUtil.size(allHooks) + " All Settings String List Count=" + ListUtil.size(allSettings));

        List<XLuaHook> filteredHooks = organizer.getHooksForSettings(allSettings);
        if(DebugUtil.isDebug())
            Log.d(TAG, "Settings List String Count=" + ListUtil.size(allSettings) + " All Hooks Count=" + ListUtil.size(allHooks) + " Filtered Hooks Count=" + ListUtil.size(filteredHooks));

        for(XLuaHook hook : filteredHooks) {
            String id = hook.getId();
            if(!TextUtils.isEmpty(id))
                continue;

            if(!hooks.contains(id)) {
                hooks.add(id);
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Now Returning! Settings List String Count=" + ListUtil.size(allSettings) + " All Hooks Count=" + ListUtil.size(allHooks) + " Filtered Hooks Count=" + ListUtil.size(filteredHooks) + " String List of Hook Ids Count=" + ListUtil.size(hooks));


        return hooks;
    }
}
