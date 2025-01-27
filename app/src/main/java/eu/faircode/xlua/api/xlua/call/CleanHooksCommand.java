package eu.faircode.xlua.api.xlua.call;

import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.xstandard.CallCommandHandler;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;
import eu.faircode.xlua.api.xstandard.database.DatabaseHelp;
import eu.faircode.xlua.api.xstandard.database.SqlQuerySnake;

public class CleanHooksCommand extends CallCommandHandler {
    public CleanHooksCommand() {
        name = "clearHooksCommand";
        requiresPermissionCheck = true;
    }

    @Override
    public Bundle handle(CallPacket_old commandData) throws Throwable {
        int failed = 0;
        int succeeded = 0;
        int found = 0;
        XResult res = XResult.create().setMethodName("clearHooksCommand");
        try {
            XDatabaseOld db = commandData.getDatabase();
            List<XLuaHook> hooks = new ArrayList<>(DatabaseHelp.getFromDatabase(db, "hook", XLuaHook.class));
            for (XLuaHook hook : hooks) {
                if (hook.getId().startsWith("PrivacyEx")) {
                    found++;
                    SqlQuerySnake snake = SqlQuerySnake.create(db, "hook")
                            .whereColumn("id", hook.getId());

                    if(DatabaseHelp.deleteItem(snake)) succeeded++;
                    else failed++;
                }
            }
        }catch (Exception e) {
            return res.setFailed("\nFailed: e=" + e + "\nFound=" + found + "\nFailed=" + failed + "\nSucceeded=" + succeeded).toBundle();
        }

        return res.setSucceeded("\nFound=" + found + "\nFailed=" + failed + "\nSucceeded=" + succeeded).toBundle();
    }

    public static XResult invokeEx(Context context) { return new XResult(invoke(context)); }
    public static Bundle invoke(Context context) {
        return XProxyContent.luaCall(
                context,
                "clearHooksCommand",
                new Bundle());
    }
}
