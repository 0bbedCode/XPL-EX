package eu.faircode.xlua.api.xlua.call;

import android.content.Context;
import android.os.Bundle;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.xstandard.CallCommandHandler;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;
import eu.faircode.xlua.api.xlua.database.LuaAppManager;
import eu.faircode.xlua.api.app.LuaSimplePacket;

public class ClearAppCommand extends CallCommandHandler {
    public static ClearAppCommand create() { return new ClearAppCommand(); };
    public ClearAppCommand() {
        name = "clearApp";
        requiresPermissionCheck = true;
    }

    @Override
    public Bundle handle(CallPacket_old commandData) throws Throwable {
        LuaSimplePacket packet = commandData.readExtrasAs(LuaSimplePacket.class);
        if(packet == null) return XResult.fromInvalidPacket(name, LuaSimplePacket.class).toBundle();
        return XResult.create()
                .setMethodName("clearApp")
                .setExtra(packet.toString())
                .setResult(LuaAppManager.clearApp(
                        commandData.getContext(), commandData.getDatabase(), packet)).toBundle();
    }

    public static Bundle invoke(Context context, LuaSimplePacket packet) {
        return XProxyContent.luaCall(
                context,
                "clearApp",
                packet.toBundle());
    }
}
