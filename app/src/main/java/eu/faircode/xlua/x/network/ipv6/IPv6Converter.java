package eu.faircode.xlua.x.network.ipv6;

import java.lang.reflect.Array;

public class IPv6Converter {
    private static final int BYTES_LENGTH = 16;
    private static final int SEGMENTS = 8;
    private static final int BITS_PER_SEGMENT = 16;
    private static final char SEGMENT_SEPARATOR = ':';

    /**
     * Converts an IPv6 address string to a byte array.
     * Handles standard, compressed, and mixed IPv4-IPv6 formats.
     *
     * @param address IPv6 address string
     * @return byte array representing the address
     * @throws IllegalArgumentException if the address is invalid
     */
    public static byte[] toBytes(String address) {
        if (address == null || address.isEmpty()) {
            throw new IllegalArgumentException("Invalid IPv6 address");
        }

        // Remove IPv6 scope id if present
        int scopeIndex = address.indexOf('%');
        if (scopeIndex != -1) {
            address = address.substring(0, scopeIndex);
        }

        String[] parts;
        if (address.contains("::")) {
            // Handle compressed format
            parts = expandCompressedFormat(address);
        } else {
            parts = address.split(":");
            if (parts.length != SEGMENTS) {
                throw new IllegalArgumentException("Invalid IPv6 address segment count");
            }
        }

        byte[] bytes = new byte[BYTES_LENGTH];
        for (int i = 0, byteIndex = 0; i < parts.length; i++) {
            String part = parts[i];
            // Handle embedded IPv4 in last segment
            if (i == parts.length - 1 && part.contains(".")) {
                byte[] v4Bytes = parseIPv4(part);
                System.arraycopy(v4Bytes, 0, bytes, byteIndex, v4Bytes.length);
                break;
            }

            // Parse hexadecimal segment
            int value = Integer.parseInt(part, 16);
            bytes[byteIndex++] = (byte) (value >> 8);
            bytes[byteIndex++] = (byte) value;
        }

        return bytes;
    }

    private static String[] expandCompressedFormat(String address) {
        String[] segments = new String[SEGMENTS];
        String[] parts = address.split("::", -1);

        if (parts.length > 2) {
            throw new IllegalArgumentException("Invalid compressed IPv6 format");
        }

        // Fill in the known parts
        String[] beforeCompression = parts[0].isEmpty() ? new String[0] : parts[0].split(":");
        String[] afterCompression = parts.length > 1 && !parts[1].isEmpty() ?
                parts[1].split(":") : new String[0];

        // Calculate zeros needed
        int totalSegments = beforeCompression.length + afterCompression.length;
        int zerosNeeded = SEGMENTS - totalSegments;
        if (zerosNeeded < 0) {
            throw new IllegalArgumentException("Invalid IPv6 address segment count");
        }

        // Fill the segments array
        int pos = 0;

        // Copy first part
        for (int i = 0; i < beforeCompression.length; i++) {
            segments[pos++] = beforeCompression[i];
        }

        // Fill zeros
        for (int i = 0; i < zerosNeeded; i++) {
            segments[pos++] = "0";
        }

        // Copy second part
        for (int i = 0; i < afterCompression.length; i++) {
            segments[pos++] = afterCompression[i];
        }

        return segments;
    }

    private static byte[] parseIPv4(String ipv4Str) {
        String[] parts = ipv4Str.split("\\.");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid IPv4 format in IPv6 address");
        }

        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            int value = Integer.parseInt(parts[i]);
            if (value < 0 || value > 255) {
                throw new IllegalArgumentException("Invalid IPv4 value in IPv6 address");
            }
            bytes[i] = (byte) value;
        }
        return bytes;
    }

    /**
     * Creates an Inet6AddressHolder with the given IPv6 address
     * @param ipv6Address IPv6 address string
     * @return Inet6AddressHolder instance
     */
    /*public static Inet6AddressHolder createInet6AddressHolder(String ipv6Address) {
        byte[] bytes = convertIPv6ToBytes(ipv6Address);

        // Parse scope ID if present
        int scopeId = 0;
        boolean scopeIdSet = false;
        int scopeIndex = ipv6Address.indexOf('%');
        if (scopeIndex != -1) {
            String scopeStr = ipv6Address.substring(scopeIndex + 1);
            try {
                scopeId = Integer.parseInt(scopeStr);
                scopeIdSet = true;
            } catch (NumberFormatException e) {
                // Scope is not a number, might be interface name
                // In this case, leave scope_id as 0 and scope_id_set as false
            }
        }

        return new Inet6AddressHolder(bytes, scopeId, scopeIdSet, null, false);
    }*/

}
