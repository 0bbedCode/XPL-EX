package eu.faircode.xlua.api.xlua.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


import eu.faircode.xlua.XDatabase;
import eu.faircode.xlua.XGlobals;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.hook.assignment.LuaAssignment;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.hook.assignment.LuaAssignmentPacket;
import eu.faircode.xlua.api.xlua.provider.XLuaAppProvider;
import eu.faircode.xlua.api.standard.database.DatabaseHelp;
import eu.faircode.xlua.api.standard.database.SqlQuerySnake;
import eu.faircode.xlua.api.hook.assignment.XLuaAssignmentDataHelper;
import eu.faircode.xlua.api.hook.group.XLuaGroupDataHelper;
import eu.faircode.xlua.hooks.XReport;

public class XLuaHookDatabase {
    private static final String TAG = "XLua.XHookDatabase";

    public static XResult updateHook(XDatabase db, XLuaHook hook, String extraId) {
        return XResult.create().setMethodName("updateHook")
                .log("[updating hook] extra id=" + extraId, TAG)
                                .setResult(hook == null ? deleteHook(db, extraId) : hook.isBuiltin() || putHook(db, hook));
    }

    public static boolean putHook(XDatabase db, XLuaHook hook) {
        //Make sure we do not need to prepare db what not
        return DatabaseHelp.insertItem(db, XLuaHook.Table.name, hook);
    }

    public static boolean deleteHook(XDatabase db, String id) { return DatabaseHelp.deleteItem(SqlQuerySnake.create(db, XLuaHook.Table.name).whereColumn("id", id)); }

    public static XResult assignHooks(Context context, XDatabase db, LuaAssignmentPacket packet) {
        String packageName = packet.getCategory();
        int uid = packet.getUser();

         XResult res = XResult.create().setMethodName("assignHooks").setExtra(packet.toString());
        //Assign Hook(s) to a App (package name, uid)
        List<String> groups = new ArrayList<>();
        XLuaAssignmentDataHelper assignmentData = new XLuaAssignmentDataHelper(packageName, uid);
        XLuaGroupDataHelper groupData = new XLuaGroupDataHelper(packageName, uid);

        try {
            if(!db.beginTransaction(true))
                return res.setFailed("Cannot being Database Transaction");

            if(!db.hasTable(LuaAssignment.Table.name)) {
                Log.e(TAG, "Table does not exist [" + LuaAssignment.Table.name + "] in Database [" + db + "]");
                return res.setFailed("Assignments Table does not exist!");
            }

            int failed = 0;
            int succeeded = 0;
            for(String hookId : packet.getHookIds()) {
                XLuaHook hook = XGlobals.getHook(hookId);

                //Add its Group to the group list
                if (hook != null && !groups.contains(hook.getGroup()))
                    groups.add(hook.getGroup());

                if(packet.isDelete()) {
                    Log.i(TAG, packageName + ":" + uid + "/" + hookId + " deleted");
                    if(!db.delete(LuaAssignment.Table.name, assignmentData.getSelectionArgs(), assignmentData.createValueArgs(hookId))) {
                        Log.e(TAG, "Failed to Delete Assignment ID=" + hookId);
                        failed++;
                    }else succeeded++;
                }else {
                    Log.i(TAG, packageName + ":" + uid + "/" + hookId + " added");
                    if(!db.insert(LuaAssignment.Table.name, assignmentData.createContentValues(hookId))) {
                        Log.e(TAG, "Failed to Insert Assignment ID=" + hookId);
                        failed++;
                    }else succeeded++;
                }
            }

            if (!packet.isDelete())
                for (String group : groups) {
                    if(!db.delete("`group`", groupData.getSelectionArgs(), groupData.createValueArgs(group))) {
                        Log.e(TAG, "Failed to Delete Group=" + group);
                        failed++;
                    }else succeeded++;
                }

            Log.i(TAG, "assignHooks succeeded operations=" + succeeded + " failed operations=" + failed);
            if(succeeded == 0 && packet.getHookIds().size() > 0)
                return res.setFailed("None of the Operations succeeded ! (all failed)");

            db.setTransactionSuccessful();
        }finally {
            db.endTransaction(true, false);
        }

        if (packet.isKill())
            XLuaAppProvider.forceStop(context, packageName, XUtil.getUserId(uid));

        return res.setSucceeded();
    }

    public static long report(XReport report, XLuaHook hook, XDatabase db) {
        Log.i(TAG , "Updating Assignment: " + report);

        //Update Assignment , make it a function ?


        long used = -1;
        if(!DatabaseHelp.updateItem(db, LuaAssignment.Table.name, report.generateQuery(), report))
            Log.w(TAG, "Error updating Assignment: " + report);

        //Update Group
        if(hook != null && report.event.equals("use") && report.getRestricted() == 1 && report.getNotify(db)) {
            Log.i(TAG, " cool invokging shit");
            used = SqlQuerySnake
                    .create(db , "`group`")
                    .whereColumns("package", "uid", "name")
                    .whereColumnValues(report.packageName, Integer.toString(report.getUserId()), hook.getGroup())
                    .queryGetFirstLong("used", true);
            //Integer.toString(report.uid)

            Log.i(TAG, " used count=" + used + " hook group=" + hook.getGroup() + " pkg=" + report.packageName + " uid=" + report.uid + " uid resolved=" + Integer.toString(report.getUserId()));

            //ahh fix this ugly code
            //dupes too many standardize query snake
            if(!DatabaseHelp.insertItem(db, "`group`",  report.createGroupObject(hook, used)))
                Log.e(TAG, "Error inserting group: " + report);

            ContentValues cv = new ContentValues();
            cv.put("package", report.packageName);
            cv.put("uid", report.getUserId());
            cv.put("name", hook.getGroup());
            cv.put("used", time);
            long rows = db.insertWithOnConflict("`group`", null, cv, SQLiteDatabase.CONFLICT_REPLACE);

        }

        return used;
    }
}
