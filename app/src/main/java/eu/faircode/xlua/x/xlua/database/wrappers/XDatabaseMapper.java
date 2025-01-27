package eu.faircode.xlua.x.xlua.database.wrappers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XposedBridge;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.JsonHelperEx;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.database.TableInfo;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.hook.GroupPacket;
import eu.faircode.xlua.x.xlua.identity.IUidCompress;
import eu.faircode.xlua.x.xlua.settings.SettingReMappedItem;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

public class XDatabaseMapper {
    private static final String TAG = "XLua.XDatabaseMapper";

    public static final String JSON_SETTINGS_REMAP = "remap_settings.json";
    public static final String JSON_SETTINGS_DEFAULT = "settingdefaults.json";

    public static final String TABLE_SETTINGS_OLD = "setting";
    public static final String TABLE_ASSIGNMENTS_OLD = "assignment";

    private static final LinkedHashMap<String, String> RE_MAPPED_CACHE = new LinkedHashMap<>();

    public static void initReMapCache(Context context) {
        if(context != null) {
            synchronized (RE_MAPPED_CACHE) {
                if(RE_MAPPED_CACHE.isEmpty()) {
                    Collection<SettingReMappedItem> remapped_settings = JsonHelperEx.findJsonElementsFromAssets(
                            XUtil.getApk(context),
                            JSON_SETTINGS_REMAP,
                            true,
                            SettingReMappedItem.class);

                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Re Map Init Cache, Settings Count=" + ListUtil.size(remapped_settings));

                    if(ListUtil.isValid(remapped_settings))
                        for(SettingReMappedItem item : remapped_settings)
                            for(String oldName : item.oldNames)
                                RE_MAPPED_CACHE.put(oldName, item.name);

                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Finished Initializing the Re Mapped Settings to Cache, Size=" + RE_MAPPED_CACHE.size());

                }
            }
        }
    }

    public static boolean clean(Context context, SQLDatabase database) {
        try {
            for(TableInfo tableInfo : Arrays.asList(SettingPacket.TABLE_INFO, AssignmentPacket.TABLE_INFO, GroupPacket.TABLE_INFO)) {
                if(!DatabaseHelpEx.prepareDatabase(database, tableInfo)) {
                    logE(Str.fm("Failed to Prepare Database Table! Database=%s Table=%s", Str.noNL(database), tableInfo.name));
                    return false;
                }
            }

            if(!database.beginTransaction(true)) {
                logE("Failed to being Database Transaction! Skipping Re Mapper!");
                return false;
            }

            if(database.tableEntries(TABLE_SETTINGS_OLD) > 0) {
                if(DebugUtil.isDebug())
                    logD("Original Settings Table has Entries, Re Mapping them to the new Table, Count=" + database.tableEntries(TABLE_SETTINGS_OLD));


                Collection<SettingReMappedItem> remapped_settings =
                        JsonHelperEx.findJsonElementsFromAssets(
                                XUtil.getApk(context), JSON_SETTINGS_REMAP, true, SettingReMappedItem.class);



            }



        }catch (Exception e) {
            logE(Str.fm("Failed to Init Database, Re Map Failure. Database=%s  Error=%s  Stack=%s", database, e, RuntimeUtils.getStackTraceSafeString(e)));
        }finally {
            return false;
        }
    }


    public static <T extends IUidCompress> List<T> compressGhostUid(SQLDatabase database, String tableName, Class<T> clazz) {
        Collection<T> database_values = DatabaseHelpEx.getFromDatabase(database, tableName, clazz, true);

        if(DebugUtil.isDebug())
            Log.d(TAG, "Database Entry Count for Table [" + tableName + "] Count=" + ListUtil.size(database_values) + " Compressing Ghost UIDs");

        if(!ListUtil.isValid(database_values))
            return ListUtil.copyToArrayList(database_values);

        LinkedHashMap<String, LinkedHashMap<String, T>> map = new LinkedHashMap<>();
        for(T item : database_values) {
            if(TextUtils.isEmpty(item.getCategory()) || TextUtils.isEmpty(item.getId()))
                continue;

            LinkedHashMap<String, T> category_values = map.get(item.getCategory());
            if(category_values == null) {
                category_values = new LinkedHashMap<>();
                map.put(item.getCategory(), category_values);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Created Map for UID Compression, Category=" + item.getCategory() + " Id=" + item.getId());
            }

            category_values.put(item.getId(), item);
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Total Categories for UID Compression=" + map.size());

        List<T> final_output = new ArrayList<>();
        for(LinkedHashMap.Entry<String, LinkedHashMap<String, T>> entry : map.entrySet()) {
            String category = entry.getKey();
            Map<String, T> block = entry.getValue();
            if(ObjectUtils.anyNull(category, block))
                continue;

            if(DebugUtil.isDebug())
                Log.d(TAG, "Category [" + category + "] Block Size=" + block.size() + " Pushing to main list!");

            final_output.addAll(block.values());
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Finished Cleaning out any Ghost UIDs, Final size of Items=" + ListUtil.size(final_output));

        return final_output;
    }

    public static void logI(String msg) {
        Log.i(TAG, msg);
        XposedBridge.log(TAG + " " + msg);
    }

    public static void logD(String msg) {
        Log.d(TAG, msg);
        XposedBridge.log(TAG + " " + msg);
    }

    public static void logE(String msg) {
        Log.e(TAG, msg);
        XposedBridge.log(TAG + " " + msg);
    }

    public static void logW(String msg) {
        Log.w(TAG, msg);
        XposedBridge.log(TAG + " " + msg);
    }
}
