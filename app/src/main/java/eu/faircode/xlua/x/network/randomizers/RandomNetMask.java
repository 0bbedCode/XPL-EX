package eu.faircode.xlua.x.network.randomizers;

import eu.faircode.xlua.x.network.NetInfoGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomNetMask extends RandomElement {
    public static RandomNetMask create() { return new RandomNetMask(); }
    public RandomNetMask() {
        super("Network Net Mask");
        bindSetting("network.netmask");
    }

    @Override
    public String generateString() { return new NetInfoGenerator().getNetmask(); }
}