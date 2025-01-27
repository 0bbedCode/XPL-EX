package eu.faircode.xlua.x.xlua.configs;

import android.util.Log;

import java.util.Collection;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.commands.XPacket;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.database.sql.SQLSnake;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;
import eu.faircode.xlua.x.xlua.settings.data.SettingsApi;
import eu.faircode.xlua.x.xlua.settings.data.SettingsApiUtils;

public class ConfigApi {
    private static final String TAG = "XLua.ConfigApi";

    public static A_CODE single_locked(SQLDatabase db, XPacket<XPConfig> packet) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "Config Api Action Single, Config=" + Str.toStringOrNull(packet));


        if(packet.extra == null)
            return A_CODE.FAILED;

        A_CODE code = DatabaseHelpEx.ensureRead(db, XPConfig.TABLE_INFO);
        if(code != A_CODE.NONE)
            return code;

        switch (packet.code) {
            case UPDATE:
            case PUSH:
                code = SettingsApiUtils.resultToCode(DatabaseHelpEx.insertItem(db, XPConfig.TABLE_NAME, packet.extra));
                break;
            case DELETE:
                code = SettingsApiUtils.resultToCode(DatabaseHelpEx.deleteItem(
                        SQLSnake.create(db, XPConfig.TABLE_NAME)
                                //.whereIdentity(packet.getUserId(), packet.getPackageName())
                                .whereColumn(XPConfig.FIELD_USER, packet.getUserId())
                                .whereColumn(XPConfig.FIELD_NAME, packet.extra.name)
                                .asSnake()));
                break;
            case APPLY:
                //SettingsApi.

                break;
        }

        //Do Is Kill ?

        return code;
    }

    public static Collection<XPConfig> getConfigs(SQLDatabase database, int user) {
        return SQLSnake.create(database, XPConfig.TABLE_NAME)
                .ensureDatabaseIsReady()
                .ensureTableIsAvailable(XPConfig.TABLE_INFO)
                .whereColumn(XPConfig.FIELD_USER, user)
                .asSnake()
                .queryAs(XPConfig.class, true, false);
    }

    public static Collection<XPConfig> getAllConfigs(SQLDatabase database) {
        return DatabaseHelpEx.getFromDatabase(
                database,
                XPConfig.TABLE_NAME,
                XPConfig.class,
                true);
    }
}
