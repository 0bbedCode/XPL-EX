package eu.faircode.xlua.x.xlua.settings;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.ui.core.interfaces.IDiffFace;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.xlua.settings.interfaces.INameInformation;
import eu.faircode.xlua.x.xlua.settings.interfaces.IValueDescriptor;

/**
 * Holds the actual Setting that would be stored in the Database
 * We use its "name" as its UI Component binding Identifier, we were going to use its Nice Name, but plan is for the actual Component Have a Nice Name and below it smaller text its RAW Name
 * Nice name assuming its in Format for Children "Index [X]" then will not be unique to help identify its component
 *
 */
public class SettingHolder extends UiBindingsController implements IValueDescriptor, IDiffFace, IIdentifiableObject {
    public boolean wasCreatedFromContainer = false;

    private String value;
    private String newValue;
    private String description;
    private String id = null;

    @Override
    public String getDescription() { return description; }

    @Override
    public String getValue() { return value; }

    public void setValue(String value) { setValue(value, true); }
    public void setValue(String value, boolean syncNewValue) { setValue(value, null, syncNewValue); }
    public void setValue(String value, String defaultIfNull, boolean syncNewValue) {
        this.value = value == null ? defaultIfNull : value;
        if(syncNewValue)
            this.newValue = this.value;
    }

    @Override
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    @Override
    public boolean isValid() { return hasNameInformation(); }

    public boolean isNotSaved() { return !Str.areEqual(this.value, this.newValue, true, true); }    //Fix
    public boolean hasValue(boolean treatEmptyAsNull) { return treatEmptyAsNull ? TextUtils.isEmpty(value) : value != null; }

    @Override
    public String getSharedId() {
        if(TextUtils.isEmpty(id)) id = "setting:" + getName();
        return id;
    }

    @Override
    public void setId(String id) {

    }

    public String getContainerName() { return nameInformation != null ? nameInformation.getContainerName() : ""; }

    public void setDescription(String description) { if(!TextUtils.isEmpty(description)) this.description = description; }

    protected SettingHolder() {  }
    public SettingHolder(NameInformation nameInformation, String value, String description) {
        super.bindNameInformation(nameInformation);
        this.value = value;
        this.newValue = value;
        this.description = description;
    }


    public void setNameLabelColor(Context context) { setNameLabelColor(context, isNotSaved()); }
    public void setNameLabelColor(Context context, boolean isNotSaved) { setNameLabelColor(context, isNotSaved, hasValue(false)); }

    public void reset(Context context) {
        this.newValue = value;
        ensureUiUpdated(value);
        setNameLabelColor(context);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof String) return ((String)obj).equalsIgnoreCase(getName());
        if(obj instanceof INameInformation) {
            INameInformation info = (INameInformation) obj;
            return this.getName().equalsIgnoreCase(info.getName()) && this.getGroup().equalsIgnoreCase(info.getGroup());
        }

        return false;
    }

    @Override
    public int hashCode() { return getName().hashCode(); }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendLine(Str.toStringOrNull(this.getNameInformation()))
                .appendFieldLine("Value", this.value)
                .appendFieldLine("New Value", this.newValue)
                .appendFieldLine("Description", this.description)
                .appendFieldLine("Was Created From Container", this.wasCreatedFromContainer)
                .toString(true);
    }

    /*Leave*/
    @Override
    public boolean areItemsTheSame(IDiffFace newItem) { return false; }

    @Override
    public boolean areContentsTheSame(IDiffFace newItem) { return false; }
}
