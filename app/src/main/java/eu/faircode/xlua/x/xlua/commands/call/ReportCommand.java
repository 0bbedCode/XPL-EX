package eu.faircode.xlua.x.xlua.commands.call;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.hook.AssignmentApi;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.identity.UserIdentityUtils;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;
import eu.faircode.xlua.x.xlua.settings.data.SettingsApi;

public class ReportCommand extends CallCommandHandlerEx {
    public static final String COMMAND_NAME = "report";
    private static final String TAG = LibUtil.generateTag(ReportCommand.class);

    public ReportCommand() {
        name = COMMAND_NAME;
        requiresPermissionCheck = false;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        Bundle bundle = commandData.extras;


        /*

            public static final String FIELD_USER = UserIdentityIO.FIELD_USER;
            public static final String FIELD_CATEGORY = UserIdentityIO.FIELD_CATEGORY;

            public static final String FIELD_HOOK = "hook";

            public static final String FIELD_INSTALLED = "installed";
            public static final String FIELD_USED = "used";
            public static final String FIELD_RESTRICTED = "restricted";
            public static final String FIELD_EXCEPTION = "exception";
            public static final String FIELD_OLD = "old";
            public static final String FIELD_NEW = "new";

         */


        SQLDatabase database = commandData.getDatabase();
        SQLiteDatabase db = database.getDatabase();

        int uid = bundle.getInt("uid");
        String pkg = bundle.getString("packageName");
        String hookId = bundle.getString("hook");

        long time = bundle.getLong("time");//installed, used
                                                //restricted = (1 or 0)

        String event = bundle.getString("event");

        int userId = UserIdentityUtils.getUserId(uid);
        AssignmentPacket packet = database.executeWithReadLock(() -> AssignmentApi.getAssignment(database, userId, pkg, hookId));
        if(packet == null) {
            packet = new AssignmentPacket();
            packet.hook = hookId;
        }

        packet.setUserIdentity(UserIdentity.from(userId, uid, pkg));
        packet.setActionPacket(ActionPacket.create(ActionFlag.PUSH, false));

        Bundle sub = bundle.getBundle("data");
        if(event != null && sub != null) {
            packet.restricted = Str.toBool(String.valueOf(sub.getInt("restricted", 0)));
            if(event.equalsIgnoreCase("use")) {
                packet.used = time;
                packet.newValue = sub.getString("new");
                packet.oldValue = sub.getString("old");
            }
            else if(event.equalsIgnoreCase("install"))
                packet.installed = time;
            if (sub.containsKey("exception"))
                packet.exception = sub.getString("exception");
        }

        ContentValues cv = packet.toContentValues();
        if(DebugUtil.isDebug()) {
            StringBuilder sb = new StringBuilder();
            for(String k : cv.keySet()) {
                if(sb.length()  > 0)
                    sb.append("\n");
                sb.append(k).append(" >> ").append(Str.toStringOrNull(cv.get(k)));
            }

            Log.w(TAG, "Pushing=" + sb.toString());
        }

        A_CODE result = DatabaseHelpEx.execute_one_locked_name(database, packet, AssignmentPacket.TABLE_INFO);
        if(DebugUtil.isDebug())
            Log.d(TAG, "Result=" + result.name());

        //Log.d(TAG, "Result of Assignment Update=" + result.name());

        /*try {
            database.beginTransaction(true);
            long rows = db.update("assignments", cv, "user = ? category = ? AND hook = ?",
                    new String[]{ String.valueOf(userId), pkg, hookId});
            if (rows != 1)
                throw new Exception("Error updating assignment id: " + hookId);
            database.setTransactionSuccessful();
        }catch (Exception e) {
            Log.e(TAG, "Error updating Assignment, Error=" + e);
        } finally {
            database.endTransaction(true, false);
        }*/



        //use
        //
        //  => new
        //  => old
        //

        //install
        //exception


        /*if(sub != null) {
            long duration = sub.getLong("duration");
            long restricted = sub.getLong("restricted");

            String newValue = sub.getString("new");
            String oldValue = sub.getString("old");

            String func = sub.getString("function");

            //AssignmentPacket packet = new AssignmentPacket();

        }


        StringBuilder sb = new StringBuilder();
        if(bundle != null) {
            for(String key : bundle.keySet()) {
                Object o = bundle.get(key);
                if(o instanceof Bundle) {
                    Bundle sub = (Bundle) o;
                    for(String subKey : sub.keySet()) {
                        if(sb.length() > 0)
                            sb.append("\n");

                        sb.append("[>]").append(subKey).append(" >> ").append(Str.toStringOrNull(sub.get(subKey)));
                    }
                } else {
                    if(sb.length() > 0)
                        sb.append("\n");

                    sb.append(key).append(" >> ").append(Str.toStringOrNull(o));
                }
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.ensureNoDoubleNewLines(sb.toString()).replaceAll("\n\n", "\n"));*/

        return null;
    }
}
