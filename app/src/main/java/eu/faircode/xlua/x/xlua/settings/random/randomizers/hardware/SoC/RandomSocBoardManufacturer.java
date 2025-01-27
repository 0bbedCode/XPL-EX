package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.SoC;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanHwdUtils;

public class RandomSocBoardManufacturer extends RandomElement {
    public RandomSocBoardManufacturer() {
        super("Hardware SoC Board Manufacturer");
        putSettings(RandomizersCache.SETTING_SOC_BOARD_MANUFACTURER);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushValue(context.stack.pop(), RanHwdUtils.socCodeNames(context));
    }
}