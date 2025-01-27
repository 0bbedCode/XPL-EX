package eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanAndUtils;

public class RandomAndroidBuildIncremental extends RandomElement {
    public RandomAndroidBuildIncremental() {
        super("Android Build Incremental");
        putSettings(RandomizersCache.SETTING_ANDROID_BUILD_INCREMENTAL);
    }

    @Override
    public void randomize(RandomizerSessionContext context) { context.pushValue(context.stack.pop(), RanAndUtils.incremental(context)); }
}