package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.DataNullElement;
import eu.faircode.xlua.random.elements.DataNumberElement;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomSIMCount implements IRandomizerOld {
    private final List<ISpinnerElement> dataStates = new ArrayList<>();
    public RandomSIMCount() {
        dataStates.add(DataNullElement.EMPTY_ELEMENT);
        for(int i = 0; i < 10; i++) dataStates.add(DataNumberElement.create(i));
    }

    @Override
    public boolean isSetting(String setting) { return
            setting.equalsIgnoreCase(getSettingName()) ||
                    setting.equalsIgnoreCase("gsm.index.sim.slot") || setting.equalsIgnoreCase("gsm.index.sim.port"); }

    @Override
    public String getSettingName() {  return "gsm.sim.count"; }

    @Override
    public String getName() {
        return "SIM Count / Index";
    }

    @Override
    public String getID() {
        return "%sim_count_index%";
    }

    @Override
    public String generateString() { return dataStates.get(RandomGenerator.nextInt(1, dataStates.size())).getValue(); }

    @Override
    public int generateInteger() { return Integer.parseInt(dataStates.get(RandomGenerator.nextInt(1, dataStates.size())).getValue()); }

    @Override
    public List<ISpinnerElement> getOptions() { return this.dataStates; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}
