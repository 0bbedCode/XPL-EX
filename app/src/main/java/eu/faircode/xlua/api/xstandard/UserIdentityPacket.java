package eu.faircode.xlua.api.xstandard;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import eu.faircode.xlua.AppGeneric;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.api.hook.assignment.LuaAssignment;
import eu.faircode.xlua.api.xstandard.database.SqlQuerySnake;
import eu.faircode.xlua.api.xstandard.interfaces.IJsonSerial;
import eu.faircode.xlua.api.xstandard.interfaces.IUserPacket;
import eu.faircode.xlua.utilities.BundleUtil;
import eu.faircode.xlua.utilities.ContentValuesUtil;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.utilities.JSONUtil;
import eu.faircode.xlua.utilities.StringUtil;

public class UserIdentityPacket implements IJsonSerial, IUserPacket {
    public static final int USER_QUERY_PACKET_ONE = 0x0;
    public static final int USER_QUERY_PACKET_TWO = 0x1;
    public static final int CODE_NULL_EMPTY = 0xDEAD;

    public static final int QUERY_FLAG_ONE = 0x1;
    public static final int QUERY_FLAG_TWO = 0x2;

    public static final String GLOBAL_NAMESPACE = "global";
    public static final int GLOBAL_USER = 0;

    protected Integer user;
    protected String category;
    protected Integer code;
    protected Boolean kill;
    protected String key;
    protected Integer userId;

    private  boolean resolved = false;

    private boolean useUserIdentity = true;

    public UserIdentityPacket() { this(GLOBAL_USER, GLOBAL_NAMESPACE);   }
    public UserIdentityPacket(Integer user, String category) {
        setUser(user);
        setCategory(category);
        setCode(CODE_NULL_EMPTY);
        setKill(false);
    }

    public UserIdentityPacket setIsResolved(boolean isResolved) { this.resolved = isResolved; return this; }

    public boolean isGlobal() { return this.category != null && this.category.equalsIgnoreCase(GLOBAL_NAMESPACE); }
    public boolean isValidUser() { return !StringUtil.isValidString(this.category); }

    public Integer getUser() { return this.user; }
    public UserIdentityPacket setUser(Integer user) { if(user != null) this.user = user; return this; }

    public Integer getOriginalUser() {  return this.userId == null ? this.user : this.userId;  }

    public String getCategory() { return this.category; }
    public UserIdentityPacket setCategory(String category) { if(category != null) this.category = category; return this; }

    public UserIdentityPacket setUseUserIdentity(boolean useUserIdentity) { this.useUserIdentity = useUserIdentity; return this; }
    public boolean getUseUserIdentity() { return this.useUserIdentity; }

    public Boolean isKill() {  return this.kill != null && this.kill; }
    public UserIdentityPacket setKill(Boolean kill) { if(kill != null) this.kill = kill; return this; }

    public Integer getCode() { if(this.code == null) return CODE_NULL_EMPTY;  return this.code; }
    public UserIdentityPacket setCode(Integer code) { if(code != null) this.code = code; return this; }

    public boolean isNullOrEmptyCode() { return this.code == null || this.code == CODE_NULL_EMPTY; }

    public boolean isCode(int code) { return isCode(true, code); }
    public boolean isCode(boolean ensureNotEmptyOrNull, int code) { return ensureNotEmptyOrNull ? !isNullOrEmptyCode() && this.code == code : isNullOrEmptyCode() || this.code == code; }

    public boolean isCodes(int... codes) { return isCodes(true, codes); }
    public boolean isCodes(boolean ensureNotEmptyOrNull, int... codes) {
        if(!ensureNotEmptyOrNull && isNullOrEmptyCode())
            return true;
        else if(isNullOrEmptyCode())
            return false;

        for (int c : codes)
            if(c == this.code)
                return true;

        return false;
    }

    public String getKey() { return this.key; }
    public UserIdentityPacket setKey(String key) { if(key != null) this.key = key; return this; }

    public SqlQuerySnake createUserQuery(XDatabaseOld db, String table) { return table.equalsIgnoreCase(LuaAssignment.Table.name) ? createUserQuery(db, table, USER_QUERY_PACKET_TWO) : createUserQuery(db, table, USER_QUERY_PACKET_ONE); }
    public SqlQuerySnake createUserQuery(XDatabaseOld db, String table, int flags) {
        if(flags == USER_QUERY_PACKET_TWO) {
            return SqlQuerySnake.create(db, table)
                    .whereColumn("package", getCategory())
                    .whereColumn("uid", getOriginalUser());
        } else if(flags == USER_QUERY_PACKET_ONE) {
            return SqlQuerySnake
                    .create()
                    .whereColumn("user", getUser())
                    .whereColumn("category", getCategory());
        }
        return null;
    }

    @Override
    public void ensureCode(int defaultCode) { if(isNullOrEmptyCode()) setCode(defaultCode); }

    @Override
    public void resolveUserID() {
        ensureIdentification();
        if(this.user > 0 && !this.resolved) {
            this.resolved = true;
            this.userId = this.user;
            this.user = XUtil.getUserId(user);
        }
    }

    @Override
    public void ensureIdentification() {
        if(this.user == null) this.user = GLOBAL_USER;
        if(this.category == null) this.category = GLOBAL_NAMESPACE;
    }

    @Override
    public void readSelectionArgsFromQuery(String[] selection, int flags) {
        if(selection != null && selection.length > 0) {
            if(DebugUtil.isDebug())
                Log.i("XLua.UserID", "entering read selection =" + selection.length + " flags=" + flags);

            if(flags == USER_QUERY_PACKET_ONE) {
                this.user = StringUtil.toInteger(selection[0], GLOBAL_USER);
                if(selection.length > 1) {
                    this.category = selection[1];
                    if(selection.length > 2)
                        this.code = StringUtil.toInteger(selection[2], CODE_NULL_EMPTY);
                }
            }else if(flags == USER_QUERY_PACKET_TWO) {
                this.category = selection[0];
                if(selection.length > 1) {
                    this.user =  StringUtil.toInteger(selection[1], GLOBAL_USER);
                    if(selection.length > 2)
                        this.code = StringUtil.toInteger(selection[2], CODE_NULL_EMPTY);
                }
            }
        }else Log.e("XLua.UserID", "Selection is null or empty");
    }

    @Override
    public SqlQuerySnake generateSelectionArgsQuery(int flags) {
        ensureIdentification();
        if(DebugUtil.isDebug())
            Log.i("XLua.UserID", "Entering Selection Args Generation: flags=" + flags + " tostring= " + this.toString());

        if(flags == USER_QUERY_PACKET_ONE) {
            return SqlQuerySnake.create()
                    .whereColumn("user", this.user)
                    .whereColumn("category", this.category)
                    .whereColumn("code", getCode());
        }else if(flags == USER_QUERY_PACKET_TWO) {
            return SqlQuerySnake.create()
                    .whereColumn("category", this.category)
                    .whereColumn("user", this.user)
                    .whereColumn("code", getCode());
        } return null;
    }

    @Override
    public void identificationFromApplication(AppGeneric application) {
        if(application != null) {
            this.user = application.getUid();
            this.category = application.getPackageName();
        }
    }

    @Override
    public boolean isValidIdentity() { return (this.user != null && this.user >= 0) && this.category != null; }

    @Override
    public Bundle writePacketUserBundle(Bundle b) {
        if(b != null) {
            ensureIdentification();
            if(this.user != null) b.putInt("user", this.user);
            if(this.category != null) b.putString("category", this.category);
        } return b;
    }

    @Override
    public void readPacketUserBundle(Bundle b) {
        if(b != null) {
            this.user = BundleUtil.readInteger(b, "user", BundleUtil.readInteger(b, "uid", GLOBAL_USER));
            this.category = BundleUtil.readString(b, "category", BundleUtil.readString(b, "packageName", GLOBAL_NAMESPACE));
        }
    }

    @Override
    public Bundle writePacketHeaderBundle(Bundle b) {
        if(b != null) {
            if(this.code != null) b.putInt("code", getCode());
            if(this.kill != null) b.putBoolean("kill", isKill());
            if(this.key != null) b.putString("key", getKey());
        }

        return b;
    }

    @Override
    public void readPacketHeaderBundle(Bundle b) {
        if(b != null) {
            this.code = BundleUtil.readInteger(b, "code", getCode());
            this.kill = BundleUtil.readBoolean(b, "kill", false);
            this.key = BundleUtil.readString(b, "key", null);
        }
    }

    @Override
    public Bundle toBundle() {
        Bundle b = new Bundle();
        if(useUserIdentity) { writePacketUserBundle(b); } return b;
    }

    @Override
    public void fromBundle(Bundle b) { if(useUserIdentity) { readPacketUserBundle(b); } }

    @Override
    public ContentValues createContentValues() {
        ContentValues cv = new ContentValues();
        if(useUserIdentity) {
            ensureIdentification();
            if(this.user != null) cv.put("user", this.user);
            if(this.category != null) cv.put("category", this.category);
        } return cv;
    }

    @Override
    public void fromContentValues(ContentValues contentValue) {
        if(useUserIdentity) {
            this.user = ContentValuesUtil.getInteger(contentValue, "user", GLOBAL_USER);
            this.category = ContentValuesUtil.getString(contentValue, "category", GLOBAL_NAMESPACE);
        }
    }

    @Override
    public List<ContentValues> createContentValuesList() { return null;  }

    @Override
    public void fromContentValuesList(List<ContentValues> contentValues) { }

    @Override
    public void fromCursor(Cursor cursor) {
        if(useUserIdentity) {
            this.user = CursorUtil.getInteger(cursor, "user", GLOBAL_USER);
            this.category = CursorUtil.getString(cursor, "category", GLOBAL_NAMESPACE);
        }
    }

    @Override
    public void fromParcel(Parcel in) {
        if(useUserIdentity) {
            this.user = in.readInt();
            this.category = in.readString();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if(useUserIdentity) {
            ensureIdentification();
            dest.writeInt(this.user);
            dest.writeString(this.category);
        }
    }

    @Override
    public String toJSON() throws JSONException { return toJSONObject().toString(2); }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jRoot = new JSONObject();
        if(useUserIdentity) {
            ensureIdentification();
            if(this.user != null) jRoot.put("user", this.user);
            if(this.category != null) jRoot.put("category", this.category);
        } return jRoot;
    }

    @Override
    public void fromJSONObject(JSONObject obj) throws JSONException {
        if(useUserIdentity) {
            this.user = JSONUtil.getInteger(obj, "user", GLOBAL_USER);
            this.category = JSONUtil.getString(obj, "category", GLOBAL_NAMESPACE);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(this.category == null) return false;
        if(obj instanceof String) return  ((String)obj).equalsIgnoreCase(this.category);
        else if(obj instanceof UserIdentityPacket) {
            UserIdentityPacket packet = ((UserIdentityPacket) obj);
            if(packet.getCategory() == null) return false;
            return packet.getCategory().equalsIgnoreCase(this.category);
        } return false;
    }

    @NonNull
    @Override
    public String toString() {
        if(useUserIdentity)
            return new StringBuilder()
                    .append(" user=")
                    .append(user)
                    .append(" category=")
                    .append(category)
                    .append(" code=")
                    .append(code).toString();

        return "";
    }
}
