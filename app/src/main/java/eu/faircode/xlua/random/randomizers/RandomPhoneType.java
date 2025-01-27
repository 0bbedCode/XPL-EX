package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.DataNameValueElement;
import eu.faircode.xlua.random.elements.DataNullElement;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomPhoneType implements IRandomizerOld {
    private final List<ISpinnerElement> dataStates = new ArrayList<>();
    public RandomPhoneType() {
        dataStates.add(DataNullElement.EMPTY_ELEMENT);
        dataStates.add(DataNameValueElement.create("NONE", 0));
        dataStates.add(DataNameValueElement.create("GSM", 1));
        dataStates.add(DataNameValueElement.create("CDMA", 2));
        dataStates.add(DataNameValueElement.create("SIP", 3));
    }

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() {  return "gsm.phone.type"; }

    @Override
    public String getName() {
        return "Phone Type";
    }

    @Override
    public String getID() {
        return "%phone_type%";
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
