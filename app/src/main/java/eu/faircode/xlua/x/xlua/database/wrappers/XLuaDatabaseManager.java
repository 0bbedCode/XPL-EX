package eu.faircode.xlua.x.xlua.database.wrappers;

import android.content.Context;
import android.util.Log;

import de.robv.android.xposed.XposedBridge;
import eu.faircode.xlua.UberCore888;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.database.DatabaseUtils;
import eu.faircode.xlua.x.xlua.database.IDatabaseManager;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.database.updaters.DatabaseUpdater;
import eu.faircode.xlua.x.xlua.settings.SettingReMappedItem;

public class XLuaDatabaseManager implements IDatabaseManager {
    public static XLuaDatabaseManager create() { return new XLuaDatabaseManager(); }

    private static final String TAG = LibUtil.generateTag(XLuaDatabaseManager.class);

    private final Object lock = new Object();
    private SQLDatabase db;
    private boolean init = false;

    public XLuaDatabaseManager() {  }

    @Override
    public SQLDatabase getDatabase(Context context) {
        initializeDatabase(context, true);
        return db;
    }

    @Override
    public boolean initializeDatabase(Context context, boolean checkIsReady) {
        synchronized (lock) {
            return internalInitializeDatabase(context, checkIsReady);
        }
    }

    private boolean internalInitializeDatabase(Context context, boolean checkIsReady) {
        if(checkIsReady && db != null) {
            if(!DatabaseUtils.isReady(db))
                reset(true);
        }

        try {
            if(db == null) {
                db = new SQLDatabase(SQLDatabase.DATABASE_X_LUA, context, true);
                XposedBridge.log(Str.fm("Opened Database, Database=%s", Str.noNL(db)));
                reset(false);
                if(!db.isOpen(true))
                    return false;
            }

            if(!init && db.isOpen(true)) {
                init = true;
                XLuaDatabaseHelp.ensureIsUpdated_legacy(db);
                UberCore888.loadHooksEx(context, db);
                DatabaseUpdater.ensureUpdated(context, db);
                //GlobalDatabaseResolver.initEnsureFunctions(context, db);
                //DatabaseUpdater<SettingReMappedItem.Setting_legacy>
            }
        }catch (Throwable e) {
            Log.e(TAG, Str.fm("Error Init for XLua Database! Database=%s  Error=%s", db, e));
            return false;
        }

        return init;
    }

    @Override
    public void reset(boolean setDatabaseNull) {
        XposedBridge.log("Resetting Database (xl), Set Database to null: " + String.valueOf(setDatabaseNull));
        if(setDatabaseNull && db != null) {
            if(db.isOpen())
                db.close();

            db = null;
        }

        init = false;
    }
}
