package eu.faircode.xlua.api.xlua.query;

import android.content.Context;
import android.database.Cursor;

import eu.faircode.xlua.UberCore888;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.xstandard.QueryCommandHandler;
import eu.faircode.xlua.api.xstandard.command.QueryPacket_old;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.utilities.CursorUtil;

public class GetHooksCommand extends QueryCommandHandler {
    @SuppressWarnings("unused")
    public GetHooksCommand() { this(false); }
    public GetHooksCommand(boolean marshall) {
        this.marshall = marshall;
        this.name = marshall ? "getHooks2" : "getHooks";
        requiresPermissionCheck = false;
        requiresSingleThread = true;
    }

    @Override
    public Cursor handle(QueryPacket_old commandData) throws Throwable {
        String[] selection = commandData.getSelection();
        boolean all = (selection != null && selection.length == 1 && "all".equals(selection[0]));
        return CursorUtil.toMatrixCursor(
                UberCore888.getHooks(commandData.getContext(), commandData.getDatabase(), all),
                marshall,
                XLuaHook.FLAG_WITH_LUA);
    }

    public static Cursor invoke(Context context, boolean marshall) {
        return XProxyContent.luaQuery(
                context,
                marshall ? "getHooks2" : "getHooks");
    }

    public static Cursor invoke(Context context, boolean marshall, boolean all) {
        return XProxyContent.luaQuery(
                context,
                marshall ? "getHooks2" : "getHooks", new String[] { all ? "all" : "some" });
    }
}
