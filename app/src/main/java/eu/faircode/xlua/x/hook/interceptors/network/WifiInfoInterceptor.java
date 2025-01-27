package eu.faircode.xlua.x.hook.interceptors.network;

import android.net.wifi.WifiInfo;
import android.text.TextUtils;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.runtime.reflect.DynamicField;
import eu.faircode.xlua.x.runtime.reflect.DynamicMethod;
import eu.faircode.xlua.x.Rnd;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.GroupedMap;
import eu.faircode.xlua.x.network.NetUtils;
import eu.faircode.xlua.x.runtime.reflect.DynamicGetSetPairs;

public class WifiInfoInterceptor {
    private static final String TAG = "XLua.WifiInfoCustomParcel";
    public static final String DEFAULT_MAC_ADDRESS = "02:00:00:00:00:00";

    public static final DynamicGetSetPairs MAC_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mMacAddress")
            .bindGetMethod("getMacAddress")
            .bindSetMethod("setMacAddress");

    public static final DynamicGetSetPairs SSID_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mWifiSsid")
            .bindGetMethod("getSSID")
            .bindSetMethod("setSSID")
            .bindGetMethod("getWifiSsid");  //This one is in form of string

    public static final DynamicMethod WIFI_SSID_TO_STRING = new DynamicMethod("android.net.wifi.WifiSsid", "toString")
            .setAccessible(true);

    public static final DynamicGetSetPairs WIFI_SSID_CREATE_PAIRS = DynamicGetSetPairs.create("android.net.wifi.WifiSsid")
            .bindSetMethod("fromUtf8Text", CharSequence.class)
            .bindSetMethod("fromString", String.class)
            .bindSetMethod("createFromAsciiEncoded", String.class);

    public static final DynamicGetSetPairs BSSID_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mBSSID")
            .bindSetMethod("setBSSID", String.class)
            .bindGetMethod("getBSSID");

    public static final DynamicGetSetPairs NET_ID_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mNetworkId")
            .bindGetMethod("getNetworkId")
            .bindSetMethod("setNetworkId");

    public static final DynamicGetSetPairs ADDRESS_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mIpAddress")
            .bindSetMethod("setInetAddress")
            .bindGetMethod("getIpAddress");

    public static final DynamicField FIELD_MULTI_LINK_MAC_ADDRESS = new DynamicField(WifiInfo.class, "mApMldMacAddress")
            .setAccessible(true);

    public static final List<DynamicField> SPEED_FIELDS = Arrays.asList(
            new DynamicField(WifiInfo.class, "mApMldMacAddress").setAccessible(true),
            new DynamicField(WifiInfo.class, "mTxLinkSpeed").setAccessible(true),
            new DynamicField(WifiInfo.class, "mMaxSupportedTxLinkSpeed").setAccessible(true),
            new DynamicField(WifiInfo.class, "mRxLinkSpeed").setAccessible(true),
            new DynamicField(WifiInfo.class, "mMaxSupportedRxLinkSpeed").setAccessible(true));

    public static boolean intercept(XParam param, boolean getResult) {
        if(param == null) return false;
        try {
            Object obj = getResult ? param.getResult() : param.getThis();
            if(!(obj instanceof WifiInfo))
                throw new Exception("Object is Not instance of WifiIno, Object=" + (obj == null ? "null" : obj.getClass().getName()));

            WifiInfo instance = (WifiInfo) obj;
            if(DebugUtil.isDebug())
                Log.d(TAG, "Intercepting WifiInfo Object, toString=" + instance.toString());

            param.setOldResult(instance.toString());

            GroupedMap map = param.getGroupedMap(NetUtils.GROUP_NAME);
            //Assume its wlan0
            int speed = map.getValueOrDefault(
                    NetUtils.ASSUMED_WIFI_NET_INF_NAME,
                    "generic_speed",
                    Rnd.nextInt(144, 255), false);
            for(DynamicField speedField : SPEED_FIELDS) {
                if(speedField.isValid())
                    speedField.trySetValueInstanceEx(instance, speed);
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Speed Set=" + speed + " (mbps)");

            String newBssid = map.getValueOrSetting(
                    NetUtils.ASSUMED_WIFI_NET_INF_NAME,
                    "bssid",
                    param,
                    "unique.network.bssid", false);
            String oldBssid = BSSID_PAIRS.getValueInstance(instance);
            if(!TextUtils.isEmpty(oldBssid) && !DEFAULT_MAC_ADDRESS.equalsIgnoreCase(oldBssid)) {
                if(!BSSID_PAIRS.setValueInstance(instance, newBssid)) {
                    Log.e(TAG, "Critical Error Failed to Set WifiInfo BSSID, methods seem to be Invalid, please Contact the Developer!");
                } else {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Old BSSID=" + oldBssid + "\n" +
                                        "New BSSID=" + newBssid);
                }
            }

            String newSsid = map.getValueOrSetting(
                    NetUtils.ASSUMED_WIFI_NET_INF_NAME,
                    "ssid",
                    param,
                    "unique.network.ssid", false);
            Object ssidObject = SSID_PAIRS.getValueInstanceMethodsFirst(instance);
            if(ssidObject != null) {
                String oldSsid = null;
                if(!(ssidObject instanceof CharSequence)) {
                    //Not a String but a WifiSsid Object
                    Object ssidVal = WIFI_SSID_TO_STRING.tryInstanceInvokeEx(ssidObject);
                    if(ssidVal instanceof CharSequence)
                        oldSsid = (String) ssidVal;
                    else
                        Log.w(TAG,  "WIFI SSID Object is null... cant resolve SSID name");
                } else {
                    oldSsid = (String) ssidObject;
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG, "CURRENT SSID=" + oldSsid);

                if(!TextUtils.isEmpty(oldSsid)) {
                    Object newSsidObject = WIFI_SSID_CREATE_PAIRS.getValueInstance(null, newSsid);
                    if(newSsidObject == null)
                        WIFI_SSID_CREATE_PAIRS.getValueInstance(null, Str.convertToUTF8CharSequence(newSsid));

                    if(newSsidObject == null || !SSID_PAIRS.setValueInstance(instance, newSsidObject)) {
                        Log.e(TAG, "Critical Error Failed to Set SSID Object, Contact Developer!");
                    } else {
                        if(DebugUtil.isDebug())
                            Log.d(TAG, "Old SSID=" + oldSsid + "\n" +
                                            "New SSID=" + newSsid);
                    }
                }
            }

            String oldMac = MAC_PAIRS.getValueInstance(instance);
            String newMac = map.getValueOrSetting(
                    NetUtils.ASSUMED_WIFI_NET_INF_NAME,
                    "hardware_address",
                    param,
                    "unique.network.mac.address", false);
            if(!TextUtils.isEmpty(oldMac) && !DEFAULT_MAC_ADDRESS.equalsIgnoreCase(oldMac)) {
                if(!MAC_PAIRS.setValueInstance(instance, newMac)) {
                    Log.e(TAG, "Critical Error Failed to Set WifiInfo Hardware Address, methods seem to be Invalid, please Contact the Developer!");
                } else {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Old MAC=" + oldMac + "\n" +
                                        "New MAC=" + newMac);
                }
            }

            //Set this to Null always
            if(FIELD_MULTI_LINK_MAC_ADDRESS.isValid())
                FIELD_MULTI_LINK_MAC_ADDRESS.trySetValueInstanceEx(instance, null);

            Object netIdObject = NET_ID_PAIRS.getValueInstance(instance);
            if(netIdObject instanceof Integer) {
                NET_ID_PAIRS.setValueInstance(instance, -1);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Old Net ID=" + ((int)netIdObject) + "\n" +
                                    "New Net ID=-1");
            }

            Object addressObject = ADDRESS_PAIRS.getValueInstance(instance);
            if(addressObject != null) {
                String address = "";
                if(addressObject instanceof Integer) {
                    InetAddress toAddress = NetUtils.intToInetAddress((int)addressObject);
                    if(toAddress == null)
                        Log.w(TAG, "IPAddress Object is Null...");
                    else
                        address = toAddress.getHostAddress();
                } else {
                    address = ((InetAddress) addressObject).getHostAddress();
                }

                if(!"127.0.0.1".equalsIgnoreCase(address) && !TextUtils.isEmpty(address)) {
                    String newAddress = map.getValueOrSetting(
                            NetUtils.ASSUMED_WIFI_NET_INF_NAME,
                            address,
                            param,
                            "network.host.address");
                    if(!TextUtils.isEmpty(newAddress)) {
                        if(ADDRESS_PAIRS.setValueInstance(instance, Inet4Address.getByName(newAddress))) {
                            if(DebugUtil.isDebug())
                                Log.d(TAG, "Old Address=" + address + "\n" +
                                                "New Address=" + newAddress);
                        }else {
                            Log.e(TAG, "Failed to Set new Address! Old=" + address + " New=" + newAddress);
                        }
                    }
                } else {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Skipping Address Spoof=" + address);
                }
            }

            param.setNewResult(instance.toString());
            if(getResult) param.setResult(instance);
            return true;
        }catch (Throwable e) {
            Log.e(TAG, "Failed to Intercept and Clean WifiInfo Object, Error=" + e + " Stack=" + Log.getStackTraceString(e));
            return false;
        }
    }
}
