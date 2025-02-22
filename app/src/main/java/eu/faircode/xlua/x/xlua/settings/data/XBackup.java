package eu.faircode.xlua.x.xlua.settings.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.faircode.xlua.BuildConfig;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.xstandard.interfaces.IJsonSerial;
import eu.faircode.xlua.utilities.JSONUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;

public class XBackup implements IJsonSerial {
    private final String TAG = LibUtil.generateTag(XBackup.class);

    //Make it options to encode / PGP / Encrypt, later (low on list to do)
    public static final String FIELD_NAME = "name";
    public static final String FIELD_APP_VERSION = "appVersion";
    public static final String FIELD_DATE = "date";

    public static final String FIELD_DEFINITION = "definition";
    public static final String FIELD_SCRIPT = "script";

    public static final String FIELD_ASSIGNMENTS = "assignment";
    public static final String FIELD_SETTINGS = "setting";

    private String name;
    private String appVersion;
    private long date;

    public boolean dropOld = false;

    private final List<XLuaHook> definitions = new ArrayList<>();
    private final List<XScript> scripts = new ArrayList<>();
    private final List<AssignmentPacket> assignments = new ArrayList<>();
    private final List<SettingPacket> settings = new ArrayList<>();

    public String getName() { return name; }
    public String getAppVersion() { return appVersion;  }
    public long getDate() { return date; }

    // Add these getter methods to XBackup.java

    // Getters
    public List<XLuaHook> getDefinitions() { return definitions; }
    public List<XScript> getScripts() { return scripts; }
    public List<AssignmentPacket> getAssignments() { return assignments; }
    public List<SettingPacket> getSettings() { return settings; }

    // Setter for name since dialog needs to set it
    public void setName(String name) { this.name = name; }
    public void setDate(long date) { this.date = date; }
    public void setAppVersion(String version) { this.appVersion = version; }

    // Utility count methods (optional, if you want to use them)
    public int getDefinitionsCount() { return ListUtil.size(definitions); }
    public int getScriptsCount() { return ListUtil.size(scripts); }
    public int getAssignmentsCount() { return ListUtil.size(assignments); }
    public int getSettingsCount() { return ListUtil.size(settings); }

    // Utility clear methods
    public void clearDefinitions() { definitions.clear(); }
    public void clearScripts() { scripts.clear(); }
    public void clearAssignments() { assignments.clear(); }
    public void clearSettings() { settings.clear(); }

    // Add/Remove methods (optional, for finer control)
    public void addDefinition(XLuaHook hook) { if (hook != null) definitions.add(hook); }
    public void addScript(XScript script) { if (script != null) scripts.add(script); }
    public void addAssignment(AssignmentPacket assignment) { if (assignment != null) assignments.add(assignment); }
    public void addSetting(SettingPacket setting) { if (setting != null) settings.add(setting); }

    // Validation helper (used by dialog to check if content exists)
    public boolean hasContent() { return ListUtil.isValid(definitions) || ListUtil.isValid(scripts) || ListUtil.isValid(assignments) || ListUtil.isValid(settings); }

    public XBackup() { }
    public XBackup(Parcel in) {  }

    public void reset() {
        this.name = null;
        this.appVersion = null;
        this.date = 0;
        this.definitions.clear();
        this.scripts.clear();
        this.assignments.clear();
        this.settings.clear();
    }

    public void copyFrom(XBackup from) {
        if(from != null) {
            this.name = from.name;
            this.appVersion = from.appVersion;
            this.date = from.date;
            this.dropOld = from.dropOld;
            ListUtil.addAllIfValid(this.definitions, from.definitions, true);
            ListUtil.addAllIfValid(this.scripts, from.scripts, true);
            ListUtil.addAllIfValid(this.assignments, from.assignments, true);
            ListUtil.addAllIfValid(this.settings, from.settings, true);
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Backup Copied!\n > From:([%s])\n > This:([%s])",
                        Str.toStringOrNull(Str.noNL(from.dumpLog())),
                        Str.toStringOrNull(Str.noNL(dumpLog()))));
        }
    }

    /**
     * Creates a new XBackup instance containing only the specified lists
     */
    public XBackup createFilteredCopy(boolean includeDefinitions,
                                      boolean includeScripts,
                                      boolean includeAssignments,
                                      boolean includeSettings) {
        XBackup filtered = new XBackup();
        filtered.name = Str.ensureIsNotNullOrDefault(this.name, "null");
        filtered.appVersion = Str.ensureIsNotNullOrDefault(this.appVersion, "null-version");
        filtered.date = this.date;
        filtered.dropOld = this.dropOld;

        if (includeDefinitions && ListUtil.isValid(definitions))
            filtered.definitions.addAll(definitions);

        if (includeScripts && ListUtil.isValid(scripts))
            filtered.scripts.addAll(scripts);

        if (includeAssignments && ListUtil.isValid(assignments))
            filtered.assignments.addAll(assignments);

        if (includeSettings && ListUtil.isValid(settings))
            filtered.settings.addAll(settings);

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Filtering, Include Definitions (%s) Include Scripts (%s) Include Assignments (%s) Include Settings (%s).\n > Current Dump=([%s])\n > Copy Dump=([%s])",
                    includeDefinitions,
                    includeScripts,
                    includeAssignments,
                    includeSettings,
                    Str.toStringOrNull(Str.noNL(dumpLog())),
                    Str.toStringOrNull(Str.noNL(filtered.dumpLog()))));

        return filtered;
    }

    public String dumpLog() {
        return Str.fm(" Name [%s] App Version [%s] Data [%s] Definitions Count [%s] Scripts Count [%s] Assignment Count [%s] Settings Count [%s]",
                Str.ensureIsNotNullOrDefault(name, "null"),
                Str.ensureIsNotNullOrDefault(appVersion, "null-version"),
                String.valueOf(date),
                ListUtil.size(definitions),
                ListUtil.size(scripts),
                ListUtil.size(assignments),
                ListUtil.size(scripts));
    }

    @Override
    public String toJSON() throws JSONException { return toJSONObject().toString(2); }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();

        if(DebugUtil.isDebug())
            Log.d(TAG, "ToJson, Dump=" + dumpLog());

        try {
            obj.put(FIELD_NAME, Str.getNonNullString(name, "null"));
            obj.put(FIELD_APP_VERSION, Str.getNonNullString(appVersion, BuildConfig.VERSION_NAME));
            obj.put(FIELD_DATE, date > 0 ? date : System.currentTimeMillis());
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("(0) Pushing (%s) Definitions to JSON, Name (%s) App Version (%s) Date (%s)",
                        ListUtil.size(definitions),
                        name,
                        appVersion,
                        date));

            // Handle definition array
            if (ListUtil.isValid(definitions)) {
                JSONArray definitionArray = new JSONArray();
                for (XLuaHook hook : definitions) {
                    try {
                        if (hook == null)
                            continue;

                        JSONObject hookObj = hook.toJSONObject();
                        if (hookObj != null)
                            definitionArray.put(hookObj);

                    } catch (Exception e) {
                        if (DebugUtil.isDebug())
                            Log.e(TAG, "Error serializing hook: " + e);
                    }
                }
                if (ArrayUtils.isValid(definitionArray))
                    obj.put(FIELD_DEFINITION, definitionArray);
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("(1) Finished Pushing (%s) Definitions to JSON, now Pushing Scripts (%s)",
                        ListUtil.size(definitions),
                        ListUtil.size(scripts)));

            // Handle script array
            if (ListUtil.isValid(scripts)) {
                JSONArray scriptArray = new JSONArray();
                for (XScript script : scripts) {
                    try {
                        if (script == null)
                            continue;

                        JSONObject scriptObj = script.toJSONObject();
                        if (scriptObj != null)
                            scriptArray.put(scriptObj);

                    } catch (Exception e) {
                        if (DebugUtil.isDebug())
                            Log.e(TAG, "Error serializing script: " + e);
                    }
                }
                if (ArrayUtils.isValid(scriptArray))
                    obj.put(FIELD_SCRIPT, scriptArray);
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("(2) Finished Pushing (%s) Script to JSON, now Pushing Assignments (%s)",
                        ListUtil.size(scripts),
                        ListUtil.size(assignments)));

            // Handle assignments array
            if (ListUtil.isValid(assignments)) {
                JSONArray assignmentArray = new JSONArray();
                for (AssignmentPacket assignment : assignments) {
                    try {
                        if (assignment == null)
                            continue;

                        if(Str.isEmpty(assignment.getHookId()))
                            continue;

                        JSONObject assignObj = assignment.toJSONObject();
                        if (assignObj != null)
                            assignmentArray.put(assignObj);

                    } catch (Exception e) {
                        if (DebugUtil.isDebug())
                            Log.e(TAG, "Error serializing assignment: " + e);
                    }
                }
                if (ArrayUtils.isValid(assignmentArray))
                    obj.put(FIELD_ASSIGNMENTS, assignmentArray);
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("(3) Finished Pushing (%s) Assignments to JSON, now Pushing Settings (%s)",
                        ListUtil.size(assignments),
                        ListUtil.size(settings)));

            // Handle settings array
            if (ListUtil.isValid(settings)) {
                JSONArray settingsArray = new JSONArray();
                for (SettingPacket setting : settings) {
                    try {
                        if (setting == null)
                            continue;

                        if(setting.value == null)
                            continue;

                        JSONObject settingObj = setting.toJSONObject();
                        if (settingObj != null)
                            settingsArray.put(settingObj);

                    } catch (Exception e) {
                        if (DebugUtil.isDebug())
                            Log.e(TAG, "Error serializing setting: " + e);
                    }
                }
                if (ArrayUtils.isValid(settingsArray))
                    obj.put(FIELD_SETTINGS, settingsArray);
            }
        } catch (Exception e) {
            if (DebugUtil.isDebug())
                Log.e(TAG, "Error creating JSON object: " + e);
        }

        if(DebugUtil.isDebug()) {
            String json = Str.ensureNoDoubleNewLines(JSONUtil.objectToString(obj));
            Log.d(TAG, "To JSON Result Raw=" + json);
        }

        return obj;
    }

    @Override
    public void fromJSONObject(JSONObject obj) {
        if (obj == null)
            return;
        try {
            // Core fields with safe defaults
            this.name = obj.optString(FIELD_NAME, "null");
            this.appVersion = obj.optString(FIELD_APP_VERSION, BuildConfig.VERSION_NAME);
            this.date = obj.optLong(FIELD_DATE, System.currentTimeMillis());

            // Ensure lists are initialized
            definitions.clear();
            scripts.clear();
            assignments.clear();
            settings.clear();

            // Parse definition array safely
            try {
                JSONArray definitionArray = obj.optJSONArray(FIELD_DEFINITION);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("(1) Iterating Json Array, Count=" + ArrayUtils.safeLength(definitionArray)));

                if (ArrayUtils.isValid(definitionArray)) {
                    for (int i = 0; i < definitionArray.length(); i++) {
                        try {
                            JSONObject hookObj = definitionArray.optJSONObject(i);
                            if (hookObj != null) {
                                XLuaHook hook = new XLuaHook();
                                hook.fromJSONObject(hookObj);
                                definitions.add(hook);
                            }
                        } catch (Exception e) {
                            if (DebugUtil.isDebug()) {
                                Log.e(TAG, "Error parsing hook at index " + i + ": " + e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (DebugUtil.isDebug()) {
                    Log.e(TAG, "Error parsing definitions array: " + e);
                }
            }

            // Parse script array safely
            try {
                JSONArray scriptArray = obj.optJSONArray(FIELD_SCRIPT);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("(2) Iterating Json Array, Count=" + ArrayUtils.safeLength(scriptArray)));

                if (ArrayUtils.isValid(scriptArray)) {
                    for (int i = 0; i < scriptArray.length(); i++) {
                        try {
                            JSONObject scriptObj = scriptArray.optJSONObject(i);
                            if (scriptObj != null) {
                                XScript script = new XScript();
                                script.fromJSONObject(scriptObj);
                                scripts.add(script);
                            }
                        } catch (Exception e) {
                            if (DebugUtil.isDebug()) {
                                Log.e(TAG, "Error parsing script at index " + i + ": " + e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (DebugUtil.isDebug()) {
                    Log.e(TAG, "Error parsing scripts array: " + e);
                }
            }

            // Parse assignments array safely
            try {
                JSONArray assignmentArray = obj.optJSONArray(FIELD_ASSIGNMENTS);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("(3) Iterating Json Array, Count=" + ArrayUtils.safeLength(assignmentArray)));

                if (ArrayUtils.isValid(assignmentArray)) {
                    for (int i = 0; i < assignmentArray.length(); i++) {
                        try {
                            JSONObject assignObj = assignmentArray.optJSONObject(i);
                            if (assignObj != null) {
                                AssignmentPacket assignment = new AssignmentPacket();
                                assignment.fromJSONObject(assignObj);
                                if(Str.isEmpty(assignment.getHookId()))
                                    continue;

                                assignments.add(assignment);
                            }
                        } catch (Exception e) {
                            if (DebugUtil.isDebug()) {
                                Log.e(TAG, "Error parsing assignment at index " + i + ": " + e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (DebugUtil.isDebug()) {
                    Log.e(TAG, "Error parsing assignments array: " + e);
                }
            }

            // Parse settings array safely
            try {
                JSONArray settingsArray = obj.optJSONArray(FIELD_SETTINGS);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("(4) Iterating Json Array, Count=" + ArrayUtils.safeLength(settingsArray)));

                if (ArrayUtils.isValid(settingsArray)) {
                    for (int i = 0; i < settingsArray.length(); i++) {
                        try {
                            JSONObject settingObj = settingsArray.optJSONObject(i);
                            if (settingObj != null) {
                                SettingPacket setting = new SettingPacket();
                                setting.fromJSONObject(settingObj);
                                if(setting.value == null)
                                    continue;

                                settings.add(setting);
                            }
                        } catch (Exception e) {
                            if (DebugUtil.isDebug()) {
                                Log.e(TAG, "Error parsing setting at index " + i + ": " + e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (DebugUtil.isDebug()) {
                    Log.e(TAG, "Error parsing settings array: " + e);
                }
            }

        } catch (Exception e) {
            if (DebugUtil.isDebug()) {
                Log.e(TAG, "Error parsing JSON object: " + e);
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "FromJson, Dump=" + dumpLog());
    }

    @Override
    public ContentValues createContentValues() { return null; }

    @Override
    public List<ContentValues> createContentValuesList() { return Collections.emptyList(); }

    @Override
    public void fromContentValuesList(List<ContentValues> contentValues) { }

    @Override
    public void fromContentValues(ContentValues contentValue) { }

    @Override
    public void fromCursor(Cursor cursor) { }

    @Override
    public Bundle toBundle() { return null; }

    @Override
    public void fromBundle(Bundle bundle) { }

    @Override
    public void fromParcel(Parcel in) { }

    @Override
    public void writeToParcel(Parcel dest, int flags) { }
}
