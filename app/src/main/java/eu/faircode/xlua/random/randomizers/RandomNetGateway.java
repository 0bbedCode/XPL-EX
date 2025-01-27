package eu.faircode.xlua.random.randomizers;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;


import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;


public class RandomNetGateway implements IRandomizerOld {
    private static final String IP_FORMAT = "%s.%s.%s.%s";
    private static final List<String> ipList = Arrays.asList("127.0.0.1", "10.0.0.1", "192.168.1.0", "random");

    @Override
    public boolean isSetting(String setting) { return getSettingName().equalsIgnoreCase(setting); }

    @Override
    public String getSettingName() {  return "network.gateway"; }

    @Override
    public String getName() {
        return "Gateway Address (wlan0) IPV4";
    }

    @Override
    public String getID() {
        return "%ipaddress_gateway%";
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String generateString() {
        String ip = ipList.get(RandomGenerator.nextInt(0, ipList.size()));
        if(ip.equals("random")) {
            return String.format(IP_FORMAT,
                    RandomGenerator.nextInt(1, 192),
                    RandomGenerator.nextInt(0, 168),
                    RandomGenerator.nextInt(0, 9),
                    RandomGenerator.nextInt(0, 9));
        } return ip;
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
