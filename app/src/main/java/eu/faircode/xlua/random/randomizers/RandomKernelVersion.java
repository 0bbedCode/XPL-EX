package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;

public class RandomKernelVersion implements IRandomizerOld {
    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() {  return "android.kernel.version"; }

    @Override
    public String getName() {
        return "Kernel Version";
    }

    @Override
    public String getID() { return "%kernel_version%"; }

    @Override
    public String generateString() { return "SMP PREEMPT " + new RandomDateZero().generateString(); }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return null; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}
