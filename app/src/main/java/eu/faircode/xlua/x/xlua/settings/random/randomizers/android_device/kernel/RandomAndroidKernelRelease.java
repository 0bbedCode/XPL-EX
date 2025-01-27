package eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.kernel;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanAndUtils;

public class RandomAndroidKernelRelease extends RandomElement {
    public RandomAndroidKernelRelease() {
        super("Android Kernel Release");
        putSettings(RandomizersCache.SETTING_ANDROID_KERNEL_RELEASE);
        putOptions(RandomOptionString.generate(RanAndUtils.ANDROID_KERNEL_VERSION_RELEASES));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}