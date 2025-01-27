package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.app.AppPacket;
import eu.faircode.xlua.api.xlua.provider.XLuaAppProvider;
import eu.faircode.xlua.api.xstandard.CallCommandHandler;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.hook.AppProviderApi;
import eu.faircode.xlua.x.xlua.hook.AppXpPacket;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;

@SuppressWarnings("all")
public class GetAppInfoCommand extends CallCommandHandlerEx {
    private static final String TAG = "XLua.GetAppInfoCommand";

    public GetAppInfoCommand() {
        name = "getExAppEx";
        requiresPermissionCheck = true;
        requiresSingleThread = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        if(DebugUtil.isDebug())
            Log.d(TAG, "Called Get App Info! Extras=" + Str.toStringOrNull(commandData.extras));

        return AppProviderApi.getApp(
                commandData.getContext(),
                commandData.getDatabase(),
                commandData.getUid(),
                commandData.getCategory(),
                true,
                true).toBundle();
    }

    public static AppXpPacket get(Context context, int uid, String packageName) {
        return new AppXpPacket(
                XProxyContent.luaCall(
                        context,
                        "getExAppEx",
                        UserIdentity.fromUid(uid, packageName).toIdentityBundle()));
    }
}
