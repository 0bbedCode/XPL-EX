package eu.faircode.xlua.x.hook.interceptors.build.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomBuildCodeName extends RandomElement {
    public static IRandomizer create() { return new RandomBuildCodeName(); }
    public static final String[] DEV_CODE_NAMES = {
            // OnePlus
            "avicii",        // Nord
            "bacon",         // One
            "cheeseburger",  // 5
            "dumpling",      // 5T
            "enchilada",     // 6
            "fajita",        // 6T
            "guacamole",     // 7 Pro
            "guacamoleb",    // 7
            "hotdog",        // 7T Pro
            "hotdogb",       // 7T
            "instantnoodle", // 8
            "instantnoodlep",// 8 Pro
            "kebab",         // 8T
            "lemonade",      // 9
            "lemonadep",     // 9 Pro
            "martini",       // 10 Pro

            // Google
            "sailfish",      // Pixel
            "marlin",        // Pixel XL
            "walleye",       // Pixel 2
            "taimen",        // Pixel 2 XL
            "blueline",      // Pixel 3
            "crosshatch",    // Pixel 3 XL
            "sargo",         // Pixel 3a
            "bonito",        // Pixel 3a XL
            "flame",         // Pixel 4
            "coral",         // Pixel 4 XL
            "sunfish",       // Pixel 4a
            "bramble",       // Pixel 4a 5G
            "redfin",        // Pixel 5
            "barbet",        // Pixel 5a
            "oriole",        // Pixel 6
            "raven",         // Pixel 6 Pro
            "bluejay",       // Pixel 6a
            "panther",       // Pixel 7
            "cheetah",       // Pixel 7 Pro

            // Samsung
            "a52q",          // A52
            "a53x",          // A53
            "b0q",           // S22 Ultra
            "r0q",           // S22
            "r8q",           // S22+
            "dm1q",          // S23
            "dm2q",          // S23+
            "dm3q",          // S23 Ultra
            "x1q",           // S20
            "y2q",           // S20+
            "z3q",           // S20 Ultra
            "o1q",           // S21
            "p3q",           // S21+
            "g1q",           // S21 Ultra

            // Xiaomi
            "apollo",        // Mi 10T
            "alioth",        // Poco F3
            "beryllium",     // Poco F1
            "cepheus",       // Mi 9
            "davinci",       // Redmi K20
            "equuleus",      // Mi 8 Pro
            "gauguin",       // Mi 10i
            "haydn",         // Redmi K40 Pro
            "umi",           // Mi 10
            "venus",         // Mi 11
            "star",          // Mi 11 Ultra
            "sweet",         // Redmi Note 10 Pro
            "raphael",       // Redmi K20 Pro
            "picasso",       // Redmi K30 5G

            // ASUS
            "obiwan",        // ROG Phone 3
            "rog2",          // ROG Phone 2
            "rog3",          // ROG Phone 3
            "rog5",          // ROG Phone 5
            "sake",          // Zenfone 8
            "vodka",         // Zenfone 8 Flip

            // POCO
            "gram",          // POCO M2 Pro
            "karna",         // POCO X3
            "surya",         // POCO X3 NFC
            "vayu",          // POCO X3 Pro
            "alioth",        // POCO F3
            "beryllium",     // POCO F1

            // Sony
            "griffin",       // Xperia 1
            "bahamut",       // Xperia 5
            "pdx203",        // Xperia 1 II
            "pdx206",        // Xperia 5 II
            "sagami",        // Xperia 1 III

            // LG
            "flash",         // V50
            "mh2lm",         // V60
            "timelm",        // Velvet
            "judypn",        // V40
            "joan",          // V30

            // Motorola
            "river",         // G7
            "ocean",         // G7 Power
            "kane",          // One Vision
            "parker",        // One Zoom
            "def",           // One Fusion+
            "nio",           // Edge S
            "random"
    };

    public RandomBuildCodeName() {
        super("Build Code Name");
        bindSetting("android.build.codename");
    }

    @Override
    public String generateString() {
        String high = DEV_CODE_NAMES[RandomGenerator.nextInt(0, DEV_CODE_NAMES.length)];
        return "random".equalsIgnoreCase(high) ?
                RandomGenerator.generateRandomAlphanumericString(RandomGenerator.nextInt(6, 17)) + "_" + RandomGenerator.generateRandomAlphanumericString(RandomGenerator.nextInt(6, 28))
                : RandomRomName.create().generateString() + "_" + high;
    }
}
