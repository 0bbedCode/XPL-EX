package eu.faircode.xlua.x.xlua.settings.random_old.randomizers.etc;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomEtcNetflix extends RandomElement {
    public static IRandomizer create() { return new RandomEtcNetflix(); }
    private static final String[] PLATFORM_PREFIXES = {"Q", "A", "S", "M"};
    private static final String[] COMMON_PLATFORMS = {
            "855",
            "888",
            "8550",
            "6115",
            "7125",
            "662",
            "460"
    };

    public static String generateBspRev() {
        // Choose random format (70% chance of Q/Platform format, 30% chance of A format)
        return RandomGenerator.nextFloat() < 0.7f ? generatePlatformFormat() : generateAFormat();
    }

    private static String generatePlatformFormat() {
        // Format: Q855-16947-1
        StringBuilder bsp = new StringBuilder();
        // Add platform prefix (Q, S, M)
        bsp.append(RandomGenerator.nextElement(PLATFORM_PREFIXES));
        // Add platform number
        bsp.append(RandomGenerator.nextElement(COMMON_PLATFORMS));
        // Add separator
        bsp.append("-");
        // Add random 5-digit number
        bsp.append(String.format("%05d", RandomGenerator.nextInt(0, 100000)));
        // Add revision
        bsp.append("-");
        bsp.append(RandomGenerator.nextInt(1, 10)); // 1-9
        return bsp.toString();
    }

    private static String generateAFormat() {
        // Format: A00000
        StringBuilder bsp = new StringBuilder();
        bsp.append("A");
        bsp.append(String.format("%05d", RandomGenerator.nextInt(0, 100000)));
        return bsp.toString();
    }

    public RandomEtcNetflix() {
        super("Netflix Shit");
        bindSetting("android.rom.netflix");
    }

    @Override
    public String generateString() { return generateBspRev();  }

}
