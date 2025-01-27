package eu.faircode.xlua.hooks;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.api.xmock.database.LuaSettingsManager;
import eu.faircode.xlua.api.xstandard.interfaces.IDBSerial;
import eu.faircode.xlua.api.hook.group.XLuaGroup;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.xstandard.database.SqlQuerySnake;

public class XReport implements IDBSerial {
    public String hookId;
    public String packageName;
    public Integer uid;
    public String event;
    public Long time;
    public Bundle data;

    private Boolean notify = null;
    private Integer userid = null;
    private Integer restricted = null;

    public XReport() { }
    public XReport(Bundle b) { fromBundle(b); }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        if(hookId != null) b.putString("hook", hookId);
        if(packageName != null) b.putString("packageName", packageName);
        if(uid != null) b.putInt("uid", uid);
        if(event != null) b.putString("event", event);
        if(time != null) b.putLong("time", time);
        if(data != null) b.putBundle("data", data);
        return b;
    }

    public void fromBundle(Bundle b) {
        hookId = b.getString("hook");
        packageName = b.getString("packageName");
        uid = b.getInt("uid");
        userid = XUtil.getUserId(uid);
        event = b.getString("event");
        time = b.getLong("time");
        data = b.getBundle("data");
        restricted = data.getInt("restricted", 0);
    }

    public XLuaGroup createGroupObject(XLuaHook hook, long used) { return new XLuaGroup(packageName, uid, hook.getGroup(), used); }

    public int getRestricted() {
        if(restricted == null) restricted = data.getInt("restricted", 0);
        return restricted;
    }

    public int getUserId() {
        if(userid == null) userid = XUtil.getUserId(uid);
        return userid;
    }

    public boolean getNotify(XDatabaseOld db) {
        if(notify == null)//This is where error can arrive , context is null and context is used and abused alot :P
            notify = LuaSettingsManager.getSettingBoolean(null, db, "notify", getUserId(), packageName);

        //Log.i("XLua.XReport", "XXR use=true,  (pkg=" + packageName + " userId=" + getUserId() + " uid=" + uid + " notify=" + notify + " )");
        return notify;
    }

    public SqlQuerySnake generateQuery() {
        return SqlQuerySnake.create()
                .whereColumns("package", "uid", "hook")
                .whereColumnValues(packageName, Integer.toString(uid), hookId);
    }

    public String getFullException() {
        StringBuilder b = new StringBuilder();
        for (String key : data.keySet()) {
            b.append(' ');
            b.append(key);
            b.append('=');
            Object value = data.get(key);
            b.append(value == null ? "null" : value.toString());
        }

        return b.toString();
    }

    @NonNull
    @Override
    public String toString() {
        return "Hook " + hookId + " pkg=" + packageName + ":" + uid + " event=" + event;
    }

    @Override
    public ContentValues createContentValues() {
        ContentValues cv = new ContentValues();
        if(event.equals("install"))
            cv.put("installed", time);
        else if(event.equals("use")) {
            cv.put("used", time);
            cv.put("restricted", restricted);
        }

        if (data.containsKey("exception"))
            cv.put("exception", data.getString("exception"));
        if (data.containsKey("old"))
            cv.put("old", data.getString("old"));
        if (data.containsKey("new"))
            cv.put("new", data.getString("new"));

        return cv;
    }

    @Override
    public List<ContentValues> createContentValuesList() { return null; }

    @Override
    public void fromContentValuesList(List<ContentValues> contentValues) { }

    @Override
    public void fromContentValues(ContentValues contentValue) { }

    @Override
    public void fromCursor(Cursor cursor) { }
}
