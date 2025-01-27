package eu.faircode.xlua.x.xlua.database;

import android.text.TextUtils;
import android.util.Log;

import java.util.Map;

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

    public static String dynamicCreateQueryEx(Map<String, String> columns, String tableName) {
        String top = "CREATE TABLE IF NOT EXISTS " + tableName + " (";
        StringBuilder mid = new StringBuilder();

        String pValue = columns.remove("PRIMARY");
        int i = 1;
        int sz = columns.size();
        for(Map.Entry<String, String> r : columns.entrySet()) {
            String l = r.getKey() + " " + r.getValue();
            mid.append(l);
            if(i < sz) mid.append(", ");
            i++;
        }

        if(pValue != null) {
            mid.append(", PRIMARY ").append(pValue);
        }

        return top + mid + ");";
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
