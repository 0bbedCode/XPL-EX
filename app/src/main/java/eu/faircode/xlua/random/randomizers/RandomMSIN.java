package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.random.IRandomizer;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomStringGenerator;

public class RandomMSIN implements IRandomizer {
    @Override
    public boolean isSetting(String setting) {
        return  setting.equalsIgnoreCase("gsm.operator.msin") || setting.equalsIgnoreCase(getSettingName());
    }

    @Override
    public String getSettingName() {  return "unique.gsm.operator.msin"; }

    @Override
    public String getName() {
        return "MSIN";
    }

    @Override
    public String getID() {
        return "%msin%";
    }

    @Override
    public String generateString() {
        return RandomStringGenerator.generateRandomNumberString(10);
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
