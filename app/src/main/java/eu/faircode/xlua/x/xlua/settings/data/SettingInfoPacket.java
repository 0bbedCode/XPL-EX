package eu.faircode.xlua.x.xlua.settings.data;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import eu.faircode.xlua.utilities.ContentValuesUtil;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.xlua.database.IDatabaseEntry;
import eu.faircode.xlua.x.xlua.database.TableInfo;
import eu.faircode.xlua.x.xlua.database.sql.SQLQueryBuilder;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;
import eu.faircode.xlua.x.xlua.interfaces.ICursorType;
import eu.faircode.xlua.x.xlua.interfaces.IJsonType;
import eu.faircode.xlua.x.xlua.settings.interfaces.INameResolver;

public class SettingInfoPacket implements IDatabaseEntry, IJsonType, INameResolver, ICursorType, IIdentifiableObject {
    public static final String FIELD_USER = UserIdentityIO.FIELD_USER;
    public static final String FIELD_NAME = "name";
    public static final String FIELD_DEFAULT_VALUE = "defaultValue";
    public static final String FIELD_DESCRIPTION = "description";

    public static final String TABLE_NAME = "default_settings";

    public static final String JSON = "settingdefaults.json";

    public static final TableInfo TABLE_INFO = TableInfo.create(TABLE_NAME)
            .putInteger(FIELD_USER)
            .putText(FIELD_NAME)
            .putText(FIELD_DEFAULT_VALUE)
            .putText(FIELD_DESCRIPTION)
            .putPrimaryKey(FIELD_USER, FIELD_NAME);

    public int userId = 0;
    public String name;
    public String defaultValue;
    public String description;

    public boolean wasModified = false;

    public SettingInfoPacket() { }
    public SettingInfoPacket(String name, String defaultValue, String description) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    @Override
    public void populateContentValues(ContentValues cv) {
        if(cv != null) {
            cv.put(FIELD_USER, this.userId);
            cv.put(FIELD_NAME, this.name);
            cv.put(FIELD_DEFAULT_VALUE, this.defaultValue);
            cv.put(FIELD_DESCRIPTION, this.description);
        }
    }

    @Override
    public void populateFromContentValues(ContentValues cv) {
        if(cv != null) {
            this.userId = ContentValuesUtil.getInteger(cv, FIELD_USER, 0);
            this.name = ContentValuesUtil.getString(cv, FIELD_NAME);
            this.defaultValue = ContentValuesUtil.getString(cv, FIELD_DEFAULT_VALUE);
            this.description = ContentValuesUtil.getString(cv, FIELD_DESCRIPTION);
        }
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        populateContentValues(cv);
        return cv;
    }

    @Override
    public void fromCursor(Cursor c) {
        if(c != null) {
            this.userId = CursorUtil.getInteger(c, FIELD_USER, 0);
            this.name = CursorUtil.getString(c, FIELD_NAME);
            this.defaultValue = CursorUtil.getString(c, FIELD_DEFAULT_VALUE);
            this.description = CursorUtil.getString(c, FIELD_DESCRIPTION);
        }
    }

    @Override
    public void populateSnake(SQLQueryBuilder snake) {
        //ToDO
    }

    @Override
    public String toJSONString() throws JSONException { return toJSONObject().toString(); }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(FIELD_USER, this.userId > -1 ? this.userId : 0);
        jsonObject.put(FIELD_NAME, this.name);
        jsonObject.put(FIELD_DEFAULT_VALUE, this.defaultValue);
        jsonObject.put(FIELD_DESCRIPTION, this.description);
        return jsonObject;
    }

    @Override
    public void fromJSONObject(JSONObject obj) throws JSONException {
        if(obj != null) {
            this.userId = obj.optInt(FIELD_USER, 0);
            this.name = obj.optString(FIELD_NAME);
            this.defaultValue = obj.optString(FIELD_DEFAULT_VALUE);
            this.description = obj.optString(FIELD_DESCRIPTION);
        }
    }

    @Override
    public boolean ensureNamed(HashMap<String, String> map) {
        if(map == null) return false;
        String newName = map.get(this.name);
        if(newName != null) this.name = newName;
        return newName != null;
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine(FIELD_USER, this.userId)
                .appendFieldLine("Name", this.name)
                .appendFieldLine("Default Value", this.defaultValue)
                .appendFieldLine("Description", this.description)
                .toString(true);
    }


    @Override
    public String getSharedId() {
        return name;
    }

    @Override
    public void setId(String id) {
        this.name = id;
    }

    @Override
    public boolean consumeId(Object o) {
        boolean wasConsumed = false;
        if(o instanceof SettingInfoPacket) {
            SettingInfoPacket other = (SettingInfoPacket) o;
            if(other.getSharedId().equalsIgnoreCase(this.getSharedId()) && !Str.areEqualIgnoreCase(this.description, other.description)) {
                this.description = other.description;
                wasConsumed = true;
            }
        }

        return wasConsumed;
    }

    @Override
    public String getCategory() {
        return IIdentifiableObject.super.getCategory();
    }
}
