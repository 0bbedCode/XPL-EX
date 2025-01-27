package eu.faircode.xlua.api.xstandard.command;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.api.xstandard.interfaces.IBridgePacketContext;
import eu.faircode.xlua.api.xstandard.interfaces.ISerial;
import eu.faircode.xlua.utilities.BundleUtil;
import eu.faircode.xlua.x.xlua.IBundleData;

public class CallPacket_old extends BridgePacket_old implements IBridgePacketContext {
    private static final String TAG = "XLua.CallPacket";

    private final Bundle extras;

    public CallPacket_old(Context context, String method, Bundle extras, XDatabaseOld db) { this(context, null, method, extras, db, null); }
    public CallPacket_old(Context context, String commandPrefix, String method, Bundle extras, XDatabaseOld db) { this(context, commandPrefix, method, extras, db, null); }
    public CallPacket_old(Context context, String commandPrefix, String method, Bundle extras, XDatabaseOld db, String packageName) {
        super(commandPrefix, method, context, db, packageName);
        this.extras = extras;
    }

    public String getExtraString(String key) { return BundleUtil.readString(this.extras, key, null); }
    public String getExtraString(String key, String defaultValue) { return BundleUtil.readString(this.extras, key, defaultValue); }

    public Integer getExtraInt(String key) { return BundleUtil.readInteger(this.extras, key, null); }
    public Integer getExtraInt(String key, Integer defaultValue) { return BundleUtil.readInteger(this.extras, key, defaultValue); }

    public Boolean getExtraBool(String key) { return BundleUtil.readBoolean(this.extras, key, null); }
    public Boolean getExtraBool(String key, Boolean defaultValue) { return BundleUtil.readBoolean(this.extras, key, defaultValue); }

    public Bundle getExtras() { return extras; }

    public <T extends ISerial> T readExtrasAs(Class<T> clazz) {
        try {
            T inst = clazz.newInstance();
            inst.fromBundle(extras);
            return inst;
        }catch (Exception e) {
            Log.e(TAG, "Failed to read bundle extras! flag=" + " class type=" + clazz.getName() + " " + this + "\ne=" + e);
            return null;
        }
    }

    public <T extends IBundleData> T readExtrasEx(Class<T> clazz) {
        try {
            T inst = clazz.newInstance();
            inst.populateFromBundle(extras);
            return inst;
        }catch (Exception e) {
            Log.e(TAG, "Failed to read bundle extras! flag=" + " class type=" + clazz.getName() + " " + this + "\ne=" + e);
            return null;
        }
    }
}
