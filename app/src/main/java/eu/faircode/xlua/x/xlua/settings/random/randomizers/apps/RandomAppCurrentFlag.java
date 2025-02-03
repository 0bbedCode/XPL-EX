package eu.faircode.xlua.x.xlua.settings.random.randomizers.apps;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionNullElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomAppCurrentFlag /*extends RandomElement*/ {
    /*public RandomAppCurrentFlag() {
        super("Apps Time Current App");
        putSettings(RandomizersCache.SETTING_APP_TIME_CURRENT_ONLY);
        putOptions(
                RandomOptionNullElement.create(),
                RandomOptionString.create("true"),
                RandomOptionString.create("false"));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        if(!context.stack.isEmpty())
            context.stack.pop();
    }*/
}