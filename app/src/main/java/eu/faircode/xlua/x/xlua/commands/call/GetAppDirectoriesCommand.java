package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.utilities.BundleUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.configs.AppProfileUtils;
import eu.faircode.xlua.x.xlua.configs.PathDetails;
import eu.faircode.xlua.x.xlua.hook.AppProviderApi;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.identity.UserIdentityUtils;

public class GetAppDirectoriesCommand extends CallCommandHandlerEx {
    public GetAppDirectoriesCommand() {
        name = "getAppDirectories";
        requiresPermissionCheck = false;    //ToDO: Flip to true!
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        List<PathDetails> paths = AppProfileUtils.getAppDirectories(commandData.getUserId(), commandData.getCategory());
        Bundle b = new Bundle();
        b.putString("paths", PathDetails.encodeDetails(paths));
        return b;
    }

    public static List<PathDetails> get(Context context, UserIdentity identity) {
        return PathDetails.fromEncoded(
                BundleUtil.readString(XProxyContent.luaCall(context, "getAppDirectories", identity.toIdentityBundle()),
                        "paths", ""));
    }
}
