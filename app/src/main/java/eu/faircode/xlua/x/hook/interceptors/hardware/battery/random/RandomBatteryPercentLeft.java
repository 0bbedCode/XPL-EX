package eu.faircode.xlua.x.hook.interceptors.hardware.battery.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomBatteryPercentLeft extends RandomElement {
    public static IRandomizer create() { return new RandomBatteryPercentLeft(); }
    public RandomBatteryPercentLeft() {
        super("Battery Percentage Remaining");
        bindSetting("battery.percentage.left");
    }

    @Override
    public String generateString() { return String.valueOf(RandomGenerator.nextInt(1, 100)); }
}
