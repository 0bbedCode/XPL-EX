package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.ran.RanGpuUtils;

public class RandomSocBasebandBoardRadioVersion extends RandomElement {
    public RandomSocBasebandBoardRadioVersion() {
        super("Hardware Base Band Board Radio Version");
        putSettings(RandomizersCache.SETTING_SOC_BASEBAND_BOARD_RADIO_VERSION);
    }

    @Override
    public void randomize(RandomizerSessionContext context) { context.pushValue(context.stack.pop(), RanGpuUtils.socBasebandBoardRadioVersion(context)); }
}
