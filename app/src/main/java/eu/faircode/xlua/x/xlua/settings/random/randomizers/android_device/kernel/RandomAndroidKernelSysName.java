package eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.kernel;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanAndUtils;

public class RandomAndroidKernelSysName extends RandomElement {
    public RandomAndroidKernelSysName() {
        super("Android Kernel SysName");
        putSettings(RandomizersCache.SETTING_ANDROID_KERNEL_SYS_NAME);
        putOptions(RandomOptionString.generate(RanAndUtils.SYS_MACHINES));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}