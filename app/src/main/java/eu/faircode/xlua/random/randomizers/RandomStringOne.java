package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomStringGenerator;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomStringOne implements IRandomizerOld {
    private static final List<String> SETTINGS = Arrays.asList("android.build.incremental", "android.build.host", "android.build.fingerprint", "android.build.codename", "android.build.description", "android.build.display.id", "android.build.flavor");

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()) || SETTINGS.contains(setting); }

    @Override
    public String getSettingName() {  return "random.string.one"; }

    @Override
    public String getName() {
        return "Random String (1)";
    }

    @Override
    public String getID() {
        return "%random_string_one%";
    }

    @Override
    public String generateString() { return RandomStringGenerator.generateRandomAlphanumericString(RandomGenerator.nextInt(6, 25));
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
