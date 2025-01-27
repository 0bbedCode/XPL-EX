package eu.faircode.xlua.api.properties;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;

import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.api.xstandard.database.SqlQuerySnake;
import eu.faircode.xlua.api.xstandard.interfaces.IDBQuery;
import eu.faircode.xlua.api.xstandard.interfaces.IJsonSerial;
import eu.faircode.xlua.utilities.BundleUtil;
import eu.faircode.xlua.utilities.ContentValuesUtil;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.utilities.JSONUtil;

public class MockPropSetting extends MockPropMap implements IJsonSerial, IDBQuery, Parcelable {
    public static final int PROP_HIDE = 0x3;
    public static final int PROP_SKIP = 0x4;
    public static final int PROP_FORCE = 0x5;
    public static final int PROP_NULL = 0x8;

    //public static boolean useProperty(Integer code) { return code == null || (code != MockPropSetting.PROP_HIDE && code != MockPropSetting.PROP_SKIP); }

    public static MockPropSetting create() { return new MockPropSetting(); }
    public static MockPropSetting create(MockPropPacket packet) { return new MockPropSetting(packet); }
    public static MockPropSetting create(String propertyName) { return new MockPropSetting(null, null, propertyName, null, null); }
    public static MockPropSetting create(String propertyName, String settingName) { return new MockPropSetting(null, null, propertyName, settingName, null); }
    public static MockPropSetting create(String propertyName, String settingName, Integer value) { return new MockPropSetting(null, null, propertyName, settingName, value); }
    public static MockPropSetting create(Integer user, String category, String propertyName, String settingName, Integer value) { return new MockPropSetting(user, category, propertyName, settingName, value); }

    protected Integer value;

    public MockPropSetting(MockPropGroupHolder holder, String name, Integer value) { this( holder.getUser(), holder.getPackageName(), name, holder.getSettingName(), value); }
    public MockPropSetting(MockPropPacket packet) { this(packet.getUser(), packet.getCategory(), packet.getName(), packet.getSettingName(), packet.getValue()); }
    public MockPropSetting(MockPropMap map, Integer value) { this(null, null, map.getName(), map.getCategory(), value); }
    public MockPropSetting(MockPropMap map, Integer user, String category, Integer value) { this(user, category, map.getName(), map.getCategory(), value); }

    public MockPropSetting() { setUseUserIdentity(true); }
    public MockPropSetting(ContentValues cv) {  this(); fromContentValues(cv); }
    public MockPropSetting(Parcel in) { this(); fromParcel(in); }
    public MockPropSetting(Integer user, String category, String propertyName, String settingName, Integer value) {
        this();
        setUser(user);
        setCategory(category);
        setName(propertyName);
        setSettingName(settingName);
        setValue(value);
    }

    public MockPropMap createMap() { return MockPropMap.create(this); }
    public MockPropPacket createPacket() { return MockPropPacket.create(this); }
    public MockPropPacket createPacket(Integer code) { return MockPropPacket.create(this, code); }

    public Integer getValue() { return value; }
    public MockPropSetting setValue(Integer value) { if(value != null) this.value = value; return this; }

    public boolean isSkip() { return !isNullOrEmptyValue() && this.value == PROP_SKIP; }
    public boolean isHide() { return !isNullOrEmptyValue() && this.value == PROP_HIDE;}
    public boolean isForce() { return !isNullOrEmptyValue() && this.value == PROP_FORCE; }
    public boolean isNullOrEmptyValue() { return this.value == null || this.value == PROP_NULL; }

    @Override
    public Bundle toBundle() {
        Bundle b = super.toBundle();
        if(this.value != null) b.putInt("value", this.value);
        return b;
    }

    @Override
    public void fromBundle(Bundle b) {
        if(b != null) {
            super.fromBundle(b);
            this.value = BundleUtil.readInteger(b, "value", PROP_HIDE);
        }
    }

    @Override
    public ContentValues createContentValues() {
        ContentValues cv = super.createContentValues();
        if(this.value != null) cv.put("value", this.value);
        return cv;
    }

    @Override
    public void fromContentValues(ContentValues contentValue) {
        if(contentValue != null) {
            super.fromContentValues(contentValue);
            this.value = ContentValuesUtil.getInteger(contentValue, "value", PROP_NULL);
        }
    }

    @Override
    public List<ContentValues> createContentValuesList() { return null;  }

    @Override
    public void fromContentValuesList(List<ContentValues> contentValues) { }

    @Override
    public void fromCursor(Cursor cursor) {
        if(cursor != null) {
            super.fromCursor(cursor);
            this.value = CursorUtil.getInteger(cursor, "value", PROP_NULL);
        }
    }

    @Override
    public void fromParcel(Parcel in) {
        if(in != null) {
            super.fromParcel(in);
            this.value = in.readInt();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if(dest != null) {
            super.writeToParcel(dest, flags);
            if(this.value == null) this.value = PROP_NULL;
            dest.writeInt(this.value);
        }
    }

    @Override
    public String toJSON() throws JSONException { return toJSONObject().toString(2); }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jRoot = super.toJSONObject();
        if(this.value != null) jRoot.put("value", this.value);
        return jRoot;
    }

    @Override
    public void fromJSONObject(JSONObject obj) throws JSONException {
        if(obj != null) {
            super.fromJSONObject(obj);
            this.value = JSONUtil.getInteger(obj, "value", PROP_HIDE);
        }
    }

    @Override
    public SqlQuerySnake createQuery(XDatabaseOld db) {
        ensureIdentification();
        return SqlQuerySnake.create(db, MockPropSetting.Table.NAME)
                .whereColumn("user", this.user)
                .whereColumn("category", this.category)
                .whereColumn("name", this.name);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Parcelable.Creator<MockPropSetting> CREATOR = new Parcelable.Creator<MockPropSetting>() {
        @Override
        public MockPropSetting createFromParcel(Parcel source) { return new MockPropSetting(source); }
        @Override
        public MockPropSetting[] newArray(int size) { return new MockPropSetting[size]; }
    };

    @NonNull
    @Override
    public String toString() {
        return new StringBuilder(super.toString())
                .append(" value=")
                .append(value).toString();
    }

    public static class Table {
        public static final String NAME = "prop_settings";
        public static final LinkedHashMap<String, String> COLUMNS = new LinkedHashMap<String, String>() {{
            put("user", "INTEGER");
            put("category", "TEXT");
            put("name", "TEXT PRIMARY KEY");
            put("settingName", "TEXT");
            put("value", "INTEGER");
        }};
    }
}
