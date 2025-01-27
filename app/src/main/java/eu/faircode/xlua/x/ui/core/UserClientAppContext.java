package eu.faircode.xlua.x.ui.core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import eu.faircode.xlua.AppGeneric;
//import eu.faircode.xlua.GlideApp;

import eu.faircode.xlua.api.xstandard.UserIdentityPacket;
import eu.faircode.xlua.builders.objects.Bundler;
import eu.faircode.xlua.x.data.interfaces.IValidator;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.hook.AppXpPacket;

/**
 * This Helps provide Context to things that need some sort of Identification usually either Current Android Profile User ID / Package Information
 * Try to use the Constructor for when you want to Create the Start of it, from there use Bundle to Transport it to lower views
 */
public class UserClientAppContext implements IValidator {
    private static final String TAG = "XLua.UserContext";

    public static UserClientAppContext DEFAULT = new UserClientAppContext(null, null);

    public static UserClientAppContext create(AppXpPacket app) { return new UserClientAppContext(app); }

    public static final String GLOBAL_NAME_SPACE = UserIdentityPacket.GLOBAL_NAMESPACE;
    public static final int GLOBAL_USER_ID = UserIdentityPacket.GLOBAL_USER;
    public static final int DEFAULT_PROFILE_USER_ID = 0;

    public static final String USER_CONTEXT_ARG = "user_context";

    public static String FIELD_ICON = "icon";
    public static String FIELD_USER_ID = "userId";
    public static String FIELD_APP_UID = "appUid";
    public static String FIELD_APP_NAME = "appName";
    public static String FIELD_APP_PACKAGE_NAME = "appPackageName";
    public static String FIELD_KILL = "kill";

    public static boolean hasArgs(Bundle b) { return b != null && b.containsKey("appPackageName"); }

    public static UserClientAppContext fromBundle(Context context, Bundle data) {
        if(data == null)
            return DEFAULT;

        UserClientAppContext uCtx = new UserClientAppContext();
        uCtx.fromBundle(data);
        if(context != null)
            uCtx.resolveFromPackageName(uCtx.appPackageName, context);

        return uCtx;
    }

    public int icon = 0;
    public int profileUserId = DEFAULT_PROFILE_USER_ID;
    public int appUid;
    public String appName;
    public String appPackageName;
    public boolean kill = false;

    public boolean isGlobal() { return GLOBAL_NAME_SPACE.equalsIgnoreCase(appPackageName); }

    public UserClientAppContext setProfileUserId(int profileUserId) { this.profileUserId = profileUserId; return this; }
    public UserClientAppContext setAppUid(int uid) { this.appUid = uid; return this; }
    public UserClientAppContext setAppName(String appName) { this.appName = appName; return this; }
    public UserClientAppContext setAppPackageName(String appPackageName) { this.appPackageName = appPackageName; return this; }

    @Override
    public boolean isValid() { return !TextUtils.isEmpty(this.appPackageName); }

    public UserClientAppContext(AppXpPacket app) { fromApp(app);  }

    public UserClientAppContext() { }
    public UserClientAppContext(String packageName, Context context) { resolveFromPackageName(packageName, context); }

    public UserClientAppContext consumeAppGeneric(AppGeneric appGeneric) { return appGeneric == null ? DEFAULT : setAppUid(appGeneric.getUid()).setAppName(appGeneric.getName()).setAppPackageName(appGeneric.getPackageName()); }

    public AppGeneric getAsAppGeneric() { return new AppGeneric(this); }

    public void fromApp(AppXpPacket app) {
        if(app != null) {
            this.appName = app.label;
            this.appPackageName = app.packageName;
            this.icon = app.icon;
            this.appUid = app.uid;
            this.kill = app.forceStop;
        }
    }

    public boolean resolveFromPackageName(String packageName, Context context) {
        this.profileUserId = getCurrentProfileUserId();
        if(context == null || TextUtils.isEmpty(packageName) || GLOBAL_NAME_SPACE.equalsIgnoreCase(packageName)) {
            this.appName = UserIdentityPacket.GLOBAL_NAMESPACE;
            this.appPackageName = UserIdentityPacket.GLOBAL_NAMESPACE;
            this.appUid = UserIdentityPacket.GLOBAL_USER;
            return true;
        } else {
            try {
                this.appPackageName = packageName;
                PackageManager packageManager = context.getPackageManager();
                ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
                this.icon = appInfo.icon;
                this.appUid = appInfo.uid;
                this.appName = (String) packageManager.getApplicationLabel(appInfo);
                return true;
            }catch (Exception e) {
                Log.e(TAG, "Failed to get Package Info of package: " + packageName + " Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
                return false;
            }
        }
    }

    public void setImageViewTextViewTexts(Context context, ImageView ivIcon, TextView tvName, TextView tvPackageName, TextView tvUid) {
        try {
            if(ivIcon != null)
                setImageView(ivIcon, context);
            if(tvName != null)
                tvName.setText(appName);
            if(tvPackageName != null)
                tvPackageName.setText(appPackageName);
            if(tvUid != null)
                tvUid.setText(String.valueOf(appUid));
        }catch (Exception e) {
            Log.e(TAG, "Failed to bind App Info to Text Views, Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
        }
    }

    public void setImageView(ImageView ivAppIcon, Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, typedValue, true);
        int height = TypedValue.complexToDimensionPixelSize(typedValue.data, context.getResources().getDisplayMetrics());
        int iconSize = Math.round(height * context.getResources().getDisplayMetrics().density + 0.5f);
        try {
            if (icon <= 0)
                ivAppIcon.setImageResource(android.R.drawable.sym_def_app_icon);
            else {
                attachIcon(
                        context,
                        iconSize,
                        ivAppIcon,
                        Uri.parse("android.resource://" + this.appPackageName + "/" + icon));
            }
        }catch (Exception e) {
            Log.e(TAG, "Failed to set AppIcon: " + this.appPackageName + " " + e);
        }
    }

    public Bundle toBundle() {
        return Bundler.create()
                .wInt(FIELD_ICON, this.icon)
                .wInt(FIELD_USER_ID, this.profileUserId)
                .wInt(FIELD_APP_UID, this.appUid)
                .wString(FIELD_APP_NAME, this.appName)
                .wString(FIELD_APP_PACKAGE_NAME, this.appPackageName)
                .wBool(FIELD_KILL, this.kill)
                .toBundle();
    }

    public void fromBundle(Bundle bundle) {
        if(bundle != null) {
            if(bundle.containsKey(USER_CONTEXT_ARG)) {
                fromBundle(bundle.getBundle(USER_CONTEXT_ARG));
            } else {
                this.icon = bundle.getInt(FIELD_ICON, 0);
                this.profileUserId = bundle.getInt(FIELD_USER_ID, DEFAULT_PROFILE_USER_ID);
                this.appUid = bundle.getInt(FIELD_APP_UID, UserIdentityPacket.GLOBAL_USER);
                this.appName = bundle.getString(FIELD_APP_NAME);
                this.appPackageName = bundle.getString(FIELD_APP_PACKAGE_NAME);
                this.kill = bundle.getBoolean(FIELD_KILL);
            }
        }
    }

    public static int getCurrentProfileUserId() {
        return DEFAULT_PROFILE_USER_ID;
    }

    public static void attachIcon(Context context, int iconSize, ImageView imageView, String packageName, int icon) { attachIcon(context, iconSize, imageView, getIconUriFromAndroidResource(packageName, icon)); }
    public static void attachIcon(Context context, int iconSize, ImageView imageView, Uri iconUri) {
        /*
            Ensure all Calls go here to Attach ICONs, also ensure its never imported but used as "eu.faircode.xlua.GlideApp"
            Reason is that sometimes when a Error that will not be caught by the IDE at Build, it will then Hang/Get Stuck on building glide app
            As Glide App is Compiled Dynamically but requires a successful build but it wont be successful if there is an error that the IDE will not Display
            This is Unless we Comment the below Line of Code out, then the IDE on next Rebuild Will Display the Actual ERROR Stopping project from a Successful build.

            So If when project is starting build, then it stops to display error that "GlideApp" is missing, that means there is an actual Error within the Project
                BUT the IDE is not Displaying it, Comment this Line out, re build then Fix the Lonely Error then Un Comment Glide App Code when all Errors are Resolved
         */
        eu.faircode.xlua.GlideApp.with(context)
                .applyDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_RGB_565))
                .load(iconUri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .override(iconSize, iconSize)
                .into(imageView);
    }

    //attachIconEx from new XPLEX

    public static Uri getIconUriFromAndroidResource(String packageName, int icon) {
        return Uri.parse("android.resource://" + packageName + "/" + icon);
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Icon", this.icon)
                .appendFieldLine("Profile User ID", this.profileUserId)
                .appendFieldLine("App Uid", this.appUid)
                .appendFieldLine("App Name", this.appName)
                .appendFieldLine("App Package Name", this.appPackageName)
                .toString(true);
    }
}
