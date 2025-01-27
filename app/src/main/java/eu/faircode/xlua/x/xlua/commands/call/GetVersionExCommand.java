package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.xlua.call.GetVersionCommand;
import eu.faircode.xlua.api.xlua.database.LuaAppManager;
import eu.faircode.xlua.api.xlua.provider.XLuaAppProvider;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;
import eu.faircode.xlua.utilities.BundleUtil;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.hook.AppProviderApi;

/**
 * ToDo: Make use of this, using this can tell if the Bridge has been Updated aka Device ReBoot
 */
public class GetVersionExCommand extends CallCommandHandlerEx {
    public GetVersionExCommand() {
        name = "getVersion";
        requiresPermissionCheck = false;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        int res = AppProviderApi.getVersion(commandData.getContext());
        return AppProviderApi.INVALID_VERSION == res ?
                null :
                BundleUtil.createSingleInt("version", res);
    }

    public static Bundle invoke(Context context) {
        return XProxyContent.luaCall(
                context,
                "getVersion");
    }

    public static int get(Context context) {
        return BundleUtil.readInteger(XProxyContent.luaCall(
                context,
                "getVersion"), "version", AppProviderApi.INVALID_VERSION);
    }
}
