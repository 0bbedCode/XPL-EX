package eu.faircode.xlua.x.hook.interceptors.pkg;

import android.content.pm.PackageInfo;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.hook.interceptors.zone.RandomDateHelper;
import eu.faircode.xlua.x.data.GroupedMap;

public class PackageInfoInterceptor {
    private static final String TAG = "XLua.PackageInfoInterceptor";

    public static boolean interceptTimeStamps(XParam param, boolean isReturn) {
        try {
            Object obj = isReturn ? param.getResult() : param.getThis();
            GroupedMap map = param.getGroupedMap(GroupedMap.MAP_APP_TIMES);
            if(obj instanceof PackageInfo) {
                PackageInfo pkgInfo = (PackageInfo) obj;
                long[] times = RandomDateHelper.generateEpochTimeStamps(2, true);

                boolean val = param.getSettingBool("apps.sync.times.bool", false);
                long timeInstalled = System.currentTimeMillis() - RandomDateHelper.generateMinutesInMilliseconds(5, 50);
                long newInstallTime = map.getValueOrDefault("installTime", pkgInfo.packageName,
                        val ? timeInstalled : pkgInfo.firstInstallTime + times[0], false);
                long newUpdateTime = map.getValueOrDefault("updateTime", pkgInfo.packageName,
                        val ? timeInstalled : pkgInfo.lastUpdateTime + times[1], false);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Spoofing Package TimeStamps, Pkg=" + pkgInfo.packageName + "\n" +
                            "Old Install Time=" + pkgInfo.firstInstallTime + "\n" +
                            "New Install Time=" + newInstallTime + "\n" +
                            "Old Update Time=" + pkgInfo.lastUpdateTime + "\n" +
                            "New Update Time=" + newUpdateTime);

                pkgInfo.firstInstallTime = newInstallTime;
                pkgInfo.lastUpdateTime = newUpdateTime;
                if(isReturn) param.setResult(pkgInfo);
                return true;
            } else {
                Log.e(TAG, "This is NOT a package Info Class Type Weird... " + obj + " Type=" + (obj == null ? "null" : obj.getClass().getName()));
                return false;
            }
        }catch (Throwable e) {
            Log.e(TAG, "Failed to Spoof Package Install & Update Times ! " + e);
            return false;
        }
    }


    public static boolean isPackageAllowed(XParam param, String packageName) {
        if(TextUtils.isEmpty(packageName) || param.getPackageName().equalsIgnoreCase(packageName)) return true;
        try {
            boolean blacklist = param.getSettingBool("apps.blacklist.mode.bool", false);
            Object map = param.getValue("OBC.AppList", param.getApplicationContext());
            if(map == null) {
                //Create it
                HashMap<String, Boolean> list = new HashMap<>();

            }

            return true;
        }catch (Throwable e) {
            Log.e(TAG, "Failed to Determine if Package is Allowed... p=" + packageName + " Error=" + e + " Stack=" + Log.getStackTraceString(e));
            return false;
        }
    }

    /*@SuppressWarnings("unused")
    public boolean isPackageAllowed(String str) {
        if(str == null || str.isEmpty() || str.equalsIgnoreCase(this.packageName)) return true;
        str = str.toLowerCase().trim();
        String blackSetting = getSetting("apps.blacklist.mode.bool");
        if(blackSetting != null) {
            boolean isBlacklist = StringUtil.toBoolean(blackSetting, false);
            if(isBlacklist) {
                String block = getSetting("apps.block.list");
                if(Str.isValidNotWhitespaces(block)) {
                    block = block.trim();
                    if(block.contains(",")) {
                        String[] blocked = block.split(",");
                        for(String b : blocked)
                            if(b.trim().equalsIgnoreCase(str))
                                return false;
                    }else {
                        if(block.equalsIgnoreCase("*"))
                            return false;
                        else if(block.equalsIgnoreCase(str))
                            return false;
                    }
                }
            } else {
                String allow = getSetting("apps.allow.list");
                if(Str.isValidNotWhitespaces(allow)) {
                    allow = allow.trim();
                    if(allow.contains(",")) {
                        String[] allowed = allow.split(",");
                        for(String a : allowed)
                            if(a.trim().equalsIgnoreCase(str))
                                return true;
                    }else {
                        if(allow.equalsIgnoreCase("*"))
                            return true;
                        else if(allow.equalsIgnoreCase(str))
                            return true;
                    }
                }
                if(Evidence.packageName(str, 3))
                    return false;

                //Revert this
                if(getSettingBool("apps.blacklist.allow.vital.apps.bool", true)) {
                    for(String p : ALLOWED_PACKAGES)
                        if(str.contains(p))
                            return true;
                }

                return false;
            }
        } return !Evidence.packageName(str, 3);
    }*/
}
