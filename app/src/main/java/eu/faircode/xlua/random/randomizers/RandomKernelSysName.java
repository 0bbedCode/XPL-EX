package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;


import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomStringGenerator;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomKernelSysName implements IRandomizerOld {
    public static final String[] SYS_NAMES = new String[] { "Linux", "Unix", "BSD", "XNU", "random" };

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() {  return "android.kernel.sys.name"; }

    @Override
    public String getName() {
        return "Kernel Sys Name";
    }

    @Override
    public String getID() { return "%kernel_sys_name%"; }

    @Override
    public String generateString() {
        String nodeName = SYS_NAMES[RandomGenerator.nextInt(0, SYS_NAMES.length)];
        if ("random".equals(nodeName))
            return RandomStringGenerator.generateRandomAlphanumericString(RandomGenerator.nextInt(5, 15), RandomStringGenerator.LOWER_LETTERS);
        return nodeName;
    }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return null; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}
