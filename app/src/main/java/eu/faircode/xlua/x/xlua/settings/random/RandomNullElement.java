package eu.faircode.xlua.x.xlua.settings.random;

import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanAndUtils;

public class RandomNullElement extends RandomElement {
    public static IRandomizer create() { return new RandomNullElement(); }
    public RandomNullElement() {
        super("[Select Randomizer]");
        putSettings("null");
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //Go through all randomizer and select one ? xD
    }
}