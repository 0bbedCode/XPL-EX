package eu.faircode.xlua.x.hook.interceptors.build.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomBuildType extends RandomElement {
    public static IRandomizer create() { return new RandomBuildType(); }
    public static final String[] DEFAULT_BUILD_TYPES = new String[] {
            "user",
            "userdebug",
            "eng"
    };


    public RandomBuildType() {
        super("Build Type");
        bindSetting("android.build.type");
    }

    @Override
    public String generateString() { return DEFAULT_BUILD_TYPES[RandomGenerator.nextInt(0, DEFAULT_BUILD_TYPES.length)]; }
}