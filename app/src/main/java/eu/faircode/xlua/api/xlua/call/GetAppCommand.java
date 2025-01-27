package eu.faircode.xlua.api.xlua.call;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.app.AppPacket;
import eu.faircode.xlua.api.xstandard.CallCommandHandler;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;
import eu.faircode.xlua.api.xlua.provider.XLuaAppProvider;

public class GetAppCommand extends CallCommandHandler {
    @SuppressWarnings("unused")
    public GetAppCommand() {
        name = "getApp";
        requiresPermissionCheck = true;
        requiresSingleThread = true;
    }

    @Override
    public Bundle handle(CallPacket_old commandData) throws Throwable {
        AppPacket packet = commandData.readExtrasAs(AppPacket.class);
        if(packet == null)
            throw new Exception("App Packet was NULL... [getApp]");

        return XLuaAppProvider.getApp(
                commandData.getContext(),
                commandData.getDatabase(),
                Binder.getCallingUid(),
                packet.getCategory(),
                packet.isInitForceStop(),
                packet.isInitSettings()).toBundle();
    }

    public static Bundle invoke(Context context, AppPacket packet) {
        return XProxyContent.luaCall(
                context,
                "getApp",
                packet.toBundle());
    }
}
