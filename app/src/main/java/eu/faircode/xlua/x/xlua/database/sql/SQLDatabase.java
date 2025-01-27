package eu.faircode.xlua.x.xlua.database.sql;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.xstandard.database.SqlQuerySnake;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.file.FileApi;
import eu.faircode.xlua.x.file.FileEx;
import eu.faircode.xlua.x.xlua.XposedUtility;
import eu.faircode.xlua.x.xlua.database.DatabasePathUtil;
import eu.faircode.xlua.x.xlua.database.DatabaseUtils;
import eu.faircode.xlua.x.xlua.database.TableInfo;
import eu.faircode.xlua.x.xlua.settings.data.SettingInfoPacket;


public class SQLDatabase {
    private static final String TAG = "XLua.XDatabase.ex";

    public static SQLDatabase create(String databaseFileName, Context context) { return new SQLDatabase(databaseFileName, context, false); }
    public static SQLDatabase create(String databaseFileName, Context context, boolean open) { return new SQLDatabase(databaseFileName, context, open); }

    private final ReentrantReadWriteLock dbLock = new ReentrantReadWriteLock(true);

    public static final String PATH = FileApi.buildPath(true,Environment.getDataDirectory().getAbsolutePath(), "misc");
    public static final String PATH_OLD = FileApi.buildPath(true,Environment.getDataDirectory().getAbsolutePath(), "system");

    public static final String FOLDER_OLD = "xlua";
    public static final String FOLDER = "xplex";

    public static final String DATABASE_X_LUA = "xlua";
    public static final String DATABASE_MOCK = "mock";

    public final FileEx file;
    public final String name;


    private SQLiteDatabase db = null;

    public SQLiteDatabase getDatabase() { return this.db; }
    public FileEx getFile() { return file; }

    public SQLDatabase(String databaseFileName, Context context, boolean open) {
        FileEx base_dir = DatabasePathUtil.getDatabaseFolderOrCreate(context);
        DatabaseUtils.ensureValidFile(base_dir);

        String base_path = FileApi.buildPath(Objects.requireNonNull(base_dir).getAbsolutePath());
        this.name = DatabaseUtils.ensureValidDatabaseFileName(databaseFileName);

        String full_path = FileApi.buildPath(base_path, this.name);

        this.file = new FileEx(full_path);

        DatabasePathUtil.logI(Str.fm("Full path to Database, Path=%s  Name=%s  Base path=%s  File object Path=%s", full_path, this.name, base_path, this.file.getAbsolutePath()));

        if(open)
            open(true);
    }


    public boolean isMock() { return isName(DATABASE_MOCK); }
    public boolean isXLua() { return isName(DATABASE_X_LUA); }
    public boolean isName(String name) {
        if(this.name == null || name == null)
            return false;

        String check = Str.ensureEndsWith(Str.getLastStringEx(name, File.separator), ".db");
        if(DebugUtil.isDebug())
            XposedUtility.logD_xposed(TAG, "Is Name (" + check + ") name=" + this.name);


        return this.name.equalsIgnoreCase(check);
    }

    public String getDirectory() { return file.getDirectory().getAbsolutePath(); }
    public FileEx getDirectoryFile() { return file.getDirectory(); }

    public boolean exists() { return file != null && file.isFile(); }
    public boolean isOpen() { return isOpen(false); }
    public boolean isOpen(boolean openIfNotOpened) {
        if(db == null || !db.isOpen()) {
            if(openIfNotOpened)
                open(true);
        }

        return db != null && db.isOpen();
    }

    public boolean open() { return open(false); }
    public boolean open(boolean setPermissions) {
        try {
            if(db == null || !db.isOpen()) {
                if(setPermissions) {
                    this.file.takeOwnership();
                    this.file.setPermissions(FileApi.MODE_SOME_RW__770);
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Set Database Permissions (0770) (1000) >> " + this.file.getAbsolutePath());
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG, "Opening Database File retry, Database=" + this);

                db = SQLiteDatabase.openOrCreateDatabase(file, null);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Database Connection Opened: " + file.getAbsolutePath() + " Str=" + Str.toStringOrNull(db));
            }

            return db.isOpen();
        }catch (Exception e) {
            Log.e(TAG, "Failed to Open Database: " + file.getAbsolutePath());
            return false;
        }
    }

    public boolean close() {
        try {
            if(db != null) {
                db.close();
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Database Connection was closed, Database=" + file.getAbsolutePath());
            }

            return true;
        }catch (Exception e) {
            Log.e(TAG, "Failed to Close Database Connection, Database=" + file.getAbsolutePath());
            return false;
        }
    }

    public boolean delete(String tableName) { return delete(tableName, null, null); }
    public boolean delete(String tableName, String selectionArgs, String[] compareValues) {
        try {
            long rows = db.delete(tableName, selectionArgs, compareValues);
            if(rows < 0)
                throw new Exception("The item may already be Deleted, please double check. Rows Deleted Return=" + rows);

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Deleted [%s] Rows from Table [%s] Selection Args=[%s] Selection Values=[%s]", rows, tableName, selectionArgs, Str.joinArray(compareValues)));

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to Delete Database Item, Table=" + tableName + " Selection Args=" + selectionArgs + " Error=" + e + " Database=" + file.getAbsolutePath());
            return false;
        }
    }

    public boolean update(String tableName, ContentValues cv, SQLSnake queryFilter) { return update(tableName, cv, queryFilter.getWhereClause(), queryFilter.getWhereArgs()); }
    public boolean update(String tableName, ContentValues cv, String selectionArgs, String[] compareValues) {
        try {
            long rows = db.update(tableName, cv, selectionArgs, compareValues);
            if(rows != 1) {
                rows = db.updateWithOnConflict(tableName, cv, selectionArgs, compareValues, SQLiteDatabase.CONFLICT_REPLACE);
                if(rows != 1)
                    throw new Exception("Generic error, rows updated=" + rows);
            }

            return true;
        }catch (Exception e) {
            Log.e(TAG, "Failed to Update Database Item, Table=" + tableName + " Selection Args=" + selectionArgs + " Content Values=" + Str.toStringOrNull(cv) + " Error=" + e + " Database=" + file.getAbsolutePath());
            return false;
        }
    }

    public boolean insert(String tableName, ContentValues cv) {
        try {
            long rows = db.insertWithOnConflict(tableName, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            if(rows < 0)
                throw new Exception("Generic error, rows inserted=" + rows);

            return true;
        }catch (Exception e) {
            Log.e(TAG, "Failed to Insert Database Item, Table=" + tableName + " Content Values=" + Str.toStringOrNull(cv) + " Error=" + e + " Database=" + file.getAbsolutePath());
            return false;
        }
    }

    public boolean createTable(TableInfo tableInfo) { return createTable(tableInfo.name, tableInfo.columns); }
    public boolean createTable(String tableName, Map<String, String> columns) {
        if(!isOpen(true))
            return false;

        if(Str.isEmpty(tableName)) {
            Log.e(TAG, "Failed to Create Table, Name is not valid, its null or empty.... Database=" + file.getAbsolutePath());
            return false;
        }

        if(!ListUtil.isValid(columns)) {
            Log.e(TAG, "Failed to Create Table=" + tableName + " Columns is Null or Empty.... Database=" + file.getAbsolutePath());
            return false;
        }

        String qry = DatabaseUtils.dynamicCreateQueryEx(columns, tableName);
        if(DebugUtil.isDebug())
            Log.d(TAG, "Creating Database Table, Name=" + tableName + " Query=" + qry + " Database=" + file.getAbsolutePath());

        try {
            db.execSQL(qry);
            return true;
        }catch (Exception e) {
            Log.e(TAG, "Failed to Create Table, Table=" + tableName + " Database=" + file.getAbsolutePath() +  " Query=" + qry + " Error=" + e);
            return false;
        }
    }

    public boolean isTableEmpty(String tableName) { return tableEntries(tableName) <= 0; }

    public int tableEntries(String tableName) {
        if(!isOpen(true) || !hasTable(tableName))
            return -1;

        int count = 0;
        try {
            String query = "SELECT COUNT(*) FROM " + tableName;
            Cursor cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst())
                count = cursor.getInt(0);

            cursor.close();
        }catch (Exception e) {
            Log.e(TAG, "Error Enumerating Table Entries to get Count, Table Name=" + tableName + " Database=" + file.getAbsolutePath() + " Error=" + e);
        }

        return count;
    }


    /**
     * Gets the column count for a given table in the database.
     *
     * @param tableName The name of the table to query.
     * @return The number of columns in the table, or -1 if the table does not exist or an error occurs.
     */
    public int getColumnCount(String tableName) {
        if(!isOpen(true))
            return -1;

        Cursor cursor = null;
        try {
            // Query PRAGMA table_info for the table
            String query = "PRAGMA table_info(" + tableName + ")";
            cursor = db.rawQuery(query, null);

            // Return the column count from the cursor
            if (cursor != null) {
                return cursor.getCount();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get Table Column Count, Table=" + tableName + " db=" + this + " Error=" + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1; // Return -1 if table doesn't exist or error occurs
    }

    /**
     * Checks if a column exists in a given table.
     *
     * @param tableName The name of the table to check.
     * @param columnName The name of the column to check for.
     * @return True if the column exists, false otherwise.
     */
    public boolean doesColumnExist(String tableName, String columnName) {
        if(!isOpen(true))
            return false;

        Cursor cursor = null;
        try {
            // Query PRAGMA table_info for the table
            String query = "PRAGMA table_info(" + tableName + ")";
            cursor = db.rawQuery(query, null);

            // Check if the column name exists in the result
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex("name");
                while (cursor.moveToNext()) {
                    String existingColumnName = cursor.getString(nameIndex);
                    if (columnName.equals(existingColumnName)) {
                        return true; // Column found
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to Check if Table Column Exists, Table=" + tableName + " db=" + this + " Error=" + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false; // Column not found
    }

    /**
     * Checks if the column names of a table match the given list of column names.
     *
     * @param tableName   The name of the table to query.
     * @param columnNames The list of expected column names.
     * @return True if the table's column names match the given list, false otherwise.
     */
    public boolean doColumnNamesMatch(String tableName, Collection<String> columnNames) {
        if(!isOpen(true))
            return false;

        columnNames.remove("PRIMARY");
        if(columnNames.isEmpty())
            return true;

        Cursor cursor = null;
        try {
            // Query PRAGMA table_info for the table
            String query = "PRAGMA table_info(" + tableName + ")";
            cursor = db.rawQuery(query, null);

            // Collect column names from the table
            List<String> tableColumnNames = new ArrayList<>();
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex("name");
                while (cursor.moveToNext()) {
                    tableColumnNames.add(cursor.getString(nameIndex));
                }
            }

            if(DebugUtil.isDebug())
                XposedUtility.logD_xposed(TAG, "Column Names=" + Str.joinList(tableColumnNames) + " Wanted Columns=" + Str.joinList(new ArrayList<>(columnNames)));

            if(columnNames.size() != tableColumnNames.size())
                return false;

            for(String c : tableColumnNames) {
                if(!columnNames.contains(c)) {
                    XposedUtility.logE_xposed(TAG, "Column Name is not found=" + c);
                    return false;
                }
            }

            // Compare the two lists
            //return tableColumnNames.equals(columnNames);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to Check if Table Columns align, Table=" + tableName + " db=" + this + " Error=" + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false; // Return false if an error occurs
    }

    public boolean dropTable(String tableName) {
        if(!isOpen(true))
            return false;

        try {
            String query = "DROP TABLE IF EXISTS " + tableName;
            db.execSQL(query);
            return true;
        }catch (Exception e) {
            Log.e(TAG, "Failed to Drop Table From Database, Table=" + tableName + " Database=" + file.getAbsolutePath() + " Error=" + e);
            return false;
        }
    }

    public boolean hasTable(String tableName) {
        if(!isOpen(true))
            return false;

        Cursor cursor = null;
        try {
            String qry = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name=?";
            cursor = db.rawQuery(qry, new String[] { tableName });
            if (!cursor.moveToFirst())
                return false;

            int count = cursor.getInt(0);
            return count > 0;
        }catch (Exception e) {
            Log.e(TAG, "Failed to Get Table/ Check if Table Exists, Table=" + tableName + " Database=" + file.getAbsolutePath() + " Error=" + e);
            return false;
        }
        finally {
            CursorUtil.closeCursor(cursor);
        }
    }

    /*
        WriteLock is considered a "strong" lock in a ReentrantReadWriteLock.
        - Acquiring a WriteLock provides exclusive access to the resource.
        - While a WriteLock is held:
            * No other thread can acquire either a ReadLock or another WriteLock.
            * The thread holding the WriteLock can perform both read and write operations.
        - Use WriteLock when modifications to the resource are required,
          or when a conditionally read-write operation (e.g., check-then-create) is needed.

        ReadLock is a "weaker" lock:
        - Multiple threads can acquire the ReadLock simultaneously for read-only access.
        - However, no thread can acquire the WriteLock until all ReadLocks are released.
        - Use ReadLock when performing purely read-only operations to allow concurrent access.

        IMPORTANT: Avoid acquiring both ReadLock and WriteLock simultaneously
        in the same thread to prevent deadlocks or unnecessary complexity.
    */

    public void writeLock() { dbLock.writeLock().lock(); }
    public void writeUnlock() { dbLock.writeLock().unlock(); }
    public void readLock() { dbLock.readLock().lock(); }
    public void readUnlock() { dbLock.readLock().unlock(); }
    public boolean beginTransaction() { return beginTransaction(false); }

    public boolean beginTransaction(boolean writeLock) {
        if(!isOpen(true))
            return false;

        try {
            if(writeLock) writeLock();
            db.beginTransaction();
            return true;
        }catch (Exception e) {
            if(writeLock) writeUnlock();
            Log.e(TAG, "Failed to begin Transaction, Database=" + file.getAbsolutePath() + " Error=" + e);
            return false;
        }
    }

    public void setTransactionSuccessful(){
        try {
            db.setTransactionSuccessful();
        }catch (Exception e) {
            Log.e(TAG, "Failed to set the Transaction as Successful, Database=" + file.getAbsolutePath() + " Error=" + e);
        }
    }

    public boolean endTransaction() { return endTransaction(false, false); }
    public boolean endTransaction(boolean writeUnlock, boolean setTransactionToSuccessful) {
        if(!isOpen(false))
            return false;

        try {
            if(setTransactionToSuccessful) setTransactionSuccessful();
            db.endTransaction();
            return true;
        }catch (Exception e) {
            Log.e(TAG, "Failed to End Transaction, Database=" + file.getAbsolutePath() + " Error=" + e);
            return false;
        } finally {
            if(writeUnlock) writeUnlock();
        }
    }

    public void executeWithWriteLock(Runnable action) { executeWithWriteLock(false, action); }
    public void executeWithWriteLock(boolean endTransaction, Runnable action) {
        writeLock();
        try {
            action.run();
        } catch (Exception e) {
            Log.e(TAG, "[1] Failed to Execute Write Lock Database Action! Error=" + e);
        } finally {
            writeUnlock();
        }
    }

    public void executeWithReadLock(Runnable action) {
        readLock();
        try {
            action.run();
        } catch (Exception e) {
            Log.e(TAG, "[1] Failed to Execute Read Lock Database Action! Error=" + e);
        } finally {
            readUnlock();
        }
    }

    public <T> T executeWithWriteLock(Callable<T> action) {
        writeLock();
        try {
            return action.call();
        } catch (Exception e) {
            Log.e(TAG, "Failed to Execute Write Lock Database Action! Error=" + e);
            return null;
        } finally {
            writeUnlock();
        }
    }

    public <T> T executeWithReadLock(Callable<T> action) {
        readLock();
        try {
            return action.call();
        } catch (Exception e) {
            Log.e(TAG, "Failed to Execute Read Lock Database Action! Error=" + e);
            return null;
        } finally {
            readUnlock();
        }
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Database", this.file.getAbsolutePath())
                .appendFieldLine("Is Open", this.isOpen(false))
                .toString(true);
    }
}
