package eu.faircode.xlua.x.network.randomizers;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.network.NetInfoGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

//network.routes
public class RandomRoutes extends RandomElement {
    public static RandomRoutes create() { return new RandomRoutes(); }
    public RandomRoutes() {
        super("Network Routes");
        bindSetting("network.routes");
    }

    @Override
    public String generateString() { return Str.joinList(new NetInfoGenerator().getRoutes(), ","); }
}