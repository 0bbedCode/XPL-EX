package eu.faircode.xlua.api.settings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.xstandard.interfaces.IJsonSerial;
import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.IRandomizerManager;
//import eu.faircode.xlua.x.random.ILinkParent;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.SettingUtil;
import eu.faircode.xlua.utilities.StringUtil;

public class LuaSettingExtended extends LuaSettingDefault implements IJsonSerial, Parcelable {
    private static final String TAG = "XLua.LuaSetting";

    public static LuaSettingExtended create() { return new LuaSettingExtended(); }
    public static LuaSettingExtended create(LuaSettingPacket packet) { return new LuaSettingExtended(packet); }
    public static LuaSettingExtended create(LuaSetting setting) { return new LuaSettingExtended(setting); }
    public static LuaSettingExtended create(LuaSetting setting, String description) { return new LuaSettingExtended(setting, description); }
    public static LuaSettingExtended create(LuaSetting setting, String description, String defaultValue) { return new LuaSettingExtended(setting, description, defaultValue); }
    public static LuaSettingExtended create(LuaSettingDefault defaultSetting) { return new LuaSettingExtended(defaultSetting); }
    public static LuaSettingExtended create(LuaSettingDefault defaultSetting, Integer user, String category) { return new LuaSettingExtended(defaultSetting, user, category); }
    public static LuaSettingExtended create(LuaSettingDefault defaultSetting, Integer user, String category, String value) { return new LuaSettingExtended(defaultSetting, user, category, value); }

    public static LuaSettingExtended create(Integer user, String category, String name, String value) { return new LuaSettingExtended(user, category, name, value, null, null, null); }
    public static LuaSettingExtended create(Integer user, String category, String name, String value, String description) { return new LuaSettingExtended(user, category, name, value, description, null, null); }
    public static LuaSettingExtended create(Integer user, String category, String name, String value, String description, String defaultValue) { return new LuaSettingExtended(user, category, name, value, description, defaultValue, null); }
    public static LuaSettingExtended create(Integer user, String category, String name, String value, String description, String defaultValue, Boolean isEnabled) { return new LuaSettingExtended(user, category, name, value, description, defaultValue, isEnabled); }


    public LuaSettingExtended(LuaSettingPacket packet) { this(packet.getUser(), packet.getCategory(), packet.getName(), packet.getValue(), packet.getDescription(), packet.getDefaultValue(), packet.isEnabled()); }

    public LuaSettingExtended(LuaSetting setting) { this(setting.getUser(), setting.getCategory(), setting.getName(), setting.getValue(), null, null, null); }
    public LuaSettingExtended(LuaSetting setting, String description) { this(setting.getUser(), setting.getCategory(), setting.getName(), setting.getValue(), description, null, null); }
    public LuaSettingExtended(LuaSetting setting, String description, String defaultValue) { this(setting.getUser(), setting.getCategory(), setting.getName(), setting.getValue(), description, defaultValue, null); }

    public LuaSettingExtended(LuaSettingDefault defaultSetting) { this(defaultSetting.getUser(), defaultSetting.getCategory(), defaultSetting.getName(), defaultSetting.getValue(), defaultSetting.getDescription(), defaultSetting.getDefaultValue(true), null); }
    public LuaSettingExtended(LuaSettingDefault defaultSetting, Integer user, String category) { this(user, category, defaultSetting.getName(), null, defaultSetting.getDescription(), defaultSetting.getDefaultValue(true), null); }
    public LuaSettingExtended(LuaSettingDefault defaultSetting, Integer user, String category, String value) { this(user, category, defaultSetting.getName(), value, defaultSetting.getDescription(), defaultSetting.getDefaultValue(true), null); }

    protected Boolean enabled = false;
    protected String modifiedValue;
    protected String groupId;
    protected Boolean isBusy = false;

    protected IRandomizerOld randomizer;
    protected TextInputEditText inputText;
    protected TextWatcher textWatcher;
    protected TextView tvName;

    public List<LuaSettingExtended> settings;

    public LuaSettingExtended() { setUseUserIdentity(true); }
    public LuaSettingExtended(Parcel in) { this(); fromParcel(in); }

    public LuaSettingExtended(Integer user, String category, String name, String value) { this(user, category, name, value, null, null, null); }
    public LuaSettingExtended(Integer user, String category, String name, String value, String description) { this(user, category, name, value, description, null, null); }
    public LuaSettingExtended(Integer user, String category, String name, String value, String description, String defaultValue) { this(user, category, name, value, description, defaultValue, null); }
    public LuaSettingExtended(Integer user, String category, String name, String value, String description, String defaultValue, Boolean isEnabled) {
        this();
        setUser(user);
        setCategory(category);
        setName(name);
        setValue(value);
        setDescription(description);
        setDefaultValue(defaultValue);
        setIsEnabled(isEnabled);
    }

    public Boolean isBusy() { return this.isBusy; }
    public void setIsBusy(Boolean isBusy) { if(isBusy != null) this.isBusy = isBusy; }

    public Boolean isEnabled() { return this.enabled; }
    public LuaSettingExtended setIsEnabled(Boolean enabled) {  this.enabled = enabled; return this; }
    public boolean isBuiltIntSetting() { return SettingUtil.isBuiltInSetting(this.getName()); }

    public String getGroupId() {
        if(this.groupId == null) {
            String name = this.getName();
            if(SettingUtil.isBuiltInSetting(name)) return "M66Bs Built In";
            if(name.contains(".")) this.groupId = StringUtil.capitalizeFirstLetter(name.split("\\.")[0]);
            else this.groupId = name;
        }

        return this.groupId;
    }

    public void updateValue() { this.value = this.modifiedValue; }
    public void updateValue(boolean updateTextBoxBinding) {
        updateValue();
        if(updateTextBoxBinding && this.inputText != null) setInputText(this.value);
    }



    public TextInputEditText getInputTextBox() { return this.inputText; }

    public boolean isSameNameAsDisplayCache() {
        if(this.tvName != null) {
            String t = this.tvName.getText().toString();
            if(t.contains(" ")) {
                String tc = t.replace(" ", ".");
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Text View Name=" + tc + " Setting Name=" + name);
                return tc.equalsIgnoreCase(name);
            } else {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Text View Name=" + t + " Setting Name=" + name);
                return t.equalsIgnoreCase(name);
            }
        }

        return true;
    }

    public String getModifiedValue() { return this.modifiedValue; }
    public LuaSetting setModifiedValueToNull() { this.modifiedValue = null; return this; }
    public void setModifiedValue(String modifiedValue) { setModifiedValue(modifiedValue, false); }
    public void setModifiedValue(String modifiedValue, boolean setTextInput) {
        this.modifiedValue = modifiedValue;
        if(this.inputText != null && setTextInput)
            setInputText(modifiedValue);
    }

    public void resetModified() { resetModified(false);  }
    public void resetModified(boolean setInput) {
        this.modifiedValue = this.value;
        if(setInput && this.inputText != null) {
            try {
                this.inputText.setText(this.value);
            }catch (Exception e) {
                Log.e(TAG, "Failed to input / set input text e=" + e);
            }
        }
    }

    public boolean isModified() {
        if(this.modifiedValue == null) return this.value != null;
        if(TextUtils.isEmpty(this.modifiedValue)) return this.value == null || !TextUtils.isEmpty(this.value);
        if(this.value == null) return true;
        return !this.value.equals(this.modifiedValue);
    }

    public void randomizeValue() { randomizeValue(null); }
    public void randomizeValue(Context context) {
        if(this.randomizer != null) {
            /*if(this.randomizer instanceof ILinkParent) {
                ILinkParent parent = (ILinkParent) this.randomizer;
                parent.randomizeSettings(settings);
            } else {
                if(context != null && this.randomizer instanceof IRandomizerManager) {
                    IRandomizerManager manager = (IRandomizerManager) this.randomizer;
                    if(!manager.hasNaNSelected()) {
                        String v = manager.generateString(context);
                        setModifiedValue(v, true);
                    }
                }else {
                    String v = this.randomizer.generateString();
                    setModifiedValue(v, true);
                }
            }*/

            if(context != null && this.randomizer instanceof IRandomizerManager) {
                IRandomizerManager manager = (IRandomizerManager) this.randomizer;
                if(!manager.hasNaNSelected()) {
                    String v = manager.generateString(context);
                    setModifiedValue(v, true);
                }
            }else {
                String v = this.randomizer.generateString();
                setModifiedValue(v, true);
            }
        }
    }

    public IRandomizerOld getRandomizer() { return this.randomizer; }
    public void unBindRandomizer() { this.randomizer = null; }
    public void bindRandomizer(IRandomizerOld randomizer) {
        if(this.randomizer != null) {
            List<ISpinnerElement> elements = this.randomizer.getOptions();
            if(this.randomizer.isSetting(getName()) && (elements != null && !elements.isEmpty())) {
                //locked in cant change :P
                return;
            }
        }

        this.randomizer = randomizer;
    }

    public void bindRandomizer(List<IRandomizerOld> randomizers) {
        if(this.randomizer == null) {
            if(!randomizers.isEmpty()) {
                for(IRandomizerOld r : randomizers) {
                    if(r.isSetting(getName())) {
                        this.randomizer = r;
                        return;
                    }
                }

                if(isBuiltIntSetting())
                    return;

                for(IRandomizerOld r : randomizers)
                    if(r.getID().equalsIgnoreCase("%n_a%"))
                        this.randomizer = r;

                //this.randomizer = randomizers.get(0);
            }
        }
    }

    public void bindTextView(TextView tv) { this.tvName = tv; }
    public void bindInputTextBox(TextInputEditText textEdit) { bindInputTextBox(textEdit, null); }

    //Maybe we should accept null ??
    public void bindInputTextBox(TextInputEditText textEdit, TextWatcher onTextEdit) {
        if(textEdit != null) {
            this.inputText = textEdit;
            this.textWatcher = onTextEdit;
        }
    }
    public void unBindInputTextBox() { this.inputText = null; this.textWatcher = null; }
    public void setInputTextEmpty() { setInputText(""); }
    public void setInputText(String text) {
        try {
            if(text == null) text = "";
            if(this.inputText != null) {
                if(this.textWatcher != null)
                    this.inputText.removeTextChangedListener(this.textWatcher);
                this.inputText.setText(text);
                //this.inputText.getParent()
                if(this.textWatcher != null)
                    this.inputText.addTextChangedListener(this.textWatcher);
            }
        }catch (Exception e) {
            Log.e("XLua.LuaSettingExtended", "Failed to set InputTextEdit e=" + e);
        }
    }

    public void setInputText() {
        if(this.inputText != null) {
            if(isModified() && this.modifiedValue != null) this.inputText.setText(this.modifiedValue);
            else if(this.value != null) this.inputText.setText(this.value);
            else this.inputText.setText("");
        }
    }

    public LuaSetting createSetting() { return LuaSetting.create(this); }
    public LuaSettingDefault createDefaultSetting() { return LuaSettingDefault.create(this); }
    public LuaSettingPacket createPacket() { return LuaSettingPacket.create(this); }
    public LuaSettingPacket createPacket(Integer code) { return LuaSettingPacket.create(this, code); }
    public LuaSettingPacket createPacket(Integer code, Boolean kill) { return LuaSettingPacket.create(this, code, kill); }

    @Override
    public ContentValues createContentValues() { return super.createContentValues(); }

    @Override
    public List<ContentValues> createContentValuesList() { return null; }

    @Override
    public void fromContentValuesList(List<ContentValues> contentValues) { }

    @Override
    public void fromContentValues(ContentValues contentValue) { super.fromContentValues(contentValue); }

    @Override
    public void fromCursor(Cursor cursor) { super.fromCursor(cursor); }

    @Override
    public String toJSON() throws JSONException { return toJSONObject().toString(2); }

    @Override
    public JSONObject toJSONObject() throws JSONException { return  super.toJSONObject(); }

    @Override
    public void fromJSONObject(JSONObject obj) throws JSONException { super.fromJSONObject(obj); }

    @Override
    public Bundle toBundle() { return super.toBundle(); }

    @Override
    public void fromBundle(Bundle bundle) { super.fromBundle(bundle); }

    @Override
    public void fromParcel(Parcel in) { super.fromParcel(in); }

    @Override
    public void writeToParcel(Parcel dest, int flags) { super.writeToParcel(dest, flags); }

    @Override
    public int describeContents() { return 0; }

    @Override
    public boolean equals(@Nullable Object obj) { return super.equals(obj); }

    @NonNull
    @Override
    public String toString() { return new StringBuilder(super.toString()).append(" enabled=").append(this.enabled).toString(); }

    public static Map<String, LuaSettingExtended> toMap(Collection<LuaSettingExtended> managed_settings) {
        Map<String, LuaSettingExtended> m = new HashMap<>();
        for(LuaSettingExtended s : managed_settings)
            m.put(s.getName(), s);

        return m;
    }
}
