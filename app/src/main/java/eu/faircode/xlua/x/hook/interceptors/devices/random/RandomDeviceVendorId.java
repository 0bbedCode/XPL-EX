package eu.faircode.xlua.x.hook.interceptors.devices.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomDeviceVendorId {
    // Common vendor IDs as integers
    public static final class VendorIds {
        public static final int MICROSOFT = 0x045e;  // 1118
        public static final int SONY = 0x054c;       // 1356
        public static final int NINTENDO = 0x057e;   // 1406
        public static final int LOGITECH = 0x046d;   // 1133
        public static final int RAZER = 0x1532;      // 5426
        public static final int POWER_A = 0x20d6;    // 8406
        public static final int DRAGONRISE = 0x0079; // 121
        public static final int PDP = 0x0583;        // 1411
        public static final int ASUS = 0x0b05;       // 2821
        public static final int STEELSERIES = 0x1038;// 4152
        public static final int VALVE = 0x28de;      // 10462
        public static final int GENERIC = 0x0000;    // 0
    }

    /**
     * Extract vendor ID from USB descriptor or device name
     */
    public static int getVendorIdFromDescriptorOrName(String descriptor, String name) {
        //if(originalVendorId < 1)
        //    return originalVendorId;

        // First try to get from USB descriptor
        if (descriptor != null && descriptor.startsWith("usb-")) {
            try {
                // Extract vendor ID from format "usb-xxxx:yyyy-zzzz/type"
                String vendorIdStr = descriptor.substring(4, 8);
                return Integer.parseInt(vendorIdStr, 16);
            } catch (Exception e) {
                // Fall through to name-based detection
            }
        }

        // If not USB or parsing failed, try to determine from name
        String lowercaseName = name.toLowerCase();

        if (lowercaseName.contains("xbox") || lowercaseName.contains("microsoft"))
            return VendorIds.MICROSOFT;
        if (lowercaseName.contains("playstation") || lowercaseName.contains("sony") ||
                lowercaseName.contains("dualshock") || lowercaseName.contains("ps4") ||
                lowercaseName.contains("ps5"))
            return VendorIds.SONY;
        if (lowercaseName.contains("nintendo") || lowercaseName.contains("switch"))
            return VendorIds.NINTENDO;
        if (lowercaseName.contains("logitech"))
            return VendorIds.LOGITECH;
        if (lowercaseName.contains("razer"))
            return VendorIds.RAZER;
        if (lowercaseName.contains("powera"))
            return VendorIds.POWER_A;
        if (lowercaseName.contains("pdp"))
            return VendorIds.PDP;
        if (lowercaseName.contains("steelseries"))
            return VendorIds.STEELSERIES;
        if (lowercaseName.contains("valve") || lowercaseName.contains("steam"))
            return VendorIds.VALVE;

        // For built-in devices
        if (isBuiltInDeviceName(lowercaseName))
            return VendorIds.GENERIC;

        // For unknown external devices, randomly select a vendor
        return getRandomVendorId();
    }

    private static boolean isBuiltInDeviceName(String name) {
        return name.contains("built-in") ||
                name.contains("internal") ||
                name.contains("system") ||
                name.contains("virtual") ||
                name.contains("touchscreen") ||
                name.contains("gpio") ||
                name.contains("sensor");
    }

    private static int getRandomVendorId() {
        int[] vendors = {
                VendorIds.MICROSOFT, VendorIds.SONY, VendorIds.NINTENDO,
                VendorIds.LOGITECH, VendorIds.RAZER, VendorIds.POWER_A,
                VendorIds.DRAGONRISE, VendorIds.PDP, VendorIds.ASUS,
                VendorIds.STEELSERIES, VendorIds.VALVE
        };
        return vendors[RandomGenerator.nextInt(vendors.length)];
    }
}
