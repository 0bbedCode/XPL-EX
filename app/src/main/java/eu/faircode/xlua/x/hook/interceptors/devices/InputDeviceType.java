package eu.faircode.xlua.x.hook.interceptors.devices;

import android.view.InputDevice;

import eu.faircode.xlua.x.hook.interceptors.devices.random.RandomDeviceDescriptor;
import eu.faircode.xlua.x.hook.interceptors.devices.random.RandomDeviceName;

public enum InputDeviceType {
    KEYBOARD_BUILTIN("Built-in Keyboard", InputDevice.SOURCE_KEYBOARD),
    KEYBOARD_EXTERNAL("External Keyboard", InputDevice.SOURCE_KEYBOARD),
    TOUCHSCREEN("Touchscreen", InputDevice.SOURCE_TOUCHSCREEN),
    GAMEPAD("Gamepad", InputDevice.SOURCE_GAMEPAD),
    MOUSE("Mouse", InputDevice.SOURCE_MOUSE),
    TOUCHPAD("Touchpad", InputDevice.SOURCE_TOUCHPAD),
    JOYSTICK("Joystick", InputDevice.SOURCE_JOYSTICK),
    AUDIO_JACK("Audio Jack", 0),  // Special case - detected by name
    BUTTONS("Button Device", InputDevice.SOURCE_DPAD),
    STYLUS("Stylus", 0),          // Special case - detected by name
    SENSOR("Sensor", 0),          // Special case - detected by name
    UNKNOWN("Unknown Device", 0);

    private final String displayName;
    private final int source;

    InputDeviceType(String displayName, int source) {
        this.displayName = displayName;
        this.source = source;
    }

    public String getDisplayName() { return displayName; }

    public int getSource() { return source; }

    /**
     * Determines the type of an input device based on its characteristics
     *
     * @param device The input device to check
     * @return The determined InputDeviceType
     */
    public static InputDeviceType fromDevice(InputDevice device) {
        if (device == null) return UNKNOWN;

        String name = device.getName() != null ? device.getName().toLowerCase() : "";
        int sources = device.getSources();

        // Check audio jack first (based on name patterns)
        if (name.contains("headset") || name.contains("audio") || name.contains("sound") || name.contains("snd-card")) {
            return AUDIO_JACK;
        }

        // Check stylus/pen
        if (name.contains("pen") || name.contains("stylus") || name.contains("s-pen")) {
            return STYLUS;
        }

        // Check sensor devices
        if (name.contains("sensor") || name.contains("accelerometer") || name.contains("gyroscope") || name.contains("orientation")) {
            return SENSOR;
        }

        // Check source-based devices
        if ((sources & InputDevice.SOURCE_KEYBOARD) != 0) {
            // Differentiate between built-in and external keyboards
            boolean isBuiltIn = device.getVendorId() == 0 && device.getProductId() == 0;
            return isBuiltIn ? KEYBOARD_BUILTIN : KEYBOARD_EXTERNAL;
        }

        if ((sources & InputDevice.SOURCE_GAMEPAD) != 0) return GAMEPAD;
        if ((sources & InputDevice.SOURCE_TOUCHSCREEN) != 0) return TOUCHSCREEN;
        if ((sources & InputDevice.SOURCE_MOUSE) != 0) return MOUSE;
        if ((sources & InputDevice.SOURCE_TOUCHPAD) != 0) return TOUCHPAD;
        if ((sources & InputDevice.SOURCE_JOYSTICK) != 0) return JOYSTICK;
        if ((sources & InputDevice.SOURCE_DPAD) != 0) return BUTTONS;
        return UNKNOWN;
    }

    /**
     * Gets appropriate mock name for this device type
     */
    public String getRandomMockName() { return RandomDeviceName.generateDeviceName(this); }

    /**
     * Gets appropriate mock descriptor for this device type
     */
    public String getRandomMockDescriptor() { return RandomDeviceDescriptor.generateDescriptor(this); }

    /**
     * Utility method to check if this device type typically has a controller number
     */
    public boolean hasControllerNumber() { return this == GAMEPAD || this == JOYSTICK; }

    /**
     * Utility method to check if this device type is typically built-in
     */
    public boolean isTypicallyBuiltIn() {
        return this == KEYBOARD_BUILTIN ||
                this == TOUCHSCREEN ||
                this == BUTTONS ||
                this == SENSOR;
    }
}