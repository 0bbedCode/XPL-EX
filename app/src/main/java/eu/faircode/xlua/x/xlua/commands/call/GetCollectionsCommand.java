package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;

import java.util.List;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.utilities.BundleUtil;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.hook.AppProviderApi;

public class GetCollectionsCommand extends CallCommandHandlerEx {
    public GetCollectionsCommand() {
        name = "getAllCollections";
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
                "getAllCollections");
    }

    public List<String> getAllCollections(Context context) {
        //
        return null;
    }
}
