package eu.faircode.xlua.x.xlua.commands.query;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.QueryCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.QueryPacket;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.database.sql.SQLSnake;
import eu.faircode.xlua.x.xlua.hook.AppProviderUtils;
import eu.faircode.xlua.x.xlua.hook.AssignmentApi;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;

public class GetAssignmentsCommand extends QueryCommandHandlerEx {
    private static final String TAG = LibUtil.generateTag(GetAssignmentsCommand.class);

    public static final String COMMAND_NAME = "getAssignments";

    public GetAssignmentsCommand() { this.name = COMMAND_NAME; this.requiresPermissionCheck = false; }

    @Override
    public Cursor handle(QueryPacket commandData) throws Throwable {
        if(commandData.isDump()) {
            return CursorUtil.toMatrixCursor_final(
                    AssignmentApi.dumpAssignments(commandData.getDatabase()),
                    marshall,
                    0);
        } else {
            return CursorUtil.toMatrixCursor_final(
                    AppProviderUtils.filterAssignments(
                            AssignmentApi.getAssignments(
                                    commandData.getDatabase(),
                                    commandData.getUserId(),
                                    commandData.getCategory()),
                            false,
                            commandData.getIntSelectionAt(2, 0) == 3),
                    marshall, 0);
        }
    }


    public static List<AssignmentPacket> dump(Context context, boolean marshall) {
        return ListUtil.copyToArrayList(
                CursorUtil.readCursorAs_final(
                        XProxyContent.luaQuery(context, XProxyContent.commandName(COMMAND_NAME, marshall), new String[] { ActionPacket.ACTION_DUMP }),
                        marshall, AssignmentPacket.class));
    }

    public static List<AssignmentPacket> get(Context context, boolean marshall, int uid, String category) { return get(context, marshall, uid, category, 0); }
    public static List<AssignmentPacket> get(Context context, boolean marshall, int uid, String category, int extraFlags) {
        return ListUtil.copyToArrayList(
                CursorUtil.readCursorAs_final(
                        XProxyContent.luaQuery(context, XProxyContent.commandName(COMMAND_NAME, marshall),
                                UserIdentity.createSnakeQueryUID(uid, category).whereColumn(ActionPacket.FIELD_CODE, extraFlags).asSnake()),
                        marshall, AssignmentPacket.class));
    }
}
