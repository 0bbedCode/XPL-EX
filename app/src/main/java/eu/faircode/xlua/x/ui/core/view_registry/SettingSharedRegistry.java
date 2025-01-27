package eu.faircode.xlua.x.ui.core.view_registry;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.call.GetAppInfoCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetHooksCommand;
import eu.faircode.xlua.x.xlua.hook.AppXpPacket;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.hook.HookGroupOrganizer;
import eu.faircode.xlua.x.xlua.hook.data.AssignmentData;
import eu.faircode.xlua.x.xlua.hook.data.AssignmentState;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

/*
     ToDO: This can act as a "repo"
 */
public class SettingSharedRegistry extends SharedRegistry {
    private static final String TAG = LibUtil.generateTag(SettingSharedRegistry.class);

    public static SettingSharedRegistry create() { return new SettingSharedRegistry(); }

    private final Map<String, IRandomizer> randomizers = new WeakHashMap<>();
    private final Map<String, XLuaHook> hooks = new WeakHashMap<>();


    //private final Map<String, SettingsContainer> settings = new WeakHashMap<>();
    private HookGroupOrganizer groups;

    private void doDebugTest() {
        if(DebugUtil.isDebug())
            Log.d(TAG, "Has Randomizer for [unique.bluetooth.address] = " + (randomizers.containsKey("unique.bluetooth.address")));
    }

    public static String getHooksGroupTag(String packageName) { return SharedRegistry.STATE_TAG_HOOKS + "_" + packageName; }
    public static String getAppAssignmentDataTag(String packageName) { return packageName + "_assignment_data"; }
    public static String getAppPacketTag(String packageName) { return packageName + "_packet"; }

    public boolean hasRandomizers() { return !randomizers.isEmpty(); }
    public List<IRandomizer> getRandomizers() {
        doDebugTest();
        return ListUtil.copyToArrayList(randomizers.values());
    }
    public Map<String, IRandomizer> getRandomizersMap() {
        doDebugTest();
        return randomizers;
    }


    public IRandomizer getRandomizer(String settingName) {
        doDebugTest();
        return randomizers.get(settingName);
    }

    public IRandomizer getRandomizer(List<SettingHolder> settings) {
        for(SettingHolder setting : settings) {
            IRandomizer randomizer = getRandomizer(setting.getName());
            if(randomizer != null)
                return randomizer;
        }

        return null;
    }

    public int randomize(List<SettingHolder> settings, Context context) {
        doDebugTest();
        int randomized = 0;
        if(!settings.isEmpty()) {
            RandomizerSessionContext ctx = RandomizerSessionContext.create();
            for(SettingHolder setting : settings) {
                String name = setting.getName();
                IRandomizer randomizer = randomizers.get(name);
                if(randomizer != null) {
                    randomizer.randomize(ctx);
                    randomized++;

                    String newValue = ctx.getValue(name);

                    setting.setNewValue(newValue);
                    setting.ensureUiUpdated(newValue);
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Randomized Setting, Setting=" + name + " New Value=" + newValue + " Old Value=" + setting.getValue());

                    if(context != null)
                        setting.setNameLabelColor(context);
                } else {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Randomizer is Missing from Setting=" + name);
                }
            }
        } else {
            Log.e(TAG, "Error Invalid Input, Settings List if Null or Empty!");
        }

        return randomized;
    }

    public List<SettingHolder> getSettingsForContainer(SettingsContainer container) { return getSettingsForContainer(container, true); }
    public List<SettingHolder> getSettingsForContainer(SettingsContainer container, boolean ensureIsChecked) {
        List<SettingHolder> settings = container.getSettings();
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Settings Count [%s] For Container [%s]", ListUtil.size(settings), container.getContainerName()));

        List<SettingHolder> enabled = new ArrayList<>();
        for(SettingHolder setting : settings)
            if(!ensureIsChecked || isChecked(SharedRegistry.STATE_TAG_SETTINGS, setting.getId()))
                enabled.add(setting);

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Settings Count [%s] For Container [%s] Enabled Count [%s]", ListUtil.size(settings), container.getContainerName(), enabled.size()));

        return enabled.isEmpty() ? settings : enabled;
    }

    public void refresh(Context context) { refresh(context, -1, null); }
    public void refresh(Context context, int uid, String packageName) {
        refreshRandomizers();
        if(uid > -1 && packageName != null && !UserIdentity.GLOBAL_NAMESPACE.equalsIgnoreCase(packageName)) {
            refreshHooks(context);
            initAppAssignments(context, uid, packageName);
        }
    }

    public void initAssignmentDataForSettings(List<String> setting_names, String packageName, AssignmentData assignmentData) {
        String tagIdHooks = getHooksGroupTag(packageName);
        if(assignmentData == null) {
            Log.e(TAG, "Error Assignment Data Struct is NULL!");
            return;
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Getting States for Settings (2), PackageName=%s  Settings Count=%s  Tag Id=%s  Session Id=%s", packageName, setting_names.size(), tagIdHooks, getSessionId()));

        assignmentData.refresh();
        List<XLuaHook> hooksList = groups.getHooksForSettings(setting_names);
        for(XLuaHook hook : hooksList) {
            AssignmentState state = new AssignmentState();
            state.hook = hook;
            state.enabled = isChecked(tagIdHooks, hook.getId());
            assignmentData.addAssignment(state);
        }
    }

    public void refreshRandomizers() {
        try {
            randomizers.clear();
            randomizers.putAll(RandomizersCache.getCopy());
            doDebugTest();
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to Refresh Randomizers, Id=%s, Size=%s Error=%s", getSessionId(), randomizers.size(), e));
        } finally {
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Finished Refreshing Randomizers, Id=%s, Size=%s", getSessionId(), randomizers.size()));
        }
    }

    public void refreshHooks(Context context) {
        try {
            hooks.clear();
            List<XLuaHook> hookList = GetHooksCommand.getHooks(context, true, false);
            for(XLuaHook hook : hookList)
                hooks.put(hook.getId(), hook);

            groups = new HookGroupOrganizer();
            groups.initGroups(hookList, context);
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to Refresh Hooks, Id=%s, Size=%s, Groups Count=%s, Error=%s", getSessionId(), hooks.size(), groups.groups.size(), e));
        } finally {
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Finished Refreshing Hooks, Id=%s, Size=%s, Groups Count=%s", getSessionId(), groups.groups.size(), hooks.size()));
        }
    }

    public void initAppAssignments(Context context, int uid, String packageName) { initAppAssignments(GetAppInfoCommand.get(context, uid, packageName)); }
    public void initAppAssignments(AppXpPacket app) {
        if(app != null) {
            try {
                String tagId1 = getHooksGroupTag(app.packageName);
                String tagId2 = getAppPacketTag(app.packageName);
                String tagId3 = getAppAssignmentDataTag(app.packageName);
                pushSharedObject(tagId2, app);

                clearTag(tagId1);
                pushSharedObject(tagId3, null);
                for(AssignmentPacket assignment : app.assignments)
                    setChecked(tagId1, assignment.hook, true);

                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Init App Assignments Finished, Tag(1)=%s  Tag(2)=%s Tag(3)=%s  Packet=%s", tagId1, tagId2, tagId3, app));

            }catch (Exception e) {
                Log.e(TAG, "Failed to init App Assignments App=" + app);
            }
        }
    }
}
