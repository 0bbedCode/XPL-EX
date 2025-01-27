package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomUtil;

public class RandomDateZero implements IRandomizerOld {
    private static final List<String> SETTINGS = Arrays.asList("android.build.date");

    public static final String FORMAT = "%s %s %s %s:%s:%s %s %s";

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()) || SETTINGS.contains(setting); }

    @Override
    public String getSettingName() {  return "random.date.zero"; }

    @Override
    public String getName() {
        return "Random Date (W MT DY HR:MIN:SC TZ YR)";
    }

    @Override
    public String getID() {
        return "%random_date_zero%";
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

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return null; }

    @NonNull
    @Override
    public String toString() {
        return getName();
    }
}
