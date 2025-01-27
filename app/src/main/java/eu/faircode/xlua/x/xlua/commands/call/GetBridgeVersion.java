package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.utilities.BundleUtil;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.hook.AppProviderApi;

public class GetBridgeVersion extends CallCommandHandlerEx {
    public static int BRIDGE_VERSION = 150; //Since this is Static in the Context of the Service it will not Update until User Reboot ?
                                            //Do more cache ing with this ?
    public GetBridgeVersion() {
        name = "getBridgeVersion";
        requiresPermissionCheck = false;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        int res = AppProviderApi.getVersion(commandData.getContext());
        return AppProviderApi.INVALID_VERSION == res ?
                null :
                BundleUtil.createSingleInt("version", BRIDGE_VERSION);
    }

    public static boolean isUpdated(Context context) {
        return get(context) == BRIDGE_VERSION;  //Compare User Context App to Service Side Context
    }

    public static int get(Context context) {
        return BundleUtil.readInteger(XProxyContent.luaCall(
                context,
                "getBridgeVersion"), "version", 0);
    }
}
