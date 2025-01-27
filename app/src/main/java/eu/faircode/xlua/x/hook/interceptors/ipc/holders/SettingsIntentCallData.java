package eu.faircode.xlua.x.hook.interceptors.ipc.holders;

//    android.net.Uri uri,
//    String method,
//    String arg,
//    android.os.Bundle extras

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;

public class SettingsIntentCallData {
    public static SettingsIntentCallData create(XParam param, boolean getResult) { return new SettingsIntentCallData(param, getResult); }

    private static final String TAG = "XLua.IntentCallData";

    public static final String GET_SECURE_METHOD = "GET_secure";
    public static final String BUNDLE_ARG = "value";

    public String authority;
    public String method;
    public String arg;
    public Bundle extras;
    public Bundle result;
    public boolean isStringReturn = false;

    public boolean hasAuthority() { return !TextUtils.isEmpty(authority); }
    public boolean hasMethod() { return !TextUtils.isEmpty(method); }
    public boolean hasArg() { return !TextUtils.isEmpty(arg); }
    public boolean hasResult() { return result != null; }

    public boolean isSettingsAuthority() { return hasAuthority() && authority.contains("settings"); }
    public boolean isGetSetting() { return hasMethod() && method.toLowerCase().startsWith("get_"); }

    public SettingsIntentCallData(XParam param, boolean getResult) {
        try {
            Object pOne = param.tryGetArgument(0, null);
            if(pOne instanceof ContentResolver) {
                //ASSUME this is part of the Setting.getString function
                this.arg = param.tryGetArgument(1, null);
                if(this.arg != null) {
                    this.isStringReturn = true;
                    this.authority = "settings";
                    this.method = GET_SECURE_METHOD;
                    this.extras = null;
                    if(getResult) {
                        Bundle res = new Bundle();
                        res.putString(BUNDLE_ARG, param.tryGetResult(""));
                        this.result = res;
                    }
                }
            } else {
                if(pOne instanceof Uri)
                    this.authority = ((Uri) pOne).getAuthority();
                else if(pOne instanceof String)
                    this.authority = (String) pOne;
                else {
                    Log.e(TAG, "Intent Call, First Param is not a URI or String, Error...");
                }

                if(pOne != null) {
                    this.method = param.tryGetArgument(1, null);
                    this.authority = param.tryGetArgument(2, null);
                    this.extras = param.tryGetArgument(3, null);
                    if(getResult) this.result = param.tryGetResult(null);
                }
            }
        }catch (Exception e) {
            Log.e(TAG, "Error in Constructor<SettingsIntentCallData>. Error:" + e);
        }
    }

    public boolean replaceSettingStringResult(XParam param) {
        try {
            if(!isSettingsAuthority() || !isGetSetting()) {
                if(DebugUtil.isDebug()) Log.d(TAG, "Not Authority or Arg: " + this);
                return false;
            }

            if(!hasResult()) {
                if(DebugUtil.isDebug()) Log.d(TAG, "Call does not Have a Return: " + this);
                return false;
            }

            if(!hasArg()) {
                if(DebugUtil.isDebug()) Log.d(TAG, "Call does not Have a Arg: " + this);
                return false;
            }

            String argLow = arg.toLowerCase();
            String setNameMapped = "call:" + argLow;
            String settingName = param.getSetting(setNameMapped);
            if(TextUtils.isEmpty(settingName)) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Arg Does not Have a Linking Setting: " + this + "  Setting Name Mapped:" + setNameMapped);
                return false;
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Call => " + this + " Setting Name=" + settingName + " Is String Return=" + (isStringReturn));

            String resValue = result.getString(BUNDLE_ARG, null);
            if(TextUtils.isEmpty(resValue)) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Call Result Value is NULL or Empty Ignoring....");
                return false;
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Call => " + this + " Setting name=" + settingName + " Original Value=" + resValue);

            String settingValue = param.getSetting(settingName);
            if(settingValue == null) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Setting: " + settingName + " Has a Null Value skipping Call Replacement: " + this);
                return false;
            }

            if(settingValue.equalsIgnoreCase(resValue)) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Setting Value is the Same as the Result: Setting Value=" + settingValue + " Result Value=" + resValue + " Authority=" + authority);
                return false;
            }

            param.setOldResult(resValue);
            param.setNewResult(settingValue);
            if(!isStringReturn) {
                Bundle b = new Bundle();
                b.putString(BUNDLE_ARG, settingValue);
                param.setResult(b);
            } else {
                param.setResult(settingValue);
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Finished Replacing Intent Call Value: Old=" + resValue + " New=" + settingValue + " this=" + this);

            return true;
        }catch (Throwable e) {
            Log.e(TAG, "Error Replacing Result: " + this + " Error: " + e);
            return false;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "Authority=" + authority + "\nMethod=" + method + "\nArg=" + arg;
    }
}
