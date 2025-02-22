package eu.faircode.xlua.x.xlua.hook;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.call.InitAssignments;

//We can organize either via Group, or Setting
public class AppAssignmentInfo {
    private static final String TAG = LibUtil.generateTag(AppAssignmentInfo.class);

    public static final String DEFAULT_PREFIX = "---";
    public static final AppAssignmentInfo DEFAULT = new AppAssignmentInfo(Str.DEFAULT_DEFAULT, HookApp.create(UserClientAppContext.DEFAULT));

    public static AppAssignmentInfo create(String name, HookApp app) { return new AppAssignmentInfo(name, app); }
    public static AppAssignmentInfo create(String name, HookApp app, AppAssignmentsMap map) { return new AppAssignmentInfo(name, app, map); }

    private int count = 0;
    private int assignedCount = 0;
    private AppAssignmentsMap map;

    public final HookApp app;
    public final String name;
    public final Map<String, Boolean> assignments = new HashMap<>();

    // int color = XUtil.resolveColor(context, data.total > 0 ?  R.attr.colorUnsavedSetting  : R.attr.colorTextOne);

    public int getLabelColor(Context context) { return XUtil.resolveColor(context, count > 0 ? R.attr.colorUnsavedSetting : R.attr.colorTextOne); }

    public int getCount() { return count; }
    public int getAssignedCount() { return assignedCount; }

    public boolean isValid() { return !Str.DEFAULT_DEFAULT.equals(name) && !HookApp.isGlobalApp(app); }

    public String getPrefix() { return count == 0 ? DEFAULT_PREFIX : Str.combineEx(assignedCount, "/", count); }

    public void attachMap(AppAssignmentsMap map) { this.map = map; }

    public AppAssignmentInfo(String name, HookApp app) { this(name, app, null); }
    public AppAssignmentInfo(String name, HookApp app, AppAssignmentsMap map) {
        this.name = name;
        this.app = app;
        this.map = map;
    }

    public void refreshSettingAssignmentsFromMap(List<String> settings) { refreshSettingAssignmentsFromMap(null, settings); }
    public void refreshSettingAssignmentsFromMap(Context context, List<String> settings) {
        if(this.map != null && isValid()) {
            reset();
            if(context != null)
                this.map.refresh(context);

            List<String> hookIds = HooksSettingsGlobal.getHookIdsForSettings(context, settings);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Refreshing Assignments from Map, Hooks Count=" + ListUtil.size(hookIds));

            for(String hookId : hookIds) {
                if(!Str.isEmpty(hookId)) {
                    boolean isAssigned = this.map.isAssigned(hookId);
                    this.assignments.put(hookId, isAssigned);
                    if(isAssigned)
                        assignedCount++;
                }
            }

            count = this.assignments.size();
        }
    }

    public void refreshSettingAssignments(Context context, List<String> settings) {
        if(isValid()) {
            reset();
            ListUtil.addAllIfValid(this.assignments, InitAssignments.get(context, HooksSettingsGlobal.getHookIdsForSettings(context, settings), app.uid, app.packageName), false);
            count = this.assignments.size();
            for(Map.Entry<String, Boolean> entry : this.assignments.entrySet()) {
                if(Boolean.TRUE.equals(entry.getValue())) {
                    assignedCount++;
                }
            }
        }
    }

    public void reset() {
        assignments.clear();
        count = 0;
        assignedCount = 0;
    }

    @NonNull
    @Override
    public String toString() {
        return getPrefix();
    }
}
