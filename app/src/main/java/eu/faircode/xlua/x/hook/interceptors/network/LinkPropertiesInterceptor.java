package eu.faircode.xlua.x.hook.interceptors.network;

import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.RouteInfo;
import android.text.TextUtils;
import android.util.Log;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import eu.faircode.xlua.x.data.GroupedMap;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.runtime.reflect.DynamicField;
import eu.faircode.xlua.x.network.NetUtils;
import eu.faircode.xlua.x.network.ipv6.IPv6Converter;
import eu.faircode.xlua.x.network.ipv6.Inet6AddressInfo;
import eu.faircode.xlua.x.runtime.reflect.DynamicGetSetPairs;
import eu.faircode.xlua.x.xlua.LibUtil;

public class LinkPropertiesInterceptor {
    private static final String TAG = LibUtil.generateTag(LinkPropertiesInterceptor.class);

    public static final DynamicField FIELD_LINK_ADDRESS_ADDRESS = new DynamicField(LinkAddress.class, "address")
            .setAccessible(true);

    public static final DynamicGetSetPairs DNS_PAIRS = DynamicGetSetPairs.create(LinkProperties.class)
            .bindField("mDnses")
            .bindSetMethod("setDnsServers", Collection.class)
            .bindGetMethod("getDnsServers");

    public static final DynamicGetSetPairs DOMAINS_PAIRS = DynamicGetSetPairs.create(LinkProperties.class)
            .bindField("mDomains")
            .bindGetMethod("getDomains")
            .bindSetMethod("setDomains", String.class);

    public static final DynamicGetSetPairs DHCP_PAIRS = DynamicGetSetPairs.create(LinkProperties.class)
            .bindField("mDhcpServerAddress")
            .bindSetMethod("setDhcpServerAddress", Inet4Address.class)
            .bindGetMethod("getDhcpServerAddress");

    public static final DynamicGetSetPairs ROUTES_PAIRS = DynamicGetSetPairs.create(LinkProperties.class)
            .bindField("mRoutes")
            .bindGetMethod("getRoutes");

    public static final DynamicGetSetPairs HAS_GATEWAY_PAIRS = DynamicGetSetPairs.create(RouteInfo.class)
            .bindField("mHasGateway")
            .bindGetMethod("hasGateway");

    public static final DynamicGetSetPairs GATEWAY_PAIRS = DynamicGetSetPairs.create(RouteInfo.class)
            .bindField("mGateway")
            .bindGetMethod("getGateway");

    public static final DynamicField INET6_ADDRESS_HOLDER = new DynamicField(Inet6Address.class, "holder6")
            .setAccessible(true);

    public static final DynamicField INET6_IP_ADDRESS_FIELD = new DynamicField("java.net.InetAddress$InetAddressHolder", "ipaddress")
            .setAccessible(true);

    public static boolean intercept(XParam param, boolean isResult) {
        try {
            Object res = isResult ? param.getResult() : param.getThis();
            if(!(res instanceof LinkProperties))
                throw new Exception("Object is not Instance of LinkProperties, Type=" + (res == null ? "null" : res.getClass().getName()));

            LinkProperties instance = (LinkProperties) res;
            if(DebugUtil.isDebug())
                Log.d(TAG, "Intercepting LinkProperties, toString=" + res.toString());

            param.setOldResult(res.toString());
            GroupedMap map = param.getGroupedMap(NetUtils.GROUP_NAME);
            String interfaceName = instance.getInterfaceName();
            List<LinkAddress> addresses = instance.getLinkAddresses();
            if(DebugUtil.isDebug())
                Log.d(TAG, "Starting Interface: " + interfaceName + " cleaning!");

            if(!FIELD_LINK_ADDRESS_ADDRESS.isValid()) {
                Log.e(TAG, "Error Getting Link Addresses for Interface [" + interfaceName + "] Please Contact the DEV");
            } else {
                for(LinkAddress linkAddress : addresses) {
                    InetAddress netAddress = linkAddress.getAddress();
                    if(netAddress != null) {
                        if(netAddress instanceof Inet6Address) {
                            try {
                                //Make this into a Helper Object so anything else needs to Force set [Inet6Address] Object Address
                                Inet6AddressInfo addressInfo = new Inet6AddressInfo((Inet6Address) netAddress);
                                String origAddress = netAddress.getHostAddress();
                                String fakeAddress = map.getValueOrDefault(
                                        interfaceName,
                                        netAddress.getHostAddress(),
                                        addressInfo.generateRandomAddress());
                                if(fakeAddress.equalsIgnoreCase(origAddress))
                                    continue;

                                byte[] fakeBytes = IPv6Converter.toBytes(fakeAddress);
                                byte[] realBytes = IPv6Converter.toBytes(netAddress.getHostAddress());
                                if(DebugUtil.isDebug())
                                    Log.d(TAG,
                                            "Spoofing IPV6 Address [" + interfaceName + "]\n" +
                                            "Old Address=" + origAddress + "\n" +
                                            "New Address=" + fakeAddress + "\n" +
                                            "Old Bytes=" + Str.bytesToHex(netAddress.getAddress()) + "\n" +
                                            "Old Bytes=" + Str.bytesToHex(realBytes) + "\n" +
                                            "New Bytes=" + Str.bytesToHex(fakeBytes));

                                Object holder = INET6_ADDRESS_HOLDER.tryGetValueInstanceEx(netAddress);
                                if(holder == null) {
                                    Log.d(TAG, "Error Getting IPV6 Holder Object [holder6] Network Interface [" + interfaceName + "]");
                                    continue;
                                }

                                //Set the new BYTES to the [InetAddress$InetAddressHolder] Object, Set the Instance Field on InetAddress Object (too make sure)
                                INET6_IP_ADDRESS_FIELD.trySetValueInstanceEx(holder, fakeBytes);
                                INET6_ADDRESS_HOLDER.trySetValueInstanceEx(netAddress, holder);
                                FIELD_LINK_ADDRESS_ADDRESS.trySetValueInstanceEx(linkAddress, netAddress);
                                if(DebugUtil.isDebug())
                                    Log.d(TAG, "Spoofed IPV6 [" + interfaceName + "]\n" +
                                            "Is Local Link=" + (addressInfo.isLinkLocal) + "\n" +
                                            "Is Global=" + (addressInfo.isGlobal) + "\n" +
                                            "Is Permanent=" + (!addressInfo.isTemporary) + "\n" +
                                            "Old Address=" + netAddress.getHostAddress() + "\n" +
                                            "New Address=" + fakeAddress);
                            }catch (Exception e) {
                                Log.e(TAG, "Error Spoofing IPV6 Address on Network Interface [" + interfaceName + "] Error=" + e + " Stack=" + Log.getStackTraceString(e));
                            }
                        }
                        else {
                            //This condition Should never be met
                            if(!(netAddress instanceof Inet4Address))
                                Log.e(TAG,  "Network Address Object is not [Inet4Address] or [Inet6Address] weird, please Contact DEV, Type=" + netAddress.getClass().getName());
                            else if(!("wlan0".equalsIgnoreCase(interfaceName) ||
                                        "dummy0".equalsIgnoreCase(interfaceName) ||
                                        (!netAddress.isLoopbackAddress() && netAddress.isSiteLocalAddress())))  {
                                if(DebugUtil.isDebug())
                                    Log.w(TAG, "Skipping Interface [" + interfaceName + "] Address=" + netAddress.getHostAddress() + " Does not meet requirements");
                            } else {
                                String orgAddress = netAddress.getHostAddress();
                                String newAddress = map.getValueOrSetting(
                                        interfaceName,
                                        orgAddress,
                                        param,
                                        "network.host.address");
                                if(orgAddress == null || orgAddress.equalsIgnoreCase(newAddress) || newAddress == null)
                                    continue;

                                if(!FIELD_LINK_ADDRESS_ADDRESS.trySetValueInstanceEx(linkAddress, Inet4Address.getByName(newAddress)))
                                    Log.e(TAG, "Failed to Set new Link Address IPV4 Address, new Address=" + newAddress + " Interface Name [" + interfaceName + "], Critical Error please Contact DEV");
                                else if (DebugUtil.isDebug())
                                    Log.d(TAG, "Spoofed IPV4 Address on Network Interface [" + interfaceName + "]\n" +
                                            "Old Address=" + netAddress.getHostAddress() + "\n" +
                                            "New Address=" + newAddress);
                            }
                        }
                    }
                }
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Spoofing DNS Server List for Network Interface [" + interfaceName + "]");

            List<InetAddress> oldDnsList = instance.getDnsServers();
            if(!oldDnsList.isEmpty() || "wlan0".equalsIgnoreCase(interfaceName)) {
                String dnsList = map.getValueOrDefault(
                        interfaceName,
                        "dns",
                        Str.combineWithDelimiter(
                                param.getSetting("network.dns.list"),
                                param.getSetting("network.dns"),
                                Str.COMMA,
                                false));

                List<String> allowedDnsEs = new ArrayList<>(Arrays.asList(Str.split(dnsList, Str.COMMA, true, true)));
                if(DebugUtil.isDebug())
                    Log.d(TAG,  "DNS Filtering for Interface [" + interfaceName + "]\n" +
                            "New DNS List Size=" + allowedDnsEs.size() + "\n" +
                            "New DNS List=(" + Str.joinList(allowedDnsEs, ",") + ")\n" +
                            "Old DNS List Size=" + oldDnsList.size() + "\n" +
                            "Old DNS List=(" + NetUtils.joinInetAddresses(oldDnsList) + ")");

                List<InetAddress> newDnsList = new ArrayList<>();
                for(String dns : allowedDnsEs) {
                    InetAddress adder = NetUtils.isIpv6OrInet6(dns) ?
                            NetUtils.parseIpv6ToInetAddress(dns) :
                            NetUtils.parseIpv4ToInetAddress(dns);
                    if(adder != null && !newDnsList.contains(adder))
                        newDnsList.add(adder);
                    else {
                        Log.w(TAG, "Skipped Parsing DNS (is null or invalid) " + adder);
                    }
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG, "DNS Filtering Finished for Interface [" + interfaceName + "] Now Setting new DNS List, Size=" + newDnsList.size());

                if(DNS_PAIRS.setValueInstance(instance, newDnsList)) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "DNS Servers Set for Network Interface [" + interfaceName + "]\n" +
                                "Old DNS List=" + NetUtils.joinInetAddresses(oldDnsList) + "\n" +
                                "New DNS List=" + NetUtils.joinInetAddresses(newDnsList));
                } else {
                    Log.e(TAG, "Critical Error Failed to set NEW DNS List no valid member to Invoke, contact DEV");
                }
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Spoofing Network Domains for Network Interface [" + interfaceName + "]");

            if(!("wlan0".equalsIgnoreCase(interfaceName) || "dummy0".equalsIgnoreCase(interfaceName))) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Skipping Network Interface [" + interfaceName + "] For targeted Domain Spoofing...");
            } else {
                String allowedDomains = map.getValueOrDefault(
                        interfaceName,
                        "domains",
                        Str.ensureIsValidOrNull(param.getSetting("network.domains")),
                        false,
                        true);
                if(!DOMAINS_PAIRS.setValueInstance(instance, allowedDomains)) {
                    Log.e(TAG, "Failed to Set Network Domains for Network Interface [" + interfaceName + "], Critical...");
                } else {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Domains Spoofed for Network Interface [" + interfaceName + "]\n" +
                                "Old Domains=(" + instance.getDomains() + ")\n" +
                                "New Domains=(" + allowedDomains + ")");
                }
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Spoofing DHCP Server for Network Interface [" + interfaceName + "]");

            Inet4Address serverAddress = DHCP_PAIRS.getValueInstance(instance);
            if(serverAddress != null) {
                String newServerAddress = map.getValueOrSetting(
                        interfaceName,
                        "server_address",
                        param,
                        "network.dhcp.server");
                InetAddress fakeAddressInstance = NetUtils.parseIpv4ToInetAddress(newServerAddress);
                if(TextUtils.isEmpty(newServerAddress) || !(fakeAddressInstance instanceof Inet4Address)) {
                    Log.e(TAG, "Error Spoofing DHCP Server, either new Server is NULL/Empty or Fake Instance is not Instance of [Inet4Address] [" + interfaceName + "]");
                } else {
                    if(!DHCP_PAIRS.setValueInstance(instance, fakeAddressInstance))
                        Log.e(TAG, "Error  Spoofing DHCP Server for Network Interface [" + interfaceName + "], Critical Error");
                    else if (DebugUtil.isDebug()){
                        Log.d(TAG, "Spoofed DHCP Server on Network Interface [" + interfaceName + "]\n" +
                                "Old DHCP=" + serverAddress + "\n" +
                                "New DHCP=" + newServerAddress);
                    }
                }
            }

            param.setNewResult(instance.toString());
            if(isResult) param.setResult(instance);
            return true;
        }catch (Throwable e) {
            Log.e(TAG, "Failed to Intercept LinkProperties, Error=" + e + " Stack=" + Log.getStackTraceString(e));
            return false;
        }
    }
}
