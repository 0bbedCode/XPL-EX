package eu.faircode.xlua.x.xlua.settings.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.xlua.IBundleData;
import eu.faircode.xlua.x.xlua.PacketBase;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.database.IDatabaseEntry;
import eu.faircode.xlua.x.xlua.database.TableInfo;
import eu.faircode.xlua.x.xlua.database.sql.SQLQueryBuilder;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;
import eu.faircode.xlua.x.xlua.interfaces.IJsonType;
import eu.faircode.xlua.x.xlua.interfaces.IParcelType;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;

public class SettingPacket extends PacketBase implements IDatabaseEntry, IBundleData, IParcelType, IJsonType {
    public static SettingPacket create(String name, String value, UserClientAppContext userContext, ActionPacket actionPacket) { return new SettingPacket(name, value, userContext, actionPacket); }
    public static SettingPacket create(String name, String value, ActionPacket actionPacket, UserIdentity userIdentity) { return new SettingPacket(name, value, actionPacket, userIdentity); }

    public String name;
    public String value;
    public String description;

    public static final int DEFAULT_USER = UserIdentity.DEFAULT_USER;
    public static final String GLOBAL_CATEGORY = UserIdentity.GLOBAL_NAMESPACE;

    public static final String FIELD_USER = UserIdentityIO.FIELD_USER;
    public static final String FIELD_CATEGORY = UserIdentityIO.FIELD_CATEGORY;

    public static final String FIELD_NAME = "name";
    public static final String FIELD_VALUE = "value";
    public static final String FIELD_DESCRIPTION = SettingInfoPacket.FIELD_DESCRIPTION;
    public static final String TABLE_NAME = "settings";

    public static final TableInfo TABLE_INFO = TableInfo.create(TABLE_NAME)
            .putIdentification()
            .putText(FIELD_NAME)
            .putText(FIELD_VALUE)
            .putPrimaryKey(true, FIELD_NAME);

    public static final LinkedHashMap<String, String> COLUMNS = TABLE_INFO.columns;

    @Override
    public String getId() { return name; }

    @Override
    public void setId(String id) {
        this.name = id;
    }

    public SettingPacket() {  }
    public SettingPacket(SettingHolder from) {
        this.name = from.getName();
        this.value = from.getValue();
    }

    public SettingPacket(SettingHolder from, UserClientAppContext userContext, ActionPacket actionPacket) {
        this.setUserIdentity(new UserIdentity(userContext.appUid, userContext.appPackageName));
        this.setActionPacket(actionPacket);
        this.name = from.getName();
        this.value = from.getNewValue();
    }

    public SettingPacket(String name, String value, UserClientAppContext userContext, ActionPacket actionPacket) {
        this.setUserIdentity(new UserIdentity(userContext.appUid, userContext.appPackageName));
        this.setActionPacket(actionPacket);
        this.name = name;
        this.value = value;
    }

    public SettingPacket(Parcel in) {
        fromParcel(in);
    }

    public SettingPacket(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public SettingPacket(String name, String value, ActionPacket actionPacket, UserIdentity userIdentity) {
        this.name = name;
        this.value = value;
        setActionPacket(actionPacket);
        setUserIdentity(userIdentity);
    }

    @Override
    public void fromParcel(Parcel in) {
        setUserIdentity(UserIdentity.fromUid(in.readInt(), in.readString()));

        this.name = in.readString();
        this.value = in.readString();
        this.description = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(UserIdentity.ensureValidUid(getUid()));
        parcel.writeString(UserIdentity.ensureValidCategory(getCategory()));

        parcel.writeString(Str.getNonNullString(name, "error"));
        //parcel.writeString(Str.getNonNullString(value, ""));
        parcel.writeString(value);
        parcel.writeString(Str.getNonNullString(description, "N/A"));
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        populateContentValues(cv);
        return cv;
    }

    @Override
    public void populateContentValues(ContentValues cv) {
        if(cv != null) {
            super.populateContentValues(cv);
            cv.put(FIELD_NAME, this.name);
            cv.put(FIELD_VALUE, this.value);
        }
    }

    @Override
    public void populateFromContentValues(ContentValues cv) {
        if(cv != null) {
            super.populateFromContentValues(cv);
            this.name = cv.getAsString(FIELD_NAME);
            this.value = cv.getAsString(FIELD_VALUE);
        }
    }

    @Override
    public void fromCursor(Cursor c) {
        if(c != null) {
            setUserIdentity(UserIdentity.create(c));
            this.name = CursorUtil.getString(c, FIELD_NAME, null);
            this.value = CursorUtil.getString(c, FIELD_VALUE, null);
        }
    }

    @Override
    public void populateSnake(SQLQueryBuilder snake) {
        //TODO
    }


    @Override
    public void populateFromBundle(Bundle b) {
        if(b != null) {
            super.populateFromBundle(b);
            this.name = b.getString(FIELD_NAME);
            this.value = b.getString(FIELD_VALUE);
        }
    }

    @Override
    public void populateBundle(Bundle b) {
        if(b != null) {
            super.populateBundle(b);
            b.putString(FIELD_NAME, this.name);
            b.putString(FIELD_VALUE, this.value);
        }
    }

    @Override
    public Bundle toBundle() {
        Bundle b = new Bundle();
        populateBundle(b);
        return b;
    }

    public SettingPacket sendCallRequest(Context context, String command) {
        Bundle res = XProxyContent.luaCall(context, command, toBundle());
        if(res != null) populateFromBundle(res);
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Name", this.name)
                .appendFieldLine("Value", this.value)
                .appendLine(Str.toStringOrNull(getUserIdentity()))
                .appendLine(Str.toStringOrNull(getActionPacket()))
                .toString(true);
    }

    /*public static final LinkedHashMap<String, String> COLUMNS = new LinkedHashMap<String, String>() {{
        put(UserIdentityIO.FIELD_USER, "INTEGER");
        put(UserIdentityIO.FIELD_CATEGORY, "TEXT");
        put(FIELD_NAME, "TEXT");
        put(FIELD_VALUE, "TEXT");
        put("PRIMARY", "KEY(" + UserIdentityIO.FIELD_USER + ", " + UserIdentityIO.FIELD_CATEGORY + ", " + FIELD_NAME + ")");//KEY(user, category, receiverName)
    }};*/


    public static final ListUtil.IMapItem<SettingPacket, String, String> TO_MAP = new ListUtil.IMapItem<SettingPacket, String, String>() {
        @Override
        public boolean isValid(SettingPacket settingPacket) { return !TextUtils.isEmpty(settingPacket.name); }

        @Override
        public String getKey(SettingPacket settingPacket) { return settingPacket.name; }

        @Override
        public String getValue(SettingPacket settingPacket) { return settingPacket.value; }
    };

    @Override
    public int describeContents() { return 0; }

    public static final Parcelable.Creator<SettingPacket> CREATOR = new Parcelable.Creator<SettingPacket>() {
        @Override
        public SettingPacket createFromParcel(Parcel source) { return new SettingPacket(source); }
        @Override
        public SettingPacket[] newArray(int size) { return new SettingPacket[size]; }
    };

    //ToDO
    @Override
    public String toJSONString() throws JSONException {
        return "";
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        return null;
    }

    @Override
    public void fromJSONObject(JSONObject obj) throws JSONException {

    }
}
