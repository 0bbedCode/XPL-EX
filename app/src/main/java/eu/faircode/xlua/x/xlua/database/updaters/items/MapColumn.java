package eu.faircode.xlua.x.xlua.database.updaters.items;

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

public class MapColumn implements IJsonType {
    public static final String FIELD_TABLE = "tableName";
    public static final String FIELD_NAME = "columnName";
    public static final String FIELD_OLD_COLUMNS = "oldColumns";

    public static final String JSON = "table_maps.json";

    public String tableName;
    public String columnName;
    public final List<String> oldColumns = new ArrayList<>();

    @Override
    public String toJSONString() throws JSONException { return toJSONObject().toString(); }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(FIELD_TABLE, this.tableName);
        obj.put(FIELD_NAME, this.columnName);
        obj.put(FIELD_OLD_COLUMNS, new JSONArray(this.oldColumns));
        return obj;
    }

    @Override
    public void fromJSONObject(JSONObject obj) throws JSONException {
        if(obj != null) {
            this.tableName = obj.optString(FIELD_TABLE);
            this.columnName = obj.optString(FIELD_NAME);
            ListUtil.addAllIfValid(this.oldColumns, JsonHelperEx.getStringArrayAsList(obj, FIELD_OLD_COLUMNS), true);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .appendFieldLine(FIELD_TABLE, this.tableName)
                .appendFieldLine(FIELD_NAME, this.columnName)
                .appendDividerTitleLine(Str.combineEx(FIELD_OLD_COLUMNS, "[", this.oldColumns.size(), "]"))
                .appendLine(this.oldColumns, Str.NEW_LINE)
                .toString(true);
    }
}


