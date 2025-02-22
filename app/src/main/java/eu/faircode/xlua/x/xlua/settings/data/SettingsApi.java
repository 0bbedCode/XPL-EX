package eu.faircode.xlua.x.xlua.settings.data;

import android.content.ContentValues;
import android.content.Context;
import android.os.Process;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.database.sql.SQLSnake;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;

public class SettingsApi {
    private static final String TAG = "XLua.SettingsApi";

    private static final String JSON = "settingdefaults.json";
    private static final int COUNT = 5;
    public static final String USE_DEFAULT = "Usedefault";


    public static final String DATABASE_TABLE_NAME = SettingPacket.TABLE_NAME;

    public static A_CODE single_locked(SQLDatabase db, SettingPacket packet) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "Settings Api Action single, Packet=" + Str.toStringOrNull(packet));

        A_CODE code = SettingsApiUtils.ensureRead(db, packet);
        if(code != A_CODE.NONE)
            return code;

        if(GetSettingExCommand.SETTING_VERBOSE_DEBUG.equalsIgnoreCase(packet.name)) {
            boolean enable = !ActionFlag.isDelete(packet.getActionFlags()) && Str.toBoolean(packet.value ,false);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Setting Service Side Verbose Debug Logging flag=" + enable);

            //Make sure we have proper start up for this flag
            DebugUtil.setForceDebug(enable);
        }

        switch (packet.getActionFlags()) {
            case UPDATE:
            case PUSH:
                code = SettingsApiUtils.resultToCode(DatabaseHelpEx.insertItem(db, DATABASE_TABLE_NAME, packet));
                break;
            case DELETE:
                code = SettingsApiUtils.resultToCode(DatabaseHelpEx.deleteItem(
                        SQLSnake.create(db, SettingPacket.TABLE_NAME)
                                .whereIdentity(packet.getUserIdentity().getUserId(true), packet.getCategory())
                                .whereColumn(SettingPacket.FIELD_NAME, packet.name)
                                .asSnake()));
                                break;
        }

        //Do Is Kill ?

        return code;
    }

    public static SettingPacket getVerboseState(SQLDatabase db, int userId) {
        return getSettingOrDefault(
                db,
                userId,
                SettingPacket.GLOBAL_CATEGORY,
                GetSettingExCommand.SETTING_VERBOSE_DEBUG,
                "false");
    }

    public static String getThemeValue(SQLDatabase db, int userId) { return getTheme(db, userId).value; }
    public static SettingPacket getTheme(SQLDatabase db, int userId) {
        return getSettingOrDefault(
                db,
                userId,
                SettingPacket.GLOBAL_CATEGORY,
                GetSettingExCommand.SETTING_THEME,
                GetSettingExCommand.SETTING_THEME_DEFAULT);
    }

    public static List<String> getCollectionsValue(SQLDatabase db, int userId) { return Str.splitToList(getCollections(db, userId).value); }
    public static SettingPacket getCollections(SQLDatabase db, int userId) {
        return getSettingOrDefault(
                db,
                userId,
                SettingPacket.GLOBAL_CATEGORY,
                GetSettingExCommand.SETTING_COLLECTION,
                GetSettingExCommand.SETTING_COLLECTION_DEFAULT);
    }

    public static SettingPacket getSettingOrDefault(SQLDatabase db, int userId, String category, String name, String defaultValue) {
        SettingPacket packet = getSetting(db, userId, category, name);
        if(packet == null || packet.value == null) {
            packet = SettingPacket.create(
                    name,
                    defaultValue,
                    ActionPacket.create(ActionFlag.PUSH, false),
                    UserIdentity.fromUid(Process.myUid(), category));

            ContentValues cv = packet.toContentValues();
            db.beginTransaction();
            db.endTransaction(false, db.insert(SettingPacket.TABLE_NAME, cv));
        }

        return packet;
    }

    public static Collection<SettingPacket> dumpSettings(SQLDatabase db) {
        if(!DatabaseHelpEx.ensureTableIsReady(SettingPacket.TABLE_INFO, db))
            return ListUtil.emptyList();

        if(DebugUtil.isDebug())
            Log.d(TAG, "Dumping Settings from Table...");

        return DatabaseHelpEx.getFromDatabase(
                db,
                SettingPacket.TABLE_NAME,
                SettingPacket.class,
                true);
    }

    public static Collection<SettingPacket> getAllSettings(SQLDatabase db, int userId, String category) {
        return SQLSnake.create(db, SettingPacket.TABLE_NAME)
                .ensureDatabaseIsReady()
                .whereIdentity(userId, category)
                .asSnake()
                .queryAs(SettingPacket.class, true, false);
    }

    public static SettingPacket getSettingOrBuiltInSetting(SQLDatabase db, int userId, String category, String name) {
        String nameLow = name.toLowerCase();
        if(DebugUtil.isDebug())
            Log.d(TAG, "Getting Setting or Built in, UserId=" + userId + " Category=" + category + " Name=" + name);

        switch (nameLow) {
            case GetSettingExCommand.SETTING_THEME:
                return getTheme(db, userId);
            case GetSettingExCommand.SETTING_COLLECTION:
                return getCollections(db, userId);
            case GetSettingExCommand.SETTING_VERBOSE_DEBUG:
                SettingPacket packet = getVerboseState(db, userId);
                DebugUtil.setForceDebug(Str.toBool(packet.value));
                return packet;
            default:
                return getSetting(db, userId, category, name);
        }
    }

    public static SettingPacket getSetting(SQLDatabase db, int userId, String category, String name) {
        return SQLSnake.create(db, SettingPacket.TABLE_NAME)
                .ensureDatabaseIsReady()
                .whereIdentity(userId, category)
                .whereColumn(SettingPacket.FIELD_NAME, name)
                .asSnake()
                .queryGetFirstAs(SettingPacket.class, true, false);
    }

    public static Collection<SettingPacket> getAllSettings(Context context, SQLDatabase db, int userId, String category) {
        HashMap<String, SettingPacket> settings = new HashMap<>();
        Collection<SettingInfoPacket> mapped_settings = getMappedSettings_lock(context, db);
        Collection<SettingPacket> saved_settings = getAllSettings(db, userId, category);

        if(DebugUtil.isDebug())
            Log.d(TAG, "Mapped Settings Size=" + ListUtil.size(mapped_settings) + " User Saved Settings=" + ListUtil.size(saved_settings) + " user id=" + userId);

        for(SettingInfoPacket setting_info : mapped_settings) {
            if(Str.isEmpty(setting_info.name) && USE_DEFAULT.equalsIgnoreCase(setting_info.name))
                continue;

            SettingPacket packet = new SettingPacket();
            packet.name = setting_info.name;
            packet.description = setting_info.description;
            settings.put(packet.name, packet);
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Total settings size before assigning values with descriptions what not, size=" + settings.size() + " user id=" + userId);

        if(ListUtil.isValid(saved_settings)) {
            for(SettingPacket setting : saved_settings) {
                if(Str.isEmpty(setting.name))
                    continue;

                SettingPacket packet = settings.get(setting.name);
                if(packet == null)
                    settings.put(setting.name, setting);
                else {
                    packet.value = setting.value;
                }
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Finished gathering settings for the user id=" + userId + " Size=" + settings.size() + "  User saved Settings=" + ListUtil.size(saved_settings) + " Mapped Settings=" + ListUtil.size(mapped_settings));

        return settings.values();
    }

    public static Collection<SettingInfoPacket> getMappedSettings_lock(Context context, SQLDatabase db) {
        return DatabaseHelpEx.getOrInitTable(
                context,
                db,
                SettingInfoPacket.TABLE_NAME,
                SettingInfoPacket.TABLE_INFO.columns,
                JSON,
                true,
                SettingInfoPacket.class,
                COUNT);
    }
}
