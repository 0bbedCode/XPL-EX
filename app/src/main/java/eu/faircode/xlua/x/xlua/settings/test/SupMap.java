package eu.faircode.xlua.x.xlua.settings.test;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.runtime.reflect.DynType;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.xlua.LibUtil;

/*
    So like:
        We open a View, we set focus of that view
        Also we can build a View Stack of Every item on view
        Then upon events iterate through each and check if it inherits IDataChange
 */

public class SupMap<T extends IIdentifiableObject> {
    private static final String TAG = LibUtil.generateTag(SupMap.class);

    private Class<T> _clazz;

    private String _focusKey = null;
    private T _focus = null;

    private final Object _lock = new Object();
    private final Map<String, T> _map = new HashMap<>();

    public T get(String key) { return _map.get(key); }
    public T put(String key, T val) { return _map.put(key, val); }
    public boolean hasKey(String key) { return _map.containsKey(key); }
    public T remove(String key) { return _map.remove(key); }

    public SupMap() { }
    public SupMap(Class<T> clazz) { this._clazz = clazz; }

    public Class<T> getClazz() {
        if(this._clazz == null) this._clazz = new DynType<T>().getClazz();
        return this._clazz;
    }

    public T focus(String key) { return focus(key, null); }
    public T focus(String key, T valueIfNull) {
        synchronized (_lock) {
            if(key != null && !key.equals(_focusKey)) {
                T val = _map.get(key);
                if(val == null) {
                    if(valueIfNull == null) {
                        try {
                            T instance = getClazz().newInstance();
                            instance.setId(key);
                            _map.put(key, instance);
                            _focus = instance;
                            _focusKey = key;
                            return instance;
                        }catch (Exception e) {
                            Log.e(TAG, "Error Constructing item for Map! Key=" + key + " Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
                            return null;
                        }
                    }

                    _map.put(key, valueIfNull);
                    _focus = valueIfNull;
                    _focusKey = key;
                    return valueIfNull;
                } else {
                    _focus = val;
                    _focusKey = key;
                    return _focus;
                }
            }

            return _focus;
        }
    }
}
