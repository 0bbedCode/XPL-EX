package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;

import java.util.List;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.utilities.BundleUtil;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.configs.AppProfileUtils;
import eu.faircode.xlua.x.xlua.configs.PathDetails;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.hook.AppProviderApi;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;
import eu.faircode.xlua.x.xlua.identity.UserIdentityUtils;

public class ForceStopAppCommand extends CallCommandHandlerEx {
    public ForceStopAppCommand() {
        name = "forceToStop";
        requiresPermissionCheck = true;
        requiresSingleThread = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        int uid = commandData.getUid();
        String packageName = commandData.getCategory();
        return A_CODE.result(AppProviderApi.forceStop(commandData.getContext(), UserIdentityUtils.getUserId(uid), packageName)).toBundle();
    }


    public static A_CODE stop(Context context, int uid, String packageName) {
        Bundle b = new Bundle();
        b.putInt(UserIdentityIO.FIELD_UID, uid);
        b.putString(UserIdentityIO.FIELD_CATEGORY, packageName);
        Bundle res = XProxyContent.luaCall(context, "forceToStop", b);
        return A_CODE.fromBundle(res);
    }
}
