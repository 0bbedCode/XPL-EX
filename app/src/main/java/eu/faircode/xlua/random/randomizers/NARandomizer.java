package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;

//msg_na_randomizer

public class NARandomizer implements IRandomizerOld {
    public static boolean isNA(IRandomizerOld r) { return r.getID().equalsIgnoreCase("%n_a%"); }

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() { return "N/A"; }

    @Override
    public String getName() {
        return "N/A (No Randomizer)";
    }

    @Override
    public String getID() {
        return "%n_a%";
    }

    @Override
    public String generateString() { return "This is not a Random Value, Google the best value you think that should fit. Sorry I didn't code 1000 Randomizers in one week will try harder"; }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return null; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}
