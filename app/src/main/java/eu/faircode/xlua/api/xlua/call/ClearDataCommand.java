package eu.faircode.xlua.api.xlua.call;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.app.LuaSimplePacket;
import eu.faircode.xlua.api.xstandard.CallCommandHandler;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;
import eu.faircode.xlua.api.xlua.database.LuaAppManager;
import eu.faircode.xlua.utilities.BundleUtil;

public class ClearDataCommand extends CallCommandHandler {
    private static final String TAG = "XLua.ClearDataCommand";

    @SuppressWarnings("unused")
    public ClearDataCommand() {
        name = "clearData";
        requiresPermissionCheck = true;
    }

    @Override
    public Bundle handle(CallPacket_old commandData) throws Throwable {
        int packetUid = commandData.getExtras().getInt("user");
        int callingUid = Binder.getCallingUid();
        if(DebugUtil.isDebug())
            Log.d(TAG, "[ClearDataCommand] Command Handler executed! " +
                    "Packet UID=" + packetUid +
                    "\nAppId=" + XUtil.getAppId(packetUid) +
                    "\nUserId=" + XUtil.getUserId(packetUid) +
                    "\nUserUid=" + XUtil.getUserUid(packetUid, XUtil.getAppId(packetUid)) +
                    "\nCallingUid=" + callingUid +
                    "\nCallingUid AppId=" + XUtil.getAppId(callingUid) +
                    "\nCallingUid UserId=" + XUtil.getUserId(callingUid) +
                    "\nCallingUid UserUid=" + XUtil.getUserUid(callingUid, XUtil.getAppId(callingUid)));

        return BundleUtil.createResultStatus(
                LuaAppManager.clearData(
                        packetUid,
                        commandData.getDatabase()));
    }

    public static Bundle invoke(Context context, LuaSimplePacket packet) {
        return XProxyContent.luaCall(
                context,
                "clearData",
                packet.toBundle());
    }
}
