package eu.faircode.xlua.x.hook.interceptors.devices;

import android.os.Build;
import android.util.Log;
import android.view.InputDevice;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.runtime.reflect.DynamicField;

public class InputDeviceUtils {
    private static final String TAG = "XLua.InputDeviceUtils";

    public static final DynamicField FIELD_IS_EXTERNAL = new DynamicField(InputDevice.class, "mIsExternal")
            .setAccessible(true);

    /**
     * Determines if an input device is built-in for SDK 23+
     * @param device The InputDevice to check
     * @return true if the device is likely built-in, false otherwise
     */
    /*public static boolean isBuiltIn(InputDevice device) {
        if (device == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return !device.isExternal();
        }

        if(FIELD_IS_EXTERNAL.isValid()) {
            Object v = FIELD_IS_EXTERNAL.tryGetValueInstanceEx(device);
            if(v instanceof Boolean) {
                return (boolean) v;
            }
        }

        // Get the device sources
        int sources = device.getSources();

        // Check for built-in touchscreen/touchpad
        boolean isInternalTouch =
                (sources & InputDevice.SOURCE_TOUCHSCREEN) == InputDevice.SOURCE_TOUCHSCREEN ||
                        (sources & InputDevice.SOURCE_TOUCHPAD) == InputDevice.SOURCE_TOUCHPAD;

        // Check for built-in keyboard by examining sources and IDs
        // Built-in keyboards typically have SOURCE_KEYBOARD and zero vendor/product IDs
        boolean isInternalKeyboard =
                (sources & InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD &&
                        device.getVendorId() == 0 && device.getProductId() == 0;

        // Additional check - built-in devices typically have vendor/product ID of 0
        boolean hasZeroIds = device.getVendorId() == 0 && device.getProductId() == 0;

        // If it has a controller number, it's definitely external
        boolean hasControllerNumber = device.getControllerNumber() >= 0;

        // Combine all checks
        return (isInternalTouch || isInternalKeyboard || hasZeroIds) && !hasControllerNumber;
    }*/

    public static boolean isBuildIn(InputDevice originalDevice) { return isBuiltInDevice(null, originalDevice); }
    public static boolean isBuiltInDevice(
            MockDevice mockDevice,
            InputDevice originalDevice) {
        if(originalDevice == null) {
            Log.e(TAG, "Passed in NULL Device for Is Built In Check...");
            return false;
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Checking IF Device is Built in=" + originalDevice.toString() + (mockDevice == null ? "   null" : "   Mock=" + mockDevice.toString()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            boolean isExt = originalDevice.isExternal();
            if(DebugUtil.isDebug())
                Log.d(TAG, "Is Built In Device Check, is Android Q = Is External ? " + (isExt));

            return !isExt;
            //return !originalDevice.isExternal();
        }

        if(FIELD_IS_EXTERNAL.isValid()) {
            Object v = FIELD_IS_EXTERNAL.tryGetValueInstanceEx(originalDevice);
            if(v instanceof Boolean) {
                boolean isExt = (boolean) v;
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Is Built In Device Check 2, has Field, Is External ? " + (isExt));
                return !isExt;
            }
        }

        String name = mockDevice == null ? originalDevice.getName() : mockDevice.name;
        String descriptor = mockDevice == null ? originalDevice.getDescriptor() : mockDevice.descriptor;
        int vendorId = mockDevice == null ? originalDevice.getVendorId() : mockDevice.vendorId;
        int productId = mockDevice == null ? originalDevice.getProductId() : mockDevice.productId;
        return isBuiltInDevice(name, descriptor, originalDevice.getSources(), vendorId, productId, originalDevice.getControllerNumber());
    }

    public static boolean isBuiltInDevice(
            String name,
            String descriptor,
            int sources,
            int vendorId,
            int productId,
            int controllerNumber) {

        if(DebugUtil.isDebug())
            Log.d(TAG, "Checking Is Built In Device Flag Two with Params Name=" + name + " Descriptor=" + descriptor + " Sources=" + sources + " Vendor ID=" + vendorId + " Product ID=" + productId + " Controller Number=" + controllerNumber);

        //if (device == null) return false;

        // Get device characteristics
        //String name = device.getName() != null ? device.getName().toLowerCase() : "";
        //int sources = device.getSources();
        //int vendorId = device.getVendorId();
        //int productId = device.getProductId();

        // 1. Check vendor and product IDs
        // Built-in devices typically have both IDs as 0
        boolean hasZeroIds = vendorId == 0 && productId == 0;

        // 2. Check controller number
        // External game controllers have controller numbers, built-in devices don't
        //boolean hasControllerNumber = device.getControllerNumber() >= 0;
        boolean hasControllerNumber = controllerNumber >= 0;

        // 3. Name-based detection for built-in devices
        boolean hasBuiltInName = name.contains("built-in") ||
                name.contains("internal") ||
                name.contains("virtual") ||
                name.contains("system") ||
                name.contains("platform");

        // 4. Common built-in device names
        boolean isCommonBuiltIn = name.contains("touchscreen") ||
                name.contains("touch screen") ||
                name.contains("touch input") ||
                name.contains("ts_input") ||
                name.contains("gpio") ||
                name.contains("power button") ||
                name.contains("volume") ||
                name.contains("buttons") ||
                name.contains("keys") ||
                name.contains("sensor");

        // 5. Check device sources for typical built-in types
        boolean isBuiltInSource = false;

        // Touchscreen is almost always built-in
        if ((sources & InputDevice.SOURCE_TOUCHSCREEN) != 0) {
            isBuiltInSource = true;
        }

        // Check if it's a built-in keyboard
        // External keyboards typically have vendor/product IDs
        if ((sources & InputDevice.SOURCE_KEYBOARD) != 0) {
            isBuiltInSource = hasZeroIds;
        }

        // 6. Check for common external device indicators
        boolean hasExternalIndicators = name.contains("bluetooth") ||
                name.contains("usb") ||
                name.contains("wireless") ||
                name.contains("dongle") ||
                name.contains("receiver") ||
                name.contains("xbox") ||
                name.contains("playstation") ||
                name.contains("ps4") ||
                name.contains("ps5") ||
                name.contains("nintendo") ||
                name.contains("logitech") ||
                name.contains("razer");

        // 7. Special case: check descriptor format
        //String descriptor = device.getDescriptor();
        boolean hasExternalDescriptor = descriptor != null && (
                descriptor.startsWith("usb-") ||
                        descriptor.startsWith("bluetooth-")
        );

        if(DebugUtil.isDebug())
            Log.d(TAG, "Is Built In Device Flag Two with Params Name=" + name + " Descriptor=" + descriptor + " Sources=" + sources + " Vendor ID=" + vendorId + " Product ID=" + productId + " Controller Number=" + controllerNumber + " With Results of "
            + "\nHas Zero Ids=" + (hasZeroIds) + "\n" +
                    "Has BuiltInName=" + (hasBuiltInName) + "\n" +
                    "Is Common Built In=" + (isCommonBuiltIn) + "\n" +
                    "External Indicators=" + (hasExternalIndicators) + "\n" +
                    "Has Controller Number=" + (hasControllerNumber) + "\n" +
                    "Is Built In Source=" + (isBuiltInSource) + "\n" +
                    "Is Common Built In=" + (isCommonBuiltIn) + "\n" +
                    "Has External Descriptor=" + (hasExternalDescriptor));


        // Combine all checks for final determination
        // Device is considered built-in if:
        // - It has zero IDs OR has built-in name indicators
        // - AND doesn't have controller number
        // - AND doesn't have external indicators
        // - AND either has built-in source or common built-in name
        // - AND doesn't have external descriptor
        return (hasZeroIds || hasBuiltInName || isCommonBuiltIn) &&
                !hasControllerNumber &&
                !hasExternalIndicators &&
                (isBuiltInSource || isCommonBuiltIn) &&
                !hasExternalDescriptor;
    }

    /**
     * Alternative method focusing on identifying external devices
     * Sometimes easier to identify what is definitely external
     */
    public static boolean isExternalDevice(InputDevice device) {
        if (device == null) return false;

        String name = device.getName() != null ? device.getName().toLowerCase() : "";
        int vendorId = device.getVendorId();
        int productId = device.getProductId();

        // Definite external device indicators
        return device.getControllerNumber() >= 0 || // Has controller number
                (vendorId != 0 && productId != 0) || // Has non-zero IDs
                name.contains("bluetooth") ||
                name.contains("wireless") ||
                name.contains("usb") ||
                name.contains("xbox") ||
                name.contains("playstation") ||
                name.contains("nintendo") ||
                name.contains("logitech") ||
                name.contains("razer") ||
                (device.getDescriptor() != null && (
                        device.getDescriptor().startsWith("usb-") ||
                                device.getDescriptor().startsWith("bluetooth-")
                ));
    }

    /**
     * Gets a human-readable device type description
     * @param device The InputDevice to check
     * @return A string describing the device type
     */
    public static String getDeviceTypeDescription(InputDevice device) {
        if (device == null) return "null device";

        StringBuilder type = new StringBuilder();
        int sources = device.getSources();
        if ((sources & InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD) {
            type.append(isBuildIn(device) ? "Built-in Keyboard" : "External Keyboard");
        }
        if ((sources & InputDevice.SOURCE_TOUCHSCREEN) == InputDevice.SOURCE_TOUCHSCREEN) {
            if (type.length() > 0) type.append(", ");
            type.append("Touchscreen");
        }
        if ((sources & InputDevice.SOURCE_TOUCHPAD) == InputDevice.SOURCE_TOUCHPAD) {
            if (type.length() > 0) type.append(", ");
            type.append("Touchpad");
        }
        if ((sources & InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE) {
            if (type.length() > 0) type.append(", ");
            type.append("Mouse");
        }
        if ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
            if (type.length() > 0) type.append(", ");
            type.append("Gamepad");
        }
        if ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) {
            if (type.length() > 0) type.append(", ");
            type.append("Joystick");
        }
        if ((sources & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD) {
            if (type.length() > 0) type.append(", ");
            type.append("Dpad");
        }

        return type.length() > 0 ? type.toString() : "Unknown Device Type";
    }
}
