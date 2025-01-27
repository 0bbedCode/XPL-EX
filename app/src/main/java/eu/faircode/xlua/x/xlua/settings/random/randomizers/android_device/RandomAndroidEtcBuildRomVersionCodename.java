package eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanAndUtils;

/*
    ToDo:
 */
public class RandomAndroidEtcBuildRomVersionCodename extends RandomElement {
    public RandomAndroidEtcBuildRomVersionCodename() {
        super("Android ETC ROM Version Codename");
        putSettings(RandomizersCache.SETTING_ANDROID_ETC_BUILD_ROM_VERSION_CODENAME);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushValue(context.stack.pop(), RanAndUtils.romVersionCodename(context));
    }
}