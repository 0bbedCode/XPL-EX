package eu.faircode.xlua.x.xlua.settings.deprecated;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.List;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;

public class SettingRandomContextOld {
    private static final String TAG = "XLua.SettingRandomContext";

    public static SettingRandomContextOld create(SettingExtendedOld setting) { return new SettingRandomContextOld(setting); }
    private final SettingExtendedOld mSetting;
    private boolean mHasRandomized = false;

    public SettingRandomContextOld(SettingExtendedOld setting) { this.mSetting = setting; }

    public String getName() { return mSetting == null ? null : mSetting.getNameWithoutIndex(); }
    public SettingExtendedOld getSetting() { return mSetting; }
    public IRandomizer getRandomizer() { return hasRandomizer() ? mSetting.getRandomizer() : null; }

    public boolean isValid() { return mSetting != null; }
    public boolean isEnabled() { return mSetting != null && mSetting.isEnabled(); }
    public boolean isEnabled(boolean isEnabledOverrideFlag) { return mSetting != null && (mSetting.isEnabled() || isEnabledOverrideFlag); }
    public boolean isParentControl() {
        //return mSetting != null && ((getName().endsWith(".parent")) || (hasRandomizer() && getRandomizer().isParentControl()));
        return false;
    }  //Lets also check for interface ?

    public boolean hasRandomized() { return mHasRandomized; }
    public boolean hasRandomizer() { return mSetting != null && mSetting.getRandomizer() != null; }

    public String getCurrentValueOrDefault(String defaultValue) { return getCurrentValueOrDefault(defaultValue, true, false, false); }
    public String getCurrentValueOrDefault(String defaultValue, boolean ensureValueIsNotNullOrEmpty) { return getCurrentValueOrDefault(defaultValue, ensureValueIsNotNullOrEmpty, false, false); }
    public String getCurrentValueOrDefault(String defaultValue, boolean ensureValueIsNotNullOrEmpty, boolean updateValueIfNeeded) { return getCurrentValueOrDefault(defaultValue, ensureValueIsNotNullOrEmpty, updateValueIfNeeded, false); }
    public String getCurrentValueOrDefault(
            String defaultValue,
            boolean ensureValueIsNotNullOrEmpty,
            boolean updateValueIfNeeded,
            boolean isEnabledIfNotOverrideFlag) {
        if(mSetting == null) {
            Log.e(TAG, "Settings is NULL, failed to get Current Value or Default... Stack=" + RuntimeUtils.getStackTraceSafeString());
            return defaultValue;
        } else {
            //REMOVE CLASS
            String v = Str.getNonNullOrEmptyString(mSetting.getNewValue(), mSetting.getValue());
            if(ensureValueIsNotNullOrEmpty && TextUtils.isEmpty(v)) {
                if(updateValueIfNeeded && isEnabled(isEnabledIfNotOverrideFlag)) {
                    setNewValue(defaultValue);
                    return defaultValue;
                }
            }

            return v;
        }
    }

    public String getValue(List<String> acceptableValues) { return getValue(acceptableValues, false, false); }
    public String getValue(List<String> acceptableValues, boolean updateValueIfNeeded) { return getValue(acceptableValues, updateValueIfNeeded, false); }
    public String getValue(
            List<String> acceptableValues,
            boolean updateValueIfNeeded,
            boolean isEnabledIfNotOverrideFlag) {
        if(!ListUtil.isValid(acceptableValues)) return null;
        if(mSetting == null) {
            Log.e(TAG, "Settings is NULL, failed to get Current Value... Stack=" + RuntimeUtils.getStackTraceSafeString());
            return null;
        } else {
            String v = Str.getNonNullOrEmptyString(mSetting.getNewValue(), mSetting.getValue());
            if(TextUtils.isEmpty(v) || !acceptableValues.contains(v.toLowerCase())) {
                if(updateValueIfNeeded && isEnabled(isEnabledIfNotOverrideFlag)) {
                    String ran = RandomGenerator.nextElement(acceptableValues);
                    setNewValue(ran);
                    return ran;
                }
            }

            return v;
        }
    }

    public void randomize(SettingsContextOld context) {
        if(context != null && hasRandomizer()) {
            //getRandomizer().randomize(context);
            setNewValue(getRandomizer().generateString(), !context.ignoreDisabled());  //Make sure if this is parent / options its just the Display Name ?
        }
    }

    public void setNewValue(String newValue) { setNewValue(newValue, false); }
    public void setNewValue(String newValue, boolean isEnabledOverrideFlag) {
        if(!mHasRandomized && mSetting != null) {
            if(isEnabled(isEnabledOverrideFlag)) {
                mHasRandomized = true;
                mSetting.updateModified(newValue);
            }
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) { return (hasRandomizer() && getRandomizer().equals(obj)) || (mSetting != null && mSetting.equals(obj)); }
}
