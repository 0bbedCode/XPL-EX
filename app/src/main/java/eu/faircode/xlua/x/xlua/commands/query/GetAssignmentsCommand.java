package eu.faircode.xlua.x.xlua.commands.query;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.Log;

import java.util.Collection;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.UberCore888;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.hook.XLuaHookConversions;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.QueryCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.QueryPacket;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.database.sql.SQLSnake;
import eu.faircode.xlua.x.xlua.hook.AssignmentApi;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;
import eu.faircode.xlua.x.xlua.settings.data.SettingsApi;

public class GetAssignmentsCommand extends QueryCommandHandlerEx {
    private static final String TAG = LibUtil.generateTag(GetAssignmentsCommand.class);

    public GetAssignmentsCommand() { this.name = "getAssignments"; this.requiresPermissionCheck = false; }

    @Override
    public Cursor handle(QueryPacket commandData) throws Throwable {
        return CursorUtil.toMatrixCursor_final(AssignmentApi.getAssignments(
                commandData.getDatabase(),
                commandData.getUserId(),
                commandData.getCategory()), marshall, 0);
    }

    public static List<AssignmentPacket> get(Context context, boolean marshall, int uid, String category) { return get(context, marshall, uid, category, 0); }
    public static List<AssignmentPacket> get(Context context, boolean marshall, int uid, String category, int extraFlags) {
        SQLSnake snake = UserIdentity.createSnakeQueryUID(uid, category)
                .whereColumn("code", extraFlags)
                .asSnake();

        return ListUtil.copyToArrayList(
                CursorUtil.readCursorAs_final(
                        XProxyContent.luaQuery(context, marshall ? "getAssignments2" : "getAssignments", snake),
                        marshall, AssignmentPacket.class));
    }
}
