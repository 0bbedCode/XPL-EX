package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomStringGenerator;

public class RandomSimSerial implements IRandomizerOld {
    @Override
    public boolean isSetting(String setting) {
        return setting.equalsIgnoreCase(getSettingName()) || setting.equalsIgnoreCase("unique.gsm.sim.serial");
    }

    @Override
    public String getSettingName() {  return "phone.simserial"; }

    @Override
    public String getName() {
        return "SIM Serial";
    }

    @Override
    public String getID() {
        return "%sim_serial%";
    }

    @Override
    public String generateString() {
        return RandomStringGenerator.generateRandomNumberString(20);
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
