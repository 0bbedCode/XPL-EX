package eu.faircode.xlua.x.xlua.database;

import android.text.TextUtils;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.file.FileEx;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;

public class DatabaseUtils {
    private static final String TAG = "XLua.DatabaseUtils";

    public static String ensureValidDatabaseFileName(String name) { return TextUtils.isEmpty(name) ? name : name.endsWith(".db") ? name : name + ".db"; }


    public static boolean isReady(SQLDatabase database, String tableName) {
        if(!isReady(database))
            return false;

        if(TextUtils.isEmpty(tableName)) {
            Log.e(TAG, "[isReady(1)] Database is not Ready, Table Name is null or Empty! Database=" + database);
            return false;
        }

        if(!database.hasTable(tableName)) {
            Log.e(TAG, "[isReady(1)] Database is not Ready, Does not have Table=" + tableName + " Database=" + database);
            return false;
        }

        return true;
    }

    public static boolean isReady(SQLDatabase database) {
        if(database == null) {
            Log.e(TAG, "[isReady(0)] Database is not Ready, it is null the parameter...");
            return false;
        }

        if(!database.isOpen(true)) {
            Log.e(TAG, "Database is not Ready, Database=" + database + " Failed to open!");
            return false;
        }

        return true;
    }


    public static String dynamicColumnQuery(List<String> columns) {
        if(!ListUtil.isValid(columns))
            return Str.EMPTY;

        StrBuilder sb = StrBuilder.create().ensureDelimiter(TableInfo.QUERY_DEL);
        for(String column : columns)
            if(!Str.isEmpty(column))
                sb.append(column);

        return sb.toString();
    }

    public static String dynamicCreateQueryEx(Map<String, String> columns, String tableName) {
        if(Str.isEmpty(tableName) || !ListUtil.isValid(columns))
            return Str.EMPTY;

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("CREATE TABLE IF NOT EXISTS ")
                .append(tableName)
                .append(" (");

        // Make a copy of columns to avoid modifying the original
        Map<String, String> columnsCopy = new LinkedHashMap<>(columns);

        // Extract PRIMARY KEY definition if it exists
        String primaryKeyDef = columnsCopy.remove("PRIMARY");

        // Build column definitions
        boolean isFirst = true;
        for (Map.Entry<String, String> column : columnsCopy.entrySet()) {
            if (!isFirst) {
                queryBuilder.append(", ");
            }

            String columnName = column.getKey();
            String columnDef = column.getValue();
            if(Str.isEmpty(columnName) || Str.isEmpty(columnDef))
                continue;

            queryBuilder.append(columnName)
                    .append(" ")
                    .append(columnDef);

            isFirst = false;
        }

        // Append PRIMARY KEY constraint if present
        if (!TextUtils.isEmpty(primaryKeyDef)) {
            queryBuilder.append(", PRIMARY ")
                    .append(primaryKeyDef);
        }

        queryBuilder.append(");");

        // Log the final query in debug mode
        //if (DebugUtil.isDebug()) {
        //    Log.d(TAG, "Generated CREATE TABLE query for " + tableName + ": " + queryBuilder.toString());
        //}

        return queryBuilder.toString();
    }

    public static void ensureValidFile(FileEx file, boolean throwIsInvalid) throws Exception {
        if(file == null || file.isDirectory()) {
            String msg = "Error invalid File for Database!!! File=" + (file == null ? "null" : file.getAbsolutePath()) + " Stack=" + RuntimeUtils.getStackTraceSafeString();
            Log.e(TAG, msg);
            if(throwIsInvalid)
                throw new Exception(msg);
        }
    }

    public static void ensureValidFile(FileEx file) {
        if(file == null || file.isDirectory()) {
            String msg = "Error invalid File for Database!!! File=" + (file == null ? "null" : file.getAbsolutePath()) + " Stack=" + RuntimeUtils.getStackTraceSafeString();
            Log.e(TAG, msg);
        }
    }
}
