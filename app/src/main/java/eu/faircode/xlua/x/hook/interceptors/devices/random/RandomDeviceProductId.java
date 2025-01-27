package eu.faircode.xlua.x.hook.interceptors.devices.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.hook.interceptors.devices.InputDeviceType;

public class RandomDeviceProductId {
    // Product ID ranges for different vendors and device types
    public static final class ProductIds {
        // Microsoft (Xbox) Controllers
        public static final int XBOX_ONE = 0x02ea;    // Xbox One Controller
        public static final int XBOX_360 = 0x028e;    // Xbox 360 Controller
        public static final int XBOX_ELITE = 0x0b00;  // Xbox Elite Controller
        public static final int XBOX_ADAPTIVE = 0x0b0a; // Xbox Adaptive Controller

        // Sony PlayStation Controllers
        public static final int PS4_V1 = 0x05c4;     // DualShock 4 v1
        public static final int PS4_V2 = 0x09cc;     // DualShock 4 v2
        public static final int PS5 = 0x0ce6;        // DualSense Controller

        // Nintendo Controllers
        public static final int SWITCH_PRO = 0x2009;  // Switch Pro Controller
        public static final int JOYCON_L = 0x2006;    // Joy-Con Left
        public static final int JOYCON_R = 0x2007;    // Joy-Con Right

        // Logitech Products
        public static final int G29_WHEEL = 0xc24f;   // G29 Racing Wheel
        public static final int F310 = 0xc216;        // F310 Gamepad
        public static final int F710 = 0xc21f;        // F710 Gamepad
        public static final int K780 = 0x405e;        // K780 Keyboard
        public static final int MX_MASTER = 0x4082;   // MX Master Mouse

        // Razer Products
        public static final int RAIJU = 0x1000;       // Razer Raiju Controller
        public static final int WOLVERINE = 0x0a00;   // Wolverine Controller
        public static final int BLACKWIDOW = 0x0339;  // BlackWidow Keyboard
        public static final int DEATHADDER = 0x0084;  // DeathAdder Mouse

        // Generic/Default
        public static final int GENERIC = 0x0000;
    }

    /**
     * Get product ID based on vendor ID and device type
     */
    public static int getProductId(int vendorId, InputDeviceType type, String deviceName) {
        String name = deviceName.toLowerCase();

        switch (vendorId) {
            case RandomDeviceVendorId.VendorIds.MICROSOFT:
                return getMicrosoftProductId(name);

            case RandomDeviceVendorId.VendorIds.SONY:
                return getSonyProductId(name);

            case RandomDeviceVendorId.VendorIds.NINTENDO:
                return getNintendoProductId(name);

            case RandomDeviceVendorId.VendorIds.LOGITECH:
                return getLogitechProductId(name, type);

            case RandomDeviceVendorId.VendorIds.RAZER:
                return getRazerProductId(name, type);

            case RandomDeviceVendorId.VendorIds.GENERIC:
                return ProductIds.GENERIC;

            default:
                return getRandomProductIdForType(type);
        }
    }

    private static int getMicrosoftProductId(String name) {
        if (name.contains("elite")) return ProductIds.XBOX_ELITE;
        if (name.contains("360")) return ProductIds.XBOX_360;
        if (name.contains("adaptive")) return ProductIds.XBOX_ADAPTIVE;
        return ProductIds.XBOX_ONE; // default to Xbox One controller
    }

    private static int getSonyProductId(String name) {
        if (name.contains("ps5") || name.contains("dualsense")) return ProductIds.PS5;
        if (name.contains("v2")) return ProductIds.PS4_V2;
        return ProductIds.PS4_V1;
    }

    private static int getNintendoProductId(String name) {
        if (name.contains("joy-con l")) return ProductIds.JOYCON_L;
        if (name.contains("joy-con r")) return ProductIds.JOYCON_R;
        return ProductIds.SWITCH_PRO;
    }

    private static int getLogitechProductId(String name, InputDeviceType type) {
        if (name.contains("g29")) return ProductIds.G29_WHEEL;
        if (name.contains("f710")) return ProductIds.F710;
        if (name.contains("f310")) return ProductIds.F310;
        if (name.contains("k780")) return ProductIds.K780;
        if (name.contains("mx master")) return ProductIds.MX_MASTER;

        // Return appropriate product ID based on device type
        switch (type) {
            case GAMEPAD:
                return ProductIds.F310;
            case KEYBOARD_EXTERNAL:
                return ProductIds.K780;
            case MOUSE:
                return ProductIds.MX_MASTER;
            default:
                return getRandomProductIdForType(type);
        }
    }

    private static int getRazerProductId(String name, InputDeviceType type) {
        if (name.contains("raiju")) return ProductIds.RAIJU;
        if (name.contains("wolverine")) return ProductIds.WOLVERINE;
        if (name.contains("blackwidow")) return ProductIds.BLACKWIDOW;
        if (name.contains("deathadder")) return ProductIds.DEATHADDER;

        // Return appropriate product ID based on device type
        switch (type) {
            case GAMEPAD:
                return ProductIds.RAIJU;
            case KEYBOARD_EXTERNAL:
                return ProductIds.BLACKWIDOW;
            case MOUSE:
                return ProductIds.DEATHADDER;
            default:
                return getRandomProductIdForType(type);
        }
    }

    private static int getRandomProductIdForType(InputDeviceType type) {
        switch (type) {
            case GAMEPAD:
                int[] gamepadIds = {
                        ProductIds.XBOX_ONE, ProductIds.PS4_V2,
                        ProductIds.SWITCH_PRO, ProductIds.F310,
                        ProductIds.RAIJU
                };
                return gamepadIds[RandomGenerator.nextInt(gamepadIds.length)];

            case KEYBOARD_EXTERNAL:
                int[] keyboardIds = {
                        ProductIds.K780, ProductIds.BLACKWIDOW
                };
                return keyboardIds[RandomGenerator.nextInt(keyboardIds.length)];

            case MOUSE:
                int[] mouseIds = {
                        ProductIds.MX_MASTER, ProductIds.DEATHADDER
                };
                return mouseIds[RandomGenerator.nextInt(mouseIds.length)];

            default:
                // For built-in or unknown devices
                return ProductIds.GENERIC;
        }
    }

    /**
     * Extract product ID from USB descriptor if available
     */
    public static int getProductIdFromDescriptor(String descriptor) {
        if (descriptor != null && descriptor.startsWith("usb-")) {
            try {
                // Extract product ID from format "usb-xxxx:yyyy-zzzz/type"
                String productIdStr = descriptor.substring(9, 13);
                return Integer.parseInt(productIdStr, 16);
            } catch (Exception e) {
                return ProductIds.GENERIC;
            }
        }
        return ProductIds.GENERIC;
    }
}
