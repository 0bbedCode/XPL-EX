package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomStringGenerator;

public class RandomICCID implements IRandomizerOld {
    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() {  return "unique.gsm.icc.id"; }

    @Override
    public String getName() {
        return "ICC ID";
    }

    @Override
    public String getID() {
        return "%icc_id%";
    }

    @Override
    public String generateString() { return RandomStringGenerator.generateRandomNumberString(18) + RandomStringGenerator.generateRandomLetterString(1, "ABCDEF"); }

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
