package eu.faircode.xlua.utilities;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import eu.faircode.xlua.x.xlua.interfaces.IJsonType;

public class JSONUtil {

    private static final String TAG = "XLua.JSONUtil";


    //@Override
    //public void fromJSONObject(JSONObject obj) throws JSONException

    public static String objectToString(JSONObject o) {
        try {
            return o.toString();
        }catch (Exception ignored) { }
        return null;
    }

    public static JSONObject toObject(IJsonType o) {
        try {
            return o.toJSONObject();
        }catch (Exception ignore) { }
        return null;
    }

    public static void fromObject(JSONObject obj, IJsonType o) {
        try {
            if(o != null && obj != null)
                o.fromJSONObject(obj);
        }catch (Throwable ignore) { }
    }

    public static JSONObject objectFromString(String s) {
        try {
            return new JSONObject(s);
        }catch (Throwable ignore) { }
        return null;
    }

    public static String getString(JSONObject obj, String jsonFieldName) { return getString(obj, jsonFieldName, null); }
    public static String getString(JSONObject obj, String jsonFieldName, String defaultValue) {
        if(!StringUtil.isValidString(jsonFieldName))
            return defaultValue;

        try {
            return obj.getString(jsonFieldName);
        }catch (JSONException ex) {
            Log.e(TAG, "JSON Field [" + jsonFieldName + "] Failed to get Grab String! " + ex);
            return defaultValue;
        }
    }

    public static Integer getInteger(JSONObject obj, String jsonFieldName) { return  getInteger(obj, jsonFieldName, null); }
    public static Integer getInteger(JSONObject obj, String jsonFieldName, Integer defaultValue) {
        if(!StringUtil.isValidString(jsonFieldName))
            return defaultValue;

        try {
            return obj.getInt(jsonFieldName);
        }catch (JSONException ex) {
            Log.e(TAG, "JSON Field [" + jsonFieldName + "] Failed to get Grab Integer! " + ex);
            return defaultValue;
        }
    }

    public static Boolean getBoolean(JSONObject obj, String jsonFieldName) { return getBoolean(obj, jsonFieldName, false); }
    public static Boolean getBoolean(JSONObject obj, String jsonFieldName, Boolean defaultValue) {
        if(!StringUtil.isValidString(jsonFieldName))
            return defaultValue;

        try {
            return obj.getBoolean(jsonFieldName);
        }catch (JSONException ex) {
            Log.e(TAG, "JSON Field [" + jsonFieldName + "] Failed to get Grab Boolean! " + ex);
            return defaultValue;
        }
    }

}
