package eu.faircode.xlua.api.xmock.call;

import android.content.Context;
import android.os.Bundle;

import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.settings.LuaSettingPacket;
import eu.faircode.xlua.api.xlua.provider.XLuaAppProvider;
import eu.faircode.xlua.api.xstandard.CallCommandHandler;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;

public class ClearAppDataCommand extends CallCommandHandler {
    @SuppressWarnings("unused")
    public ClearAppDataCommand() {
        name = "clearAppData";
        requiresPermissionCheck = true;
    }

    @Override
    public Bundle handle(CallPacket_old commandData) throws Throwable {
        LuaSettingPacket packet = commandData.readExtrasAs(LuaSettingPacket.class);
        XResult res = XResult.create().setMethodName("killApp").setExtra(packet.toString());
        XLuaAppProvider.forceStop(
                commandData.getContext(),
                packet.getCategory(),
                XUtil.getUserId(packet.getUser()),
                res);

        XLuaAppProvider.clearAppData(commandData.getContext(), packet.getCategory());
        return XResult.create().setSucceeded("Finished").toBundle();
    }

    public static XResult invokeEx(Context context, String packageName, int uid) {
        LuaSettingPacket packet = new LuaSettingPacket();
        packet.setCategory(packageName);
        packet.setUser(uid);
        return XResult.from(invoke(context, packet));
    }

    public static Bundle invoke(Context context, LuaSettingPacket packet) {
        return XProxyContent.mockCall(
                context,
                "clearAppData",
                packet.toBundle());
    }
}
