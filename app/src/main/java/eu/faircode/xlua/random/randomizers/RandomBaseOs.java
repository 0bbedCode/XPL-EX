package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;


import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomBaseOs  implements IRandomizerOld {
    //android.build.base.os
    private static final String[] DEFAULT_MANUFACTURERS = new String[] {
            "Linux",
            "Unix",
            "Android",
            "ios",
            "Debian"
    };

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() {  return "android.build.base.os"; }

    @Override
    public String getName() {
        return "Base OS";
    }

    @Override
    public String getID() {
        return "%base_os%";
    }

    @Override
    public String generateString() { return DEFAULT_MANUFACTURERS[RandomGenerator.nextInt(0, DEFAULT_MANUFACTURERS.length)]; }

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
