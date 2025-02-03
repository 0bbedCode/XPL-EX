package eu.faircode.xlua.x.ui.dialogs;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.faircode.xlua.R;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.core.dialog.CheckableDialog;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.call.AssignHooksCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetAppInfoCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetHooksCommand;
import eu.faircode.xlua.x.xlua.hook.AppXpPacket;
import eu.faircode.xlua.x.xlua.hook.AssignmentsPacket;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.xlua.hook.HookGroupOrganizer;

public class HooksDialog extends CheckableDialog<XLuaHook> {
    private static final String TAG = LibUtil.generateTag(HooksDialog.class);

    public static HooksDialog create() { return new HooksDialog(); }

    /*
        Make a helper class for this
        It is kinda like repo
        When you open or do certain actions then and only then it will invoke refresh
        to refresh the base elements
     */

    private AppXpPacket app;
    private int uid;
    private String packageName;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public List<XLuaHook> getItems() {
        return items;
    }

    public List<XLuaHook> getEnabled() {
        List<XLuaHook> enabled = new ArrayList<>();
        for(XLuaHook hook : items) {
            if(this.viewRegistry != null && this.viewRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, hook.getSharedId())) {
                enabled.add(hook);
            }
        }

        return enabled;
    }

    public static List<String> toHookIds(List<XLuaHook> hooks) {
        List<String> ids = new ArrayList<>(hooks.size());
        for(XLuaHook h : hooks)
            ids.add(h.getSharedId());

        return ids;
    }

    @Override
    protected void onFinishedPush(List<XLuaHook> enabled, List<XLuaHook> disabled) {
        if (!enabled.isEmpty()) {
            executor.submit(() -> AssignHooksCommand.call(context,
                    AssignmentsPacket.create(app.uid, app.packageName, toHookIds(enabled), false, app.forceStop)));
        }

        if (!disabled.isEmpty()) {
            executor.submit(() -> AssignHooksCommand.call(context,
                    AssignmentsPacket.create(app.uid, app.packageName, toHookIds(disabled), true, app.forceStop)));
        }
    }


    public HooksDialog set(int uid, String packageName, Context context, List<String> setting_names) {
        if(context != null) {
            this.TAG_ITEMS = SharedRegistry.STATE_TAG_HOOKS;
            this.title = context.getString(R.string.title_hooks_assign);
            this.uid = uid;
            this.packageName = packageName;
            refresh(context, setting_names);
        }

        return this;
    }

    private void refresh(Context context, List<String> setting_names) {
        this.app = GetAppInfoCommand.get(context, uid, packageName);
        HookGroupOrganizer groupHolder = new HookGroupOrganizer();
        this.viewRegistry = new SharedRegistry();

        List<XLuaHook> allHooks =  GetHooksCommand.getHooks(context, true, false);
        groupHolder.collectApp(this.app, allHooks, context, viewRegistry);
        ListUtil.addAllIfValid(items, groupHolder.getHooksForSettings(setting_names), true);

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Refresh Finished for Hooks Dialog, " +
                            "UID=%s Package Name=%s Groups Count=%s All Hooks Count=%s Targeted Hooks Count=%s Setting Names=[%s]",
                    uid,
                    packageName,
                    groupHolder.groups.size(),
                    allHooks.size(),
                    items.size(),
                    Str.joinList(setting_names)));
    }

}
