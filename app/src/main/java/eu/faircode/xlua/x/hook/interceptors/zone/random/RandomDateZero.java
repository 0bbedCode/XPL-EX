package eu.faircode.xlua.x.hook.interceptors.zone.random;

import eu.faircode.xlua.utilities.RandomUtil;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomDateZero extends RandomElement {
    public static IRandomizer create() { return new RandomDateZero(); }
    public static final String FORMAT = "%s %s %s %s:%s:%s %s %s";

    public RandomDateZero() {
        super("Random Date (W MT DY HR:MIN:SC TZ YR)");
        bindSettings("android.build.date", "random.date.zero");
    }

    @Override
    public String generateString() {
        return String.format(FORMAT,
                RandomUtil.getWeekDayAbbreviated(),                         //WeekDay
                RandomUtil.getMonthAbbreviated(),                           //Month
                RandomUtil.getInt(1, 29),            //Day
                RandomUtil.getIntEnsureFormat(1, 25),       //Hour
                RandomUtil.getIntEnsureFormat(1, 60),       //Minute
                RandomUtil.getIntEnsureFormat(1, 60),       //Second
                RandomUtil.getTimeZoneAbbreviated(),                        //TimeZone
                RandomUtil.getInt(1999, 2030));      //Year
    }
}
