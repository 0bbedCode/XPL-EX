package eu.faircode.xlua.x.ui.dialogs;

//todo this
/*public class RandomizedSettingsDialog  extends CheckableDialog<XLuaHook> {
    private static final String TAG = "XLua.HooksDialog";

    private AppXpPacket app;
    private int uid;
    private String packageName;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static List<String> toHookIds(List<XLuaHook> hooks) {
        List<String> ids = new ArrayList<>(hooks.size());
        for (XLuaHook h : hooks)
            ids.add(h.getId());

        return ids;
    }

    @Override
    protected void onDisabledPush(List<XLuaHook> disabled) {
        if (!disabled.isEmpty()) {
            executor.submit(() -> AssignHooksCommand.call(context,
                    AssignmentsPacket.create(app.uid, app.packageName, toHookIds(disabled), true, app.forceStop)));
        }
    }

    @Override
    protected void onEnabledPush(List<XLuaHook> enabled) {
        if (!enabled.isEmpty()) {
            executor.submit(() -> AssignHooksCommand.call(context,
                    AssignmentsPacket.create(app.uid, app.packageName, toHookIds(enabled), false, app.forceStop)));
        }
    }

    public HooksDialog set(int uid, String packageName, Context context, List<String> setting_names) {
        if (context != null) {
            this.TAG_ITEMS = ViewStateRegistry.STATE_TAG_HOOKS;
            this.title = context.getString(R.string.title_hooks_assign);
            this.uid = uid;
            this.packageName = packageName;
            refresh(context, setting_names);
        }

        return this;
    }

    private void refresh(Context context, List<String> setting_names) {
        this.app = GetAppInfoCommand.get(context, uid, packageName);
        HookGroupHolder groupHolder = new HookGroupHolder(this.app);
        this.viewRegistry = new ViewStateRegistry();

        List<XLuaHook> allHooks = GetHooksCommand.getHooks(context, true, false);
        groupHolder.collectApp(this.app, allHooks, context, viewRegistry);
        ListUtil.addAllIfValid(items, groupHolder.getHooksForSettings(setting_names), true);

        if (DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Refresh Finished for Hooks Dialog, " +
                            "UID=%s Package Name=%s Groups Count=%s All Hooks Count=%s Targeted Hooks Count=%s Setting Names=[%s]",
                    uid,
                    packageName,
                    groupHolder.groupCount(),
                    allHooks.size(),
                    items.size(),
                    Str.joinList(setting_names)));
    }
}*/