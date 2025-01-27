package eu.faircode.xlua.x.xlua.settings.random.randomizers.unique;

import eu.faircode.xlua.utilities.RandomStringGenerator;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomNetSSID extends RandomElement {

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


    public RandomNetSSID() {
        super("Network SSID Name");
        putSettings(RandomizersCache.SETTING_UNIQUE_NET_SSID);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushSpecial(context.stack.pop(), getRandomSSID());
    }

    public static String getRandomSSID() {
        String prefix = default_ssid_names[RandomGenerator.nextInt(0, default_ssid_names.length)];
        if(prefix.equals("random"))
            return RandomGenerator.nextString(8, 35);
        else if(prefix.endsWith("-") || prefix.endsWith("_"))
            return prefix + RandomGenerator.nextString(6, 15).toUpperCase();
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
        else
            return prefix;
    }
}