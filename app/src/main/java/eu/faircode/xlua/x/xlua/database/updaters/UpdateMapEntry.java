package eu.faircode.xlua.x.xlua.database.updaters;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.JsonHelperEx;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.interfaces.IJsonType;

public class UpdateMapEntry implements IJsonType {
    public static final String FIELD_ID = "id";
    public static final String FIELD_OLD_IDS = "oldIds";

    public String id;
    public final List<String> oldIds = new ArrayList<>();

    public boolean hasOldId(String id) { return id != null && !id.isEmpty() && oldIds.contains(id); }

    @Override
    public String toJSONString() throws JSONException { return toJSONObject().toString(); }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(FIELD_ID, this.id);
        obj.put(FIELD_OLD_IDS, new JSONArray(this.oldIds));
        return obj;
    }

    @Override
    public void fromJSONObject(JSONObject obj) throws JSONException {
        if(obj != null) {
            this.id = obj.optString(FIELD_ID);
            ListUtil.addAllIfValid(this.oldIds, JsonHelperEx.getStringArrayAsList(obj, FIELD_OLD_IDS), true);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .appendFieldLine(FIELD_ID, this.id)
                .appendDividerTitleLine(Str.combineEx(FIELD_OLD_IDS, "[", this.oldIds.size(), "]"))
                .appendLine(this.oldIds, Str.NEW_LINE)
                .toString(true);
    }
}
