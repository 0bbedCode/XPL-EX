package eu.faircode.xlua.x.hook.interceptors.hardware.location.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.hook.interceptors.zone.RandomDateHelper;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomGpsModelYear extends RandomElement {
    public static RandomGpsModelYear create() { return new RandomGpsModelYear(); }
    public RandomGpsModelYear() {
        super("GPS Hardware Model Year");
        bindSetting("hardware.gps.model.year");
    }

    @Override
    public String generateString() { return String.valueOf(RandomGenerator.nextInt(2018, RandomDateHelper.CURRENT_YEAR)); }
}