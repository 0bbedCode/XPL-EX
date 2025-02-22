package eu.faircode.xlua.x.xlua.database.sql;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.XposedUtility;
import eu.faircode.xlua.x.xlua.database.TableInfo;

public class SQLUtils {
    private static final String TAG = LibUtil.generateTag(SQLUtils.class);


    public static Map<String, SQLTableColumn> columnListToMapNames(List<SQLTableColumn> columns) {
        if(!ListUtil.isValid(columns))
            return  new LinkedHashMap<>();

        Map<String, SQLTableColumn> map = new LinkedHashMap<>(columns.size());  // Changed to LinkedHashMap
        for(SQLTableColumn column : columns)
            map.put(column.name, column);

        return map;
    }

    public static List<SQLTableColumn> columnsMapToObjects(Map<String, String> map) {
        if(!ListUtil.isValid(map))
            return ListUtil.emptyList();

        Map<String, String> columnsCopy = new LinkedHashMap<>(map);
        String primary = columnsCopy.remove(TableInfo.SQLITE_PRIMARY_WORD);
        List<String> primaries = new ArrayList<>();
        if(!Str.isEmpty(primary)) {
            String cleaned = primary.replaceAll(Str.WHITE_SPACE, Str.EMPTY).replaceAll("KEY\\(", Str.EMPTY).replaceAll("\\)", Str.EMPTY);
            String[] parts = cleaned.split(",");
            if(ArrayUtils.isValid(parts)) {
                for(String p : parts) {
                    String trimmed = p.trim();
                    if(!Str.isEmpty(trimmed) && !primaries.contains(trimmed))
                        primaries.add(trimmed);
                }
            } else {
                if(!Str.isEmpty(cleaned))
                    primaries.add(cleaned);
            }
        }

        List<SQLTableColumn> columns = new ArrayList<>();
        for(Map.Entry<String, String> entry : columnsCopy.entrySet()) {
            String name = entry.getKey();
            String type = entry.getValue();
            if(Str.isEmpty(name) || Str.isEmpty(type))
                continue;

            String typeTrimmed = type.trim();
            boolean isPrim = typeTrimmed.contains(TableInfo.SQLITE_PRIMARY_KEY_WORD);
            if(isPrim) {
                StrBuilder sb = StrBuilder.create();
                char[] chars = typeTrimmed.toCharArray();
                for(int i = 0; i < chars.length; i++) {
                    char c = chars[i];
                    if(c == ' ' || c == '\t' || c == '\n' || c == '\r')
                        break;

                    sb.append(c);
                }

                type = sb.isEmpty() ? typeTrimmed : sb.toString(false);
            } else {
                type = typeTrimmed;
                isPrim = !primaries.isEmpty() && primaries.contains(name);
            }

            columns.add(SQLTableColumn.create(name, type, isPrim));
        }

        return columns;
    }

    public static List<String> columnsMapToNames(Map<String, String> map) {
        if(!ListUtil.isValid(map))
            return ListUtil.emptyList();

        Map<String, String> columnsCopy = new LinkedHashMap<>(map);
        List<String> names = new ArrayList<>();
        for(Map.Entry<String, String> entry : columnsCopy.entrySet()) {
            String name = entry.getKey();
            if(!Str.isEmpty(name))
                names.add(name);
        }

        return names;
    }

    public static String dynamicColumnQueryReMapped(List<String> columns, Map<String, String> map) {
        if(!ListUtil.isValid(map))
            return dynamicColumnQuery(columns);

        if(!ListUtil.isValid(columns))
            return Str.EMPTY;

        StrBuilder sb = StrBuilder.create().ensureDelimiter(TableInfo.QUERY_DEL);
        for(String column : columns) {
            if(!Str.isEmpty(column)) {
                String newName = map.get(column);
                if(!Str.isEmpty(newName) && !Str.areEqual(newName, column)) {
                    sb.append(Str.combineEx(column, " as ", newName));
                } else {
                    sb.append(column);
                }
            }
        }

        return sb.toString();
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

    public static String dynamicCreateQuery(Map<String, String> columns, String tableName) {
        if(Str.isEmpty(tableName) || !ListUtil.isValid(columns))
            return Str.EMPTY;

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("CREATE TABLE IF NOT EXISTS ")
                .append(tableName)
                .append(" (");

        // Make a copy of columns to avoid modifying the original
        Map<String, String> columnsCopy = new LinkedHashMap<>(columns);

        // Extract PRIMARY KEY definition if it exists
        String primaryKeyDef = columnsCopy.remove(TableInfo.SQLITE_PRIMARY_WORD);

        // Build column definitions
        boolean isFirst = true;
        for (Map.Entry<String, String> column : columnsCopy.entrySet()) {
            if (!isFirst)
                queryBuilder.append(", ");

            String columnName = column.getKey();
            String columnDef = column.getValue();
            if(Str.isEmpty(columnName) || Str.isEmpty(columnDef))
                continue;

            queryBuilder.append(columnName)
                    .append(" ")
                    .append(columnDef);

            isFirst = false;
        }

        if (!Str.isEmpty(primaryKeyDef))
            queryBuilder.append(", PRIMARY ").append(primaryKeyDef);

        queryBuilder.append(");");
        return queryBuilder.toString();
    }

    public static SQLTable openSQLTable(SQLDatabase database, String tableName) {
        if (!database.isOpen(true) || !database.hasTable(tableName)) {
            return null;
        }

        List<SQLTableColumn> columns = new ArrayList<>();
        Cursor cursor = null;
        try {
            // Query table structure using PRAGMA
            cursor = database.getDatabase().rawQuery("PRAGMA table_info(" + tableName + ")", null);
            while (cursor != null && cursor.moveToNext()) {
                String name = CursorUtil.getString(cursor, "name");
                String type = CursorUtil.getString(cursor, "type");
                boolean isPk = CursorUtil.getInteger(cursor, "pk", 0) > 0;
                columns.add(SQLTableColumn.create(name, type, isPk));
            }

            return  new SQLTable(database, tableName, columnListToMapNames(columns));
        } catch (Exception e) {
            Log.e(TAG, "Failed to create SQLTable for " + tableName, e);
            return null;
        } finally {
            CursorUtil.closeCursor(cursor);
        }
    }

    public static boolean reMapTable(TableInfo tableInfo, SQLDatabase database, List<String> columnNames, Map<String, String> remappedColumns) {
        if(database == null || !database.isOpen(true)) {
            XposedUtility.logE_xposed(TAG, Str.fm("Database [%s] is Not Opened or Null! Failed to Create Temp Table for [%s]",
                    Str.noNL(Str.toStringOrNull(database)),
                    tableInfo.name));
            return false;
        }

        if(tableInfo == null) {
            XposedUtility.logE_xposed(TAG, Str.fm("Table Info Passed is Null or Invalid Name! Skipping Re Map / Re Init! Database=%s", Str.noNL(Str.toStringOrNull(database))));
            return false;
        }

        if(!ListUtil.isValid(columnNames)) {
            XposedUtility.logE_xposed(TAG, Str.fm("Table [%s] in Database [%s] Re Init has Failed, Given Empty or Null Columns, Count=%s",
                    tableInfo.name,
                    Str.noNL(database),
                    ListUtil.size(columnNames)));

            return false;
        }

        try {
            //Have Size checks on table, how many items in table check
            //Also have this function handle new columns, and or columns moved around

            String tempName = tableInfo.getTempTableName();
            SQLiteDatabase db = database.getDatabase();
            String columns = SQLUtils.dynamicColumnQuery(columnNames);
            String columnsTemp = SQLUtils.dynamicColumnQueryReMapped(columnNames, remappedColumns);

            if(Str.isEmpty(columnsTemp) || Str.isEmpty(columns))
                throw new Exception(Str.fm("Column Query [%s] or Temp Query [%s] is Null, Table [%s]",
                        Str.toStringOrNull(columns),
                        Str.toStringOrNull(columnsTemp),
                        tableInfo.name));

            //CREATE TABLE temp_settings AS SELECT user, package as category, name, value FROM settings
            String createTempQuery = StrBuilder.create()
                    .append("CREATE TABLE ")
                    .append(tempName)
                    .append(" AS SELECT ")
                    .append(columnsTemp)
                    .append(" FROM ")
                    .append(tableInfo.name)
                    .toString(false);

            if(DebugUtil.isDebug())
                XposedUtility.logI_xposed(TAG, Str.fm("Creating Temp Table [%s] for Table [%s], Column Query(1) [%s] Column Query(2) [%s] Create Temp Table Query [%s] Item Count=%s",
                        tempName,
                        tableInfo.name,
                        columns,
                        columnsTemp,
                        createTempQuery,
                        database.tableEntries(tableInfo.name)));

            //[1] Create the Temp Table
            db.execSQL(createTempQuery);

            //[2] Drop the original Table
            if(!database.dropTable(tableInfo.name))
                throw new Exception(Str.fm("Failed to Drop the Old Table!"));

            if(!database.createTable(tableInfo))
                throw new Exception(Str.fm("Failed to Create the Original Table one more!"));

            //"INSERT INTO settings (user, category, name, value) SELECT user, category, name, value FROM temp_settings"
            String copyBackQuery = StrBuilder.create()
                    .append("INSERT OR REPLACE INTO ")
                    .append(tableInfo.name)
                    .append(" (")
                    .append(columns)
                    .append(") SELECT ")  // Added space after SELECT
                    .append(columns)
                    .append(" FROM ")
                    .append(tempName)
                    .toString(false);

            //[3] Copy Items from the Temp Table into the new Table
            db.execSQL(copyBackQuery);
            if(DebugUtil.isDebug())
                XposedUtility.logI_xposed(TAG, Str.fm("Created the Original Table [%s] now Copying Temp Items from Table [%s] Query [%s] Temp Item Count=%s Copied Item Count=%s",
                        tableInfo.name,
                        tempName,
                        copyBackQuery,
                        database.tableEntries(tempName),
                        database.tableEntries(tableInfo.name)));

            //[4] Drop the Temp Table Now
            database.dropTable(tempName);
            return true;
        }catch (Exception e) {
            XposedUtility.logE_xposed(TAG, Str.fm("Failed to Create Temp Table [%s] for Table [%s] Error=%s",
                    tableInfo.getTempTableName(),
                    tableInfo.name,
                    e));

            return false;
        }
    }
}
