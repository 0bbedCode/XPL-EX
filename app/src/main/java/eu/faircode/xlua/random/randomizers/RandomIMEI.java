package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomStringGenerator;

public class RandomIMEI implements IRandomizerOld {
    @Override
    public boolean isSetting(String setting) {
        return setting.equalsIgnoreCase(getSettingName()) || setting.equalsIgnoreCase("imei") || setting.equalsIgnoreCase("unique.gsm.imei");
    }

    @Override
    public String getSettingName() {  return "value.imei"; }

    @Override
    public String getName() {
        return "IMEI";
    }

    @Override
    public String getID() {
        return "%imei%";
    }

    @Override
    public String generateString() {
        return RandomStringGenerator.generateRandomNumberString(15);
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
