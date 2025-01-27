package eu.faircode.xlua.x.hook.interceptors.build.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomBuildVersion extends RandomElement {
    public static IRandomizer create() { return new RandomBuildVersion(); }
    public RandomBuildVersion() {
        super("Build Android Version");
        bindSetting("android.build.version");
    }

    @Override
    public String generateString() { return String.valueOf(RandomGenerator.nextInt(6, 15)); }
}