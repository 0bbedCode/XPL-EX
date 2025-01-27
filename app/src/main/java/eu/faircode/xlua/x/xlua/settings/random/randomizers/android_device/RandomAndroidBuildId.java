package eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanAndUtils;

public class RandomAndroidBuildId extends RandomElement {
    public RandomAndroidBuildId() {
        super("Android Build ID");
        putSettings(RandomizersCache.SETTING_ANDROID_BUILD_ID);
    }

    @Override
    public void randomize(RandomizerSessionContext context) { context.pushValue(context.stack.pop(), RanAndUtils.id(context)); }
}