package eu.faircode.xlua;

import android.util.Log;

public class DebugUtil {
    private static boolean _forceDebug = false;
    public static boolean isDebug() {
        return BuildConfig.DEBUG || _forceDebug;
    }

    public static void setForceDebug(boolean forceDebug) {
        _forceDebug = forceDebug;
        Log.d("XL.DBG", "DV=" + forceDebug);
    }
}
