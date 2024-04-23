package eu.faircode.xlua.api.xlua;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XDatabase;
import eu.faircode.xlua.XGlobals;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.api.hook.LuaHookUpdate;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.xmock.database.LuaSettingsManager;
import eu.faircode.xlua.api.xstandard.JsonHelper;
import eu.faircode.xlua.api.xstandard.database.DatabaseHelp;
import eu.faircode.xlua.api.xstandard.database.SqlQuerySnake;
import eu.faircode.xlua.api.xstandard.interfaces.IInitDatabase;
import eu.faircode.xlua.utilities.DatabasePathUtil;
import eu.faircode.xlua.utilities.StringUtil;

public class XLuaDatabase implements IInitDatabase {
    private XDatabase db = null;
    private boolean init = false;
    private boolean check_1 = false;
    private boolean setPerms = true;

    private final Object lock = new Object();

    public static XLuaDatabase createEmpty() { return new XLuaDatabase(); }

    public XLuaDatabase() {  }
    public XLuaDatabase(Context context) {
        this(context, true);
    }
    public XLuaDatabase(Context context, boolean setPerms) { this.db = new XDatabase("xlua", context, setPerms); this.setPerms = setPerms; }

    @Override
    public XDatabase getDatabase(Context context) {
        initDatabase(context, true);
        return db;
    }

    @Override
    public boolean initDatabase(Context context, boolean checkIsReady) {
        synchronized (lock) { return internalInitDatabase(context, checkIsReady); }
    }

    private boolean internalInitDatabase(Context context, boolean checkIsReady) {
        if(checkIsReady && db != null) {
            if(!XDatabase.isReady(db))
                reset(true);
        }

        if(db == null) {
            db = new XDatabase(XGlobals.DB_NAME_LUA, context, setPerms);
            DatabasePathUtil.log("Created XLUA DB =>" + db, false);
            reset(false);
            if(!db.isOpen(true))
                return false;
        }

        if(!init) {
            if(!check_1) check_1 = LuaSettingsManager.forceDatabaseCheck(context, db) && LuaSettingsManager.compareJsonToDatabase(context, db);
            DatabasePathUtil.log("XLUA init settings=" + check_1, false);
            init = check_1;
            //Make a centralized core for logs

            SQLiteDatabase _db = db.getDatabase();
            db.writeLock();
            try {
                if (_db.needUpgrade(1)) {
                    DatabasePathUtil.log("Database upgrade version 1", false);
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

                if (_db.needUpgrade(2)) {
                    DatabasePathUtil.log("Database upgrade version 2", false);
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
                    DatabasePathUtil.log("Database upgrade version 3", false);
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

                if (_db.needUpgrade(4)) {
                    DatabasePathUtil.log("Database upgrade version 4", false);
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
                                tmp.put(hook.getId(), hook);
                            }
                        } finally {
                            if (cursor != null)
                                cursor.close();
                        }

                        DatabasePathUtil.log("Converting definitions=" + tmp.size(), false);

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

                if (_db.needUpgrade(5)) {
                    DatabasePathUtil.log("Database upgrade version 5", false);
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

                if(DebugUtil.isDebug())
                    DatabasePathUtil.log("Renaming XLUA Hooks", false);

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

                if(DebugUtil.isDebug())
                    DatabasePathUtil.log("Database version=" + _db.getVersion(), false);

                // Reset usage data
                ContentValues cv = new ContentValues();
                cv.put("installed", -1);
                cv.putNull("exception");
                long rows = _db.update("assignment", cv, null, null);
                if(DebugUtil.isDebug())
                    DatabasePathUtil.log("Reset assigned hook data count=" + rows, false);

            } catch (Throwable ex) {
                DatabasePathUtil.log("DB EXCEPTION=" + ex + "\n" + Log.getStackTraceString(ex), true);
            } finally {
                db.writeUnlock();// first lock is the bug
            }

            try {
                //Make sure I fiinsihed
                XGlobals.loadHooks(context, db);
                Map<String, XLuaHook> hooks = XGlobals.getAllHooks(context, db);
                for (Map.Entry<String, XLuaHook> hSet : hooks.entrySet()) {
                    XLuaHook h = hSet.getValue();
                    String id = hSet.getKey();
                    String a = h.getAuthor().toLowerCase();
                    if(a.contains("obc") || a.contains("obbed")) {
                        XLuaHook hookDb = SqlQuerySnake
                                .create(db, "hook")
                                .whereColumn("id", id)
                                .queryGetFirstAs(XLuaHook.class, true);
                        if(hookDb != null) {
                            if(!hookDb.toJSON().equals(h.toJSON())) {
                                SqlQuerySnake snake = SqlQuerySnake
                                        .create(db, "hook")
                                        .whereColumn("id", id);
                                DatabaseHelp.updateItem(snake, h);
                            }
                        }
                    }
                }

                /*Collection<LuaHookUpdate> updates = JsonHelper.findJsonElementsFromAssets(XUtil.getApk(context), "updates.json", true, LuaHookUpdate.class);
                for(LuaHookUpdate lUpdate : updates) {
                    if(StringUtil.isValidAndNotWhitespaces(lUpdate.getOldId())) {
                        if(StringUtil.isValidAndNotWhitespaces(lUpdate.getNewId())) {
                            //first check for old exists
                            //if so then replace with new
                        }else {

                        }
                    }


                    //if new is null BUT old is not NULL then
                    //that means the actual script stuff
                }*/

                //for(Map.Entry<String, XLuaHook> hook)
            }catch (Throwable ex) {
                DatabasePathUtil.log("Failed to load in hooks: " + ex, true);
            }
        }

        return init;
    }

    private static void renameHookId(SQLiteDatabase _db, String oldId, String newId) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("hook", newId);
            long rows = _db.update("assignment", cv, "hook = ?", new String[]{oldId});
            DatabasePathUtil.log("Renamed hook " + oldId + " into " + newId + " rows=" + rows, false);
        } catch (Throwable ex) {
            DatabasePathUtil.log("Renamed hook " + oldId + " into " + newId + " ex=" + ex.getMessage(), true);
        }
    }

    //assignment
    //pkg, uid, hook (ID) not Name
    //
    //hook (ID) not name
    //
    //this.collection + "." + this.name;

    public static void updateInternalHooks(SQLiteDatabase _db, String oldId, String newId) {

    }

    @Override
    public void reset(boolean setDatabaseNull) {
        if(setDatabaseNull) {
            db.close();
            db = null;
        }

        init = false;
        check_1 = false;
    }

    @NonNull
    @Override
    public String toString() {
        if(db != null) return db.toString();
        return "null";
    }
}
