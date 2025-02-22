package eu.faircode.xlua.x.xlua.hook;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.UberCore888;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.database.sql.SQLSnake;
import eu.faircode.xlua.x.xlua.identity.UserIdentityUtils;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;
import eu.faircode.xlua.x.xlua.settings.data.SettingsApi;

public class AppProviderUtils {
    private static final String TAG = LibUtil.generateTag(AppProviderUtils.class);


    public static AppXpPacket assignAppInfoToPacket(ApplicationInfo ai, PackageManager pm,
            boolean initForceStop,
            boolean initAssignments) {

        AppXpPacket packet = new AppXpPacket();
        try {
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Creating App Packet from AppInfo. ai=%s InitForceStop=%s InitAssignments=%s", ai, initForceStop, initAssignments));

            int enabledSetting = pm.getApplicationEnabledSetting(ai.packageName);
            boolean enabled = (ai.enabled &&
                    (enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT ||
                            enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_ENABLED));
            boolean persistent = ((ai.flags & ApplicationInfo.FLAG_PERSISTENT) != 0 ||
                    "android".equals(ai.packageName));
            boolean system = ((ai.flags &
                    (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0);

            packet.uid = ai.uid;
            packet.packageName = ai.packageName;
            packet.icon = ai.icon;
            packet.label = (String) pm.getApplicationLabel(ai);
            packet.enabled = enabled;
            packet.persistent = persistent;
            packet.system = system;
            packet.forceStop = !persistent && !system;
            if(DebugUtil.isDebug())
                Log.d(TAG, "Created App Packet, Packet=" + packet);

            return packet;
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to attach AppInfo to Packet! ai=%s InitForceStop=%s InitAssignments=%s", ai, initForceStop, initAssignments));
            return packet;
        }
    }


    public static void initAppsForceStop(Map<String, AppXpPacket> apps, SQLDatabase database, int userId) {
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Initializing App List force Stop Flags, Apps Count=%s Database=%s  UserId=%s", apps.size(), Str.noNL(database), userId));

        if(apps.size() == 1) {
            for(Map.Entry<String, AppXpPacket> a : apps.entrySet()) {
                SettingPacket setting = SQLSnake
                        .create(database, SettingPacket.TABLE_NAME)
                        .whereColumn(SettingPacket.FIELD_USER, userId)
                        .whereColumn(SettingPacket.FIELD_CATEGORY, a.getKey())
                        .whereColumn(SettingPacket.FIELD_NAME, GetSettingExCommand.SETTING_FORCE_STOP)
                        .asSnake()
                        .queryGetFirstAs(SettingPacket.class, true, true);

                AppXpPacket app = a.getValue();
                app.forceStop = Str.toBoolean(setting.value, false);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Set Single Force Stop Flag for App: %s  UserId: %s  Flag: %s", a.getKey(), userId, setting.value));

                break;
            }
        }
        else {
            Collection<SettingPacket> settings = SQLSnake
                    .create(database, SettingPacket.TABLE_NAME)
                    .whereColumn(SettingPacket.FIELD_USER, userId)
                    .whereColumn(SettingPacket.FIELD_NAME, GetSettingExCommand.SETTING_FORCE_STOP)
                    .onlyReturn(SettingPacket.FIELD_CATEGORY, SettingPacket.FIELD_VALUE)
                    .asSnake()
                    .queryAs(SettingPacket.class, true, true);

            for(SettingPacket setting : settings) {
                AppXpPacket app = apps.get(setting.getCategory());
                if(app != null)
                    app.forceStop = Str.toBoolean(setting.value, false);
            }
        }
    }

    public static List<AssignmentPacket> filterAssignments(List<AssignmentPacket> assignments) { return filterAssignments(assignments, false, false); }
    public static List<AssignmentPacket> filterAssignments(List<AssignmentPacket> assignments, boolean allowFilters) { return filterAssignments(assignments, allowFilters, false);  }
    public static List<AssignmentPacket> filterAssignments(List<AssignmentPacket> assignments, boolean allowFilters, boolean allowSpecial) {
        //We wil double filter, one init drop all Entries with the Filter Hooks
        //Apps should not be able to be Assigned the "Filters" those get assigned by my system upon determination if the user uses RULES
        //Double as in, the DB Updater check / some stage along those lines, drop and just block from any Get Assignment Api
        List<AssignmentPacket> finalList = new ArrayList<>();
        String pkg = null;
        if(ListUtil.isValid(assignments)) {
            for(AssignmentPacket assignment : assignments) {
                if(Str.isEmpty(pkg))
                    pkg = assignment.getCategory();

                String hookId = assignment.getHookId();
                if(Str.isEmpty(hookId))
                    continue;

                if(!allowFilters && AssignmentUtils.isFilterHook(hookId)) {
                    if(DebugUtil.isDebug()) Log.w(TAG, "Skipping Assignment as it is an Filter, ID=" + hookId); //We can delete it from here ?
                    continue;
                }

                if(!allowSpecial && AssignmentUtils.isSpecialSetting(hookId)) {
                   if(DebugUtil.isDebug()) Log.w(TAG, "Skipping Assignment as it is an Special Setting, ID=" + hookId);
                   continue;
                }

                finalList.add(assignment);
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Given Assignments Count=" + ListUtil.size(assignments) + " Final Count=" + ListUtil.size(finalList) + " app=" + pkg);

        return finalList;
    }

    public static void initAppsAssignmentSettings(Map<String, AppXpPacket> apps, SQLDatabase database, int userId) {
        int start = XUtil.getUserUid(userId, 0);
        int end = XUtil.getUserUid(userId, Process.LAST_APPLICATION_UID);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Initializing App List Assignment Data, Apps Count=%s  Database=%s  UserId=%s  Start=%s End=%s", apps.size(), Str.noNL(database), userId, start, end));

        List<String> collections =
                database.executeWithWriteLock(() -> SettingsApi.getCollectionsValue(
                        database,
                        userId));

        //We can check collections
        List<AssignmentPacket> assignments = new ArrayList<>();
        if(apps.size() == 48543)  {
            AppXpPacket app = ListUtil.copyToArrayList(apps.values()).get(0);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Is Single App for Init Assignments, App Pkg=" + app.packageName + " UserId=" + userId);

            ListUtil.addAllIfValid(assignments, filterAssignments(SQLSnake
                    .create(database, AssignmentPacket.TABLE_NAME)
                    .whereColumn(AssignmentPacket.FIELD_USER, userId)
                    .whereColumn(AssignmentPacket.FIELD_CATEGORY, app.packageName)
                    .asSnake()
                    .queryAs(AssignmentPacket.class, true, true)));
        } else {
            ListUtil.addAllIfValid(assignments, filterAssignments(SQLSnake
                    .create(database, AssignmentPacket.TABLE_NAME)
                    .onlyReturn(AssignmentPacket.FIELD_USER, AssignmentPacket.FIELD_CATEGORY, AssignmentPacket.FIELD_HOOK, AssignmentPacket.FIELD_INSTALLED, AssignmentPacket.FIELD_USED, AssignmentPacket.FIELD_RESTRICTED, AssignmentPacket.FIELD_EXCEPTION)
                    .whereColumn(AssignmentPacket.FIELD_USER, start, ">=")
                    .whereColumn(AssignmentPacket.FIELD_USER, end, "<=")
                    .asSnake()
                    .queryAs(AssignmentPacket.class, true, true)));
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Starting Assignment Check Loop, Assignments=%s  User Id=%s  Collections=%s", ListUtil.size(assignments), userId, Str.joinList(collections)));

        for(AssignmentPacket assignment : assignments) {
            //hmmm
            AppXpPacket app = apps.get(assignment.getCategory());
            if(app == null) {
                if(DebugUtil.isDebug())
                    Log.e(TAG, "Assignment App is Null, Category=" + assignment.getCategory());

                continue;
            }

            //This should be fine no ? Should only get that users
            int appUserId = UserIdentityUtils.getUserId(app.uid);
            if(appUserId != assignment.getUserId(true)) {
                if(DebugUtil.isDebug())
                    Log.w(TAG, Str.fm("Package Name %s is the Same but the UID is not, App:%s Assignment:%s  AppUserId:%s is Not...", assignment.getCategory(), app.uid, assignment.getUserId(true), appUserId));
            }

            XLuaHook hook = UberCore888.getHook(assignment.getHookId(), assignment.getCategory(), collections);
            if(hook == null) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Hook Is NULL for Assignment, Hook=%s  Category=%s  User Id=%s  Collection Size=%s", assignment.getHookId(), assignment.getCategory(), userId, ListUtil.size(collections)));

                continue;
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Found the Hook for Assignment, ID=%s  User ID=%s", assignment.getHookId(), userId));

            assignment.setHook(hook);
            app.addAssignment(assignment);
        }
    }
}
