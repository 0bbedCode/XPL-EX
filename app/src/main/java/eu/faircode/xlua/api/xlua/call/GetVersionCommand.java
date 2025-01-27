package eu.faircode.xlua.api.xlua.call;

import android.content.Context;
import android.os.Bundle;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.xstandard.CallCommandHandler;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;
import eu.faircode.xlua.api.xlua.provider.XLuaAppProvider;
import eu.faircode.xlua.utilities.BundleUtil;

public class GetVersionCommand extends CallCommandHandler {
    public static GetVersionCommand create() { return new GetVersionCommand(); };
    public GetVersionCommand() {
        name = "getVersion";
        requiresPermissionCheck = false;
    }

    @Override
    public Bundle handle(CallPacket_old commandData) throws Throwable {
        int res = XLuaAppProvider.getVersion(commandData.getContext());
        if(res == -55) return null;
        return BundleUtil.createSingleInt("version", res);
    }

    public static Bundle invoke(Context context) {
        return XProxyContent.luaCall(
                context,
                "getVersion");
    }
}
