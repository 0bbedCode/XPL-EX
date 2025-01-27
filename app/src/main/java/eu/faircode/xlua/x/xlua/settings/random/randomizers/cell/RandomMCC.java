package eu.faircode.xlua.x.xlua.settings.random.randomizers.cell;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanUnqUtils;

public class RandomMCC extends RandomElement {
    public RandomMCC() {
        super("CELL MCC");
        putIndexSettings(RandomizersCache.SETTING_CELL_OPERATOR_MCC, 1, 2);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushSpecial(context.stack.pop(), String.valueOf(RandomGenerator.nextInt(100, 999)));
    }
}