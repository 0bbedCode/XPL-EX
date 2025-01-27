package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomStringGenerator;

public class RandomMEID implements IRandomizerOld {
    @Override
    public boolean isSetting(String setting) {
        return setting.equalsIgnoreCase(getSettingName()) || setting.equalsIgnoreCase("meid") || setting.equalsIgnoreCase("unique.gsm.meid");
    }

    @Override
    public String getSettingName() {  return "value.meid"; }

    @Override
    public String getName() {
        return "MEID";
    }

    @Override
    public String getID() {
        return "%meid%";
    }

    @Override
    public String generateString() {
        return RandomStringGenerator.generateRandomNumberString(14);
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
