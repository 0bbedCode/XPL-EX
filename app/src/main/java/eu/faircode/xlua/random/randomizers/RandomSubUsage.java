package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.DataNameValueElement;
import eu.faircode.xlua.random.elements.DataNullElement;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomSubUsage implements IRandomizerOld {
    private final List<ISpinnerElement> dataStates = new ArrayList<>();
    public RandomSubUsage() {
        dataStates.add(DataNullElement.EMPTY_ELEMENT);
        dataStates.add(DataNameValueElement.create("UNKNOWN", -1));
        dataStates.add(DataNameValueElement.create("DEFAULT", 0));
        dataStates.add(DataNameValueElement.create("VOICE_CENTRIC", 1));
        dataStates.add(DataNameValueElement.create("DATA_CENTRIC", 2));
    }

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() {  return "gsm.subscription.usage.setting"; }

    @Override
    public String getName() {
        return "SUB Usage";
    }

    @Override
    public String getID() {
        return "%gsm_sim_usage%";
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
