package eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanDevUtils;

public class RandomDeviceModel extends RandomElement {
    public RandomDeviceModel() {
        super("Device Model");
        putSettings(RandomizersCache.SETTING_DEVICE_MODEL);
        //putParents(RandomizersCache.SETTING_PARENT_DEVICE);
    }

    @Override
    public void randomize(RandomizerSessionContext context) { context.pushValue(context.stack.pop(), RanDevUtils.model(context)); }
}