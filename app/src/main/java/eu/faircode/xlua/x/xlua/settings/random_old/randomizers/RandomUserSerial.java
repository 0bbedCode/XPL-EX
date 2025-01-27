package eu.faircode.xlua.x.xlua.settings.random_old.randomizers;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomUserSerial extends RandomElement {
    public static RandomUserSerial create() { return new RandomUserSerial(); }
    public RandomUserSerial() {
        super("User Profile Serial");
        bindSetting("account.user.serial");
    }

    @Override
    public String generateString() { return String.valueOf(RandomGenerator.nextInt(1000, 9999)); }
}