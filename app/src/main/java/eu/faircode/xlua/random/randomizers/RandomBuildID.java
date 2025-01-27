package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomStringGenerator;
import eu.faircode.xlua.utilities.RandomUtil;

public class RandomBuildID implements IRandomizerOld {
    public static final String FORMAT = "%s.%s.%s";

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() {  return "android.build.id"; }

    @Override
    public String getName() {
        return "Build ID";
    }

    @Override
    public String getID() { return "%build_id%"; }

    @Override
    public String generateString() {
        return String.format(FORMAT,
                RandomStringGenerator.generateRandomLetterString(RandomUtil.getInt(4, 8), RandomStringGenerator.UPPER_LETTERS),
                RandomUtil.getInt(10000, 99999),
                RandomUtil.getIntEnsureFormat(1, 999));
    }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return null; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}
