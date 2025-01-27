package eu.faircode.xlua.x.xlua.hook;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.utilities.ParcelUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.xlua.IBundleData;
import eu.faircode.xlua.x.xlua.PacketBase;
import eu.faircode.xlua.x.xlua.database.TableInfo;
import eu.faircode.xlua.x.xlua.database.sql.SQLQueryBuilder;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;
import eu.faircode.xlua.x.xlua.interfaces.IJsonType;
import eu.faircode.xlua.x.xlua.interfaces.IParcelType;

public class AssignmentPacket extends PacketBase implements IBundleData, IParcelType, IJsonType {
    public static final long NO_VALUE = -1;

    public static AssignmentPacket create(XLuaHook hook) { return new AssignmentPacket(hook); }

    protected boolean isLegacy = false;

    public String hook;
    public long installed;
    public long used;
    public boolean restricted;
    public String exception;
    public String oldValue;
    public String newValue;

    public XLuaHook hookObj;

    public static final String FIELD_USER = UserIdentityIO.FIELD_USER;
    public static final String FIELD_CATEGORY = UserIdentityIO.FIELD_CATEGORY;

    public static final String FIELD_HOOK = "hook";
    public static final String FIELD_INSTALLED = "installed";
    public static final String FIELD_USED = "used";
    public static final String FIELD_RESTRICTED = "restricted";
    public static final String FIELD_EXCEPTION = "exception";
    public static final String FIELD_OLD = "old";
    public static final String FIELD_NEW = "new";

    public static final String TABLE_NAME = "assignments";


    public static final TableInfo TABLE_INFO = TableInfo.create(TABLE_NAME)
            .putIdentification()
            .putText(FIELD_HOOK)
            .putInteger(FIELD_INSTALLED)
            .putInteger(FIELD_USED)
            .putInteger(FIELD_RESTRICTED)
            .putText(FIELD_EXCEPTION)
            .putText(FIELD_OLD)
            .putText(FIELD_NEW)
            .putPrimaryKey(true, FIELD_HOOK);

    public static LinkedHashMap<String, String> COLUMNS = TABLE_INFO.columns;


    @Override
    public String getId() { return hook; }

    @Override
    public void setId(String id) {
        this.hook = id;
    }

    public AssignmentPacket() { }
    public AssignmentPacket(Parcel in) { fromParcel(in); }
    public AssignmentPacket(XLuaHook hook) { setHook(hook); }

    public AssignmentPacket(AssignmentLegacy legacy) {
        this.hook = legacy.hook;
        this.installed = legacy.installed;
        this.used = legacy.used;
        this.restricted = legacy.restricted;
        this.exception = legacy.exception;
        this.oldValue = legacy.oldValue;
        this.newValue = legacy.newValue;
    }

    public void setHook(XLuaHook hook) {
        if(hook != null) {
            this.hookObj = hook;
            this.hook = hook.getId();
        }
    }

    @Override
    public void fromParcel(Parcel in) {
        if(in != null) {
            setUserIdentity(getUserIdentityFromParcel(in, isLegacy, !isLegacy));

            setHook(in.readParcelable(XLuaHook.class.getClassLoader()));

            this.installed = in.readLong();
            this.used = in.readLong();
            this.restricted = ParcelUtil.readBool(in);
            this.exception = in.readString();
            this.oldValue = in.readString();
            this.newValue = in.readString();
        }
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        writeUserIdentityToParcel(parcel, isLegacy, !isLegacy);

        parcel.writeParcelable(this.hookObj, flags);

        parcel.writeLong(this.installed);
        parcel.writeLong(this.used);
        ParcelUtil.writeBool(parcel, this.restricted);
        parcel.writeString(this.exception);
        parcel.writeString(this.oldValue);
        parcel.writeString(this.newValue);
    }

    @Override
    public void populateContentValues(ContentValues cv) {
        if(cv != null) {
            super.populateContentValues(cv);

            cv.put(FIELD_HOOK, this.hook);

            cv.put(FIELD_INSTALLED, this.installed);
            cv.put(FIELD_USED, this.used);
            cv.put(FIELD_RESTRICTED, this.restricted);
            cv.put(FIELD_EXCEPTION, this.exception);
            cv.put(FIELD_OLD, this.oldValue);
            cv.put(FIELD_NEW, this.newValue);
        }
    }

    @Override
    public void populateFromContentValues(ContentValues cv) {
        if(cv != null) {
            super.populateFromContentValues(cv);

            this.hook = cv.getAsString(FIELD_HOOK);

            this.installed = cv.getAsLong(FIELD_INSTALLED);
            this.used = cv.getAsLong(FIELD_USED);
            this.restricted = cv.getAsBoolean(FIELD_RESTRICTED);
            this.exception = cv.getAsString(FIELD_EXCEPTION);
            this.oldValue = cv.getAsString(FIELD_OLD);
            this.newValue = cv.getAsString(FIELD_NEW);
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
            UserIdentityIO.populateFromCursor_group_assignment(c, this, isLegacy);

            this.hook = CursorUtil.getString(c, FIELD_HOOK);

            this.installed = CursorUtil.getLong(c, FIELD_INSTALLED, NO_VALUE);
            this.used = CursorUtil.getLong(c, FIELD_USED, NO_VALUE);
            this.restricted = CursorUtil.getBoolean(c, FIELD_RESTRICTED, false);
            this.exception = CursorUtil.getString(c, FIELD_EXCEPTION);
            this.oldValue = CursorUtil.getString(c, FIELD_OLD);
            this.newValue = CursorUtil.getString(c, FIELD_NEW);
        }
    }

    @Override
    public void populateSnake(SQLQueryBuilder snake) {
        //ToDO
    }

    @Override
    public void populateFromBundle(Bundle b) {
        if(b != null) {
            super.populateFromBundle(b);

            this.hook = b.getString(FIELD_HOOK);

            this.installed = b.getLong(FIELD_INSTALLED, NO_VALUE);
            this.used = b.getLong(FIELD_USED, NO_VALUE);
            this.restricted = b.getBoolean(FIELD_RESTRICTED);
            this.exception = b.getString(FIELD_EXCEPTION);
            this.oldValue = b.getString(FIELD_OLD);
            this.newValue = b.getString(FIELD_NEW);
        }
    }

    @Override
    public void populateBundle(Bundle b) {
        if(b != null) {
            super.populateBundle(b);

            b.putString(FIELD_HOOK, this.hook);

            b.putLong(FIELD_INSTALLED, this.installed);
            b.putLong(FIELD_USED, this.used);
            b.putBoolean(FIELD_RESTRICTED, this.restricted);
            b.putString(FIELD_EXCEPTION, this.exception);
            b.putString(FIELD_OLD, this.oldValue);
            b.putString(FIELD_NEW, this.newValue);
        }
    }

    @Override
    public Bundle toBundle() {
        Bundle b = new Bundle();
        populateBundle(b);
        return b;
    }

    @Override
    public String toJSONString() throws JSONException { return toJSONObject().toString(); }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();

            obj.put(FIELD_HOOK, this.hook);

        obj.put(FIELD_INSTALLED, this.installed);
        obj.put(FIELD_USED, this.used);
        obj.put(FIELD_RESTRICTED, this.restricted);
        obj.put(FIELD_EXCEPTION, this.exception);
        obj.put(FIELD_OLD, this.oldValue);
        obj.put(FIELD_NEW, this.newValue);

        return obj;
    }

    @Override
    public void fromJSONObject(JSONObject obj) throws JSONException {
        if(obj != null) {
            this.hook = obj.optString(FIELD_HOOK);

            this.installed = obj.optLong(FIELD_INSTALLED);
            this.used = obj.optLong(FIELD_USED);
            this.restricted = obj.optBoolean(FIELD_RESTRICTED);
            this.exception = obj.optString(FIELD_EXCEPTION);
            this.oldValue = obj.optString(FIELD_OLD);
            this.newValue = obj.optString(FIELD_NEW);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Hook", this.hook)
                .appendFieldLine("Installed", this.installed)
                .appendFieldLine("Used", this.used)
                .appendFieldLine("Restricted", this.restricted)
                .appendFieldLine("Exception", this.exception)
                .appendFieldLine("Old", this.oldValue)
                .appendFieldLine("New", this.newValue)
                .appendFieldLine("Hook Object=", Str.toStringOrNull(this.hookObj))
                .toString(true);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Parcelable.Creator<AssignmentPacket> CREATOR = new Parcelable.Creator<AssignmentPacket>() {
        @Override
        public AssignmentPacket createFromParcel(Parcel source) { return new AssignmentPacket(source); }
        @Override
        public AssignmentPacket[] newArray(int size) { return new AssignmentPacket[size]; }
    };
}
