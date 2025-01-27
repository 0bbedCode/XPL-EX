package eu.faircode.xlua.x.xlua.hook;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.BuildConfig;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.XposedUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.identity.UserIdentityUtils;

public class AppProviderApi {
    private static final String TAG = "XLua.AppProviderApi";

    public static final int INVALID_VERSION = -55;

    public static Map<String, AppXpPacket> getApps(Context context, SQLDatabase database, int uid, boolean initForceStop, boolean initAssignments) {
        int userId = UserIdentityUtils.getUserId(uid);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Getting App(s), Database=%s UserId=%s InitForceStop=%s InitAssignments=%s UID=%s", Str.noNL(database), userId, initForceStop, initAssignments, uid));

        Map<String, AppXpPacket> apps = new HashMap<>();

        long identity = Binder.clearCallingIdentity();
        try {
            //https://github.com/zer0def/vXPrivacyLua/commit/c030dd1880afda81b989185f8e2527c8358db799
            //For TaiChi support
            Context contextForUser = XUtil.createContextForUser(context, userId);
            PackageManager pm = contextForUser.getPackageManager();

            for(ApplicationInfo ai : XposedUtil.getApplications(contextForUser)) {
                if(ai.packageName.startsWith(BuildConfig.APPLICATION_ID))
                    continue;

                AppXpPacket packet = AppProviderUtils.assignAppInfoToPacket(ai, pm, initForceStop, initAssignments);
                apps.put(packet.packageName, packet);
            }
        }catch (Throwable e) {
            Log.e(TAG, Str.fm("Error Getting App, Database=%s UserId=%s InitForceStop=%s InitAssignments=%s UID=%s", Str.noNL(database), userId, initForceStop, initAssignments, uid));
        } finally {
            Binder.restoreCallingIdentity(identity);
        }

        if(initForceStop)
            AppProviderUtils.initAppsForceStop(apps, database, userId);

        if(initAssignments)
            AppProviderUtils.initAppsAssignmentSettings(apps, database, userId);

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Getting App(s) Finished, Database=%s UserId=%s InitForceStop=%s InitAssignments=%s UID=%s  Apps Count=%s", Str.noNL(database), userId, initForceStop, initAssignments, uid, apps.size()));

        return apps;
    }

    public static AppXpPacket getApp(Context context, SQLDatabase database, int uid, String packageName, boolean initForceStop, boolean initAssignments) {
        int userId = UserIdentityUtils.getUserId(uid);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Getting App, Database=%s UserId=%s PackageName=%s InitForceStop=%s InitAssignments=%s UID=%s", Str.noNL(database), userId, packageName, initForceStop, initAssignments, uid));

        long identity = Binder.clearCallingIdentity();
        try {
            Context contextForUser = XUtil.createContextForUser(context, userId);
            PackageManager pm = contextForUser.getPackageManager();

            for(ApplicationInfo ai : XposedUtil.getApplications(contextForUser)) {
                if(!packageName.equalsIgnoreCase(ai.packageName))
                    continue;

                AppXpPacket packet = AppProviderUtils.assignAppInfoToPacket(ai, pm, initForceStop, initAssignments);
                if(initAssignments || initForceStop) {
                    HashMap<String, AppXpPacket> temporaryMap = new HashMap<>();
                    temporaryMap.put(packet.packageName, packet);

                    if(initForceStop)
                        AppProviderUtils.initAppsForceStop(temporaryMap, database, userId);

                    if(initAssignments)
                        AppProviderUtils.initAppsAssignmentSettings(temporaryMap, database, userId);
                }

                return packet;
            }
        }catch (Throwable e) {
            Log.e(TAG, Str.fm("Error Getting App, Database=%s UserId=%s PackageName=%s InitForceStop=%s InitAssignments=%s UID=%s", Str.noNL(database), userId, packageName, initForceStop, initAssignments, uid));
        } finally {
            Binder.restoreCallingIdentity(identity);
        }

        return null;
    }

    public static int getVersion(Context context) throws Throwable {
        if (XposedUtil.isVirtualXposed()) {
            PackageInfo pi = context.getPackageManager().getPackageInfo(BuildConfig.APPLICATION_ID, 0);
            return pi.versionCode;
        } else
            return INVALID_VERSION;
    }

    public static boolean clearAppData(Context context, String packageName) {
        if(UserIdentity.GLOBAL_NAMESPACE.equalsIgnoreCase(packageName))
            return false;

        if("com.android.providers.settings".equalsIgnoreCase(packageName) || packageName.toLowerCase().contains("xlua"))
            return false;

        if(DebugUtil.isDebug())
            Log.d(TAG, "Clearing Package App Data, Package=" + packageName);

        try {
            long identity = Binder.clearCallingIdentity();
            try {
                //android.content.pm.IPackageDataObserver java
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                Method mForceStop = am.getClass().getMethod("clearApplicationUserData", String.class, Class.forName("android.content.pm.IPackageDataObserver"));
                mForceStop.invoke(am, packageName, null);
                return true;
            }catch (Exception e) {
                Log.e(TAG, Str.fm("Failed to Clear Package App Data: %s  Error:%s", packageName, e));
                return false;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }catch (Exception ignored) {
            return false;
        }
    }

    public static boolean forceStop(Context context, int userId, String packageName) {
        if(UserIdentity.GLOBAL_NAMESPACE.equalsIgnoreCase(packageName))
            return false;

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Force Stopping Package: %s  UserId:%s", packageName, userId));

        try {
            long identity = Binder.clearCallingIdentity();
            try {
                // public void forceStopPackageAsUser(String packageName, int userId)
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                Method mForceStop = am.getClass().getMethod("forceStopPackageAsUser", String.class, int.class);
                mForceStop.invoke(am, packageName, userId);
                return true;
            }catch (Exception e) {
                Log.e(TAG, Str.fm("Failed to Force Stop Package: %s  UserId:%s", packageName, userId));
                return false;
            }finally {
                Binder.restoreCallingIdentity(identity);
            }
        }catch (Exception ignored) {
            return false;
        }
    }
}
