package eu.faircode.xlua.x.hook.interceptors.network;

import android.net.DhcpInfo;
import android.text.TextUtils;
import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.GroupedMap;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.interfaces.INullableInit;
import eu.faircode.xlua.x.network.NetUtils;
import eu.faircode.xlua.x.xlua.LibUtil;

public class DhcpInfoInterceptor {
    private static final String TAG = LibUtil.generateTag(DhcpInfoInterceptor.class);

    public static boolean intercept(XParam param, boolean getResult) {
        try {
            Object obj = getResult ? param.tryGetResult(null) : param.getThis();
            if(!(obj instanceof DhcpInfo)) {
                Log.e(TAG, "DhcpInfo Result or Param is null, skipping interception...");
                return false;
            }

            DhcpInfo value = (DhcpInfo) obj;
            GroupedMap map = param.getGroupedMap(NetUtils.GROUP_NAME);
            param.setOldResult(value.toString());

            if(DebugUtil.isDebug())
                Log.d(TAG, "Intercepting DHCP Info=" + value.toString());

            if(value.ipAddress > 0) {
                String ip = map.getValueOrSetting(
                        NetUtils.ASSUMED_WIFI_NET_INF_NAME,
                        NetUtils.intToIpv4(value.ipAddress),
                        param,
                        "network.host.address");
                if(ip != null) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "DHCP Info Replacing\n" +
                                "Old IpAddress=(" + value.ipAddress + ", " + NetUtils.intToIpv4(value.ipAddress) + ")\n" +
                                "New IpAddress=(" + NetUtils.ipv4ToInt(ip) + ", " + ip + ")");
                    value.ipAddress = NetUtils.ipv4ToInt(ip);
                }
            }

            if(value.gateway > 0) {
                String gateway = map.getValueOrSetting(
                        NetUtils.ASSUMED_WIFI_NET_INF_NAME,
                        NetUtils.intToIpv4(value.gateway),
                        param,
                        "network.gateway");
                if(gateway != null) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "DHCP Info Replacing\n" +
                                "Old Gateway=(" + value.gateway + ", " + NetUtils.intToIpv4(value.gateway) + ")\n" +
                                "New Gateway=(" + NetUtils.ipv4ToInt(gateway) + ", " + gateway + ")");
                    value.gateway = NetUtils.ipv4ToInt(gateway);
                }
            }

            if(value.netmask > 0) {
                String netmask = map.getValueOrSetting(
                        NetUtils.ASSUMED_WIFI_NET_INF_NAME,
                        NetUtils.intToIpv4(value.netmask),
                        param,
                        "network.netmask");
                if(netmask != null) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "DHCP Info Replacing\n" +
                                "Old Netmask=(" + value.netmask + ", " + NetUtils.intToIpv4(value.netmask) + ")\n" +
                                "New Netmask=(" + NetUtils.ipv4ToInt(netmask) + ", " + netmask + ")");
                    value.netmask = NetUtils.ipv4ToInt(netmask);
                }
            }

            if(value.dns1 > 0 || value.dns2 > 0) {
                String dnsList = map.getValueOrDefault(
                        NetUtils.ASSUMED_WIFI_NET_INF_NAME,
                        "dns",
                        Str.combineWithDelimiter(
                                param.getSetting("network.dns.list"),
                                param.getSetting("network.dns"), Str.COMMA, false));

                if(!TextUtils.isEmpty(dnsList)) {
                    String[] parts = dnsList.split(",");
                    if(value.dns1 > 0) {
                        value.dns1 = NetUtils.ipv4ToInt(parts[0]);
                        if(value.dns2 > 0) {
                            value.dns2 = parts.length > 1 ? NetUtils.ipv4ToInt(parts[1]) : 0;
                        }
                    } else if(value.dns2 > 0) {
                        value.dns2 = NetUtils.ipv4ToInt(parts[0]);
                    }

                    if(DebugUtil.isDebug())
                        Log.d(TAG, "DHCP Info Replacing\n" +
                                "Old DNS1=(" + value.dns1 + ", " + NetUtils.intToIpv4(value.dns1) + ")\n" +
                                "Old DNS2=(" + value.dns2 + ", " + NetUtils.intToIpv4(value.dns2) + ")\n" +
                                "New DNS List=(" + dnsList + ")");
                }
            }

            if(value.serverAddress > 0) {
                String serverAddress = map.getValueOrSetting(
                        NetUtils.ASSUMED_WIFI_NET_INF_NAME,
                        NetUtils.intToIpv4(value.netmask),
                        param,
                        "network.dhcp.server");
                if(serverAddress != null) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "DHCP Info Replacing\n" +
                                "Old Server Address=(" + value.serverAddress + ", " + NetUtils.intToIpv4(value.serverAddress) + ")\n" +
                                "New Server Address=(" + NetUtils.ipv4ToInt(serverAddress) + ", " + serverAddress + ")");
                    value.serverAddress = NetUtils.ipv4ToInt(serverAddress);
                }
            }

            if(value.leaseDuration > 0) {
                String leaseDurationSeconds = map.getValueOrNullableInit(
                        NetUtils.ASSUMED_WIFI_NET_INF_NAME,
                        "dhcp_lease_seconds",
                        new INullableInit() {
                            @Override
                            public Object initGetObject() { return String.valueOf(NetUtils.generateDHCPLeaseTime()); } });

                int newLease = Integer.parseInt(leaseDurationSeconds);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "DHCP Info Replacing\n" +
                            "Old Lease=" + value.leaseDuration + "\n" +
                            "New Lease=" + newLease);

                value.leaseDuration = newLease;
            }

            param.setNewResult(value.toString());
            if(getResult) {
                param.setResult(value);
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, StrBuilder.create().ensureOneNewLinePer(true)
                        .appendLine("DHCP Info Old=")
                        .appendLine(param.getOldResult())
                        .appendLine("DHCP Info New=")
                        .appendLine(param.getNewResult())
                        .toString(true));

            return true;
        }catch (Throwable e) {
            Log.e(TAG, "Failed to Intercept DHCP Info Error: " + e);
            return false;
        }
    }
}
