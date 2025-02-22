package eu.faircode.xlua.x.xlua.identity;

import android.content.Context;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import eu.faircode.xlua.api.xstandard.database.SqlQuerySnake;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.xlua.database.sql.SQLSnake;

/*
    UID:        Typically the ID that is tied to the Installed Package Android, each Package gets their own UID
    AppID:      In my case it appears the Same as UID always, not sure when it is Different, Some App ID
    UserID:     The User ID, User as in the Signed in Android Device Account/User, Work Profile etc... Typically Default is (0)
    UserUid:    Typically the (User ID) + (0) + (UID), So Combining User ID and UID with a Zero in between them, not sure when this is used or why

                Deprecate use of Global User ID as User ID (0) is just the First Default user
                PS (BIG PS) when we are using "getCallingUid" we need to Drop Current one as the Current one (I think) is always (1000)
                    Its never the actual XPL-EX app or any, it goes through Settings Storage First (1000), I think ?
                        Well its not NEEDED right now lets just ignore it.....

                We should TRY to ALWAYS handle UserID on Server Side, They give us UID, then we Convert the UID into a User ID
                Never should it be other way around!
 */
public class UserIdentity implements IIdentification {
    public static final int DEFAULT_USER = 0;
    public static final String GLOBAL_NAMESPACE = "global";
    public static final UserIdentity DEFAULT = new UserIdentity(DEFAULT_USER, GLOBAL_NAMESPACE);

    public static UserIdentity create(Bundle b) { return new UserIdentity(b); }
    public static UserIdentity create(Cursor c) { return new UserIdentity(c); }

    public static UserIdentity fromUid(int uid, String packageName) { return new UserIdentity(uid, packageName); }
    public static UserIdentity from(int userId, int uid, String packageName) { return new UserIdentity(userId, uid, packageName); }

    private int uid;
    private int user = -1;
    private String category;

    private int callingUid = -1;
    private String callingPackageName;

    public boolean hasCategory() { return !TextUtils.isEmpty(category); }
    public boolean hasCallingPackageName() { return !TextUtils.isEmpty(callingPackageName); }

    public boolean hasUid() { return uid > -1; }
    public boolean hasUserId() { return user > -1; }
    public boolean hasCallingUid() { return callingUid > 0; }

    @Override
    public boolean isGlobal() { return GLOBAL_NAMESPACE.equalsIgnoreCase(callingPackageName); }

    public void setUid(int uid) { this.uid = uid; }
    public void setUserId(int userId) { this.user = userId; }
    public void setCategory(String category) { this.category = category; }

    //public boolean isProPackageCaller(Context context) { return context == null ?  }

    public UserIdentity() { }
    public UserIdentity(int uid) { this.uid = uid; }
    public UserIdentity(int uid, String packageName) { this.uid = uid; this.category = packageName; }
    public UserIdentity(int userId, int uid, String packageName) { this.user = userId; this.uid = uid; this.category = packageName; }

    public UserIdentity(Cursor c) {
        if(c != null) {
            this.user = CursorUtil.getInteger(c, UserIdentityIO.FIELD_USER, 0);
            this.category = CursorUtil.getString(c, UserIdentityIO.FIELD_CATEGORY, GLOBAL_NAMESPACE);
        }
    }

    public UserIdentity(Bundle b) {
        if(b != null) {
            UserIdentityIO.fromBundleEx(b, this, false);
        }
    }

    public static int ensureValidUser(int userId) { return userId > -1 ? userId : DEFAULT_USER; }
    public static int ensureValidUid(int uid) { return uid > -1 ? uid : DEFAULT_USER; }
    public static String ensureValidCategory(String category) { return TextUtils.isEmpty(category) ? GLOBAL_NAMESPACE : category; }

    public int refreshCallingUid() { return refreshCallingUid(true); }
    public int refreshCallingUid(boolean resetCallingUidPackageName) { this.callingUid = Binder.getCallingUid(); if(resetCallingUidPackageName) this.category = null;  return callingUid; }

    public String ensurePackageName(Context context) { if(TextUtils.isEmpty(category)) this.category = UserIdentityUtils.resolvePackageNameForUid(context, uid); return this.category; }
    public String ensureCallingPackageName(Context context) { if(TextUtils.isEmpty(callingPackageName)) this.callingPackageName = UserIdentityUtils.resolvePackageNameForUid(context, getCallingUid()); return this.callingPackageName; }

    public int resolveUserId() {
        if(user == -1) user = UserIdentityUtils.getUserId(uid);
        return user;
    }

    @Override
    public int getUid() { return uid; }
    public int getAppId() { return UserIdentityUtils.getAppId(uid); }
    @Override
    public int getUserId(boolean resolve) { return resolve ? resolveUserId() : user; }
    public int getUserUid() { return UserIdentityUtils.getUserUid(uid, getAppId()); }
    @Override
    public String getCategory() { return category; }

    @Override
    public int getCallingUid() { if(callingUid < 0) this.callingUid = Binder.getCallingUid(); return this.callingUid; }
    public int getCallingAppId() { return UserIdentityUtils.getAppId(callingUid); }
    @Override
    public int getCallingUserId() { return UserIdentityUtils.getUserId(callingUid); }
    public int getCallingUserUid() { return UserIdentityUtils.getUserUid(callingUid, getCallingAppId()); }
    @Override
    public String getCallingPackageName() { return callingPackageName; }

    @Override
    public UserIdentity asObject() { return this; }

    public Bundle toIdentityBundle() {
        Bundle b = new Bundle();
        UserIdentityIO.toBundleEx(this, b);
        return b;
    }

    public static SQLSnake createSnakeQueryUID(int uid, String category) {
        return SQLSnake.create()
                .whereColumn("uid", uid)
                .whereColumn("category", category)
                .asSnake();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof Integer) return  ((int)obj) == this.uid;
        if(obj instanceof String) return ((String)obj).equalsIgnoreCase(this.category);
        if(obj instanceof UserIdentity) {
            UserIdentity data = (UserIdentity) obj;
            return this.uid == data.uid && Str.areEqualIgnoreCase(this.category, data.category);
        }

        return super.equals(obj);
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Uid Valid", hasUid())
                .appendFieldLine("Uid", this.uid)
                .appendFieldLine("Uid App Id", getAppId())
                .appendFieldLine("Uid User Id", getUserId(false))
                .appendFieldLine("Uid UserUid", getUserUid())
                .appendFieldLine("Category or PackageName Valid", hasCategory())
                .appendFieldLine("Category or PackageName", this.category)
                .appendFieldLine("Calling Uid Valid", hasCallingUid())
                .appendFieldLine("Calling Uid", this.callingUid)
                .appendFieldLine("Calling Uid App Id", getCallingAppId())
                .appendFieldLine("Calling Uid User Id", getCallingUserId())
                .appendFieldLine("Calling Uid UserUid", getCallingUserUid())
                .appendFieldLine("Calling Uid PackageName Valid", hasCallingPackageName())
                .appendFieldLine("Calling Uid PackageName", this.callingPackageName)
                .toString(true);
    }
}
