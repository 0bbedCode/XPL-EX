package eu.faircode.xlua.x.xlua.database.wrappers;

import android.content.Context;

import de.robv.android.xposed.XposedBridge;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.database.DatabaseUtils;
import eu.faircode.xlua.x.xlua.database.IDatabaseManager;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;

public class XMocDatabaseManager implements IDatabaseManager  {
    public static XMocDatabaseManager create() { return new XMocDatabaseManager(); }

    private final Object lock = new Object();
    private SQLDatabase db;
    private boolean init = false;

    public XMocDatabaseManager() { }

    @Override
    public SQLDatabase getDatabase(Context context) {
        initializeDatabase(context, true);
        return db;
    }

    @Override
    public boolean initializeDatabase(Context context, boolean checkIsReady) {
        synchronized (lock) {
            if(checkIsReady && db != null) {
                if(!DatabaseUtils.isReady(db))
                    reset(true);
            }

            if(db == null) {
                db = new SQLDatabase(SQLDatabase.DATABASE_MOCK, context, true);
                XposedBridge.log(Str.fm("Opened Database [2], Database=%s", Str.noNL(db)));
                reset(false);
                if(!db.isOpen(true))
                    return false;
            }

            if(!init && db.isOpen(true)) {
                //ensure all the tables are created and ready!
                init = true;
            }
        }

        return init;
    }

    @Override
    public void reset(boolean setDatabaseNull) {
        XposedBridge.log("Resetting Database (xm), Set Database to null: " + String.valueOf(setDatabaseNull));
        if(setDatabaseNull && db != null) {
            if(db.isOpen())
                db.close();

            db = null;
        }

        init = false;
    }
}
