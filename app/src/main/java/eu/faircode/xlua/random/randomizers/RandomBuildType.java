package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;


import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomBuildType implements IRandomizerOld {
    private static final String[] DEFAULT_BUILD_TYPES = new String[] {
            "user",
            "userdebug",
            "eng"
    };

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() {  return "android.build.type"; }

    @Override
    public String getName() {
        return "Build Type";
    }

    @Override
    public String getID() {
        return "%build_type%";
    }

    @Override
    public String generateString() { return DEFAULT_BUILD_TYPES[RandomGenerator.nextInt(0, DEFAULT_BUILD_TYPES.length)]; }

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
