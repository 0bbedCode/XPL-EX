package eu.faircode.xlua.x.xlua.settings.random.randomizers.unique;

import java.util.UUID;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanUnqUtils;

public class RandomUUID extends RandomElement {
    public RandomUUID() {
        super("Random UUID");
        putSettings(
                RandomizersCache.SETTING_UNIQUE_UUID,
                RandomizersCache.SETTING_UNIQUE_VA_ID,
                RandomizersCache.SETTING_UNIQUE_ANON_ID,
                RandomizersCache.SETTING_UNIQUE_OPEN_ANON_ID,
                RandomizersCache.SETTING_UNIQUE_BOOT_ID,
                RandomizersCache.SETTING_UNIQUE_FACEBOOK_ID,
                RandomizersCache.SETTING_UNIQUE_GOOGLE_ID);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushValue(context.stack.pop(), UUID.randomUUID().toString());
    }
}