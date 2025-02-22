package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.UberCore888;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.database.TableInfo;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.hook.AppProviderApi;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;
import eu.faircode.xlua.x.xlua.identity.UserIdentityUtils;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

public class DropTableCommand extends CallCommandHandlerEx {
    private static final String TAG = LibUtil.generateTag(DropTableCommand.class);

    public static final String COMMAND_NAME = "dropTable";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_DB = "database";

    public DropTableCommand() {
        name = COMMAND_NAME;
        requiresPermissionCheck = true;
        requiresSingleThread = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        String tableName = commandData.getExtraString(FIELD_NAME);
        if(Str.isEmpty(tableName)) {
            Log.e(TAG, "Dump Table Name is NULL...");
            return A_CODE.FAILED.toBundle();
        }

        //UberCore888.loadHooksEx(context, ....);

        SQLDatabase database = commandData.getDatabase();
        if(database == null || !database.isOpen(true)) {
            Log.e(TAG, Str.fm("Failed to Drop Table [%s] Database is Not Open and or is Null [%s]",
                    tableName,
                    Str.toStringOrNull(database)));

            return A_CODE.FAILED.toBundle();
        }

        if(!database.hasTable(tableName)) {
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Table [%s] is Already Dropped!", tableName));
        } else {
            if(!DatabaseHelpEx.dropTable_locked(database, tableName)) {
                Log.e(TAG, Str.fm("Failed to Drop Table [%s], no specific Reason...", tableName));
                return A_CODE.FAILED.toBundle();
            } else {
                if(DebugUtil.isDebug())
                    Log.w(TAG, Str.fm("Table [%s] was Successfully Dropped!", tableName));
            }
        }

        TableInfo resolvedTable = resolveTable(tableName);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Dropped Table [%s] Resolved Table Info=%s",
                    tableName,
                    Str.toStringOrNull(Str.noNL(resolvedTable))));

        return resolvedTable != null ?
                A_CODE.result(DatabaseHelpEx.ensureTableIsReady_locked(resolvedTable, database)).toBundle() :
                A_CODE.SUCCESS.toBundle();
    }

    public static A_CODE drop(Context context, String tableName, String db) {
        Bundle send = new Bundle();
        send.putString(FIELD_NAME, tableName);
        send.putString(FIELD_DB, db);

        Bundle result = XProxyContent.luaCall(context, COMMAND_NAME, send);
        return A_CODE.fromBundle(result);
    }

    public static TableInfo resolveTable(String name) {
        //TODO: add more table infos, least hook
        switch (name) {
            case AssignmentPacket.TABLE_NAME:
                return AssignmentPacket.TABLE_INFO;
            case SettingPacket.TABLE_NAME:
                return SettingPacket.TABLE_INFO;
            case XLuaHook.Table.name:
                return XLuaHook.TABLE_INFO;
            default:
                return null;
        }
    }
}