package eu.faircode.xlua.x.hook.interceptors.build.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomBuildSDK extends RandomElement {
    public static IRandomizer create() { return new RandomBuildSDK(); }
    public RandomBuildSDK() {
        super("Build SDK");
        bindSettings("android.build.min.sdk", "android.build.version.sdk");
    }

    @Override
    public String generateString() { return String.valueOf(RandomGenerator.nextInt(23, 35)); }
}