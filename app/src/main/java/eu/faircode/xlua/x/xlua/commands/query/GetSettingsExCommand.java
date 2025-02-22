package eu.faircode.xlua.x.xlua.commands.query;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.xstandard.database.SqlQuerySnake;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.ListUtils;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.QueryCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.QueryPacket;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.database.sql.SQLSnake;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;
import eu.faircode.xlua.x.xlua.settings.data.SettingsApi;

@SuppressWarnings("unused")
public class GetSettingsExCommand extends QueryCommandHandlerEx {
    private static final String TAG = LibUtil.generateTag(GetSettingsExCommand.class);

    public static final String COMMAND_NAME = "getExSettingsEx";

    public static final int FLAG_ONE = 0x0;
    public static final int FLAG_TWO = 0x2;

    public GetSettingsExCommand() { this.name = COMMAND_NAME; this.requiresPermissionCheck = false; this.requiresSingleThread = true; }

    @Override
    public Cursor handle(QueryPacket commandData) throws Throwable {
        if(commandData.isDump()) {
            return CursorUtil.toMatrixCursor_final(SettingsApi.dumpSettings(
                    commandData.getDatabase()),
                    marshall, 0);
        } else {
            return CursorUtil.toMatrixCursor_final(SettingsApi.getAllSettings(
                    commandData.getContext(),
                    commandData.getDatabase(),
                    commandData.getUserId(),
                    commandData.getCategory()), marshall, 0);
        }
    }

    public static List<SettingPacket> dump(Context context, boolean marshall) {
        return ListUtil.copyToArrayList(
                CursorUtil.readCursorAs_final(
                        XProxyContent.luaQuery(context, XProxyContent.commandName(COMMAND_NAME, marshall), new String[] { ActionPacket.ACTION_DUMP }),
                        marshall, SettingPacket.class));
    }

    public static Map<String, String> getAsMap(Context context, boolean marshall, int uid, String category, int extraFlags) {
        return ListUtil.toMap(
                get(context, marshall, uid, category, extraFlags),
                SettingPacket.TO_MAP);
    }

    public static List<SettingPacket> get(Context context, boolean marshall, int uid, String category, int extraFlags) {
        return ListUtil.copyToArrayList(
                CursorUtil.readCursorAs_final(
                        XProxyContent.luaQuery(context, XProxyContent.commandName(COMMAND_NAME, marshall),
                                UserIdentity.createSnakeQueryUID(uid, category).whereColumn(ActionPacket.FIELD_CODE, extraFlags).asSnake()),
                        marshall, SettingPacket.class));
    }
}
