package eu.faircode.xlua;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import eu.faircode.xlua.api.xstandard.database.SqlQuerySnake;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.utilities.DatabasePathUtil;
import eu.faircode.xlua.x.xlua.database.TableInfo;

public class XDatabaseOld {
    private static final String TAG = "XLua.Database.SqliteWrapper";

    private String path;
    private String name;
    private File dbFile;

    public ReentrantReadWriteLock dbLock = new ReentrantReadWriteLock(true);
    private SQLiteDatabase db = null;

    public XDatabaseOld(String dbname, Context context) {
        this(dbname, context, true);
    }
    public XDatabaseOld(String dbname, Context context, boolean setPerms) {
        if(!dbname.endsWith(".db"))
            dbname += ".db";

        DatabasePathUtil.log("Creating the Database=" + dbname, false);

        name = dbname;
        path = !DatabasePathUtil.ensureDirectoryChange(context) ?
                DatabasePathUtil.getOriginalDataLocationString(context) :
                DatabasePathUtil.getDatabaseDirectory(context).getAbsolutePath();

        dbFile = new File(path + File.separator + name);
        DatabasePathUtil.log("db created=" + dbFile.getAbsolutePath(), false);
        //XFileUtils.chown(dbFile.getAbsolutePath(), Process.SYSTEM_UID, Process.SYSTEM_UID);
        if(setPerms)
            setPermissions(dbFile);
    }

    public boolean open() {
        try {
            if(dbFile == null) {
                Log.e(TAG, "Failed to open, DB_FILE is null...");
                return  false;
            }

            if(db == null) {
                db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
                Log.i(TAG, "Database file=" + dbFile  + " open=" + db.isOpen());
            }

            return db.isOpen();
        }catch (Exception e) {
            Log.e(TAG, "Failed to open DB: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(String tableName) { return delete(tableName, null, null); }
    public boolean delete(String tableName, String selectionArgs, String[] compareValues) {
        try {
            long rows = db.delete(tableName, selectionArgs, compareValues);
            if(rows < 0) {
                Log.e(TAG, "Failed to delete I think ? Row count=" + rows);
                return false;
            }

            return true;
        }catch (Exception e) {
            Log.e(TAG, "Failed to Delete Item from Table [" + tableName + "] from DB [" + db + "] ");
            return false;
        }
    }

    public boolean update(String tableName, ContentValues values, SqlQuerySnake queryFilter) {
        boolean ass = false;
        try {
            long rows = db.update(tableName, values, queryFilter.getSelectionArgs(), queryFilter.getSelectionCompareValues());
            if(rows != 1){
                rows = db.updateWithOnConflict(tableName, values, queryFilter.getSelectionArgs(), queryFilter.getSelectionCompareValues(), SQLiteDatabase.CONFLICT_REPLACE);
                if(rows != 1) {
                    Log.e(TAG, " Failed to Update Data into Table:" + tableName + " stack=" + Log.getStackTraceString(new Throwable()));
                    return false;
                }
            }

            return true;
        }catch (Exception e) {
            Log.e(TAG, "Failed to Add / Update Row in Table=" + tableName + "\n" + e.getMessage() + " stack=" + Log.getStackTraceString(e));
            return false;
        }
    }

    public boolean insert(String tableName, ContentValues values)  {
        try {
            long rows = db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            if (rows < 0) {
                Log.e(TAG, "Failed to Insert Data into Table:" + tableName);
                return false;
            }

            return true;
        }catch (Exception e) {
            Log.e(TAG, "Failed to Add / Insert Row in Table=" + tableName + "\n" + e.getMessage());
            return false;
        }
    }

    public void writeLock() {
        dbLock.writeLock().lock();
    }
    public void writeUnlock() {
        dbLock.writeLock().unlock();
    }
    public void readLock() {
        dbLock.readLock().lock();
    }
    public void readUnlock() {
        dbLock.readLock().unlock();
    }

    public boolean beginTransaction() {
        return beginTransaction(false);
    }
    public boolean beginTransaction(boolean writeLock) {
        try {
            if(!isOpen(true))
                return false;

            if(writeLock) writeLock();

            db.beginTransaction();
            return true;
        }catch (Exception e) {
            if(writeLock) writeUnlock();
            Log.e(TAG, "Begin Transaction Error: " + e + "\n" + Log.getStackTraceString(e));
            return false;
        }
    }

    public void setTransactionSuccessful(){
        try {
            db.setTransactionSuccessful();
        }catch (Exception e) {
            Log.e(TAG, "Failed to set Transaction Successful\n" + e.getMessage());
        }
    }

    public boolean endTransaction() {
        return endTransaction(false, false);
    }

    public boolean endTransaction(boolean writeUnlock, boolean wasSuccessful) {
        try {
            if(!isOpen(false))
                return false;

            if(wasSuccessful) setTransactionSuccessful();
            db.endTransaction();
            if(writeUnlock) writeUnlock();
            return true;
        }catch (Exception e) {
            if(writeUnlock) writeUnlock();
            Log.e(TAG, "Failed to End Transaction: " + e.getMessage());
            return false;
        }
    }

    public boolean close() {
        try {
            if(dbFile == null)
                return  false;

            if(db != null) {
                db.close();
                Log.i(TAG, "Database file closed=" + dbFile);
            }

            return true;
        }catch (Exception e) {
            Log.e(TAG, "Failed to close DB: " + e.getMessage());
            return false;
        }
    }

    public boolean isOpen() {
        return isOpen(false);
    }

    public boolean exists() {
        try {
            return dbFile.exists();
        }catch (Exception e) {
            Log.e(TAG, "Failed to check if DB File exists assuming it does not... \n" + e.getMessage());
            return false;
        }
    }
    public boolean isOpen(boolean open_if_not) {
        if(db == null || !db.isOpen()) {
            if(open_if_not)
                return open();
            else
                return false;
        }

        return true;
    }

    public boolean createTable(TableInfo tableInfo) { return createTable(tableInfo.columns, tableInfo.name); }
    public boolean createTable(Map<String, String> columns, String name) {
        if(!isOpen(true))
            return false;

        if(name == null || name.equalsIgnoreCase(" ")) {
            Log.e(TAG, "[createTable] Not a valid Table Name");
            return false;
        }

        String qry = dynamicCreateQueryEx(columns, name);
        if(DebugUtil.isDebug())
            Log.i(TAG, "QUERY=" + qry);

        try {
            db.execSQL(qry);
            return true;
        }catch (Exception e) {
            Log.e(TAG, "Failed to create Table: props >> " + e.getMessage());
            return false;
        }
    }

    public boolean dropTable(TableInfo tableInfo) { return dropTable(tableInfo.name); }
    public boolean dropTable(String tableName) {
        if(!isOpen(true))
            return false;

        try{
            String query = "DROP TABLE IF EXISTS " + tableName;
            db.execSQL(query);
            return true;
        }catch (Exception e){
            Log.e(TAG, "Failed to drop Table: " + tableName);
            return false;
        }
    }

    public boolean tableIsEmpty(String tableName) { return tableEntries(tableName) < 1; }

    public int tableEntries(String tableName) {
        if(!isOpen(true))
            return -1;

        if(!hasTable(tableName))
            return 0;

        int count = 0;
        try {
            String query = "SELECT COUNT(*) FROM " + tableName;
            Cursor cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst())
                count = cursor.getInt(0);

            cursor.close();
        }catch (Exception e) {
            Log.e(TAG, "Failed to get Table Item Count.. " + e.getMessage());
        }finally {
            Log.i(TAG, " table entries in " + tableName + "  size=" + count);
            return count;
        }
    }

    public boolean hasTable(String tableName) {
        if(!isOpen(true))
            return false;

        try {
            String qry = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name=?";
            Cursor cursor = db.rawQuery(qry, new String[] { tableName });
            if (!cursor.moveToFirst()) {
                //cursor.close();
                CursorUtil.closeCursor(cursor);
                return false;
            }
            int count = cursor.getInt(0);
            //cursor.close();
            CursorUtil.closeCursor(cursor);
            return count > 0;
        }catch (Exception e) {
            Log.e(TAG, "Failed to check for DB Table: " + e.getMessage());
            return false;
        }
    }

    public String getName() {
        return name;
    }
    public String getPath() {
        return path;
    }
    public  SQLiteDatabase getDatabase() {
        return db;
    }

    private void setPermissions(File dbFile) {
        if(!XposedUtil.isVirtualXposed()) {
            File dbpfile = dbFile.getParentFile();
            Log.i(TAG, "mkdirs::" + dbpfile.getPath() + " " + dbpfile.getName());

            dbpfile.mkdirs();

            this.open();

            Log.i(TAG, "Setting File Permissions (0770) SYSTEM_UID For XLUA Directory");

            // Set database file permissions
            // Owner: rwx (system)
            // Group: rwx (system)
            // World: ---
            XUtil.setPermissions(dbpfile.getAbsolutePath(), 0770, Process.SYSTEM_UID, Process.SYSTEM_UID);
            File[] files = dbpfile.listFiles();
            if (files != null)
                for (File file : files)
                    XUtil.setPermissions(file.getAbsolutePath(), 0770, Process.SYSTEM_UID, Process.SYSTEM_UID);
        }
    }


    public static boolean isReady(XDatabaseOld database, String tableName) {
        boolean p1 = isReady(database);
        if(!p1)
            return false;

        if(!database.hasTable(tableName)) {
            Log.e(TAG, "null Database Table Does not exist, table=" + tableName + " db=" + database);
            return false;
        }

        return true;
    }

    public static boolean isReady(XDatabaseOld database) {
        if(database == null) {
            Log.e(TAG, "null Database entry cannot check if [isReady] if Database object is null...");
            return false;
        }

        //Bug IF file does not exist silly
        if(!database.exists()) {
            Log.e(TAG, "[" + database.getName() + "] Does not exist [" + database.dbFile.getAbsolutePath() + "]");
            return false;
        }

        if (!database.isOpen(true)) {
            Log.e(TAG, "[" + database.getName() + "] failed to open...");
            return false;
        }

        return true;
    }

    private static String dynamicCreateQuery(Map<String, String> colms, String tableName) {
        String top = "CREATE TABLE IF NOT EXISTS " + tableName + " (";
        StringBuilder mid = new StringBuilder();

        int i = 1;
        int sz = colms.size();
        for(Map.Entry<String, String> r : colms.entrySet()) {
            String l = r.getKey() + " " + r.getValue();
            mid.append(l);
            //if(i == 1) mid.append(" PRIMARY KEY");
            if(sz != i)  mid.append(",");
            i++;
        }

        String fullQuery = top + mid + ");";
        if(DebugUtil.isDebug())
            Log.i(TAG, "query=" + fullQuery);

        return fullQuery;
    }

    public static String dynamicCreateQueryEx(Map<String, String> columns, String tableName) {
        String top = "CREATE TABLE IF NOT EXISTS " + tableName + " (";
        StringBuilder mid = new StringBuilder();

        String pValue = columns.remove("PRIMARY");
        int i = 1;
        int sz = columns.size();
        for(Map.Entry<String, String> r : columns.entrySet()) {
            String l = r.getKey() + " " + r.getValue();
            mid.append(l);
            //if(i == 1) mid.append(" PRIMARY KEY");
            //if(sz != i)  mid.append(",");
            if(i < sz) mid.append(", ");
            i++;
        }

        if(pValue != null) {
            mid.append(", PRIMARY ").append(pValue);
        }

        return top + mid + ");";
    }

    @NonNull
    @Override
    public String toString() {
        return new StringBuilder()
                .append("file=[")
                .append(getPath())
                .append(" => ")
                .append(getName())
                .append("]  >> isOpen=")
                .append(isOpen(false)).toString();
    }
}
