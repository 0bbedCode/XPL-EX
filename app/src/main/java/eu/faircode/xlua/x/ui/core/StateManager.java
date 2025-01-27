package eu.faircode.xlua.x.ui.core;

import android.util.Log;
import android.util.Pair;

import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.x.runtime.RuntimeUtils;


public class StateManager {
    private static final String TAG = "XLua.StateManager";

    private final Map<String, Map<String, Pair<Boolean, Boolean>>> states = new HashMap<>();

    public Pair<Boolean, Boolean> flipExpanded(String tag, String id, boolean isCheckedIfNull, boolean isExpandedIfNull) {
        Pair<Boolean, Boolean> currentState = ensureState(tag, id, isCheckedIfNull, isExpandedIfNull);
        Map<String, Pair<Boolean, Boolean>> map = states.get(tag);
        if(map == null) {
            Log.e(TAG, "Critical Error! Some Reason the Map for Pairs is null (impossible fuck off abuser!) ... Stack=" + RuntimeUtils.getStackTraceSafeString());
            return null;
        }

        Pair<Boolean, Boolean> newState = Pair.create(currentState.first, !currentState.second);
        map.put(id, newState);
        return newState;
    }

    public Pair<Boolean, Boolean> flipEnabled(String tag, String id, boolean isCheckedIfNull, boolean isExpandedIfNull) {
        Pair<Boolean, Boolean> currentState = ensureState(tag, id, isCheckedIfNull, isExpandedIfNull);
        Map<String, Pair<Boolean, Boolean>> map = states.get(tag);
        if(map == null) {
            Log.e(TAG, "Critical Error! Some Reason the Map for Pairs is null (impossible fuck off abuser!) ... Stack=" + RuntimeUtils.getStackTraceSafeString());
            return null;
        }

        Pair<Boolean, Boolean> newState = Pair.create(!currentState.first, currentState.second);
        map.put(id, newState);
        return newState;
    }

    public Pair<Boolean, Boolean> ensureState(String tag, String id, boolean isCheckedIfNull, boolean isExpandedIfNull) {
        Map<String, Pair<Boolean, Boolean>> map = states.get(tag);
        Pair<Boolean, Boolean> currentState = null;
        if(map == null) {
            map = new HashMap<>();
            states.put(tag, map);
            currentState = Pair.create(isCheckedIfNull, isExpandedIfNull);
            map.put(tag, currentState);
            return currentState;
        } else {
            currentState = map.get(id);
            if(currentState == null) {
                currentState = Pair.create(isCheckedIfNull, isExpandedIfNull);
                map.put(id, currentState);
                return currentState;
            }
        }

        return currentState;
    }
}
