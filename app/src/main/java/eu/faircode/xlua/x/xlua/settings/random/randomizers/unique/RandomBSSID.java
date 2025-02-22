package eu.faircode.xlua.x.xlua.settings.random.randomizers.unique;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanUnqUtils;

public class RandomBSSID extends RandomElement {
    public RandomBSSID() {
        super("Wifi BSSID");
        putSettings(RandomizersCache.SETTING_UNIQUE_NET_BSSID);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushValue(context.stack.pop(), RanUnqUtils.mac(context));
    }
}