package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.ran.RanGpuUtils;

public class RandomSocBasebandBoardImplementor extends RandomElement {
    public RandomSocBasebandBoardImplementor() {
        super("Hardware Base Band Board Impl");
        putSettings(RandomizersCache.SETTING_SOC_BASEBAND_BOARD_IMPLEMENTOR);
    }

    @Override
    public void randomize(RandomizerSessionContext context) { context.pushValue(context.stack.pop(), RanGpuUtils.socBasebandBoardRadioVersion(context)); }
}
