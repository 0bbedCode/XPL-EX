package eu.faircode.xlua.x.xlua.configs;


import android.text.TextUtils;

public enum ConfigType {
    NONE(0),
    NETWORK(1),
    CELL(2),
    PHONE(3),
    HOOKS(4),
    PHONE_ROM(5),
    INTERNAL(6),
    ROM(7);

    private final int value;
    ConfigType(int value) { this.value = value; }
    public int getValue() { return value; }

    public static ConfigType getTypeFromString(String type) {
        if(TextUtils.isEmpty(type))
            return ConfigType.NONE;

        String low = type.toLowerCase();
        switch (low) {
            case "phone":
                return ConfigType.PHONE;
            case "rom":
                return ConfigType.ROM;
            case "cell":
                return ConfigType.CELL;
            case "network":
                return ConfigType.NETWORK;
            case "phone_rom":
                return ConfigType.PHONE_ROM;
            case "internal":
                return ConfigType.INTERNAL;
            default:
                return ConfigType.NONE;
        }
    }
}