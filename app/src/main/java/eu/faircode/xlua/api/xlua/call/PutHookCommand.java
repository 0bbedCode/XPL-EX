package eu.faircode.xlua.api.xlua.call;

import android.content.Context;
import android.os.Bundle;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.xstandard.CallCommandHandler;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;
import eu.faircode.xlua.api.hook.LuaHookPacket;
import eu.faircode.xlua.api.xlua.provider.XLuaHookProvider;

public class PutHookCommand extends CallCommandHandler {
    public static PutHookCommand create() { return new PutHookCommand(); };
    public PutHookCommand() {
        name = "putHook";
        requiresPermissionCheck = true;
    }

    @Override
    public Bundle handle(CallPacket_old commandData) throws Throwable {
        LuaHookPacket packet = commandData.readExtrasAs(LuaHookPacket.class);
        if(packet == null) return XResult.fromInvalidPacket(name, LuaHookPacket.class).toBundle();
        return XLuaHookProvider.putHook(
                commandData.getContext(),
                commandData.getDatabase(),
                packet).toBundle();
    }

    public static Bundle invoke(Context context, LuaHookPacket packet) {
        return XProxyContent.luaCall(
                context,
                "putHook",
                packet.toBundle());
    }
}
