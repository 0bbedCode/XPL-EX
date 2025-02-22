package eu.faircode.xlua.x.xlua.hook;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;

import java.util.LinkedHashMap;

import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.xlua.PacketBase;
import eu.faircode.xlua.x.xlua.database.TableInfo;
import eu.faircode.xlua.x.xlua.database.sql.SQLQueryBuilder;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;
import eu.faircode.xlua.x.xlua.interfaces.ICursorType;

public class GroupPacket extends PacketBase implements ICursorType {
    public String name;
    public long used = -1;

    public boolean isLegacy = false;

    public static final String FIELD_USER = UserIdentityIO.FIELD_USER;
    public static final String FIELD_CATEGORY = UserIdentityIO.FIELD_CATEGORY;
    public static final String FIELD_NAME = "name";
    public static final String FIELD_USED = "used";

    public static final String TABLE_NAME = "groups";

    public static final TableInfo TABLE_INFO = TableInfo.create(TABLE_NAME)
            .putIdentification()
            .putText(FIELD_NAME)
            .putInteger(FIELD_USED)
            .putPrimaryKey(true, FIELD_NAME);

    public static final LinkedHashMap<String, String> COLUMNS = TABLE_INFO.columns;

    public static GroupPacket create(String name) { return new GroupPacket(name); }

    public GroupPacket() { }
    public GroupPacket(String name) { this.name = name; }

    @Override
    public void populateFromContentValues(ContentValues cv) {
        if(cv != null) {
            super.populateFromContentValues(cv);
            this.name = cv.getAsString(FIELD_NAME);
            this.used = cv.getAsLong(FIELD_USED);
        }
    }

    @Override
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
            cv.put(FIELD_USED, this.used);
        }
    }

    @Override
    public void populateBundle(Bundle b) {
        if(b != null) {
            super.populateBundle(b);
            b.putString(FIELD_NAME, this.name);
            b.putLong(FIELD_USED, this.used);
        }
    }

    @Override
    public void populateFromBundle(Bundle b) {
        if(b != null) {
            super.populateFromBundle(b);
            this.name = b.getString(FIELD_NAME);
            this.used = b.getLong(FIELD_USED);
        }
    }

    @Override
    public Bundle toBundle() {
        Bundle b = new Bundle();
        populateBundle(b);
        return b;
    }

    @Override
    public void fromCursor(Cursor c) {
        if(c != null) {
            UserIdentityIO.populateFromCursor_group_assignment(c, this, isLegacy);
            this.name = CursorUtil.getString(c, FIELD_NAME);
            this.used = CursorUtil.getLong(c, FIELD_USED);
        }
    }

    @Override
    public void populateSnake(SQLQueryBuilder snake) {
        //ToDO:
    }

    @Override
    public String getObjectId() { return name; }

    @Override
    public void setId(String id) {
        this.name = id;
    }
}
