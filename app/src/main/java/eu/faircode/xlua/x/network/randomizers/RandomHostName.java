package eu.faircode.xlua.x.network.randomizers;

import eu.faircode.xlua.x.network.NetInfoGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomHostName extends RandomElement {
    public static RandomHostName create() { return new RandomHostName(); }
    public RandomHostName() {
        super("Network Host Name");
        bindSetting("network.host.name");
    }

    @Override
    public String generateString() { return new NetInfoGenerator().getIpv4Address(); }
}