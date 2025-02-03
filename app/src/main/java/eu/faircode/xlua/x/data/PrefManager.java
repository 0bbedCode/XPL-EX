package eu.faircode.xlua.x.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;

/**
 * ToDo:    [1] Clean Up things like "isValidManager" / "ensureValidTag" Maybe Utils Class ? also integrate IValidator ?
 *          [2] Clean up Warning with Suppress Warning Attribute
 *          [3] Have a Global Cache System, ensures One Instance to that Name Space is Opened and Created and Used, maybe like StrongRandom Class
 *          [4] Ensure all is Safe, Thread Safe
 *          [5] Ensure Both Old and New Method for Opening / using Preferences works and Update Code to use this class
 *          [6] Ensure works for Direct Default name space (main activity usage) and name space with Tags
 *          [7] Have Option to Set Mode, maybe options to "clean" ? "Dispose" ?
 *          [8] Finish "toString()" functions and "equals" functions
 *          [9] Work with more "que" systems similar to the "report" system
 *          [10] Work on Compare Functions like for Repos
 *          [11] Work on Repo Interface
 *          [12] Work on System for like IPreferences ? "getString" "ensureHasData" etc, one Can link to DB table called "settings" and then local prefs
 *          [13] Possible Backup for Prefs & Clear all Functions etc
 *          [14] Make a Apply Function for Prefs that Directly Returns the Value back that was being applied ?
 *          [15] Clean Up String Utils Class add more helpers for "toStringOrNull()"
 *          [16] Add more Utils for List
 *          [17] Add more Randomizer Code / Clean up and Summaries
 *          [18] Clean up "AppGeneric"
 *          [19] Update "to" List Functions to Ensure if its EMPTY then just create a Empty
 *          [20] "isValid(Object... objects)" check if each not null if they are Instance of "IValidator" check is Valid, maybe have checks for Empty Kinds
 *          [21] For Name Space make object, has tag , namespace, sub location etc, String List etc
 *          EXTRA:  Update StringBuilder Custom Maybe a Replacer / Interceptor, similar to Block Builder use for Delimiter
 *                  Make some interface for Objects that use Pref Manager, IPrefManager ? returns Struct for its Data
 *                  Log system like old DB manager "logError(int errorCode, String tag, boolean doLog, boolean doThrow)
 */
public class PrefManager {
    public static final String NAMESPACE_DELIMINATOR = "_";
    public static final String DEFAULT_NAMESPACE = "settings";
    public static final String SETTINGS_NAMESPACE = "settings_settings";

    public static final String SETTING_SETTINGS_CHECKED = "_checked_settings_1_";

    public static String nameForChecked() { return nameForChecked(true, null); }
    public static String nameForChecked(boolean global) { return nameForChecked(global, null); }
    public static String nameForChecked(boolean global, String pkg) {
        return global || pkg == null ? SETTING_SETTINGS_CHECKED : Str.combine(SETTING_SETTINGS_CHECKED, pkg);
    }

    public static PrefManager create() { return new PrefManager(); }
    public static PrefManager create(String tag) {  return new PrefManager(tag); }
    public static PrefManager create(Context context, String tag) { return new PrefManager(context, tag); }

    public static boolean isValidManager(PrefManager manager) { return manager != null && manager.getPreferences() != null; }

    public static String ensureValidTag(String tag) { return tag == null ? DEFAULT_NAMESPACE : tag; }
    public static String createFullNameSpace(String tag) {
        return StrBuilder.create()
                .append(DEFAULT_NAMESPACE)
                .append(NAMESPACE_DELIMINATOR)
                .append(ensureValidTag(tag))
                .toString();
    }

    private String tag;
    private String namespace;
    private SharedPreferences preferences;

    public String getTag() { return tag; }
    public String getNamespace() { return namespace; }
    public SharedPreferences getPreferences() { return preferences; }

    public PrefManager() { }
    public PrefManager(String tag) { this.tag = tag; }
    public PrefManager(Context context, String tag) { ensureIsOpen(context, tag); }

    public void ensureIsOpen(Context context, String tagOrNamespace) {
        if(!Str.isEmpty(tagOrNamespace)) {
            this.tag = tagOrNamespace;
        }

        if(this.preferences == null && context != null && !Str.isEmpty(this.tag)) {
            try {
                this.tag = ensureValidTag(tag);
                this.namespace = createFullNameSpace(tag);
                this.preferences = context.getSharedPreferences(this.namespace, Context.MODE_PRIVATE);
            }catch (Exception e) {
                Log.e("XLua.Pref", "Failed to Open Shared Preferences! Tag=" + tagOrNamespace + " Error=" + e);
            }
        }
    }

    public void close() {
        //re open options
    }

    public static String getString(PrefManager manager, String key) { return getString(manager, key, null, false); }
    public static String getString(PrefManager manager, String key, String defaultValue) { return getString(manager, key, defaultValue, true); }
    public static String getString(PrefManager manager, String key, String defaultValue, boolean putIfMissing) { return !isValidManager(manager) ? defaultValue : manager.getString(key, defaultValue, putIfMissing); }

    public static boolean getBoolean(PrefManager manager, String key) { return getBoolean(manager, key, false, false); }
    public static boolean getBoolean(PrefManager manager, String key, boolean defaultValue) { return getBoolean(manager, key, defaultValue, true); }
    public static boolean getBoolean(PrefManager manager, String key, boolean defaultValue, boolean putIfMissing) { return !isValidManager(manager) ? defaultValue : manager.getBoolean(key, defaultValue, putIfMissing); }

    public static int getInteger(PrefManager manager, String key) { return getInteger(manager, key, -1, false); }
    public static int getInteger(PrefManager manager, String key, int defaultValue) { return getInteger(manager, key, defaultValue, true); }
    public static int getInteger(PrefManager manager, String key, int defaultValue, boolean putIfMissing) { return !isValidManager(manager) ? defaultValue : manager.getInteger(key, defaultValue, putIfMissing); }


    public List<String> getStringList(String key) { return getStringList(key, null, false); }
    public List<String> getStringList(String key, List<String> defaultValue) { return getStringList(key, defaultValue, defaultValue != null); }
    public List<String> getStringList(String key, List<String> defaultValue, boolean putIfMissing) {
        if(preferences == null || Str.isEmpty(key))
            return defaultValue;

        if(!preferences.contains(key)) {
            if(putIfMissing) preferences.edit().putString(key, Str.joinList(defaultValue)).apply();
            return defaultValue;
        }

        return Str.splitToList(preferences.getString(key, Str.EMPTY));
    }

    public List<String> putStringList(String key, List<String> value) {
        if(preferences == null || Str.isEmpty(key))
            return value;

        if(!ListUtil.isValid(value))
            if(preferences.contains(key))
                preferences.edit().remove(key).apply();

        preferences.edit().putString(key, Str.joinList(value)).apply();
        return value;
    }

    public int getInteger(String key) { return getInteger(key, -1, false); }
    public int getInteger(String key, int defaultValue) { return getInteger(key, defaultValue, true); }
    public int getInteger(String key, int defaultValue, boolean putIfMissing) {
        if(preferences == null || Str.isEmpty(key))
            return defaultValue;

        if(!preferences.contains(key)) {
            if(putIfMissing) preferences.edit().putInt(key, defaultValue).apply();
            return defaultValue;
        }

        return preferences.getInt(key, defaultValue);
    }

    public int putInteger(String key, int value) {
        if(preferences == null || Str.isEmpty(key))
            return value;

        preferences.edit().putInt(key, value).apply();
        return value;
    }

    public String getString(String key) { return getString(key, null, false); }
    public String getString(String key, String defaultValue) { return getString(key, defaultValue, defaultValue != null); }
    public String getString(String key, String defaultValue, boolean putIfMissing) {
        if(preferences == null || Str.isEmpty(key))
            return defaultValue;

        if(!preferences.contains(key)) {
            if(putIfMissing) preferences.edit().putString(key, defaultValue).apply();
            return defaultValue;
        }

        return preferences.getString(key, defaultValue);
    }

    public String putString(String key, String value) {
        if(this.preferences == null || Str.isEmpty(key))
            return value;

        preferences.edit().putString(key, value).apply();
        return value;
    }

    public boolean getBoolean(String key) { return getBoolean(key, false, false); }
    public boolean getBoolean(String key, boolean defaultValue) { return getBoolean(key, defaultValue, true); }
    public boolean getBoolean(String key, boolean defaultValue, boolean putIfMissing) {
        if(preferences == null || Str.isEmpty(key))
            return defaultValue;

        if(!preferences.contains(key)) {
            if(putIfMissing) preferences.edit().putBoolean(key, defaultValue).apply();
            return defaultValue;
        }

        return preferences.getBoolean(key, defaultValue);
    }

    public boolean putBoolean(String key, boolean value) {
        if(preferences == null || Str.isEmpty(key))
            return value;

        preferences.edit().putBoolean(key, value).apply();
        return value;
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Tag", this.tag)
                .appendFieldLine("Namespace", this.namespace)
                .appendFieldLine("Is Valid", isValidManager(this))
                .toString(true);
    }
}
