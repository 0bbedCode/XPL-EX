package eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanAndUtils;

public class RandomAndroidBuildFingerprint extends RandomElement {
    public RandomAndroidBuildFingerprint() {
        super("Android Build Fingerprint");
        putSettings(RandomizersCache.SETTING_ANDROID_BUILD_FINGERPRINT);
    }

    @Override
    public void randomize(RandomizerSessionContext context) { context.pushValue(context.stack.pop(), RanAndUtils.fingerprint(context)); }
}