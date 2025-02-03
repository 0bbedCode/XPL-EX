package eu.faircode.xlua.x.xlua.database.updaters;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.x.LogX;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.JsonHelperEx;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.xlua.XposedUtility;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.interfaces.ICursorType;

/*

 */
public class UpdateUtils {
    public static final String TAG = "XLua.UpdateUtils";
    public static final String TAG_COMPRESS = "XLua.UpdateUtils(getUidDatabaseEntries)";
    public static final String TAG_R_ITEMS = "XLua.UpdateUtils(remapItems)";


    //JsonHelperEx.findJsonElementsFromAssets(XUtil.getApk(context), JSON_SETTINGS_DEFAULT, true, SettingInfoPacket.class))
    //id [new ids....]

    public static <T extends IIdentifiableObject & ICursorType> WeakHashMap<T, String> remapItems(Context context, Collection<T> items, String jsonFile) {
        if(ObjectUtils.anyNull(context, items, jsonFile)) {
            XposedUtility.logE_xposed(TAG_R_ITEMS, LogX.errorInput(context, items, jsonFile));
            return new WeakHashMap<>();
        }

        WeakHashMap<T, String> result = new WeakHashMap<>();

        List<UpdateMapEntry> jsonEntries = JsonHelperEx.findJsonElementsFromAssets(
                XUtil.getApk(context),
                jsonFile,
                true,
                UpdateMapEntry.class);

        String prefix = Str.fm("Items Count=%s JsonFile=%s Json Items Count=%s", items.size(), jsonFile, jsonEntries.size());
        try {
            if(!ListUtil.isValid(jsonEntries))
                throw new Exception("Invalid JSON Entries Count! Count=" + jsonEntries.size());

            //
            //Lets pretend we got these two settings
            //
            //cell.old.mnc   => cell.mnc.2
            //cell.older.mnc => cell.mnc.1
            //
            //

            HashMap<String, String> map = new HashMap<>(jsonEntries.size());
            for(UpdateMapEntry updateEntry : jsonEntries)
                for(String oldId : updateEntry.oldIds)
                    map.put(oldId, updateEntry.id);

            for(T item : items)
                result.put(item, map.get(item.getSharedId()));

        }catch (Exception e) {
            XposedUtility.logE_xposed(TAG_R_ITEMS, Str.fm("Error Re Mapping Items to new Mappings! Error=%s %s", e, prefix));
        } finally {
            if(DebugUtil.isDebug())
                XposedUtility.logD_xposed(TAG_R_ITEMS, Str.fm("Returning from mapping items, Map Size=%s %s", result.size(), prefix));
        }

        return result;
    }


    public static <T extends IIdentifiableObject & ICursorType> List<T> getUidDatabaseEntries(SQLDatabase database, String tableName, Class<T> clazz, boolean dropTableIfExists) {
        if(ObjectUtils.anyNull(database, clazz, tableName)) {
            LogX.errorInput(database, clazz, tableName);
            return ListUtil.emptyList();
        }

        List<T> list = new ArrayList<>();
        boolean hasTable = database.hasTable(tableName);
        int tableEntryCount = database.tableEntries(tableName);

        String prefix = Str.fm("Database=%s TableName=%s Clazz=%s Table Exist=%s Table Entry Count=%s", Str.noNL(database), tableName, Str.toStringOrNull(clazz), hasTable, tableEntryCount);
        try {
            if(hasTable) {
                List<T> databaseEntries = DatabaseHelpEx.getFromDatabase(database, tableName, clazz, true);
                if(!ListUtil.isValid(databaseEntries))
                    throw new Exception("Failed to get Database Entries");

                if(DebugUtil.isDebug())
                    Log.d(TAG_COMPRESS, Str.fm("Got Database Entries now Organizing to use newest values, Count=%s %s", ListUtil.size(databaseEntries), prefix));

                LinkedHashMap<String, LinkedHashMap<String, T>> organized = new LinkedHashMap<>();
                for(T item : databaseEntries) {
                    String category = item.getCategory();
                    String id = item.getSharedId();

                    if(TextUtils.isEmpty(category) || TextUtils.isEmpty(id))
                        continue;

                    LinkedHashMap<String, T> categoryValues = organized.get(category);
                    if(categoryValues == null) {
                        categoryValues = new LinkedHashMap<>();
                        organized.put(category, categoryValues);
                        if(DebugUtil.isDebug())
                            Log.d(TAG_COMPRESS, Str.fm("Created Category Value Map, TableName=%s, Category=%s Id=%s", tableName, category, id));
                    }

                    categoryValues.put(item.getSharedId(), item);
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG_COMPRESS, Str.fm("Created organized map that compresses ghost UIDs now pushing to list, Size=%s %s", organized.size(), prefix));

                for(Map.Entry<String, LinkedHashMap<String, T>> entry : organized.entrySet()) {
                    String category = entry.getKey();
                    Map<String, T> block = entry.getValue();
                    if(ObjectUtils.anyNull(category, block))
                        continue;

                    list.addAll(block.values());
                    if(DebugUtil.isDebug())
                        Log.d(TAG_COMPRESS, Str.fm("Pushed Category block, Category=%s Size=%s TableName=%s", category, block.size(), tableName));
                }

                if(dropTableIfExists) {
                    database.beginTransaction(false);
                    database.endTransaction(false, database.dropTable(tableName));
                }
            }
        }catch (Exception e) {
            XposedUtility.logE_xposed(TAG_COMPRESS, Str.fm("Error Compressing UID Entries, error=%s %s", e, prefix));
        } finally {
            if(DebugUtil.isDebug())
                XposedUtility.logD_xposed(TAG_COMPRESS, Str.fm("Returning from Compressing UID Entries, Size=%s %s", ListUtil.size(list), prefix));
        }

        return list;
    }
}
