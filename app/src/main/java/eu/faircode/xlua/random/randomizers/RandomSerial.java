package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomStringGenerator;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomSerial implements IRandomizerOld {

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() {  return "unique.serial.no"; }

    @Override
    public String getName() {
        return "Serial Number";
    }

    @Override
    public String getID() {
        return "%serial%";
    }

    @Override
    public String generateString() { return RandomStringGenerator.generateRandomAlphanumericString(RandomGenerator.nextInt(8, 18), RandomStringGenerator.UPPER_LETTERS); }

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
