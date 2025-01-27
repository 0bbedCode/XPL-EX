package eu.faircode.xlua.x.network.randomizers;

import eu.faircode.xlua.x.network.NetInfoGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomDHCPServer extends RandomElement {
    public static RandomDHCPServer create() { return new RandomDHCPServer(); }
    public RandomDHCPServer() {
        super("Network DHCP Server");
        bindSetting("network.dhcp.server");
    }

    @Override
    public String generateString() { return new NetInfoGenerator().getDhcpServer(); }
}