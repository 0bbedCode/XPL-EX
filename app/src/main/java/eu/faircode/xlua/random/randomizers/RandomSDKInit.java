package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomUtil;

public class RandomSDKInit implements IRandomizerOld {
    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()) || "android.build.version.sdk".equalsIgnoreCase(setting); }

    @Override
    public String getSettingName() {  return "android.build.version.min.sdk"; }

    @Override
    public String getName() { return "Build SDK"; }

    @Override
    public String getID() { return "%build_sdk%"; }

    @Override
    public String generateString() { return String.valueOf(RandomUtil.getInt(23, 35  )); }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return null; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}
