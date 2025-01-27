package eu.faircode.xlua.x.xlua.database.wrappers;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import eu.faircode.xlua.x.xlua.hook.AssignmentLegacy;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.hook.GroupLegacy;
import eu.faircode.xlua.x.xlua.hook.GroupPacket;
import eu.faircode.xlua.x.xlua.settings.SettingReMappedItem;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.identity.IUidCompress;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;
import eu.faircode.xlua.x.xlua.identity.UserIdentityUtils;
import eu.faircode.xlua.x.xlua.settings.data.SettingInfoPacket;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

/*
    ToDo: Do Path Update /data/misc
            MAYBE add a Interceptor(s) for Settings, Ensures the Old is with the New
            Issues are If the Context is in the Hook, or something can have Context issues or Speed
                Theory Cache in <String, String> <OLD_NAME,NEW_NAME>
            Update Default Values
            Update Mock Props
            Update any Hooks stored locally
            Support "deleted" settings or "disabled" settings
            Major Clean this class when time comes around not #1
 */
public class GlobalDatabaseResolver {
    private static final String TAG = "XLua.SettingsTransformer";

    public static final String JSON_SETTINGS_REMAP = "remap_settings.json";
    public static final String JSON_SETTINGS_DEFAULT = "settingdefaults.json";

    public static final String TABLE_SETTINGS_OLD = "setting";
    public static final String TABLE_ASSIGNMENTS_OLD = "assignment";
    public static final String TABLE_GROUP_OLD = "`group`";

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

    public static String resolveName(String name) { return resolveName(null, name); }
    public static String resolveName(Context context, String name) {
        initReMapCache(context);
        String resolved = RE_MAPPED_CACHE.get(name);
        return resolved == null ? name : resolved;
    }

    public static void initEnsureFunctions(Context context, SQLDatabase database) {
        ensureDefaultsReMapped(context, database);
        ensureTransformation(context, database);
        ensureAssignments(context, database);
        ensureGroups(context, database);
    }

    public static boolean ensureTransformation(Context context, SQLDatabase database) {
        initReMapCache(context);
        try {
           if(!DatabaseHelpEx.prepareDatabase(database, SettingPacket.TABLE_NAME, SettingPacket.COLUMNS)) {
               Log.e(TAG, "Failed to Prepare Table! [" + SettingPacket.TABLE_NAME + "] Can not Start Re Mapping!");
               return false;
           }

           //Read Lock ?
           if(!database.beginTransaction(true)) {
               Log.e(TAG, "Failed to being Database Transaction! Skipping Re Mapper for Settings!");
               return false;
           }

           if(database.tableEntries(TABLE_SETTINGS_OLD) > 0) {
               Collection<SettingReMappedItem> remapped_settings = JsonHelperEx.findJsonElementsFromAssets(XUtil.getApk(context), JSON_SETTINGS_REMAP, true, SettingReMappedItem.class);
               if(DebugUtil.isDebug())
                   Log.d(TAG, "Old Settings Table exists and has entries, [" + TABLE_SETTINGS_OLD + "] Starting ReMap! Old Count=" + database.tableEntries(TABLE_SETTINGS_OLD) + " Re Map Count=" + ListUtil.size(remapped_settings));

               LinkedHashMap<String, List<SettingReMappedItem.Setting_legacy>> old_pairs = toNamedSettingsMap(getOldSettings(database, true));

               if(DebugUtil.isDebug())
                   Log.d(TAG, "Pushing Settings with out dated Names first to the new Table, Re Mapped Setting names Size=" + ListUtil.size(remapped_settings) + " All Old Settings Size=" + old_pairs.size());

               for(SettingReMappedItem item : remapped_settings) {
                   for(String oldName : item.oldNames) {
                       if(old_pairs.containsKey(oldName)) {
                           List<SettingReMappedItem.Setting_legacy> chunk = old_pairs.get(oldName);
                           if(!ListUtil.isValid(chunk, 1)) {
                               Log.d(TAG, "Empty or Null Chunk Warning! Skipping Old Settings Chunk under the name: " + oldName + " Size=" + ListUtil.size(chunk));
                               continue;
                           }

                           for(SettingReMappedItem.Setting_legacy old : chunk) {
                               int userId = UserIdentityUtils.getUserId(old.user);

                               ContentValues cv = new ContentValues();
                               cv.put(UserIdentityIO.FIELD_USER, userId);           //Use USER ID NOT (UID) as First Key
                               cv.put(UserIdentityIO.FIELD_CATEGORY, old.category); //Use Package Name as Category
                               cv.put(SettingPacket.FIELD_NAME, item.name);         //Use the new Name
                               cv.put(SettingPacket.FIELD_VALUE, old.value);        //Use Value

                               String data = "Old ID=" + old.user + " New ID=" + userId + " Category=" + old.category + " Old Name=" + old.name + " New Name=" + item.name + " Value=" + old.value;
                               if(DebugUtil.isDebug())
                                   Log.d(TAG, "Inserting Re Mapped Setting into new Settings Table, " + data);

                               if(!database.insert(SettingPacket.TABLE_NAME, cv)) {
                                   Log.e(TAG, "Failed to Insert Item into new Settings Database! " + data + " [Setting Re Map Failed]");
                                   continue;
                               }

                               if(DebugUtil.isDebug())
                                   Log.d(TAG, "Successfully Inserted / Re Mapped setting: " + data);
                           }

                           //clear old ensure it will not be re-inserted
                           chunk.clear();
                       }
                   }
               }

               if(DebugUtil.isDebug())
                   Log.d(TAG, "Pushing the Left over Blocks to the new Settings Table! Finished pushing Out Dated Names...");

               for(Map.Entry<String, List<SettingReMappedItem.Setting_legacy>> entry : old_pairs.entrySet()) {
                   String name = entry.getKey();
                   List<SettingReMappedItem.Setting_legacy> block = entry.getValue();
                   if(!ListUtil.isValid(block, 1)) {
                       Log.w(TAG, "Skipping Settings block to push to new Table, its empty aka it has been handled. Most likely a old name Block. Name=" + name);
                       continue;
                   }

                   if(DebugUtil.isDebug())
                       Log.d(TAG, "Pushing settings block to the new Settings Table, Size=" + ListUtil.size(block) + " Name=" + name);

                   for(SettingReMappedItem.Setting_legacy setting : block) {
                       int userId = UserIdentityUtils.getUserId(setting.user);

                       ContentValues cv = new ContentValues();
                       cv.put(UserIdentityIO.FIELD_USER, userId);
                       cv.put(UserIdentityIO.FIELD_CATEGORY, setting.category);
                       cv.put(SettingPacket.FIELD_NAME, setting.name);
                       cv.put(SettingPacket.FIELD_VALUE, setting.value);

                       String data = "ID=" + userId + " Category=" + setting.category + " Name=" + setting.name + " Value=" + setting.value;
                       if(DebugUtil.isDebug())
                           Log.d(TAG, "Inserting Re Mapped Setting into new Settings Table, " + data);

                       if(!database.insert(SettingPacket.TABLE_NAME, cv)) {
                           Log.e(TAG, "Failed to Insert Item into new Settings Database! " + data + " [Setting Re Map Failed]");
                           continue;
                       }

                       if(DebugUtil.isDebug())
                           Log.d(TAG, "Successfully Inserted / Re Mapped setting: " + data);
                   }
               }

               if(DebugUtil.isDebug())
                   Log.d(TAG, "Finished Pushing old Settings to the new Settings Table, All Transformed / ReMapped! Old Settings Size=" + old_pairs.size() + " New Table Size=" + database.tableEntries(SettingPacket.TABLE_NAME));

               database.dropTable(TABLE_SETTINGS_OLD);
               if(DebugUtil.isDebug())
                   Log.d(TAG, "Dropped old Settings Table [" + TABLE_SETTINGS_OLD + "] Count Check=" + database.tableEntries(TABLE_SETTINGS_OLD));

               //Do other tables while we are at it
               //If setting table old is empty but exists then still drop old tables ?
           }

           database.setTransactionSuccessful();
           return true;
        }catch (Exception e) {
            Log.e(TAG, "Error Ensuring Settings has been Transformed! Error=" + e + " Database=" + Str.toStringOrNull(database) + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            return false;
        } finally {
            database.endTransaction(true, false);
        }
    }

    public static boolean ensureDefaultsReMapped(Context context, SQLDatabase database) {
        initReMapCache(context);
        try {
            if(!DatabaseHelpEx.prepareDatabase(database, SettingInfoPacket.TABLE_INFO)) {
                Log.e(TAG, "Failed to Prepare Table! [" + SettingInfoPacket.TABLE_NAME + "] Can not Start Re Mapping of Defaults!");
                return false;
            }

            //Read Lock ?
            if(!database.beginTransaction(true)) {
                Log.e(TAG, "Failed to being Database Transaction! Skipping Re Mapper for Default Settings!");
                return false;
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Ensuring Table [" + SettingInfoPacket.TABLE_NAME + "] Has been updated and initialized and has their Names Resolved!");


            Pair<Collection<SettingInfoPacket>, Collection<SettingInfoPacket>> cleaned = ensureInfosIfUpdated(
                    DatabaseHelpEx.getFromDatabase(database, SettingInfoPacket.TABLE_NAME, SettingInfoPacket.class, false),
                    JsonHelperEx.findJsonElementsFromAssets(XUtil.getApk(context), JSON_SETTINGS_DEFAULT, true, SettingInfoPacket.class));

            Collection<SettingInfoPacket> modified = cleaned.first;
            Collection<SettingInfoPacket> same = cleaned.second;
            if(DebugUtil.isDebug())
                Log.d(TAG, "Total Number of Default Setting Info Objects Modified=" + ListUtil.size(modified) + " Same Size=" + ListUtil.size(same));

            if(!modified.isEmpty()) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Seems the Default Setting Info Objects were Modified, {" + ListUtil.size(modified) + "} Now will be Dropping the old Table and pushing the new Elements!");

                database.dropTable(SettingInfoPacket.TABLE_NAME);
                database.createTable(SettingInfoPacket.TABLE_INFO);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Dropped old Settings Default Table! Count after=" + database.tableEntries(SettingInfoPacket.TABLE_NAME));

                for(SettingInfoPacket info : ListUtil.combine(modified, same)) {
                    ContentValues cv = new ContentValues();
                    info.populateContentValues(cv);
                    if(!database.insert(SettingInfoPacket.TABLE_NAME, cv))
                        Log.e(TAG, "Failed to Insert Default Setting into Table! Setting Info=" + Str.toStringOrNull(info) + " CV=" + Str.toStringOrNull(cv));

                }
            }


            database.setTransactionSuccessful();
            return true;
        }catch (Exception e) {
            Log.e(TAG, "Error Ensuring Default Settings has been Transformed! Error=" + e + " Database=" + Str.toStringOrNull(database) + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            return false;
        }
        finally {
            database.endTransaction(true, false);
        }
    }


    public static LinkedHashMap<String, List<SettingReMappedItem.Setting_legacy>> toNamedSettingsMap(List<SettingReMappedItem.Setting_legacy> settings) {
        LinkedHashMap<String, List<SettingReMappedItem.Setting_legacy>> pairs = new LinkedHashMap<>();
        for(SettingReMappedItem.Setting_legacy setting : settings) {
            List<SettingReMappedItem.Setting_legacy> chunk = pairs.get(setting.name);
            if(chunk == null) {
                chunk = new ArrayList<>();
                pairs.put(setting.name, chunk);
            }

            chunk.add(setting);
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Transformed List<Setting> into a Map, Key is the Setting Name, Value is the Chunk (settings under that name) List Size=" + ListUtil.size(settings) + " Map Size=" + pairs.size());


        return pairs;
    }

    public static List<SettingReMappedItem.Setting_legacy> getOldSettings(SQLDatabase database, boolean ensureNonDuplicates) {
        Collection<SettingReMappedItem.Setting_legacy> settings_raw = DatabaseHelpEx.getFromDatabase(database, TABLE_SETTINGS_OLD, SettingReMappedItem.Setting_legacy.class, true);
        if(ensureNonDuplicates && ListUtil.isValid(settings_raw)) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Organizing Old Settings to Ensure no Duplicates with Ghost UIDs! Count=" + ListUtil.size(settings_raw));

           /*
                Organize the Settings, as if the user has re-installed the Target App the UID changes
                So there may be Entries in the Table from the last old UID
                So we the "newest" will replace the "last" value used
            */

            //<CATEGORY,<NAME,OBJ>>
            LinkedHashMap<String, LinkedHashMap<String, SettingReMappedItem.Setting_legacy>> organized = new LinkedHashMap<>();
            for(SettingReMappedItem.Setting_legacy legacy_setting : settings_raw) {
                if(!TextUtils.isEmpty(legacy_setting.category) && !TextUtils.isEmpty(legacy_setting.name)) {
                    LinkedHashMap<String, SettingReMappedItem.Setting_legacy> pkg_settings = organized.get(legacy_setting.category);
                    if(pkg_settings == null) {
                        pkg_settings = new LinkedHashMap<>();
                        organized.put(legacy_setting.category, pkg_settings);
                        if(DebugUtil.isDebug())
                            Log.d(TAG, "Created Category Chunk for Re Mapper, Category=" + legacy_setting.category);
                    }

                    //Overwrite the last setting if any
                    pkg_settings.put(legacy_setting.name, legacy_setting);
                }
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Finished Organizing old Settings to ensure no Ghost UIDs / Duplicates! Count=" + organized.size());

            List<SettingReMappedItem.Setting_legacy> final_list = new ArrayList<>();
            for(LinkedHashMap.Entry<String, LinkedHashMap<String, SettingReMappedItem.Setting_legacy>> entry : organized.entrySet()) {
                String category = entry.getKey();
                Map<String, SettingReMappedItem.Setting_legacy> settings = entry.getValue();
                if(settings == null || settings.isEmpty() || category == null)
                    continue;   //Weird

                if(DebugUtil.isDebug())
                    Log.d(TAG, "Parsed old Settings Category: " + category + " Total Settings final after ensuring no Duplicated=" + settings.size());

                final_list.addAll(settings.values());
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Finished organizing Settings to ensure no Ghost UIDs / Duplicates! Raw Settings Count=" + ListUtil.size(settings_raw) + " Organized Settings Count=" + ListUtil.size(final_list));

            return final_list;
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Returning Raw Settings from old Table, Size=" + ListUtil.size(settings_raw));

        return ListUtil.copyToArrayList(settings_raw);
    }


    public static Pair<Collection<SettingInfoPacket>, Collection<SettingInfoPacket>> ensureInfosIfUpdated(
            Collection<SettingInfoPacket> fromDatabase,
            Collection<SettingInfoPacket> fromJson) {

        if(DebugUtil.isDebug())
            Log.d(TAG, "Making Sure that the Default Settings in Database are aligned to the JSON Version and are Name Resolved! Database Count=" + ListUtil.size(fromDatabase) + " JSON Count=" + ListUtil.size(fromJson));

        List<SettingInfoPacket> modified = new ArrayList<>();
        List<SettingInfoPacket> same = new ArrayList<>();

        Map<String, SettingInfoPacket> mapped = new HashMap<>(fromJson.size());
        for(SettingInfoPacket j : fromJson)
            mapped.put(j.name, j);

        if(!ListUtil.isValid(fromDatabase)) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Default Settings from Database is Empty or Null returning JSON Elements to Parse to Database! Size=" + ListUtil.size(fromJson));

            modified.addAll(fromJson);
            return Pair.create(modified, same);
        }

        for(SettingInfoPacket d : fromDatabase) {
            SettingInfoPacket j = mapped.get(d.name);
            boolean wasModified = d.ensureNamed(RE_MAPPED_CACHE);

            if(j != null && !Str.areEqualsAnyIgnoreCase(d.description, j.description)) {
                d.description = j.description;
                wasModified = true;
            }

            if(wasModified) modified.add(d);
            else same.add(d);
            if(wasModified && DebugUtil.isDebug())
                Log.d(TAG, "Setting Default Info Element was modified, Info=" + Str.toStringOrNull(d));
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Finished Parsing Default settings for Changes, Total Modified=" + ListUtil.size(modified) + " Non Modified=" + ListUtil.size(same));

        return Pair.create(modified, same);
    }

    /*public static <T extends INameResolver> int ensureAllElementsResolved(Context context, Collection<T> objects) {
        initReMapCache(context);
        int renamed = 0;
        if(ListUtil.isValid(objects)) {
            for(INameResolver o : objects) {
                if(o.ensureNamed(RE_MAPPED_CACHE)) {
                    renamed++;
                }
            }
        }

        return renamed;
    }*/

    public static boolean ensureGroups(Context context, SQLDatabase database) {
        initReMapCache(context);
        try {
            if(!DatabaseHelpEx.prepareDatabase(database, GroupPacket.TABLE_INFO)) {
                logE( "Failed to Prepare Table! [" + GroupPacket.TABLE_NAME + "] Can not Start Re Mapping!");
                return false;
            }

            //Read Lock ?
            if(!database.beginTransaction(true)) {
                logE("Failed to being Database Transaction! Skipping Re Mapper for Group! Table:" + GroupPacket.TABLE_NAME);
                return false;
            }

            int old_count = database.tableEntries(TABLE_GROUP_OLD);
            if(old_count > 0) {
                List<GroupLegacy> compressed = compressGhostUid(database, TABLE_GROUP_OLD, GroupLegacy.class);
                //drop old table
                if(DebugUtil.isDebug())
                    logE("Got groups Compressed, Old Count=" + old_count + " Compressed Count=" + ListUtil.size(compressed) + " Now dropping old Table!");

                for(GroupLegacy group : compressed) {
                    if(!database.insert(GroupPacket.TABLE_NAME, group.toContentValues()))
                        logD("Failed to Insert New Group Packet! Group=" + group.name);
                }

                if(DebugUtil.isDebug())
                    logD("Groups Sent Count=" + ListUtil.size(compressed) + " Now dropping the old table!");

            }

            //This shit is not finding the fucking table
            if(database.hasTable(TABLE_GROUP_OLD)) {
                logI("Dropping the old Groups Table! Item Count=" +  old_count);
                if(!database.dropTable(TABLE_GROUP_OLD))
                    logE("Failed to Drop Table [" + TABLE_GROUP_OLD + "] Not sure why...");
            }

            database.setTransactionSuccessful();
            return true;
        }catch (Exception e) {
            logE("Failed to ensure groups are updated and resolved! Error=" + e + " Db=" + Str.toStringOrNull(database) + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            return false;
        }
        finally {
            database.endTransaction(true, false);
        }
    }

    public static boolean ensureAssignments(Context context, SQLDatabase database) {
        initReMapCache(context);
        try {
            if(!DatabaseHelpEx.prepareDatabase(database, AssignmentPacket.TABLE_INFO)) {
                Log.e(TAG, "Failed to Prepare Table! [" + AssignmentPacket.TABLE_NAME + "] Can not Start Re Mapping!");
                return false;
            }

            //Read Lock ?
            if(!database.beginTransaction(true)) {
                Log.e(TAG, "Failed to being Database Transaction! Skipping Re Mapper for Assignments! Table:" + AssignmentPacket.TABLE_NAME);
                return false;
            }

            int old_count = database.tableEntries(TABLE_ASSIGNMENTS_OLD);
            if(old_count > 0) {
                List<AssignmentLegacy> compressed = compressGhostUid(database, TABLE_ASSIGNMENTS_OLD, AssignmentLegacy.class);
                //drop old table
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Got assignments Compressed, Old Count=" + old_count + " Compressed Count=" + ListUtil.size(compressed) + " Now dropping old Table!");

                for(AssignmentLegacy assignment : compressed) {
                    ContentValues cv = new ContentValues();
                    assignment.populateContentValues(cv);
                    if(!database.insert(AssignmentPacket.TABLE_NAME, cv))
                        Log.d(TAG, "Failed to Insert New Assignment Packet! Assignment=" + Str.toStringOrNull(assignment) + " CV=" + Str.toStringOrNull(cv));

                }

                if(DebugUtil.isDebug())
                    Log.d(TAG, "Assignments Sent Count=" + ListUtil.size(compressed) + " Now dropping the old table!");

            }

            if(database.hasTable(TABLE_ASSIGNMENTS_OLD)) {
                Log.i(TAG, "Dropping the old Assignments Table! Item Count=" +  old_count);
                if(!database.dropTable(TABLE_ASSIGNMENTS_OLD)) {
                    Log.e(TAG, "Failed to Drop Table [" + TABLE_ASSIGNMENTS_OLD + "] Not sure why...");
                }
            }

            database.setTransactionSuccessful();
            return true;
        }catch (Exception e) {
            Log.e(TAG, "Failed to ensure assignments are updated and resolved! Error=" + e + " Db=" + Str.toStringOrNull(database) + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            return false;
        }
        finally {
            database.endTransaction(true, false);
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
