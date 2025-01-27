/*
    This file is part of XPrivacyLua.

    XPrivacyLua is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    XPrivacyLua is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with XPrivacyLua.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2017-2019 Marcel Bokhorst (M66B)
 */

package eu.faircode.xlua;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Process;
import android.os.UserHandle;
import android.util.Log;
import android.util.TypedValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.MessageDigest;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

public class XUtil {
    private final static String TAG = "XLua.Util";

    public static final String PRO_PACKAGE_NAME = "eu.faircode.xlua.pro";
    private static final int PER_USER_RANGE = 100000;

    public static String getApk(Context context) {
        // Read built-in definition

        try{
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(BuildConfig.APPLICATION_ID, 0);
            return ai.publicSourceDir;
        }catch (Throwable ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
            return "eu.faircode.xlua";//Add in
        }
    }

    public static void setPermissions(String path, int mode, int uid, int gid) {
        try {
            Class<?> fileUtils = Class.forName("android.os.FileUtils");
            Method setPermissions = fileUtils
                    .getMethod("setPermissions", String.class, int.class, int.class, int.class);
            setPermissions.invoke(null, path, mode, uid, gid);
        } catch (Throwable ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
        }
    }

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

    public static String getSha1FingerprintString(Context context, String packageName) throws Throwable {
        byte[] bys = getSha1Fingerprint(context, packageName);
        StringBuilder sb = new StringBuilder();
        for (byte b : bys) sb.append(Integer.toString(b & 0xff, 16).toLowerCase());
        return sb.toString();
    }

    public static byte[] getSha1Fingerprint(Context context, String packageName) throws Throwable {
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        return digest.digest(packageInfo.signatures[0].toByteArray());
    }

    public static Context createContextForUser(Context context, int userid) throws Throwable {
        if (XposedUtil.isVirtualXposed())
            return context;

        // public UserHandle(int h)
        Class<?> clsUH = Class.forName("android.os.UserHandle");
        Constructor<?> cUH = clsUH.getDeclaredConstructor(int.class);
        UserHandle uh = (UserHandle) cUH.newInstance(userid);

        // public Context createPackageContextAsUser(String packageName, int flags, UserHandle user)
        Method c = context.getClass().getDeclaredMethod("createPackageContextAsUser", String.class, int.class, UserHandle.class);
        return (Context) c.invoke(context, "android", 0, uh);
    }

    @SuppressLint("MissingPermission")
    public static void notifyAsUser(Context context, String tag, int id, Notification notification, int userid) throws Throwable {
        NotificationManager nm = context.getSystemService(NotificationManager.class);

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PackageManager pm = context.getPackageManager();
            Resources resources = pm.getResourcesForApplication(BuildConfig.APPLICATION_ID);
            NotificationChannel channel = new NotificationChannel(
                    UberCore888.cChannelName, resources.getString(R.string.channel_privacy), NotificationManager.IMPORTANCE_HIGH);
            /*NotificationChannel channel = new NotificationChannel(
                    XProvider.cChannelName, resources.getString(R.string.channel_privacy), NotificationManager.IMPORTANCE_HIGH);*/
            channel.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT);
            nm.createNotificationChannel(channel);
        }

        if (XposedUtil.isVirtualXposed()) {
            nm.notify(tag, id, notification);
            return;
        }

        // public void notifyAsUser(String tag, int id, Notification notification, UserHandle user)
        Method mNotifyAsUser = nm.getClass().getDeclaredMethod(
                "notifyAsUser", String.class, int.class, Notification.class, UserHandle.class);
        mNotifyAsUser.invoke(nm, tag, id, notification, XUtil.getUserHandle(userid));
        Log.i(TAG, "Notified " + tag + ":" + id + " as " + userid);
    }

    public static void cancelAsUser(Context context, String tag, int id, int userid) throws Throwable {
        NotificationManager nm = context.getSystemService(NotificationManager.class);

        if (XposedUtil.isVirtualXposed()) {
            nm.cancel(tag, id);
            return;
        }

        // public void cancelAsUser(String tag, int id, UserHandle user)
        Method mCancelAsUser = nm.getClass().getDeclaredMethod(
                "cancelAsUser", String.class, int.class, UserHandle.class);
        mCancelAsUser.invoke(nm, tag, id, XUtil.getUserHandle(userid));
        Log.i(TAG, "Cancelled " + tag + ":" + id + " as " + userid);
    }

    public static int resolveColor(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        return typedValue.data;
        //boolean found = theme.resolveAttribute(attr, typedValue, true);
        //return found ? typedValue.data : 0;  // Fallback to default color or handle error
    }

    //Hmm we have this
    public static void areYouSure(ActivityBase activity, String question, final DoubtListener listener) {
        final DialogObserver observer = new DialogObserver();
        AlertDialog ad = new AlertDialog.Builder(activity)
                .setMessage(question)
                .setCancelable(true)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onSure();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        observer.stopObserving();
                    }
                })
                .create();
        ad.show();
        observer.startObserving(activity, ad);
    }

    public interface DoubtListener {
        void onSure();
    }

    public static class DialogObserver implements LifecycleObserver {
        private LifecycleOwner owner = null;
        private Dialog dialog = null;

        void startObserving(LifecycleOwner owner, Dialog dialog) {
            this.dialog = dialog;
            this.owner = owner;
            owner.getLifecycle().addObserver(this);
        }

        void stopObserving() {
            if (this.owner != null && this.dialog != null) {
                owner.getLifecycle().removeObserver(this);
                this.dialog = null;
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onDestroy() {
            if (this.owner != null && this.dialog != null)
                this.dialog.dismiss();
        }
    }
}
