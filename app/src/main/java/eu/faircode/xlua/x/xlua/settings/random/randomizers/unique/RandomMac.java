package eu.faircode.xlua.x.xlua.settings.random.randomizers.unique;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.ran.RanGpuUtils;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanUnqUtils;

public class RandomMac extends RandomElement {
    public RandomMac() {
        super("Unique MacAddress");
        putSettings(RandomizersCache.SETTING_UNIQUE_MAC);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushValue(context.stack.pop(), RanUnqUtils.mac(context));
    }
}