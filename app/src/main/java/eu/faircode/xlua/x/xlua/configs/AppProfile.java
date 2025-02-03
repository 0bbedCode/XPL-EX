package eu.faircode.xlua.x.xlua.configs;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eu.faircode.xlua.XParam;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.utilities.JSONUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.IBundleData;
import eu.faircode.xlua.x.xlua.PacketBase;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.database.TableInfo;
import eu.faircode.xlua.x.xlua.database.sql.SQLQueryBuilder;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;
import eu.faircode.xlua.x.xlua.interfaces.IJsonType;
import eu.faircode.xlua.x.xlua.interfaces.IParcelType;

public class AppProfile extends PacketBase implements IBundleData, IParcelType, IJsonType {
    public static AppProfile create() { return new AppProfile(); }

    public static final String FIELD_USER = UserIdentityIO.FIELD_USER;
    public static final String FIELD_CATEGORY = UserIdentityIO.FIELD_CATEGORY;

    public static final String FIELD_NAME = "name";
    public static final String FIELD_VERSION = "version";
    public static final String FIELD_DESCRIPTION = "description";

    public static final String FIELD_CREATED_DATE = "creationDate";
    public static final String FIELD_LAST_APPLIED = "lastApplied";

    public static final String FIELD_FILE_BACKUPS = "fileBackups";

    public static final String FIELD_CONFIG = "config";

    public static final String TABLE_NAME = "profiles";

    public static final TableInfo TABLE_INFO = TableInfo.create(TABLE_NAME)
            .putIdentification()
            .putText(FIELD_NAME)
            .putText(FIELD_VERSION)
            .putText(FIELD_DESCRIPTION)
            .putInteger(FIELD_CREATED_DATE)
            .putInteger(FIELD_LAST_APPLIED)
            .putText(FIELD_FILE_BACKUPS)
            .putText(FIELD_CONFIG)
            .putPrimaryKey(true, FIELD_NAME);

    public String name;
    public String version;
    public String description;
    public long creationDate;
    public long lastApplied;
    public final List<PathDetails> fileBackups = new ArrayList<>();

    public XPConfig config;

    public AppProfile() { }
    public AppProfile(Parcel in) { fromParcel(in); }

    @Override
    public void populateFromBundle(Bundle b) {
        if(b != null) {
            super.populateFromBundle(b);
            this.name = b.getString(FIELD_NAME);
            this.version = b.getString(FIELD_VERSION);
            this.description = b.getString(FIELD_DESCRIPTION);
            this.creationDate = b.getLong(FIELD_CREATED_DATE);
            this.lastApplied = b.getLong(FIELD_LAST_APPLIED);

            ListUtil.addAllIfValid(this.fileBackups, PathDetails.fromEncoded(b.getString(FIELD_FILE_BACKUPS)));

            this.config = new XPConfig();
            if(b.containsKey(FIELD_CONFIG)) {
                Bundle bundleConfig = b.getBundle(FIELD_CONFIG);
                this.config.populateFromBundle(bundleConfig);
            }
        }
    }

    @Override
    public void populateBundle(Bundle b) {
        if(b != null) {
            super.populateBundle(b);
            b.putString(FIELD_NAME, this.name);
            b.putString(FIELD_VERSION, this.version);
            b.putString(FIELD_DESCRIPTION, this.description);
            b.putLong(FIELD_CREATED_DATE, this.creationDate);
            b.putLong(FIELD_LAST_APPLIED, this.lastApplied);

            b.putString(FIELD_FILE_BACKUPS, PathDetails.encodeDetails(this.fileBackups));
            if(this.config != null) {
                b.putBundle(FIELD_CONFIG, this.config.toBundle());
            }
        }
    }

    @Override
    public Bundle toBundle() {
        Bundle b = new Bundle();
        populateBundle(b);
        return b;
    }

    @Override
    public void fromParcel(Parcel in) {
        setUserIdentity(UserIdentity.fromUid(in.readInt(), in.readString()));
        this.name = in.readString();
        this.version = in.readString();
        this.description = in.readString();
        this.creationDate = in.readLong();
        this.lastApplied = in.readLong();

        ListUtil.addAllIfValid(this.fileBackups, PathDetails.fromEncoded(in.readString()));

        this.config = XPConfig.fromJsonString(in.readString());
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(this.name);
        parcel.writeString(this.version);
        parcel.writeString(this.description);
        parcel.writeLong(this.creationDate);
        parcel.writeLong(this.lastApplied);

        parcel.writeString(PathDetails.encodeDetails(this.fileBackups));
        parcel.writeString(JSONUtil.objectToString(JSONUtil.toObject(this.config)));
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(FIELD_NAME, this.name);
        values.put(FIELD_VERSION, this.version);
        values.put(FIELD_DESCRIPTION, this.description);
        values.put(FIELD_CREATED_DATE, this.creationDate);
        values.put(FIELD_LAST_APPLIED, this.lastApplied);

        values.put(FIELD_FILE_BACKUPS, PathDetails.encodeDetails(this.fileBackups));

        values.put(FIELD_CONFIG, JSONUtil.objectToString(JSONUtil.toObject(this.config)));

        return values;
    }

    @Override
    public void fromCursor(Cursor c) {
        if(c != null) {
            setUserIdentity(UserIdentity.create(c));
            this.name = CursorUtil.getString(c, FIELD_NAME);
            this.version = CursorUtil.getString(c, FIELD_VERSION);
            this.description = CursorUtil.getString(c, FIELD_DESCRIPTION);

            this.creationDate = CursorUtil.getLong(c, FIELD_CREATED_DATE);
            this.lastApplied = CursorUtil.getLong(c, FIELD_LAST_APPLIED);

            ListUtil.addAllIfValid(this.fileBackups, PathDetails.fromEncoded(CursorUtil.getString(c, FIELD_FILE_BACKUPS)));

            this.config = XPConfig.fromJsonString(CursorUtil.getString(c, FIELD_CONFIG));
        }
    }

    @Override
    public void populateSnake(SQLQueryBuilder snake) {
        snake.whereIdentity(getUserIdentity().getUserId(true), getCategory());
        snake.whereColumn(FIELD_NAME, this.name);
    }

    @Override
    public String getSharedId() { return name; }

    @Override
    public void setId(String id) {
        this.name = id;
    }

    @Override
    public int describeContents() { return 0; }

    public static final Parcelable.Creator<AppProfile> CREATOR = new Parcelable.Creator<AppProfile>() {
        @Override
        public AppProfile createFromParcel(Parcel source) { return new AppProfile(source); }
        @Override
        public AppProfile[] newArray(int size) { return new AppProfile[size]; }
    };

    @Override
    public String toJSONString() throws JSONException {
        return toJSONObject().toString();
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject object = new JSONObject();
        object.put(FIELD_USER, 0);
        object.put(FIELD_CATEGORY, getCategory());
        object.put(FIELD_NAME, name);
        object.put(FIELD_VERSION, version);
        object.put(FIELD_DESCRIPTION, description);
        object.put(FIELD_CREATED_DATE, creationDate);
        object.put(FIELD_LAST_APPLIED, lastApplied);
        //object.put(FIELD_FILE_BACKUPS, JSONUtil.objectToString(JSONUtil.toObject(this.config)));
        object.put(FIELD_FILE_BACKUPS, PathDetails.encodeDetails(this.fileBackups));
        object.put(FIELD_CONFIG, JSONUtil.objectToString(JSONUtil.toObject(this.config)));

        return object;
    }

    @Override
    public void fromJSONObject(JSONObject obj) throws JSONException {
        if(obj != null) {
            int user = 0;
            String cat = obj.optString(FIELD_CATEGORY);
            setUserIdentity(new UserIdentity(0, user, cat));

            this.name = obj.optString(FIELD_NAME);
            this.version = obj.optString(FIELD_VERSION);
            this.description = obj.optString(FIELD_DESCRIPTION);
            this.creationDate = obj.getLong(FIELD_CREATED_DATE);
            this.lastApplied = obj.getLong(FIELD_LAST_APPLIED);

            ListUtil.addAllIfValid(this.fileBackups, PathDetails.fromEncoded(obj.getString(FIELD_FILE_BACKUPS)));
            this.config = XPConfig.fromJsonString(obj.getString(FIELD_CONFIG));
        }
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine(FIELD_USER, getUid())
                .appendFieldLine(FIELD_CATEGORY, getCategory())
                .appendFieldLine(FIELD_NAME, name)
                .appendFieldLine(FIELD_VERSION, version)
                .appendFieldLine(FIELD_DESCRIPTION, description)
                .appendFieldLine(FIELD_CREATED_DATE, creationDate)
                .appendFieldLine(FIELD_LAST_APPLIED, lastApplied)
                .appendFieldLine(FIELD_FILE_BACKUPS, PathDetails.encodeDetails(this.fileBackups))
                .appendFieldLine(FIELD_CONFIG, JSONUtil.objectToString(JSONUtil.toObject(this.config)))
                .toString(true);
    }
}
