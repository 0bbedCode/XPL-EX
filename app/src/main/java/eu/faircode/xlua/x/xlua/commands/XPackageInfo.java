package eu.faircode.xlua.x.xlua.commands;

import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;

import eu.faircode.xlua.x.xlua.database.TableInfo;
import eu.faircode.xlua.x.xlua.identity.UserIdentityUtils;

public class XPackageInfo {
    private static final String TAG = "XLua.XPackageInfo";

    public static final String FIELD_UID = "_uid";
    public static final String FIELD_USER_ID = "_user";
    public static final String FIELD_PACKAGE_NAME = "_packageName";
    public static final String FIELD_KILL = "_kill";

    public int uid = 0;
    public int userId = -1;
    public String packageName = "global";
    public boolean kill = false;

    public XPackageInfo() { }
    public XPackageInfo(int uid, String packageName) { this.uid = uid; this.packageName = packageName; }
    public XPackageInfo(int uid, int userId, String packageName) { this.uid = uid; this.userId = userId; this.packageName = packageName; }

    public int getUserId() {
        if(userId < 0)
            userId = UserIdentityUtils.getUserId(uid);

        return userId;
    }

    public void fromBundle(Bundle b) {
        if(b != null) {
            this.uid = b.getInt(FIELD_UID, 0);
            this.userId = b.getInt(FIELD_USER_ID, -1);
            this.packageName = b.getString(FIELD_PACKAGE_NAME, "");
            this.kill = b.getBoolean(FIELD_KILL, false);
        }
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putInt(FIELD_UID, this.uid);
        b.putInt(FIELD_USER_ID, this.userId);
        b.putString(FIELD_PACKAGE_NAME, this.packageName);
        b.putBoolean(FIELD_KILL, this.kill);
        return b;
    }

    public void writeToContentValues(ContentValues cv, TableInfo tableInfo) {
        if(cv != null) {
            if(tableInfo != null) {

            }
        }
    }

    public void feedObject(IPkgInfo pkgInfoObject) {
        if(pkgInfoObject != null) {
            try {
                pkgInfoObject.consumePackageInfo(this);
            }catch (Exception e) {
                Log.e(TAG, "Failed to Feed Object! Error=" + e);
            }
        }
    }
}
