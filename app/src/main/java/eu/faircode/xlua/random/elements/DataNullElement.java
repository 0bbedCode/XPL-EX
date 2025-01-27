package eu.faircode.xlua.random.elements;

import androidx.annotation.NonNull;

import java.util.List;

public class DataNullElement implements ISpinnerElement  {
    public static final ISpinnerElement EMPTY_ELEMENT = new DataNullElement();
    private String displayName = "Select Option";
    private String value = "Select Option";

    public DataNullElement() { }

    @Override
    public String getName() { return this.displayName; }

    @Override
    public String getValue() { return this.value; }

    @Override
    public boolean isSetting(String settingName) { return false; }

    @Override
    public String getSettingName() { return Integer.toString(this.displayName.hashCode()); }

    @Override
    public String getID() { return Integer.toString(this.value.hashCode()); }

    @Override
    public String generateString() { return this.value; }

    @Override
    public int generateInteger() { return this.value.hashCode(); }

    @Override
    public List<ISpinnerElement> getOptions() {
        return null;
    }

    @NonNull
    @Override
    public String toString() { return this.displayName; }
}
