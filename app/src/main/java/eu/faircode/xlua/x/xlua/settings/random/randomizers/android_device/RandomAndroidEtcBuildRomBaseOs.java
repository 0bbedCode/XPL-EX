package eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanAndUtils;

public class RandomAndroidEtcBuildRomBaseOs extends RandomElement {
    public RandomAndroidEtcBuildRomBaseOs() {
        super("Android ETC Base OS");
        putSettings(RandomizersCache.SETTING_ANDROID_ETC_BUILD_ROM_BASE_OS);
    }

    @Override
    public void randomize(RandomizerSessionContext context) { context.pushValue(context.stack.pop(), RanAndUtils.baseOs(context)); }
}