package eu.faircode.xlua.x.xlua.hook;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.UberCore888;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.database.sql.SQLSnake;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

public class AssignmentApi {
    private static final String TAG = LibUtil.generateTag(AssignmentApi.class);

    public static A_CODE assignHooks(Context context, SQLDatabase database, AssignmentsPacket packet) {
        try {
            if(!database.beginTransaction(true)) {
                Log.e(TAG, "Failed to Start Database Transaction for Assignments!");
                return A_CODE.ERROR_TABLE_BEGIN_TRANSACTION_X;
            }

            if(!DatabaseHelpEx.prepareDatabase(database, AssignmentPacket.TABLE_INFO)) {
                Log.e(TAG, "Failed to Prepare Database Table for Assignments ! ");
                return A_CODE.ERROR_TABLE_PREPARE_X;
            }

            if(DebugUtil.isDebug())
                Log.d(TAG,  Str.fm("Assigning Hooks, Delete=%s  Assignment Table Entries=%s  Packet=%s  Table Info=%s", packet.isAction(ActionFlag.DELETE), database.tableEntries(AssignmentPacket.TABLE_NAME), Str.noNL(packet), Str.toStringOrNull(AssignmentPacket.TABLE_INFO)));

            List<String> groups = new ArrayList<>();

            for(String hookId : packet.hookIds) {
                XLuaHook hook = UberCore888.getHook(hookId);
               if(hook != null && !groups.contains(hook.getGroup()))
                   groups.add(hook.getGroup());

               if(DebugUtil.isDebug())
                   Log.d(TAG, "Got Hook for HookID Assignment, HookId=" + hookId + " Hook Object=" + Str.toStringOrNull(hook) + " UID=" + packet.getUserId(true) + " PKG=" + packet.getCategory());

               if(packet.isAction(ActionFlag.DELETE)) {
                   SQLSnake snake = SQLSnake.create()
                           .whereIdentity(packet.getUserId(true), packet.getCategory())
                           .whereColumn(AssignmentPacket.FIELD_HOOK, hookId)
                           .asSnake();

                   if(DebugUtil.isDebug())
                       Log.d(TAG, "Deleting Snake Assignment! Where Clause=" + snake.getWhereClause() + " Args=" + Str.joinArray(snake.getWhereArgs()) + " UID=" + packet.getUid());

                   if(!database.delete(AssignmentPacket.TABLE_NAME, snake.getWhereClause(), snake.getWhereArgs()))
                       Log.e(TAG, "Failed to Delete Assignment, Id=" + hookId);
               }
               else {
                   ContentValues cv = new ContentValues();
                   cv.put(AssignmentPacket.FIELD_USER, packet.getUserId(true));
                   cv.put(AssignmentPacket.FIELD_CATEGORY, packet.getCategory());
                   cv.put(AssignmentPacket.FIELD_HOOK, hookId);
                   cv.put(AssignmentPacket.FIELD_INSTALLED, -1);
                   cv.put(AssignmentPacket.FIELD_USED, -1);
                   cv.put(AssignmentPacket.FIELD_RESTRICTED, 0);
                   cv.putNull(AssignmentPacket.FIELD_EXCEPTION);

                   if(DebugUtil.isDebug())
                       Log.d(TAG, Str.fm("Assigning Hook Assignment, Content Values=[%s] HookId=%s Category=%s  UserId=%s", Str.toStringOrNull(cv), hookId, packet.getCategory(), packet.getUserId(true)));

                   if (!database.insert(AssignmentPacket.TABLE_NAME, cv))
                       Log.e(TAG, "Failed to Insert Assignment, Id=" + hookId);
               }
            }

            if(!packet.isAction(ActionFlag.DELETE)) {
                for(String group : groups) {
                    SQLSnake snake = SQLSnake.create()
                            .whereIdentity(packet.getUserId(true), packet.getCategory())
                            .whereColumn(GroupPacket.FIELD_NAME, group)
                            .asSnake();

                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Deleting Group from Table, Group=%s  Category=%s", group, packet.getCategory()));

                    if(!database.delete(GroupPacket.TABLE_NAME, snake.getWhereClause(), snake.getWhereArgs()))
                        Log.e(TAG, "Failed to Delete Group! " + group);
                }
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Finished Updating Assignments now Killing if needed, Info=" + packet + " Table Entries=");

            if(packet.shouldKill())
                AppProviderApi.forceStop(context, packet.getUserId(true), packet.getCategory());

            database.setTransactionSuccessful();
            return A_CODE.SUCCESS;
        }catch (Exception e) {
            Log.e(TAG, "Failed to Assign Hooks! Error=" + e);
            return A_CODE.FAILED;
        } finally {
            database.endTransaction(true, false);
        }
    }

    public static AssignmentPacket getAssignment(SQLDatabase db, int userId, String category, String hookId) {
        return SQLSnake.create(db, AssignmentPacket.TABLE_NAME)
                .ensureDatabaseIsReady()
                .whereIdentity(userId, category)
                .whereColumn(AssignmentPacket.FIELD_HOOK, hookId)
                .asSnake()
                .queryGetFirstAs(AssignmentPacket.class, true, false);
    }

    public static SettingPacket getSetting(SQLDatabase db, int userId, String category, String name) {
        return SQLSnake.create(db, SettingPacket.TABLE_NAME)
                .ensureDatabaseIsReady()
                .whereIdentity(userId, category)
                .whereColumn(SettingPacket.FIELD_NAME, name)
                .asSnake()
                .queryGetFirstAs(SettingPacket.class, true, false);
    }

    public static List<AssignmentPacket> dumpAssignments(SQLDatabase db) {
        if(!DatabaseHelpEx.ensureTableIsReady(AssignmentPacket.TABLE_INFO, db))
            return ListUtil.emptyList();

        return DatabaseHelpEx.getFromDatabase(
                db,
                AssignmentPacket.TABLE_NAME,
                AssignmentPacket.class, true);
    }

    public static List<AssignmentPacket> getAssignments(SQLDatabase db, int userId, String category) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "DB=" + Str.toStringOrNull(db) + " User Id=" + userId + " Category=" + category);

        return SQLSnake.create(db, AssignmentPacket.TABLE_NAME)
                .ensureDatabaseIsReady()
                .whereIdentity(userId, category)
                .asSnake()
                .queryAs(AssignmentPacket.class, true, false);
    }
}
