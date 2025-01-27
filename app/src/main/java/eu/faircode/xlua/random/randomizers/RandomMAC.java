package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomStringGenerator;

public class RandomMAC implements IRandomizerOld {
    @Override
    public boolean isSetting(String setting) {
        return getSettingName().equalsIgnoreCase(setting) ||
                "bluetooth.id".equalsIgnoreCase(setting) ||
                "unique.bluetooth.address".equalsIgnoreCase(setting) ||
                "net.bssid".equalsIgnoreCase(setting) ||
                "unique.network.mac.address".equalsIgnoreCase(setting) ||
                "unique.network.bssid".equalsIgnoreCase(setting) ||
                "unique.network.ethernet.mac.address".equalsIgnoreCase(setting);
    }

    @Override
    public String getSettingName() { return "net.mac"; }

    @Override
    public String getName() { return "MAC Format"; }

    @Override
    public String getID() { return "%net_mac%"; }

    @Override
    public String generateString() {
        String rawString = RandomStringGenerator.generateRandomHexString(12).toUpperCase();
        StringBuilder sb = new StringBuilder();
        int rawLen = rawString.length();
        for(int i = 0; i < rawString.length(); i += 2) {
            sb.append(rawString, i, Math.min(i + 2, rawLen));
            if(i + 2 < rawLen)
                sb.append(":");
        }

        return sb.toString();
    }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return null; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}
