package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.DataNullElement;
import eu.faircode.xlua.random.elements.DataNameValueElement;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomSIMType implements IRandomizerOld {
    private final List<ISpinnerElement> dataStates = new ArrayList<>();
    public RandomSIMType() {
        dataStates.add(DataNullElement.EMPTY_ELEMENT);
        dataStates.add(DataNameValueElement.create("LOCAL_SIM", 0));
        dataStates.add(DataNameValueElement.create("REMOTE_SIM", 1));
    }

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() {  return "gsm.sim.type"; }

    @Override
    public String getName() {
        return "SIM Type";
    }

    @Override
    public String getID() {
        return "%sim_type%";
    }

    @Override
    public String generateString() {
        ISpinnerElement el = dataStates.get(RandomGenerator.nextInt(1, dataStates.size()));
        return el.getValue();
    }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return this.dataStates; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}
