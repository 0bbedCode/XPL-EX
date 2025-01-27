package eu.faircode.xlua.x.hook.interceptors.devices.random;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.hook.interceptors.devices.InputDeviceType;

public class RandomDeviceDescriptor {

    /**
     * Generates a random device descriptor based on device type
     */
    public static String generateDescriptor(InputDeviceType type) {
        switch (type) {
            case KEYBOARD_BUILTIN:
                return generateBuiltInDescriptor("kbd");
            case KEYBOARD_EXTERNAL:
                return RandomGenerator.nextBoolean() ?
                        generateUsbDescriptor("kbd") :
                        generateBluetoothDescriptor("kbd");
            case TOUCHSCREEN:
                return generateBuiltInDescriptor("ts");
            case GAMEPAD:
                return RandomGenerator.nextBoolean() ?
                        generateBluetoothDescriptor("gc") :
                        generateUsbDescriptor("gc");
            case MOUSE:
                return RandomGenerator.nextBoolean() ?
                        generateBluetoothDescriptor("mouse") :
                        generateUsbDescriptor("mouse");
            case TOUCHPAD:
                return generateBuiltInDescriptor("touchpad");
            case JOYSTICK:
                return generateUsbDescriptor("joy");
            case AUDIO_JACK:
                return generateBuiltInDescriptor("audio");
            case BUTTONS:
                return generateBuiltInDescriptor("btn");
            case STYLUS:
                return generateBluetoothDescriptor("stylus");
            case SENSOR:
                return generateBuiltInDescriptor("sensor");
            case UNKNOWN:
            default:
                return generateGenericDescriptor();
        }
    }

    /**
     * Generates a USB-style descriptor
     * Format: usb-xxxx:xxxx-xxxx/instance
     */
    private static String generateUsbDescriptor(String type) {
        // Generate vendor ID (real companies use registered VIDs)
        String[] commonVendorIds = {
                "045e",  // Microsoft
                "054c",  // Sony
                "057e",  // Nintendo
                "046d",  // Logitech
                "1532",  // Razer
                "20d6",  // PowerA
                "0079",  // DragonRise
                "0583",  // PDP
                "0b05",  // ASUS
                "1038",  // SteelSeries
                "28de"   // Valve
        };
        String vendorId = commonVendorIds[RandomGenerator.nextInt(commonVendorIds.length)];

        // Generate product ID and instance ID
        String productId = String.format("%04x", RandomGenerator.nextInt(0xFFFF));
        String instanceId = String.format("%04x", RandomGenerator.nextInt(0xFFFF));

        return String.format("usb-%s:%s-%s/%s", vendorId, productId, instanceId, type);
    }

    /**
     * Generates a Bluetooth-style descriptor
     * Format: bluetooth-xx:xx:xx:xx:xx:xx/instance
     */
    private static String generateBluetoothDescriptor(String type) {
        // Generate Bluetooth MAC address
        StringBuilder mac = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (i > 0) mac.append(":");
            mac.append(String.format("%02x", RandomGenerator.nextInt(256)));
        }

        String instanceId = String.format("%04x", RandomGenerator.nextInt(0xFFFF));
        return String.format("bluetooth-%s/%s-%s", mac.toString(), type, instanceId);
    }

    /**
     * Generates a built-in device descriptor
     * Format: platform-type-instance
     */
    private static String generateBuiltInDescriptor(String type) {
        String[] platforms = {
                "gpio", "i2c", "spi", "platform", "serio",
                "pci", "virtio", "misc", "soc"
        };

        String platform = platforms[RandomGenerator.nextInt(platforms.length)];
        String instanceId = String.format("%04x", RandomGenerator.nextInt(0xFFFF));

        return String.format("%s-%s-%s", platform, type, instanceId);
    }

    /**
     * Generates a generic descriptor for unknown devices
     */
    private static String generateGenericDescriptor() {
        String[] prefixes = {"input", "event", "dev", "generic"};
        String prefix = prefixes[RandomGenerator.nextInt(prefixes.length)];
        String instanceId = String.format("%08x", RandomGenerator.nextInt());
        return String.format("%s-%s", prefix, instanceId);
    }


    /**
     * Generates a hash for device identification
     * @param input Base string to generate hash from
     * @return 40 character hex hash string
     */
    public static String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            //return generateFallbackHash();
            return "fuck";
        }
    }

    /**
     * Fallback method to generate a 40-character hex string without using hash
     */
    private static String generateFallbackDescriptor() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            sb.append(Integer.toHexString(RandomGenerator.nextInt(16)));
        }
        return sb.toString();
    }
}
