package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.XPacket;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.configs.ConfigApi;
import eu.faircode.xlua.x.xlua.configs.XPConfig;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;
import eu.faircode.xlua.x.xlua.settings.data.SettingsApi;

public class PutConfigCommand extends CallCommandHandlerEx {
    private static final String TAG = "XLua.PutConfigCommand";

    public static final String COMMAND_NAME = "putConfig"; //Intermap <Clazz, String> COMMNAND_CLASS, NAME, so it can be called from Packet as Clazz

    public PutConfigCommand() {
        name = COMMAND_NAME;
        requiresPermissionCheck = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        XPacket<XPConfig> packet = new XPacket<XPConfig>();
        packet.fromBundle(commandData.extras, XPConfig.class);

        if(DebugUtil.isDebug())
            Log.d(TAG, "Command Handler got Config Packet! Packet=" + Str.toStringOrNull(packet));

        return ConfigApi.single_locked(commandData.getDatabase(), packet).toBundle();
    }
}
