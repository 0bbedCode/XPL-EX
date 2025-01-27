package eu.faircode.xlua.utilities;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Parcel;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.xstandard.interfaces.IJsonSerial;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.interfaces.IJsonType;
import eu.faircode.xlua.x.xlua.interfaces.IParcelType;

public class CursorUtil {
    private static final String TAG = "XLua.CursorUtil";

    // Vivo OAID Cursor Structure
    // URI: content://com.vivo.vms.IdProvider/IdentifierId/OAID


    // Cursor structure:
    // | key       | value           |
    // |-----------|-----------------|
    // | android_id| 3847563829457622|


    // Cursor structure:
    // | value                                |
    // |--------------------------------------|
    // | 5ecd7c57-8512-4935-a648-85cb8e3fb1a2 |

    public static MatrixCursor replaceValue(Cursor c, String newValue, String... possibleKeyNames) {
        MatrixCursor newCursor = new MatrixCursor(c.getColumnNames());
        int valIx = c.getColumnIndex("value");
        if (valIx == -1) {
            return (MatrixCursor) c;
        }

        int keyIx = c.getColumnIndex("key");
        if(keyIx == -1) {
            try {
                boolean replacedFirst = false;
                if(c.moveToFirst()) {
                    do {
                        MatrixCursor.RowBuilder rowBuilder = newCursor.newRow();
                        if(!replacedFirst) {
                            rowBuilder.add(newValue);
                            replacedFirst = true;
                        } else {
                            rowBuilder.add(c.getString(valIx));
                        }
                    } while (c.moveToNext());
                }
            }catch (Exception e) {
                Log.e(TAG, "Failed to copy value matrix cursor Single Dimension! e=" + e);
            } finally {
                newCursor.moveToFirst();
            }
        } else {
            try {
                //This is UNDER the assumption its KEY:VALUE (STRING)!!!!!
                Map<String, String> vals = new HashMap<>();
                if(c.moveToFirst()) {
                    do {
                        vals.put(c.getString(keyIx), c.getString(valIx));
                    } while (c.moveToNext());
                }

                if(vals.size() == 1) {
                    //JUST ASSUME FOR NOW FUCK ITT im tired this bullshit
                    for(Map.Entry<String, String> e : vals.entrySet()) {
                        MatrixCursor.RowBuilder rowBuilder = newCursor.newRow();
                        String kName = e.getKey();
                        rowBuilder.add(kName);
                        rowBuilder.add(newValue);
                        if(DebugUtil.isDebug())
                            Log.d(TAG, "Cursor Replacing at Key: " + kName + " Value: " + newValue);
                    }
                } else {
                    for(Map.Entry<String, String> e : vals.entrySet()) {
                        MatrixCursor.RowBuilder rowBuilder = newCursor.newRow();
                        String k = e.getKey();
                        rowBuilder.add(k);

                        boolean replaced = false;
                        for(String pk : possibleKeyNames) {
                            if(k.equalsIgnoreCase(pk)) {
                                replaced = true;
                                rowBuilder.add(newValue);
                                if(DebugUtil.isDebug())
                                    Log.d(TAG, "Cursor Replacing at Key: " + k + " Value: " + newValue);
                                break;
                            }
                        }

                        if(!replaced) {
                            rowBuilder.add(e.getValue());
                        }
                    }
                }
            }catch (Exception e) {
                Log.e(TAG, "Failed to copy key value matrix cursor! e=" + e);
            } finally {
                newCursor.moveToFirst();
            }
        }

        return newCursor;
    }



    public static MatrixCursor copyKeyValue(Cursor c, String replaceKeyName, String newValue) {
        MatrixCursor newCursor = new MatrixCursor(c.getColumnNames());
        int keyIx = c.getColumnIndex("key");
        int valIx = c.getColumnIndex("value");

        if (keyIx == -1 || valIx == -1) {
            // Log error or throw an exception: "Key or value column not found."
            return newCursor;
        }

        try {
            if (c.moveToFirst()) {
                do {
                    MatrixCursor.RowBuilder rowBuilder = newCursor.newRow();
                    String kName = c.getString(keyIx);
                    //if (kName == null) continue;
                    rowBuilder.add(kName);
                    if (kName != null && kName.equalsIgnoreCase(replaceKeyName)) {
                        rowBuilder.add(newValue);
                    } else {
                        rowBuilder.add(c.getString(valIx));
                    }
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to copy key value matrix cursor! e=" + e);
        } finally {
            newCursor.moveToFirst(); // Reset cursor to the beginning before returning.
        }

        return newCursor;
    }

    public static void closeCursor(Cursor c) {
        if(c != null) {
            try {
                c.close();
            }catch (Throwable e) {

            }
        }
    }

    public static Long getLong(Cursor c, String columnName) {return getLong(c, columnName, null); }
    public static Long getLong(Cursor c, String columnName, Long defaultValue) {
        int ix = c.getColumnIndex(columnName);
        if(ix == -1) return defaultValue;

        //if(c.getType(ix) == Cursor.)
        //    return c.getInt(ix);

        return c.getLong(ix);
    }

    public static Integer getInteger(Cursor c, String columnName) { return getInteger(c, columnName, null); }
    public static Integer getInteger(Cursor c, String columnName, Integer defaultValue) {
        int ix = c.getColumnIndex(columnName);
        if(ix == -1) return defaultValue;

        if(c.getType(ix) == Cursor.FIELD_TYPE_INTEGER)
            return c.getInt(ix);

        return defaultValue;
    }

    public static String getString(Cursor c, String columnName) { return getString(c, columnName, null); }
    public static String getString(Cursor c, String columnName, String defaultValue) {
        int ix = c.getColumnIndex(columnName);
        if(ix < 0)
            return defaultValue;

        return c.getString(ix);
    }

    public static Boolean getBoolean(Cursor c, String columnName, Boolean defaultValue) {
        int ix = c.getColumnIndex(columnName);
        if(ix == -1) return defaultValue;
        String v = c.getString(ix).toLowerCase();
        Boolean parsed = Str.toBoolean(v);
        return parsed == null ? defaultValue : parsed;
    }

    public static Boolean getBoolean(Cursor c, String columnName) {
        int ix = c.getColumnIndex(columnName);
        if(ix == -1) return null;
        String v = c.getString(ix).toLowerCase();
        if(v.equals("true"))
            return true;
        if(v.equals("false"))
            return false;

        try {
            return Integer.parseInt(v) == 1;
        }catch (NumberFormatException e) {
            return null;
        }
    }

    public static Map<String, String> toDictionary(Cursor c, boolean close) {
        final Map<String, String> dic = new HashMap<>();

        try {
            if(c != null) {
                while (c.moveToNext())
                    dic.put(c.getString(0), c.getString(1));
            }
        }finally {
            if(close && c != null)
                 closeCursor(c);
        }

        return dic;
    }


    /*public static <T extends IParcelType> Cursor toMatrixCursor_final(Collection<T> items, boolean marshall, int flags)  {
        MatrixCursor result = new MatrixCursor(new String[]{marshall ? "blob" : "json"});
        if(!CollectionUtil.isValid(items)) {
            Log.w(TAG, "Collection passed to Convert into Matrix Cursor was Null or Empty...");
            return result;
        }

        IParcelType lastItem = null;
        String logEnd = " collection size=" + items.size() + " marshall=" + marshall + " flags=" + flags;
        if(DebugUtil.isDebug())
            Log.d(TAG, "Converting collection into Matrix Cursor..." + logEnd);

        int pos = 0;
        try {
            for (IParcelType item : items) {
                lastItem = item;
                if (marshall) {
                    Parcel parcel = Parcel.obtain();
                    item.writeToParcel(parcel, flags);
                    result.newRow().add(parcel.marshall());
                    parcel.recycle();
                } else {
                    //String jData = item.toJSON();
                    //if(jData == null) continue;
                    //result.addRow(new Object[]{ jData });
                } pos++;
            }
        //}catch (JSONException je) {
        //    XLog.e("Failed to write IJsonSerial object as Json Blob using IJsonSerial inherited method [toJSON]. last position=" + pos + " last item=" + lastItem + logEnd, je, true);
        //    return result;
        }catch (Exception e) {
            XLog.e("Failed to write IJsonSerial Collection to Matrix Cursor. last position=" + pos + " last item=" + lastItem + logEnd, e, true);
            return result;
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Finished Writing IJsonSerial Collection to Matrix Cursor. last position=" + pos + logEnd);

        return result;
    }*/

    public static <T extends IJsonSerial> Cursor toMatrixCursor(Collection<T> items, boolean marshall, int flags)  {
        MatrixCursor result = new MatrixCursor(new String[]{marshall ? "blob" : "json"});
        if(!CollectionUtil.isValid(items)) {
            XLog.w("Collection passed to Convert into Matrix Cursor was Null or Empty...");
            return result;
        }

        IJsonSerial lastItem = null;
        String logEnd = " collection size=" + items.size() + " marshall=" + marshall + " flags=" + flags;
        XLog.i("Converting collection into Matrix Cursor... " + logEnd);
        int pos = 0;
        try {
            for (IJsonSerial item : items) {
                lastItem = item;
                if (marshall) {
                    Parcel parcel = Parcel.obtain();
                    item.writeToParcel(parcel, flags);
                    result.newRow().add(parcel.marshall());
                    parcel.recycle();
                } else {
                    String jData = item.toJSON();
                    if(jData == null) continue;
                    result.addRow(new Object[]{ jData });
                } pos++;
            }
        }catch (JSONException je) {
            XLog.e("Failed to write IJsonSerial object as Json Blob using IJsonSerial inherited method [toJSON]. last position=" + pos + " last item=" + lastItem + logEnd, je, true);
            return result;
        }catch (Exception e) {
            XLog.e("Failed to write IJsonSerial Collection to Matrix Cursor. last position=" + pos + " last item=" + lastItem + logEnd, e, true);
            return result;
        }

        XLog.i("Finished Writing IJsonSerial Collection to Matrix Cursor. last position=" + pos + logEnd);
        return result;
    }

    public static <T extends IJsonSerial> Collection<T> readCursorAs(Cursor cursor, boolean marshall, Class<T> classType) {
        Collection<T> items = new ArrayList<>();
        if(classType == null) {
            XLog.e("IJsonSerial Class input is null, failed to read cursor to collection. marshall=" + marshall, new Throwable("ouch"), true);
            return items;
        }

        int pos = 0;
        try {
            if(cursor != null && !cursor.isClosed() && cursor.moveToFirst()) {
                XLog.i("Reading Matrix Cursor to a IJsonSerial Collection. marshall=" + marshall + " class type=" + classType.getName());
                if (marshall) {
                    do {
                        T inst = classType.newInstance();
                        byte[] marshaled = cursor.getBlob(0);
                        Parcel parcel = Parcel.obtain();
                        parcel.unmarshall(marshaled, 0, marshaled.length);
                        parcel.setDataPosition(0);
                        inst.fromParcel(parcel);
                        parcel.recycle();
                        items.add(inst);
                        pos++;
                    }while (cursor.moveToNext());
                } else {
                    do {
                        T inst = classType.newInstance();
                        String json = cursor.getString(0);
                        inst.fromJSONObject(new JSONObject(json));
                        items.add(inst);
                        pos++;
                    }while (cursor.moveToNext());
                }
            }else {
                XLog.e("Cursor is Empty, Closed or Null... failed to read cursor to collection. marshall=" + marshall + " class type=" + classType.getName(), new Throwable("ouch"), true);
                return items;
            }
        }catch (JSONException je) {
            XLog.e("Failed to Read the Json Blob to IJsonSerial Object. Function [fromJSONObject] failed for IJsonSerial class. last position=" + pos + " total elements read=" + items.size() + " class=" + classType.getName());
            return items;
        }catch (IllegalAccessException ie) {
            XLog.e("Failed to Construct IJsonSerial class to Read Cursor blob! last position=" + pos + " total elements read=" + items.size() + " class type=" + classType.getName(), ie, true);
            return items;
        } catch (Exception e) {
            XLog.e("Failed to Read Cursor as IJsonSerial Collection! last position=" + pos + " total elements read=" + items.size() + " class type=" + classType.getName(), e, true);
            return items;
        }

        XLog.i("Finished Reading Cursor to IJsonSerial Collection. Total elements read=" + items.size() + " last position=" + pos);
        return items;
    }


    public static <T extends IParcelType & IJsonType> Cursor toMatrixCursor_final(Collection<T> items, boolean marshall, int flags)  {
        MatrixCursor result = new MatrixCursor(new String[]{marshall ? "blob" : "json"});
        if(!CollectionUtil.isValid(items)) {
            Log.e(TAG, "Failed to Convert Collection to Cursor! Collection passed is Null or Empty!");
            return result;
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Converting Collection to Cursor, Count=" + ListUtil.size(items));

        try {
            for (T item : items) {
                if (marshall) {
                    Parcel parcel = Parcel.obtain();
                    item.writeToParcel(parcel, flags);
                    result.newRow().add(parcel.marshall());
                    parcel.recycle();
                } else {
                    String jData = item.toJSONString();
                    if(jData == null)
                        continue;

                    result.addRow(new Object[]{ jData });
                }
            }
        }catch (JSONException je) {
            Log.e(TAG, Str.fm("Failed to Write Collection to Cursor JSON, Error=%s Stack=%s", je, RuntimeUtils.getStackTraceSafeString(je)));
            return result;
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to Write Collection to Cursor, Error=%s Stack=%s", e, RuntimeUtils.getStackTraceSafeString(e)));
            return result;
        }

        return result;
    }


    public static <T extends IParcelType & IJsonType> Collection<T> readCursorAs_final(Cursor cursor, boolean marshall, Class<T> classType) {
        Collection<T> items = new ArrayList<>();
        if(classType == null) {
            Log.e(TAG, "Failed to Read JSON Object Cursor, Class Type is null! Stack=" + RuntimeUtils.getStackTraceSafeString());
            return items;
        }

        //int pos = 0;
        try {
            if(cursor != null && !cursor.isClosed() && cursor.moveToFirst()) {
                //XLog.i("Reading Matrix Cursor to a IJsonSerial Collection. marshall=" + marshall + " class type=" + classType.getName());
                if (marshall) {
                    do {
                        T inst = classType.newInstance();
                        byte[] marshaled = cursor.getBlob(0);
                        Parcel parcel = Parcel.obtain();
                        parcel.unmarshall(marshaled, 0, marshaled.length);
                        parcel.setDataPosition(0);
                        inst.fromParcel(parcel);
                        parcel.recycle();
                        items.add(inst);
                        //pos++;
                    }while (cursor.moveToNext());
                } else {
                    do {
                        T inst = classType.newInstance();
                        String json = cursor.getString(0);
                        inst.fromJSONObject(new JSONObject(json));
                        items.add(inst);
                        //pos++;
                    }while (cursor.moveToNext());
                }
            }else {
                Log.e(TAG, Str.fm("Cursor is Empty or Null failed to Read, Marshall=%s Class=%s Stack=%s", marshall, classType, RuntimeUtils.getStackTraceSafeString()));
                return items;
            }
        }catch (JSONException je) {
            Log.e(TAG, Str.fm("Error Reading Objects from Cursor JSON, Error=%s Class=%s Stack=%s", je, classType, RuntimeUtils.getStackTraceSafeString(je)));
            return items;
        } catch (Exception e) {
            Log.e(TAG, Str.fm("Error Reading Objects from Cursor JSON, Error=%s Marshall=%s Class=%s Stack=%s", e, marshall, classType, RuntimeUtils.getStackTraceSafeString(e)));
            return items;
        }

        return items;
    }
}
