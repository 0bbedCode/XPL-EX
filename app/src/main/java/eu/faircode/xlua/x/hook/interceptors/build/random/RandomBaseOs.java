package eu.faircode.xlua.x.hook.interceptors.build.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomBaseOs extends RandomElement {
    public static IRandomizer create() { return new RandomBaseOs(); }
    private static final String[] DEFAULT_MANUFACTURERS = new String[] {
            "Linux",
            "Unix",
            "Android",
            "ios",
            "Debian"
    };

    public RandomBaseOs() {
        super("Build Base OS");
        bindSetting("android.build.base.os");
        setIsCheckableRandom(false);
    }

    @Override
    public String generateString() { return DEFAULT_MANUFACTURERS[RandomGenerator.nextInt(0, DEFAULT_MANUFACTURERS.length)];  }
}
