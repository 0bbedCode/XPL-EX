package eu.faircode.xlua.x.hook.interceptors.pkg;

import android.content.pm.PackageInfo;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.utilities.DateTimeUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.hook.interceptors.zone.RandomDateHelper;
import eu.faircode.xlua.x.data.GroupedMap;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class PackageInfoInterceptor {
    private static final String TAG = LibUtil.generateTag(PackageInfoInterceptor.class);

    public static final String RAND_ONCE = "%random.once%";
    public static final String RAND_ALWAYS = "%random.always%";

    public static final String NOW_ONCE = "%now.once%";
    public static final String NOW_ALWAYS = "%now.always%";




    //public static final String ONLY_CURRENT_APP = RandomizersCache.SETTING_APP_TIME_CURRENT_ONLY;


    public static final String INSTALL_CURRENT_OFFSET_SETTING = "apps.current.install.time.offset";
    public static final String UPDATE_CURRENT_OFFSET_SETTING = "apps.current.update.time.offset";

    public static final String INSTALL_OFFSET_SETTING = "apps.install.time.offset";
    public static final String UPDATE_OFFSET_SETTING = "apps.update.time.offset";

    public static final String INSTALL_GROUP = "installTime";
    public static final String UPDATE_GROUP = "updateTime";


    public static long get(
            String groupName,
            String packageName,
            String settingValue,
            GroupedMap map,
            long defValue,
            long originalValue) {
        if(Str.isEmpty(settingValue)) {
            map.pushValueLong(groupName, packageName, originalValue + defValue);
            return originalValue + defValue;
        }

        if(RAND_ALWAYS.equalsIgnoreCase(settingValue))
            return originalValue + defValue;
            //return RandomDateHelper.generateEpochTimeStamps(1, false)[0];

        if(NOW_ALWAYS.equalsIgnoreCase(settingValue))
            return System.currentTimeMillis();

        long val = map.getValueLong(groupName, packageName, false);
        if(val > 0)
            return val;

        if(NOW_ONCE.equalsIgnoreCase(settingValue)) {
            long now = System.currentTimeMillis() - RandomGenerator.nextInt(300, 800);
            map.pushValueLong(groupName, packageName, now);
            return now;
        }

        if(RAND_ONCE.equalsIgnoreCase(settingValue)) {
            map.pushValueLong(groupName, packageName, originalValue + defValue);
            return originalValue + defValue;
        }

        try {
            long[] iTimes = DateTimeUtil.toTimeSpecs(settingValue);
            if(ArrayUtils.isValid(iTimes) && iTimes.length == 2) {
                long seconds = iTimes[0];
                if(seconds > 0) {
                    long off = seconds * 1000; // Convert seconds to milliseconds
                    long newValue = originalValue + off;
                    map.pushValueLong(groupName, packageName, newValue);
                    return newValue;
                } else {
                    throw new Exception("Seconds is 0...");
                }
            } else {
                throw new Exception("Array is Invalid!");
            }
        }catch (Exception e) {
            Log.e(TAG, "Failed to get time, error=" + e + " Pkg=" + packageName + " Value=" + settingValue + " Group=" + groupName);
            map.pushValueLong(groupName, packageName, originalValue + defValue);
            return originalValue + defValue;
        }
    }

    public static boolean interceptTimeStamps(XParam param, boolean isReturn) {
        try {
            Object obj = isReturn ? param.getResult() : param.getThis();
            GroupedMap map = param.getGroupedMap(GroupedMap.MAP_APP_TIMES);
            if(obj instanceof PackageInfo) {
                PackageInfo pkgInfo = (PackageInfo) obj;
                boolean isCurrent = pkgInfo.packageName.equalsIgnoreCase(param.getPackageName());

                long[] times = RandomDateHelper.generateEpochTimeStamps(2, true);
                long install = get(isCurrent ? INSTALL_CURRENT_OFFSET_SETTING : INSTALL_GROUP, pkgInfo.packageName, param.getSetting(INSTALL_OFFSET_SETTING), map, times[0], pkgInfo.firstInstallTime);
                long update = get(isCurrent ? UPDATE_CURRENT_OFFSET_SETTING : UPDATE_GROUP, pkgInfo.packageName, param.getSetting(UPDATE_OFFSET_SETTING), map, times[1], pkgInfo.lastUpdateTime);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Package Name=" + pkgInfo.packageName + " Install Offset=" + install + " Update Offset=" + update);

                pkgInfo.firstInstallTime = install;
                pkgInfo.lastUpdateTime = update;
                if(isReturn)
                    param.setResult(pkgInfo);

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
