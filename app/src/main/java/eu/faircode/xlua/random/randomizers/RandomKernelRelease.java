package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomUtil;

public class RandomKernelRelease implements IRandomizerOld {
    private static final String FORMAT = "%s.%s.%s";

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() {  return "android.kernel.release"; }

    @Override
    public String getName() {
        return "Kernel Version";
    }

    @Override
    public String getID() { return "%kernel_version%"; }

    @Override
    public String generateString() {
        return String.format(FORMAT,
                RandomUtil.getInt(1, 5),
                RandomUtil.getInt(10, 99),
                RandomUtil.getInt(100, 999));
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
