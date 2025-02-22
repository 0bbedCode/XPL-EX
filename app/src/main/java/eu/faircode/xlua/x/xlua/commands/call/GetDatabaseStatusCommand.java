package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.utilities.BundleUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.file.FileEx;
import eu.faircode.xlua.x.file.FileUtils;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.ui.dialogs.LogDialog;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.database.DatabasePathUtil;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;

public class GetDatabaseStatusCommand extends CallCommandHandlerEx {
    private static final String TAG = LibUtil.generateTag(GetDatabasePathCommand.class);

    public static final String FIELD_CODE = "code";
    public static final String FIELD_MESSAGE = "message";

    public static final String COMMAND_NAME = "getDatabaseStatus";

    public static final int CODE_READY = 1;
    public static final int CODE_ERROR = 2;

    public GetDatabaseStatusCommand() {
        name = COMMAND_NAME;
        requiresPermissionCheck = true;
        requiresSingleThread = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        SQLDatabase database = commandData.getDatabase();
        int code = database == null || !database.isOpen(true) ? CODE_ERROR : CODE_READY;
        StrBuilder msg = StrBuilder.create()
                .append("Database=" + Str.toStringOrNull(database))
                .append(database == null ? "Database is NULL! Contact Developer please!" : database.isOpen(true) ? " Database was Successfully Opened: " : " Database Failed to Open! ")
                .appendLine(ListUtil.forEachConditionTo(
                        FileUtils.combineTwoLists(ListUtil.arrayToList(FileEx.createFromDirectory(DatabasePathUtil.NEW_DIRECTORY).listFilesEx()), ListUtil.arrayToList(FileEx.createFromDirectory(DatabasePathUtil.OLD_DIRECTORY).listFilesEx()), false), new ListUtil.IIteratePairCondition<FileEx, String>() {
                            @Override
                            public boolean isFine(FileEx file) {
                                return file.isDirectory() &&
                                        (file.getName().equalsIgnoreCase(DatabasePathUtil.OLD_PREFIX) || file.getName().toLowerCase().startsWith(DatabasePathUtil.NEW_PREFIX));
                            }

                            @Override
                            public String get(FileEx file) {
                                return "File=" + file.getAbsolutePath() +
                                        "\n\tSub Files Count=" + ArrayUtils.safeLength(file.listFilesEx()) +
                                        "\n\tStat=" + RuntimeUtils.executeCommand("stat", file.getAbsolutePath());
                            }
                        }));

        Bundle res = new Bundle();
        res.putInt(FIELD_CODE, code);
        res.putString(FIELD_MESSAGE, msg.toString());
        return res;
    }

    public static Pair<Integer, String> get(Context context) {
        Pair<Integer, String> result = internalGet(context);
        if(DebugUtil.isDebug()) Log.d(TAG, "Database Check Result Code=" + result.first + " Msg=" + Str.ensureNoDoubleNewLines(result.second));
        return result;
    }

    private static Pair<Integer, String> internalGet(Context context) {
        if(context == null)
            return Pair.create(CODE_ERROR, "Context is Null, Failed to check Service / Database...");

        Bundle result = XProxyContent.luaCall(context, COMMAND_NAME);
        if(result == null || !result.containsKey(FIELD_CODE))
            return Pair.create(CODE_ERROR, context.getString(R.string.msg_error_service));

        int code = result.getInt(FIELD_CODE, CODE_ERROR);
        String msg = result.getString(FIELD_MESSAGE, "error-null");
        return Pair.create(code, msg);
    }
}