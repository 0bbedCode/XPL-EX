package eu.faircode.xlua.x.xlua.settings.random.utils;

import eu.faircode.xlua.x.data.utils.random.RandomDate;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;

public class RanAndUtils {

    public static final String[] DEFAULT_BUILD_TAGS = new String[] {
            "release-keys",
            "test-keys",
            "dev-keys",
            "debug",
            "eng",
            "user",
            "userdebug",
            "keys",
            "release",
            "unofficial"
    };

    public static final String[] DEFAULT_BUILD_USERS = new String[] {
            "jenkins",
            "buildbot",
            "android-build",
            "ido",
            "god",
            "random"
    };

    public static final String[] SYS_MACHINES = new String[] {
            "Linux",
            "Unix",
            "Debian",
            "ido",
            "god",
            "random"
    };

    public static final String[] ANDROID_KERNEL_VERSION_RELEASES = {
            "2.6.25",     // Android 1.0-1.1
            "2.6.27",     // Android 1.5 (Cupcake)
            "2.6.29",     // Android 1.6 (Donut) and 2.0-2.1 (Eclair)
            "2.6.32",     // Android 2.2 (Froyo)
            "2.6.35",     // Android 2.3 (Gingerbread)
            "2.6.36",     // Android 3.0-3.2 (Honeycomb)
            "3.0.1",      // Android 4.0 (Ice Cream Sandwich)
            "3.0.31",     // Android 4.1-4.3 (Jelly Bean)
            "3.4",        // Android 4.4 (KitKat)
            "3.10",       // Android 5.0-5.1 (Lollipop) and 6.0 (Marshmallow)
            "3.18",       // Android 7.0-7.1 (Nougat)
            "4.4",        // Android 7.0+ (Nougat, Oreo, Pie, 10)
            "4.9",        // Android 8.0+ (Oreo, Pie, 10, 11)
            "4.14",       // Android 9+ (Pie, 10, 11, 12, 13, 14)
            "4.19",       // Android 10+ (10, 11, 12, 13, 14)
            "5.4",        // Android 11+ (11, 12, 13, 14)
            "5.10",       // Android 12+ (12, 13, 14)
            "5.15",       // Android 13+ (13, 14)
            "6.1"         // Android 14+
    };

    public static String dateEpoch(RandomizerSessionContext session) {
        return String.valueOf(RandomGenerator.nextDate().getDateSeconds());
    }

    public static String dateZero(RandomizerSessionContext session) {
        return RandomGenerator.nextDate().toDate();
    }

    public static String dateOne(RandomizerSessionContext session) {
        return RandomGenerator.nextDate().toDateOne();
    }

    public static String dateTwo(RandomizerSessionContext session) {
        return RandomGenerator.nextDate().toDateTwo();
    }

    public static int version(RandomizerSessionContext session) {
        return RandomGenerator.nextInt(6, 15);
    }

    /*public static String tags(RandomizerSessionContext session) {
        return RandomGenerator.nextElement(DEFAULT_BUILD_TAGS);
    }*/

    public static String incremental(RandomizerSessionContext session) {
        return RandomGenerator.nextString(6, 28);
    }

    public static String description(RandomizerSessionContext session) {
        return RandomGenerator.nextString(15, 35);
    }

    public static String id(RandomizerSessionContext session) {
        return RandomGenerator.nextString(8, 25);
    }

    public static String displayId(RandomizerSessionContext session) {
        return RandomGenerator.nextString(8, 25);
    }

    public static String flavor(RandomizerSessionContext session) {
        return RandomGenerator.nextString(8, 25);
    }

    public static String host(RandomizerSessionContext session) {
        return RandomGenerator.nextString(8, 25);
    }

    public static String patch(RandomizerSessionContext session) {
        return RandomGenerator.nextDate().toIsoDate();
    }

    public static String codename(RandomizerSessionContext session) {
        return RandomGenerator.nextString(5, 15);
    }

    public static String fingerprint(RandomizerSessionContext session) {
        return RandomGenerator.nextString(5, 27);
    }

    public static String baseOs(RandomizerSessionContext session) {
        return RandomGenerator.nextString(6, 28);
    }

    public static String romVersionCodename(RandomizerSessionContext session) {
        return RandomGenerator.nextString(3, 6);
    }

    public static String kernelVersionString(RandomizerSessionContext session) {
        return RandomGenerator.nextDate().toKernelDate();
    }


    public static String kernelNodeName(RandomizerSessionContext session) {
        return generateNodeName();
    }



    private static final String[] MANUFACTURERS = {
            "samsung", "huawei", "xiaomi", "oneplus", "google", "sony", "lg", "motorola", "asus"
    };

    private static final String[] DEVICE_TYPES = {
            "phone", "tablet", "mobile", "device", "android"
    };

    private static final String[] ROM_PREFIXES = {
            "lineage", "aosp", "pixel", "evolution", "crdroid", "havoc"
    };

    private static final String[] GENERIC_NAMES = {
            "localhost", "android", "buildbot", "android-device", "unknown", "mobile-android", "(none)", "generic"
    };

    public static String generateNodeName() {
        int type = RandomGenerator.nextInt(5); // Choose random pattern type
        switch(type) {
            case 0: // Manufacturer-based
                return MANUFACTURERS[RandomGenerator.nextInt(MANUFACTURERS.length)] +
                        "-" + generateRandomModel();

            case 1: // ROM-based
                return ROM_PREFIXES[RandomGenerator.nextInt(ROM_PREFIXES.length)] +
                        "_" + generateRandomModel();

            case 2: // Android pattern
                return "android-" + RandomGenerator.nextInt(9999);

            case 3: // Test/Build pattern
                return RandomGenerator.nextBoolean() ?
                        "test-" + RandomGenerator.nextInt(999) :
                        "build-" + generateRandomModel();

            default: // Generic names
                return GENERIC_NAMES[RandomGenerator.nextInt(GENERIC_NAMES.length)];
        }
    }

    private static String generateRandomModel() {
        StringBuilder model = new StringBuilder();

        // Generate something like "a50", "m31", "p20", etc.
        model.append(Character.toLowerCase((char)('a' + RandomGenerator.nextInt(26))))
                .append(RandomGenerator.nextInt(99));

        // Sometimes add a suffix
        if (RandomGenerator.nextBoolean()) {
            model.append(RandomGenerator.nextBoolean() ? "pro" : "lite");
        }

        return model.toString();
    }

}
