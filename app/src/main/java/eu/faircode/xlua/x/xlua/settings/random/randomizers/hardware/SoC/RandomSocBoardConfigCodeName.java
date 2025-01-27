package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.SoC;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanHwdUtils;

public class RandomSocBoardConfigCodeName extends RandomElement {
    public RandomSocBoardConfigCodeName() {
        super("Hardware SoC Board Config Code");
        putSettings(RandomizersCache.SETTING_SOC_BOARD_CONFIG_CODE_NAME);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushValue(context.stack.pop(), RanHwdUtils.socCodeNames(context));
    }
}