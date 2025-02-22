package eu.faircode.xlua.x.xlua.database.updaters;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.JsonHelperEx;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.xlua.XposedUtility;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.database.TableInfo;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.database.sql.SQLSnake;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.interfaces.ICursorType;
import eu.faircode.xlua.x.xlua.interfaces.IJsonType;
import eu.faircode.xlua.x.xlua.settings.SettingReMappedItem;
import eu.faircode.xlua.x.xlua.settings.data.SettingInfoPacket;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

//Builder then use factory for caller ?
public class DatabaseUpdater<T extends IIdentifiableObject & ICursorType & IJsonType> {

    private static final String TAG = "XLua.DatabaseUpdater";
    private static final String TAG_PUSH = "XLua.DatabaseUpdater(push)";
    private static final String TAG_ENSURE_TABLE = "XLua.DatabaseUpdater(ensureTableIsUpdated)";

    public static <T extends IIdentifiableObject & ICursorType & IJsonType> DatabaseUpdater<T> create() { return new DatabaseUpdater<>(); }


    public static interface IContentValues<T extends IIdentifiableObject & ICursorType> {
        ContentValues getContentValues(T o);
    }

    public static final String DELETE_ID = "<!delete!>";

    private final WeakHashMap<String, T> all = new WeakHashMap<>();
    private final WeakHashMap<String, Pair<T, String>> push = new WeakHashMap<>();

    private TableInfo table;
    private Class<T> clazz;
    private SQLDatabase database;
    private boolean shouldContinue = true;

    public DatabaseUpdater<T> clearMaps(boolean resetContinue) {
        if(resetContinue)
            this.shouldContinue = true;

        this.all.clear();
        this.push.clear();
        return this;
    }

    public DatabaseUpdater<T> setTable(TableInfo table) {
        this.table = table;
        if(DebugUtil.isDebug())
            XposedUtility.logD_xposed(TAG, "Set Table=" + Str.toStringOrNull(table));

        return this;
    }

    public DatabaseUpdater<T> clearAll() {
        this.all.clear();
        return this;
    }

    public DatabaseUpdater<T> clearPush() {
        this.push.clear();
        return this;
    }

    public DatabaseUpdater<T> setDatabase(SQLDatabase database) {
        this.database = database;
        if(DebugUtil.isDebug())
            XposedUtility.logD_xposed(TAG, "Set Database=" + Str.toStringOrNull(database));

        return this;
    }

    public DatabaseUpdater<T> setContinue(boolean shouldContinue) {
        this.shouldContinue = shouldContinue;
        return this;
    }

    public DatabaseUpdater<T> continueIfItemsNotEmpty() {
        this.shouldContinue = ListUtil.size(this.all) > 0;
        return this;
    }

    public DatabaseUpdater<T> setClazz(Class<T> clazz) {
        this.clazz = clazz;
        if(DebugUtil.isDebug())
            XposedUtility.logD_xposed(TAG, "Set Clazz=" + Str.toStringOrNull(clazz));
        return this;
    }

    public DatabaseUpdater<T> closeDatabase() {
        if(this.database != null)
            this.database.close();

        return this;
    }

    public DatabaseUpdater<T> setDatabaseIfExists(Context context, String databaseName) {
        if(!ObjectUtils.anyNull(context, databaseName)) {
            SQLDatabase db = new SQLDatabase(databaseName, context, false);
            if(db.exists()) {
                this.database = db;
            }
        }

        this.shouldContinue = this.database != null;
        return this;
    }

    public String getInfoPrefix() { return getInfoPrefix(table); }
    public String getInfoPrefix(TableInfo tableInfo) {
        tableInfo = tableInfo == null ? table : tableInfo;
        return Str.fm("All Count=%s  Push Count=%s  Database=%s  TableInfo=%s", this.all.size(), this.push.size(), this.database.file.getAbsolutePath(), tableInfo == null ? "null" : tableInfo.name);
    }

    public DatabaseUpdater<T> ensureTableExists() { return ensureTableExists(table); }
    public DatabaseUpdater<T> ensureTableExists(TableInfo tableInfo) {
        if(shouldContinue && !ObjectUtils.anyNull(tableInfo, database) && database.isOpen(true))
            this.shouldContinue = DatabaseHelpEx.prepareDatabase(database, tableInfo);

        if(DebugUtil.isDebug())
            XposedUtility.logD_xposed(TAG, "Ensure if Table Exists, Should Continue=" + this.shouldContinue + " " + getInfoPrefix(tableInfo));

        return this;
    }

    public DatabaseUpdater<T> ensureTableIsUpdated(boolean dropOldTableIfOutdated) { return ensureTableIsUpdated(table, dropOldTableIfOutdated); }
    public DatabaseUpdater<T> ensureTableIsUpdated(TableInfo tableInfo, boolean dropOldTableIfOutdated) {
        if(shouldContinue && !ObjectUtils.anyNull(database, tableInfo) && database.isOpen(true)) {
            int old = this.push.size();
            if(!database.doColumnNamesMatch(tableInfo.name, tableInfo.columns.keySet())) {
                sendRemainingToPush();
                XposedUtility.logI_xposed(TAG_ENSURE_TABLE, "Table Columns do not align!\n" + tableInfo);
                if(dropOldTableIfOutdated) {
                    database.beginTransaction(true);
                    database.endTransaction(true, database.dropTable(tableInfo.name));
                }
            }

            if(DebugUtil.isDebug())
                XposedUtility.logD_xposed(TAG_ENSURE_TABLE, Str.fm("Finished Ensuring Table matches columns, old push=%s  %s", old, getInfoPrefix(tableInfo)));
        }

        return this;
    }

    public DatabaseUpdater<T> ensureMatchesJson(String defaultsJson, Context context) { return ensureMatchesJson(table, defaultsJson, context, clazz); }
    public DatabaseUpdater<T> ensureMatchesJson(String defaultsJson, Context context, Class<T> clazz) { return ensureMatchesJson(table, defaultsJson, context, clazz); }
    public DatabaseUpdater<T> ensureMatchesJson(TableInfo tableInfo, String defaultsJson, Context context, Class<T> clazz) {
        if(shouldContinue && !ObjectUtils.anyNull(database, tableInfo) && database.isOpen(true)) {
            if(!DatabaseHelpEx.prepareDatabaseLocked(database, tableInfo))
                return this;

            if(defaultsJson != null) {
                List<T> jsonElements = JsonHelperEx.findJsonElementsFromAssets(XUtil.getApk(context), defaultsJson, true, clazz);
                if(DebugUtil.isDebug())
                    XposedUtility.logD_xposed(TAG, "Got JSON Elements Count=" + jsonElements.size() + " JSON=" + defaultsJson + " " + getInfoPrefix(tableInfo));

                Map<String, T> toUpdates = new HashMap<>();
                for(Map.Entry<String, Pair<T, String >> entry : this.push.entrySet()) {
                    Pair<T, String> pair = entry.getValue();
                    T item = pair.first;
                    String newId = pair.second;
                    if(!Str.isEmpty(newId))
                        toUpdates.put(newId, item);
                }

                for(T jsonElement : jsonElements) {
                    T up = toUpdates.get(jsonElement.getObjectId());
                    if(up != null) {
                        if(up.consumeId(jsonElement))
                            if(DebugUtil.isDebug())
                                XposedUtility.logD_xposed(TAG, "Consumed, id=" + up.getObjectId());
                    } else {
                        Pair<T, String> pItem = push.get(jsonElement.getObjectId());

                        if(pItem != null) {
                            if(pItem.first.consumeId(jsonElement))
                                if(DebugUtil.isDebug())
                                    XposedUtility.logD_xposed(TAG, "Consumed, id=" + pItem.first.getObjectId() + " new id=" + pItem.second);
                        } else {
                            T item = all.get(jsonElement.getObjectId());
                            if(item == null)
                                push.put(jsonElement.getObjectId(), Pair.create(jsonElement, null));
                            else if(item.consumeId(jsonElement)) {
                                push.put(item.getObjectId(), Pair.create(item, null));
                                if(DebugUtil.isDebug())
                                    XposedUtility.logD_xposed(TAG, "Consumed, id=" + item.getObjectId());
                            }
                        }
                    }
                }
            }
        }

        return this;
    }

    public DatabaseUpdater<T> pushUpdates(IContentValues<T> ic) { return pushUpdates(table, ic); }
    public DatabaseUpdater<T> pushUpdates(TableInfo toTable, IContentValues<T> ic) {
        if(shouldContinue && !ObjectUtils.anyNull(database, toTable, ic) && database.isOpen(true)) {
            if(!DatabaseHelpEx.prepareDatabaseLocked(database, toTable)) {
                XposedUtility.logE_xposed(TAG_PUSH, "Failed to prepare Database for push!");
                return this;
            }

            if(!database.beginTransaction(true)) {
                XposedUtility.logE_xposed(TAG_PUSH, "Failed to Begin Database Transaction for push!");
                return this;
            }

            Map<String, Pair<T, String>> failed = new HashMap<>();
            Map<String, T> success = new HashMap<>();
            List<String> removed = new ArrayList<>();
            if(DebugUtil.isDebug())
                XposedUtility.logD_xposed(TAG_PUSH, Str.fm("Pushing Database Update, %s", getInfoPrefix(toTable)));

            StrBuilder sb = StrBuilder.create().ensureOneNewLinePer(true);
            try {
                for(Map.Entry<String, Pair<T, String>> entry : push.entrySet()) {
                    String id = entry.getKey();
                    Pair<T, String> pair = entry.getValue();
                    T item = pair.first;
                    String newId = pair.second;

                    if(item == null) {
                        Log.e(TAG_PUSH, "Item is fucking null ? " + id);
                        continue;
                    }

                    //How are we dropping the old ?
                    //database.delete()
                    //DatabaseHelpEx.deleteItem()
                    //item.getSharedId()
                    //We can also just update ? no ?
                    //Either way we need to SQL Snake this bitch

                    SQLSnake snake = item.createSnake();
                    if(newId != null) {
                        item.setId(newId);
                        sb.appendLine(Str.combineEx("Old ID=", id, " => ", newId));
                    }

                    ContentValues cv = ic.getContentValues(item);
                    if(!Str.isEmpty(newId) && snake != null && !Str.areEqualIgnoreCase(id, newId)) {
                        if(cv != null && database.update(toTable.name, cv, snake)) {
                            if(!removed.contains(id))
                                removed.add(id);

                            continue;
                        } else  {
                            Log.e(TAG_PUSH, "Failed to Update Entry: " + id + " Prefix=" + getInfoPrefix(toTable));
                            database.delete(toTable.name, snake.getWhereClause(), snake.getWhereArgs());
                            if(!removed.contains(id))
                                removed.add(id);
                        }
                    }

                    if(!database.insert(toTable.name, cv)) {
                        Log.e(TAG_PUSH, "Failed to Insert Entry: " + id + " Prefix=" + getInfoPrefix(toTable));
                        failed.put(id, pair);
                        if(newId != null)
                            item.setId(id);
                    } else {
                        success.put(item.getObjectId(), item);
                    }
                }

                database.setTransactionSuccessful();
            } finally {
                database.endTransaction(true, false);
                ListUtil.addAllIfValid(this.push, failed, true);
                ListUtil.addAllIfValid(this.all, success, false);

                StrBuilder sb2 = StrBuilder.create().ensureOneNewLinePer(true);
                for(String rem : removed) {
                    sb2.appendLine(Str.combineEx("[REM]::[", rem, "]::[", String.valueOf(this.all.remove(rem) != null), "]"));
                    this.all.remove(rem);
                }

                if(DebugUtil.isDebug())
                    XposedUtility.logD_xposed(TAG_PUSH, Str.fm("Removed From Table=%s  Output=%s",
                            toTable.name,
                            sb2.toString(true)));

                if(DebugUtil.isDebug())
                    XposedUtility.logD_xposed(TAG_PUSH, Str.fm("Updated Table=%s  Output=%s",
                            toTable.name,
                            sb.toString(true)));

                if(DebugUtil.isDebug())
                    XposedUtility.logD_xposed(TAG_PUSH, Str.fm("Finished Pushing %s Failed=%s Success=%s",
                            getInfoPrefix(toTable),
                            failed.size(),
                            success.size()));
            }
        }

        return this;
    }

    public DatabaseUpdater<T> sendRemainingToPush() {
        if(shouldContinue) {
            if(!this.all.isEmpty()) {
                for(Map.Entry<String, T> entry : all.entrySet()) {
                    String id = entry.getKey();
                    T item = entry.getValue();
                    if(!push.containsKey(id))
                        push.put(id, Pair.create(item, null));
                }
            }
        }

        return this;
    }

    public DatabaseUpdater<T> continueIfTableExists(String tableName) { return continueIfTableExists(tableName, -1, false); }
    public DatabaseUpdater<T> continueIfTableExists(String tableName, int minimumItemCount, boolean dropIfLessThanMinimum) {
        if(database == null)
            this.shouldContinue = false;
        else {
            if(!database.isOpen(true)) {
                this.shouldContinue = false;
                return this;
            }

            if(!database.hasTable(tableName))
                this.shouldContinue = false;
            else if(minimumItemCount > -1) {
                int count = database.tableEntries(tableName);
                this.shouldContinue = count >= minimumItemCount;
                if(dropIfLessThanMinimum && !this.shouldContinue) {
                    database.beginTransaction(true);
                    database.endTransaction(true, database.dropTable(tableName));
                }
            }
        }

        return this;
    }
    public DatabaseUpdater<T> getItems(boolean compressUIDs, boolean dropTableIfExists) { return getItems(clazz, table.name, compressUIDs, dropTableIfExists); }
    public DatabaseUpdater<T> getItems(String tableName, boolean compressUIDs, boolean dropTableIfExists) { return getItems(clazz, tableName, compressUIDs, dropTableIfExists); }
    public DatabaseUpdater<T> getItems(Class<T> clazz, String tableName, boolean compressUIDs, boolean dropTableIfExists) {
        if(DebugUtil.isDebug())
            XposedUtility.logD_xposed(TAG, "Getting Items, do=" + shouldContinue + " " + getInfoPrefix());

        //This can cause user related issues
        //If multiple users, then the ID key will be written
        //ToDo: High Priority
        if(shouldContinue && !ObjectUtils.anyNull(clazz, tableName, database) && database.isOpen(true)) {
            List<T> items = compressUIDs ?
                    UpdateUtils.getUidDatabaseEntries(database, tableName, clazz, dropTableIfExists) :
                    DatabaseHelpEx.getFromDatabase(database, tableName, clazz, true);

            for(T item : items)
                this.all.put(item.getObjectId(), item);

            if(DebugUtil.isDebug())
                XposedUtility.logD_xposed(TAG, "Got All Items, Size=" + this.all.size() + " Table Name=" + tableName);
        }

        return this;
    }

    public DatabaseUpdater<T> ensureItemsMapped(Context context, String jsonIdFile) {
        if(shouldContinue && !ObjectUtils.anyNull(context, jsonIdFile)) {
            if(!this.all.isEmpty()) {
                LinkedHashMap<String, T> mapFull = new LinkedHashMap<>();
                for(Map.Entry<String, T> entry : this.all.entrySet()) {
                    String id = entry.getKey();
                    T item = entry.getValue();

                    //This is to check make sure it is not already being pushed or updated at least its ID
                    Pair<T, String> pair = this.push.get(id);
                    if(pair == null || Str.isEmpty(pair.second))
                        mapFull.put(id, item);
                }

                if(DebugUtil.isDebug())
                    XposedUtility.logD_xposed(TAG, "Ensure Mapping, Full Size=" + mapFull.size() + " " + getInfoPrefix());

                //Got to make sure if something is already getting updated don't push
                //Should not matter actually should be fine
                for(Map.Entry<String, Pair<T, String>> entry : this.push.entrySet()) {
                    Pair<T, String > pair = entry.getValue();
                    T item = pair.first;
                    String newId = pair.second;
                    if(Str.isEmpty(newId) && !mapFull.containsKey(entry.getKey()))
                        mapFull.put(entry.getKey(), item);
                }

                if(DebugUtil.isDebug())
                    XposedUtility.logD_xposed(TAG, "Ensure Mapping done enumerating push, Full Size=" + mapFull.size() + " " + getInfoPrefix());

                if(ListUtil.isValid(mapFull)) {
                    Map<T, String> map = UpdateUtils.remapItems(context, mapFull.values(), jsonIdFile);
                    if(ListUtil.isValid(map)) {
                        for(Map.Entry<T, String> entry : map.entrySet()) {
                            T item = entry.getKey();
                            String newId = entry.getValue();
                            String oldId = item.getObjectId();
                            if(push.containsKey(oldId))
                                if(DebugUtil.isDebug())
                                    XposedUtility.logD_xposed(TAG, "Push already Contains the ID item: " + oldId);

                            if(!Str.isEmpty(newId)) {
                                push.put(oldId, Pair.create(item, newId));
                            }
                        }
                    }
                }

                if(DebugUtil.isDebug())
                    XposedUtility.logD_xposed(TAG, "Ensure Mapping finished, " + getInfoPrefix());

            }
        }

        return this;
    }

    /*

    public DatabaseUpdater<T> ensureItemsMapped(Context context, String jsonIdFile) {
        if(shouldContinue && !ObjectUtils.anyNull(context, jsonIdFile, this.items)) {
            this.map = UpdateUtils.remapItems(context, this.items, jsonIdFile);
            this.same.clear();
            this.needsUpdate.clear();
            if(ListUtil.isValid(this.map)) {
                for(Map.Entry<T, String> entry : this.map.entrySet()) {
                    T item = entry.getKey();
                    String newId = entry.getValue();

                    if(newId != null && !newId.isEmpty())
                        needsUpdate.put(item, newId);
                    else {
                        same.add(item);
                    }
                }
            }
        }

        return this;
    }

    public DatabaseUpdater<T> pushSame(TableInfo toTable, IContentValues<T> ic) {
        if(DatabaseHelpEx.prepareDatabaseLocked(database, toTable)) {
            if(ListUtil.size(this.same) > 0 && ic != null) {
                if(!database.beginTransaction(true)) {
                    XposedUtility.logE_xposed(TAG, "Failed to begin Transaction [pushSame]");
                    return this;
                }

                List<T> sameCopy = new ArrayList<>(this.same);
                ListUtil.clear(this.same);
                try {
                    for(T item : sameCopy) {
                        ContentValues cv = ic.getContentValues(item);
                        if(!database.insert(toTable.name, cv))
                            this.same.add(item);
                    }

                    database.setTransactionSuccessful();
                }finally {
                    database.endTransaction(true, false);
                    if(DebugUtil.isDebug())
                        XposedUtility.logD_xposed(TAG, Str.fm("Same Sized after push=%s  Before push=%s  Items=%s  Map=%s", ListUtil.size(this.same), sameCopy.size(), ListUtil.size(this.items), ListUtil.size(this.map)));

                }
            }
        }

        return this;
    }

    public DatabaseUpdater<T> pushUpdated(TableInfo toTable, IContentValues<T> ic) {
        if(DatabaseHelpEx.prepareDatabaseLocked(database, toTable)) {
            if(ListUtil.size(this.needsUpdate) > 0 && ic != null) {
                if(!database.beginTransaction(true)) {
                    XposedUtility.logE_xposed(TAG, "Failed to begin Transaction [pushUpdated]");
                    return this;
                }

                WeakHashMap<T, String> updateCopy = new WeakHashMap<>(this.needsUpdate);
                ListUtil.clear(this.needsUpdate);
                try {
                    for(Map.Entry<T, String> entry : updateCopy.entrySet()) {
                        T item = entry.getKey();
                        String newId = entry.getValue();
                        if(item == null || TextUtils.isEmpty(newId))
                            continue;

                        String oldId = item.getId();
                        item.setId(newId);

                        ContentValues cv = ic.getContentValues(item);
                        if(!database.insert(toTable.name, cv)) {
                            item.setId(oldId);
                            this.needsUpdate.put(item, newId);
                        }
                    }

                    database.setTransactionSuccessful();
                }finally {
                    database.endTransaction(true, false);
                    if(DebugUtil.isDebug())
                        XposedUtility.logD_xposed(TAG, Str.fm("Updated Sized after push=%s  Before push=%s  Items=%s  Map=%s", ListUtil.size(this.needsUpdate), updateCopy.size(), ListUtil.size(this.items), ListUtil.size(this.map)));
                }
            }
        }

        return this;
    }*/

    public static boolean ensureUpdated(Context context, SQLDatabase database) {
        if(database.isOpen(true)) {
            if(DebugUtil.isDebug())
                XposedUtility.logI_xposed(TAG, "Starting Update Checker, " + database.file.getAbsolutePath() + " name=" + database.name + " IsOpen=" + database.isOpen(true) + " IsXLua=" + database.isXLua());

            if(database.isXLua()) {
                try {
                    //Just have it so the main fucking legacy item has a function like "readFromOld(Cursor c)" or something
                    //And have a function "ensureTableIsMoved(From, To)

                    /* Init Settings, Convert old to New / Re Map Names */
                    /*new DatabaseUpdater<SettingReMappedItem.Setting_legacy>()
                            .setDatabase(database)
                            .setClazz(SettingReMappedItem.Setting_legacy.class)
                            .continueIfTableExists(SettingReMappedItem.Setting_legacy.OLD_TABLE_NAME, 1, true)
                            .getItems(SettingReMappedItem.Setting_legacy.OLD_TABLE_NAME, true, true)
                            .ensureItemsMapped(context, SettingReMappedItem.Setting_legacy.JSON)
                            .sendRemainingToPush()
                            .setTable(SettingPacket.TABLE_INFO)
                            .pushUpdates(SettingReMappedItem.Setting_legacy::toContentValuesFromLegacy)
                            .clearMaps(true)
                            .ensureTableExists()
                            .getItems(false, false)
                            .ensureTableIsUpdated(true)
                            .ensureItemsMapped(context, SettingReMappedItem.Setting_legacy.JSON)
                            .pushUpdates(SettingReMappedItem.Setting_legacy::toContentValuesFromNew);*/


                    //Ensure IDs are Updated
                    new IDMapper<SettingReMappedItem.Setting_legacy>()
                            .setDatabase(database)
                            .setTable(SettingPacket.TABLE_INFO)
                            .getItems(SettingReMappedItem.Setting_legacy.class, false, false)
                            .remapToJson(context, SettingReMappedItem.Setting_legacy.JSON, SettingReMappedItem.Setting_legacy::toContentValuesFromNew);


                    /*new DatabaseUpdater<SettingInfoPacket>()
                            .setDatabase(database)
                            .setClazz(SettingInfoPacket.class)
                            .setTable(SettingInfoPacket.TABLE_INFO)
                            .ensureTableExists()
                            .getItems(SettingInfoPacket.TABLE_NAME, false, true)    //Should we drop ?
                            .ensureItemsMapped(context, SettingReMappedItem.Setting_legacy.JSON)
                            .ensureTableExists()
                            .pushUpdates(SettingInfoPacket::toContentValues);*/

                    /* Assignment => Assignments */
                    //Fix this, we have to use "package"
                    //This is broken it drops new Assignments Table
                    //You need to re do this fucking updater fuck this Updater Class we will use things like "ColumnSimpleReMapper" etc
                    //This class is ACTUALLY banned from being used,
                    /*new DatabaseUpdater<AssignmentLegacy>()
                            .setDatabase(database)
                            .setClazz(AssignmentLegacy.class)
                            .setTable(AssignmentLegacy.TABLE_INFO)
                            .continueIfTableExists(AssignmentLegacy.TABLE_NAME, 1, true)
                            .getItems(true, true)
                            .ensureTableExists(AssignmentPacket.TABLE_INFO)
                            .pushUpdates(AssignmentPacket.TABLE_INFO, (o) -> {
                                AssignmentPacket packet = new AssignmentPacket(o);
                                return packet.toContentValues();
                            });*/

                    ColumnSimpleReMapper.create()
                            .setDatabase(database)
                            .createMap(context)
                            .ensureIsUpdated(AssignmentPacket.TABLE_INFO);




                    ColumnSimpleReMapper.create()
                            .setDatabase(database)
                            .createMap(context)
                            .ensureIsUpdated(SettingInfoPacket.TABLE_INFO);

                    /* Init Settings Defaults */
                    new DatabaseUpdater<SettingInfoPacket>()
                            .setDatabase(database)
                            .setClazz(SettingInfoPacket.class)
                            .setTable(SettingInfoPacket.TABLE_INFO)
                            .ensureTableExists()
                            .getItems(false, false)
                            .ensureMatchesJson(SettingInfoPacket.JSON, context)
                            .ensureTableIsUpdated(true)
                            .pushUpdates(SettingInfoPacket::toContentValues);

                    new IDMapper<SettingInfoPacket>()
                            .setDatabase(database)
                            .setTable(SettingInfoPacket.TABLE_INFO)
                            .getItems(SettingInfoPacket.class, false, false)
                            .remapToJson(context, SettingInfoPacket.REMAP_JSON, SettingInfoPacket::toContentValues);

                }catch (Exception e) {
                    XposedUtility.logE_xposed(TAG, "Error Updating= error:" + e);
                }
            }
        }

        return true;
    }
}
