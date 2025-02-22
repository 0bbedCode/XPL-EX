package eu.faircode.xlua.x.xlua.database.sql;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.database.DatabaseUtils;
import eu.faircode.xlua.x.xlua.database.IDatabaseEntry;
import eu.faircode.xlua.x.xlua.database.TableInfo;

public class SQLSnake extends SQLQueryBuilder {
    private static final String TAG = "XLua.SQLSnake";

    public static SQLSnake create() { return new SQLSnake();  }
    public static SQLSnake create(SQLDatabase db) { return new SQLSnake(db); }
    public static SQLSnake create(SQLDatabase db , String tableName) { return new SQLSnake(db, tableName); }

    private SQLDatabase db;
    private boolean canCompile = true;
    private Exception exception = null;

    public SQLDatabase getDatabase() { return db; }
    public SQLiteDatabase getRawDatabase() { return db != null ? db.getDatabase() : null; }

    public SQLSnake database(SQLDatabase database) { this.db = database; return this; }

    public SQLSnake() { }
    public SQLSnake(SQLDatabase db) { this.db = db; }
    public SQLSnake(SQLDatabase db, String tableName) { this.db = db; table(tableName); }

    public SQLSnake ensureDatabaseIsReady() {
        canCompile = DatabaseUtils.isReady(db);
        return this;
    }

    public SQLSnake ensureTableIsAvailable(TableInfo tableInfo) {
        if(db != null)
            DatabaseHelpEx.prepareDatabase(db, tableInfo);

        return this;
    }

    public SQLSnake readLock() {
        if(db != null) db.readLock();
        return this;
    }

    public SQLSnake readUnlock() {
        if(db != null) db.readUnlock();
        return this;
    }

    public <T extends IDatabaseEntry> List<T> queryAs(Class<T> typeClass, boolean cleanUpAfter, boolean lockRead) {
        if(!canCompile) return new ArrayList<>();
        canCompile = false;
        if(lockRead)
            db.readLock();

        Cursor c = query();
        List<T> items = new ArrayList<>();
        try {
            if(c != null) {
                if (c.moveToFirst()) {
                    do {
                        T item = typeClass.newInstance();   // Create a new instance of T
                        item.fromCursor(c);                 // Read data from cursor
                        items.add(item);
                    } while (c.moveToNext());
                }
            }

            return items;
        }catch (Exception e) {
            exception = e;
            Log.e(TAG, Str.ensureNoDoubleNewLines("Failed to Query As, Error=" + e + " this=" + this + "\nStack=" + RuntimeUtils.getStackTraceSafeString(e)));
            return items;
        } finally {
            if(lockRead) db.readUnlock();
            if(cleanUpAfter) CursorUtil.closeCursor(c);
        }
    }

    public <T extends IDatabaseEntry> T queryGetFirstAs(Class<T> typeClass, boolean cleanUpAfter, boolean readLock) {
        if(!canCompile) return null;
        canCompile = false;

        if(readLock)
            db.readLock();

        T item = null;
        Cursor c = null;
        try {
            // Create a new instance of T, we create it first to init a default so its never 'null' assuming the typeClass can be constructed ()
            item = typeClass.newInstance();

            c = query();
            if(c != null) {
                if (c.moveToFirst()) {
                    item.fromCursor(c);             // Read data from cursor
                    return item;
                }
            }
        }
        catch (InstantiationException ie) {
            exception = ie;
            Log.e(TAG, "Your object is messed up via constructor not my fault...");
        }catch (Exception e) {
            exception = e;
            Log.e(TAG, Str.ensureNoDoubleNewLines("Failed Getting First Element Query, Error=" + e + " this=" + this + "\nStack=" + RuntimeUtils.getStackTraceSafeString(e)));
        } finally {
            if(readLock) db.readUnlock();
            if(cleanUpAfter) CursorUtil.closeCursor(c);
        }

        return item;
    }

    public boolean exists_lock() {
        if(!canCompile) return false;
        canCompile = false;

        db.readLock();
        Cursor c = query();
        try {
            if(c == null) return false;
            return c.moveToFirst();
        }catch (Exception e) {
            Log.e(TAG, Str.ensureNoDoubleNewLines("Failed Check if item Exists assuming it does not, Error=" + e + " this=" + this + "\nStack=" + RuntimeUtils.getStackTraceSafeString(e)));
            return false;
        } finally {
            db.readUnlock();
            CursorUtil.closeCursor(c);
        }
    }


    public Cursor query() {
        Cursor c = null;
        try {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Executing Query, " + this);

            c = db.getDatabase().query(
                    tableName,
                    getOnlyReturn(),
                    getWhereClause(),
                    getWhereArgs(),
                    null,
                    null,
                    getColumnOrder());
        }catch (Exception e) {
            exception = e;
            Log.e(TAG, Str.ensureNoDoubleNewLines("Failed to execute query, Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e)  + "\nThis:" + this));
        }

        return c;
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Table", this.tableName)
                .appendFieldLine("Database", db)
                .appendFieldLine("Where Clause", getWhereClause())
                .appendFieldLine("Where Args", Str.joinArray(getWhereArgs(), ","))
                .appendFieldLine("Column Order", getColumnOrder())
                .appendFieldLine("Only Return", Str.joinArray(getOnlyReturn()))
                .toString(true);
    }
}
