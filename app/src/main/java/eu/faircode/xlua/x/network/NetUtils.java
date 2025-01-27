package eu.faircode.xlua.x.network;

import android.text.TextUtils;
import android.util.Log;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

//fe80000000000000025056fffec00000 02 40 20 80 eth0
//File outputs from "/proc/net/if_inet6"
    /*
        fe800000000000000075a2ea32ff8d59 0a 40 20 80 rmnet_data0
        00000000000000000000000000000001 01 80 10 80       lo
        fe800000000000009c1f58fffe0ec503 03 40 20 80   dummy0
        260102491a8479f078940f0ee058a6ca 1e 40 00 01    wlan0
        260102491a8479f0f9a6f6cc5a9eccaa 1e 40 00 00    wlan0
        fe80000000000000f76ff3ec1708acd3 15 40 20 80 r_rmnet_data0
        fe80000000000000592fcbb060f1f4e3 1e 40 20 80    wlan0
     */

    /*
        fe80000000000000d511fd750b90fb1c 15 40 20 80 r_rmnet_data0
        00000000000000000000000000000001 01 80 10 80       lo
        fe800000000000007c9da4fffe837fe8 03 40 20 80   dummy0
        fe8000000000000039e14a41150f5bb3 0a 40 20 80 rmnet_data0
        260102491a8479f0e11daa40f9218b51 1e 40 00 01    wlan0
        fe800000000000001ce369fffe881083 1e 40 20 80    wlan0
        260102491a8479f01ce369fffe881083 1e 40 00 00    wlan0
     */

    /*
        fe80000000000000025056fffec00000 02 40 20 80 eth0
            This line can be interpreted as:

            IPv6 Address: fe80::250:56ff:fec0:0
            Device Number: 02
            Prefix Length: 64 (40 in hexadecimal)
            Scope: 20 (link-local)
            Flags: 80
            Device Name: eth0
     */

//Seems to Be
    /*
        260102491a8479f0e11daa40f9218b51 1e 40 00 01    wlan0 [Global][Temporary Address]
        fe800000000000001ce369fffe881083 1e 40 20 80    wlan0 [Link Local][Permanent Address]
        260102491a8479f01ce369fffe881083 1e 40 00 00    wlan0 [Global][Permanent Address]       [Most likely this one]

        return isGlobal && !isTemporary && !isDeprecated;

        The first entry:
        Scope: 00 (global)
        Flags: 01 (temporary address)

        The second entry:
        Scope: 20 (link-local)
        Flags: 80 (permanent address)

        The third entry:
        Scope: 00 (global)
        Flags: 00 (permanent address)
     */


public class NetUtils {
    private static final String TAG = "XLua.NetUtils";
    public static final String IF_INET_6_FILE = "/proc/net/if_inet6";
    public static final String GROUP_NAME = "OBC.Network.Settings";
    public static final String ASSUMED_WIFI_NET_INF_NAME = "wlan0";


    public static boolean isIpv6OrInet6(String address) { return (!TextUtils.isEmpty(address) && !address.contains(".")) && ((address.contains(":") || address.length() == 32)); }

    public static byte[] macAddressToByteArray(String macAddress) {
        // Remove any colons or hyphens from the MAC address
        String cleanMac = macAddress.replaceAll("[:-]", "");
        // Check if the cleaned MAC address has the correct length
        if (cleanMac.length() != 12) {
            throw new IllegalArgumentException("Invalid MAC address format");
        }

        // Convert the hex string to a byte array
        byte[] macBytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            int index = i * 2;
            String byteString = cleanMac.substring(index, index + 2);
            macBytes[i] = (byte) Integer.parseInt(byteString, 16);
        }

        return macBytes;
    }

    public static String ensureIfInet6Format(String inet6Address) {
        if(inet6Address == null) return null;
        if(inet6Address.contains(Str.COLLEN)) inet6Address = inet6Address.replaceAll(Str.COLLEN, Str.EMPTY);
        if(inet6Address.isEmpty()) return Str.EMPTY;
        int length = inet6Address.length();
        if(length > 32)
            return inet6Address.substring(0, 32);
        else if(length < 32) {
            StringBuilder sb = new StringBuilder();
            int needed = 32 - inet6Address.length();
            for(int i = 0; i < needed + 1; i++)
                sb.append("0");

            return sb.toString();
        }

        return inet6Address;
    }

    /**
     * Convert a IPv4 address from an integer to an InetAddress.
     * @param hostAddress an int corresponding to the IPv4 address in network byte order
     */
    public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = { (byte)(0xff & hostAddress),
                (byte)(0xff & (hostAddress >> 8)),
                (byte)(0xff & (hostAddress >> 16)),
                (byte)(0xff & (hostAddress >> 24)) };

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    public static String intToIpv4(int hostAddress) {
        return String.format("%d.%d.%d.%d",
                hostAddress & 0xFF,           // Last octet
                (hostAddress >> 8) & 0xFF,    // Third octet
                (hostAddress >> 16) & 0xFF,   // Second octet
                (hostAddress >> 24) & 0xFF    // First octet
        );
    }


    public static String intToIpv4Alt(int hostAddress) {
        StringBuilder sb = new StringBuilder(15); // max length of IPv4 string

        sb.append(hostAddress & 0xFF).append('.')
                .append((hostAddress >> 8) & 0xFF).append('.')
                .append((hostAddress >> 16) & 0xFF).append('.')
                .append((hostAddress >> 24) & 0xFF);

        return sb.toString();
    }

    /**
     * Convert a IPv4 address from an InetAddress to an integer
     * @param inetAddr is an InetAddress corresponding to the IPv4 address
     * @return the IP address as an integer in network byte order
     */
    public static int inetAddressToInt(Inet4Address inetAddr)
            throws IllegalArgumentException {
        byte [] addr = inetAddr.getAddress();
        return ((addr[3] & 0xff) << 24) | ((addr[2] & 0xff) << 16) |
                ((addr[1] & 0xff) << 8) | (addr[0] & 0xff);
    }

    public static int inetAddressToInt(byte[] addressBytes) {
        return ((addressBytes[3] & 0xff) << 24) | ((addressBytes[2] & 0xff) << 16) |
                ((addressBytes[1] & 0xff) << 8) | (addressBytes[0] & 0xff);
    }

    public static int ipv4ToInt(String ipAddress) {
        try {
            String[] octets = ipAddress.split("\\.");
            return (Integer.parseInt(octets[3]) << 24)
                    | (Integer.parseInt(octets[2]) << 16)
                    | (Integer.parseInt(octets[1]) << 8)
                    | (Integer.parseInt(octets[0]));
        }catch (Exception e) {
            Log.e(TAG, "Error Converting IPV4 Address to INT, Ip=" + ipAddress + " Error: " + e);
            return 0;
        }
    }

    public static int generateDHCPLeaseTime() {
        // Common DHCP lease times:
        // 24 hours = 86400 seconds
        // 168 hours (1 week) = 604800 seconds
        // 720 hours (30 days) = 2592000 seconds
        // 8760 hours (1 year) = 31536000 seconds

        int[] commonLeaseTimes = {
                86400,    // 24 hours
                172800,   // 48 hours
                604800,   // 1 week (168 hours)
                1209600,  // 2 weeks
                2592000,  // 30 days
                5184000   // 60 days
        };

        return commonLeaseTimes[RandomGenerator.nextInt(commonLeaseTimes.length)];
    }

    // Alternative version that generates based on time units
    public static int generateDHCPLeaseTimeAlt() {
        // Choose between different time units
        int choice = RandomGenerator.nextInt(4);

        switch (choice) {
            case 0: // 1-7 days
                return RandomGenerator.nextInt(1, 8) * 86400;
            case 1: // 1-4 weeks
                return RandomGenerator.nextInt(1, 5) * 604800;
            case 2: // 24, 48, or 72 hours
                return RandomGenerator.nextInt(1, 4) * 86400;
            default: // exactly 168 hours (1 week)
                return 604800;
        }
    }

    // Alternative version using byte array (similar to Android's approach)
    public static int ipv4ToIntAlt(String ipAddress) {
        try {
            byte[] bytes = InetAddress.getByName(ipAddress).getAddress();
            return ((bytes[3] & 0xFF) << 24)
                    | ((bytes[2] & 0xFF) << 16)
                    | ((bytes[1] & 0xFF) << 8)
                    | (bytes[0] & 0xFF);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IPv4 address", e);
        }
    }


    /**
     * Converts a byte array between little-endian and big-endian formats
     * @param source The source byte array
     * @param sourceIsBigEndian true if source is big-endian, false if little-endian
     * @return A new byte array with reversed endianness
     */
    public static byte[] convertEndianness(byte[] source, boolean sourceIsBigEndian) {
        if (source == null || source.length <= 1) {
            return source; // No conversion needed for null or single byte
        }

        byte[] result = new byte[source.length];

        // If source is big-endian, convert to little-endian and vice versa
        for (int i = 0; i < source.length; i++) {
            result[i] = source[source.length - 1 - i];
        }

        return result;
    }

    public static byte[] convertIPv4ToInetAddress(String ipv4String)  {
        String[] octets = ipv4String.split("\\.");
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++)
            bytes[i] = (byte) Integer.parseInt(octets[i]);

        return bytes;
    }

    public static String convertIpv6ToIfInet6Format(String inet6Address) {
        if(inet6Address == null) return null;
        return ensureIfInet6Format(inet6Address.replaceAll(Str.COLLEN, Str.EMPTY));
    }

    public static String convertIfInet6ToIpv6Format(String ifInet6Address) {
        ifInet6Address = ensureIfInet6Format(ifInet6Address);
        StringBuilder formattedAddress = new StringBuilder();
        for (int i = 0; i < ifInet6Address.length(); i += 4) formattedAddress.append(ifInet6Address.substring(i, i + 4)).append(":");
        return formattedAddress.substring(0, formattedAddress.length() - 1);
    }

    public static String joinInetAddresses(List<InetAddress> addresses) {
        if(addresses == null || addresses.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        int last = addresses.size();
        for(int i = 0; i < addresses.size(); i++) {
            InetAddress a = addresses.get(i);
            sb.append(a.getHostAddress());
            if(i != last) sb.append(",");
        }

        return sb.toString();
    }

    public static InetAddress parseIpv4ToInetAddress(String address) {
        if(TextUtils.isEmpty(address)) return null;
        try {
            return Inet4Address.getByName(address);
        }catch (Exception e) {
            Log.e(TAG, "Failed to Parse IPV4 Address: " + address + " Error: " + e);
            return null;
        }
    }

    public static InetAddress parseIpv6ToInetAddress(String address) {
        if(TextUtils.isEmpty(address)) return null;
        try {
            return address.contains(Str.COLLEN) ?
                    Inet6Address.getByName(normalizeIPv6Address(address)) :
                    Inet6Address.getByName(normalizeIPv6Address(ensureIfInet6Format(address)));
        }catch (Exception e) {
            try {
                return Inet6Address.getByName(normalizeIPv6Address(ensureIfInet6Format(address)));
            } catch (Exception oe) {
                Log.e(TAG, "Failed to Parse IPV6 Address: " + address + " Error: " + e);
                return null;
            }
        }
    }

    //Another Check to ensure the IPV6 is in Correct Format
    public static String normalizeIPv6Address(String ipv6Address) {
        // Remove leading and trailing whitespace
        ipv6Address = ipv6Address.trim();

        // Split the address into groups
        String[] groups = ipv6Address.split(":");

        // Check if we have a valid number of groups
        if (groups.length > 8) {
            throw new IllegalArgumentException("IPv6 address has too many groups");
        }

        StringBuilder normalizedAddress = new StringBuilder();
        int emptyGroupIndex = -1;

        for (int i = 0; i < groups.length; i++) {
            if (groups[i].isEmpty()) {
                // Found an empty group
                if (emptyGroupIndex != -1) {
                    // We've already found an empty group before
                    throw new IllegalArgumentException("IPv6 address can only have one '::' abbreviation");
                }
                emptyGroupIndex = i;
            } else {
                // Pad each group to 4 characters with leading zeros
                normalizedAddress.append(String.format("%4s", groups[i]).replace(' ', '0'));
                if (i < groups.length - 1) {
                    normalizedAddress.append(":");
                }
            }
        }

        // If we found an empty group, expand it
        if (emptyGroupIndex != -1) {
            int groupsToAdd = 8 - (groups.length - 1);
            StringBuilder expandedGroups = new StringBuilder(":");
            for (int i = 0; i < groupsToAdd; i++) {
                expandedGroups.append("0000:");
            }
            normalizedAddress.insert(emptyGroupIndex * 5, expandedGroups);
        }

        // If we still don't have 8 groups, pad with zeros
        while (normalizedAddress.toString().split(":").length < 8) {
            normalizedAddress.append(":0000");
        }

        // Remove trailing colon if present
        if (normalizedAddress.charAt(normalizedAddress.length() - 1) == ':') {
            normalizedAddress.setLength(normalizedAddress.length() - 1);
        }

        return normalizedAddress.toString();
    }
}
