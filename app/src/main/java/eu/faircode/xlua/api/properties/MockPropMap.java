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
import eu.faircode.xlua.api.xstandard.UserIdentityPacket;
import eu.faircode.xlua.api.xstandard.database.SqlQuerySnake;
import eu.faircode.xlua.api.xstandard.interfaces.IDBQuery;
import eu.faircode.xlua.api.xstandard.interfaces.IJsonSerial;
import eu.faircode.xlua.utilities.BundleUtil;
import eu.faircode.xlua.utilities.ContentValuesUtil;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.utilities.JSONUtil;

public class MockPropMap extends UserIdentityPacket implements IJsonSerial, IDBQuery, Parcelable {
    public static MockPropMap create() { return new MockPropMap(); }
    public static MockPropMap create(MockPropSetting setting) { return new MockPropMap(setting); }
    public static MockPropMap create(MockPropPacket packet) { return new MockPropMap(packet); }
    public static MockPropMap create(String name, String settingName) { return new MockPropMap(name, settingName); }

    protected String name;
    protected String settingName;

    public MockPropMap(MockPropPacket packet) { this(packet.getName(), packet.getSettingName()); }
    public MockPropMap(MockPropSetting setting) { this(setting.getName(), setting.getSettingName()); }

    public MockPropMap() { setUseUserIdentity(false); }
    public MockPropMap(Parcel in) {  this(); fromParcel(in);  }
    public MockPropMap(String name, String settingName) {
        this();
        setName(name);
        setSettingName(settingName);
    }

    public String getName() { return this.name; }
    public MockPropMap setName(String name) { if(name != null) this.name = name; return this; }

    public String getSettingName() { return this.settingName; }
    public MockPropMap setSettingName(String settingName) { if(settingName != null) this.settingName = settingName; return this; }

    @Override
    public Bundle toBundle() {
        Bundle b = super.toBundle();
        if(this.name != null) b.putString("name", this.name);
        if(this.settingName != null) b.putString("settingName", this.settingName);
        return b;
    }

    @Override
    public void fromBundle(Bundle b) {
        if(b != null) {
            super.fromBundle(b);
            this.name = BundleUtil.readString(b, "name");
            this.settingName = BundleUtil.readString(b, "settingName");
        }
    }

    @Override
    public ContentValues createContentValues() {
        ContentValues cv = super.createContentValues();
        if(this.name != null) cv.put("name", this.name);
        if(this.settingName != null) cv.put("settingName", this.settingName);
        return cv;
    }

    @Override
    public void fromContentValues(ContentValues contentValue) {
        if(contentValue != null) {
            super.fromContentValues(contentValue);
            this.name = ContentValuesUtil.getString(contentValue, "name");
            this.settingName = ContentValuesUtil.getString(contentValue, "settingName");
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
            this.name = CursorUtil.getString(cursor, "name");
            this.settingName = CursorUtil.getString(cursor, "settingName");
        }
    }

    @Override
    public void fromParcel(Parcel in) {
        if(in != null) {
            super.fromParcel(in);
            this.name = in.readString();
            this.settingName = in.readString();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if(dest != null) {
            super.writeToParcel(dest, flags);
            dest.writeString(this.name);
            dest.writeString(this.settingName);
        }
    }

    @Override
    public String toJSON() throws JSONException { return toJSONObject().toString(2); }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jRoot = super.toJSONObject();
        if(this.name != null) jRoot.put("name", this.name);
        if(this.settingName != null) jRoot.put("settingName", this.settingName);
        return jRoot;
    }

    @Override
    public void fromJSONObject(JSONObject obj) throws JSONException {
        super.fromJSONObject(obj);
        this.name = JSONUtil.getString(obj, "name");
        this.settingName = JSONUtil.getString(obj, "settingName");
    }

    @Override
    public SqlQuerySnake createQuery(XDatabaseOld db) { return SqlQuerySnake.create(db, Table.name).whereColumn("name", this.name); }

    @Override
    public int describeContents() { return 0; }

    public static final Parcelable.Creator<MockPropMap> CREATOR = new Parcelable.Creator<MockPropMap>() {
        @Override
        public MockPropMap createFromParcel(Parcel source) {
            return new MockPropMap(source);
        }

        @Override
        public MockPropMap[] newArray(int size) {
            return new MockPropMap[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof String) {
            String s = ((String)obj);
            return (this.name != null && this.name.equalsIgnoreCase(s)) || (this.settingName != null && this.settingName.equalsIgnoreCase(s));
            //return res || super.equals(s);
        }else if(obj instanceof MockPropMap) {
            MockPropMap map = ((MockPropMap)obj);
            return ((map.name != null && this.name != null) && this.name.equalsIgnoreCase(map.name)) ||
                    (map.settingName != null && this.settingName != null) && this.settingName.equalsIgnoreCase(map.settingName);
        } return false;
    }

    @NonNull
    @Override
    public String toString() {
        return new StringBuilder(super.toString())
                .append(" name=")
                .append(name)
                .append(" setting=")
                .append(settingName).toString();
    }

    public static class Table {
        public static final String name = "prop_maps";
        public static final LinkedHashMap<String, String> columns = new LinkedHashMap<String, String>() {{
            put("name", "TEXT PRIMARY KEY");
            put("settingName", "TEXT");
        }};
    }
}
