package eu.faircode.xlua.x.xlua.settings.random.randomizers.network;

import eu.faircode.xlua.x.network.NetInfoGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomNetGateway extends RandomElement {
    public RandomNetGateway() {
        super("Network Gateway");
        putSettings(RandomizersCache.SETTING_NET_GATEWAY);
        putParents(RandomizersCache.SETTING_NET_PARENT_CONTROL);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushValue(context.stack.pop(), new NetInfoGenerator().getGateway());
    }
}