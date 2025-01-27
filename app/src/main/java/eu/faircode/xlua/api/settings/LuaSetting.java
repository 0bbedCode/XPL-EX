package eu.faircode.xlua.api.settings;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;

import eu.faircode.xlua.api.xstandard.UserIdentityPacket;
import eu.faircode.xlua.api.xstandard.interfaces.IJsonSerial;
import eu.faircode.xlua.utilities.ContentValuesUtil;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.utilities.JSONUtil;
import eu.faircode.xlua.utilities.ParcelUtil;
import eu.faircode.xlua.x.xlua.settings.deprecated.SettingsHelperOld;

public class LuaSetting extends UserIdentityPacket implements IJsonSerial, Parcelable {
    public static LuaSetting create() { return new LuaSetting(); }
    public static LuaSetting create(String name) { return new LuaSetting(null, null, name, null); }
    public static LuaSetting create(Integer userId, String category, String name) { return new LuaSetting(userId, category, name, null); }
    public static LuaSetting create(Integer userId, String category, String name, String value) { return new LuaSetting(userId, category, name, value); }

    public static LuaSetting create(LuaSettingDefault defaultSetting) { return new LuaSetting(defaultSetting); }
    public static LuaSetting create(LuaSettingDefault defaultSetting, Integer user, String category) { return new LuaSetting(defaultSetting, user, category); }
    public static LuaSetting create(LuaSettingExtended setting) { return new LuaSetting(setting); }
    public static LuaSetting create(LuaSettingPacket packet) { return new LuaSetting(packet); }

    protected String name;
    protected String value;

    public LuaSetting(LuaSettingPacket packet) { this(packet.getUser(), packet.getCategory(), packet.getName(), packet.getValue()); }
    public LuaSetting(LuaSettingExtended setting) { this(setting.getUser(), setting.getCategory(), setting.getName(), setting.getValue()); }
    public LuaSetting(LuaSettingDefault defaultSetting) { this(defaultSetting.getUser(), defaultSetting.getCategory(), defaultSetting.getName(), defaultSetting.getDefaultValue(true)); }
    public LuaSetting(LuaSettingDefault defaultSetting, Integer user, String category) { this(user, category, defaultSetting.getName(), defaultSetting.getDefaultValue(true)); }

    public LuaSetting() { setUseUserIdentity(true); }
    public LuaSetting(Parcel p) { this(); fromParcel(p); }
    public LuaSetting(Integer user, String category, String name, String value) {
        this();
        setUser(user);
        setCategory(category);
        setName(name);
        setValue(value);
    }

    public String getName() { return this.name; }
    public LuaSetting setName(String name) { if(name != null) this.name = name; return this; }

    public String getValue() { return this.value; }
    public LuaSetting setValue(String value) { if(value != null) this.value = value; return this; }
    public LuaSetting setValueForce(String value) { this.value = value; return this; }
    public LuaSetting setValueToNull() { this.value = null; return this; }

    public boolean isValueNull() { return this.value == null; }
    public boolean isValueEmpty() { return this.value != null && TextUtils.isEmpty(this.value); }
    public boolean isValueEmptyOrNull() { return isValueNull() || isValueEmpty(); }

    public boolean isBuiltIn() { return SettingsHelperOld.isBuiltInSetting(this.name); }

    public LuaSettingDefault createDefault(String description) { return LuaSettingDefault.create(this, description); }
    public LuaSettingExtended createExtended(String description) { return LuaSettingExtended.create(this, description); }
    public LuaSettingPacket createPacket(Integer code, Boolean kill) { return LuaSettingPacket.create(this, code, kill); }

    @Override
    public Bundle toBundle() {
        Bundle b = super.toBundle();
        if(this.name != null) b.putString("name", this.name);
        if(this.value != null) b.putString("value", this.value);
        return b;
    }

    @Override
    public void fromBundle(Bundle b) {
        if(b != null) {
            super.fromBundle(b);
            this.name = b.getString("name");
            this.value = b.getString("value");
        }
    }

    @Override
    public void fromParcel(Parcel in) {
        if(in != null) {
            super.fromParcel(in);
            this.name = in.readString();
            this.value = ParcelUtil.readString(in, null, ParcelUtil.IGNORE_VALUE);
        }
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if(dest != null) {
            super.writeToParcel(dest, flags);
            dest.writeString(this.name);
            ParcelUtil.writeString(dest, this.value, ParcelUtil.IGNORE_VALUE, false);
        }
    }

    @Override
    public void fromCursor(Cursor cursor) {
        if(cursor != null) {
            super.fromCursor(cursor);
            this.name = CursorUtil.getString(cursor, "name");
            this.value = CursorUtil.getString(cursor, "value");
        }
    }

    @Override
    public ContentValues createContentValues() {
        ContentValues cv = super.createContentValues();
        if(this.name != null) cv.put("name", this.name);
        if(this.value != null) cv.put("value", this.value);
        return cv;
    }

    @Override
    public List<ContentValues> createContentValuesList() { return null; }

    @Override
    public void fromContentValuesList(List<ContentValues> contentValues) { }

    @Override
    public void fromContentValues(ContentValues contentValue) {
        if(contentValue != null) {
            super.fromContentValues(contentValue);
            this.name = ContentValuesUtil.getString(contentValue, "name");
            this.value = ContentValuesUtil.getString(contentValue, "value");
        }
    }

    @Override
    public String toJSON() throws JSONException { return toJSONObject().toString(2); }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj = super.toJSONObject();
        if(this.name != null) obj.put("name", this.name);
        if(this.value != null) obj.put("value", this.value);
        return obj;
    }

    @Override
    public void fromJSONObject(JSONObject obj) throws JSONException {
        if(obj != null) {
            super.fromJSONObject(obj);
            this.name = JSONUtil.getString(obj, "name");
            this.value = JSONUtil.getString(obj, "value");
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(getName() == null) return false;

        String name = null;
        if(obj instanceof String) name = (String)obj;
        else if(obj instanceof LuaSetting) name = ((LuaSetting) obj).getName();
        if(name == null) return false;
        return this.getName().equalsIgnoreCase(name);
    }

    @NonNull
    @Override
    public String toString() {
        return new StringBuilder(super.toString())
                .append(" name=")
                .append(this.name)
                .append(" value=")
                .append(this.value).toString();
    }

    public static final Parcelable.Creator<LuaSetting> CREATOR = new Parcelable.Creator<LuaSetting>() {
        @Override
        public LuaSetting createFromParcel(Parcel source) {
            return new LuaSetting(source);
        }

        @Override
        public LuaSetting[] newArray(int size) {
            return new LuaSetting[size];
        }
    };

    public static class Table {
        public static final String NAME = "setting";

        public static final String FIELD_USER = "user";
        public static final String FIELD_CATEGORY = "category";
        public static final String FIELD_NAME = "name";
        public static final String FIELD_VALUE = "value";

        public static final LinkedHashMap<String, String> columns = new LinkedHashMap<String, String>() {{
            put(FIELD_USER, "INTEGER");
            put(FIELD_CATEGORY, "TEXT");
            put(FIELD_NAME, "TEXT");
            put(FIELD_VALUE, "TEXT");
        }};
    }
}
