package eu.faircode.xlua.x.hook.interceptors.devices.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.hook.interceptors.devices.InputDeviceType;

public class RandomDeviceName {
    //private static final Random random = new Random();

    // Virtual/Built-in Keyboards
    private static final String[] BUILTIN_KEYBOARD_NAMES = {
            "Virtual Keyboard",
            "Samsung Virtual Keyboard",
            "Pixel Virtual Keyboard",
            "System Virtual Keyboard",
            "Built-in Keyboard",
            "Generic Keyboard Device",
            "Internal Keyboard",
            "Hardware Keyboard",
            "Samsung Hardware Keyboard",
            "Pixel Hardware Keyboard",
            "QWERTY Keyboard",
            "Android System Keyboard",
            "Internal Input Device",
            "Platform Keyboard",
            "System Input Controller"
    };

    // External Keyboards
    private static final String[] EXTERNAL_KEYBOARD_NAMES = {
            "Logitech K780 Keyboard",
            "Apple Magic Keyboard",
            "Microsoft Bluetooth Keyboard",
            "Razer BlackWidow V3",
            "Corsair K70 RGB MK.2",
            "Anne Pro 2",
            "Ducky One 2 Mini",
            "Keychron K2",
            "SteelSeries Apex Pro",
            "HyperX Alloy Origins",
            "Das Keyboard 4 Professional",
            "Durgod Taurus K320",
            "HHKB Professional 2",
            "Leopold FC660C",
            "Varmilo VA87M",
            "Filco Majestouch 2",
            "Logitech G915",
            "Razer Huntsman Elite",
            "Corsair K95 Platinum",
            "Drop CTRL Mechanical Keyboard"
    };

    // Touchscreens
    private static final String[] TOUCHSCREEN_NAMES = {
            "Touchscreen",
            "Touch Screen",
            "Samsung Touchscreen",
            "Pixel Touch Screen",
            "system_touch",
            "Touch Input",
            "Primary Touchscreen",
            "Main Touch Screen",
            "Internal Touch Device",
            "ts_input_device",
            "Synaptics TouchScreen",
            "Goodix TouchScreen",
            "FTS TouchScreen",
            "sec_touchscreen",
            "himax-touchscreen",
            "focaltech_touch",
            "atmel_mxt_ts",
            "Novatek TouchScreen",
            "Touch Display",
            "Display with Touch"
    };

    // Gamepads/Controllers
    private static final String[] GAMEPAD_NAMES = {
            "Xbox Wireless Controller",
            "Xbox 360 Controller",
            "Xbox One Controller",
            "Xbox Elite Wireless Controller Series 2",
            "Wireless Controller", // PS4
            "Sony Interactive Entertainment Wireless Controller", // PS5
            "DualShock 4 Wireless Controller",
            "Nintendo Pro Controller",
            "8BitDo Pro 2",
            "8BitDo SN30 Pro+",
            "GameSir T4 Pro",
            "Razer Kishi",
            "Razer Wolverine V2",
            "PowerA Enhanced Wireless Controller",
            "PowerA MOGA XP5-X Plus",
            "SteelSeries Stratus Duo",
            "SteelSeries Nimbus+",
            "Bluetooth XBOX Controller",
            "Generic Gamepad",
            "NVIDIA Shield Controller",
            "Stadia Controller",
            "Luna Controller",
            "Backbone One Controller",
            "Nacon Revolution Pro Controller",
            "PDP Wired Controller"
    };

    // Mice
    private static final String[] MOUSE_NAMES = {
            "Logitech G Pro X Superlight",
            "Razer DeathAdder V2",
            "Corsair Dark Core RGB Pro",
            "SteelSeries Rival 600",
            "Bluetooth Travel Mouse",
            "Microsoft Surface Mouse",
            "Apple Magic Mouse",
            "Generic USB Mouse",
            "Gaming Mouse",
            "Bluetooth Mouse",
            "Logitech MX Master 3",
            "Razer Viper Ultimate",
            "Glorious Model O",
            "Zowie EC2",
            "Corsair M65 RGB Elite",
            "HyperX Pulsefire Haste",
            "Roccat Kone Pro",
            "Endgame Gear XM1r",
            "BenQ ZOWIE S2",
            "Finalmouse Starlight-12"
    };

    // Touchpads
    private static final String[] TOUCHPAD_NAMES = {
            "System Touchpad",
            "Internal Touchpad",
            "Synaptics Touchpad",
            "ELAN Touchpad",
            "Apple Magic Trackpad",
            "Built-in Touchpad",
            "Generic Touchpad Device",
            "PS/2 Synaptics TouchPad",
            "Precision Touchpad",
            "Multi-touch Trackpad",
            "ALPS Touchpad",
            "Microsoft Precision Touchpad",
            "Synaptics SMBus TouchPad",
            "Cypress TouchPad",
            "I2C HID Touchpad",
            "FocalTech Touchpad",
            "Goodix Touchpad",
            "Touch Pad Controller",
            "MacBook Trackpad",
            "Surface Touchpad"
    };

    // Audio Input Devices
    private static final String[] AUDIO_DEVICE_NAMES = {
            "waipo-mtp-snd-card Headset Jack",
            "msm8952-snd-card-mtp Headset",
            "WCD9xxx Headset",
            "Android Headset",
            "Bluetooth Headset",
            "USB Audio Device",
            "Generic Audio Jack",
            "Audio Input Device",
            "External Microphone",
            "Internal Microphone",
            "Realtek Audio Jack",
            "Qualcomm SND Card",
            "tasha-snd-card Headset",
            "msm8x16-snd-card-mtp Jack",
            "alsa-audio Headset Jack",
            "mt8167-snd-card jack",
            "tas2562-codec Jack",
            "cs35l41-codec Jack",
            "Audio-Codec Headset",
            "Sound Card Jack"
    };

    // Button Devices
    private static final String[] BUTTON_DEVICE_NAMES = {
            "Power Button",
            "Volume Buttons",
            "GPIO Keys",
            "Samsung Power Button",
            "Pixel Button Device",
            "System Buttons",
            "Hardware Keys",
            "Key Input Device",
            "Button Controller",
            "gpio-keys",
            "qpnp_pon",
            "pmic8xxx_pwrkey",
            "Vol_up&Vol_down",
            "pm8xxx-pwrkey",
            "mt6397-pwrkey",
            "mtk-pmic-keys",
            "spmi_pwrkey",
            "Button Array",
            "Navigation Keys",
            "Media Control Buttons"
    };

    // Sensor Devices
    private static final String[] SENSOR_DEVICE_NAMES = {
            "Device Orientation Sensor",
            "Motion Input",
            "Accelerometer",
            "Gyroscope Input",
            "Sensor Hub",
            "Position Sensor",
            "Rotation Vector",
            "Gravity Sensor",
            "Light Sensor Input",
            "Movement Detection",
            "BMI160 Accelerometer",
            "LSM6DSL Gyroscope",
            "AK09911 Magnetometer",
            "BMA255 Motion Sensor",
            "MPU6050 6-axis",
            "ICM20689 IMU",
            "LSM6DSO Sensor Hub",
            "BMG160 Gyro",
            "Proximity Sensor Input",
            "Fusion Sensor Device"
    };

    // Stylus/Pen Devices
    private static final String[] STYLUS_NAMES = {
            "S-Pen",
            "Samsung S-Pen",
            "Wacom Pen",
            "Active Pen",
            "Pixel Pen",
            "Stylus Input Device",
            "Digital Pen",
            "Smart Pen",
            "Pen Digitizer",
            "Surface Pen",
            "Apple Pencil",
            "Wacom Bamboo Ink",
            "Huawei M-Pencil",
            "Lenovo Precision Pen",
            "HP Tilt Pen",
            "Dell Premium Active Pen",
            "Adonit Ink Pro",
            "USI Stylus",
            "Galaxy S-Pen Pro",
            "Wacom One Pen"
    };

    // Joystick Devices
    private static final String[] JOYSTICK_NAMES = {
            "Logitech Extreme 3D Pro",
            "Thrustmaster T16000M",
            "CH Products Fighterstick",
            "Generic USB Joystick",
            "Flight Control System",
            "Saitek X52 Pro",
            "HOTAS Warthog",
            "VKB Gladiator NXT",
            "Turtle Beach VelocityOne",
            "Mad Catz V.1",
            "Thrustmaster TWCS",
            "Virpil WarBRD",
            "WinWing Orion",
            "VKB Gunfighter III",
            "CH Pro Throttle",
            "Logitech X56 HOTAS",
            "Thrustmaster TPR",
            "Honeycomb Alpha Flight Controls",
            "Force2D Joystick",
            "Cobra M5"
    };

    public static String generateDeviceName(InputDeviceType type) {
        switch (type) {
            case KEYBOARD_BUILTIN:
                return getRandomName(BUILTIN_KEYBOARD_NAMES);
            case KEYBOARD_EXTERNAL:
                return getRandomName(EXTERNAL_KEYBOARD_NAMES);
            case TOUCHSCREEN:
                return getRandomName(TOUCHSCREEN_NAMES);
            case GAMEPAD:
                return getRandomName(GAMEPAD_NAMES);
            case MOUSE:
                return getRandomName(MOUSE_NAMES);
            case TOUCHPAD:
                return getRandomName(TOUCHPAD_NAMES);
            case JOYSTICK:
                return getRandomName(JOYSTICK_NAMES);
            case AUDIO_JACK:
                return getRandomName(AUDIO_DEVICE_NAMES);
            case BUTTONS:
                return getRandomName(BUTTON_DEVICE_NAMES);
            case STYLUS:
                return getRandomName(STYLUS_NAMES);
            case SENSOR:
                return getRandomName(SENSOR_DEVICE_NAMES);
            case UNKNOWN:
            default:
                // For unknown, randomly select from any category
                String[][] allArrays = {
                        BUILTIN_KEYBOARD_NAMES, EXTERNAL_KEYBOARD_NAMES, TOUCHSCREEN_NAMES,
                        GAMEPAD_NAMES, MOUSE_NAMES, TOUCHPAD_NAMES, JOYSTICK_NAMES,
                        AUDIO_DEVICE_NAMES, BUTTON_DEVICE_NAMES, STYLUS_NAMES, SENSOR_DEVICE_NAMES
                };
                return getRandomName(allArrays[RandomGenerator.nextInt(allArrays.length)]);
        }
    }

    private static String getRandomName(String[] names) { return names[RandomGenerator.nextInt(names.length)]; }
}