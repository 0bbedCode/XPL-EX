package eu.faircode.xlua.x.network.randomizers;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.network.NetInfoGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomDNSList extends RandomElement {
    public static RandomDNSList create() { return new RandomDNSList(); }
    public RandomDNSList() {
        super("Network DNS List");
        bindSetting("network.dns.list");
    }

    @Override
    public String generateString() { return Str.joinList(new NetInfoGenerator().getDnsServers(), ","); }
}