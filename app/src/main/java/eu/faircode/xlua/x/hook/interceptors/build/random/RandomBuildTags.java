package eu.faircode.xlua.x.hook.interceptors.build.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomBuildTags extends RandomElement {
    public static IRandomizer create() { return new RandomBuildTags(); }
    private static final String[] DEFAULT_BUILD_TAGS = new String[] {
            "release-keys",
            "test-keys",
            "dev-keys",
            "debug",
            "eng",
            "user",
            "userdebug",
            "keys",
            "release",
            "unofficial"
    };

    public RandomBuildTags() {
        super("Build Tag");
        bindSetting("android.build.tags");
    }

    @Override
    public String generateString() { return DEFAULT_BUILD_TAGS[RandomGenerator.nextInt(0, DEFAULT_BUILD_TAGS.length)]; }
}