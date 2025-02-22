package eu.faircode.xlua.x.xlua.settings;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.JsonHelperEx;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.xlua.database.sql.SQLSnake;
import eu.faircode.xlua.x.xlua.identity.UserIdentityUtils;
import eu.faircode.xlua.x.xlua.interfaces.ICursorType;
import eu.faircode.xlua.x.xlua.interfaces.IJsonType;

public class SettingReMappedItem implements IJsonType {
    public static final String FIELD_NAME = "id";
    public static final String FIELD_OLD_NAMES = "oldIds";

    public String name;
    public List<String> oldNames = new ArrayList<>();

    @Override
    public String toJSONString() throws JSONException { return toJSONObject().toString(); }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jRoot = new JSONObject();
        jRoot.put(FIELD_NAME, name);
        jRoot.put(FIELD_OLD_NAMES, new JSONArray(oldNames));
        return jRoot;
    }

    @Override
    public void fromJSONObject(JSONObject obj) throws JSONException {
        if(obj != null) {
            this.name = obj.optString(FIELD_NAME);
            ListUtil.addAllIfValid(this.oldNames, JsonHelperEx.getStringArrayAsList(obj, FIELD_OLD_NAMES));
        }
    }

    public static class Setting_legacy implements ICursorType, IIdentifiableObject, IJsonType {
        public int user;
        public String category;

        public String name;
        public String value;

        public static String OLD_TABLE_NAME = "setting";
        public static String NEW_TABLE_NAME = "settings";
        public static String JSON = "remap_settings.json";

        public static ContentValues toContentValuesFromNew(Setting_legacy item) {
            if(item == null)
                return null;

            ContentValues cv = new ContentValues();
            cv.put("user", item.user);
            cv.put("category", item.category);
            cv.put("name", item.name);
            cv.put("value", item.value);

            return cv;
        }

        public static ContentValues toContentValuesFromLegacy(Setting_legacy item) {
            if(item == null)
                return null;

            ContentValues cv = new ContentValues();
            cv.put("user", UserIdentityUtils.getUserId(item.user));
            cv.put("category", item.category);
            cv.put("name", item.name);
            cv.put("value", item.value);

            return cv;
        }

        @Override
        public SQLSnake createSnake() {
            return SQLSnake.create()
                    .whereColumn("user", this.user)
                    .whereColumn("category", this.category)
                    .whereColumn("name", this.name)
                    .asSnake();
        }

        @Override
        public void fromCursor(Cursor c) {
            if(c != null) {
                this.user = CursorUtil.getInteger(c, "user", 0);
                this.category = CursorUtil.getString(c, "category");
                this.name = CursorUtil.getString(c, "name");
                this.value = CursorUtil.getString(c, "value");
            }
        }

        @Override
        public String getObjectId() {
            return this.name;
        }

        @Override
        public void setId(String id) {
            this.name = id;
        }

        @Override
        public String getCategory() {
            return category;
        }

        @Override
        public String toJSONString() throws JSONException {
            return toJSONObject().toString();
        }

        @Override
        public JSONObject toJSONObject() throws JSONException {
            //JSONObject obj = new JSONObject();
            //obj.put("user", )
            return null;
        }

        @Override
        public void fromJSONObject(JSONObject obj) throws JSONException {

        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if(obj instanceof Setting_legacy) {
                Setting_legacy val = (Setting_legacy) obj;
                return Str.areEqual(this.name, val.name) && val.user == this.user && Str.areEqual(this.category, val.category);
            }

            return false;
        }
    }
}
