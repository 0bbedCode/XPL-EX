package eu.faircode.xlua.x.hook.interceptors.network;

import android.system.Os;
import android.system.OsConstants;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.lang.reflect.Array;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.random.randomizers.RandomMAC;
import eu.faircode.xlua.tools.FileReaderEx;
import eu.faircode.xlua.utilities.FileUtil;
import eu.faircode.xlua.x.runtime.reflect.DynamicField;
import eu.faircode.xlua.x.runtime.reflect.ReflectUtil;
import eu.faircode.xlua.x.data.GroupedMap;
import eu.faircode.xlua.x.network.NetUtils;
import eu.faircode.xlua.x.network.ipv6.IPv6Converter;
import eu.faircode.xlua.x.network.ipv6.Inet6AddressInfo;
import eu.faircode.xlua.x.network.ipv6.Inet6File;
import eu.faircode.xlua.x.xlua.LibUtil;

public class NetworkInterfaceInterceptor {
    private static final String TAG = LibUtil.generateTag(NetworkInterfaceInterceptor.class);

    //   */
    //    public StructIfaddrs[] getifaddrs() throws ErrnoException;
    //
    /*public final class StructIfaddrs {
        public final String ifa_name;
        public final int ifa_flags;
        public final InetAddress ifa_addr;
        public final InetAddress ifa_netmask;
        public final InetAddress ifa_broadaddr;
        public final byte[] hwaddr;*/
        /*public StructIfaddrs(String ifa_name, int ifa_flags, InetAddress ifa_addr, InetAddress ifa_netmask,
                             InetAddress ifa_broadaddr, byte[] hwaddr) {
            this.ifa_name = ifa_name;
            this.ifa_flags = ifa_flags;
            this.ifa_addr = ifa_addr;
            this.ifa_netmask = ifa_netmask;
            this.ifa_broadaddr = ifa_broadaddr;
            this.hwaddr = hwaddr;
        }
        @Override public String toString() {
            return Objects.toString(this);
        }
    }*/


    /*
        @Nullable
        public static String getDeviceIpAddress(@NonNull ConnectivityManager connectivityManager) {
            LinkProperties linkProperties = connectivityManager.getLinkProperties(connectivityManager.getActiveNetwork());
            InetAddress inetAddress;
            for(LinkAddress linkAddress : linkProperties.getLinkAddresses()) {
                inetAddress = linkAddress.getAddress();
                if (inetAddress instanceof Inet4Address
                        && !inetAddress.isLoopbackAddress()
                        && inetAddress.isSiteLocalAddress()) {
                    return inetAddress.getHostAddress();
                }
            }
            return null;
        }
     */

    //public static

    private static final String STRUCT_CLASS_NAME = "android.system.StructIfaddrs";

    //String
    public static final DynamicField FIELD_IF_ADDRS_IFA_NAME = new DynamicField(STRUCT_CLASS_NAME, "ifa_name")
           .setAccessible(true);

    //int
    public static final DynamicField FIELD_IF_ADDRS_IFA_FLAGS = new DynamicField(STRUCT_CLASS_NAME, "ifa_flags")
            .setAccessible(true);

    //InetAddress
    public static final DynamicField FIELD_IF_ADDRS_IFA_ADDR = new DynamicField(STRUCT_CLASS_NAME, "ifa_addr")
            .setAccessible(true);

    //InetAddress
    public static final DynamicField FIELD_IF_ADDRS_IFA_NETMASK = new DynamicField(STRUCT_CLASS_NAME, "ifa_netmask")
            .setAccessible(true);

    //InetAddress
    public static final DynamicField FIELD_IF_ADDRS_IFA_BROADADDR = new DynamicField(STRUCT_CLASS_NAME, "ifa_broadaddr")
            .setAccessible(true);
    //byte[]
    public static final DynamicField FIELD_IF_ADDRS_IFA_HWADDR = new DynamicField(STRUCT_CLASS_NAME, "hwaddr")
            .setAccessible(true);


    //Command to get the IP ADDRESS of a Interface
    public static final int SIOCGIFADDR
            = ReflectUtil.useFieldValueOrDefaultInt(OsConstants.class, "SIOCGIFADDR", 0x8915, 0);

    public static final int SIOCGIFBRDADDR
            = ReflectUtil.useFieldValueOrDefaultInt(OsConstants.class, "SIOCGIFBRDADDR", 0x8919, 0);

    public static final int SIOCGIFNETMASK
            = ReflectUtil.useFieldValueOrDefaultInt(OsConstants.class, "SIOCGIFNETMASK", 0x891b, 0);

    public static final String INET6_FILE = "/proc/net/if_inet6";

    public static final long RETURN_NULL_HARDWARE_ADDRESS = 170188668L;
    private static final byte[] DEFAULT_MAC_ADDRESS = { 0x02, 0x00, 0x00, 0x00, 0x00, 0x00 };


    public static boolean interceptGetifaddrs(XParam param) {
        try {
            String M_TAG = "[IF_ADDRs] ";
            //Lower versions of Android do not use this method
            //Get a list to spoof ?
            GroupedMap map = param.getGroupedMap(NetUtils.GROUP_NAME);
            RandomMAC macRandomizer = new RandomMAC();
            Object res = param.getResult();
            //get Array of "android.system.StructIfaddrs" from "Os.getifaddrs"
            int length = Array.getLength(res);
            for(int i = 0; i < length; i++) {
                Object ifAddress = Array.get(res, i);
                if(ifAddress != null) {
                    //Spoof "hwaddr" for all
                    String name = FIELD_IF_ADDRS_IFA_NAME.tryGetValueInstanceEx(ifAddress);
                    Object addressField = FIELD_IF_ADDRS_IFA_ADDR.tryGetValueInstanceEx(ifAddress);
                    if(name != null && addressField instanceof InetAddress) {
                        InetAddress address = (InetAddress) addressField;
                        if (address instanceof Inet6Address) {
                            try {
                                                            /*Inet6AddressInfo info = new Inet6AddressInfo((Inet6Address) address);
                            String fakeAddress = null;
                            if (!info.isTemporary) {
                                if ("wlan0".equalsIgnoreCase(name)) {
                                    if (info.isLinkLocal)
                                        fakeAddress = param.getSetting("network.host.address.6.wlan0.local.link.perm");
                                    else if (info.isGlobal)
                                        fakeAddress = param.getSetting("network.host.address.6.wlan0.global.perm");
                                } else if ("dummy0".equalsIgnoreCase(name)) {
                                    if (info.isLinkLocal)
                                        fakeAddress = param.getSetting("network.host.address.6.dummy0.local.link.perm");
                                }
                            }

                            fakeAddress = map.getValue(name, address.getHostAddress(),
                                    fakeAddress == null ?
                                            NetUtils.convertIfInet6ToIpv6Format(info.generateRandomAddress()) :
                                            fakeAddress);*/
                                String ip6TAG = "XLua.IPV6";

                                Inet6AddressInfo info = new Inet6AddressInfo((Inet6Address) address);
                                String fakeAddress = map.getValueOrDefault(name, address.getHostAddress(), info.generateRandomAddress());
                                //Log.d(ip6TAG, "[Old] IPV6 Info=" + address.toString() + " Host Address=" + address.getHostAddress() + " Host Name=" + address.getHostName() + " Fake Address=" + fakeAddress);
                                Log.d(ip6TAG, "[Old] IPV6 Host Address=" + address.getHostAddress() + " Fake Address=" + fakeAddress);

                                byte[] fakeBytes = IPv6Converter.toBytes(fakeAddress);
                                byte[] realBytes = IPv6Converter.toBytes(address.getHostAddress());

                                DynamicField holderField = new DynamicField(Inet6Address.class, "holder6")
                                        .setAccessible(true);

                                //                                Log.d(ip6TAG, "[Old] IPV6 Info=" + address.toString() + " Host Address=" + address.getHostAddress() + " Host Name=" + address.getHostName());
                                //Log.d(ip6TAG, "[Old] IPV6 Info=" + address.toString() + " Host Address=" + address.getHostAddress());
                                Log.d(ip6TAG, "[Old] Host Address=" + address.getHostAddress());

                                Log.d(ip6TAG, "[COMP] IPV6\n" +
                                        "Old Address=" + address.getHostAddress() + "\n" +
                                        "New Address=" + fakeAddress + "\n" +
                                        "Old Bytes=" + Str.bytesToHex(address.getAddress()) + "\n" +
                                        "Old Bytes=" + Str.bytesToHex(realBytes) + "\n" +
                                        "New Bytes=" + Str.bytesToHex(fakeBytes));

                                //byte[] ipaddress;
                                //int scope_id;  // 0
                                //boolean scope_id_set;  // false
                                //NetworkInterface scope_ifname;  // null
                                //boolean scope_ifname_set; // false;
                                Object holder = holderField.tryGetValueInstanceEx(address);
                                if(holder == null) {
                                    Log.d(ip6TAG, "[!] ERROR HOLDER IPV6 IS NULL!");
                                } else {
                                    DynamicField ipaddressField = new DynamicField(holder.getClass(), "ipaddress")
                                            .setAccessible(true);

                                    //Log.d(ip6TAG, "[2] Setting IPV6 Bytes in Holder, first is Valid: " + (ipaddressField.isValid()) + "...");
                                    ipaddressField.trySetValueInstanceEx(holder, fakeBytes);
                                    //Log.d(ip6TAG, "[3] Set IPV6 Address Bytes! Setting Holder Field");
                                    holderField.trySetValueInstanceEx(address, holder);
                                    //Log.d(ip6TAG, "[4] Set IPV6 Field!");
                                    //try {
                                        //Log.d(ip6TAG, "[NEW] IPV6 Info=" + address.toString() + " Host Address=" + address.getHostAddress() + " Host Name=" + address.getHostName() + " Bytes=" + Str.bytesToHex(address.getAddress()));
                                        //Log.d(ip6TAG, "[NEW] IPV6 Info=" + address.toString() + " Host Address=" + address.getHostAddress() + " Host Name=" + address.getHostName() + " Bytes=" + Str.bytesToHex(address.getAddress()));
                                    //}catch (Exception ignored) { }
                                }

                                //FIELD_IF_ADDRS_IFA_ADDR.trySetValueInstanceEx(ifAddress, Inet6Address.getByName(fakeAddress));
                                FIELD_IF_ADDRS_IFA_ADDR.trySetValueInstanceEx(ifAddress, address);
                                if (DebugUtil.isDebug())
                                    Log.d(TAG, M_TAG + "Replaced IPV6 From Interface [" + name + "]\n" +
                                            "Is Local Link=" + (info.isLinkLocal) + "\n" +
                                            "Is Global=" + (info.isGlobal) + "\n" +
                                            "Is Permanent=" + (!info.isTemporary) + "\n" +
                                            "Old Address=" + address.getHostAddress() + "\n" +
                                            "New Address=" + fakeAddress);
                            }catch (Exception e) {
                                Log.e("XLua.IPV6", "Network Interface Error: " + e + " Stack=" + Log.getStackTraceString(e));
                            }
                        } else if (address instanceof Inet4Address) {
                            if ("wlan0".equalsIgnoreCase(name) || "dummy0".equalsIgnoreCase(name)) {
                                if (!"127.0.0.1".equals(address.getHostAddress())) {
                                    String newAddress = map.getValueOrSetting(
                                            name,
                                            address.getHostAddress(),
                                            param,
                                            "network.host.address");
                                    if (newAddress != null) {
                                        FIELD_IF_ADDRS_IFA_ADDR.trySetValueInstanceEx(ifAddress, Inet4Address.getByName(newAddress));
                                        String[] parts = newAddress.split("\\.");
                                        FIELD_IF_ADDRS_IFA_BROADADDR.trySetValueInstanceEx(ifAddress, Inet4Address.getByName(parts[0] + ".0.0.255"));
                                        if (DebugUtil.isDebug())
                                            Log.d(TAG, M_TAG + "Replaced [" + name + "] Interface IPV4 Address!\n" +
                                                    "Old Address=" + address.getHostAddress() + "\n" +
                                                    "New Address=" + newAddress);

                                        byte[] hardwareAddress = FIELD_IF_ADDRS_IFA_HWADDR.tryGetValueInstanceEx(ifAddress);
                                        String newHardwareAddress = map.getValueOrSetting(
                                                name,
                                                address.getHostAddress(),
                                                param,
                                                "unique.network.mac.address");
                                        if (hardwareAddress != null && newHardwareAddress != null) {
                                            if(!Arrays.equals(hardwareAddress, DEFAULT_MAC_ADDRESS)) {
                                                FIELD_IF_ADDRS_IFA_HWADDR.trySetValueInstanceEx(ifAddress, NetUtils.macAddressToByteArray(newHardwareAddress));
                                                if (DebugUtil.isDebug())
                                                    Log.d(TAG, M_TAG + "Replaced [" + name + "] IPV4 Hardware Address! New Address: " + newHardwareAddress);
                                            }
                                        }

                                        continue;
                                    }
                                }
                            }

                            if(DebugUtil.isDebug())
                                Log.d(TAG, M_TAG + "Skipping Interface [" + name + "] Address=" + address.getHostAddress());
                        }

                        //Spoof MAC
                        byte[] hardwareAddress = FIELD_IF_ADDRS_IFA_HWADDR.tryGetValueInstanceEx(ifAddress);
                        if (hardwareAddress != null) {
                            if(!Arrays.equals(hardwareAddress, DEFAULT_MAC_ADDRESS)) {
                                String newHardwareAddress = macRandomizer.generateString();
                                FIELD_IF_ADDRS_IFA_HWADDR.trySetValueInstanceEx(ifAddress, NetUtils.macAddressToByteArray(newHardwareAddress));
                                if (DebugUtil.isDebug())
                                    Log.d(TAG, M_TAG + "Replaced [" + name + "] ["  + address.getHostAddress() + "] Hardware Address with: " + newHardwareAddress);
                            }
                        }
                    }
                }
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, M_TAG + "Now Filtering the Network Interface List!");

            //network.interfaces.allowed.list
            String settingAllowInterfaces = param.getSetting("network.interfaces.allowed.list");
            boolean allowAll = settingAllowInterfaces == null || settingAllowInterfaces.equals("*");
            if(!allowAll) {
                Class<?> structClass = ReflectUtil.tryGetClassForName(STRUCT_CLASS_NAME);
                if(structClass != null) {
                    List<Object> lst = new ArrayList<>();
                    if(!settingAllowInterfaces.equals("!")) {
                        if(DebugUtil.isDebug())
                            Log.d(TAG, M_TAG + "Network Interfaces Filter => " + settingAllowInterfaces);

                        String[] filter = settingAllowInterfaces.split(Str.COMMA);
                        if(!settingAllowInterfaces.contains("wlan0")) {
                            filter = Arrays.copyOf(filter, filter.length + 1);
                            filter[filter.length - 1] = "wlan0";
                        }

                        if(DebugUtil.isDebug())
                            Log.d(TAG, M_TAG + "Network Interfaces Filter: " + Str.joinArray(filter, Str.COMMA) + " Size: " + filter.length);

                        for(int i = 0; i < length; i++) {
                            Object ifAddress = Array.get(res, i);
                            String name = FIELD_IF_ADDRS_IFA_NAME.tryGetValueInstanceEx(ifAddress);
                            if(name == null) continue;
                            //have a wlan0 check
                            for(String f : filter) {
                                String fCleaned = f.trim().toLowerCase();
                                String aCleaned = name.trim().toLowerCase();
                                if(fCleaned.endsWith(aCleaned) && !aCleaned.startsWith("!")) {
                                    //then allow
                                    lst.add(ifAddress);
                                    break;
                                }
                            }
                        }
                    }

                    Object newCopy = Array.newInstance(structClass, lst.size());
                    for(int i = 0; i < lst.size(); i++)
                        Array.set(newCopy, i, lst.get(i));

                    res = newCopy;
                }
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, M_TAG + "Filtered Network Interface List Size: " + Array.getLength(res));

            param.setResult(res);
            return true;
        }catch (Throwable e) {
            Log.e(TAG, "Error Intercepting Array for GetIfAddrs: " + e.getMessage());
            return false;
        }
    }

    public static boolean interceptIoctlInetAddress(XParam param) {
        try {
            //This is used for IPV4 Not IPV6
            GroupedMap map = param.getGroupedMap(NetUtils.GROUP_NAME);
            String M_TAG = "[IOCTL InetAddress] ";

            int code = (int)param.getArgument(1);
            String interfaceName = (String)param.getArgument(2);
            if("wlan0".equalsIgnoreCase(interfaceName) || "dummy0".equalsIgnoreCase(interfaceName)) {
                //wlan+
                Object res = param.getResult();
                if(res instanceof InetAddress) {
                    InetAddress address = (InetAddress) res;
                    if("127.0.0.1".equals(address.getHostAddress())) {
                        if(DebugUtil.isDebug())
                            Log.d(TAG, M_TAG + interfaceName + " Address Is 127.0.0.1 Address Skipping...");
                        return false;
                    }

                    if(address instanceof Inet4Address) {
                        String fakeAddress = map.getValueOrSetting(
                                interfaceName,
                                address.getHostAddress(),
                                param,
                                "network.host.address");
                        if(fakeAddress == null) return false;

                        if(code == SIOCGIFADDR) {
                            param.setResult(InetAddress.getByName(fakeAddress));
                            if(DebugUtil.isDebug())
                                Log.d(TAG, M_TAG + interfaceName + " IPv4 Address Spoofed: Old: " + address.getHostAddress() + " New: " + fakeAddress);

                            return true;
                        }

                        if(code == SIOCGIFBRDADDR) {
                            String[] ipParts = fakeAddress.split("\\.");
                            String broadcast = ipParts[0] + ".0.0.255";
                            param.setResult(InetAddress.getByName(broadcast));
                            if(DebugUtil.isDebug())
                                Log.d(TAG, M_TAG + interfaceName + " IPv4 Broadcast Address Spoofed!\n" +
                                        "Old=" + address.getHostAddress() + "\n" +
                                        "New=" + broadcast);

                            return true;
                        }

                        if(code == SIOCGIFNETMASK) {
                            if (DebugUtil.isDebug())
                                Log.d(TAG, M_TAG + interfaceName + " IPv4 Broadcast [SIOCGIFNETMASK] Skipping Net Mast: Address: " + address.getHostAddress());
                        }
                    }

                    if(DebugUtil.isDebug())
                        Log.d(TAG, M_TAG + "Skipping [" + interfaceName + "] Address: " + address.getHostAddress() + " Code: " + code);

                }
            }

            return false;
        }catch (Throwable e) {
            Log.e(TAG, "Error Intercepting IOCTL InetAddress: " + e);
            return false;
        }
    }

    public static boolean interceptFileOpenProcIfNet6(XParam param) {
        try {
            GroupedMap map = param.getGroupedMap(NetUtils.GROUP_NAME);
            String M_TAG = "[INET6 OPEN] ";
            //Assuming this is From Something like BlockGuardOs.open(String file, ....)
            Object res = param.getResult();
            if(!(res instanceof FileDescriptor))
                return false;

            FileDescriptor fd = (FileDescriptor) res;
            String file = (String)param.getArgument(0);
            if(file.startsWith(INET6_FILE)) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, M_TAG + " Intercepting File: " + INET6_FILE);

                String contents = FileReaderEx.readFileAsString(fd, true);
                if(contents == null || contents.isEmpty())
                    return false;

                //I am not sure if this will work ?
                //WWe ran into issues with swapping return value versus swapping the param ?
                //Something with Permissions, it also may be a issue with IoBridge so lets stay away ?
                Inet6File parsed = new Inet6File(contents);
                parsed.replaceAddresses(
                        param.getSetting("network.host.address.6.wlan0.local.link.perm"),
                        param.getSetting("network.host.address.6.wlan0.global.perm"),
                        param.getSetting("network.host.address.6.dummy0.local.link.perm"),
                        map);

                int paramFlags = (int)param.getArgument(1);
                int paramMode = (int)param.getArgument(2);

                String newContents = parsed.getCopyData();
                if(DebugUtil.isDebug())
                    Log.d(TAG, M_TAG + "Replaced Contents Old=" + contents + "  New=" + newContents);

                File fakeFile = FileUtil.generateTempFakeFile(newContents);
                if(fakeFile == null) return false;
                FileReaderEx.closeQuietly(fd);
                if(DebugUtil.isDebug())
                    Log.d(TAG, M_TAG + "Created Fake INET 6 File: " + fakeFile.getAbsolutePath() + " Opening File Descriptor...");

                FileDescriptor fakeDesc = Os.open(file, paramFlags, paramMode);
                if(DebugUtil.isDebug())
                    Log.d(TAG, M_TAG + "Opened Fake INET 6 File: " + fakeFile.getAbsolutePath());

                param.setResult(fakeDesc);
                return true;
            }
            return false;
        }catch (Throwable e) {
            Log.e(TAG, "Error Intercepting .open(/proc/net/if_inet6) Error: " + e);
            return false;
        }
    }

    //public static String generateFakeInet6Output()

    //https://github.com/xdtianyu/android-6.0.0_r1/blob/master/libcore/luni/src/main/java/java/net/NetworkInterface.java#L234
    // public InetAddress ioctlInetAddress(FileDescriptor fd, int cmd, String interfaceName) throws ErrnoException { return os.ioctlInetAddress(fd, cmd, interfaceName); }
    //

    public static boolean interceptFileList(XParam param) {
        try {
            //readIntFile("/sys/class/net/" + interfaceName + "/ifindex");
            //Index is in the ifindex file
            //network interface name is a directory
            Object ths = param.getThis();
            if(ths instanceof File) {
                File f = (File) ths;
                if(!f.getAbsolutePath().startsWith("/sys/class/net"))
                    return false;
            }

            String settingAllowInterfaces = param.getSetting("network.interfaces.allowed.list");
            boolean allowAll = settingAllowInterfaces == null || settingAllowInterfaces.equals("*");
            if(allowAll) return false;  //No need to intercept

            Object res = param.getResult();
            if(res == null) return false;
            if(settingAllowInterfaces.equals("!")) {
                if(res instanceof String[]) {
                    String[] empty = new String[0];
                    param.setResult(empty);
                    return true;
                }
                else if(res instanceof File[]) {
                    File[] empty = new File[0];
                    param.setResult(empty);
                    return true;
                }
             }

            String[] filter = settingAllowInterfaces.split(",");
            if(!settingAllowInterfaces.contains("wlan0")) {
                filter = Arrays.copyOf(filter, filter.length + 1);
                filter[filter.length - 1] = "wlan0";
            }

            if(res instanceof String[]) {
                String[] fileNames = (String[]) res;
                List<String> newFiles = new ArrayList<>();
                for(String fName : fileNames) {
                    for(String f : filter) {
                        String fNCleaned = fName.trim().toLowerCase();
                        String fTCleaned = f.trim().toLowerCase();
                        boolean block = fNCleaned.startsWith("!");
                        if(block) fNCleaned = fTCleaned.substring(1);
                        if(fNCleaned.endsWith(fTCleaned) && !block) {
                            newFiles.add(fName);
                            break;
                        }
                    }
                }

                param.setResult(newFiles.toArray(new String[0]));
                return true;
            }
            else if(res instanceof File[]) {
                File[] files = (File[]) res;
                List<File> newFiles = new ArrayList<>();
                for(File fObj : files) {
                    for(String f : filter) {
                        String fNCleaned = fObj.getName().trim().toLowerCase();
                        String fTCleaned = f.trim().toLowerCase();
                        boolean block = fNCleaned.startsWith("!");
                        if(block) fNCleaned = fTCleaned.substring(1);
                        if(fNCleaned.endsWith(fTCleaned) && !block) {
                            newFiles.add(fObj);
                            break;
                        }
                    }
                }

                param.setResult(newFiles.toArray(new File[0]));
                return true;
            }

            return false;
        }catch (Throwable e) {
            Log.e(TAG, "Error Intercepting File.list for Network Devices: " + e);
            return false;
        }
    }
}
