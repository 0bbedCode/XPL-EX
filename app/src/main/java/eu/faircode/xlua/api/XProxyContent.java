package eu.faircode.xlua.api;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import eu.faircode.xlua.XSecurity;
import eu.faircode.xlua.api.xstandard.database.SqlQuerySnake;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.x.xlua.database.sql.SQLSnake;

public class XProxyContent {
    private static final String TAG = "XLua.XProxyContent";

    public static Bundle mockCall(Context context, String method) { return mockCall(context, method, new Bundle()); }
    public static Bundle mockCall(Context context, String method, Bundle extras) { return invokeCall(context, "mock", method, extras); }

    public static Bundle luaCall(Context context, String method) { return luaCall(context, method, new Bundle()); }
    public static Bundle luaCall(Context context, String method, Bundle extras) { return invokeCall(context, "xlua", method, extras); }

    //public static Cursor mockQuery(Context context, String method) { return invokeQuery(context, method, null, null); }
    //public static Cursor mockQuery(Context context, String method, String[] args_selection) { return mockQuery(context, method, args_selection, null); }
    //public static Cursor mockQuery(Context context, String method, String[] args_selection, String selection) { return invokeQuery(context, "mock", method, args_selection, selection); }

    public static Cursor mockQuery(Context context, String method) { return mockQuery(context, method, null, null); }
    public static Cursor mockQuery(Context context, String method, String[] args_selection) { return mockQuery(context, method, args_selection, null); }
    public static Cursor mockQuery(Context context, String method, String[] args_selection, String selection) { return invokeQuery(context, "mock", method, args_selection, selection); }
    public static Cursor mockQuery(Context context, String method, SqlQuerySnake query) { return invokeQuery(context, "mock", method, query); }

    public static Cursor luaQuery(Context context, String method) { return luaQuery(context, method, null, null); }
    public static Cursor luaQuery(Context context, String method, String[] args_selection) { return luaQuery(context, method, args_selection, null); }
    public static Cursor luaQuery(Context context, String method, String[] args_selection, String selection) { return invokeQuery(context, "xlua", method, args_selection, selection); }
    public static Cursor luaQuery(Context context, String method, SqlQuerySnake query) { return invokeQuery(context, "xlua", method, query); }



    public static Cursor invokeQuery(Context context, String handler, String method, SqlQuerySnake query) { return invokeQuery(context, handler, method, query.getSelectionCompareValues(), query.getSelectionArgs()); }

    public static Cursor luaQuery(Context context, String method, SQLSnake query) { return invokeQuery(context, "xlua", method, query); }
    public static Cursor invokeQuery(Context context, String handler, String method, SQLSnake query) { return invokeQuery(context, handler, method, query.getWhereArgs(), query.getWhereClause()); }


    public static Cursor invokeQuery(Context context,String handler, String method) { return invokeQuery(context, handler, method, null, null); }
    public static Cursor invokeQuery(Context context,String handler, String method, String[] args_selection) { return invokeQuery(context, handler, method, args_selection, null); }
    public static Cursor invokeQuery(Context context,String handler, String method, String[] args_selection, String selection) {
        try {
            return context.getContentResolver()
                    .query(
                            XSecurity.getURI(),
                            new String[]{ handler + "." + method },
                            selection,
                            args_selection,
                            null);
        }catch (Exception e) {
            Log.e(TAG, "Failed to Invoke Query! handler=" + handler + " method=" + method + " e=" + e + "\n" + Log.getStackTraceString(e));
            return null;
        }
    }

    public static Bundle invokeCall(Context context, String handler, String method) { return invokeCall(context,handler, method, new Bundle()); }
    public static Bundle invokeCall(Context context, String handler, String method, Bundle extras) {
        try {
            return context.getContentResolver()
                    .call(XSecurity.getURI(), handler, method, extras);
        }catch (Exception e) {
            Log.e(TAG, "Failed to Invoke Call! handler=" + handler + " method=" + method + " e=" + e + "\n" + Log.getStackTraceString(e));
            return null;
        }
    }
}
