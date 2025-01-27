package eu.faircode.xlua.x.xlua.settings.random.randomizers.network;

import java.util.List;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.network.NetInfoGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomNetDNS extends RandomElement {
    public RandomNetDNS() {
        super("Network DNS");
        putSettings(RandomizersCache.SETTING_NET_DNS);
        putRequirement(RandomizersCache.SETTING_NET_DNS_LIST);
        putParents(RandomizersCache.SETTING_NET_PARENT_CONTROL);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        String name = context.stack.pop();
        if(!context.values.containsKey(name)) {
            List<String> req = context.resolveRequirements(getRequirements(name));
            String dnsList = context.getValue(req.get(0));
            List<String> list = Str.splitToList(dnsList);
            context.pushSpecial(name, list.get(0));
        }
    }
}