package eu.faircode.xlua.x.hook.interceptors.hardware.battery.random;

import eu.faircode.xlua.x.hook.interceptors.zone.RandomDateHelper;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomBatteryTimeRemaining extends RandomElement {
    public static IRandomizer create() { return new RandomBatteryTimeRemaining(); }
    public RandomBatteryTimeRemaining() {
        super("Battery Time Remaining (MS)");
        bindSetting("battery.charge.time.remaining");
    }

    @Override
    public String generateString() { return String.valueOf(RandomDateHelper.generateHoursInMilliseconds(2, 23)); }
}
