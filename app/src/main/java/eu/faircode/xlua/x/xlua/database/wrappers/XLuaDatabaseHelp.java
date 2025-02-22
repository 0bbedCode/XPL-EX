package eu.faircode.xlua.x.xlua.database.wrappers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XposedBridge;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;

public class XLuaDatabaseHelp {
    private static final String TAG = "XLua.XLuaDatabaseHelp";

    public static void ensureIsUpdated_legacy(SQLDatabase database) {
        if(!database.isOpen(true))
            return;

        SQLiteDatabase _db = database.getDatabase();
        database.writeLock();
        try {
            if (_db.needUpgrade(1)) {
                if(database.hasTable("assignment")) {
                    _db.beginTransaction();
                    try {
                        _db.execSQL("CREATE TABLE assignment (package TEXT NOT NULL, uid INTEGER NOT NULL, hook TEXT NOT NULL, installed INTEGER, used INTEGER, restricted INTEGER, exception TEXT)");
                        _db.execSQL("CREATE UNIQUE INDEX idx_assignment ON assignment(package, uid, hook)");

                        _db.execSQL("CREATE TABLE setting (user INTEGER, category TEXT NOT NULL, name TEXT NOT NULL, value TEXT)");
                        _db.execSQL("CREATE UNIQUE INDEX idx_setting ON setting(user, category, name)");

                        _db.setVersion(1);
                        _db.setTransactionSuccessful();
                    } finally {
                        _db.endTransaction();
                    }
                }
            }

            if (_db.needUpgrade(2)) {
                _db.beginTransaction();
                try {
                    _db.execSQL("CREATE TABLE hook (id TEXT NOT NULL, definition TEXT NOT NULL)");
                    _db.execSQL("CREATE UNIQUE INDEX idx_hook ON hook(id, definition)");

                    _db.setVersion(2);
                    _db.setTransactionSuccessful();
                } finally {
                    _db.endTransaction();
                }
            }

            if (_db.needUpgrade(3)) {
                if(database.hasTable("assignment")) {
                    _db.beginTransaction();
                    try {
                        _db.execSQL("ALTER TABLE assignment ADD COLUMN old TEXT");
                        _db.execSQL("ALTER TABLE assignment ADD COLUMN new TEXT");
                        _db.execSQL("CREATE INDEX idx_assignment_used ON assignment(used)");

                        _db.setVersion(3);
                        _db.setTransactionSuccessful();
                    } finally {
                        _db.endTransaction();
                    }
                }
            }

            if (_db.needUpgrade(4)) {
                _db.beginTransaction();
                try {
                    Map<String, XLuaHook> tmp = new HashMap<>();
                    Cursor cursor = null;
                    try {
                        cursor = _db.query("hook", null,
                                null, null,
                                null, null, null);
                        int colDefinition = cursor.getColumnIndex("definition");
                        while (cursor.moveToNext()) {
                            String definition = cursor.getString(colDefinition);
                            XLuaHook hook = new XLuaHook();
                            hook.fromJSONObject(new JSONObject(definition));
                            tmp.put(hook.getObjectId(), hook);
                        }
                    } finally {
                        if (cursor != null)
                            cursor.close();
                    }

                    _db.execSQL("DROP INDEX idx_hook");
                    _db.execSQL("DELETE FROM hook");
                    _db.execSQL("CREATE UNIQUE INDEX idx_hook ON hook(id)");

                    for (String id : tmp.keySet()) {
                        ContentValues cv = new ContentValues();
                        cv.put("id", id);
                        cv.put("definition", tmp.get(id).toJSON());
                        long rows = _db.insertWithOnConflict("hook", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
                        if (rows < 0)
                            throw new Throwable("Error inserting hook");
                    }


                    _db.setVersion(4);
                    _db.setTransactionSuccessful();
                } finally {
                    _db.endTransaction();
                }
            }

            if(database.hasTable("`group`")) {
                if (_db.needUpgrade(5)) {
                    _db.beginTransaction();
                    try {
                        _db.execSQL("CREATE TABLE `group` (package TEXT NOT NULL, uid INTEGER NOT NULL, name TEXT NOT NULL, used INTEGER)");
                        _db.execSQL("CREATE UNIQUE INDEX idx_group ON `group`(package, uid, name)");

                        _db.setVersion(5);
                        _db.setTransactionSuccessful();
                    } finally {
                        _db.endTransaction();
                    }
                }
            }

            //if(DebugUtil.isDebug())
            //    DatabasePathUtil.log("Renaming XLUA Hooks", false);

            //deleteHook(_db, "Privacy.ContentResolver/query1");
            //deleteHook(_db, "Privacy.ContentResolver/query16");
            //deleteHook(_db, "Privacy.ContentResolver/query26");

            renameHookId(_db, "TelephonyManager/getDeviceId", "TelephonyManager.getDeviceId");
            renameHookId(_db, "TelephonyManager/getDeviceId/slot", "TelephonyManager.getDeviceId/slot");
            renameHookId(_db, "TelephonyManager/getGroupIdLevel1", "TelephonyManager.getGroupIdLevel1");
            renameHookId(_db, "TelephonyManager/getImei", "TelephonyManager.getImei");
            renameHookId(_db, "TelephonyManager/getImei/slot", "TelephonyManager.getImei/slot");
            renameHookId(_db, "TelephonyManager/getLine1Number", "TelephonyManager.getLine1Number");
            renameHookId(_db, "TelephonyManager/getMeid", "TelephonyManager.getMeid");
            renameHookId(_db, "TelephonyManager/getMeid/slot", "TelephonyManager.getMeid/slot");
            renameHookId(_db, "TelephonyManager/getNetworkSpecifier", "TelephonyManager.getNetworkSpecifier");
            renameHookId(_db, "TelephonyManager/getSimSerialNumber", "TelephonyManager.getSimSerialNumber");
            renameHookId(_db, "TelephonyManager/getSubscriberId", "TelephonyManager.getSubscriberId");
            renameHookId(_db, "TelephonyManager/getVoiceMailAlphaTag", "TelephonyManager.getVoiceMailAlphaTag");
            renameHookId(_db, "TelephonyManager/getVoiceMailNumber", "TelephonyManager.getVoiceMailNumber");
            renameHookId(_db, "Settings.Secure.getString", "Settings.Secure.getString/android_id");
            renameHookId(_db, "SystemProperties.get", "SystemProperties.get/serial");
            renameHookId(_db, "SystemProperties.get/default", "SystemProperties.get.default/serial");

            if(database.hasTable("assignment")) {
                // Reset usage data
                ContentValues cv = new ContentValues();
                cv.put("installed", -1);
                cv.putNull("exception");
                long rows = _db.update("assignment", cv, null, null);
            }

        }catch (Throwable e) {
            Log.e(TAG, Str.fm("Error with X-LUA Init Database old, Error:%s", e));
            XposedBridge.log(Str.fm("Error with X-LUA Init Database old, Error:%s", e));
        } finally {
            database.writeUnlock();
        }
    }

    private static void renameHookId(SQLiteDatabase _db, String oldId, String newId) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("hook", newId);
            long rows = _db.update("assignment", cv, "hook = ?", new String[]{oldId});
        } catch (Throwable ex) {
            Log.e(TAG, Str.fm("Error Renaming Hook ID, Old:%s  New%s  Error:%s", oldId, newId, ex));
            XposedBridge.log(Str.fm("Error Renaming Hook ID, Old:%s  New%s  Error:%s", oldId, newId, ex));
        }
    }
}
