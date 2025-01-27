package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;


import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.DataNullElement;
import eu.faircode.xlua.random.elements.DataStringElement;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomNetworkType implements IRandomizerOld {
    private final List<ISpinnerElement> dataStates = new ArrayList<>();
    public RandomNetworkType() {
        dataStates.add(DataNullElement.EMPTY_ELEMENT);
        dataStates.add(DataStringElement.create("unknown"));
        dataStates.add(DataStringElement.create("GPRS"));
        dataStates.add(DataStringElement.create("EDGE"));
        dataStates.add(DataStringElement.create("UMTS"));
    }

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() {  return "gsm.network.type"; }

    @Override
    public String getName() {
        return "Network Type 2";
    }

    @Override
    public String getID() {
        return "%net_type_2%";
    }

    @Override
    public String generateString() { return dataStates.get(RandomGenerator.nextInt(1, dataStates.size())).getValue(); }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return this.dataStates; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}
