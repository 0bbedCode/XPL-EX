package eu.faircode.xlua.x.xlua.settings.random.randomizers.generic;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanAndUtils;

public class RandomDateTwo extends RandomElement {
    public RandomDateTwo() {
        super("Random Date Two (YYYYMMDD)");
        putSettings(RandomizersCache.SETTING_ANDROID_BUILD_DATE_TWO);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        for (String setting : getSettings()) {
            context.pushValue(setting, RanAndUtils.dateTwo(context));
        }
    }
}