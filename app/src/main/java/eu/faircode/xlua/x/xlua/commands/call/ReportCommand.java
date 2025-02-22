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
import eu.faircode.xlua.x.xlua.hook.AssignmentUtils;
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

        SQLDatabase database = commandData.getDatabase();
        int uid = bundle.getInt("uid");
        String pkg = bundle.getString("packageName");
        String hookId = bundle.getString("hook");
        long time = bundle.getLong("time");//installed, used
                                                //restricted = (1 or 0)

        if(Str.isEmpty(hookId) || AssignmentUtils.isFilterHook(hookId)) {
            if(DebugUtil.isDebug())
                Log.w(TAG, "Waring found a Filter Hook Report, take this with cation! Hook Id=" + hookId);
        }

        String event = bundle.getString("event");

        int userId = UserIdentityUtils.getUserId(uid);
        AssignmentPacket packet = database.executeWithReadLock(() -> AssignmentApi.getAssignment(database, userId, pkg, hookId));
        if(packet == null || Str.isEmpty(packet.getHookId())) {
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

        A_CODE result = DatabaseHelpEx.execute_one_locked_name(database, packet, AssignmentPacket.TABLE_INFO);
        if(DebugUtil.isDebug())
            Log.d(TAG, "Result=" + result.name() + " Hook ID=" + hookId);

        return null;
    }
}
