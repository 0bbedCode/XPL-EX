package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;


import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomDNS implements IRandomizerOld {
    private static final String DNS_FORMAT = "%s.%s.%s.%s";
    private static final List<String> dnsList = Arrays.asList("1.1.1.1", "8.8.8.8", "6.6.6.6", "random");

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() {  return "network.dns"; }

    @Override
    public String getName() {
        return "DNS";
    }

    @Override
    public String getID() {
        return "%dns%";
    }

    @Override
    public String generateString() {
        String dns = dnsList.get(RandomGenerator.nextInt(0, dnsList.size()));
        if(dns.equals("random")) {
            return String.format(DNS_FORMAT,
                    RandomGenerator.nextInt(1, 9),
                    RandomGenerator.nextInt(1, 9),
                    RandomGenerator.nextInt(1, 9),
                    RandomGenerator.nextInt(1, 9));
        } return dns;
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
