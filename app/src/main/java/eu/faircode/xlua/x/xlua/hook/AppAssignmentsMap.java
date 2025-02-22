package eu.faircode.xlua.x.xlua.hook;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.query.GetAssignmentsCommand;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;

public class AppAssignmentsMap {
    private static final String TAG = LibUtil.generateTag(AppAssignmentsMap.class);

    public static AppAssignmentsMap create(HookApp app) { return new AppAssignmentsMap(app); }
    public static AppAssignmentsMap create(UserClientAppContext app) { return new AppAssignmentsMap(app); }
    public static AppAssignmentsMap create(int uid, String packageName) { return new AppAssignmentsMap(uid, packageName); }

    private final HookApp app;
    private final Map<String, Boolean> map = new HashMap<>();
    private final Map<String, AppAssignmentInfo> assignmentCache = new HashMap<>();

    public HookApp getApp() { return app; }
    public int getAppUid() { return app.uid; }
    public String getAppPackageName() { return app.packageName; }

    public AppAssignmentsMap(HookApp app) { this.app = app; }
    public AppAssignmentsMap(UserClientAppContext app) { this.app = new HookApp(app.appUid, app.appPackageName); }
    public AppAssignmentsMap(int uid, String packageName) { this.app = new HookApp(uid, packageName); }

    public AppAssignmentInfo get(SettingsContainer container) {
        List<String> settings = HooksSettingsGlobal.settingHoldersToNames(container);
        if(container == null || HookApp.isGlobalApp(app) || !ListUtil.isValid(settings))
            return AppAssignmentInfo.DEFAULT;

        AppAssignmentInfo info = internalGetFirst(settings);
        if(info == null) {
            info = AppAssignmentInfo.create(container.getName(), app, this);
            info.refreshSettingAssignmentsFromMap(null, settings);
            for(String name : settings)
                this.assignmentCache.put(name, info);
        }

        return info;
    }

    public void refresh(Context context) {
        if(context != null && !HookApp.isGlobalApp(app)) {
            clear();
            List<AssignmentPacket> assignments = GetAssignmentsCommand.get(context, true, getAppUid(), getAppPackageName(), 0);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Got Assignments Count=" + ListUtil.size(assignments));

            if(ListUtil.isValid(assignments))
                for(AssignmentPacket assignment : assignments)
                    map.put(assignment.getHookId(), true);
        }
    }

    public boolean isAssigned(String hookId) { return Boolean.TRUE.equals(map.get(hookId)); }
    public void setAssigned(String hookId, boolean isAssigned) {
        if(!Str.isEmpty(hookId)) {
            map.put(hookId, isAssigned);
            //we need to refresh rest
            //List<String> settings = HooksSettingsGlobal.
        }
    }

    public void clear() { map.clear(); assignmentCache.clear(); }

    private AppAssignmentInfo internalGetFirst(List<String> settings) {
        for(String name : settings) {
            AppAssignmentInfo info = this.assignmentCache.get(name);
            if(info != null)
                return info;
        }

        return null;
    }
}
