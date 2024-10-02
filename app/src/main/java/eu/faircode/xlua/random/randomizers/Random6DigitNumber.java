package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import eu.faircode.xlua.random.IRandomizer;
import eu.faircode.xlua.random.elements.ISpinnerElement;

public class Random6DigitNumber implements IRandomizer  {
    //"gsm.operator.mcc"
    @Override
    public boolean isSetting(String setting) { return getSettingName().equalsIgnoreCase(setting); }

    @Override
    public String getSettingName() { return "gsm.operator.id"; }

    @Override
    public String getName() {
        return "6 Digit Number";
    }

    @Override
    public String getID() {
        return "%six_digit_number%";
    }

    @Override
    public String generateString() { return Integer.toString(ThreadLocalRandom.current().nextInt(100000, 999999)); }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return null; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}
