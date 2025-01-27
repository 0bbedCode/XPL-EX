package eu.faircode.xlua.x.xlua.settings.deprecated;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.x.Str;

public class SettingsHelperOld {
    public static final List<String> XP_SETTINGS = Arrays.asList("show", "value.imei", "value.meid", "value.email", "value.android_id", "value.serial", "value.phone_number", "collection", "theme", "restrict_new_apps", "notify_new_apps", "notify");

    public static int getIndexFromCache(IndexCache indexCacheItem) { return isIndexCacheValid(indexCacheItem) ? indexCacheItem.getIndex() : -1; }
    public static boolean isIndexCacheValid(IndexCache indexCacheItem) { return indexCacheItem != null && indexCacheItem.isValid(); }
    public static boolean isIndexCacheParent(IndexCache indexCacheItem) { return isIndexCacheValid(indexCacheItem) && indexCacheItem.isParent(); }
    public static boolean isIndexCacheChild(IndexCache indexCacheItem) { return isIndexCacheValid(indexCacheItem) && indexCacheItem.isChild(); }

    public static boolean isParentSetting(SettingExtendedOld setting) { return setting != null && setting.getIndex() == 0; }
    public static boolean isChildSetting(SettingExtendedOld setting) { return setting != null && setting.getIndex() > 0; }

    public static String getIndexDescription(IndexCache indexCacheItem) { return indexCacheItem != null ? indexCacheItem.getDescription() : "unknown:error:description"; }
    public static String getIndexCacheCategory(IndexCache indexCacheItem) { return indexCacheItem != null ? indexCacheItem.getCategory() : "unknown"; }
    public static String getIndexCacheFriendlyName(IndexCache indexCacheItem) { return indexCacheItem != null ? indexCacheItem.getFriendlyName() : "unknown:error"; }
    public static String getIndexCacheNameWithoutIndex(IndexCache indexCacheItem) { return indexCacheItem != null ? indexCacheItem.getNameWithoutIndex() : "error:name_no_index"; }

    public static Map<Integer, IndexCache> getFactoryIndexCaches(SettingExtendedOld extended) { return extended != null ? getFactoryIndexCaches(extended.getFactory()) : null; }
    public static Map<Integer, IndexCache> getFactoryIndexCaches(IndexCacheFactory factory) { return factory != null ? factory.getIndexCaches() : null; }


    public static boolean seemsLikeSetting(String s) { return s != null && s.contains(".") && !s.contains(" "); }

    public static boolean isValidAndNotRandomized(SettingRandomContextOld settingRandom) { return settingRandom != null && !settingRandom.hasRandomized(); }

    public static boolean isBuiltInSetting(String settingName) {
        return XP_SETTINGS.contains(settingName) ||
                settingName.endsWith(".randomize") || settingName.equalsIgnoreCase("lac,cid"); }

    public static boolean isIndexCacheParentsChild(IndexCache parent, IndexCache child) {
        return isIndexCacheParent(parent) &&
                isIndexCacheChild(child) &&
                child.getGroupHashCode() == parent.getGroupHashCode();
    }

    public static boolean setLabelText(TextView tv, String text) {
        if(tv == null) return false;
        text = Str.ensureIsNotNullOrDefault(text, "");
        try {
            tv.setText(text);
            return true;
        }catch (Exception ignored) { return false; }
    }

    public static String getLabelText(TextView tv) { return getLabelText(tv, null); }
    public static String getLabelText(TextView tv, String defaultText) {
        if(tv == null) return defaultText;
        try {
            CharSequence c = tv.getText();
            return c == null ? defaultText : c.toString();
        }catch (Exception ignored) {
            return defaultText;
        }
    }

    public static boolean setInputTextText(TextInputEditText inputEditText, String text) { return setInputTextText(inputEditText, null, text); }
    public static boolean setInputTextText(TextInputEditText inputEditText, TextWatcher textWatcher, String text) {
        if(inputEditText == null) return false;
        text = Str.ensureIsNotNullOrDefault(text, "");
        try {
            if(textWatcher != null) inputEditText.removeTextChangedListener(textWatcher);
            inputEditText.setText(text);
            if(textWatcher != null) inputEditText.addTextChangedListener(textWatcher);
            return true;
        }catch (Exception ignored) { return false; }
    }

    public static String getInputTextText(TextInputEditText inputEditText) { return getInputTextText(inputEditText, null); }
    public static String getInputTextText(TextInputEditText inputEditText, String defaultText) {
        if(inputEditText == null) return defaultText;
        try {
            Editable e = inputEditText.getText();
            return e == null ? defaultText : e.toString();
        }catch (Exception ignored) {
            return defaultText;
        }
    }
}
