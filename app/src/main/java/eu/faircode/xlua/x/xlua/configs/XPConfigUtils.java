package eu.faircode.xlua.x.xlua.configs;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

public class XPConfigUtils {
    private static final String TAG = "XLua.XPConfigUtils";

    /**
     * Converts a list of SettingPacket to a JSON string.
     * @param settings The list of settings.
     * @return A JSON string representation of the settings or an empty string on failure.
     */
    public static String settingsToJson(List<SettingPacket> settings) {
        try {
            JSONObject settingsObj = new JSONObject();
            for (SettingPacket setting : settings) {
                settingsObj.put(setting.name, setting.value);
            }
            return settingsObj.toString();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to convert settings to JSON", e);
            return "";
        }
    }

    /**
     * Parses a JSON string into a list of SettingPacket.
     * @param json The JSON string representation of settings.
     * @return A list of SettingPacket objects or an empty list on failure.
     */
    public static List<SettingPacket> jsonToSettings(String json) {
        List<SettingPacket> settings = new ArrayList<>();
        if (json != null && !json.isEmpty()) {
            try {
                JSONObject settingsObj = new JSONObject(json);
                Iterator<String> keys = settingsObj.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = settingsObj.getString(key);
                    settings.add(new SettingPacket(key, value));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse settings JSON: " + json, e);
            }
        }
        return settings;
    }

    /**
     * Converts a list of hook IDs to a JSON string.
     * @param hooks The list of hook IDs.
     * @return A JSON array string representation of the hooks or an empty string on failure.
     */
    public static String hooksToJson(List<String> hooks) {
        try {
            return new JSONArray(hooks).toString();
        } catch (Exception e) {
            Log.e(TAG, "Failed to convert hooks to JSON", e);
            return "";
        }
    }

    /**
     * Parses a JSON string into a list of hook IDs.
     * @param json The JSON string representation of hooks.
     * @return A list of hook IDs or an empty list on failure.
     */
    public static List<String> jsonToHooks(String json) {
        List<String> hooks = new ArrayList<>();
        if (json != null && !json.isEmpty()) {
            try {
                JSONArray hooksArray = new JSONArray(json);
                for (int i = 0; i < hooksArray.length(); i++) {
                    hooks.add(hooksArray.getString(i));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse hooks JSON: " + json, e);
            }
        }
        return hooks;
    }
}
