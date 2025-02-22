package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;
import android.os.Process;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;
import eu.faircode.xlua.x.xlua.settings.data.SettingsApi;

public class PutSettingExCommand extends CallCommandHandlerEx {
    public PutSettingExCommand() {
        name = "putExSettingEx";
        requiresPermissionCheck = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        return SettingsApi.single_locked(
                        commandData.getDatabase(),
                        commandData.readExtraAs(SettingPacket.class))
                        .toBundle();
    }

    public static A_CODE call(Context context, SettingHolder holder, UserClientAppContext userContext, boolean kill, boolean delete) {
        SettingPacket packet = new SettingPacket(holder, userContext, ActionPacket.create(delete ? ActionFlag.DELETE : ActionFlag.PUSH, kill));
        Bundle b = packet.toBundle();
        return A_CODE.fromBundle(
                XProxyContent.luaCall(
                        context,
                        "putExSettingEx", b));
    }

    public static A_CODE call(Context context, SettingPacket packet) {
        return A_CODE.fromBundle(
                XProxyContent.luaCall(
                        context,
                        "putExSettingEx", packet.toBundle()));
    }

    public static A_CODE putTheme(Context context, String value) { return putGen(context, GetSettingExCommand.SETTING_THEME, value); }

    public static A_CODE putVerboseLogging(Context context, boolean verboseLogging) { return putGen(context, GetSettingExCommand.SETTING_VERBOSE_DEBUG, String.valueOf(verboseLogging)); }
    public static A_CODE putRestrictNewApps(Context context, boolean restrict) { return putGen(context, GetSettingExCommand.SETTING_RESTRICT_NEW_APPS, String.valueOf(restrict)); }
    public static A_CODE putNotifyNewApps(Context context, boolean notify) { return putGen(context, GetSettingExCommand.SETTING_NOTIFY_NEW_APPS, String.valueOf(notify)); }


    public static A_CODE putForceStop(Context context, int uid, String packageName, boolean forceStop) {
        if(!Str.isEmpty(packageName) && !UserIdentity.GLOBAL_NAMESPACE.equalsIgnoreCase(packageName)) {
            SettingPacket packet = new SettingPacket(GetSettingExCommand.SETTING_FORCE_STOP, String.valueOf(forceStop));
            packet.setUserIdentity(UserIdentity.fromUid(uid, packageName));
            packet.setActionPacket(ActionPacket.create(ActionFlag.PUSH, false));
            return call(context, packet);
        }

        return A_CODE.FAILED;
    }



    public static A_CODE putGen(Context context, String settingName, String value) {
        SettingPacket packet = new SettingPacket(settingName, value);
        packet.setUserIdentity(UserIdentity.fromUid(Process.myUid(), UserIdentity.GLOBAL_NAMESPACE));
        packet.setActionPacket(ActionPacket.create(ActionFlag.PUSH, false));
        return call(context, packet);
    }

    public static A_CODE putConfig(Context context, int uid, String packageName, String value) {
        if(!Str.isEmpty(packageName) && !UserIdentity.GLOBAL_NAMESPACE.equalsIgnoreCase(packageName)) {
            SettingPacket packet = new SettingPacket(GetSettingExCommand.SETTING_SELECTED_CONFIG, value);
            packet.setUserIdentity(UserIdentity.fromUid(uid, packageName));
            packet.setActionPacket(ActionPacket.create(ActionFlag.PUSH, false));
            return call(context, packet);
        }

        return A_CODE.FAILED;
    }
}
