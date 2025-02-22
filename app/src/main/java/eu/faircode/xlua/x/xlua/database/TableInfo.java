package eu.faircode.xlua.x.xlua.database;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;

public class TableInfo {
    private static final String TAG = LibUtil.generateTag(TableInfo.class);

    public static TableInfo create(String name) { return new TableInfo(name); }

    public static final String QUERY_DEL = Str.combine(Str.COMMA, Str.WHITE_SPACE);

    public final String name;
    public final LinkedHashMap<String, String> columns = new LinkedHashMap<>();

    private final List<String> primaryKeyNames = new ArrayList<>();
    private String primaryKey;

    public static final String FIELD_USER = UserIdentityIO.FIELD_USER;
    public static final String FIELD_CATEGORY = UserIdentityIO.FIELD_CATEGORY;

    //user, category
    public static final String TABLE_AUTH_KEY = StrBuilder.create().append(FIELD_USER).append(", ").append(FIELD_CATEGORY).toString();

    public static final String SQLITE_STRING_TYPE = "TEXT";
    public static final String SQLITE_NUMERIC_TYPE = "INTEGER";
    public static final String SQLITE_PRIMARY_KEY_WORD = "PRIMARY KEY";
    public static final String SQLITE_PRIMARY_WORD = "PRIMARY";

    public String getTempTableName() { return Str.combine("temp_", name); }
    public List<String> getPrimaryKeyNames() { return ListUtil.copyToArrayList(primaryKeyNames); }

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
    public TableInfo putText(String fieldName, boolean isKey) { return put(fieldName, generateType(fieldName, SQLITE_STRING_TYPE, isKey)); }

    public TableInfo putInteger(String fieldName) { return putInteger(fieldName, false); }
    public TableInfo putInteger(String fieldName, boolean isKey) { return put(fieldName, generateType(fieldName, SQLITE_NUMERIC_TYPE, isKey)); }

    private void appendPrimaryKey(String key) {
        if(!Str.isEmpty(key) && !primaryKeyNames.contains(key))
            primaryKeyNames.add(key);
    }

    public TableInfo putPrimaryKey(String... fields) { return putPrimaryKey(false, fields); }
    public TableInfo putPrimaryKey(boolean writeIdentificationFirst, String... fields) {
        //Most likely when the table "assignments" was "re-created" it may have Dropped it, and ignored it key ?
        //As Key failed before since there is no field "category" for the old Table column name "package" ?
        if(ArrayUtils.isValid(fields)) {
            StrBuilder mid_end = StrBuilder.create().ensureDelimiter(QUERY_DEL);
            if(writeIdentificationFirst) {
                mid_end.append(TABLE_AUTH_KEY);
                appendPrimaryKey(FIELD_USER);
                appendPrimaryKey(FIELD_CATEGORY);
            }

            //user, category, name (would be the output example)
            for(String field : fields) {
                if(Str.isEmpty(field))
                    continue;

                mid_end.append(field);
                appendPrimaryKey(field);
            }

            //Remove any White Space Trails or Commas
            String finalOutput = Str.trimEx(mid_end.toString(), true, true, Str.COMMA, Str.WHITE_SPACE);
            if(!Str.isEmpty(finalOutput)) {
                //Wrap it: KEY(user, category, name)
                String val = "KEY(" + finalOutput + ")";
                //Put it to the "columns" [PRIMARY][KEY(user, category, name)]
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

    //CREATE TABLE temp_settings AS SELECT user, package as category, name, value FROM settings
    //INSERT INTO settings (user, category, name, value) SELECT user, category, name, value FROM temp_settings

    public String generateDynamicCreateQuery() { return DatabaseUtils.dynamicCreateQueryEx(columns, name); }

    /*public String generateDynamicColumnQuery() {
        if(columns.isEmpty())
            return Str.EMPTY;

        Map<String, String> columnsCopy = new LinkedHashMap<>(columns);
        for(Map.Entry<String, String> entry : columnsCopy.entrySet()) {
            String name
        }

    }*/

    public String generateDynamicColumnQuery(SQLDatabase database) { return database == null ? Str.EMPTY : DatabaseUtils.dynamicColumnQuery(database.getColumnNames(name)); }

    private String generateType(String name, String type, boolean isKey) {
        if(Str.isEmpty(type))
            return Str.EMPTY;
        if(isKey) {
            appendPrimaryKey(name);
            return Str.combine(Str.WHITE_SPACE, SQLITE_PRIMARY_KEY_WORD);
        } else {
            return type;
        }
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
