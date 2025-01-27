package eu.faircode.xlua.api.hook;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.api.xstandard.interfaces.IJsonSerial;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.utilities.StringUtil;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;

public class XLuaHook extends XLuaHookBase implements IJsonSerial, Parcelable, IIdentifiableObject {
    private static final String TAG = "XLua.Hook";

    private final List<LuaSettingExtended> managed_settings = new ArrayList<>();

    public XLuaHook() { }
    public XLuaHook(Parcel in) { fromParcel(in); }
    public XLuaHook(HookDatabaseEntry hookDb) { fromBundle(hookDb.toBundle()); }

    public HookDatabaseEntry toHookDatabase() {
        try {
            LuaHookPacket packet = new LuaHookPacket(getId(), toJSON());
            return packet;
        }catch (Exception e) {
            Log.e(TAG, "Error converting xHook to Hook Packet! e=" + e + "\n" + Log.getStackTraceString(e));
            return null;
        }
    }

    public List<LuaSettingExtended> getManagedSettings() { return this.managed_settings; }
    public void initSettings(Map<String, LuaSettingExtended> settings) {
        if(this.settings != null) {
            managed_settings.clear();
            for (String s : this.settings) {
                if(!StringUtil.isValidAndNotWhitespaces(s)) continue;
                if(settings.containsKey(s)) {
                    LuaSettingExtended luaSetting = settings.get(s);
                    if(luaSetting == null) continue;
                    managed_settings.add(luaSetting);
                }
            }
        }
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if(flags == FLAG_WITH_DB) {
            //dest.writeString(this.name);
            dest.writeString(this.getId());
            try {
                dest.writeString(toJSON());
            }catch (JSONException e) {
                Log.e(TAG, "JsonException for Hook:\n" + e + "\n" + Log.getStackTraceString(e));
            }
        }else {
            dest.writeByte(this.builtin ? (byte) 1 : (byte) 0);
            dest.writeString(this.collection);
            dest.writeString(this.group);
            dest.writeString(this.name);
            dest.writeString(this.author);
            dest.writeInt(this.version);
            dest.writeString(this.description);
            dest.writeString(this.className);
            dest.writeString(this.resolvedClassName);
            dest.writeString(this.methodName);
            dest.writeStringArray(this.parameterTypes);
            dest.writeString(this.returnType);
            dest.writeInt(this.minSdk);
            dest.writeInt(this.maxSdk);
            dest.writeInt(this.minApk);
            dest.writeInt(this.maxApk);
            dest.writeStringArray(this.excludePackages);
            dest.writeByte(this.enabled ? (byte) 1 : (byte) 0);
            dest.writeByte(this.optional ? (byte) 1 : (byte) 0);
            dest.writeByte(this.usage ? (byte) 1 : (byte) 0);
            dest.writeByte(this.notify ? (byte) 1 : (byte) 0);
            if ((flags & FLAG_WITH_LUA) == 0) dest.writeString(null);
            else dest.writeString(this.luaScript);
            dest.writeStringArray(this.settings);
        }
    }

    @Override
    public ContentValues createContentValues() {
        ContentValues cv = new ContentValues();
        cv.put("id", this.getId());

        try {
            cv.put("definition", toJSON());
        }catch (JSONException e) {
            Log.e(TAG, "JsonException for Hook:\n" + e + "\n" + Log.getStackTraceString(e));
        }

        return cv;
    }

    @Override
    public List<ContentValues> createContentValuesList() { return null; }

    @Override
    public void fromContentValuesList(List<ContentValues> contentValues) { }

    @Override
    public void fromContentValues(ContentValues contentValue) {
        try {
            fromJSONObject(new JSONObject(contentValue.getAsString("definition")));
        }catch (JSONException ex) {
            Log.e(TAG, "[fromContentValues] failed to convert to JSON the DEFINITION in ContentValues Database Entry: " + ex);
        }
    }

    @Override
    public void fromCursor(Cursor cursor) {
        //this.getId() = CursorUtil.getString(cursor, "id");
        String json = CursorUtil.getString(cursor, "definition");
        if(json != null) {
            try {
                fromJSONObject(new JSONObject(json));
            }catch (JSONException e) {
                Log.e(TAG, "JsonException for Hook:\n" + e + "\n" + Log.getStackTraceString(e));
            }
        }
    }

    @Override
    public String toJSON() throws JSONException {
        return toJSONObject().toString(2);
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jRoot = new JSONObject();

        jRoot.put("builtin", this.builtin);
        jRoot.put("collection", this.collection);
        jRoot.put("group", this.group);
        jRoot.put("name", this.name);
        jRoot.put("author", this.author);
        jRoot.put("version", this.version);
        if (this.description != null)
            jRoot.put("description", this.description);

        jRoot.put("className", this.className);
        if (this.resolvedClassName != null)
            jRoot.put("resolvedClassName", this.resolvedClassName);
        if (this.methodName != null)
            jRoot.put("methodName", this.methodName);

        JSONArray jParam = new JSONArray();
        for (int i = 0; i < this.parameterTypes.length; i++)
            jParam.put(this.parameterTypes[i]);
        jRoot.put("parameterTypes", jParam);

        if (this.returnType != null)
            jRoot.put("returnType", this.returnType);

        jRoot.put("minSdk", this.minSdk);
        jRoot.put("maxSdk", this.maxSdk);

        jRoot.put("minApk", this.minApk);
        jRoot.put("maxApk", this.maxApk);

        if (this.excludePackages != null)
            jRoot.put("excludePackages", TextUtils.join(",", this.excludePackages));

        jRoot.put("enabled", this.enabled);
        jRoot.put("optional", this.optional);
        jRoot.put("usage", this.usage);
        jRoot.put("notify", this.notify);

        jRoot.put("luaScript", this.luaScript);

        if (this.settings != null) {
            JSONArray jSettings = new JSONArray();
            for (int i = 0; i < this.settings.length; i++)
                jSettings.put(this.settings[i]);
            jRoot.put("settings", jSettings);
        }
        return jRoot;
    }

    @Override
    public void fromJSONObject(JSONObject obj) throws JSONException {
        this.builtin = (obj.has("builtin") ? obj.getBoolean("builtin") : false);
        this.collection = obj.getString("collection");
        this.group = obj.getString("group");
        this.name = obj.getString("name");
        this.author = obj.getString("author");
        this.version = (obj.has("version") ? obj.getInt("version") : 0);
        this.description = (obj.has("description") ? obj.getString("description") : null);

        this.className = obj.getString("className");
        this.resolvedClassName = (obj.has("resolvedClassName") ? obj.getString("resolvedClassName") : null);
        this.methodName = (obj.has("methodName") ? obj.getString("methodName") : null);

        JSONArray jParam = obj.getJSONArray("parameterTypes");
        this.parameterTypes = new String[jParam.length()];
        for (int i = 0; i < jParam.length(); i++)
            this.parameterTypes[i] = jParam.getString(i);

        this.returnType = (obj.has("returnType") ? obj.getString("returnType") : null);

        this.minSdk = obj.getInt("minSdk");
        this.maxSdk = (obj.has("maxSdk") ? obj.getInt("maxSdk") : 999);

        this.minApk = (obj.has("minApk") ? obj.getInt("minApk") : 0);
        this.maxApk = (obj.has("maxApk") ? obj.getInt("maxApk") : Integer.MAX_VALUE);

        this.excludePackages = (obj.has("excludePackages")
                ? obj.getString("excludePackages").split(",") : null);

        this.enabled = (obj.has("enabled") ? obj.getBoolean("enabled") : true);
        this.optional = (obj.has("optional") ? obj.getBoolean("optional") : false);
        this.usage = (obj.has("usage") ? obj.getBoolean("usage") : true);
        this.notify = (obj.has("notify") ? obj.getBoolean("notify") : false);

        this.luaScript = obj.getString("luaScript");

        if (obj.has("settings")) {
            JSONArray jSettings = obj.getJSONArray("settings");
            this.settings = new String[jSettings.length()];
            for (int i = 0; i < jSettings.length(); i++)
                this.settings[i] = jSettings.getString(i);
        } else
            this.settings = null;

    }

    @Override
    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putString("id", this.getId());
        try {
            b.putString("definition", toJSON());
        }catch (JSONException e) {
            Log.e(TAG, "JsonException for xHook:\n" + e + "\n" + Log.getStackTraceString(e));
        }

        return b;
    }

    @Override
    public void fromBundle(Bundle bundle) {
        //this.name = bundle.getString("name");
        String json = bundle.getString("definition");
        if(json != null) {
            try {
                fromJSONObject(new JSONObject(json));
            }catch (JSONException e) {
                Log.e(TAG, "JsonException for Hook:\n" + e + "\n" + Log.getStackTraceString(e));
            }
        }
    }

    @Override
    public void fromParcel(Parcel in) {
        this.builtin = (in.readByte() != 0);
        this.collection = in.readString();
        this.group = in.readString();
        this.name = in.readString();
        this.author = in.readString();
        this.version = in.readInt();
        this.description = in.readString();
        this.className = in.readString();
        this.resolvedClassName = in.readString();
        this.methodName = in.readString();
        this.parameterTypes = in.createStringArray();
        this.returnType = in.readString();
        this.minSdk = in.readInt();
        this.maxSdk = in.readInt();
        this.minApk = in.readInt();
        this.maxApk = in.readInt();
        this.excludePackages = in.createStringArray();
        this.enabled = (in.readByte() != 0);
        this.optional = (in.readByte() != 0);
        this.usage = (in.readByte() != 0);
        this.notify = (in.readByte() != 0);
        this.luaScript = in.readString();
        this.settings = in.createStringArray();
        /*if(DebugUtil.isDebug()) {
            if(this.settings != null) Log.d(TAG, "Hook Settings Count From Parcel [" + this.settings.length + "] Id=" + this.getId());
            else Log.w(TAG, "Hook Id=" + this.getId() + " From Parcel has Null Settings...");
        }*/

    }

    public static final Parcelable.Creator<XLuaHook> CREATOR = new Parcelable.Creator<XLuaHook>() {
        @Override
        public XLuaHook createFromParcel(Parcel source) {
            return new XLuaHook(source);
        }

        @Override
        public XLuaHook[] newArray(int size) {
            return new XLuaHook[size];
        }
    };

    @Override
    public void setId(String id) {

    }

    public static class Table {
        public static final String name = "hook";
        public static final LinkedHashMap<String, String> columns = new LinkedHashMap<String, String>() {{
            put("id", "TEXT");
            put("definition", "TEXT");
        }};
    }
}
