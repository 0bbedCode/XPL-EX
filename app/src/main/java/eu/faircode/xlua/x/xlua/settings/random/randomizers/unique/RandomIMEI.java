package eu.faircode.xlua.x.xlua.settings.random.randomizers.unique;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomIMEI extends RandomElement {
    public RandomIMEI() {
        super("CELL IMEI");
        putIndexSettings(RandomizersCache.SETTING_UNIQUE_IMEI, 1, 2);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushSpecial(context.stack.pop(), RandomGenerator.nextStringNumeric(15));
    }
}
