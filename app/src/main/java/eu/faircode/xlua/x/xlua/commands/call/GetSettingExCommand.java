package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;
import eu.faircode.xlua.x.xlua.settings.data.SettingsApi;
import eu.faircode.xlua.x.xlua.settings.data.SettingsApiUtils;

public class GetSettingExCommand extends CallCommandHandlerEx {
    private static final String TAG = "XLua.GetSettingExCommand";

    public static final String SETTING_USE_DEFAULT = "useDefault";
    public static final String SETTING_COLLECTION = "collection";
    public static final String SETTING_FORCE_STOP = "forcestop";
    public static final String SETTING_THEME = "theme";
    public static final String SETTING_SHOW = "show";

    public static final String SETTING_RESTRICT_NEW_APPS = "restrict_new_apps";
    public static final String SETTING_NOTIFY_NEW_APPS = "notify_new_apps";

    public static final String SETTING_VERBOSE_DEBUG = "verbose_debug_logs";
    public static final String SETTING_SELECTED_CONFIG = "selected_config";

    public static final String SETTING_COLLECTION_DEFAULT = "PrivacyEx";
    public static final String SETTING_THEME_DEFAULT = "dark";


    //this.kill = GetSettingExCommand.getBool(context, GetSettingExCommand.SETTING_FORCE_STOP, uid, packageName); }

    public GetSettingExCommand() {
        name = "getExSettingEx";
        requiresPermissionCheck = false;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        return SettingsApi.getSettingOrBuiltInSetting(
                commandData.getDatabase(),
                        commandData.getUserId(),
                        commandData.getCategory(),
                        commandData.getExtraString(SettingPacket.FIELD_NAME)).toBundle();
    }

    public static String getConfig(Context context, int uid, String pkg) {
        if(!Str.isEmpty(pkg) && !UserIdentity.GLOBAL_NAMESPACE.equalsIgnoreCase(pkg)) {
            SettingPacket packet = get(context, SETTING_SELECTED_CONFIG, uid, pkg);
            return packet == null ? null : packet.value;
        }

        return null;
    }

    public static boolean getVerboseLogs(Context context) { return getBool(context, SETTING_VERBOSE_DEBUG,  Process.myUid(), SettingPacket.GLOBAL_CATEGORY); }
    public static boolean getVerboseLogs(Context context, int uid) { return getBool(context, SETTING_VERBOSE_DEBUG, uid, SettingPacket.GLOBAL_CATEGORY); }

    public static boolean restrictNewApps(Context context) { return getBool(context, SETTING_RESTRICT_NEW_APPS,  Process.myUid(), SettingPacket.GLOBAL_CATEGORY); }
    public static boolean restrictNewApps(Context context, int uid) { return getBool(context, SETTING_RESTRICT_NEW_APPS, uid, SettingPacket.GLOBAL_CATEGORY); }

    public static boolean notifyOnNewApps(Context context) { return getBool(context, SETTING_NOTIFY_NEW_APPS, Process.myUid(), SettingPacket.GLOBAL_CATEGORY); }
    public static boolean notifyOnNewApps(Context context, int uid) { return getBool(context, SETTING_NOTIFY_NEW_APPS, uid, SettingPacket.GLOBAL_CATEGORY); }

    public static String getShow(Context context, int uid) { return getGlobalValue(context, SETTING_SHOW, uid); }

    public static List<String> getCollections(Context context, int uid) { return Str.splitToList(get(context, SETTING_COLLECTION, uid, SettingPacket.GLOBAL_CATEGORY).value); }

    public static String getTheme(Context context, int uid) { return SettingsApiUtils.ensureIsTheme(get(context, SETTING_THEME, uid, SettingPacket.GLOBAL_CATEGORY).value); }

    public static boolean getBool(Context context, String settingName, int uid, String packageName) { return Str.toBool(get(context, settingName, uid, packageName).value); }

    public static String getGlobalValue(Context context, String settingName, int uid) { return getGlobal(context, settingName, uid).value; }
    public static SettingPacket getGlobal(Context context, String settingName, int uid) { return get(context, settingName, uid, SettingPacket.GLOBAL_CATEGORY); }

    public static SettingPacket get(Context context, String settingName, int uid, String packageName) { return get(context, settingName, UserIdentity.fromUid(uid, packageName)); }
    public static SettingPacket get(Context context, String settingName, UserIdentity userIdentity) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "Getting Setting, Setting Name=" + settingName + " User ID=" + userIdentity.hasUid());

        return new SettingPacket(
                settingName,
                null,
                ActionPacket.create(ActionFlag.GET, false), userIdentity).sendCallRequest(context, "getExSettingEx");
    }
}