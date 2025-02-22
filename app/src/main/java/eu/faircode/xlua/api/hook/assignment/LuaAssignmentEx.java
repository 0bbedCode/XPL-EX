package eu.faircode.xlua.api.hook.assignment;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.xstandard.interfaces.IDBSerial;
import eu.faircode.xlua.api.xstandard.interfaces.IJsonSerial;
import eu.faircode.xlua.api.xstandard.interfaces.ISerial;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.utilities.ParcelUtil;
import eu.faircode.xlua.utilities.StringUtil;

public class LuaAssignmentEx implements ISerial, IDBSerial, IJsonSerial, Parcelable {
    protected String packageName;
    protected int uid;
    protected long installed = -1;
    protected String hookId;
    protected long used = -1;
    protected Boolean restricted = false;
    protected String exception;
    protected String old;
    protected String nNew;

    public LuaAssignmentEx() { }
    public LuaAssignmentEx(Parcel p) { fromParcel(p); }

    public String getPackageName() { return this.packageName; }
    //public LuaAssignmentEx setPackageName()

    public int getUid() { return this.uid; }
    public String getHookId() { return this.hookId; }
    public LuaAssignmentEx setHookId(String hookId) { this.hookId =  hookId; return this; }

    public long getInstalled() { return this.installed; }
    public long getUsed() { return this.used; }
    public boolean getRestricted() { return this.restricted; }
    public String getException() { return this.exception; }
    public String getOld() { return this.old; }
    public String getNew() { return this.nNew; }

    @Override
    public ContentValues createContentValues() {
        ContentValues cv = new ContentValues();
        cv.put("package", this.packageName);
        cv.put("uid", this.uid);
        cv.put("hook", this.hookId);
        cv.put("installed", this.installed);
        cv.put("used", this.used);
        cv.put("restricted", this.restricted ? 1 : 0);
        cv.put("exception", this.exception);
        cv.put("old", this.old);
        cv.put("new", this.nNew);
        return cv;
    }

    @Override
    public List<ContentValues> createContentValuesList() { return null; }

    @Override
    public void fromContentValuesList(List<ContentValues> contentValues) { }

    @Override
    public void fromContentValues(ContentValues contentValue) {
        this.packageName = contentValue.getAsString("package");
        this.uid = contentValue.getAsInteger("uid");
        this.hookId = contentValue.getAsString("hook");
        this.installed = contentValue.getAsLong("installed");
        this.used = contentValue.getAsLong("used");
        this.restricted = contentValue.getAsInteger("restricted") == 1;
        this.exception = contentValue.getAsString("exception");
        this.old = contentValue.getAsString("old");
        this.nNew = contentValue.getAsString("new");
    }

    @Override
    public void fromCursor(Cursor cursor) {
        if(cursor != null) {
            this.packageName = CursorUtil.getString(cursor,"package");
            this.uid = CursorUtil.getInteger(cursor, "uid", -1);
            this.hookId = CursorUtil.getString(cursor, "hook");
            this.installed = CursorUtil.getLong(cursor, "installed", (long)-1);
            this.used = CursorUtil.getLong(cursor, "used", (long) -1);
            this.restricted = CursorUtil.getBoolean(cursor, "restricted");
            this.exception = CursorUtil.getString(cursor, "exception");
            this.old = CursorUtil.getString(cursor, "old");
            this.nNew = CursorUtil.getString(cursor, "new");
        }
    }

    @Override
    public Bundle toBundle() {
        //Bundle b = new Bundle();
        //b.putString("packageName", this.packageName);
        //b.putInt("uid", this.uid);
        //b.putString("hook", this.hookId);
        //b.putLong("installed", this.installed);
        //b.
        return null;
    }

    @Override
    public void fromBundle(Bundle bundle) { }

    @Override
    public void fromParcel(Parcel in) {
        //this.packageName = in.readString();
        //this.uid = in.readInt();

    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) { }

    public static final Parcelable.Creator<LuaAssignmentEx> CREATOR = new Parcelable.Creator<LuaAssignmentEx>() {
        @Override
        public LuaAssignmentEx createFromParcel(Parcel source) { return new LuaAssignmentEx(source); }

        @Override
        public LuaAssignmentEx[] newArray(int size) {
            return new LuaAssignmentEx[size];
        }
    };

    @Override
    public String toJSON() throws JSONException { return toJSONObject().toString(2); }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jRoot = new JSONObject();
        jRoot.put("package", this.packageName);
        jRoot.put("uid", this.uid);
        jRoot.put("installed", this.installed);
        jRoot.put("hook", this.hookId);
        jRoot.put("used", this.used);
        jRoot.put("restricted", this.restricted);
        jRoot.put("exception", this.exception);
        jRoot.put("old", this.old);
        jRoot.put("new", this.nNew);
        return jRoot;
    }

    @Override
    public void fromJSONObject(JSONObject obj) throws JSONException {
        this.packageName = obj.getString("package");
        this.uid = obj.getInt("uid");
        this.installed = obj.getLong("installed");
        this.hookId = obj.getString("hook");
        this.used = obj.getLong("used");
        this.restricted = obj.getBoolean("restricted");
        this.exception = obj.getString("exception");
        this.old = obj.getString("old");
        this.nNew = obj.getString("new");
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LuaAssignmentEx))
            return false;
        LuaAssignmentEx other = (LuaAssignmentEx) obj;
        return this.hookId.equals(other.hookId);
    }

    @Override
    public int hashCode() { return this.hookId.hashCode(); }
}
