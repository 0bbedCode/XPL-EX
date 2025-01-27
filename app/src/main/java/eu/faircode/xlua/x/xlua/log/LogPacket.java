package eu.faircode.xlua.x.xlua.log;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.xlua.IBundleData;
import eu.faircode.xlua.x.xlua.PacketBase;
import eu.faircode.xlua.x.xlua.configs.AppProfile;
import eu.faircode.xlua.x.xlua.database.IDatabaseEntry;
import eu.faircode.xlua.x.xlua.database.TableInfo;
import eu.faircode.xlua.x.xlua.database.sql.SQLQueryBuilder;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;
import eu.faircode.xlua.x.xlua.interfaces.ICursorType;
import eu.faircode.xlua.x.xlua.interfaces.IParcelType;

public class LogPacket implements IBundleData, ICursorType, IDatabaseEntry, IParcelType {

    public static final String DATABASE_NAME = "xlg.db";
    public static final String TABLE_NAME = "logs";
    //Post all the "logs" in a que within the server ?
    //Nah have this root ?
    //Less stress on the Server!
    //We can have it auto new thread away the packet assume we have it "handled" ? have a log Prefix

    //Due to speed wanted reason
    //Log core should be as barebones , less dependent as POSSIBLE!
    //Hell even try to avoid snake

    //For a Simple View, have check boxes above the "logs"
    //Check types or Drop down
    //Either way a list view for main dialog

    public static final String FIELD_USER = UserIdentityIO.FIELD_USER;
    public static final String FIELD_CATEGORY = UserIdentityIO.FIELD_CATEGORY;

    public static final String FIELD_UID = "uid";
    public static final String FIELD_ID = "id";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_TIME = "time";
    public static final String FIELD_MESSAGE = "message";

    public static final String FIELD_REGISTER_S1 = "sR1";
    public static final String FIELD_REGISTER_S2 = "sR2";
    public static final String FIELD_REGISTER_L1 = "lR1";
    public static final String FIELD_REGISTER_L2 = "lR2";

    public static final TableInfo TABLE_INFO = TableInfo.create(TABLE_NAME)
            .putIdentification()
            .putInteger(FIELD_UID)
            .putText(FIELD_ID)
            .putInteger(FIELD_TYPE)
            .putInteger(FIELD_TIME)
            .putText(FIELD_MESSAGE)
            .putText(FIELD_REGISTER_S1)
            .putText(FIELD_REGISTER_S2)
            .putInteger(FIELD_REGISTER_L1)
            .putInteger(FIELD_REGISTER_L2)
            .putPrimaryKey(true, FIELD_ID);

    public int userId;
    public String category;

    public int uid;
    public String id;
    public long type;
    public long time;
    public String message;  //Parse log as JSON ?
    public String sR1;
    public String sR2;
    public long lR1;
    public long lR2;

    public LogPacket() { }
    public LogPacket(Parcel in) { fromParcel(in); }

    @Override
    public void populateBundle(Bundle b) {
        if(b != null) {
            b.putInt(FIELD_USER, this.userId);
            b.putString(FIELD_CATEGORY, this.category);

            b.putInt(FIELD_UID, this.uid);
            b.putString(FIELD_ID, this.id);
            b.putLong(FIELD_TYPE, this.type);

            b.putLong(FIELD_TIME, this.time);
            b.putString(FIELD_MESSAGE, this.message);
            b.putString(FIELD_REGISTER_S1, this.sR1);
            b.putString(FIELD_REGISTER_S2, this.sR2);

            b.putLong(FIELD_REGISTER_L1, this.lR1);
            b.putLong(FIELD_REGISTER_L2, this.lR2);
        }
    }

    @Override
    public void populateFromBundle(Bundle b) {
        if(b != null) {
            this.userId = b.getInt(FIELD_USER);
            this.category = b.getString(FIELD_CATEGORY);
            this.uid = b.getInt(FIELD_UID);
            this.id = b.getString(FIELD_ID);
            this.type = b.getLong(FIELD_TYPE);
            this.time = b.getLong(FIELD_TIME);
            this.message = b.getString(FIELD_MESSAGE);
            this.sR1 = b.getString(FIELD_REGISTER_S1);
            this.sR2 = b.getString(FIELD_REGISTER_S1);
            this.lR1 = b.getLong(FIELD_REGISTER_L1);
            this.lR2 = b.getLong(FIELD_REGISTER_L1);
        }
    }

    @Override
    public Bundle toBundle() {
        Bundle b = new Bundle();
        populateBundle(b);
        return b;
    }

    @Override
    public void populateContentValues(ContentValues cv) {
        cv.put(FIELD_USER, this.userId);
        cv.put(FIELD_CATEGORY, this.category);
        cv.put(FIELD_UID, this.uid);
        cv.put(FIELD_ID, this.id);
        cv.put(FIELD_TYPE, this.type);
        cv.put(FIELD_TIME, this.time);
        cv.put(FIELD_MESSAGE, this.message);
        cv.put(FIELD_REGISTER_S1, this.sR1);
        cv.put(FIELD_REGISTER_S2, this.sR2);
        cv.put(FIELD_REGISTER_L1, this.lR1);
        cv.put(FIELD_REGISTER_L2, this.lR2);
    }

    @Override
    public void populateFromContentValues(ContentValues cv) {
        this.userId = cv.getAsInteger(FIELD_USER);
        this.category = cv.getAsString(FIELD_CATEGORY);
        this.uid = cv.getAsInteger(FIELD_UID);
        this.id = cv.getAsString(FIELD_ID);
        this.type = cv.getAsLong(FIELD_TYPE);
        this.time = cv.getAsLong(FIELD_TIME);
        this.message = cv.getAsString(FIELD_MESSAGE);
        this.sR1 = cv.getAsString(FIELD_REGISTER_S1);
        this.sR2 = cv.getAsString(FIELD_REGISTER_S2);
        this.lR1 = cv.getAsLong(FIELD_REGISTER_L1);
        this.lR2 = cv.getAsLong(FIELD_REGISTER_L2);
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
            this.userId = CursorUtil.getInteger(c, FIELD_USER);
            this.category = CursorUtil.getString(c, FIELD_CATEGORY);
            this.uid = CursorUtil.getInteger(c, FIELD_UID);
            this.id = CursorUtil.getString(c, FIELD_ID);
            this.type = CursorUtil.getLong(c, FIELD_TYPE);
            this.time = CursorUtil.getLong(c, FIELD_TIME);
            this.message = CursorUtil.getString(c, FIELD_MESSAGE);
            this.sR1 = CursorUtil.getString(c, FIELD_REGISTER_S1);
            this.sR2 = CursorUtil.getString(c, FIELD_REGISTER_S2);
            this.lR1 = CursorUtil.getLong(c, FIELD_REGISTER_L1);
            this.lR2 = CursorUtil.getLong(c, FIELD_REGISTER_L2);
        }
    }

    @Override
    public void populateSnake(SQLQueryBuilder snake) {
        //ToDO: If we are using snake
    }

    @Override
    public void fromParcel(Parcel in) {
        this.userId = in.readInt();
        this.category = in.readString();
        this.uid = in.readInt();
        this.id = in.readString();
        this.type = in.readLong();
        this.time = in.readLong();
        this.message = in.readString();
        this.sR1 = in.readString();
        this.sR2 = in.readString();
        this.lR1 = in.readLong();
        this.lR2= in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(this.userId);
        parcel.writeString(this.category);
        parcel.writeInt(this.uid);
        parcel.writeString(this.id);
        parcel.writeLong(this.type);
        parcel.writeLong(this.time);
        parcel.writeString(this.message);
        parcel.writeString(this.sR1);
        parcel.writeString(this.sR2);
        parcel.writeLong(this.lR1);
        parcel.writeLong(this.lR2);
    }

    public static final Parcelable.Creator<LogPacket> CREATOR = new Parcelable.Creator<LogPacket>() {
        @Override
        public LogPacket createFromParcel(Parcel source) { return new LogPacket(source); }
        @Override
        public LogPacket[] newArray(int size) { return new LogPacket[size]; }
    };

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine(FIELD_USER, this.userId)
                .appendFieldLine(FIELD_CATEGORY, this.category)
                .appendFieldLine(FIELD_UID, this.uid)
                .appendFieldLine(FIELD_ID, this.id)
                .appendFieldLine(FIELD_TYPE, this.type)
                .appendFieldLine(FIELD_TIME, this.time)
                .appendFieldLine(FIELD_MESSAGE, this.message)
                .appendFieldLine(FIELD_REGISTER_S1, this.sR1)
                .appendFieldLine(FIELD_REGISTER_S2, this.sR2)
                .appendFieldLine(FIELD_REGISTER_L1, this.lR1)
                .appendFieldLine(FIELD_REGISTER_L2, this.sR2)
                .toString(true);
    }
}
