package eu.faircode.xlua.x.network.cell.randomizers;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomMNC extends RandomElement {
    public static RandomMCC create() { return new RandomMCC(); }
    public RandomMNC() {
        super("Cell MNC");
        bindSetting("gsm.operator.mnc");
    }

    @Override
    public String generateString() { return String.valueOf(RandomGenerator.nextInt(100, 999)); }
}
