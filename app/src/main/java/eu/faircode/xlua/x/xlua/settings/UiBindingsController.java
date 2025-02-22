package eu.faircode.xlua.x.xlua.settings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;
import eu.faircode.xlua.x.xlua.settings.deprecated.SettingsHelperOld;
import eu.faircode.xlua.x.xlua.settings.interfaces.IValueDescriptor;
import eu.faircode.xlua.x.xlua.settings.interfaces.NameInformationTypeBase;

/**
 * This class will Help us bind UI Components to the Setting so we can Control the UI from Settings Level Easier
 * Since UIs can "recycle" Components to other Elements / Objects we have Functions here to Ensure the UI Component that is being Modified is Linked to the target object
 * We compare the Target Label Text that is being Shown to the UI with the Given Param / Object Name Identifier
 * If both are the Same then we can apply the Changes to the UI Component assuming it links to the Target Object else Modify the Target Object but no changes to the UI Component
 * If no Changes to the UI Component are Made but only to the Target Object then when the UI component is in VIEW again it will still reflect changes assuming you know how to Code
 * When the Item View of the Object is Created it Binds its Binding here so it can leave a Old Object that was bind ed with the UI components not updated
 *
 * A Nice Name is a Name that does not Contain Periods, or sometimes Numbers for indication of Index or any special / weird Char
 * Usually made up of just Alphabetical Letters A-Z each Word being Capitalized and periods / special chars turned into a Single Space
 * Example: Cool Setting of Some Sort
 *
 * While on the Other Hand a Regular (non) Nice Name will just be the "raw" Name of the Object most likely the Name that will be Reflected in the Database, Database Entry Name
 * Typically wont be Displayed in the UI and if so will be displayed in Small Text or in a Non Important part of the Component to indicate "if it looks confusing ignore it just here for geeks"
 * Example: cool.setting.of.some.sort.1
 *
 * EDIT! I don't think we need it for the Settings Container ? there is no input Text and the Label is Static controlled by UI ?
 * So Scratch SettingsContainer usage
 *
 * Types
 * --------SettingsContainer--------
 *  -> Setting Name Nice    We will bind to this as a Indicator to match UI with Object, note its a Nice Name of the Settings its Containing used for Settings that can exist more than once
 *  -> Check Box            Indicate Container Control
 *
 *  --------Setting--------
 *  -> Setting Name         Name of the Actual Setting that will be Reflected in Database, we will bind the UI Component to this Value to help Identify
 *  -> Setting Name Nice    Setting Name that will Appear within the UI but this will not be used as the Container will Display its "nice" name
 *
 *  We store Extra Field within this Class Such as "TextInputExitText" and "TextWatcher" so it can Reflect the Requested Changes if identification of Object and UI component matched
 *  So we Need:
 *
 *  TextView        bindingLabel;       //We will use this to help identify / match the UI component with Object Requesting Update of Changes
 *  CheckBox        bindingCheckbox;    //Help ReSet Controls if needed or Identify State ?
 *
 *                                      //Optional Bindings if the UI has a Input Text to update
 *  TextInputEditText bindingInputText;
 *  TextWatcher       bindingInputTextWatcher;
 *
 *
 *  hmm I see "abstract = no need to implement all the functions in interface the caller has too, interesting..."
 */
public abstract class UiBindingsController extends NameInformationTypeBase implements IValueDescriptor {

    protected boolean isChecked = false;

    private TextView nameLabel;
    private TextView niceNameLabel;

    private EditText editText;
    private TextInputEditText inputEditText;
    private TextWatcher textWatcher;

    public TextView getIdentifyingLabel() { return nameLabel != null ? nameLabel : niceNameLabel; }
    public String getIdentification() { return nameLabel != null ? getName() : getNameNice(); }

    public boolean labelMatchesIdentification() { return labelMatchesIdentification(getIdentification()); }
    public boolean labelMatchesIdentification(String objectIdentification) { return SettingsHelper.matchesLabel(objectIdentification, getIdentifyingLabel()); }


    public boolean isEnabled() { return isChecked; }



    public void ensureUiUpdated(String inputText) {
        CoreUiUtils.setEditTextText(this.editText, this.textWatcher, inputText, false);
    }

    public void setNameLabelColor(Context context, boolean isNotSaved, boolean hasValue) {
        if(context != null && nameLabel != null) {
            int c = isNotSaved ?  R.attr.colorUnsavedSetting : hasValue ? R.attr.colorAccent : R.attr.colorTextOne;
            int color = XUtil.resolveColor(context, c);
            CoreUiUtils.setTextColor(nameLabel, color, false);
        }
    }

    public void setBindings(TextView compareLabel, EditText inputEditText, TextWatcher textWatcher) {
        this.nameLabel = compareLabel;
        this.editText = inputEditText;
        this.textWatcher = textWatcher;
    }
}
