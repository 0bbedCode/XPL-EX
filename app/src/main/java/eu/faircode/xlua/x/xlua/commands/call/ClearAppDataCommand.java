package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.hook.AppProviderApi;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;
import eu.faircode.xlua.x.xlua.identity.UserIdentityUtils;

public class ClearAppDataCommand extends CallCommandHandlerEx {
    public ClearAppDataCommand() {
        name = "clearAppData";
        requiresPermissionCheck = true;
        requiresSingleThread = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        String packageName = commandData.getCategory();
        return A_CODE.result(AppProviderApi.clearAppData(commandData.getContext(), packageName)).toBundle();
    }


    public static A_CODE clear(Context context, String packageName) {
        Bundle b = new Bundle();
        b.putString(UserIdentityIO.FIELD_CATEGORY, packageName);
        Bundle res = XProxyContent.luaCall(context, "clearAppData", b);
        return A_CODE.fromBundle(res);
    }
}
