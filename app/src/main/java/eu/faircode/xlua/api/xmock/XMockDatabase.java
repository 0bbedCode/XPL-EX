package eu.faircode.xlua.api.xmock;

import android.content.Context;

import androidx.annotation.NonNull;

import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.UberCore888;
import eu.faircode.xlua.api.xmock.database.MockConfigManager;
import eu.faircode.xlua.api.xmock.database.MockPropManager;
import eu.faircode.xlua.api.xstandard.interfaces.IInitDatabase;
import eu.faircode.xlua.api.xmock.database.MockUserAgentManager;
import eu.faircode.xlua.api.xmock.database.MockCpuManager;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.utilities.CollectionUtil;

public class XMockDatabase implements IInitDatabase {
    private XDatabaseOld db;
    private boolean init = false;
    private boolean setPerms = true;

    private boolean check_1 = false;
    private boolean check_2 = false;
    private boolean check_3 = false;
    private boolean check_4 = false;
    private boolean check_5 = false;

    private final Object lock = new Object();

    public static XMockDatabase createEmpty() { return new XMockDatabase(); }

    public XMockDatabase() {  }
    public XMockDatabase(Context context) {
        this(context, true);
    }
    public XMockDatabase(Context context, boolean setPerms) { this.db = new XDatabaseOld("mock", context, setPerms); this.setPerms = setPerms; }

    @Override
    public XDatabaseOld getDatabase(Context context) {
        initDatabase(context, true);
        return db;
    }

    @Override
    public boolean initDatabase(Context context, boolean checkIsReady) {
        synchronized (lock) {
            if(checkIsReady && db != null) {
                if(!XDatabaseOld.isReady(db))
                    reset(true);
            }

            if(db == null) {
                db = new XDatabaseOld(UberCore888.DB_NAME_MOCK, context, setPerms);
                XLog.i(true, "Created XMOCK DB: " + db);
                reset(false);
                if(!db.isOpen(true))
                    return false;
            }

            if(!init) {
                if(!check_1) check_1 = MockConfigManager.forceDatabaseCheck(context, db);
                if(!check_2) check_2 = MockCpuManager.forceDatabaseCheck(context, db);
                if(!check_3) check_3 = CollectionUtil.isValid(MockPropManager.forceCheckMapsDatabase(context, db));
                if(!check_4) check_4 = MockPropManager.ensurePropSettingsDatabase(context, db);
                if(!check_5) check_5 = MockUserAgentManager.forceDatabaseCheck(context, db);
                XLog.i(true, new StringBuilder()
                        .append("\t[1] Config Table Check=").append(check_1).append("\n")
                        .append("\t[2] Cpu Table Check=").append(check_2).append("\n")
                        .append("\t[3] Prop Maps Table Check=").append(check_3).append("\n")
                        .append("\t[4] Prop Settings Table Check=").append(check_4).append("\n")
                        .append("\t[5] User Agent Table Check=").append(check_5)
                        .toString());

                init = check_1 && check_2 && check_3 && check_4 && check_5;
            }

            return init;
        }
    }

    @Override
    public void reset(boolean setDatabaseNull) {
        if(setDatabaseNull) {
            db.close();
            db = null;
        }

        init = false;
        check_1 = false;
        check_2 = false;
        check_3 = false;
        check_4 = false;
        check_5 = false;
    }

    @NonNull
    @Override
    public String toString() {
        if(db != null) return db.toString();
        return "null";
    }
}
