package eu.faircode.xlua.x.hook.interceptors.build.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomRomSecure extends RandomElement {
    public static IRandomizer create() { return new RandomRomSecure(); }
    public static final String[] SECURE_STATES = new String[] {
            "enforce",
            "log",
            "disable"
    };

    public RandomRomSecure() {
        super("Build ROM Secure");
        bindSetting("android.rom.secure");
    }

    @Override
    public String generateString() { return RandomGenerator.nextElement(SECURE_STATES);  }
}
