package eu.faircode.xlua.interceptors;

import android.util.Log;

import java.util.Map;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.utilities.CollectionUtil;
import eu.faircode.xlua.utilities.StringUtil;

public class UserContextMaps {
    public static UserContextMaps create(Map<String, String> settings, Map<String, String> propMaps, Map<String, Integer> propSettings) { return new UserContextMaps(settings, propMaps, propSettings); }

    private static final String TAG = "XLua.InterceptUserContext";
    private Map<String, String> settings;
    private Map<String, String> propMaps;
    private Map<String, Integer> propSettings;

    public UserContextMaps() { }
    public UserContextMaps(Map<String, String> settings, Map<String, String> propMaps, Map<String, Integer> propSettings) {
        this.settings = settings;
        this.propMaps = propMaps;
        this.propSettings = propSettings;
        Log.i(TAG, "settings size=" + this.settings.size() + "isValid=" + this.isSettingsValid() + " propMaps=" + this.propMaps.size() + " isValid=" + this.isPropMapsValid() + " propSettings=" + this.propSettings.size() + " isValid=" + this.isPropSettingsValid());
    }

    public boolean isSettingsValid() { return CollectionUtil.isValid(this.settings); }
    public void setSettings(Map<String, String> settings) { this.settings = settings; }
    public Map<String, String> getSettings() { return this.settings; }

    public boolean isPropMapsValid() { return CollectionUtil.isValid(this.propMaps); }
    public void setPropMaps(Map<String, String> propMaps) { this.propMaps = propMaps; }
    public Map<String, String> getPropMaps() { return this.propMaps; }

    public boolean isPropSettingsValid() { return CollectionUtil.isValid(this.propSettings); }
    public void setPropSettings(Map<String, Integer> propSettings) { this.propSettings = propSettings; }
    public Map<String, Integer> getPropSettings() { return this.propSettings; }

    public boolean hasPropertySetting(String property) { return isPropSettingsValid() && propSettings.containsKey(property); }
    public Integer getPropertySetting(String property) {
        if(!isPropSettingsValid()) return null;
        return propSettings.get(property);
    }

    public boolean hasProperty(String property) { return isPropMapsValid() && propMaps.containsKey(property); }
    public String getPropSetting(String property) {
        if(!isPropMapsValid()) return null;
        return propMaps.get(property);
    }

    public Integer getSettingInteger(String name, Integer defaultValue) {
        String s = getSetting(name);
        if(s == null) return defaultValue;
        return StringUtil.toInteger(s, defaultValue);
    }

    public boolean getSettingBool(String name) { return getSettingBool(name, false); }
    public boolean getSettingBool(String name, boolean defaultValue) {
        String v = getSetting(name);
        if(v == null) return defaultValue;
        return Str.toBoolean(v, defaultValue);
    }

    public String getSetting(String name, String defaultValue) {
        if(!isSettingsValid()) return null;
        String setting = getSetting(name);
        return setting == null ? defaultValue : setting;
    }

    public String getSettingReMap(String name, String oldName, String defaultValue) {
        if(!isSettingsValid()) return null;
        String setting = getSetting(name);
        if(setting == null && StringUtil.isValidString(oldName))
            setting = getSetting(oldName);

        if(setting == null) return defaultValue;
        else return setting;
    }

    public String getSetting(String name) {
        if(!isSettingsValid()) return null;
        try {
            return (this.settings.containsKey(name) ? this.settings.get(name) : null);
        }catch (Exception e) {
            Log.e(TAG, "Failed to get Setting: " + name + " e=" + e);
            return null;
        }
    }
}
