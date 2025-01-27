package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomUtil;

public class RandomDateTwo implements IRandomizerOld {
    public static final String FORMAT = "%s%s%s";

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() {  return "android.build.date.two"; }

    @Override
    public String getName() {
        return "Random Date Two (YYYYMMDD)";
    }

    @Override
    public String getID() { return "%random_date_two%"; }

    @Override
    public String generateString() {
        return String.format(FORMAT,
                RandomUtil.getInt(1999, 2030),
                RandomUtil.getMonthNumberFormatted(),
                RandomUtil.getIntEnsureFormat(1, 30));
    }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return null; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}
