package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.SoC;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanHwdUtils;

public class RandomSocBoardModel extends RandomElement {
    public RandomSocBoardModel() {
        super("Hardware SoC Model");
        putSettings(RandomizersCache.SETTING_SOC_BOARD_MODEL);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
         context.pushValue(context.stack.pop(), RanHwdUtils.socModel(context));
    }
}