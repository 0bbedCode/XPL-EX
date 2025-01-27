package eu.faircode.xlua.x.hook.interceptors.build.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomBuildUser extends RandomElement {
    public static IRandomizer create() { return new RandomBuildUser(); }
    public static final String[] DEFAULT_BUILD_USERS = new String[] {
            "jenkins",
            "buildbot",
            "android-build",
            "ido",
            "god",
            "random"
    };

    public RandomBuildUser() {
        super("Build User");
        bindSetting("android.build.user");
    }

    @Override
    public String generateString() { return DEFAULT_BUILD_USERS[RandomGenerator.nextInt(0, DEFAULT_BUILD_USERS.length)]; }
}