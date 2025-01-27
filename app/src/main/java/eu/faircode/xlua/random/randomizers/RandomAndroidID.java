package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomStringGenerator;

public class RandomAndroidID implements IRandomizerOld {
    @Override
    public boolean isSetting(String setting) {
        return setting.equalsIgnoreCase(getSettingName()) || setting.equalsIgnoreCase("unique.android.id");
    }

    @Override
    public String getSettingName() { return "value.android_id"; }

    @Override
    public String getName() {
        return "Android ID";
    }

    @Override
    public String getID() {
        return "%android_id%";
    }

    @Override
    public String generateString() { return RandomStringGenerator.generateRandomHexString(16).toLowerCase(); }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return null; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}
