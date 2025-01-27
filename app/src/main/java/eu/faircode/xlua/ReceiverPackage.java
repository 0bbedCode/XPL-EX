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
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import de.robv.android.xposed.XposedBridge;
import eu.faircode.xlua.api.xstandard.UserIdentityPacket;
import eu.faircode.xlua.api.xlua.XLuaCall;

public class ReceiverPackage extends BroadcastReceiver {
    private static final String TAG = "XLua.Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String packageName = intent.getData().getSchemeSpecificPart();
            int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
            boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            Log.i(TAG, "Received " + intent + " uid=" + uid);

            int userid = XUtil.getUserId(uid);
            Context ctx = XUtil.createContextForUser(context, userid);

            if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
                if (!replacing && !packageName.startsWith(BuildConfig.APPLICATION_ID)) {
                    // Initialize app

                    //Bundle args = new Bundle();
                    //args.putString("packageName", packageName);
                    //args.putInt("uid", uid);
                    //context.getContentResolver()
                    //        .call(XSecurity.getURI(), "xlua", "clearApp", args);
                    XLuaCall.clearApp(context, uid, packageName);

                    //Ensure this bullshit works (userid then use Global namespace ???????? )
                    if (XLuaCall.getSettingBoolean(context, userid, UserIdentityPacket.GLOBAL_NAMESPACE, "restrict_new_apps"))
                        //XLuaCall.initApp(context, packageName, uid);
                        XLuaCall.initApp(context, uid, packageName);
                        //context.getContentResolver()
                        //        .call(XSecurity.getURI(), "xlua", "initApp", args);


                    // Notify new app
                    if (XLuaCall.getSettingBoolean(context, userid,  UserIdentityPacket.GLOBAL_NAMESPACE, "notify_new_apps")) {
                        PackageManager pm = ctx.getPackageManager();
                        Resources resources = pm.getResourcesForApplication(BuildConfig.APPLICATION_ID);

                        Notification.Builder builder = new Notification.Builder(ctx);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            builder.setChannelId(UberCore888.cChannelName);
                        builder.setSmallIcon(android.R.drawable.ic_dialog_alert);
                        builder.setContentTitle(resources.getString(R.string.msg_review_settings));
                        builder.setContentText(pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)));

                        builder.setPriority(Notification.PRIORITY_HIGH);
                        builder.setCategory(Notification.CATEGORY_STATUS);
                        builder.setVisibility(Notification.VISIBILITY_SECRET);

                        // Main
                        int flags = (Build.VERSION.SDK_INT > Build.VERSION_CODES.R ? 0x04000000 : 0);
                        Intent main = ctx.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
                        main.putExtra(ActivityMain.EXTRA_SEARCH_PACKAGE, packageName);
                        @SuppressLint("WrongConstant") PendingIntent pi = PendingIntent.getActivity(ctx, uid, main, flags);
                        builder.setContentIntent(pi);

                        builder.setAutoCancel(true);

                        XUtil.notifyAsUser(ctx, "xlua_new_app", uid, builder.build(), userid);
                    }
                }
            } else if (Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(intent.getAction())) {
                if (BuildConfig.APPLICATION_ID.equals(packageName)) {
                    //Bundle args = new Bundle();
                    //args.putInt("user", userid);
                    //context.getContentResolver()
                    //        .call(XSecurity.getURI(), "xlua", "clearData", args);

                    //WTF IS THIS GLOBAL ARGUMENT ????????
                    //Clear XLUA data ?
                    XLuaCall.clearData(context, userid);
                    //For now we comment this out
                } else {
                    //Bundle args = new Bundle();
                    //args.putString("packageName", packageName);
                    //args.putInt("uid", uid);
                    //args.putBoolean("settings", true);
                    //context.getContentResolver()
                    //        .call(XSecurity.getURI(), "xlua", "clearApp", args);

                    XLuaCall.clearApp(context, uid, packageName, true);
                    XUtil.cancelAsUser(ctx, "xlua_new_app", uid, userid);
                }
            }
        } catch (Throwable ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
            XposedBridge.log(ex);
        }
    }
}
