package eu.faircode.xlua.x.network.randomizers;

import eu.faircode.xlua.x.network.NetInfoGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomDomains extends RandomElement {
    public static RandomDomains create() { return new RandomDomains(); }
    public RandomDomains() {
        super("Network Domains");
        bindSetting("network.domains");
    }

    @Override
    public String generateString() { return new NetInfoGenerator().getDomain(); }
}