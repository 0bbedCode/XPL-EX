package eu.faircode.xlua.x.xlua.settings.random.randomizers.cell;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomMSIN extends RandomElement {
    public RandomMSIN() {
        super("CELL MSIN");
        putIndexSettings(RandomizersCache.SETTING_CELL_MSIN, 1, 2);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushSpecial(context.stack.pop(), RandomGenerator.nextStringNumeric(9));
    }
}