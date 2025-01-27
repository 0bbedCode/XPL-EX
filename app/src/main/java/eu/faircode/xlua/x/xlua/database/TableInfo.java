package eu.faircode.xlua.x.xlua.database;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.hook.interceptors.zone.Te;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;

public class TableInfo {
    private static final String TAG = "XLua.TableInfo";

    public static TableInfo create(String name) { return new TableInfo(name); }

    public final String name;
    public final LinkedHashMap<String, String> columns = new LinkedHashMap<>();

    private String primaryKey;

    public static final String FIELD_USER = UserIdentityIO.FIELD_USER;
    public static final String FIELD_CATEGORY = UserIdentityIO.FIELD_CATEGORY;

    public static final String TABLE_AUTH_KEY = StrBuilder.create().append(FIELD_USER).append(", ").append(FIELD_CATEGORY).toString();

    public static final String SQLITE_STRING_TYPE = "TEXT";
    public static final String SQLITE_NUMERIC_TYPE = "INTEGER";
    public static final String SQLITE_PRIMARY_KEY_WORD = "PRIMARY KEY";
    public static final String SQLITE_PRIMARY_WORD = "PRIMARY";

    public String getPrimaryKey() { return primaryKey; }

    public boolean is_userId_category() {
        //ToDO
        return false;
    }


    public TableInfo(String tableName) {
        this.name = tableName;
    }

    public TableInfo putIdentification() {
        putInteger(FIELD_USER);
        putText(FIELD_CATEGORY);
        return this;
    }

    public boolean isUpdated(SQLDatabase database) {
        return database != null && database.executeWithReadLock(() -> database.doColumnNamesMatch(this.name, this.columns.values()));
    }

    public TableInfo putText(String fieldName) { return putText(fieldName, false); }
    public TableInfo putText(String fieldName, boolean isKey) {
        return put(fieldName,
                StrBuilder.create(SQLITE_STRING_TYPE)
                        .setDoAppendFlag(isKey)
                        .appendSpace()
                        .append(SQLITE_PRIMARY_KEY_WORD).toString());
    }

    public TableInfo putInteger(String fieldName) { return putInteger(fieldName, false); }
    public TableInfo putInteger(String fieldName, boolean isKey) {
        return put(fieldName,
                StrBuilder.create(SQLITE_NUMERIC_TYPE)
                        .setDoAppendFlag(isKey)
                        .appendSpace()
                        .append(SQLITE_PRIMARY_KEY_WORD).toString());
    }


    public TableInfo putPrimaryKey(String... fields) { return putPrimaryKey(false, fields); }
    public TableInfo putPrimaryKey(boolean writeIdentificationFirst, String... fields) {
        if(ArrayUtils.isValid(fields)) {
            StringBuilder mid_end = new StringBuilder();
            if(writeIdentificationFirst) mid_end.append(TABLE_AUTH_KEY);

            for(int i = 0; i < fields.length; i++) {
                String f = fields[i];
                if(TextUtils.isEmpty(f)) continue;
                if(mid_end.length() > 0) mid_end.append(", ");
                mid_end.append(f);
            }

            String finalOutput = Str.trimEx(mid_end.toString(), true, true, Str.COMMA, Str.WHITE_SPACE);
            if(!TextUtils.isEmpty(finalOutput)) {
                String val = "KEY(" + finalOutput + ")";
                columns.put(SQLITE_PRIMARY_WORD, val);
                this.primaryKey = val;
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Created Table [" + name + "] Primary Key Entry: " + val);
            }
        }

        return this;
    }

    public TableInfo put(String key, String val) {
        if(TextUtils.isEmpty(key) || TextUtils.isEmpty(val)) return this;
        if(DebugUtil.isDebug())
            Log.d(TAG, "Putting Table [" + name + "] Column, key=" + key + " Val=" + val);

        columns.put(key, val);
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Table Name", this.name)
                .appendFieldLine("Columns Count", this.columns.size())
                .appendFieldLine("Columns", this.columns)
                .appendFieldLine("Primary Key", this.primaryKey)
                .appendFieldLine("Auth key String", TABLE_AUTH_KEY)
                .toString(true);
    }
}
