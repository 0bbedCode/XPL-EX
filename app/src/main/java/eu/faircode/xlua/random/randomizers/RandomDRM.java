package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Random;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;

public class RandomDRM implements IRandomizerOld {
    @Override
    public boolean isSetting(String setting) {
        return setting.equalsIgnoreCase(getSettingName()) || setting.equalsIgnoreCase("drm") || setting.equalsIgnoreCase("unique.drm.id");
    }

    @Override
    public String getSettingName() {
        return "drm.id";
    }

    @Override
    public String getName() {
        return "DRM ID";
    }

    @Override
    public String getID() {
        return "%drm_id%";
    }

    @Override
    public String generateString() {
        // Generate a random alphanumeric string of length 32 (since we are working with 16 bytes)
        int length = 64; //or 32
        String characters = "0123456789ABCDEF";
        StringBuilder result = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) result.append(characters.charAt(random.nextInt(characters.length())));
        return result.toString();
    }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return null; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}
