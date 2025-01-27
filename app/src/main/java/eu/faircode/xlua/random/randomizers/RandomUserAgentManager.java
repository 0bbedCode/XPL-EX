package eu.faircode.xlua.random.randomizers;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.api.useragent.MockUserAgent;
import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.IRandomizerManager;
import eu.faircode.xlua.random.elements.DataNullElement;
import eu.faircode.xlua.random.elements.DataUserAgentElement;
import eu.faircode.xlua.random.elements.IManagedSpinnerElement;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

//Make a linking system
//some settings have links changing the main link will change its children
//hmm lets do it



public class RandomUserAgentManager implements IRandomizerOld, IRandomizerManager {
    private IManagedSpinnerElement selectedElement;
    private final List<ISpinnerElement> dataStates = Arrays.asList(
            (ISpinnerElement) DataUserAgentElement.create(DataNullElement.EMPTY_ELEMENT.getName(), this),
            (ISpinnerElement) DataUserAgentElement.create(MockUserAgent.GET_UA_ALL, this),
            (ISpinnerElement) DataUserAgentElement.create(MockUserAgent.GET_UA_ANDROID, this),
            (ISpinnerElement) DataUserAgentElement.create(MockUserAgent.GET_UA_WINDOWS, this),
            (ISpinnerElement) DataUserAgentElement.create(MockUserAgent.GET_UA_IPHONE, this),
            (ISpinnerElement) DataUserAgentElement.create(MockUserAgent.GET_UA_LINUX, this),
            (ISpinnerElement) DataUserAgentElement.create(MockUserAgent.GET_UA_MACINTOSH, this));

    public RandomUserAgentManager() { selectedElement = (IManagedSpinnerElement) dataStates.get(0); }

    @Override
    public boolean hasNaNSelected() { return selectedElement == null || selectedElement.isNaN(); }

    @Override
    public void setSelectedElement(IManagedSpinnerElement el) { if(el != null) this.selectedElement = el; }

    @Override
    public IManagedSpinnerElement getSelectedElement() { return this.selectedElement; }

    @Override
    public String generateString(Context context) { return hasNaNSelected() ? null : selectedElement.generateString(context); }

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() {  return "user.agent"; }

    @Override
    public String getName() {
        return "User Agent";
    }

    @Override
    public String getID() {
        return "%user_agent%";
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
