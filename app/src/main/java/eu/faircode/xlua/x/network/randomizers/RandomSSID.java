package eu.faircode.xlua.x.network.randomizers;

import eu.faircode.xlua.utilities.RandomStringGenerator;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomSSID extends RandomElement {
    private static final String[] DIRECT_HP_NAMES = new String[] {
            "ENVY",
            "OfficeJet",
            "DeskJet",
            "DeskJet",
            "Tango",
            "LaserJet",
            "Sprocket",
            "Pagewide"
    };

    private static final String[] DEFAULT_SSID_NAMES = new String[] {
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

    public static RandomSSID create() { return new RandomSSID(); }
    public RandomSSID() {
        super("Network SSID");
        bindSettings("unique.network.ssid", "net.ssid");
    }

    @Override
    public String generateString() {
        String prefix = DEFAULT_SSID_NAMES[RandomGenerator.nextInt(0, DEFAULT_SSID_NAMES.length)];
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
            sb.append(DIRECT_HP_NAMES[RandomGenerator.nextInt(0, DIRECT_HP_NAMES.length)]);
            sb.append(" ");
            sb.append(RandomGenerator.nextInt(10, 9999));
            sb.append(" series");
            return sb.toString();
        }
        else return prefix;
    }
}
