package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomStringGenerator;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomSSID implements IRandomizerOld {
    private static final String[] direct_hp_names = new String[] {
            "ENVY",
            "OfficeJet",
            "DeskJet",
            "DeskJet",
            "Tango",
            "LaserJet",
            "Sprocket",
            "Pagewide"
    };

    private static final String[] default_ssid_names = new String[] {
            "iPhone-",
            "dlink-",
            "NETGEAR-",
            "Tp-link-",
            "AndroidAP-",
            "Home-",
            "AndroidShare_",
            "SpyNetwork",
            "AndroidWifi",
            "FBI",
            "NSA",
            "CIA",
            "FCC",
            "FBI Surveillance Van",
            "NSA_Surveillance",
            "xfinitywifi",
            "XFINITY",
            "McDonalds Free WiFi",
            "Starbucks WiFi",
            "iPhone",
            "NETGEAR",
            "DIRECT",
            "random"//add guest wifis
    };

    @Override
    public boolean isSetting(String setting) {
        return setting.equalsIgnoreCase(getSettingName()) || setting.equalsIgnoreCase("unique.network.ssid");
    }

    @Override
    public String getSettingName() {  return "net.ssid"; }

    @Override
    public String getName() {
        return "SSID";
    }

    @Override
    public String getID() {
        return "%net_ssid%";
    }

    @Override
    public String generateString() {
        String prefix = default_ssid_names[RandomGenerator.nextInt(0, default_ssid_names.length)];
        if(prefix.equals("random"))
            return RandomStringGenerator.generateRandomAlphanumericString(RandomGenerator.nextInt(8, 35));
        else if(prefix.endsWith("-") || prefix.endsWith("_"))
            return prefix + RandomStringGenerator.generateRandomAlphanumericString(RandomGenerator.nextInt(6, 15), RandomStringGenerator.UPPER_LETTERS);
        else if(prefix.equals("iPhone"))
            return prefix + RandomGenerator.nextInt(6, 16);
        else if(prefix.equals("NETGEAR"))
            return prefix + RandomGenerator.nextInt(10, 1000);
        else if(prefix.equals("DIRECT")) {
            StringBuilder sb = new StringBuilder(prefix);
            sb.append("-");
            sb.append(RandomGenerator.nextInt(10, 1000));
            sb.append("-HP ");
            sb.append(direct_hp_names[RandomGenerator.nextInt(0, direct_hp_names.length)]);
            sb.append(" ");
            sb.append(RandomGenerator.nextInt(10, 9999));
            sb.append(" series");
            return sb.toString();
        }
        else return prefix;
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
