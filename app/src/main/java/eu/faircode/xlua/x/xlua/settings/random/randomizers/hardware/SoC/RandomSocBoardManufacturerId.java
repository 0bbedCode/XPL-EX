package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.SoC;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanHwdUtils;

public class RandomSocBoardManufacturerId extends RandomElement {
    public RandomSocBoardManufacturerId() {
        super("Hardware SoC Board Manufacturer ID");
        putSettings(RandomizersCache.SETTING_SOC_BOARD_MANUFACTURER_ID);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushValue(context.stack.pop(), RanHwdUtils.socCodeNames(context));
    }
}