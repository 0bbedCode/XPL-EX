package eu.faircode.xlua.api.xstandard.database;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import eu.faircode.xlua.BuildConfig;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.api.xstandard.interfaces.IDBSerial;
import eu.faircode.xlua.loggers.DatabaseQueryLogger;
import eu.faircode.xlua.x.xlua.database.IDatabaseEntry;

public class SqlQuerySnake extends SqlQueryBuilder {
    private static final String TAG = "XLua.DatabaseQuerySnake";

    private boolean canCompile = true;
    private Exception error = null;

    public static SqlQuerySnake create() { return new SqlQuerySnake();}
    public static SqlQuerySnake create(String tableName) { return new SqlQuerySnake(null, tableName); }
    //public static SqlQuerySnake create(XDatabase db, String tableName)
    public static SqlQuerySnake create(XDatabaseOld db, String tableName) {
        return new SqlQuerySnake(db, tableName);
    }

    public SqlQuerySnake() { super(); }
    public SqlQuerySnake(XDatabaseOld db, String tableName) { super(db, tableName); }

    public SqlQuerySnake setDatabase(XDatabaseOld db) {
        this.db = db;
        return this;
    }

    /*public SqlQuerySnake setSymbol(String symbol, String compareValue) {
        setSymbol(symbol, compareValue);
        return this;
    }

    public SqlQuerySnake setSymbol(String symbol) {
        internal_setSymbol(symbol);
        return this;
    }*/

    public SqlQuerySnake useOr(boolean useOr) {
        internal_useOr(useOr);
        return this;
    }

    public SqlQuerySnake whereColumns(String... columnNames) {
        internal_whereColumnsEquals(columnNames);
        return this;
    }

    public SqlQuerySnake whereColumnValues(String... values) {
        internal_anchorValuesWithFields(values);
        return this;
    }

    public SqlQuerySnake whereColumnNoBind(String columnName, String value) {
        internal_whereColumnBinds(columnName, value);
        return this;
    }

    public SqlQuerySnake whereColumn(String columnName, String value) {
        internal_whereColumnBinds(columnName, value);
        return this;
    }

    public SqlQuerySnake whereColumn(String columnName, int value) {
        internal_whereColumnBinds(columnName, Integer.toString(value), null);
        return this;
    }

    public SqlQuerySnake whereColumn(String columnName, int value, String symbol) {
        internal_whereColumnBinds(columnName, Integer.toString(value), symbol);
        return this;
    }

    public SqlQuerySnake whereColumn(String columnName, String value, String symbol) {
        internal_whereColumnBinds(columnName, value, symbol);
        return this;
    }

    public SqlQuerySnake onlyReturnColumn(String columnName) {
        internal_onlyReturnColumn(columnName);
        return this;
    }

    public SqlQuerySnake onlyReturnColumns(String... fields) {
        internal_onlyReturnColumns(fields);
        return this;
    }

    public SqlQuerySnake orderBy(String orderByOrFieldName) {
        internal_orderBy(orderByOrFieldName);
        return this;
    }

    public SqlQuerySnake setSelectionArgs(String[] args) {
        compareValues.addAll(Arrays.asList(args));
        return this;
    }

    public SqlQuerySnake setCompareArgs(String compareString) {
        selectionArgsBuilder.append(compareString);
        return this;
    }

    public XDatabaseOld getDatabase() { return db; }
    public String getOrderBy() { return orderOrFieldName; }

    public List<String> getOnlyReturn() {
        for(String s : onlyReturn)
            Log.i(TAG, "onlyReturn=" + s);

        return onlyReturn;
    }

    public SqlQuerySnake ensureDatabaseIsReady() {
        canCompile = XDatabaseOld.isReady(db);
        return this;
    }

    public boolean threwError() { return error != null; }
    public Exception getError() { return error; }

    public boolean exists() {
        if(!canCompile) return false;
        canCompile = false;

        db.readLock();
        Cursor c = query();
        try {
            if(c == null) return false;
            return c.moveToFirst();
        }catch (Exception e) {
            Log.e(TAG, "Failed to query Cursor for Check if Exists! From DB [" + db + "] from Table [" + tableName + "]\n" + e + "\n" + Log.getStackTraceString(e));
            return false;
        } finally {
            db.readUnlock();
            clean(c);
        }
    }

    public Collection<String> queryAsStringList(String columnReturn, boolean cleanUpAfter) {
        if(!canCompile) return null;
        canCompile = false;

        prepareReturn(columnReturn);

        db.readLock();
        Cursor c = query();
        Collection<String> list = new ArrayList<>();
        try {
            if(c != null) {
                int ix = c.getColumnIndex(columnReturn);
                if(ix == -1) {
                    Log.e(TAG, "Database [" + db + "] Table [" + tableName + "] Field Name [" + columnReturn + "] Does not exist in the table or cursor enum ??");
                    return list;
                }

                int typeCode = c.getType(ix);
                if (typeCode == Cursor.FIELD_TYPE_NULL) {
                    Log.e(TAG, "Database [" + db + "] Table [" + tableName + "] Field Name [" + columnReturn + "] Type is null ??");
                    return list;
                }

                if(!c.moveToFirst()) {
                    Log.e(TAG, "Database [" + db + "] Table [" + tableName + "] Field Name [" + columnReturn + "] Failed to get First Element ??");
                    return list;
                }

                switch (typeCode) {
                    case Cursor.FIELD_TYPE_STRING:
                        do { list.add(c.getString(ix)); } while (c.moveToNext());
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        do { list.add(Integer.toString(c.getInt(ix))); } while (c.moveToNext());
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        do { list.add(Float.toString(c.getFloat(ix))); } while (c.moveToNext());
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                        //Add
                        break;
                        //fix add long , double , short support
                }
            }

            return list;
        }catch (Exception e) {
            error = e;
            Log.e(TAG, "Failed to query Cursor as String List! From DB [" + db + "] from Table [" + tableName + "]\n" + e + "\n" + Log.getStackTraceString(e));
            return list;
        } finally {
            db.readUnlock();
            if(cleanUpAfter)
                clean(c);
        }
    }

    public String queryGetFirstString(String columnReturn, boolean cleanUpAfter) { return queryGetFirstString(columnReturn, null, cleanUpAfter); }
    public String queryGetFirstString(String columnReturn, String defaultValue, boolean cleanUpAfter) {
        if(!canCompile) return defaultValue;
        canCompile = false;

        prepareReturn(columnReturn);
        db.readLock();
        Cursor c = query();
        try {
            if(c != null) {
                if (c.moveToFirst())
                    return c.getString(0);
            }
        }catch (Exception e) {
            error = e;
            Log.e(TAG, "Failed to query Cursor as String! From DB [" + db + "] from Table [" + tableName + "]\n" + e + "\n" + Log.getStackTraceString(e));
        }finally {
            db.readUnlock();
            if(cleanUpAfter)
                clean(c);
        }

        return defaultValue;
    }

    public int queryGetFirstInt(String columnReturn, boolean cleanUpAfter) { return queryGetFirstInt(columnReturn, 0, cleanUpAfter); }
    public int queryGetFirstInt(String columnReturn, int defaultValue, boolean cleanUpAfter) {
        if(!canCompile) return defaultValue;
        canCompile = false;

        prepareReturn(columnReturn);
        db.readLock();
        Cursor c = query();
        try {
            if(c != null) {
                if (c.moveToFirst())
                    return c.getInt(0);
            }
        }catch (Exception e) {
            error = e;
            Log.e(TAG, "Failed to query Cursor as Int! From DB [" + db + "] from Table [" + tableName + "]\n" + e + "\n" + Log.getStackTraceString(e));
        }finally {
            db.readUnlock();
            if(cleanUpAfter)
                clean(c);
        }

        return defaultValue;
    }

    public long queryGetFirstLong(String columnReturn, boolean cleanUpAfter) { return queryGetFirstLong(columnReturn, 0, cleanUpAfter); }
    public long queryGetFirstLong(String columnReturn,long defaultValue, boolean cleanUpAfter) {
        if(!canCompile) return defaultValue;
        canCompile = false;

        prepareReturn(columnReturn);
        db.readLock();
        Cursor c = query();
        try {
            if(c != null) {
                if (c.moveToFirst())
                    return c.getLong(0);
            }
        }catch (Exception e) {
            error = e;
            Log.e(TAG, "Failed to query Cursor as Long! From DB [" + db + "] from Table [" + tableName + "]\n" + e + "\n" + Log.getStackTraceString(e));
        }finally {
            db.readUnlock();
            if(cleanUpAfter)
                clean(c);
        }

        return defaultValue;
    }

    public <T extends IDatabaseEntry> T queryGetFirstAs_final(Class<T> typeClass, boolean cleanUpAfter) {
        if(!canCompile) return null;
        canCompile = false;

        T item = null;
        Cursor c = null;
        try {
            // Create a new instance of T, we create it first to init a default so its never 'null' assuming the typeClass can be constructed ()
            item = typeClass.newInstance();
            db.readLock();
            c = query();
            if(c != null) {
                if (c.moveToFirst()) {
                    item.fromCursor(c);             // Read data from cursor
                    return item;
                }
            }
        }
        catch (InstantiationException ie) {
            error = ie;
            Log.e(TAG, "Your object is messed up via constructor not my fault...");
        }catch (Exception e) {
            error = e;
            Log.e(TAG, "Failed to query Cursor! From DB [" + db + "] from Table [" + tableName + "]\n" + e + "\n" + Log.getStackTraceString(e));
        } finally {
            db.readUnlock();
            if(cleanUpAfter)
                clean(c);
        }

        return item;
    }

    public <T extends IDBSerial> T queryGetFirstAs(Class<T> typeClass, boolean cleanUpAfter) {
        if(!canCompile) return null;
        canCompile = false;

        T item = null;
        Cursor c = null;
        try {
            // Create a new instance of T, we create it first to init a default so its never 'null' assuming the typeClass can be constructed ()
            item = typeClass.newInstance();
            db.readLock();
            c = query();
            if(c != null) {
                if (c.moveToFirst()) {
                    item.fromCursor(c);             // Read data from cursor
                    return item;
                }
            }
        }
        catch (InstantiationException ie) {
            error = ie;
            Log.e(TAG, "Your object is messed up via constructor not my fault...");
        }catch (Exception e) {
            error = e;
            Log.e(TAG, "Failed to query Cursor! From DB [" + db + "] from Table [" + tableName + "]\n" + e + "\n" + Log.getStackTraceString(e));
        } finally {
            db.readUnlock();
            if(cleanUpAfter)
                clean(c);
        }

        return item;
    }


    public <T extends IDatabaseEntry> Collection<T> queryAs_final(Class<T> typeClass) { return queryAs_final(typeClass, false); }
    public <T extends IDatabaseEntry> Collection<T> queryAs_final(Class<T> typeClass, boolean cleanUpAfter) {
        if(!canCompile) return new ArrayList<>();
        canCompile = false;

        db.readLock();
        Cursor c = query();
        Collection<T> items = new ArrayList<>();
        try {
            if(c != null) {
                if (c.moveToFirst()) {
                    do {
                        T item = typeClass.newInstance();   // Create a new instance of T
                        item.fromCursor(c);             // Read data from cursor
                        items.add(item);
                    } while (c.moveToNext());
                }
            }

            return items;
        }catch (Exception e) {
            error = e;
            Log.e(TAG, "Failed to query Cursor! From DB [" + db + "] from Table [" + tableName + "]\n" + e + "\n" + Log.getStackTraceString(e));
            return items;
        } finally {
            db.readUnlock();
            if(cleanUpAfter)
                clean(c);
        }
    }


    public <T extends IDBSerial> Collection<T> queryAs(Class<T> typeClass) { return queryAs(typeClass, false); }
    public <T extends IDBSerial> Collection<T> queryAs(Class<T> typeClass, boolean cleanUpAfter) {
        if(!canCompile) return new ArrayList<>();
        canCompile = false;

        db.readLock();
        Cursor c = query();
        Collection<T> items = new ArrayList<>();
        try {
            if(c != null) {
                if (c.moveToFirst()) {
                    do {
                        T item = typeClass.newInstance();   // Create a new instance of T
                        item.fromCursor(c);             // Read data from cursor
                        items.add(item);
                    } while (c.moveToNext());
                }
            }

            return items;
        }catch (Exception e) {
            error = e;
            Log.e(TAG, "Failed to query Cursor! From DB [" + db + "] from Table [" + tableName + "]\n" + e + "\n" + Log.getStackTraceString(e));
            return items;
        } finally {
            db.readUnlock();
            if(cleanUpAfter)
                clean(c);
        }
    }

    public <T extends IDBSerial> Collection<T> queryAsData(Class<T> typeClass, boolean cleanUpAfter)  {
        if(!canCompile) return null;
        canCompile = false;

        Collection<T> items = new ArrayList<>();
        db.readLock();
        Cursor c = query();
        try {
            T reader = typeClass.newInstance();
            reader.fromCursor(c);
            return items;
        }catch (Exception e) {
            error = e;
            Log.e(TAG, "Failed to Query from DB [" + db + "] from Table [" + tableName + "] with Selection Args [" + selectionArgsBuilder + "]\n" + e + "\n" + Log.getStackTraceString(e));
            return items;
        }finally {
            db.readUnlock();
            if(cleanUpAfter)
                clean(c);
        }
    }

    public <T extends IDBSerial> Collection<T> queryAll(Class<T> typeClass) { return queryAll(typeClass, false); }
    public <T extends IDBSerial> Collection<T> queryAll(Class<T> typeClass, boolean cleanUpAfter) {
        if(!canCompile) return null;
        canCompile = false;

        Collection<T> items = new ArrayList<>();
        Cursor c = null;
        try {
            String[] columns = onlyReturn.isEmpty() ? null : onlyReturn.toArray(new String[0]);
            db.readLock();
            c = db.getDatabase().query(
                    tableName,
                    columns,
                    null,
                    null,
                    null,
                    null,
                    orderOrFieldName);

            while (c.moveToNext()) {
                T item = typeClass.newInstance();   // Create a new instance of T
                item.fromCursor(c);             // Read data from cursor
                items.add(item);
            }

            return items;
        }catch (Exception e) {
            error = e;
            Log.e(TAG, "Failed to Query from DB [" + db + "] from Table [" + tableName + "] with Selection Args [" + selectionArgsBuilder + "]\n" + e + "\n" + Log.getStackTraceString(e));
            return items;
        }finally {
            db.readUnlock();
            if(cleanUpAfter)
                clean(c);
        }
    }

    public <T extends IDBSerial> Collection<T> queryAllEx(Class<T> typeClass) { return queryAll(typeClass, false); }
    public <T extends IDBSerial> Collection<T> queryAllEx(Class<T> typeClass, boolean cleanUpAfter) {
        if(!canCompile) return null;
        canCompile = false;

        Collection<T> items = new ArrayList<>();
        Cursor c = null;
        try {
            db.readLock();
            c = db.getDatabase().rawQuery("SELECT * FROM " + tableName, null);
            while (c.moveToNext()) {
                T item = typeClass.newInstance();   // Create a new instance of T
                item.fromCursor(c);             // Read data from cursor
                items.add(item);
            }

            return items;
        }catch (Exception e) {
            error = e;
            Log.e(TAG, "Failed to Query from DB [" + db + "] from Table [" + tableName + "] with Selection Args [" + selectionArgsBuilder + "]\n" + e + "\n" + Log.getStackTraceString(e));
            return items;
        }finally {
            db.writeUnlock();
            if(cleanUpAfter)
                clean(c);
        }
    }

    public Cursor query() {
        if(DebugUtil.isDebug())
            DatabaseQueryLogger.logSnakeSnapshot(this, true, true);

        Cursor c = null;
        try {
            String[] columns = onlyReturn.isEmpty() ? null : onlyReturn.toArray(new String[0]);
            if(BuildConfig.DEBUG)
                Log.i(TAG, "query,  table=" + tableName);

            c = db.getDatabase().query(
                    tableName,
                    columns,
                    selectionArgsBuilder.toString(),
                    compareValues.toArray(new String[0]),
                    null,
                    null,
                    orderOrFieldName);
        }catch (Exception e) {
            error = e;
            Log.e(TAG, "Failed to Query from DB [" + db + "] from Table [" + tableName + "] with Selection Args [" + selectionArgsBuilder + "]\n" + e + "\n" + Log.getStackTraceString(e));
        }

        return c;
    }

    public void clean(Cursor c) {
        compareValues.clear();
        onlyReturn.clear();
        if(c != null) {
            try {
                c.close();
            }catch (Exception e) {
                Log.e(TAG, "Failed to Close Cursor ? \n" + e + "\n" + Log.getStackTraceString(e));
            }
        }
    }

    private void prepareReturn(String fieldReturn) {
        if(onlyReturn.size() > 0 && !onlyReturn.contains(fieldReturn))
            onlyReturn.clear();

        if(!onlyReturn.contains(fieldReturn))
            onlyReturn.add(fieldReturn);
    }
}
