package eu.faircode.xlua.x.xlua.settings.data;



import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.xstandard.interfaces.IJsonSerial;
import eu.faircode.xlua.x.xlua.LibUtil;

public class XScript implements IJsonSerial {
    private static final String TAG = LibUtil.generateTag(XScript.class);

    // JSON field names
    public static final String FIELD_NAME = "name";
    public static final String FIELD_CODE = "code";

    // Fields matching JSON structure
    private String name;
    private String code;

    // Getters
    public String getName() { return name; }
    public String getCode() { return code; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setCode(String code) { this.code = code; }

    // Default constructor
    public XScript() { }

    // Parcel constructor if needed
    public XScript(Parcel in) { }

    @Override
    public String toJSON() throws JSONException { return toJSONObject().toString(); }
    @Override
    public JSONObject toJSONObject() throws JSONException {
        // Skip serialization if critical fields are empty
        if (name == null || name.isEmpty() || code == null || code.isEmpty())
            return null;

        JSONObject obj = new JSONObject();
        try {
            obj.put(FIELD_NAME, name);
            obj.put(FIELD_CODE, code);
        } catch (Exception e) {
            if (DebugUtil.isDebug())
                Log.e(TAG, "Error creating JSON object: " + e);
            return null;
        }

        return obj;
    }

    @Override
    public void fromJSONObject(JSONObject obj) {
        if (obj == null) return;

        try {
            String tempName = obj.optString(FIELD_NAME, "");
            String tempCode = obj.optString(FIELD_CODE, "");

            // Only set fields if both are valid
            if (!tempName.isEmpty() && !tempCode.isEmpty()) {
                this.name = tempName;
                this.code = tempCode;
            }
        } catch (Exception e) {
            if (DebugUtil.isDebug())
                Log.e(TAG, "Error parsing JSON object: " + e);
        }
    }
    // Required interface methods that aren't used for this class
    @Override
    public ContentValues createContentValues() { return null; }

    @Override
    public List<ContentValues> createContentValuesList() {
        return Collections.emptyList();
    }

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

    @Override
    public String toString() {
        try {
            return toJSON();
        } catch (JSONException e) {
            return "XScript{name='" + name + "', code='" + (code != null ? code.substring(0, Math.min(code.length(), 50)) + "..." : "null") + "'}";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XScript xScript = (XScript) o;

        if (name != null ? !name.equals(xScript.name) : xScript.name != null) return false;
        return code != null ? code.equals(xScript.code) : xScript.code == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        return result;
    }
}