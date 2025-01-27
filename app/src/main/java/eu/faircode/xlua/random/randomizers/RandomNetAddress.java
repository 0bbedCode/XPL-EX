package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.x.network.NetRandom;
import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;

public class RandomNetAddress implements IRandomizerOld {
    @Override
    public boolean isSetting(String setting) { return getSettingName().equalsIgnoreCase(setting); }

    @Override
    public String getSettingName() {  return "network.host.address"; }

    @Override
    public String getName() { return "Host Address (IPV4)"; }

    @Override
    public String getID() {
        return "%ipaddress%";
    }

    @Override
    public String generateString() { return NetRandom.generateRandomPrivateIPv4(); }

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
