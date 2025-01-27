package eu.faircode.xlua.x.xlua.commands;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.runtime.reflect.DynType;
import eu.faircode.xlua.x.runtime.reflect.DynamicType;
import eu.faircode.xlua.x.xlua.IBundleData;
import eu.faircode.xlua.x.xlua.PacketBase;
import eu.faircode.xlua.x.xlua.configs.XPConfig;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;

public class XPacket<T> {
    private static final String TAG = "XLua.XPacket";
    
    public static void test(Context context) {
        XPConfig config = new XPConfig();
        XPacket.apply(config)
                .consumePackageInfo(1000, "com.cool", true)
                .synPackageInfoWithExtra()
                .call(context, "putConfig");
    }

    public static final String FIELD_KEY = "_packet_key";
    public static final String FIELD_CODE = "_packet_code";
    public static final String FIELD_P_INFO = "_packet_pkg_info";
    public static final String FIELD_EXTRA = "_packet_extras";

    public static <T> XPacket<T> from(Bundle extras, Class<T> clazz) {
        XPacket<T> packet = new XPacket<>();
        packet.fromBundle(extras, clazz);
        return packet;
    }

    public static <T> XPacket<T> push(T value) { return create(ActionFlag.PUSH, -1, null, false, value, false); }
    public static <T> XPacket<T> push(int uid, T value) { return create(ActionFlag.PUSH, uid, UserIdentity.GLOBAL_NAMESPACE, false, value, true); }
    public static <T> XPacket<T> push(int uid, String packageName, T value) { return create(ActionFlag.PUSH, uid, packageName, false, value, true); }
    public static <T> XPacket<T> push(int uid, String packageName, boolean kill, T value) { return create(ActionFlag.PUSH, uid, packageName, kill, value, true); }

    public static <T> XPacket<T> delete(T value) { return create(ActionFlag.DELETE, -1, null, false, value, false); }
    public static <T> XPacket<T> delete(int uid, String packageName, T value) { return create(ActionFlag.DELETE, uid, packageName, false, value, true); }
    public static <T> XPacket<T> delete(int uid, T value) { return create(ActionFlag.DELETE, uid, UserIdentity.GLOBAL_NAMESPACE, false, value, true); }
    public static <T> XPacket<T> delete(int uid, String packageName, boolean kill, T value) { return create(ActionFlag.DELETE, uid, packageName, kill, value, true); }

    public static <T> XPacket<T> apply(T value) { return create(ActionFlag.APPLY, -1, null, false, value, false); }
    public static <T> XPacket<T> apply(int uid, T value) { return create(ActionFlag.APPLY, uid, UserIdentity.GLOBAL_NAMESPACE, false, value, true); }
    public static <T> XPacket<T> apply(int uid, String packageName, T value) { return create(ActionFlag.APPLY, uid, packageName, false, value, true); }
    public static <T> XPacket<T> apply(int uid, String packageName, boolean kill, T value) { return create(ActionFlag.APPLY, uid, packageName, kill, value, true); }

    public static <T> XPacket<T> create(ActionFlag flag, int uid, String packageName, boolean kill, T value, boolean doConsumePackageInfo) {
        XPacket<T> packet = new XPacket<>();
        packet.key = "";
        packet.code = flag;

        if(uid > -1)
            packet.packageInfo.uid = uid;

        if(!Str.isEmpty(packageName))
            packet.packageInfo.packageName = packageName;

        packet.packageInfo.kill = kill;

        if(value != null) {
            packet.extra = value;
            if(doConsumePackageInfo && value instanceof IPkgInfo) {
                IPkgInfo info = (IPkgInfo)value;
                info.consumePackageInfo(packet.packageInfo);    //In some cases like Config this is not needed from "client => service"
            }
        }

        return packet;
    }

    private boolean doConsumeId = true;

    public XPacket<T> setDoConsume(boolean doConsume) {
        this.doConsumeId = doConsume;
        return this;
    }

    /**/
    public String key;
    public ActionFlag code;
    public XPackageInfo packageInfo = new XPackageInfo();
    public T extra;
    
    public int getUserId() { return packageInfo.getUserId(); }
    public String getPackageName() { return packageInfo.packageName; }

    public XPacket<T> consumeExtra(T extra, boolean setPkgInfo) {
        this.extra = extra; //?
        if(extra != null) {
            if(setPkgInfo && extra instanceof IPkgInfo) {
                IPkgInfo pkgInfo = (IPkgInfo) extra;
                pkgInfo.consumePackageInfo(packageInfo);
            }
        }

        return this;
    }
    
    public XPacket<T> consumePackageInfo(int uid, String packageName) { return consumePackageInfo(uid, packageName, false, false); }
    public XPacket<T> consumePackageInfo(int uid, String packageName, boolean kill) { return consumePackageInfo(uid, packageName, kill, doConsumeId); }
    public XPacket<T> consumePackageInfo(int uid, String packageName, boolean kill, boolean setPkgInfoToExtra) {
        packageInfo.uid = uid;
        packageInfo.packageName = packageName;
        packageInfo.kill = kill;
        if(setPkgInfoToExtra && extra != null)
            return synPackageInfoWithExtra();

        return this;
    }

    public XPacket<T> synPackageInfoWithExtra() {
        if(extra instanceof IPkgInfo) {
            IPkgInfo pkgInfo = (IPkgInfo) extra;
            pkgInfo.consumePackageInfo(packageInfo);
        }

        return this;
    }


    public <TR> TR callAs(Context context, String commandName) { return callAs(context, commandName, -1, null, false); }
    public <TR> TR callAs(Context context, String commandName, int uid, String packageName) { return callAs(context, commandName, uid, packageName, null);  }
    public <TR> TR callAs(Context context, String commandName, int uid, String packageName, Boolean kill) {
        Bundle res = call(context, commandName, uid, packageName, kill);
        try {
            Class<TR> dynamicType = new DynType<TR>().getClazz();
            TR result = dynamicType.newInstance();
            if(result instanceof IBundleData) {
                IBundleData b = (IBundleData) result;
                b.populateFromBundle(res);
            }

            return result;
        }catch (Exception e) {
            Log.e(TAG, "Error Executing Command as.. Error=" + e);
            return null;
        }
    }

    public A_CODE callRes(Context context, String commandName) { return callRes(context, commandName, -1, null, false); }
    public A_CODE callRes(Context context, String commandName, int uid, String packageName) { return callRes(context, commandName, uid, packageName, null);  }
    public A_CODE callRes(Context context, String commandName, int uid, String packageName, Boolean kill) { return A_CODE.fromBundle(call(context, commandName, uid, packageName, kill)); }

    public Bundle call(Context context, String commandName) { return call(context, commandName, -1, null, false); }
    public Bundle call(Context context, String commandName, int uid, String packageName) { return call(context, commandName, uid, packageName, null);  }
    public Bundle call(Context context, String commandName, int uid, String packageName, Boolean kill) {
        if(Str.isEmpty(commandName))
            return A_CODE.toBundle(A_CODE.FAILED);

        if(uid > -1)
            packageInfo.uid = uid;

        if(!Str.isEmpty(packageName)) {
            packageInfo.packageName = packageName;
            if(kill != null)
                packageInfo.kill = kill;
        }

        if(doConsumeId && extra instanceof IPkgInfo) {
            IPkgInfo pkgInfo = (IPkgInfo) extra;
            pkgInfo.consumePackageInfo(packageInfo);
        }

        Bundle res = XProxyContent.luaCall(context, commandName, toBundle());
        if(res == null)
            return A_CODE.toBundle(A_CODE.FAILED);

        return res;
    }

    public void fromBundle(Bundle b, Class<T> clazz) {
        if(b != null) {
            key = b.getString(FIELD_KEY);
            code = ActionFlag.fromInt(b.getInt(FIELD_CODE));

            packageInfo = new XPackageInfo();
            if(b.containsKey(FIELD_P_INFO)) {
                Bundle bInfo = b.getBundle(FIELD_P_INFO);
                if(bInfo != null) {
                    packageInfo.fromBundle(bInfo);
                }
            }


            Bundle bExtra = b.getBundle(FIELD_EXTRA);
            if(bExtra != null) {
                if(clazz != null && DynamicType.classImplementInterface(clazz, IBundleData.class)) {
                    try {
                        extra = clazz.newInstance();
                        IBundleData bObj = (IBundleData) extra;
                        bObj.populateFromBundle(bExtra);
                        //pin the "user info" object ?
                        if(extra instanceof IPkgInfo) {
                            IPkgInfo pObj = (IPkgInfo) extra;
                            pObj.consumePackageInfo(packageInfo);
                        }
                    }catch (Exception e) {
                        Log.e(TAG, "Failed to read extra for packet! Error=" + e);
                    }
                }
            }
        }
    }



    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putString(FIELD_KEY, key);
        b.putInt(FIELD_CODE, code.getValue());

        if(packageInfo != null)
            b.putBundle(FIELD_P_INFO, packageInfo.toBundle());

        if(extra != null && extra instanceof IBundleData) {
            IBundleData bObj = (IBundleData) extra;
            Bundle objBundle = bObj.toBundle();
            if(objBundle != null) {
                b.putBundle(FIELD_EXTRA, objBundle);
                if(DebugUtil.isDebug()) {
                    StrBuilder sb = StrBuilder.create().ensureOneNewLinePer(true);
                    for(String k : objBundle.keySet())
                        sb.appendLine("Value Bundle Key[" + k + "]");

                    Log.d(TAG, "Extras Value Bundle Keys that is being sent to the Command: " + sb.toString(true));
                }
            } else
                Log.e(TAG, "Error Writing Extras to the Bundle for the Command! When To Bundle on Object Value, it is null!");
        } else
            Log.e(TAG, "Error Writing Extras to the Bundle for the Command! Is Null ? " + (extra == null) + " Is Instance of IBundleData: " + (extra instanceof IBundleData));


        if(DebugUtil.isDebug()) {
            StrBuilder sb = StrBuilder.create().ensureOneNewLinePer(true);
            for(String k : b.keySet())
                sb.appendLine("Value Bundle Key[" + k + "]");

            Log.d(TAG, "Command Bundle Keys that is being sent to the Command: " + sb.toString(true));
        }

        return b;
    }

    public void queryArguments() {

    }


    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Key", key)
                .appendFieldLine("Code", code)
                .appendFieldLine("PkgName", packageInfo.packageName)
                .appendFieldLine("PkgUid", packageInfo.uid)
                .appendFieldLine("PkgKill", packageInfo.kill)
                .appendFieldLine("Value", Str.toStringOrNull(extra))
                .toString(true);
    }
}
