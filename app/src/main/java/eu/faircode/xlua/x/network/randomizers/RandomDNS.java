package eu.faircode.xlua.x.network.randomizers;

import eu.faircode.xlua.x.network.NetInfoGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomDNS extends RandomElement {
    public static RandomDNS create() { return new RandomDNS(); }
    public RandomDNS() {
        super("Network DNS");
        bindSetting("network.dns");
    }

    @Override
    public String generateString() { return new NetInfoGenerator().getDnsServers().get(0); }
}
