package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.ran.RanGpuUtils;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanDevUtils;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanHwdUtils;

public class RandomSocBasebandBoardConfigName extends RandomElement {
    public RandomSocBasebandBoardConfigName() {
        super("Hardware Base Band Board Config");
        putSettings(RandomizersCache.SETTING_SOC_BASEBAND_BOARD_CONFIG_NAME);
    }

    @Override
    public void randomize(RandomizerSessionContext context) { context.pushValue(context.stack.pop(), RanGpuUtils.socBaseboardConfigName(context)); }
}
