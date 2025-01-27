package eu.faircode.xlua.api.properties;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.api.xstandard.database.SqlQuerySnake;
import eu.faircode.xlua.api.xstandard.interfaces.IDBQuery;
import eu.faircode.xlua.api.xstandard.interfaces.IJsonSerial;
import eu.faircode.xlua.utilities.StringUtil;

public class MockPropGroupHolder implements IJsonSerial, IDBQuery {
    protected Integer user;
    protected String packageName;
    protected String settingName;
    protected String value;
    protected String description;
    protected LuaSettingExtended luaSetting;
    protected List<MockPropSetting> properties;

    public MockPropGroupHolder() {  }
    public MockPropGroupHolder(String settingName) { this(settingName, new ArrayList<MockPropSetting>()); }
    public MockPropGroupHolder(String settingName, List<MockPropSetting> properties) {
        setSettingName(settingName);
        setProperties(properties);
    }

    public LuaSettingExtended getSetting() { return luaSetting; }
    public MockPropGroupHolder setSetting(LuaSettingExtended setting) { if(setting != null) this.luaSetting = setting; return this; }

    public String getDescription() { return description; }
    public MockPropGroupHolder setDescription(String description) { if(description != null) this.description = description; return this; }

    public Integer getUser() { return user; }
    public MockPropGroupHolder setUser(Integer user) { if(user != null) this.user = user; return this; }

    public String getPackageName() { return packageName; }
    public MockPropGroupHolder setPackageName(String packageName) { if(packageName != null) this.packageName = packageName; return this; }

    public String getSettingName() { return settingName; }
    public MockPropGroupHolder setSettingName(String settingName) { if(settingName != null) this.settingName = settingName; return this; }

    public String getValue() { return value; }
    public MockPropGroupHolder setValue(String value) { if(value != null) this.value = value; return this; }

    public List<MockPropSetting> getProperties() { return properties; }
    public MockPropGroupHolder setProperties(List<MockPropSetting> properties) { if(properties != null) this.properties = properties; return this; }
    public MockPropGroupHolder addProperty(MockPropSetting property) {
        if(properties == null)
            properties = new ArrayList<>();

        if(!StringUtil.isValidString(settingName))
            this.settingName = property.settingName;

        //if(!property.getSettingName().equalsIgnoreCase(settingName))
        //    property.setSettingName(settingName);

        if(!properties.isEmpty()) {
            for(MockPropSetting m : properties) {
                if(m.getName().equalsIgnoreCase(property.getName()))
                    return this;
            }
        }

        properties.add(property);
        return this;
    }

    @Override
    public Bundle toBundle() { return null; }

    @Override
    public void fromBundle(Bundle b) { }

    @Override
    public void fromContentValues(ContentValues contentValue) { }

    @Override
    public ContentValues createContentValues() { return null; }

    @Override
    public List<ContentValues> createContentValuesList() {
        List<ContentValues> cvs = new ArrayList<>();
        for(MockPropMap setting : properties) {
            MockPropMap map = new MockPropMap(setting.getName(),  setting.getSettingName());
            cvs.add(map.createContentValues());
        }

        return cvs;
    }

    @Override
    public void fromContentValuesList(List<ContentValues> contentValues) {
        List<MockPropSetting> props = new ArrayList<>();
        for(ContentValues cv : contentValues)
            props.add(new MockPropSetting(cv));

        properties = props;
    }

    @Override
    public void fromCursor(Cursor cursor) {

    }

    //I want to expand more on "to" functions , I want to pass through "flags" for instance
    //This can read JSON from Assets, and JSON from just marshall
    //public void fromCursor(Cursor cursor, boolean marshall) { }

    ///public Cursor toCursor(boolean marshall, int flags)  {  }

    @Override
    public void fromParcel(Parcel in) { }

    @Override
    public void writeToParcel(Parcel dest, int flags) { }

    @Override
    public String toJSON() throws JSONException { return toJSONObject().toString(2); }

    @Override
    public SqlQuerySnake createQuery(XDatabaseOld db) {
        return null;
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jRoot = new JSONObject();
        if(this.settingName != null) jRoot.put("settingName", settingName);
        if(this.properties != null) jRoot.put("propNames", new JSONArray(MockPropConversions.propsListToStringList(properties)));
        return jRoot;
    }

    @Override
    public void fromJSONObject(JSONObject obj) throws JSONException {
        this.settingName = obj.getString("settingName");
        this.properties = MockPropConversions.getPropertiesFromJSON(obj);
    }

    @NonNull
    @Override
    public String toString() {
        return new StringBuilder()
                .append(" setting=")
                .append(settingName)
                .append(" property size=")
                .append(properties.size())
                .append(" user=")
                .append(user)
                .append(" pkg=")
                .append(packageName)
                .append(" value=")
                .append(value).toString();
    }
}
