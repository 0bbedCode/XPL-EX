package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;


import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomStringGenerator;

public class RandomFirebaseId implements IRandomizerOld {
    @Override
    public boolean isSetting(String setting) { return getSettingName().equalsIgnoreCase(setting); }

    @Override
    public String getSettingName() {  return "analytics.firebase.instance.id"; }

    @Override
    public String getName() {
        return "Generate Random Firebase ID";
    }

    @Override
    public String getID() {
        return "%firebase_id%";
    }

    @Override
    public String generateString() {
        return (RandomStringGenerator.generateRandomHexString(8) +
                "-" +
                RandomStringGenerator.generateRandomHexString(4) +
                "-" +
                RandomStringGenerator.generateRandomNumberString(4) +
                "-" +
                RandomStringGenerator.generateRandomNumberString(4)+
                "-" +
                RandomStringGenerator.generateRandomHexString(12)).toLowerCase();
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
