package eu.faircode.xlua.x.xlua.commands.query;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
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
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

public class GetHooksCommand extends QueryCommandHandlerEx {
    private static final String TAG = LibUtil.generateTag(GetHooksCommand.class);
    public static final String COMMAND_NAME = "getHooks";

    public GetHooksCommand() { this.name = COMMAND_NAME; this.requiresSingleThread = true; this.requiresPermissionCheck = false; }

    @Override
    public Cursor handle(QueryPacket commandData) throws Throwable {
        if(commandData.isDump()) {
            return CursorUtil.toMatrixCursor(
                    DatabaseHelpEx.getFromDatabase(
                            commandData.getDatabase(),
                            XLuaHook.Table.name,
                            XLuaHook.class,
                            true),
                    marshall,
                    XLuaHook.FLAG_WITH_LUA);
        } else {
            return CursorUtil.toMatrixCursor(
                    UberCore888.getHooksEx(commandData.getDatabase(), commandData.isAll()),
                    marshall,
                    XLuaHook.FLAG_WITH_LUA);
        }
    }

    public static List<XLuaHook> dump(Context context, boolean marshall) {
        return ListUtil.copyToArrayList(
                XLuaHookConversions.fromCursor(
                        XProxyContent.luaQuery(context, XProxyContent.commandName(COMMAND_NAME, marshall), new String[] { ActionPacket.ACTION_DUMP }),
                        marshall, true));
    }

    public static List<XLuaHook> getHooks(Context context, boolean marshall) { return ListUtil.copyToArrayList(XLuaHookConversions.fromCursor(invoke(context, marshall, false), marshall, true)); }
    public static List<XLuaHook> getHooks(Context context, boolean marshall, boolean all) { return ListUtil.copyToArrayList(XLuaHookConversions.fromCursor(invoke(context, marshall, all), marshall, true)); }
    public static Cursor invoke(Context context, boolean marshall, boolean all) {
        return XProxyContent.luaQuery(
                context,
                XProxyContent.commandName(COMMAND_NAME, marshall), all ? new String[] { ActionPacket.ACTION_ALL } : null);
    }
}
