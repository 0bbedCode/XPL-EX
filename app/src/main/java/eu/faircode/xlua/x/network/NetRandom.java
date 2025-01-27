package eu.faircode.xlua.x.network;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class NetRandom {
    private static final Set<String> NON_UNIQUE_ADDRESSES = new HashSet<>(Arrays.asList(
            "::1",
            "::",
            "::ffff:0:0",
            "64:ff9b::",
            "100::",
            "2001::",
            "2001:db8:",
            "0"
    ));

    private static final Set<String> PASS_SEGS = new HashSet<>(Arrays.asList(
            "0000",
            "000",
            "00",
            "0",
            "1",
            "11",
            "111",
            "1111",
            "0001",
            "0011",
            "0111",
            "ffff"));

    private static final Set<String> PASS_SEGS_TWO = new HashSet<>(Arrays.asList(
            "fe00",
            "fe80",
            "fd00",
            "fc00"));

    public static int collenCount(String address) {
        int count = 0;
        char[] chars = address.toCharArray();
        for(int i = 0; i < chars.length; i++) {
            if(chars[i] == ':')
                count++;
        }

        return count;
    }

    public static String randomizeIPv6(String originalAddress) {
        // Check if it's a non-unique address
        if(collenCount(originalAddress) < 4)
            return originalAddress;

        for(String t : NON_UNIQUE_ADDRESSES) {
            if(originalAddress.startsWith(t))
                return originalAddress;
        }


        // Split the address and interface identifier
        String[] parts = originalAddress.split("%");
        String address = parts[0];
        String interfaceId = parts.length > 1 ? "%" + parts[1] : "";

        String[] segments = address.split(":");
        StringBuilder result = new StringBuilder();
        boolean hasDoubleColon = address.contains("::");

        for (int i = 0; i < segments.length; i++) {
            if (segments[i].isEmpty() && hasDoubleColon) {
                result.append("::");
                hasDoubleColon = false; // Ensure we only use :: once
            } else if (!segments[i].isEmpty()) {
                // Preserve the first segment if it's a special prefix
                if (i == 0 && (PASS_SEGS_TWO.contains(segments[i]))) {
                    result.append(segments[i]);
                } else {
                    String seg = segments[i];
                    if(PASS_SEGS.contains(seg)) {
                        result.append(seg);
                    } else {
                        result.append(generateRandomHexSegment());
                    }
                }
                if (i < segments.length - 1 && !segments[i + 1].isEmpty()) {
                    result.append(":");
                }
            }
        }

        // Handle cases where :: is at the end
        if (address.endsWith("::")) {
            result.append("::");
        }

        return result.toString() + interfaceId;
    }

    private static String generateRandomHexSegment() {
        return String.format("%04x", RandomGenerator.nextInt(65536));
    }

    //Maybe make this instance, we can bind GroupedMaps ??

    public static String generateRandomPrivateIPv4() {
        int choice = RandomGenerator.nextInt(3);
        switch (choice) {
            case 0:
                // 10.0.0.0 to 10.255.255.255
                return "10." +
                        RandomGenerator.nextInt(256) + "." +
                        RandomGenerator.nextInt(256) + "." +
                        RandomGenerator.nextInt(1, 255);
            case 1:
                // 172.16.0.0 to 172.31.255.255
                return "172." +
                        RandomGenerator.nextInt(16, 32) + "." +
                        RandomGenerator.nextInt(256) + "." +
                        RandomGenerator.nextInt(1, 255);
            case 2:
                // 192.168.0.0 to 192.168.255.255
                return "192.168." +
                        RandomGenerator.nextInt(256) + "." +
                        RandomGenerator.nextInt(1, 255);
            default:
                throw new IllegalStateException("Unexpected value: " + choice);
        }
    }
}
