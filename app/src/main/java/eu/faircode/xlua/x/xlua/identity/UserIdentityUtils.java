package eu.faircode.xlua.x.xlua.identity;

import android.content.Context;
import android.os.Process;
import android.os.UserHandle;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.runtime.RuntimeUtils;

public class UserIdentityUtils {
    private static final String TAG = "XLua.UserIdentityUtils";

    private static final int PER_USER_RANGE = 100000;

    public static boolean isGlobalUidCategory(String category) { return UserIdentity.GLOBAL_NAMESPACE.equalsIgnoreCase(category); }

    public static int getAppId(int uid) {
        try {
            // public static final int getAppId(int uid)
            Method method = UserHandle.class.getDeclaredMethod("getAppId", int.class);
            return (int) method.invoke(null, uid);
        } catch (Throwable ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
            return uid % PER_USER_RANGE;
        }
    }

    public static int getUserId(int uid) {
        try {
            // public static final int getUserId(int uid)
            Method method = UserHandle.class.getDeclaredMethod("getUserId", int.class);
            return (int) method.invoke(null, uid);
        } catch (Throwable ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
            return uid / PER_USER_RANGE;
        }
    }

    public static int getUserUid(int userid, int appid) {
        try {
            // public static int getUid(@UserIdInt int userId, @AppIdInt int appId)
            Method method = UserHandle.class.getDeclaredMethod("getUid", int.class, int.class);
            return (int) method.invoke(null, userid, appid);
        } catch (Throwable ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
            return userid * PER_USER_RANGE + (appid % PER_USER_RANGE);
        }
    }

    public static UserHandle getUserHandle(int userid) {
        try {
            // public UserHandle(int h)
            Constructor ctor = UserHandle.class.getConstructor(int.class);
            return (UserHandle) ctor.newInstance(userid);
        } catch (Throwable ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
            return Process.myUserHandle();
        }
    }

    public static String resolvePackageNameForUid(Context context, int uid) {
        if(context == null || uid < 0) {
            Log.e(TAG, "Error resolving package Name, UID or Context is Null or Not Valid! UID=" + uid + " Is Context Null ? " + ObjectUtils.isNullAsString(context) + " Stack=" + RuntimeUtils.getStackTraceSafeString());
            return null;
        }

        try {
            return context.getPackageManager().getPackagesForUid(uid)[0];
        }catch (Exception e) {
            Log.e(TAG, "Error resolving package Name for UID=" + uid + " Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            return null;
        }
    }
}
