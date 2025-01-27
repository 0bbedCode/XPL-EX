package eu.faircode.xlua.x.network.randomizers;

import eu.faircode.xlua.x.network.NetInfoGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomNetAddress extends RandomElement {
    public static RandomNetAddress create() { return new RandomNetAddress(); }
    public RandomNetAddress() {
        super("Network Host Address");
        bindSetting("network.host.address");
    }

    @Override
    public String generateString() { return new NetInfoGenerator().getIpv4Address(); }
}