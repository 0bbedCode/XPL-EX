package eu.faircode.xlua.x.xlua.settings.random.randomizers.unique;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomVoicemailId extends RandomElement {
    public RandomVoicemailId() {
        super("Unique Voicemail ID (alpha tag)");
        putSettings(RandomizersCache.SETTING_UNIQUE_VOICEMAIL_ID);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushValue(context.stack.pop(), RandomGenerator.nextString(8).toLowerCase());
    }
}