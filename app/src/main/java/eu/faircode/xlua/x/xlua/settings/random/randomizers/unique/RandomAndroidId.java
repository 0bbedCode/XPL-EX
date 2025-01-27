package eu.faircode.xlua.x.xlua.settings.random.randomizers.unique;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanUnqUtils;

public class RandomAndroidId extends RandomElement {
    public RandomAndroidId() {
        super("Unique Android ID");
        putSettings(RandomizersCache.SETTING_UNIQUE_ANDROID_ID);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushValue(context.stack.pop(), RandomGenerator.nextStringHex(16).toLowerCase());
    }
}