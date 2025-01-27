package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;


import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomMemory implements IRandomizerOld {
    @Override
    public boolean isSetting(String setting) {
        return setting.equalsIgnoreCase(getSettingName()) || setting.equalsIgnoreCase("memory.total");
    }

    @Override
    public String getSettingName() {  return "memory.available"; }

    @Override
    public String getName() {
        return "MEMORY";
    }

    @Override
    public String getID() {
        return "%memory%";
    }

    @Override
    public String generateString() { return Integer.toString(generateInteger()); }

    @Override
    public int generateInteger() { return RandomGenerator.nextInt(1, 999); }

    @Override
    public List<ISpinnerElement> getOptions() { return null; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}
