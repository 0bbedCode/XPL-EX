package eu.faircode.xlua.x.hook.interceptors.build.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomRomBootState extends RandomElement {
    public static IRandomizer create() { return new RandomRomBootState(); }
    public static final String[] BOOT_STATES = new String[] {
            "green",
            "yellow",
            "orange",
            "red"
    };

    public RandomRomBootState() {
        super("Build ROM Boot State");
        bindSetting("android.rom.verified.boot");
    }

    @Override
    public String generateString() { return RandomGenerator.nextElement(BOOT_STATES);  }
}
