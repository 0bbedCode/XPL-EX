package eu.faircode.xlua.x.xlua.database.updaters;

import android.content.ContentValues;
import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.JsonHelperEx;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.XposedUtility;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.database.TableInfo;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.database.sql.SQLSnake;
import eu.faircode.xlua.x.xlua.interfaces.ICursorType;

/** @noinspection UnusedReturnValue*/
public class IDMapper<T extends IIdentifiableObject & ICursorType> {
    private static final String TAG = LibUtil.generateTag(IDMapper.class);

    public interface IContentValues<T extends IIdentifiableObject & ICursorType> {  ContentValues getContentValues(T o); }

    private SQLDatabase database;
    private TableInfo tableInfo;
    private int total = 0;
    private final Map<String, List<T>> everything = new HashMap<>();

    public void put(T item) {
        if(item != null) {
            String id = item.getObjectId();
            List<T> items = everything.get(id);
            if(items == null) {
                items = new ArrayList<>(1);
                items.add(item);
                total++;
                everything.put(id, items);
            } else {
                if(!items.contains(item)) {
                    items.add(item);
                    total++;
                }
            }
        }
    }

    public IDMapper<T> setTable(TableInfo tableInfo) {
        this.tableInfo = tableInfo;
        return this;
    }

    public IDMapper<T> setDatabase(SQLDatabase database) {
        this.database = database;
        return this;
    }

    public IDMapper<T> getItems(Class<T> clazz, boolean compressUIDs, boolean dropTableIfExists) {
        if(DebugUtil.isDebug())
            XposedUtility.logD_xposed(TAG, Str.fm("Getting Items in Database [%s] from Table [%s] Compress UIDs=%s Drop If Table Exists=%s",
                    Str.noNL(database.toString()),
                    tableInfo.name,
                    compressUIDs,
                    dropTableIfExists));

        if(!ObjectUtils.anyNull(clazz, tableInfo.name, database) && database.isOpen(true)) {
            List<T> items = compressUIDs ?
                    UpdateUtils.getUidDatabaseEntries(database, tableInfo.name, clazz, dropTableIfExists) :
                    DatabaseHelpEx.getFromDatabase(database, tableInfo.name, clazz, true);

            for(T item : items)
                put(item);

            if(DebugUtil.isDebug())
                XposedUtility.logD_xposed(TAG, Str.fm("Got Items, Total=%s Groups Count=%s", this.total, this.everything.size()));
        }

        return this;
    }

    public IDMapper<T> remapToJson(Context context, String json, IContentValues<T> ic) {
        if(ObjectUtils.anyNull(context, json, tableInfo, ic, database)) {
            XposedUtility.logE_xposed(TAG, "(2) Something is null find out fucker....");
            return this;
        }

        if(!database.isOpen(true)) {
            XposedUtility.logE_xposed(TAG, "(2) Failed to open Database...");
            return this;
        }

        Map<String, String> idetifiersMap = new HashMap<>();
        List<UpdateMapEntry> jsonEntries = JsonHelperEx.findJsonElementsFromAssets(
                XUtil.getApk(context),
                json,
                true,
                UpdateMapEntry.class);

        for(UpdateMapEntry entry : jsonEntries)
            for(String old : entry.oldIds)
                idetifiersMap.put(old, entry.id);

        if(DebugUtil.isDebug())
            XposedUtility.logD_xposed(TAG, Str.fm("Finished Mapping JSON [%s] File Map Count=%s Map Count=%s Table=%s",
                    json,
                    jsonEntries.size(),
                    idetifiersMap.size(),
                    tableInfo.name));

        int failed = 0;
        int skipped = 0;
        int updated = 0;
        try {
            if(!DatabaseHelpEx.prepareDatabase(database, tableInfo)) {
                XposedUtility.logE_xposed(TAG, Str.fm("Failed to Prepare Table [%s] JSON [%s]", tableInfo.name, json));
                return this;
            }

            database.beginTransaction(true);
            for(Map.Entry<String, List<T>> entry : new HashMap<>(this.everything).entrySet()) {
                String blockId = entry.getKey();
                List<T> block = entry.getValue();
                String newId = idetifiersMap.get(blockId);
                if(idetifiersMap.containsKey(blockId) && ListUtil.isValid(block)) {
                    if(Str.isEmpty(newId))
                        continue;   //Weird

                    for(T item : block) {
                        SQLSnake snake = item.createSnake();
                        item.setId(newId);
                        if(snake != null) {
                            if(this.everything.containsKey(newId)) {
                                if(database.delete(tableInfo.name, snake.getWhereClause(), snake.getWhereArgs())) {
                                    updated++;
                                    if(DebugUtil.isDebug())
                                        XposedUtility.logW_xposed(TAG, Str.fm("Item [%s] already Exists, Deleted current one [%s]",
                                                newId,
                                                blockId));
                                }else {
                                    failed++;
                                    if(DebugUtil.isDebug())
                                        XposedUtility.logW_xposed(TAG, Str.fm("Item [%s] already Exists, Failed to Delete current one [%s]",
                                                newId,
                                                blockId));
                                }
                            } else {
                                if(!database.update(tableInfo.name, ic.getContentValues(item), snake)) {
                                    failed++;
                                    if(DebugUtil.isDebug())
                                        XposedUtility.logE_xposed(TAG, Str.fm("Failed to Update Item From Block ID [%s] to new ID [%s] from JSON [%s] to Table [%s] Block Item Count=%s",
                                                blockId,
                                                newId,
                                                json,
                                                tableInfo.name,
                                                block.size()));
                                } else {
                                    put(item);
                                    updated++;
                                }
                            }
                        }
                    }
                } else {
                    skipped++;
                    if(DebugUtil.isDebug())
                        XposedUtility.logD_xposed(TAG, Str.fm("Skipping Block [%s] Item Count=%s Table=%s",
                                blockId,
                                block.size(),
                                tableInfo.name));
                }
            }

            database.setTransactionSuccessful();
        }catch (Exception e) {
            XposedUtility.logE_xposed(TAG, Str.fm("Failed to Re Map via JSON [%s] Table [%s] Updated Count=%s Error=%s",
                    json,
                    tableInfo.name,
                    updated,
                    e));
        }finally {
            database.endTransaction(true, false);
            if(DebugUtil.isDebug())
                XposedUtility.logD_xposed(TAG, Str.fm("Returning from attempt update from JSON [%s] Table [%s] Updated Count=%s Failed Count=%s Skipped Count=%s",
                        json,
                        tableInfo.name,
                        updated,
                        failed,
                        skipped));
        }


        return this;
    }
}
