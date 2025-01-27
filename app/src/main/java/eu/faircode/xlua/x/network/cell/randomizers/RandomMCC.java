package eu.faircode.xlua.x.network.cell.randomizers;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomMCC extends RandomElement {
    public static RandomMCC create() { return new RandomMCC(); }
    public RandomMCC() {
        super("Cell MCC");
        bindSetting("gsm.operator.mcc");
    }

    @Override
    public String generateString() { return String.valueOf(RandomGenerator.nextInt(100, 999)); }
}
