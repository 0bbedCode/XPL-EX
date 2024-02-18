package eu.faircode.xlua.api.xlua.xcall;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import eu.faircode.xlua.XGlobalCore;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.objects.CallCommandHandler;
import eu.faircode.xlua.api.objects.CallPacket;
import eu.faircode.xlua.api.xlua.XAppProvider;
import eu.faircode.xlua.api.xlua.XSettingsDatabase;
import eu.faircode.xlua.api.objects.xlua.packets.SettingPacket;
import eu.faircode.xlua.utilities.BundleUtil;

public class PutSettingCommand extends CallCommandHandler {
    public static PutSettingCommand create() { return new PutSettingCommand(); };

    public PutSettingCommand() {
        name = "putSetting";
        requiresPermissionCheck = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        throwOnPermissionCheck(commandData.getContext());
        SettingPacket packet = commandData.read(SettingPacket.class);

        boolean result = XSettingsDatabase.putSetting(commandData.getDatabase(), packet);

        if(result && (packet.getKill() != null && packet.getKill()))
            XAppProvider.forceStop(commandData.getContext(), packet.getCategory(), packet.getUser());

        return BundleUtil.createResultStatus(result);
    }

    public static Bundle invoke(Context context, Integer userId, String category, String name, String value) { return invoke(context, userId, category, name, value, false); }
    public static Bundle invoke(Context context, Integer userId, String category, String name, String value, Boolean kill) {
        SettingPacket packet = new SettingPacket(userId, category, name, value, kill);
        return invoke(context, packet);
    }

    public static Bundle invoke(Context context, SettingPacket packet) {
        return XProxyContent.luaCall(
                context,
                "putSetting",
                packet.toBundle());
    }
}
