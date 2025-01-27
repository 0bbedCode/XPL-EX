package eu.faircode.xlua.x.xlua.commands.packet;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.IBundleData;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;

public class CallPacket extends BridgePacket {
    private static final String TAG = "XLua.CallPacket";

    public final Bundle extras;
    private UserIdentity userIdentity;

    public int getExtraInt(String key) { return getExtraInt(key, 0); }
    public int getExtraInt(String key, int defaultValue) { return extras == null ? defaultValue : extras.getInt(key, defaultValue); }

    public boolean getExtraBool(String key) { return getExtraBool(key, false); }
    public boolean getExtraBool(String key, boolean defaultValue) { return extras == null ? defaultValue : extras.getBoolean(key, defaultValue); }

    public String getExtraString(String key) { return getExtraString(key, null); }
    public String getExtraString(String key, String defaultValue) { return extras == null ? defaultValue : extras.getString(key, defaultValue); }

    public String getCategory() { return getUserIdentification().getCategory(); }
    public int getUid() { return getUserIdentification().getUid(); }
    public int getUserId() { return getUserIdentification().getUserId(true); }

    public UserIdentity getUserIdentification() {
        if(userIdentity == null) {
            userIdentity = new UserIdentity();
            UserIdentityIO.fromBundleEx(extras, userIdentity, false);
        }

        return userIdentity;
    }

    public CallPacket(Context context, String commandPrefix, String method, Bundle extras, SQLDatabase database) { this(context, commandPrefix, method, extras, database, null); }
    public CallPacket(Context context, String commandPrefix, String method, Bundle extras, SQLDatabase database, String packageName) {
        super(commandPrefix, method, context, database, packageName);
        this.extras = extras;
    }

    public <T extends IBundleData> T readExtraAs(Class<T> clazz) {
        try {
            T item = clazz.newInstance();
            item.populateFromBundle(extras);
            return item;
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to Read Call Packet extras to Object! Type:%s  Error%s", clazz, e));
            return null;
        }
    }
}
