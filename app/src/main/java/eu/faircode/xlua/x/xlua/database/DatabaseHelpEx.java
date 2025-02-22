package eu.faircode.xlua.x.xlua.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.utilities.CollectionUtil;
import eu.faircode.xlua.utilities.StringUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.JsonHelperEx;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.PacketBase;
import eu.faircode.xlua.x.xlua.XposedUtility;
import eu.faircode.xlua.x.xlua.commands.XPacket;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.database.sql.SQLSnake;
import eu.faircode.xlua.x.xlua.interfaces.ICursorType;
import eu.faircode.xlua.x.xlua.interfaces.IJsonType;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

public class DatabaseHelpEx {
    private static final String TAG = "XLua.DatabaseHelperEx";
    private static final String TAG_initDatabase = "initDatabase";
    private static final String TAG_prepareTable = "initDatabase";
    private static final String TAG_InsertUpdate = "updateOrInsertItem";
    private static final String TAG_InsertUpdateS = "insertOrUpdateItems";
    private static final String TAG_DELETE_ITEM = "deleteItem";
    private static final String TAG_JSON = "compareJsonWithDatabase";

    private static final String ERROR_PREPARE = "Database not Prepared!";
    private static final String ERROR_READY = "Database not Ready!";
    private static final String ERROR_TRANSACTION = "Failed to begin Database Transaction";
    private static final String ERROR_TABLE = "Table does not Exist/ Could not Create!";
    private static final String ERROR_INSERT = "Failed to Insert Item";
    private static final String ERROR_UPDATE = "Failed to Update Item";
    private static final String ERROR_DELETE = "Failed to Delete Database Table Item";


    public static final int DB_FORCE_CHECK = -8;
    public static final int DB_FORCE_SKIP_CHECK = -18;

    public static A_CODE execute_one_locked_name(SQLDatabase database, PacketBase packet, TableInfo table) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "Executing a Single Database Transaction Table=" + table.name);

        A_CODE code = DatabaseHelpEx.ensureRead(database, packet, table);
        if(code != A_CODE.NONE)
            return code;

        switch (packet.getActionFlags()) {
            case UPDATE:
            case PUSH:
                code = A_CODE.resultToCode_x(DatabaseHelpEx.insertItem(database, table.name, packet));
                break;
            case DELETE:
                code = A_CODE.resultToCode_x(
                        DatabaseHelpEx.deleteItem(
                        SQLSnake.create(database, table.name)
                                //.whereIdentity(packet.getUserIdentity().getUserId(true), packet.getCategory())
                                .whereObjectId(packet)
                                .asSnake()));
                break;
        }

        //Do Is Kill ?
        return code;
    }

    public static boolean dropTable_locked(SQLDatabase database, String tableName) {
        if(database == null) {
            XposedUtility.logE_xposed(TAG, "[dropTable_locked] Database Object is NULL!");
            return false;
        }

        if(!database.isOpen(true)) {
            XposedUtility.logE_xposed(TAG, "[dropTable_locked] Failed to Open Database: " + Str.noNL(database));
            return false;
        }

        if(Str.isEmpty(tableName)) {
            XposedUtility.logE_xposed(TAG, "[dropTable_locked] Table Name Passed was NULL or Empty!");
            return false;
        }

        try {
            if(!database.beginTransaction(true)) {
                XposedUtility.logE_xposed(TAG, "[dropTable_locked] Failed to being Transaction, Table=" +tableName + " Db=" + Str.toStringOrNull(database));
                return false;
            }

            if(!database.hasTable(tableName)) {
                XposedUtility.logW_xposed(TAG, "[dropTable_locked] Table is already Dropped! Table=" + tableName);
                return true;
            }

            if(!database.dropTable(tableName)) {
                XposedUtility.logE_xposed(TAG, "[dropTable_locked] Failed to Drop Table, Unknown Reasons, Table=" + tableName);
                if(!database.hasTable(tableName)) {
                    database.setTransactionSuccessful();
                    return true;
                }
                else
                    return false;
            } else {
                XposedUtility.logW_xposed(TAG, "[dropTable_locked] Successfully Dropped Table! Table=" + tableName);
                database.setTransactionSuccessful();
                return true;
            }
        } catch (Exception e) {
            XposedUtility.logE_xposed(TAG, Str.fm("Failed to Drop Table [%s] from Database [%s] Error=%s",
                    Str.toStringOrNull(tableName),
                    Str.toStringOrNull(database),
                    e));
            return false;
        }finally {
            database.endTransaction(true, false);
        }
    }

    public static boolean ensureTableIsReady_locked(TableInfo tableInfo, SQLDatabase database) {
        if(database == null) {
            XposedUtility.logE_xposed(TAG, "[ensureTableIsReady_locked] Database Object is NULL!");
            return false;
        }

        if(!database.isOpen(true)) {
            XposedUtility.logE_xposed(TAG, "[ensureTableIsReady_locked] Failed to Open Database: " + Str.noNL(database));
            return false;
        }

        if(tableInfo == null || Str.isEmpty(tableInfo.name)) {
            XposedUtility.logE_xposed(TAG, "[ensureTableIsReady_locked] Table Info Passed was NULL!");
            return false;
        }

        try {
            if(!database.beginTransaction(true)) {
                XposedUtility.logE_xposed(TAG, "[ensureTableIsReady_locked] Failed to being Transaction, Table=" + Str.toStringOrNull(tableInfo) + " Db=" + Str.toStringOrNull(database));
                return false;
            }

            if(!database.hasTable(tableInfo.name)) {
                XposedUtility.logW_xposed(TAG, Str.fm("[ensureTableIsReady_locked] Table [%s] is Missing from the Database [%s] Creating!",
                        tableInfo.name,
                        Str.noNL(database)));

                //Options to lock
                if(!database.createTable(tableInfo)) {
                    XposedUtility.logE_xposed(TAG, Str.fm("[ensureTableIsReady_locked] Table [%s] was not created, Failed! Database [%s]...",
                            tableInfo.name,
                            Str.noNL(database)));

                    return false;
                } else {
                    database.setTransactionSuccessful();
                    XposedUtility.logI_xposed(TAG, Str.fm("[ensureTableIsReady_locked] Table [%s] was Created! in Database [%s]!",
                            tableInfo.name,
                            Str.noNL(database)));

                    return true;
                }
            } else {
                //Do more in depth checks ?
                return true;
            }
        } catch (Exception e) {
            XposedUtility.logE_xposed(TAG, Str.fm("Error Ensuring Table [%s] is Ready from Database [%s] Error=%s",
                    Str.toStringOrNull(tableInfo),
                    Str.toStringOrNull(database),
                    e));
            return false;
        }finally {
            database.endTransaction(true, false);
        }
    }


    public static boolean ensureTableIsReady(TableInfo tableInfo, SQLDatabase database) {
        if(database == null) {
            XposedUtility.logE_xposed(TAG, "[ensureTableIsReady] Database Object is NULL!");
            return false;
        }

        if(!database.isOpen(true)) {
            XposedUtility.logE_xposed(TAG, "[ensureTableIsReady] Failed to Open Database: " + Str.noNL(database));
            return false;
        }

        if(tableInfo == null || Str.isEmpty(tableInfo.name)) {
            XposedUtility.logE_xposed(TAG, "[ensureTableIsReady] Table Info Passed was NULL!");
            return false;
        }

        if(!database.hasTable(tableInfo.name)) {
            XposedUtility.logW_xposed(TAG, Str.fm("[ensureTableIsReady] Table [%s] is Missing from the Database [%s] Creating!",
                    tableInfo.name,
                    Str.noNL(database)));

            //Options to lock
            if(!database.createTable(tableInfo)) {
                XposedUtility.logE_xposed(TAG, Str.fm("[ensureTableIsReady] Table [%s] was not created, Failed! Database [%s]...",
                        tableInfo.name,
                        Str.noNL(database)));

                return false;
            } else {
                XposedUtility.logI_xposed(TAG, Str.fm("[ensureTableIsReady] Table [%s] was Created! in Database [%s]!",
                        tableInfo.name,
                        Str.noNL(database)));

                //do rest of checks ?
            }
        }

        return true;
    }



    public static <T extends ICursorType> List<T> getFromDatabase(
            SQLDatabase db,
            String tableName,
            Class<T> typeClass,
            boolean oldestToNewest) {

        List<T> items = new ArrayList<>();
        if(DebugUtil.isDebug())
            Log.d(TAG, "[getFromDatabase] table=" + tableName + " db=" + db);

        try {
            String qry = StrBuilder.create()
                    .append("SELECT * FROM ")
                    .append(tableName)
                    .setDoAppendFlag(oldestToNewest)
                    .append(" ORDER BY ROWID ASC")
                    .toString();

            if(DebugUtil.isDebug())
                Log.d(TAG, "Query Database Table [" + tableName + "] With Query: " + qry);

            Cursor c = db.getDatabase().rawQuery(qry, null);
            while (c.moveToNext()) {
                T item = typeClass.newInstance();
                item.fromCursor(c);
                items.add(item);
            }
        }catch (Exception e) {
            Log.e(TAG, "[getFromDatabase] error , table=" + tableName + " db=" + db);
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Items in DB Table [" + tableName + "] Count=" + ListUtil.size(items));

        return items;
    }

    public static  <T extends IJsonType & ICursorType & IDatabaseEntry> Collection<T> getOrInitTable(
            Context context,
            SQLDatabase db,
            String tableName,
            Map<String, String> columns,
            String jsonName,
            boolean stopOnFirstJson,
            Class<T> typeClass,
            int itemCheckCount) {

        Collection<T> items = new ArrayList<>();

        if(DebugUtil.isDebug())
            Log.i(TAG, "init tableName=" + tableName + " count=" + itemCheckCount + " db=" + db);

        if(!DatabaseUtils.isReady(db)) {
            error(tableName, db, TAG_initDatabase, ERROR_READY);
            return items;
        }

        if(DebugUtil.isDebug())
            Log.i(TAG, "Database is ready=" + db + " table=" + tableName);

        if(!db.beginTransaction(true)) {
            error(tableName, db, TAG_initDatabase, ERROR_TRANSACTION);
            return items;
        }

        jsonName = StringUtil.trimEnsureEnd(jsonName, ".json");
        boolean hasJsonLinked = StringUtil.isValidString(jsonName);
        db.readLock();

        try {
            if(!db.hasTable(tableName) || db.isTableEmpty(tableName)) {
                warning(tableName, db, TAG_initDatabase, "Table is Empty or NULL");

                if(!db.createTable(tableName, columns)) {
                    error(tableName, db, TAG_initDatabase, ERROR_TABLE);
                    return items;
                }

                if(hasJsonLinked) {
                    for(T item : JsonHelperEx.findJsonElementsFromAssets(XUtil.getApk(context), jsonName, stopOnFirstJson, typeClass)) {
                        ContentValues cv = new ContentValues();
                        item.populateContentValues(cv);

                        if(!db.insert(tableName, ensureProperContentValues(cv, columns))) {
                            error(tableName, db, TAG_initDatabase, ERROR_INSERT + " item=" + item);
                            continue;
                        }

                        items.add(item);
                    }
                }

            }else {
                items = getFromDatabase(db, tableName, typeClass, false);
                if(DebugUtil.isDebug())
                    info(tableName, db, TAG_initDatabase, "itemCheckCount=" + itemCheckCount + " size=" + items.size());

                if(hasJsonLinked) {
                    if(itemCheckCount == DB_FORCE_CHECK) {
                        //check if json not null
                        if(DebugUtil.isDebug())
                            info(tableName, db, TAG_initDatabase, "Forcing Database Check on Generic Items");

                        Collection<T> genericItems = new ArrayList<>();

                        for(T item : JsonHelperEx.findJsonElementsFromAssets(XUtil.getApk(context), jsonName,
                                stopOnFirstJson,
                                typeClass
                        )) {
                            boolean found = false;
                            for(T dItem : items) {
                                if(dItem.equals(item)) {
                                    found = true;
                                    genericItems.add(dItem);
                                    break;
                                }
                            }

                            if(!found) {
                                if(DebugUtil.isDebug())
                                    warning(tableName, db, TAG_initDatabase, "Missing Table Item=" + item);

                                ContentValues cv = new ContentValues();
                                item.populateContentValues(cv);
                                if(!db.insert(tableName, ensureProperContentValues(cv, columns))) {
                                    error(tableName, db, TAG_initDatabase, ERROR_INSERT + " item=" + item);
                                    continue;
                                }

                                genericItems.add(item);
                            }
                        }

                        db.setTransactionSuccessful();
                        return genericItems;
                    }
                    else if(itemCheckCount > 0 && items.size() < itemCheckCount) {
                        if(DebugUtil.isDebug())
                            error(tableName, db, TAG_initDatabase, "Size is off, table size=" + items.size() + " hardcoded size=" + itemCheckCount);


                        Collection<T> newItems = new ArrayList<>();

                        for(T item : JsonHelperEx.findJsonElementsFromAssets(
                                XUtil.getApk(context),
                                jsonName,
                                stopOnFirstJson,
                                typeClass
                        )) {
                            boolean found = false;
                            for(T dItem : items) {
                                if(dItem.equals(item)) {
                                    found = true;
                                    break;
                                }
                            }

                            if(!found) {
                                if(DebugUtil.isDebug())
                                    warning(tableName, db, TAG_initDatabase, "Missing Table Item=" + item);

                                ContentValues cv = new ContentValues();
                                item.populateContentValues(cv);

                                if(!db.insert(tableName, ensureProperContentValues(cv, columns))) {
                                    error(tableName, db, TAG_initDatabase, ERROR_INSERT + " item=" + item);
                                    continue;
                                }

                                newItems.add(item);
                            }
                        }

                        items.addAll(newItems);
                    }
                }
            }

            if(DebugUtil.isDebug()) {
                if(!CollectionUtil.isValid(items))
                    error(tableName, db, TAG_initDatabase, "Returning EMPTY List from Database Entries!");
                else
                    info(tableName, db, TAG_initDatabase, "Finished Loading Database Items Count=" + items.size());
            }

            db.setTransactionSuccessful();
            return items;
        }catch (Exception e) {
            error(tableName, db, TAG_initDatabase, e);
            return items;
        }finally {
            info(tableName, db, TAG_initDatabase, "Items Count=" + items.size());
            db.endTransaction(true, false);
            db.readUnlock();
        }
    }

    public static A_CODE ensureRead(SQLDatabase db, PacketBase packet, TableInfo tableInfo) {
        A_CODE code = PacketBase.ensurePacket(packet);
        if(code != A_CODE.NONE) {
            Log.e(TAG, "Failed to ensure Packet is valid! Table Name=" + tableInfo.name);
            return code;
        }

        if(db == null) {
            Log.e(TAG, "Database input for action is null! [0x1] ");
            return A_CODE.GENERIC_DB_ERROR_X;
        }

        boolean ret = DatabaseHelpEx.prepareDatabase(db, tableInfo);
        if(!ret) {
            Log.e(TAG, "Failed to Prepare Database / Table! Table Name=" + tableInfo.name);
            return A_CODE.GENERIC_DB_ERROR_X;
        }

        return A_CODE.NONE;
    }


    public static A_CODE ensureRead(SQLDatabase db, TableInfo tableInfo) {
        if(db == null) {
            Log.e(TAG, "Database input for action is null! [0x1] ");
            return A_CODE.GENERIC_DB_ERROR_X;
        }

        boolean ret = DatabaseHelpEx.prepareDatabase(db, tableInfo);
        if(!ret) {
            Log.e(TAG, "Failed to Prepare Database / Table! Table Name=" + tableInfo.name);
            return A_CODE.GENERIC_DB_ERROR_X;
        }

        return A_CODE.NONE;
    }


    public static boolean prepareDatabaseLocked(SQLDatabase db, TableInfo tableInfo) { return prepareDatabaseLocked(db, tableInfo.name, tableInfo.columns); }
    public static boolean prepareDatabaseLocked(SQLDatabase db, String tableName, LinkedHashMap<String, String> columns) {
        if(!DatabaseUtils.isReady(db))
            return false;

        if(!db.hasTable(tableName)) {
            warning(tableName, db, TAG_prepareTable, "Table is missing: " + tableName);
            if(!db.beginTransaction(true))
                return false;

            if(!db.createTable(tableName, columns)) {
                error(tableName, db, TAG_prepareTable, ERROR_TABLE);
                db.endTransaction(true, false);
                return false;
            }

            db.endTransaction(true, true);
        }

        return true;
    }

    public static boolean prepareDatabase(SQLDatabase db, TableInfo tableInfo) { return prepareDatabase(db, tableInfo.name, tableInfo.columns); }
    public static boolean prepareDatabase(SQLDatabase db, String tableName, LinkedHashMap<String, String> columns) {
        if(!DatabaseUtils.isReady(db))
            return false;

        if(!db.hasTable(tableName)) {
            warning(tableName, db, TAG_prepareTable, "Table is missing: " + tableName);
            if(!db.createTable(tableName, columns)) {
                error(tableName, db, TAG_prepareTable, ERROR_TABLE);
                return false;
            }
        }

        return true;
    }

    public static boolean deleteItem(SQLSnake query) { return deleteItem(query.getDatabase(), query.getTableName(), query.getWhereArgs(), query.getWhereClause()); }
    public static boolean deleteItem(SQLDatabase db, String tableName, String[] selectionArgs, String argValues) {
        return internalDeleteItem(db, tableName, selectionArgs, argValues) ||
                SQLSnake.create(db, tableName)
                                .setWhereArgs(selectionArgs)
                                .setWhereClause(argValues)
                                .asSnake()
                                .exists_lock();
    }

    private static boolean internalDeleteItem(SQLDatabase db, String tableName, String[] selectionArgs, String argValues) {
        try {
            if(!db.beginTransaction(true)) {
                error(tableName, db, TAG_DELETE_ITEM, ERROR_TRANSACTION);
                return false;
            }

            if(!db.hasTable(tableName)) {
                error(tableName, db, TAG_DELETE_ITEM, ERROR_TABLE);
                return false;
            }

            if(!db.delete(tableName, argValues, selectionArgs)) {
                error(tableName, db, TAG_DELETE_ITEM, ERROR_DELETE);
                return false;
            }

            db.setTransactionSuccessful();
            return true;
        }finally {
            db.endTransaction(true, false);
        }
    }

    public static <T extends IDatabaseEntry> boolean insertItem(SQLDatabase db, String tableName, T item) { return updateOrInsertItem(db, tableName,null, item, true); }
    public static <T extends IDatabaseEntry> boolean insertItem(SQLDatabase db, String tableName, T item, boolean prepareResult) { return updateOrInsertItem(db, tableName, null, item, prepareResult); }

    public static <T extends IDatabaseEntry> boolean updateOrInsertItem(
            SQLDatabase db,
            String tableName,
            SQLSnake query,
            T item,
            boolean prepareResult) {

        if(!prepareResult) {
            error(tableName, db, TAG_InsertUpdate, ERROR_PREPARE);
            return false;
        }

        if(!DatabaseUtils.isReady(db)) {
            error(tableName, db, TAG_InsertUpdate, ERROR_READY);
            return false;
        }

        info(tableName, db, TAG_InsertUpdate, "Updating Database Item=" + item);
        try {
            if(!db.beginTransaction(true)) {
                error(tableName, db, TAG_InsertUpdate, ERROR_TRANSACTION);
                return false;
            }

            if(!db.hasTable(tableName)) {
                error(tableName, db, TAG_InsertUpdate, ERROR_TABLE);
                return false;
            }

            if(query == null) {
                if(DebugUtil.isDebug())
                    info(tableName, db, TAG_InsertUpdate, "Inserting item=" + item);

                ContentValues cv = new ContentValues();
                item.populateContentValues(cv);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Content Values=" + Str.toStringOrNull(cv));

                if(!db.insert(tableName, cv)) {
                    error(tableName, db, TAG_InsertUpdate, ERROR_INSERT + " item=" + item);
                    return false;
                }

                if(DebugUtil.isDebug())
                    info(tableName, db, TAG_InsertUpdate, "Inserted item=" + item);

            }else {
                if(DebugUtil.isDebug())
                    info(tableName, db, TAG_InsertUpdate, "Updating item=" + item);

                ContentValues cv = new ContentValues();
                item.populateContentValues(cv);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Content Values=" + Str.toStringOrNull(cv));

                if(!db.update(tableName, cv, query)) {
                    error(tableName, db, TAG_InsertUpdate, ERROR_UPDATE + " item=" + item);
                    return false;
                }

                if(DebugUtil.isDebug())
                    info(tableName, db, TAG_InsertUpdate, "Updated item=" + item);
            }

            db.setTransactionSuccessful();
            return true;
        }finally {
            db.endTransaction(true, false);
        }
    }

    public static ContentValues ensureProperContentValues(ContentValues cv, Map<String, String> columns) {
        ContentValues copy = new ContentValues(cv);
        for(String ck : cv.keySet())
            if(!columns.containsKey(ck))
                copy.remove(ck);

        return copy;
    }

    private static void info(String tableName, SQLDatabase db, String methodName, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("PROGRESS\n");
        doLog(sb, tableName, db, methodName, message);
        Log.i(TAG, sb.toString());
    }

    private static void warning(String tableName, SQLDatabase db, String methodName, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("WARNING\n");
        doLog(sb, tableName, db, methodName, message);
        Log.w(TAG, sb.toString());
    }

    private static void error(String tableName, SQLDatabase db, String methodName, String message) { error(tableName, db, methodName, new Exception(message)); }
    private static void error(String tableName, SQLDatabase db, String methodName, Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append("ERROR");
        doLog(sb, tableName, db, methodName, e == null ? "nil" : e.getMessage());
        if(e != null) {
            sb.append("\nStack=\n");
            sb.append(Log.getStackTraceString(e));
        }

        Log.e(TAG, sb.toString());
    }

    private static void doLog(StringBuilder sb, String tableName, SQLDatabase db, String methodName, String message) {
        sb.append("Database=");
        sb.append(db.file.getAbsoluteFile());

        sb.append(" table=");
        sb.append(tableName);
        if (methodName != null) {
            sb.append("\nmethod=");
            sb.append(methodName);
        }

        if (message != null) {
            sb.append("\nmessage=");
            sb.append(message);
        }
    }
}
