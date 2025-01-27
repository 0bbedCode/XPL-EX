package eu.faircode.xlua.x.hook.interceptors.build.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomDevCodeName extends RandomElement {
    public static IRandomizer create() { return new RandomDevCodeName(); }

    public RandomDevCodeName() {
        super("Build DEV Code Name");
        bindSetting("android.build.dev.codename");
        setIsCheckableRandom(false);
    }

    @Override
    public String generateString() { return RandomGenerator.randomStringIfRandomElse(RandomBuildCodeName.DEV_CODE_NAMES[RandomGenerator.nextInt(0, RandomBuildCodeName.DEV_CODE_NAMES.length)]); }
}
