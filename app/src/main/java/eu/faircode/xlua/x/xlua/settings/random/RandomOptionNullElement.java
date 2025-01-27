package eu.faircode.xlua.x.xlua.settings.random;

import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;

public class RandomOptionNullElement extends RandomElement {
    public static IRandomizer create() { return new RandomOptionNullElement(); }
    public RandomOptionNullElement() {
        super("Select Option");
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //return
    }
}