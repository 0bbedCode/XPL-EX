package eu.faircode.xlua.api.xmock.database;

import android.content.Context;

import java.util.Collection;
import java.util.HashMap;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.UberCore888;
import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.settings.LuaSetting;
import eu.faircode.xlua.api.settings.LuaSettingDefault;
import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.api.settings.LuaSettingPacket;
import eu.faircode.xlua.api.xstandard.UserIdentityPacket;
import eu.faircode.xlua.api.xstandard.database.DatabaseHelp;
import eu.faircode.xlua.api.xstandard.database.SqlQuerySnake;
import eu.faircode.xlua.api.xlua.provider.XLuaAppProvider;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.utilities.CollectionUtil;
import eu.faircode.xlua.utilities.StringUtil;

public class LuaSettingsManager {
    private static final String TAG = "XLua.LuaSettingsDatabase";
    private static final String JSON = "settingdefaults.json";
    private static final int COUNT = 5;

    public static final String USE_DEFAULT = "Usedefault";
    public static final String DEFAULT_THEME = "dark";
    public static final String DEFAULT_COLLECTIONS = "Privacy,PrivacyEx";

    public static final String SETTING_THEME = "theme";
    public static final String SETTING_COLLECTION = "collection";

    public static XResult putSetting(Context context, XDatabaseOld db, String settingName) { return putSetting(context, db, UserIdentityPacket.GLOBAL_USER, UserIdentityPacket.GLOBAL_NAMESPACE, settingName, null, false); }
    public static XResult putSetting(Context context, XDatabaseOld db, String settingName, String value) { return putSetting(context, db, UserIdentityPacket.GLOBAL_USER, UserIdentityPacket.GLOBAL_NAMESPACE, settingName, value, false); }
    public static XResult putSetting(Context context, XDatabaseOld db, Integer user, String category, String settingName) { return putSetting(context, db, user, category, settingName, null); }
    public static XResult putSetting(Context context, XDatabaseOld db, Integer user, String category, String settingName, String value) { return putSetting(context, db, user, category, settingName, value, false); }
    public static XResult putSetting(Context context, XDatabaseOld db, Integer user, String category, String settingName, String value, Boolean kill) { return putSetting(context, db, LuaSettingPacket.create(user, category, settingName, value, null, LuaSettingPacket.getCodeFromValue(value), kill)); }
    public static XResult putSetting(Context context, XDatabaseOld db, LuaSettingPacket setting) {
        XResult res = XResult.create().setMethodName("putSetting").setExtra(setting.toString());
        setting.ensureIdentification();
        boolean result =
                !setting.isDeleteSetting() && Str.isValid(setting.getValue()) ?
                        DatabaseHelp.insertItem(
                                db,
                                LuaSetting.Table.NAME,
                                LuaSetting.create(setting)) :
                        DatabaseHelp.deleteItem(
                                SqlQuerySnake.create(db, LuaSetting.Table.NAME)
                                        .whereColumn(LuaSetting.Table.FIELD_USER, setting.getUser())
                                        .whereColumn(LuaSetting.Table.FIELD_CATEGORY, setting.getCategory())
                                        .whereColumn(LuaSetting.Table.FIELD_NAME, setting.getName()));//we should query via packageName

        if (result && setting.isKill())
            XLuaAppProvider.forceStop(context, setting.getCategory(), setting.getUser(), res);

        return res.setResult(result);
    }

    public static boolean getSettingBoolean(Context context, XDatabaseOld db, String settingName) { return Boolean.parseBoolean(getSettingValue(context, db, settingName, UserIdentityPacket.GLOBAL_USER, UserIdentityPacket.GLOBAL_NAMESPACE)); }
    public static boolean getSettingBoolean(Context context, XDatabaseOld db, String settingName, int user, String category) { return Boolean.parseBoolean(getSettingValue(context, db, settingName, user, category)); }

    public static String getSettingValue(Context context, XDatabaseOld db, String settingName) { return getSettingValue(context, db, settingName, UserIdentityPacket.GLOBAL_USER, UserIdentityPacket.GLOBAL_NAMESPACE); }
    public static String getSettingValue(Context context, XDatabaseOld db, String settingName, int user, String category) {  return getSettingValue(context, db, LuaSettingPacket.create(user, category, settingName)); }
    public static String getSettingValue(Context context, XDatabaseOld db, LuaSettingPacket packet) {
        packet.ensureIdentification();
        String v = SqlQuerySnake
                .create(db, LuaSetting.Table.NAME)
                .whereColumn(LuaSetting.Table.FIELD_USER, packet.getUser())
                .whereColumn(LuaSetting.Table.FIELD_CATEGORY, packet.getCategory())
                .whereColumn(LuaSetting.Table.FIELD_NAME, packet.getName())
                .queryGetFirstString(LuaSetting.Table.FIELD_VALUE, true);
        if(v == null) {
            if(packet.getName().equalsIgnoreCase(SETTING_THEME)) {
                putSetting(context, db, LuaSettingPacket.create(packet.getName(), DEFAULT_THEME, LuaSettingPacket.CODE_INSERT_UPDATE_SETTING));
                return DEFAULT_THEME;
            }
            else if(packet.getName().equalsIgnoreCase(SETTING_COLLECTION)) {
                putSetting(context, db, LuaSettingPacket.create(packet.getName(), DEFAULT_COLLECTIONS, LuaSettingPacket.CODE_INSERT_UPDATE_SETTING));
                return DEFAULT_COLLECTIONS;
            }else if(!packet.isGlobal()) {
                v = SqlQuerySnake
                        .create(db, LuaSetting.Table.NAME)
                        .whereColumn(LuaSetting.Table.FIELD_USER, UserIdentityPacket.GLOBAL_USER)
                        .whereColumn(LuaSetting.Table.FIELD_CATEGORY, UserIdentityPacket.GLOBAL_NAMESPACE)
                        .whereColumn(LuaSetting.Table.FIELD_NAME, packet.getName())
                        .queryGetFirstString(LuaSetting.Table.FIELD_VALUE, true);
            }

            if(v == null && packet.isGetValueOrDefault())
                return getDefaultMappedSettingValue(db, packet.getName());
        }

        return v;
    }

    public static Collection<LuaSetting> getSettings(XDatabaseOld db, LuaSettingPacket packet) { return getSettings(db, packet.getUser(), packet.getCategory()); }
    public static Collection<LuaSetting> getSettings(XDatabaseOld db, int user, String category) {
        return SqlQuerySnake
                .create(db, LuaSetting.Table.NAME)
                .ensureDatabaseIsReady()
                .whereColumn(LuaSetting.Table.FIELD_USER, user)
                .whereColumn(LuaSetting.Table.FIELD_CATEGORY, category)
                .queryAs(LuaSetting.class, true);
    }

    public static Collection<LuaSettingExtended> getAllSettings(Context context, XDatabaseOld db, LuaSettingPacket packet) { return getAllSettings(context, db, packet.getUser(), packet.getCategory());  }
    public static Collection<LuaSettingExtended> getAllSettings(Context context, XDatabaseOld db, int user, String category) {
        HashMap<String, LuaSettingExtended> allSettings = new HashMap<>();
        Collection<LuaSettingDefault> mappedSettings = getMappedSettings(context, db);
        Collection<LuaSetting> userSettings = getSettings(db, user, category);

        XLog.i("User Settings Size=" + userSettings.size() + " pkg=" + category + " user=" + user);
        for(LuaSettingDefault s : mappedSettings) {
            if(!StringUtil.isValidAndNotWhitespaces(s.getName())) continue;
            if(s.getName().equalsIgnoreCase(USE_DEFAULT)) continue;
            LuaSettingExtended ss = new LuaSettingExtended(s);
            ss.setValueForce(null);
            allSettings.put(s.getName(), ss);
        }

        XLog.i("All settings=" + allSettings.size() + " mapped settings=" + mappedSettings.size()  + " user settings=" + userSettings.size());
        if(CollectionUtil.isValid(userSettings)) {
            for(LuaSetting s : userSettings) {
                String sName = s.getName();
                if(!StringUtil.isValidAndNotWhitespaces(sName)) continue;
                LuaSettingDefault set = allSettings.get(sName);
                if(set != null) { set.setValue(s.getValue());
                }else { allSettings.put(sName, new LuaSettingExtended(s)); }
            }
        }

        XLog.i("All settings before parsing=" + allSettings.size());
        Collection<XLuaHook> hooks = UberCore888.getHooks(context, db, true);
        for(XLuaHook hook : hooks) {
            String[] settings = hook.getSettings();
            if(settings == null || settings.length < 1)
                continue;

            for(String s : settings) {
                if(!StringUtil.isValidAndNotWhitespaces(s)) continue;
                if(!allSettings.containsKey(s)) {
                    LuaSettingExtended obj = new LuaSettingExtended();
                    obj.setUser(user);
                    obj.setCategory(category);
                    obj.setName(s);
                    allSettings.put(s, obj);
                }
            }
        }

        XLog.i("All settings after parsing=" + allSettings.size());
        return allSettings.values();
    }

    public static LuaSetting getSetting(XDatabaseOld db, String settingName, int user, String category) { return getSetting(db, LuaSettingPacket.create(user, category, settingName)); }
    public static LuaSetting getSetting(XDatabaseOld db, LuaSettingPacket packet) {
        packet.ensureIdentification();
        return SqlQuerySnake
                .create(db, LuaSetting.Table.NAME)
                .whereColumns(LuaSetting.Table.FIELD_USER, LuaSetting.Table.FIELD_CATEGORY, LuaSetting.Table.FIELD_NAME)
                .whereColumnValues(Integer.toString(packet.getCode()), packet.getCategory(), packet.getName())
                .queryGetFirstAs(LuaSetting.class, true);
    }

    public static XResult putDefaultMappedSetting(Context context, XDatabaseOld db, LuaSettingPacket setting) {
        XLog.i("Setting=" + setting.toString() + " Is Delete=" + setting.isDeleteDefault());
        XResult res = XResult.create().setMethodName("putDefaultSetting").setExtra(setting.toString());
        boolean result =
                !setting.isDelete() ?
                        DatabaseHelp.insertItem(
                                db,
                                LuaSettingDefault.Table.NAME,
                                setting.createDefault()) :
                        DatabaseHelp.deleteItem(
                                SqlQuerySnake
                                        .create(db, LuaSettingDefault.Table.NAME)
                                        .whereColumn(LuaSettingDefault.Table.FIELD_NAME, setting.getName()));

        //if(setting.isDeleteDefault() && !result)
        //    result = SqlQuerySnake.create(db, LuaSettingDefault.Table.NAME)
        //            .whereColumn(LuaSettingDefault.Table.FIELD_NAME, setting.getName())
        //            .exists();

        return res.setResult(result);
    }

    public static String getDefaultMappedSettingValue(XDatabaseOld db, String settingName) {
        return SqlQuerySnake
                .create(db, LuaSettingDefault.Table.NAME)
                .ensureDatabaseIsReady()
                .whereColumn(LuaSettingDefault.Table.FIELD_NAME, settingName)
                .queryGetFirstString(LuaSettingDefault.Table.FIELD_DEFAULT_VALUE, true);
    }

    public static LuaSettingDefault getMappedSetting(Context context, XDatabaseOld db, String settingName) {
        return SqlQuerySnake
                .create(db, LuaSettingDefault.Table.NAME)
                .ensureDatabaseIsReady()
                .whereColumn(LuaSettingDefault.Table.FIELD_NAME, settingName)
                .queryGetFirstAs(LuaSettingDefault.class, true);
    }

    public static Collection<LuaSettingDefault> getMappedSettings(Context context, XDatabaseOld db) {
        return DatabaseHelp.getOrInitTable(
                context,
                db,
                LuaSettingDefault.Table.NAME,
                LuaSettingDefault.Table.COLUMNS,
                JSON,
                true,
                LuaSettingDefault.class,
                COUNT);
    }

    public static boolean compareJsonToDatabase(Context context, XDatabaseOld db) {
        return DatabaseHelp.compareJsonWithDatabase(
                context,
                db,
                LuaSettingDefault.Table.NAME,
                LuaSettingDefault.Table.COLUMNS,
                JSON,
                LuaSettingDefault.class,
                true);
    }

    public static boolean prepareDatabaseTable(XDatabaseOld db) { return DatabaseHelp.prepareDatabase(db, LuaSetting.Table.NAME, LuaSetting.Table.columns); }
    public static boolean forceDatabaseCheck(Context context, XDatabaseOld db) {
        return DatabaseHelp.prepareTableIfMissingOrInvalidCount(
                context,
                db,
                LuaSettingDefault.Table.NAME,
                LuaSettingDefault.Table.COLUMNS,
                JSON,
                true,
                LuaSettingDefault.class,
                DatabaseHelp.DB_FORCE_CHECK);
    }
}
