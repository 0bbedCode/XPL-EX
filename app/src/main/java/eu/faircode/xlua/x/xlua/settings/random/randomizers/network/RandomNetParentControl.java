package eu.faircode.xlua.x.xlua.settings.random.randomizers.network;

import java.util.List;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.network.NetInfoGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomNetParentControl extends RandomElement {
    public RandomNetParentControl() {
        super("Network ISP Parent Control");
        this.isParent = true;
        putSettings(RandomizersCache.SETTING_NET_PARENT_CONTROL);
        putRequirements(RandomizersCache.SETTING_NET_DHCP,
                RandomizersCache.SETTING_NET_DNS,
                RandomizersCache.SETTING_NET_DNS_LIST,
                RandomizersCache.SETTING_NET_DOMAINS,
                RandomizersCache.SETTING_NET_GATEWAY,
                RandomizersCache.SETTING_NET_HOST,
                RandomizersCache.SETTING_NET_HOST_NAME,
                RandomizersCache.SETTING_NET_NETMASK,
                RandomizersCache.SETTING_NET_ROUTES);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        NetInfoGenerator infoGenerator = new NetInfoGenerator();

        context.pushValue(RandomizersCache.SETTING_NET_DHCP, infoGenerator.getDhcpServer());
        context.pushValue(RandomizersCache.SETTING_NET_DNS, infoGenerator.getDnsServers().get(0));
        context.pushValue(RandomizersCache.SETTING_NET_DNS_LIST, Str.joinList(infoGenerator.getDnsServers()));
        context.pushValue(RandomizersCache.SETTING_NET_DOMAINS, infoGenerator.getDomain());
        context.pushValue(RandomizersCache.SETTING_NET_GATEWAY, infoGenerator.getGateway());
        context.pushValue(RandomizersCache.SETTING_NET_HOST, infoGenerator.getIpv4Address());
        context.pushValue(RandomizersCache.SETTING_NET_HOST_NAME, infoGenerator.getIpv4Address());
        context.pushValue(RandomizersCache.SETTING_NET_NETMASK, infoGenerator.getNetmask());
        context.pushValue(RandomizersCache.SETTING_NET_ROUTES, Str.joinList(infoGenerator.getRoutes()));
        //context.pushValue(RandomizersCache.SETTING_NET_DHCP, infoGenerator.getDhcpServer());

        context.pushSpecial(context.stack.pop(), infoGenerator.getProvider());
    }
}