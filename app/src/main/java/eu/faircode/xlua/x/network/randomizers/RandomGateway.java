package eu.faircode.xlua.x.network.randomizers;

import eu.faircode.xlua.x.network.NetInfoGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomGateway extends RandomElement {
    public static RandomGateway create() { return new RandomGateway(); }
    public RandomGateway() {
        super("Network Gateway");
        bindSetting("network.gateway");
    }

    @Override
    public String generateString() { return new NetInfoGenerator().getGateway(); }
}