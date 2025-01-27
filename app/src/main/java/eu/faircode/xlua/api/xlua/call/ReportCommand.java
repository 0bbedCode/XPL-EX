package eu.faircode.xlua.api.xlua.call;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import eu.faircode.xlua.ActivityMain;
import eu.faircode.xlua.BuildConfig;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.UberCore888;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.xstandard.CallCommandHandler;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.hooks.XReport;

public class ReportCommand extends CallCommandHandler {
    private static final String TAG = "XLua.ReportCommand";
    final static String cChannelName = "xlua";
    @SuppressWarnings("unused")
    public ReportCommand() {
        this.name = "report";
        this.requiresPermissionCheck = false;
        this.requiresSingleThread = true;
    }

    @Override
    @SuppressLint("MissingPermission")
    public Bundle handle(CallPacket_old commandData) throws Throwable {
        Bundle extras = commandData.getExtras();
        XDatabaseOld dbb = commandData.getDatabase();
        SQLiteDatabase db = dbb.getDatabase();


        String hookid = extras.getString("hook");
        String packageName = extras.getString("packageName");
        int uid = extras.getInt("uid");
        int userid = XUtil.getUserId(uid);
        String event = extras.getString("event");
        long time = extras.getLong("time");
        Bundle data = extras.getBundle("data");
        int restricted = data.getInt("restricted", 0);

        if (uid != Binder.getCallingUid())
            throw new SecurityException();

        StringBuilder sb = new StringBuilder();
        for (String key : data.keySet()) {
            sb.append(' ');
            sb.append(key);
            sb.append('=');
            Object value = data.get(key);
            sb.append(value == null ? "null" : value.toString());
        }
        Log.i(TAG, "Hook " + hookid + " pkg=" + packageName + ":" + uid + " event=" + event + sb.toString());

        // Get hook
        XLuaHook hook = UberCore888.getHook(hookid);
        /*if(hook == null) {
            try {
                Collection<XLuaHook> hooks = XGlobals.getHooks(commandData.getContext(), commandData.getDatabase(), true);
                for(XLuaHook h : hooks) {
                    if(h.getId().equalsIgnoreCase(hookid)) {
                        hook = h;
                        break;
                    }
                }
            }catch (Exception e) {
                Log.e(TAG, "Error Getting HOOK (" + userid + ")");
            }
        }*/

        if(DebugUtil.isDebug())
            Log.d(TAG, "Report Hook ID=" + (hook == null ? "" : hook.getId()) + " Report ID=" + hookid);

        // Get notify setting
        Bundle args = new Bundle();
        args.putInt("user", userid);
        args.putString("category", packageName);
        args.putString("name", "notify");
        boolean notify = Boolean.parseBoolean(GetSettingCommand.invoke(commandData.getContext(), args).getString("value"));
        //boolean notify = Boolean.parseBoolean(getSetting(context, args).getString("value"));

        long used = -1;
        dbb.writeLock();
        try {
            db.beginTransaction();
            try {
                // Store event
                ContentValues cv = new ContentValues();
                if ("install".equals(event))
                    cv.put("installed", time);
                else if ("use".equals(event)) {
                    cv.put("used", time);
                    cv.put("restricted", restricted);
                }
                if (data.containsKey("exception"))
                    cv.put("exception", data.getString("exception"));
                if (data.containsKey("old"))
                    cv.put("old", data.getString("old"));
                if (data.containsKey("new"))
                    cv.put("new", data.getString("new"));

                long rows = db.update("assignment", cv,
                        "package = ? AND uid = ? AND hook = ?",
                        new String[]{packageName, Integer.toString(uid), hookid});
                if (rows != 1)
                    Log.w(TAG, "Error updating assignment id: " + hookid);

                //Manually insert
                /*if (rows != 1) {
                    Log.w(TAG, "Error updating assignment id: " + hookid);
                    //We can assume its some filter type now so lets just "force" it ?
                    //rows = db.insert("assignment",null, cv);
                    //AssignHooksCommand
                    //Integer user, String packageName, List<String> hookIds, Boolean delete, Boolean kill
                    if(hookid != null && hookid.contains("Filter/")) {
                        if(DebugUtil.isDebug())
                            Log.d(TAG, "Force Inserting Assignment on Hood Id=" + hookid + " user=" + userid + " Package=" + packageName);

                        db.setTransactionSuccessful();
                        db.endTransaction();
                        dbb.writeUnlock();

                        List<String> hookIds = Collections.singletonList(hookid);
                        LuaAssignmentPacket assPacket = new LuaAssignmentPacket(userid, packageName, hookIds, false, false);
                        XResult res = LuaHookManager.assignHooks(commandData.getContext(), commandData.getDatabase(), assPacket);
                        if(DebugUtil.isDebug())
                            Log.d(TAG, "Result of force Inserting Hook id=" + hookid + " Result=" + res.getFullMessage());


                        dbb.writeLock();
                        db.beginTransaction();
                        rows = db.update("assignment", cv,
                                "package = ? AND uid = ? AND hook = ?",
                                new String[]{packageName, Integer.toString(uid), hookid});

                        if(DebugUtil.isDebug())
                            Log.d(TAG, "Hook ID [" + hookid + "] Rows Updated: " + rows);
                    }
                }*/

                // Update group
                if (hook != null && "use".equals(event) && restricted == 1 && notify) {
                    Cursor cursor = null;
                    try {
                        cursor = db.query("`group`", new String[]{"used"},
                                "package = ? AND uid = ? AND name = ?",
                                new String[]{packageName, Integer.toString(uid), hook.getGroup()},
                                null, null, null);
                        if (cursor.moveToNext())
                            used = cursor.getLong(0);
                    } finally {
                        if (cursor != null)
                            cursor.close();
                    }

                    cv.clear();
                    cv.put("package", packageName);
                    cv.put("uid", uid);
                    cv.put("name", hook.getGroup());
                    cv.put("used", time);
                    rows = db.insertWithOnConflict("`group`", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
                    if (rows < 0)
                        throw new Throwable("Error inserting group");
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            dbb.writeUnlock();
        }

        long ident = Binder.clearCallingIdentity();
        try {
            Context ctx = XUtil.createContextForUser(commandData.getContext(), userid);
            PackageManager pm = ctx.getPackageManager();
            Resources resources = pm.getResourcesForApplication(BuildConfig.APPLICATION_ID);

            // Notify usage
            if (hook != null && "use".equals(event) && restricted == 1 &&
                    (hook.doNotify() || (notify && used < 0))) {
                // Get group name
                String name = hook.getGroup().toLowerCase().replaceAll("[^a-z]", "_");
                int resId = resources.getIdentifier("group_" + name, "string", BuildConfig.APPLICATION_ID);
                String group = (resId == 0 ? hookid : resources.getString(resId));

                // Build notification
                Notification.Builder builder = new Notification.Builder(ctx);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    builder.setChannelId(cChannelName);
                builder.setSmallIcon(android.R.drawable.ic_dialog_info);
                builder.setContentTitle(resources.getString(R.string.msg_usage, group));
                builder.setContentText(pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)));
                if (BuildConfig.DEBUG)
                    builder.setSubText(hookid);

                builder.setPriority(Notification.PRIORITY_DEFAULT);
                builder.setCategory(Notification.CATEGORY_STATUS);
                builder.setVisibility(Notification.VISIBILITY_SECRET);

                // Main
                Intent main = ctx.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
                if (main != null) {
                    int flags = (Build.VERSION.SDK_INT > Build.VERSION_CODES.R ? 0x04000000 : 0);
                    main.putExtra(ActivityMain.EXTRA_SEARCH_PACKAGE, packageName);
                    @SuppressLint("WrongConstant") PendingIntent pi = PendingIntent.getActivity(ctx, uid, main, flags);
                    builder.setContentIntent(pi);
                }

                builder.setAutoCancel(true);

                XUtil.notifyAsUser(ctx, "xlua_use_" + hook.getGroup(), uid, builder.build(), userid);
            }

            // Notify exception
            if (data.containsKey("exception")) {
                Notification.Builder builder = new Notification.Builder(ctx);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    builder.setChannelId(cChannelName);
                builder.setSmallIcon(android.R.drawable.ic_dialog_alert);
                builder.setContentTitle(resources.getString(R.string.msg_exception, hookid));
                builder.setContentText(pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)));

                builder.setPriority(Notification.PRIORITY_HIGH);
                builder.setCategory(Notification.CATEGORY_STATUS);
                builder.setVisibility(Notification.VISIBILITY_SECRET);

                // Main
                Intent main = ctx.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
                if (main != null) {
                    int flags = (Build.VERSION.SDK_INT > Build.VERSION_CODES.R ? 0x04000000 : 0);
                    main.putExtra(ActivityMain.EXTRA_SEARCH_PACKAGE, packageName);
                    @SuppressLint("WrongConstant") PendingIntent pi = PendingIntent.getActivity(ctx, uid, main, flags);
                    builder.setContentIntent(pi);
                }

                builder.setAutoCancel(true);

                XUtil.notifyAsUser(ctx, "xlua_exception", uid, builder.build(), userid);
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }



/*        XReport report = new XReport(commandData.getExtras());
        if(report.uid !=  Binder.getCallingUid()) {
            Log.e(TAG, "Calling ID is not matched with Report User ID! " + report.uid);
            throw new SecurityException();//Ok how the fuck this can throw exception without a throws clause this JAVA is fucking dumb worst language
        }

        XLuaHook hook = XGlobals.getHook(report.hookId);
        long used = XLuaHookDatabase.report(report, hook, commandData.getDatabase());

        long identity = Binder.clearCallingIdentity();
        try {
            Context ctx = XUtil.createContextForUser(commandData.getContext(), report.getUserId());
            PackageManager pm = ctx.getPackageManager();
            Resources resources = pm.getResourcesForApplication(BuildConfig.APPLICATION_ID);

            boolean notify = report.getNotify(commandData.getDatabase());
            Log.i(TAG, "XXR hook is null ? =" + (hook == null) + " use=" + report.event.equals("use") + " get restricted=" + report.getRestricted() + " report notify=" + notify + " used=" + used);

            if(hook != null) {
                Log.i(TAG, "XXR hook do notify ? =" + hook.doNotify());
            }

            boolean a1 = hook != null && report.event.equals("use") && report.getRestricted() == 1;
            boolean a2 = hook != null && (hook.doNotify() || (report.getNotify(commandData.getDatabase()) && used < 0));
            boolean a3 = (report.getNotify(commandData.getDatabase()) && used < 0);
            Log.i(TAG, "XXR a1=" + a1 + " a2=" + a2 + " a3=" + a3 + " all=" + (a1 && a2));

            //Notify Usage
            if(hook != null && report.event.equals("use") && report.getRestricted() == 1
                    && (hook.doNotify() || (report.getNotify(commandData.getDatabase()) && used < 0))) {

                Log.i(TAG, "XXR Usage notification invoking...");
                Notification.Builder builder = XNotify.buildUsageNotification(ctx, hook, report, pm, resources);
                XUtil.notifyAsUser(ctx, "xlua_use_" + hook.getGroup(), report.uid, builder.build(), report.getUserId());
            }

            //Notify Exception
            if(report.data.containsKey("exception")) {
                Notification.Builder builder = XNotify.buildExceptionNotification(ctx, report, pm, resources);
                XUtil.notifyAsUser(ctx, "xlua_exception", report.uid, builder.build(), report.getUserId());
            }
        }catch (Throwable e) {
            Log.e(TAG, "Internal Error for Report: \n" + e + "\n" + Log.getStackTraceString(e));
        }finally {
            Binder.restoreCallingIdentity(identity);
        }
        return BundleUtil.createResultStatus(true);*/
        return new Bundle();
    }

    public static Bundle invoke(Context context, XReport report) {
        return XProxyContent.luaCall(
                context,
                "report",
                report.toBundle());
    }
}
