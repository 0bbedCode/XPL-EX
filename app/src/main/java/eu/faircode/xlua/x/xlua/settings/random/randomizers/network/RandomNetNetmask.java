package eu.faircode.xlua.x.xlua.settings.random.randomizers.network;

import eu.faircode.xlua.x.network.NetInfoGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomNetNetmask extends RandomElement {
    public RandomNetNetmask() {
        super("Network Netmask");
        putSettings(RandomizersCache.SETTING_NET_NETMASK);
        putParents(RandomizersCache.SETTING_NET_PARENT_CONTROL);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushValue(context.stack.pop(), new NetInfoGenerator().getNetmask());
    }
}