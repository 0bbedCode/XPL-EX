package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomStringGenerator;

public class RandomNetD implements IRandomizerOld {
    //unique.netd.secret.key
    //90f8:f587:2bb7:db9b:e5:306a:67d7:a155
    public static final String FORMAT = "%s:%s:%s:%s:%s:%s:%s:%s";

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() { return "unique.netd.secret.key"; }

    @Override
    public String getName() { return "NETD Secret Key"; }

    @Override
    public String getID() { return "%netd_secret_key%"; }

    @Override
    public String generateString() {
        return String.format(FORMAT,
                RandomStringGenerator.generateRandomHexString(4 ),
                RandomStringGenerator.generateRandomHexString(4 ),
                RandomStringGenerator.generateRandomHexString(4 ),
                RandomStringGenerator.generateRandomHexString(4 ),
                RandomStringGenerator.generateRandomHexString(2),
                RandomStringGenerator.generateRandomHexString(4 ),
                RandomStringGenerator.generateRandomHexString(4 ),
                RandomStringGenerator.generateRandomHexString(4 ));
    }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return null; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}
