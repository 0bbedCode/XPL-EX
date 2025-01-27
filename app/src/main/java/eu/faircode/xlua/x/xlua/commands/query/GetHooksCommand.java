package eu.faircode.xlua.x.xlua.commands.query;

import android.content.Context;
import android.database.Cursor;

import java.util.Collection;
import java.util.List;

import eu.faircode.xlua.UberCore888;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.hook.XLuaHookConversions;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.commands.QueryCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.QueryPacket;

public class GetHooksCommand extends QueryCommandHandlerEx {
    public GetHooksCommand() { this.name = "getHooks"; this.requiresSingleThread = true; this.requiresPermissionCheck = false; }

    @Override
    public Cursor handle(QueryPacket commandData) throws Throwable {
        return CursorUtil.toMatrixCursor(
                UberCore888.getHooksEx(commandData.getDatabase(), "all".equalsIgnoreCase(commandData.getSelectionAt(0))),
                marshall,
                XLuaHook.FLAG_WITH_LUA);
    }

    public static List<XLuaHook> getHooks(Context context, boolean marshall) { return ListUtil.copyToArrayList(XLuaHookConversions.fromCursor(invoke(context, marshall, false), marshall, true)); }
    public static List<XLuaHook> getHooks(Context context, boolean marshall, boolean all) { return ListUtil.copyToArrayList(XLuaHookConversions.fromCursor(invoke(context, marshall, all), marshall, true)); }
    public static Cursor invoke(Context context, boolean marshall, boolean all) {
        return XProxyContent.luaQuery(
                context,
                marshall ? "getHooks2" : "getHooks", all ? new String[] { "all" } : null);
    }
}
