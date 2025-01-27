package eu.faircode.xlua.x.xlua.settings.random.utils;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.data.utils.random.RandomStringKind;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;

public class RanDevUtils {
    public static final String MANUFACTURER_SAMSUNG = "Samsung";
    public static final String MANUFACTURER_ONE_PLUS = "OnePlus";
    public static final String MANUFACTURER_OPPO = "Oppo";
    public static final String MANUFACTURER_XIAOMI = "Xiaomi";
    public static final String MANUFACTURER_GOOGLE = "Google";
    public static final String MANUFACTURER_LENOVO = "Lenovo";
    public static final String MANUFACTURER_ROG = "ROG";

    public static final String[] MANUFACTURERS = new String[] {
            MANUFACTURER_SAMSUNG,
            MANUFACTURER_ONE_PLUS,
            MANUFACTURER_OPPO,
            MANUFACTURER_XIAOMI,
            MANUFACTURER_GOOGLE,
            MANUFACTURER_LENOVO,
            MANUFACTURER_ROG
    };

    public static String manufacturer(RandomizerSessionContext session) {
        return RandomGenerator.nextElement(RanDevUtils.MANUFACTURERS);
    }

    public static String brand(RandomizerSessionContext session) {
        return RandomGenerator.nextElement(RanDevUtils.MANUFACTURERS);
    }

    public static String bootloader(RandomizerSessionContext session) {
        return RandomGenerator.nextString(RandomStringKind.ALPHA_NUMERIC_UPPERCASE, 8, 30);
    }

    public static String nickName(RandomizerSessionContext session) {
        return RandomGenerator.nextElement(RanDevUtils.MANUFACTURERS);
    }

    public static String model(RandomizerSessionContext session) {
        return RandomGenerator.nextString(5, 15);
    }

    public static String codename(RandomizerSessionContext session) {
        return RandomGenerator.nextString(5, 15);
    }

}
