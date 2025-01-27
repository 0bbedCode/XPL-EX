package eu.faircode.xlua.x.runtime;

import android.annotation.SuppressLint;
import android.util.Log;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.lang.reflect.Method;

public class HiddenApi {
    private static final String TAG = "XLua.HiddenApi";

    private static boolean hiddenApiBypassed = false;

    @SuppressLint("NewApi")
    public static boolean bypassHiddenApiRestrictions() {
        if(hiddenApiBypassed) return true;
        if(BuildInfo.isPieApi28Android9(true)) {
            try {
                Log.i(TAG, "Bypassing Hidden API Restrictions using Method (1)");
                Method forName = Class.class.getDeclaredMethod("forName", String.class);
                Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);

                Class vmRuntimeClass = (Class) forName.invoke(null, "dalvik.system.VMRuntime");
                Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
                Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[] {String[].class});

                Object vmRuntime = getRuntime.invoke(null);
                setHiddenApiExemptions.invoke(vmRuntime, new String[][]{new String[]{"L"}});
                hiddenApiBypassed = true;
                Log.i(TAG, "Hidden API Restrictions Bypassed using Method (1)");
            }catch (Exception e) {
                Log.e(TAG, "Hidden API Restrictions bypass (1) Failed: " + e.getMessage());
                try {
                    Log.i(TAG, "Bypassing Hidden API Restrictions using Method (2)");
                    hiddenApiBypassed = HiddenApiBypass.setHiddenApiExemptions("L");
                    Log.i(TAG, "Hidden API Restrictions Bypassed using Method (2) ? " + hiddenApiBypassed);
                }catch (Exception ee) {
                    Log.e(TAG, "Hidden API Restrictions bypass (2) Failed: " + ee.getMessage());
                }
            }
        } else {
            hiddenApiBypassed = true;
            Log.i(TAG, "Android Version Appears to be Lower than Android (9) SDK API Level (28) Code Name Pie, not bypassing as anything lower than that does not implement Hidden Api Restrictions.");
        } return hiddenApiBypassed;
    }

}
