package eu.faircode.xlua.x.xlua.database.sql;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.XposedUtility;
import eu.faircode.xlua.x.xlua.database.TableInfo;
import eu.faircode.xlua.x.xlua.database.updaters.items.MapColumn;

public class SQLTable {
    private static final String TAG = LibUtil.generateTag(SQLTable.class);

    private final SQLDatabase database;
    private final LinkedHashMap<String, SQLTableColumn> columns = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> remappedColumns = new LinkedHashMap<>();

    public final String name;

    public List<SQLTableColumn> getColumns() { return ListUtil.copyToArrayList(columns.values()); }
    public List<String> getColumnNames() { return ListUtil.copyToArrayList(columns.keySet()); }
    public List<SQLTableColumn> getPrimaryKeyColumns() { return ListUtil.forEachCondition(columns.values(), (c) -> c.isPartOfPrimaryKey); }
    public List<String> getPrimaryKeyColumnNames() { return ListUtil.forEachTo(getPrimaryKeyColumns(), (c) -> c.name); }

    public int getEntryCount() { return database != null && database.isOpen(true) ? database.tableEntries(name) : -1; }
    public String getTempTableName() { return Str.combine("temp_", name); }

    public SQLTable(SQLDatabase database, String tableName, Map<String, SQLTableColumn> columns) {
        this.database = database;
        this.name = tableName;
        ListUtil.addAllIfValid(this.columns, columns);
    }

    public boolean tableRequiresUpdate(List<MapColumn> maps, List<String> primaryKeys) {
        //Handle if the Table has Columns Moved Around
        if(ListUtil.isValid(maps) && ListUtil.isValid(columns)) {
            remappedColumns.clear();
            for(MapColumn map : maps) {
                List<String> oldNames = map.oldColumns;
                if(!ListUtil.isValid(oldNames))
                    continue;
                for(String oldName : oldNames) {
                    if(Str.isEmpty(oldName))
                        continue;
                    if(columns.containsKey(oldName)) {
                        remappedColumns.put(oldName, map.columnName);
                        if(DebugUtil.isDebug())
                            Log.d(TAG, "Found a bad column, old=" + oldName + " new name=" + map.columnName);

                        break;
                        //break ? we cant map columns to multiple w.e with same name
                    }
                }
            }
        }

        if(ListUtil.isValid(primaryKeys)) {
            List<String> myPrimaryKeys = ListUtil.forEach(getPrimaryKeyColumnNames(), String::toLowerCase);
            if(primaryKeys.size() != ListUtil.size(myPrimaryKeys)) {
                XposedUtility.logW_xposed(TAG, Str.fm("Table [%s] lacks Primary keys needed (size check failed), Table Primary Keys [%s] Needed Primary Keys [%s]",
                        name,
                        Str.joinList(myPrimaryKeys),
                        Str.joinList(primaryKeys)));

                return true;
            }

            for(String primKey : primaryKeys) {
                if(!myPrimaryKeys.contains(primKey.toLowerCase())) {
                    XposedUtility.logW_xposed(TAG, Str.fm("Table [%s] lacks Primary Key [%s], Table Primary Keys [%s] Needed Primary Keys [%s]",
                            name,
                            primKey,
                            Str.joinList(myPrimaryKeys),
                            Str.joinList(primaryKeys)));

                    return true;
                }
            }
        }

        return !remappedColumns.isEmpty();
    }

    public SQLTable superUpdateTable(TableInfo tableInfo) {
        if(database == null || !database.isOpen(true)) {
            XposedUtility.logE_xposed(TAG, Str.fm("(1) Database [%s] is Not Opened or Null! Failed to Create Temp Table for [%s]", Str.noNL(Str.toStringOrNull(database)), name));
            return null;
        }

        if(!database.hasTable(name)) {
            XposedUtility.logE_xposed(TAG, Str.fm("Database [%s] lacks Table [%s] for Re Init!", Str.noNL(database), name));
            return null;
        }

        if(!database.beginTransaction(true)) {
            XposedUtility.logE_xposed(TAG, Str.fm("Failed to Being Database [%s] Transaction for Table [%s] Re Init!", Str.noNL(database), name));
            return null;
        }

        boolean worked = SQLUtils.reMapTable(tableInfo, database, getColumnNames(), remappedColumns);
        database.endTransaction(true, worked);
        if(!worked) {
            XposedUtility.logE_xposed(TAG, "Failed to Re Initialize Table: " + name);
            return null;
        }

        if(!database.hasTable(name)) {
            XposedUtility.logE_xposed(TAG, "Table for Some Reason Does not Exist, Name=" + name);
            return null;
        }

        return database.executeWithReadLock(() -> SQLUtils.openSQLTable(database, name));
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .appendFieldLine("Table Name", this.name)
                .appendFieldLine("Columns Count", this.columns.size())
                .appendFieldLine("Columns Re Mapped Count", this.remappedColumns.size())
                .appendFieldLine("Column Names", Str.joinList(getColumnNames()))
                .appendFieldLine("Column Names Key", Str.joinList(getPrimaryKeyColumnNames()))
                .appendFieldLine("Table Entry Count", getEntryCount())
                .appendFieldLine("Table Temp Name", getTempTableName())
                .appendFieldLine("Database", Str.noNL(Str.toStringOrNull(database)))
                .toString(true);
    }
}
