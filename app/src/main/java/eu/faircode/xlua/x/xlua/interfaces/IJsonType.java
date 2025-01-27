package eu.faircode.xlua.x.xlua.interfaces;

import org.json.JSONException;
import org.json.JSONObject;

public interface IJsonType {
    String toJSONString() throws JSONException;
    JSONObject toJSONObject() throws JSONException;
    void fromJSONObject(JSONObject obj) throws JSONException;
}
