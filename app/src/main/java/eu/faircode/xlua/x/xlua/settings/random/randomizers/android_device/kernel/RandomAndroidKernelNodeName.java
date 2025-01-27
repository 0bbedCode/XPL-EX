package eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.kernel;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanAndUtils;

public class RandomAndroidKernelNodeName extends RandomElement {
    public RandomAndroidKernelNodeName() {
        super("Android Kernel Node Name");
        putSettings(RandomizersCache.SETTING_ANDROID_KERNEL_NODE_NAME);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushValue(context.stack.pop(), RanAndUtils.kernelNodeName(context));
    }
}