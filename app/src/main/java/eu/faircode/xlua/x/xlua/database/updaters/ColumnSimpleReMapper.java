package eu.faircode.xlua.x.xlua.database.updaters;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.JsonHelperEx;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.XposedUtility;
import eu.faircode.xlua.x.xlua.database.TableInfo;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.database.sql.SQLTable;
import eu.faircode.xlua.x.xlua.database.sql.SQLUtils;
import eu.faircode.xlua.x.xlua.database.updaters.items.MapColumn;

public class ColumnSimpleReMapper {
    public static ColumnSimpleReMapper create() { return new ColumnSimpleReMapper(); }

    private static final String TAG = LibUtil.generateTag(ColumnSimpleReMapper.class);

    private final Map<String, List<MapColumn>> map = new HashMap<>();
    private SQLDatabase database;

    public ColumnSimpleReMapper setDatabase(SQLDatabase database) {
        this.database = database;
        return this;
    }

    public ColumnSimpleReMapper ensureIsUpdated(TableInfo tableInfo) {
        if(database == null || !database.isOpen(true)) {
            XposedUtility.logI_xposed(TAG, Str.fm("Database [%s] Failed to Open or is Null! Skipping Table Update Check...", Str.noNL(Str.toStringOrNull(database))));
            return this;
        }

        if(!database.hasTable(tableInfo.name)) {
            XposedUtility.logW_xposed(TAG, Str.fm("Database [%s] lacks Table [%s], this Updater only works if the Table is already Created!",
                    Str.noNL(database),
                    tableInfo.name));

            return this;
        }

        SQLTable table = SQLUtils.openSQLTable(database, tableInfo.name);
        if(table == null) {
            XposedUtility.logE_xposed(TAG, Str.fm("Failed to Open Table [%s] from Database [%s]! Skipping Table Update Check...",
                    tableInfo.name,
                    Str.noNL(database)));

            return this;
        }

        List<MapColumn> columns = map.get(tableInfo.name);
        boolean needsUpdate = table.tableRequiresUpdate(columns, tableInfo.getPrimaryKeyNames());
        if(DebugUtil.isDebug())
            XposedUtility.logD_xposed(TAG, Str.fm("Re Mapped Columns Count [%s] for Table [%s] from JSON. Internal Map Count=%s Database=%s, Needs Update? %s  SqlTable=%s",
                    ListUtil.size(columns),
                    tableInfo.name,
                    map.size(),
                    Str.noNL(database),
                    String.valueOf(needsUpdate),
                    Str.ensureNoDoubleNewLines(Str.toStringOrNull(table))));

        if(needsUpdate) {
            SQLTable newTable = table.superUpdateTable(tableInfo);
            if(newTable == null || !database.hasTable(tableInfo.name))
                XposedUtility.logE_xposed(TAG, Str.fm("Failed to Update Table [%s] Database [%s] Re Mapped Columns Count [%s] Internal Map Size=%s Has Table? %s",
                        tableInfo.name,
                        Str.noNL(database),
                        ListUtil.size(columns),
                        map.size(),
                        String.valueOf(database.hasTable(tableInfo.name))));
            else {
                XposedUtility.logD_xposed(TAG, Str.fm("Finished Successfully Updating Database [%s] Table [%s], Re Mapped Columns Count [%s] Internal Map Count=%s SqlTable=%s",
                        Str.noNL(database),
                        tableInfo.name,
                        ListUtil.size(columns),
                        map.size(),
                        Str.ensureNoDoubleNewLines(Str.toStringOrNull(newTable))));
            }
        }

        return this;
    }

    //This is now deprecated
    public ColumnSimpleReMapper ensureIsUpdatedOld(TableInfo tableInfo) {
        if(database == null || database.doColumnNamesMatch(tableInfo.name, tableInfo.columns.keySet())) {
            XposedUtility.logI_xposed(TAG, "Database Table [" + tableInfo.name + "] Checks out, Columns what not seem Updated!");
            return this;
        }

        if(!database.isOpen(true)) {
            XposedUtility.logE_xposed(TAG, "Failed to Open Database! Database=" + Str.noNL(database) + " Table=" + tableInfo.name);
            return this;
        }

        if(!database.hasTable(tableInfo.name)) {
            XposedUtility.logW_xposed(TAG, Str.fm("Database [%s] lacks Table [%s], this Updater only works if the Table is already Created!",
                    Str.noNL(database),
                    tableInfo.name));

            return this;
        }

        try {
            if(!database.beginTransaction(true)) {
                XposedUtility.logE_xposed(TAG, "Failed to Being Database Transaction, Database=" + Str.noNL(database) + " Table=" + tableInfo.name);
                return this;
            }

            SQLiteDatabase db = database.getDatabase();
            List<MapColumn> badColumns = map.get(tableInfo.name);
            if(DebugUtil.isDebug())
                XposedUtility.logI_xposed(TAG, Str.fm("Starting Updater for Database [%s] Table [%s] Bad Columns Map Count=%s",
                        Str.noNL(database),
                        tableInfo.name,
                        ListUtil.size(badColumns)));

            if(ListUtil.isValid(badColumns)) {
                for(MapColumn mapColumn : badColumns) {
                    //Add Support for Multiple bad Columns
                    //Will this cause issues for zero entry tables ?
                    String bad = database.getOldColumn(tableInfo.name, mapColumn.oldColumns);
                    if(!Str.isEmpty(bad)) {
                        XposedUtility.logW_xposed(TAG, Str.fm("Found a Outdated Column [%s] in the Table [%s] new Name [%s] Item Count [%s]",
                                bad,
                                tableInfo.name,
                                mapColumn.columnName,
                                database.tableEntries(tableInfo.name)));

                        StringBuilder low = new StringBuilder().append("CREATE TABLE temp_").append(tableInfo.name).append(" AS SELECT");
                        StringBuilder mid = new StringBuilder();
                        StringBuilder end = new StringBuilder();

                        for(Map.Entry<String, String> entry : tableInfo.columns.entrySet()) {
                            String name = entry.getKey();
                            if(!name.equalsIgnoreCase("primary")) {
                                if(mid.length() > 0) {
                                    mid.append(",");
                                    end.append(",");
                                }

                                mid.append(Str.WHITE_SPACE);
                                end.append(Str.WHITE_SPACE);
                                if(!mapColumn.columnName.equalsIgnoreCase(name)) {
                                    mid.append(name);
                                    end.append(name);
                                } else {
                                    //old name
                                    //new name
                                    mid.append(bad).append(" as ").append(name);
                                    end.append(name);
                                }
                            }
                        }

                        //CREATE TABLE temp_settings AS SELECT user, package as category, name, value FROM settings
                        String backupQuery = low.append(mid).append(" FROM ").append(tableInfo.name).toString();
                        if(DebugUtil.isDebug())
                            XposedUtility.logD_xposed(TAG, Str.fm("Created Query to Create Temporary Table [%s] Query=%s",
                                    tableInfo.name,
                                    backupQuery));

                        if(mid.length() > 0) {
                            //[1] Create a Temp Table Holding the Items, with the new Column Name
                            db.execSQL(backupQuery);

                            //[2] Drop the original Table
                            if(!database.dropTable(tableInfo.name)) {
                                XposedUtility.logE_xposed(TAG, "Failed to Drop Table:" + tableInfo.name);
                                return this;
                            }

                            //[3] Create the original Table
                            if(!database.createTable(tableInfo)) {
                                XposedUtility.logE_xposed(TAG, "Failed to Create Table:" + Str.noNL(tableInfo));
                                return this;
                            }

                            //"INSERT INTO settings (user, category, name, value) SELECT user, category, name, value FROM temp_settings"
                            String endQuery = new StringBuilder()
                                    .append("INSERT INTO ")
                                    .append(tableInfo.name)
                                    .append(" (")
                                    .append(end)
                                    .append(") SELECT")
                                    .append(end)
                                    .append(" FROM temp_")
                                    .append(tableInfo.name)
                                    .toString();

                            if(DebugUtil.isDebug()) {
                                XposedUtility.logI_xposed(TAG, Str.fm("Created the Copy Query to Copy back the Items into Table [%s] Query=%s",
                                        tableInfo.name,
                                        endQuery));
                            }

                            //[4] Copy the Items from the Temp Backup Table to the New Created Table
                            db.execSQL(endQuery);

                            //[5] Drop the Temp Backup Table now
                            if(!database.dropTable(Str.combine("temp_", tableInfo.name))) {
                                XposedUtility.logE_xposed(TAG, "Failed to Drop the Temp Table! Name=temp_" + tableInfo.name);
                                return this;
                            }

                            if(DebugUtil.isDebug())
                                XposedUtility.logI_xposed(TAG, Str.fm("Updated Table [%s] Item Count=%s",
                                        tableInfo.name,
                                        database.tableEntries(tableInfo.name)));
                        }

                        database.setTransactionSuccessful();
                    }
                }
            }
        }catch (Exception e) {
            XposedUtility.logE_xposed(TAG, Str.fm("Failed to Check Table [%s] Map Count=%s", Str.noNL(tableInfo), map.size()));
        } finally {
            database.endTransaction(true, false);
            if(DebugUtil.isDebug())
                XposedUtility.logI_xposed(TAG, Str.fm("Finished Checking Table [%s] Item Count=%s",
                        tableInfo.name,
                        database.executeWithReadLock(() -> database.tableEntries(tableInfo.name))));
        }


        return this;
    }

    public ColumnSimpleReMapper createMap(Context context) {
        List<MapColumn> jsonElements = JsonHelperEx.findJsonElementsFromAssets(XUtil.getApk(context), MapColumn.JSON, true, MapColumn.class);
        if(DebugUtil.isDebug())
            XposedUtility.logI_xposed(TAG, Str.fm("Initializing Database Column Table Maps, JSON Item Count=%s" , ListUtil.size(jsonElements)));

        for(MapColumn cMap : jsonElements) {
            List<MapColumn> columns = map.get(cMap.tableName);
            if(columns == null) {
                columns = new ArrayList<>();
                columns.add(cMap);
                map.put(cMap.tableName, columns);
            } else {
                columns.add(cMap);
            }
        }

        if(DebugUtil.isDebug())
            XposedUtility.logI_xposed(TAG, Str.fm("Finished Mapping Table Columns Map, Count=%s" , map.size()));

        return this;
    }


   /* public ColumnReMapperSimple<T> getItems(Class<T> clazz, boolean compressUIDs, boolean dropTableIfExists) {
        if(DebugUtil.isDebug())
            XposedUtility.logD_xposed(TAG, Str.fm("[Getting Items in Database [%s] from Table %s] Compress UIDs=%s Drop If Table Exists=%s",
                    Str.noNL(database.toString()),
                    tableInfo.name,
                    compressUIDs,
                    dropTableIfExists));

        if(!ObjectUtils.anyNull(clazz, tableInfo.name, database) && database.isOpen(true)) {
            List<T> items = compressUIDs ?
                    UpdateUtils.getUidDatabaseEntries(database, tableInfo.name, clazz, dropTableIfExists) :
                    DatabaseHelpEx.getFromDatabase(database, tableInfo.name, clazz, true);

            ListUtil.addAllIfValid(this.items, items, true);
            if(DebugUtil.isDebug())
                XposedUtility.logD_xposed(TAG, Str.fm("Got Items, Total=%s", this.items.size()));
        }

        return this;
    }*/

    /*
      int old = this.push.size();
            if(!database.doColumnNamesMatch(tableInfo.name, tableInfo.columns.keySet())) {
                sendRemainingToPush();
                XposedUtility.logI_xposed(TAG_ENSURE_TABLE, "Table Columns do not align!\n" + tableInfo);
                if(dropOldTableIfOutdated) {
                    database.beginTransaction(true);
                    database.endTransaction(true, database.dropTable(tableInfo.name));
                }
            }
     */
    //Pass the Defaults JSON as well
    //public ColumnReMapperSimple<T> ensureTableIsUpdated() { }
}
