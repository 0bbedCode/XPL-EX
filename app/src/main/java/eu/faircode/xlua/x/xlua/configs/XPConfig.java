package eu.faircode.xlua.x.xlua.configs;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.utilities.JSONUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.xlua.IBundleData;
import eu.faircode.xlua.x.xlua.PacketBase;
import eu.faircode.xlua.x.xlua.commands.PkgInfo;
import eu.faircode.xlua.x.xlua.commands.call.AssignHooksCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetSettingsExCommand;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.database.IDatabaseEntry;
import eu.faircode.xlua.x.xlua.database.TableInfo;
import eu.faircode.xlua.x.xlua.database.sql.SQLQueryBuilder;
import eu.faircode.xlua.x.xlua.hook.AssignmentsPacket;
import eu.faircode.xlua.x.xlua.identity.IIdentification;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;
import eu.faircode.xlua.x.xlua.interfaces.ICursorType;
import eu.faircode.xlua.x.xlua.interfaces.IJsonType;
import eu.faircode.xlua.x.xlua.interfaces.IParcelType;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

/*

    ToDo: Lets start to do it the other way around
            We have the base Objects like
                "Config"
            Then we can wrap the Object in a "data" Field
            class XPacket<T>:
                String key
                int code
                bool kill
                T obj           //Things like "user" or "category" can be handled else where ?


 */

public class XPConfig extends PkgInfo implements IJsonType, IDatabaseEntry, IParcelType, IBundleData, ICursorType, IIdentifiableObject {
    public static final String INTERNAL_CONFIG_PREFIX = "___internal_p_config_name__";
    public static final String INTERNAL_AUTHOR_PREFIX = "XPx";
    public static final String INTERNAL_VERSION = "1.0";

    public static final List<String> DEFAULT_TAGS = List.of("cell", "region", "hardware", "location", "network", "soc", "unique", "device", "rom", "etc", "custom");


    private static final String TAG = "XLua.XPConfig";

    public static XPConfig fromJsonString(String s) {
        XPConfig c = new XPConfig();
        JSONUtil.fromObject(JSONUtil.objectFromString(s), c);
        return c;
    }

    public static XPConfig create(AppProfile profile, List<String> hookIds, List<SettingPacket> settings) {
        XPConfig c = new XPConfig();
        c.name = Str.combine(INTERNAL_CONFIG_PREFIX, Str.combine("P", String.valueOf(profile.name.hashCode()).replaceAll("-", "M"), false), false);
        c.type = ConfigType.INTERNAL.name();
        c.author = Str.combine(INTERNAL_AUTHOR_PREFIX, profile.getCategory(), false);
        c.version = INTERNAL_VERSION;

        ListUtil.addAllIfValid(c.hooks, hookIds);
        ListUtil.addAllIfValid(c.settings, settings);

        return c;
    }

    public static XPConfig create(String name, String type, String author, String version, List<SettingPacket> settings, List<String> hookIds) {
        XPConfig config = new XPConfig();
        config.name = name;
        config.type = type;
        config.author = author;
        config.version = version;
        ListUtil.addAllIfValid(config.settings, settings, true);
        ListUtil.addAllIfValid(config.hooks, hookIds, true);
        return config;
    }

    public static final String FIELD_USER = UserIdentityIO.FIELD_USER;
    public static final String FIELD_NAME = "name";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_AUTHOR = "author";
    public static final String FIELD_VERSION = "version";

    public static final String FIELD_SETTINGS = "settings";
    public static final String FIELD_HOOKS = "hooks";

    public static final String TABLE_NAME = "configs";

    public static final TableInfo TABLE_INFO = TableInfo.create(TABLE_NAME)
            .putInteger(FIELD_USER)
            .putText(FIELD_NAME)
            .putText(FIELD_TYPE)
            .putText(FIELD_AUTHOR)
            .putText(FIELD_VERSION)
            .putText(FIELD_SETTINGS)
            .putText(FIELD_HOOKS)
            .putPrimaryKey(false, FIELD_USER,  FIELD_NAME);

    //public int userId = 0;
    public String name;
    public String type;
    public String author;
    public String version;
    public final List<SettingPacket> settings = new ArrayList<>();
    public final List<String> hooks = new ArrayList<>();

    public XPConfig() { }
    public XPConfig(Parcel in) { fromParcel(in); }

    public List<String> getTags() {
        return Str.splitToList(type);
    }

    public void setTags(List<String> tags) {
        this.type = Str.joinList(tags);
    }

    @Override
    public String toJSONString() throws JSONException { return toJSONObject().toString(); }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put(FIELD_NAME, name);
        obj.put(FIELD_TYPE, type);
        obj.put(FIELD_VERSION, version);
        obj.put(FIELD_AUTHOR, author);

        obj.put(FIELD_SETTINGS, XPConfigUtils.settingsToJson(settings));
        obj.put(FIELD_HOOKS, XPConfigUtils.hooksToJson(hooks));

        return obj;
    }

    @Override
    public void fromJSONObject(JSONObject obj) throws JSONException {
        if(obj != null) {
            name = obj.optString(FIELD_NAME);
            type = obj.optString(FIELD_TYPE);
            version = obj.optString(FIELD_VERSION);
            author = obj.optString(FIELD_AUTHOR);

            ListUtil.addAllIfValid(settings, XPConfigUtils.jsonToSettings(obj.optString(FIELD_SETTINGS)), true);
            ListUtil.addAllIfValid(hooks, XPConfigUtils.jsonToHooks(obj.optString(FIELD_HOOKS)), true);
        }
    }

    @Override
    public void populateContentValues(ContentValues cv) {
        if(cv != null) {
            cv.put(FIELD_USER, getUserId());
            cv.put(FIELD_NAME, name);
            cv.put(FIELD_TYPE, type);
            cv.put(FIELD_AUTHOR, author);
            cv.put(FIELD_VERSION, version);

            cv.put(FIELD_SETTINGS, XPConfigUtils.settingsToJson(settings));
            cv.put(FIELD_HOOKS, XPConfigUtils.hooksToJson(hooks));
        }
    }

    @Override
    public void populateFromContentValues(ContentValues cv) {
        if(cv != null) {
            // Read primitive fields
            setUser(cv.getAsInteger(FIELD_USER) != null ? cv.getAsInteger(FIELD_USER) : 0);
            name = cv.getAsString(FIELD_NAME);
            type = cv.getAsString(FIELD_TYPE);
            author = cv.getAsString(FIELD_AUTHOR);
            version = cv.getAsString(FIELD_VERSION);

            // Parse settings from JSON string
            String settingsJson = cv.getAsString(FIELD_SETTINGS);
            ListUtil.addAllIfValid(settings, XPConfigUtils.jsonToSettings(settingsJson), true);

            // Parse hooks from JSON string
            String hooksJson = cv.getAsString(FIELD_HOOKS);
            ListUtil.addAllIfValid(hooks, XPConfigUtils.jsonToHooks(hooksJson), true);
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
            setUser(CursorUtil.getInteger(c, FIELD_USER, 0));
            name = CursorUtil.getString(c, FIELD_NAME, Str.EMPTY);
            type = CursorUtil.getString(c, FIELD_TYPE, Str.EMPTY);
            author = CursorUtil.getString(c, FIELD_AUTHOR, Str.EMPTY);
            version = CursorUtil.getString(c, FIELD_VERSION, Str.EMPTY);

            ListUtil.addAllIfValid(settings, XPConfigUtils.jsonToSettings(CursorUtil.getString(c, FIELD_SETTINGS)), true);
            ListUtil.addAllIfValid(hooks, XPConfigUtils.jsonToHooks(CursorUtil.getString(c, FIELD_HOOKS)), true);
        }
    }

    @Override
    public void populateSnake(SQLQueryBuilder snake) {
        if(snake.flags == SQLQueryBuilder.SQL_FLAG_ID)
            snake.whereColumn(FIELD_USER, getUserId()).whereColumn(FIELD_NAME, name);
    }

    @Override
    public void fromParcel(Parcel in) {
        //userId = in.readInt();
        name = in.readString();
        type = in.readString();
        author = in.readString();
        version = in.readString();

        // Read settings JSON and convert back to list
        String settingsJson = in.readString();
        ListUtil.addAllIfValid(settings, XPConfigUtils.jsonToSettings(settingsJson), true);

        // Read hooks JSON and convert back to list
        String hooksJson = in.readString();
        ListUtil.addAllIfValid(hooks, XPConfigUtils.jsonToHooks(hooksJson), true);
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        //parcel.writeInt(userId);
        parcel.writeString(name);
        parcel.writeString(type);
        parcel.writeString(author);
        parcel.writeString(version);

        // Write settings list as JSON
        parcel.writeString(XPConfigUtils.settingsToJson(settings));

        // Write hooks list as JSON
        parcel.writeString(XPConfigUtils.hooksToJson(hooks));
    }

    @Override
    public void populateFromBundle(Bundle b) {
        if (b == null) return;

        // Read primitive fields
        //userId = b.getInt(FIELD_USER, 0);
        name = b.getString(FIELD_NAME, Str.EMPTY);
        type = b.getString(FIELD_TYPE, Str.EMPTY);
        author = b.getString(FIELD_AUTHOR, Str.EMPTY);
        version = b.getString(FIELD_VERSION, Str.EMPTY);

        // Parse settings JSON string from bundle
        String settingsJson = b.getString(FIELD_SETTINGS, Str.EMPTY);
        ListUtil.addAllIfValid(settings, XPConfigUtils.jsonToSettings(settingsJson), true);

        // Parse hooks JSON string from bundle
        String hooksJson = b.getString(FIELD_HOOKS, Str.EMPTY);
        ListUtil.addAllIfValid(hooks, XPConfigUtils.jsonToHooks(hooksJson), true);
    }

    @Override
    public void populateBundle(Bundle b) {
        if (b == null) return;

        // Write primitive fields to bundle
        //b.putInt(FIELD_USER, userId);
        b.putString(FIELD_NAME, name);
        b.putString(FIELD_TYPE, type);
        b.putString(FIELD_AUTHOR, author);
        b.putString(FIELD_VERSION, version);

        // Write settings list as JSON string
        b.putString(FIELD_SETTINGS, XPConfigUtils.settingsToJson(settings));

        // Write hooks list as JSON string
        b.putString(FIELD_HOOKS, XPConfigUtils.hooksToJson(hooks));
    }

    @Override
    public Bundle toBundle() {
        Bundle b = new Bundle();
        populateBundle(b);
        return b;
    }

    @Override
    public int describeContents() { return 0; }

    public void applyAssignments(Context context, int uid, String packageName, boolean cleanOutOld) {
        if(!Str.isEmpty(packageName) && !UserIdentity.GLOBAL_NAMESPACE.equalsIgnoreCase(packageName)) {
            if(ListUtil.isValid(hooks)) {
                if(cleanOutOld)  {
                    //ToDo
                }

                AssignHooksCommand.call(context, AssignmentsPacket.create(uid, packageName, hooks, false, false));
            }
        }
    }

    public void applySettings(Context context, int uid, String packageName, boolean cleanOutOld) {
        if(!Str.isEmpty(packageName) && !UserIdentity.GLOBAL_NAMESPACE.equalsIgnoreCase(packageName)) {
            if(cleanOutOld && (ListUtil.isValid(hooks) || ListUtil.isValid(settings))) {
                //Fix the un marshall
                //Fix so it ONLY grabs based off of UID and PKG when specified
                List<SettingPacket> all = GetSettingsExCommand.get(context, true, uid, packageName, GetSettingsExCommand.FLAG_ONE);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Cleaning out old setting before applying new, old size=" + ListUtil.size(all) + " New Size=" + this.settings.size());

                if(ListUtil.isValid(all)) {
                    for(SettingPacket packet : all) {
                        if(!GetSettingExCommand.SETTING_SELECTED_CONFIG.equalsIgnoreCase(packet.name) && packet.value != null) {
                            packet.value = null;
                            packet.setUserIdentity(UserIdentity.fromUid(uid, packageName));
                            packet.setActionPacket(ActionPacket.create(ActionFlag.DELETE, false));
                            PutSettingExCommand.call(context, packet);

                            if(DebugUtil.isDebug())
                                Log.d(TAG, "Deleting Setting: "  + Str.toStringOrNull(packet));
                            //A_CODE code = PutSettingExCommand.call(context, setting, app, forceKill, true);
                        }
                    }
                }
            }

            if(ListUtil.isValid(settings)) {
                for(SettingPacket packet : settings) {
                    packet.setUserIdentity(UserIdentity.fromUid(uid, packageName));
                    packet.setActionPacket(ActionPacket.create(ActionFlag.PUSH, false));
                    PutSettingExCommand.call(context, packet);
                }
            }
        }
    }

    public static final Parcelable.Creator<XPConfig> CREATOR = new Parcelable.Creator<XPConfig>() {
        @Override
        public XPConfig createFromParcel(Parcel source) { return new XPConfig(source); }
        @Override
        public XPConfig[] newArray(int size) { return new XPConfig[size]; }
    };

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine(FIELD_USER, getUserId())
                .appendFieldLine(FIELD_NAME, this.name)
                .appendFieldLine(FIELD_TYPE, this.type)
                .appendFieldLine(FIELD_AUTHOR, this.author)
                .appendFieldLine(FIELD_VERSION, this.version)
                .appendFieldLine(FIELD_SETTINGS, XPConfigUtils.settingsToJson(settings))
                .appendFieldLine(FIELD_HOOKS, XPConfigUtils.hooksToJson(hooks))
                .toString(true);
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public void setId(String id) {
        this.name = id;
    }
}








/*
    {
        "name": "Cool Cell Config",
        "type": "Cell",
        "version": "1.0.0.x",
        "author": "me",

        "settings": {
          "zone.language.iso": "IS",
          "gsm.cell.location.lac": "2345"
        }

        "hooks": [
            "hook_1",
            "hook_2",
            "hook_3"]
    }
 */