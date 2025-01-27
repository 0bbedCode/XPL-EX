package eu.faircode.xlua.api.xstandard.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.api.xstandard.interfaces.IDBSerial;
import eu.faircode.xlua.api.xstandard.interfaces.IJCompare;
import eu.faircode.xlua.api.xstandard.interfaces.IJsonSerial;
import eu.faircode.xlua.api.xstandard.JsonHelper;
import eu.faircode.xlua.utilities.CollectionUtil;
import eu.faircode.xlua.utilities.StringUtil;

public class DatabaseHelp {
    private static final String TAG = "XLua.DatabaseHelper";
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

    public static boolean prepareDatabase(XDatabaseOld db, String tableName, LinkedHashMap<String, String> columns) {
        if(!XDatabaseOld.isReady(db))
            return false;

        if(!db.hasTable(tableName)) {
            warning(tableName, db, TAG_prepareTable, "Table is missing: " + tableName);
            if(!db.createTable(columns, tableName)) {
                error(tableName, db, TAG_prepareTable, ERROR_TABLE);
                return false;
            }
        }

        return true;
    }

    public static boolean deleteItem(
            SqlQuerySnake query) {

        return deleteItem(query.db, query.tableName, query.getSelectionCompareValues(), query.getSelectionArgs());
    }

    public static boolean deleteItem(
            XDatabaseOld db,
            SqlQuerySnake query) {

        return deleteItem(db, query.tableName, query.getSelectionCompareValues(), query.getSelectionArgs());
    }

    public static boolean deleteItem(
            XDatabaseOld db,
            String tableName,
            SqlQuerySnake query) {

        return deleteItem(db, tableName, query.getSelectionCompareValues(), query.getSelectionArgs());
    }


    public static boolean deleteItem(
            XDatabaseOld db,
            String tableName,
            String[] selectionArgs,
            String argValues) {

        return internalDeleteItem(db, tableName, selectionArgs, argValues) ||
                SqlQuerySnake.create(db, tableName)
                .setSelectionArgs(selectionArgs)
                .setCompareArgs(argValues)
                .exists();
    }

    private static boolean internalDeleteItem(
            XDatabaseOld db,
            String tableName,
            String[] selectionArgs,
            String argValues) {

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

    public static <T extends IDBSerial>
        boolean updateItems(
                XDatabaseOld db,
                String tableName,
                Collection<T> items,
                SqlQuerySnake query) {

            return insertOrUpdateItems(db, tableName, items, query, true);
    }

    public static <T extends IDBSerial>
        boolean updateItems(
                XDatabaseOld db,
                String tableName,
                Collection<T> items,
                SqlQuerySnake query,
                boolean prepareResult) {

        return insertOrUpdateItems(db, tableName, items, query, prepareResult);
    }

    public static <T extends IDBSerial>
        boolean insertItems(
                XDatabaseOld db,
                String tableName,
                Collection<T> items) {

        return insertOrUpdateItems(db, tableName, items, null, true);
    }

    public static <T extends IDBSerial>
        boolean insertItems(
                XDatabaseOld db,
                String tableName,
                Collection<T> items,
                boolean prepareResult) {

        return insertOrUpdateItems(db, tableName, items, null, prepareResult);
    }

    public static <T extends IDBSerial>
        boolean insertOrUpdateItems(
                XDatabaseOld db,
                String tableName,
                Collection<T> items,
                SqlQuerySnake query,
                boolean prepareResult) {

        //Updating wont work for now
        //need to make a 'query' for all the objects
        if(!prepareResult) {
            error(tableName, db, TAG_InsertUpdateS, ERROR_PREPARE);
            return false;
        }

        if(!XDatabaseOld.isReady(db)) {
            error(tableName, db, TAG_InsertUpdateS, ERROR_READY);
            return false;
        }

        info(tableName, db, TAG_InsertUpdateS, "Updating Database items count=" + items.size());

        try {
            if(!db.beginTransaction(true)) {
                error(tableName, db, TAG_InsertUpdateS, ERROR_TRANSACTION);
                return false;
            }

            if(!db.hasTable(tableName)) {
                error(tableName, db, TAG_InsertUpdateS, ERROR_TABLE);
                return false;
            }

            if(query != null) {
                for(T item : items) {
                    if(!db.update(tableName, item.createContentValues(), query))
                        error(tableName, db, TAG_InsertUpdateS, ERROR_UPDATE + " item=" + item);
                }
            }else {
                for(T item : items) {
                    if(!db.insert(tableName, item.createContentValues()))
                        error(tableName, db, TAG_InsertUpdateS, ERROR_INSERT + " item=" + item);
                }
            }

            db.setTransactionSuccessful();
            return true;
        }finally {
            db.endTransaction(true, false);
        }
    }

    public static <T extends IDBSerial>
        boolean updateItem(
                SqlQuerySnake snake,
                T item) {

        return updateOrInsertItem(snake.db, snake.tableName, snake, item, true);
    }

    public static <T extends IDBSerial>
        boolean updateItem(
                XDatabaseOld database,
                SqlQuerySnake snake,
                T item) {

        return updateOrInsertItem(database, snake.tableName, snake, item, true);
    }

    public static <T extends IDBSerial>
        boolean updateItem(
                XDatabaseOld database,
                String tableName,
                SqlQuerySnake snake,
                T item) {

        return updateOrInsertItem(database, tableName, snake, item, true);
    }

    public static <T extends IDBSerial>
        boolean updateItem(
                XDatabaseOld database,
                String tableName,
                SqlQuerySnake snake,
                T item,
                boolean prepareResult) {

        return updateOrInsertItem(database, tableName, snake, item, prepareResult);
    }

    public static <T extends IDBSerial>
        boolean insertItem(
                XDatabaseOld db,
                String tableName,
                T item) {
        return updateOrInsertItem(db, tableName,null, item, true);
    }

    public static <T extends IDBSerial>
        boolean insertItem(
                XDatabaseOld db,
                String tableName,
                T item,
                boolean prepareResult) {
        return updateOrInsertItem(db, tableName, null, item, prepareResult);
    }

    public static <T extends IDBSerial>
        boolean updateOrInsertItem(
                XDatabaseOld db,
                String tableName,
                SqlQuerySnake query,
                T item,
                boolean prepareResult) {

        if(!prepareResult) {
            error(tableName, db, TAG_InsertUpdate, ERROR_PREPARE);
            return false;
        }

        if(!XDatabaseOld.isReady(db)) {
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
                info(tableName, db, TAG_InsertUpdate, "Inserting item=" + item);
                if(!db.insert(tableName, item.createContentValues())) {
                    error(tableName, db, TAG_InsertUpdate, ERROR_INSERT + " item=" + item);
                    return false;
                }

                info(tableName, db, TAG_InsertUpdate, "Inserted item=" + item);
            }else {
                info(tableName, db, TAG_InsertUpdate, "Updating item=" + item);
                if(!db.update(tableName, item.createContentValues(), query)) {
                    error(tableName, db, TAG_InsertUpdate, ERROR_UPDATE + " item=" + item);
                    return false;
                }

                info(tableName, db, TAG_InsertUpdate, "Updated item=" + item);
            }

            db.setTransactionSuccessful();
            return true;
        }finally {
            db.endTransaction(true, false);
        }
    }

    public static <T extends IJsonSerial>
        boolean prepareTableIfMissingOrInvalidCount (
                Context context,
                XDatabaseOld db,
                String tableName,
                Map<String, String> columns,
                Class<T> typeClass) {
        return prepareTableIfMissingOrInvalidCount(context, db, tableName, columns, null, false, typeClass, 0);
    }

    public static <T extends IJsonSerial>
        boolean prepareTableIfMissingOrInvalidCount(
                Context context,
                XDatabaseOld db,
                String tableName,
                Map<String, String> columns,
                String jsonName,
                boolean stopOnFirstJson,
                Class<T> typeClass,
                int itemCheckCount) {

        //Do note this will not work in HOOK context
        if(!XDatabaseOld.isReady(db)) {
            error(tableName, db, TAG_prepareTable, ERROR_READY);
            return false;
        }

        if(!db.hasTable(tableName)) {
            warning(tableName, db, TAG_prepareTable, "Table is missing: " + tableName);
            if(!db.createTable(columns, tableName)) {
                error(tableName, db, TAG_prepareTable, ERROR_TABLE);
                return false;
            }
        }

        if(itemCheckCount == DB_FORCE_SKIP_CHECK) {
            info(tableName, db, TAG_prepareTable, "Its Ready to Go!");
            return true;
        }

        if(jsonName == null || (itemCheckCount < 1 && itemCheckCount != DB_FORCE_CHECK)) {
            //Check if Table Exist if the given data to check if NULL or below (1) assuming not (-8)
            return db.hasTable(tableName);
        }

        if(itemCheckCount == DB_FORCE_CHECK) {
            info(tableName, db, TAG_prepareTable, "Forcing Table Check");
            Collection<T> items = getOrInitTable(
                    context,
                    db,
                    tableName,
                    columns,
                    jsonName,
                    stopOnFirstJson, typeClass, itemCheckCount);

            if(!CollectionUtil.isValid(items)) {
                error(tableName, db, TAG_prepareTable, "Failed to grab Database Items");
                return false;
            }

            int jCount = JsonHelper.findJsonElementsFromAssets(
                    XUtil.getApk(context),
                    jsonName,
                    stopOnFirstJson,
                    typeClass).size();
            //ensure only required items from the force is being returned
            //It now is updated the init function to check for FORCE flag and handle it properly
            info(tableName, db, TAG_prepareTable, "json count=" + jCount + " returned count=" + items.size());
            return jCount == items.size();
        }

        if(db.tableEntries(tableName) < itemCheckCount) {
            warning(tableName, db, TAG_prepareTable, "Table is Empty or NULL Fixing to=" + itemCheckCount);
            Collection<T> items = getOrInitTable(
                    context,
                    db,
                    tableName,
                    columns,
                    jsonName, stopOnFirstJson, typeClass, itemCheckCount);

            if(!CollectionUtil.isValid(items)) {
                error(tableName, db, TAG_prepareTable, "Failed to grab Database Items");
                return false;
            }

            int itemCount = db.tableEntries(tableName);
            if(itemCount < itemCheckCount) {
                error(tableName, db, TAG_prepareTable, "Size is off, table size=" + itemCount + " hardcoded size=" + itemCheckCount);
                return false;
            }
        }

        info(tableName, db, TAG_prepareTable, "Its Ready to Go!");
        return true;
    }

    public static <T extends IJCompare> boolean compareJsonWithDatabase(
            Context context,
            XDatabaseOld db,
            String tableName,
            Map<String, String> columns,
            String jsonName,
            Class<T> typeClass,
            boolean prepareIfNot) {

        if(!XDatabaseOld.isReady(db)) {
            error(tableName, db, TAG_initDatabase, ERROR_READY);
            return false;
        }

        try {
            db.readLock();
            boolean hasCheck = !db.hasTable(tableName) || db.tableIsEmpty(tableName);
            db.readUnlock();
            if(hasCheck) {
                if(prepareIfNot) return prepareTableIfMissingOrInvalidCount(context, db, tableName, columns, jsonName, true, typeClass, DB_FORCE_CHECK);
                return false;
            }

            List<T> dbCompareItems = new ArrayList<>(getOrInitTable(context, db, tableName, columns, jsonName, true, typeClass, DB_FORCE_CHECK));
            List<Boolean> statuses = new ArrayList<>();
            statuses.add(true);
            for(T item : JsonHelper.findJsonElementsFromAssets(XUtil.getApk(context), jsonName, true, typeClass)) {
               boolean found = false;
                for(T s : dbCompareItems) {
                   if(s.equalsPartner(item)) {
                       found = true;
                       if(!s.equalsPartnerContents(item)) {
                           statuses.add(insertItem(db, tableName, item, true));
                           break;
                       }
                   }
               }

                if(found) continue;
                statuses.add(insertItem(db, tableName, item, true));
            }

            info(tableName, db, TAG_JSON, "Database Json Comparison Check Finished Total Modified=" + (statuses.size() - 1));
            return CollectionUtil.isAllTrue(statuses);
        }catch (Exception e) {
            error(tableName, db, TAG_JSON, e);
            return false;
        }finally {
            info(tableName, db, TAG_JSON, "Database Json Comparison Check Completed");
        }
    }

    public static <TFrom extends IJsonSerial, TAs extends IJsonSerial> Collection<TAs> initDatabaseLists(
            Context context,
            XDatabaseOld db,
            String tableName,
            Map<String, String> columns,
            String jsonName,
            boolean stopOnFirstJson,
            Class<TFrom> typeClassFrom,
            Class<TAs> typeClassAs,
            boolean forceCheckElements) {  return initDatabaseLists(context, db, tableName, columns, jsonName, stopOnFirstJson, typeClassFrom, typeClassAs, forceCheckElements ? DB_FORCE_CHECK : -1); }

    public static <TJSONFrom extends IJsonSerial, TAs extends IJsonSerial> Collection<TAs> initDatabaseLists(
            Context context,
             XDatabaseOld db,
             String tableName,
             Map<String, String> columns,
             String jsonName,
             boolean stopOnFirstJson,
             Class<TJSONFrom> typeClassFrom,
             Class<TAs> typeClassAs,
             int itemCheckCount) {
        Collection<TAs> items = new ArrayList<>();
        if(!XDatabaseOld.isReady(db)) {
            error(tableName, db, TAG_initDatabase, ERROR_READY);
            return items;
        }

        Log.i(TAG, "Database is ready=" + db + " table=" + tableName);
        if(!db.beginTransaction(true)) {
            error(tableName, db, TAG_initDatabase, ERROR_TRANSACTION);
            return items;
        }

        //Write lock and Read lock ?
        db.readLock();

        try {
            if(!db.hasTable(tableName) || db.tableIsEmpty(tableName)) {
                warning(tableName, db, TAG_initDatabase, "Table is Empty or NULL");

                if(!db.createTable(columns, tableName)) {
                    error(tableName, db, TAG_initDatabase, ERROR_TABLE);
                    return items;
                }

                for(TJSONFrom item : JsonHelper.findJsonElementsFromAssets(XUtil.getApk(context), jsonName, stopOnFirstJson, typeClassFrom
                )) {
                    List<ContentValues> listContent = item.createContentValuesList();
                    if(listContent != null) {
                        for(ContentValues c : listContent) {
                            if(!db.insert(tableName, ensureProperContentValues(c, columns))) {
                                error(tableName, db, TAG_initDatabase, ERROR_INSERT + " item=" + item);
                                continue;
                            }

                            TAs tAsElement = typeClassAs.newInstance();
                            tAsElement.fromContentValues(c);
                            items.add(tAsElement);
                        }
                    }
                }
            }else {
                items = getFromDatabase(db, tableName, typeClassAs);
                info(tableName, db, TAG_initDatabase, "itemCheckCount=" + itemCheckCount + " size=" + items.size());

                if(itemCheckCount == DB_FORCE_CHECK) {
                    info(tableName, db, TAG_initDatabase, "Forcing Database Check on Generic Items");
                    List<TAs> existingPropsCopy = new ArrayList<>(items);
                    int totalSize = 0;
                    int found = 0;

                    for(TJSONFrom item : JsonHelper.findJsonElementsFromAssets(XUtil.getApk(context), jsonName, stopOnFirstJson, typeClassFrom)) {
                        List<ContentValues> jItemCvList = item.createContentValuesList();
                        if(!CollectionUtil.isValid(jItemCvList))
                            continue;

                        totalSize += jItemCvList.size();

                        for(ContentValues cv : jItemCvList) {
                            TAs tCopy = typeClassAs.newInstance();
                            tCopy.fromContentValues(ensureProperContentValues(cv, columns));

                            boolean foundOrCreated = false;
                            for(int i = existingPropsCopy.size() - 1; i >= 0; i--) {
                                if(existingPropsCopy.get(i).equals(tCopy)) {
                                    foundOrCreated = true;
                                    existingPropsCopy.remove(i);//should we remove ?
                                    found++;
                                    break;
                                }
                            }

                            if (!foundOrCreated) {
                                warning(tableName, db, TAG_initDatabase, "Missing Table Item=" + item);
                                if(!db.insert(tableName, ensureProperContentValues(cv, columns))) {
                                    error(tableName, db, TAG_initDatabase, ERROR_INSERT + " item=" + item);
                                    continue;
                                }

                                items.add(tCopy);
                                found++;
                            }
                        }
                    }

                    info(tableName, db, TAG_initDatabase, "Found=" + found + " needed size=" + totalSize);
                }
            }

            if(!CollectionUtil.isValid(items))
                error(tableName, db, TAG_initDatabase, "Returning EMPTY List from Database Entries!");

            info(tableName, db, TAG_initDatabase, "Finished Loading Database Items Count=" + items.size());
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

    public static  <T extends IJsonSerial> Collection<T> getOrInitTable(
            Context context,
            XDatabaseOld db,
            String tableName,
            Map<String, String> columns,
            Class<T> typeClass) { return getOrInitTable(context, db, tableName, columns, null, false, typeClass, 0); }

    public static  <T extends IJsonSerial> Collection<T> getOrInitTable(
            Context context,
            XDatabaseOld db,
            String tableName,
            Map<String, String> columns,
            String jsonName,
            boolean stopOnFirstJson,
            Class<T> typeClass,
            boolean forCheckElements) { return getOrInitTable(context, db, tableName, columns, jsonName, stopOnFirstJson, typeClass, forCheckElements ? DB_FORCE_CHECK : -1); }

    public static  <T extends IJsonSerial> Collection<T> getOrInitTable(
            Context context,
            XDatabaseOld db,
            String tableName,
            Map<String, String> columns,
            String jsonName,
            boolean stopOnFirstJson,
            Class<T> typeClass,
            int itemCheckCount) {

        Collection<T> items = new ArrayList<>();

        Log.i(TAG, "init tableName=" + tableName + " count=" + itemCheckCount + " db=" + db);

        if(!XDatabaseOld.isReady(db)) {
            error(tableName, db, TAG_initDatabase, ERROR_READY);
            return items;
        }

        Log.i(TAG, "Database is ready=" + db + " table=" + tableName);
        if(!db.beginTransaction(true)) {
            error(tableName, db, TAG_initDatabase, ERROR_TRANSACTION);
            return items;
        }

        jsonName = StringUtil.trimEnsureEnd(jsonName, ".json");
        boolean hasJsonLinked = StringUtil.isValidString(jsonName);
        db.readLock();

        try {
            if(!db.hasTable(tableName) || db.tableIsEmpty(tableName)) {
                warning(tableName, db, TAG_initDatabase, "Table is Empty or NULL");

                if(!db.createTable(columns, tableName)) {
                    error(tableName, db, TAG_initDatabase, ERROR_TABLE);
                    return items;
                }

                if(hasJsonLinked) {
                    for(T item : JsonHelper.findJsonElementsFromAssets(
                            XUtil.getApk(context),
                            jsonName,
                            stopOnFirstJson,
                            typeClass
                    )) {
                        if(!db.insert(tableName, ensureProperContentValues(item.createContentValues(), columns))) {
                            error(tableName, db, TAG_initDatabase, ERROR_INSERT + " item=" + item);
                            continue;
                        }

                        items.add(item);
                    }
                }
            }else {
                items = getFromDatabase(db, tableName, typeClass);
                info(tableName, db, TAG_initDatabase, "itemCheckCount=" + itemCheckCount + " size=" + items.size());
                if(hasJsonLinked) {
                    if(itemCheckCount == DB_FORCE_CHECK) {
                        //check if json not null
                        info(tableName, db, TAG_initDatabase, "Forcing Database Check on Generic Items");
                        Collection<T> genericItems = new ArrayList<>();

                        for(T item : JsonHelper.findJsonElementsFromAssets(XUtil.getApk(context), jsonName,
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
                                warning(tableName, db, TAG_initDatabase, "Missing Table Item=" + item);
                                if(!db.insert(tableName, ensureProperContentValues(item.createContentValues(), columns))) {
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
                        error(tableName, db, TAG_initDatabase, "Size is off, table size=" + items.size() + " hardcoded size=" + itemCheckCount);

                        Collection<T> newItems = new ArrayList<>();

                        for(T item : JsonHelper.findJsonElementsFromAssets(
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
                                warning(tableName, db, TAG_initDatabase, "Missing Table Item=" + item);
                                if(!db.insert(tableName, ensureProperContentValues(item.createContentValues(), columns))) {
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

            if(!CollectionUtil.isValid(items))
                error(tableName, db, TAG_initDatabase, "Returning EMPTY List from Database Entries!");
            else
                info(tableName, db, TAG_initDatabase, "Finished Loading Database Items Count=" + items.size());

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

    public static ContentValues ensureProperContentValues(ContentValues cv, Map<String, String> columns) {
        ContentValues copy = new ContentValues(cv);
        for(String ck : cv.keySet())
            if(!columns.containsKey(ck))
                copy.remove(ck);

        return copy;
    }

    public static <T extends IDBSerial> Collection<T> getFromDatabase(
            XDatabaseOld db,
            String tableName,
            Class<T> typeClass) {

        Collection<T> items = new ArrayList<>();
        Log.e(TAG, "[getFromDatabase] table=" + tableName + " db=" + db);
        try {
            Cursor c= db.getDatabase().rawQuery("SELECT * FROM " + tableName, null);
            while (c.moveToNext()) {
                T item = typeClass.newInstance();
                item.fromCursor(c);
                items.add(item);
            }
        }catch (Exception e) {
            Log.e(TAG, "[getFromDatabase] error , table=" + tableName + " db=" + db);
        }

        Log.i(TAG, "items in db=" + items.size());
        return items;
    }

    private static void info(String tableName, XDatabaseOld db, String methodName, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("PROGRESS\n");
        doLog(sb, tableName, db, methodName, message);
        Log.i(TAG, sb.toString());
    }

    private static void warning(String tableName, XDatabaseOld db, String methodName, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("WARNING\n");
        doLog(sb, tableName, db, methodName, message);
        Log.w(TAG, sb.toString());
    }

    private static void error(String tableName, XDatabaseOld db, String methodName, String message) { error(tableName, db, methodName, new Exception(message)); }
    private static void error(String tableName, XDatabaseOld db, String methodName, Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append("ERROR");
        doLog(sb, tableName, db, methodName, e == null ? "nil" : e.getMessage());
        if(e != null) {
            sb.append("\nStack=\n");
            sb.append(Log.getStackTraceString(e));
        }

        Log.e(TAG, sb.toString());
    }

    private static void doLog(StringBuilder sb, String tableName, XDatabaseOld db, String methodName, String message) {
        sb.append("Database=");
        sb.append(db.getPath());
        sb.append("::");
        sb.append(db.getName());


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
