package eu.faircode.xlua.x.xlua.hook;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.utilities.ParcelUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.xlua.IBundleData;
import eu.faircode.xlua.x.xlua.database.IDatabaseEntry;
import eu.faircode.xlua.x.xlua.database.TableInfo;
import eu.faircode.xlua.x.xlua.database.sql.SQLQueryBuilder;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;
import eu.faircode.xlua.x.xlua.interfaces.IJsonType;
import eu.faircode.xlua.x.xlua.interfaces.IParcelType;

public class AppAssignment implements IBundleData, IParcelType, IJsonType, IDatabaseEntry, IIdentifiableObject {
    public static final long NO_VALUE = -1;

    //public static AppAssignment create(XLuaHook hook) { return new AppAssignment(hook); }

    public int user;
    public String category;

    public long installed;
    public long used;
    public boolean restricted;
    public String exception;
    public String oldValue;
    public String newValue;

    private String hookId;
    private XLuaHook hookObj;

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


    public String getHookId() { return hookId; }

    //    //            XLuaHook hook = UberCore888.getHook(assignment.getHookId(), assignment.getCategory(), collections);


    public void setHook(XLuaHook hook) {
        if(hook != null) {
            this.hookObj = hook;
            this.hookId = hook.getObjectId();
        }
    }

    public XLuaHook getHook() {
        //UberCore888.getHook()
        //Find out how its resolved
        return null;
    }

    @Override
    public String getObjectId() { return hookId; }

    public AppAssignment() { }
    public AppAssignment(Parcel in) { fromParcel(in); }
    //public AppAssignment(XLuaHook hook) { setHook(hook); }


    @Override
    public void fromParcel(Parcel in) {
        if(in != null) {

            this.user = in.readInt();
            this.category = in.readString();

            //setHook(in.readParcelable(XLuaHook.class.getClassLoader()));

            this.installed = in.readLong();
            this.used = in.readLong();
            this.restricted = ParcelUtil.readBool(in);
            this.exception = in.readString();
            this.oldValue = in.readString();
            this.newValue = in.readString();

            this.hookId = in.readString();
        }
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {


        parcel.writeInt(this.user);
        parcel.writeString(this.category);

        parcel.writeParcelable(this.hookObj, flags);

        parcel.writeLong(this.installed);
        parcel.writeLong(this.used);
        ParcelUtil.writeBool(parcel, this.restricted);
        parcel.writeString(this.exception);
        parcel.writeString(this.oldValue);
        parcel.writeString(this.newValue);

        parcel.writeString(getHookId());

    }

    @Override
    public void populateContentValues(ContentValues cv) {
        if(cv != null) {
            cv.put(FIELD_USER, this.user);
            cv.put(FIELD_CATEGORY, this.category);
            cv.put(FIELD_HOOK, this.hookId);
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
            this.user = cv.getAsInteger(FIELD_USER);
            this.category = cv.getAsString(FIELD_CATEGORY);
            this.hookId = cv.getAsString(FIELD_HOOK);
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
            this.user = CursorUtil.getInteger(c, FIELD_USER, -1);
            this.category = CursorUtil.getString(c, FIELD_CATEGORY, UserIdentity.GLOBAL_NAMESPACE);

            this.hookId = CursorUtil.getString(c, FIELD_HOOK);
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
            this.user = b.getInt(FIELD_USER);
            this.category = b.getString(FIELD_CATEGORY);
            this.hookId = b.getString(FIELD_HOOK);
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
            b.putInt(FIELD_USER, this.user);
            b.putString(FIELD_CATEGORY, this.category);
            b.putString(FIELD_HOOK, this.hookId);
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

        obj.put(FIELD_USER, this.user);
        obj.put(FIELD_CATEGORY, this.category);
        obj.put(FIELD_HOOK, this.hookId);
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
            this.user = obj.optInt(FIELD_USER, -1);
            this.category = obj.optString(FIELD_CATEGORY, UserIdentity.GLOBAL_NAMESPACE);

            this.hookId = obj.optString(FIELD_HOOK);
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
                .appendFieldLine("Hook", this.hookId)
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