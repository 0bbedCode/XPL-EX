package eu.faircode.xlua.x.xlua.settings;

import android.text.TextUtils;

public class SettingsUtil {
    public static void copyHolder(SettingHolder fromHolder, SettingHolder toHolder, boolean onlyIfThisIsNull) {
        if(fromHolder != null && toHolder != null) {
            // Copy 'value'
            if (!TextUtils.isEmpty(fromHolder.getValue())) {
                if (!onlyIfThisIsNull || TextUtils.isEmpty(toHolder.getValue())) {
                    toHolder.setValue(fromHolder.getValue(), false);
                }
            }

            // Copy 'description'
            if (!TextUtils.isEmpty(fromHolder.getDescription())) {
                if (!onlyIfThisIsNull || TextUtils.isEmpty(toHolder.getDescription())) {
                    toHolder.setDescription(fromHolder.getDescription());
                }
            }

            // Copy 'newValue'
            if (!TextUtils.isEmpty(fromHolder.getNewValue())) {
                if (!onlyIfThisIsNull || TextUtils.isEmpty(toHolder.getNewValue())) {
                    toHolder.setNewValue(fromHolder.getNewValue());
                }
            }
        }
    }
}
