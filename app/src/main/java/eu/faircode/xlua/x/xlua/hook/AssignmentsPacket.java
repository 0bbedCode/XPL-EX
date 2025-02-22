package eu.faircode.xlua.x.xlua.hook;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.faircode.xlua.utilities.BundleUtil;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.PacketBase;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.database.sql.SQLQueryBuilder;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;

public class AssignmentsPacket extends PacketBase {
    private final String id = UUID.randomUUID().toString();

    public final List<String> hookIds = new ArrayList<>();

    public static AssignmentsPacket create(int uid, String packageName, List<String> hookIds, boolean delete, boolean kill) { return new AssignmentsPacket(uid, packageName, hookIds, delete, kill); }

    public AssignmentsPacket() { }
    public AssignmentsPacket(int uid, String packageName, List<String> hookIds, boolean delete, boolean kill) {
        setUserIdentity(UserIdentity.fromUid(uid, packageName));
        setActionPacket(ActionPacket.create(delete ? ActionFlag.DELETE : ActionFlag.PUSH, kill));
        ListUtil.addAllIfValid(this.hookIds, hookIds);
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        populateContentValues(cv);
        return cv;
    }

    @Override
    public void populateBundle(Bundle b) {
        if(b != null) {
            super.populateBundle(b);
            b.putStringArrayList("hooks", new ArrayList<>(this.hookIds));
        }
    }

    @Override
    public void populateFromBundle(Bundle b) {
        if(b != null) {
            super.populateFromBundle(b);
            ListUtil.addAllIfValid(this.hookIds, b.getStringArrayList("hooks"));
        }
    }

    @Override
    public Bundle toBundle() {
        Bundle b = new Bundle();
        populateBundle(b);
        return b;
    }

    @Override
    public void fromCursor(Cursor c) { }

    @Override
    public void populateSnake(SQLQueryBuilder snake) {
        //ToDO:
    }

    @Override
    public String getObjectId() { return id; }

    @Override
    public void setId(String id) {
        //this.id = id;
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendLine(getUserIdentity())
                .appendLine(getActionPacket())
                .appendFieldLine("Hook Ids Count", ListUtil.size(hookIds))
                .toString(true);
    }
}
