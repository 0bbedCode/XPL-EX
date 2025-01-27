package eu.faircode.xlua.x.data;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.x.data.interfaces.INullableInit;
import eu.faircode.xlua.x.runtime.reflect.DynamicType;

public class GroupedMap {
    public static final String MAP_APP_TIMES = "OBC.AppList.Times";
    public static final String MAP_FILE_TIMES = "OBC.File.Settings";
    public static final String MAP_FILE_FILTER = "OBC.File.Filter";
    public static final String MAP_APP_LIST = "OBC.AppList.Blocked";
    public static final String MAP_DEVICES = "OBC.Devices";

    private static final String TAG = "XLua.GroupedMap";
    private final Map<String, Map<String, Object>> mMaps = new HashMap<>();
    private final Map<String, Map<Object, String>> mModifiedMaps = new HashMap<>();

    private String mCurrentGroup;
    private Map<String, Object> mCurrentGroupMap;
    private Map<Object, String> mCurrentModifiedMap;

    public boolean hasGroup(String groupName) {
        if(groupName == null) return false;
        synchronized (mMaps) {
            return mMaps.containsKey(groupName);
        }
    }

    public void setGroup(String groupName) { setGroup(groupName, false); }
    public void setGroup(String groupName, boolean useModifiedMap) {
        synchronized (mMaps) {
            internalSetGroupFocus(groupName, useModifiedMap);
        }
    }

    public boolean isModifiedValue(String groupName, Object possiblyModifiedKey) {
        if(groupName == null || possiblyModifiedKey == null) return false;
        synchronized (mMaps) {
            internalSetGroupFocus(groupName, true);
            return internalIsModified(possiblyModifiedKey);
        }
    }

    public boolean hasValue(String groupName, String keyName) {
        if(groupName == null || keyName == null) return false;
        synchronized (mMaps) {
            internalSetGroupFocus(groupName, true);
            return internalHasKeyValue(keyName);
        }
    }

    public String getValueOrSetting(String groupName, String keyName, XParam param, String settingName) { return getValueOrSetting(groupName, keyName, param, settingName, true, false); }
    public String getValueOrSetting(String groupName, String keyName, XParam param, String settingName, boolean useModifiedMap) { return getValueOrSetting(groupName, keyName, param, settingName, useModifiedMap, false); }
    public String getValueOrSetting(
            String groupName,
            String keyName,
            XParam param,
            String settingName,
            boolean useModifiedMap,
            boolean pushIfNull) {
        if(groupName == null || keyName == null) return null;
        synchronized (mMaps) {
            internalSetGroupFocus(groupName, useModifiedMap);
            String val = internalGetValue(keyName, useModifiedMap);
            if(val == null && param != null && settingName != null) {
                String settingValue = param.getSetting(settingName);
                if(pushIfNull || settingValue != null) {
                    internalPushValue(keyName, settingValue, useModifiedMap);
                    return settingValue;
                }
            }
            return val;
        }
    }

    public String getValueOrRandomize(String groupName, String keyName, IRandomizerOld randomizer) { return getValueOrRandomize(groupName, keyName, randomizer, true, false); }
    public String getValueOrRandomize(String groupName, String keyName, IRandomizerOld randomizer, boolean useModifiedMap) { return getValueOrRandomize(groupName, keyName, randomizer, useModifiedMap, false); }
    public String getValueOrRandomize(
            String groupName,
            String keyName,
            IRandomizerOld randomizer,
            boolean useModifiedMap,
            boolean pushIfNull) {
        if(keyName == null || groupName == null) return null;
        synchronized (mMaps) {
            internalSetGroupFocus(groupName, useModifiedMap);
            String val = internalGetValue(keyName, useModifiedMap);
            if(val == null && randomizer != null) {
                String randomValue = randomizer.generateString();
                if(pushIfNull || randomValue != null) {
                    internalPushValue(keyName, randomValue, useModifiedMap);
                    return randomValue;
                }
            }
            return val;
        }
    }

    public <T> T getValueOrDefault(String groupName, String keyName, T defaultValue) { return getValueOrDefault(groupName, keyName, defaultValue, true, false); }
    public <T> T getValueOrDefault(String groupName, String keyName, T defaultValue, boolean useModifiedMap) { return getValueOrDefault(groupName, keyName, defaultValue, useModifiedMap, false); }
    public <T> T getValueOrDefault(
            String groupName,
            String keyName,
            T defaultValue,
            boolean useModifiedMap,
            boolean pushIfNull) {
        if(keyName == null || groupName == null) return null;
        synchronized (mMaps) {
            internalSetGroupFocus(groupName, useModifiedMap);
            T val = internalGetValue(keyName, useModifiedMap);
            if(val == null) {
                if(pushIfNull || defaultValue != null) {
                    internalPushValue(keyName, defaultValue, useModifiedMap);
                    return defaultValue;
                }
            }
            return val;
        }
    }

    public <T> T getValueOrNullableInit(String groupName, String keyName, INullableInit initDelegate) { return getValueOrNullableInit(groupName, keyName, initDelegate, true, false); }
    public <T> T getValueOrNullableInit(String groupName, String keyName, INullableInit initDelegate, boolean useModifiedMap) { return getValueOrNullableInit(groupName, keyName, initDelegate, useModifiedMap, false); }
    public <T> T getValueOrNullableInit(
            String groupName,
            String keyName,
            INullableInit initDelegate,
            boolean useModifiedMap,
            boolean pushIfNull) {
        if(keyName == null || groupName == null) return null;
        synchronized (mMaps) {
            internalSetGroupFocus(groupName, useModifiedMap);
            T val = internalGetValue(keyName, useModifiedMap);
            if(val == null) {
                Object newVal = initDelegate.initGetObject();
                if(pushIfNull || newVal != null) {
                    internalPushValue(keyName, newVal, useModifiedMap);
                    return DynamicType.tryCast(newVal);
                }
            }
            return val;
        }
    }

    public <T> T getValueRaw(String groupName, String keyName) { return getValueRaw(groupName, keyName, true); }
    public <T> T getValueRaw(
            String groupName,
            String keyName,
            boolean useModifiedMap) {
        if(keyName == null || groupName == null) return null;
        synchronized (mMaps) {
            internalSetGroupFocus(groupName, useModifiedMap);
            return internalGetValue(keyName, useModifiedMap);
        }
    }

    public void pushValue(String groupName, String keyName, Object value) { pushValue(groupName, keyName, value, true); }
    public void pushValue(
            String groupName,
            String keyName,
            Object value,
            boolean useModified) {
        synchronized (mMaps) {
            internalSetGroupFocus(groupName, useModified);
            internalPushValue(keyName, value, useModified);
        }
    }

    private boolean internalIsModified(Object possiblyModifiedKey) { return mCurrentModifiedMap != null && mCurrentModifiedMap.containsKey(possiblyModifiedKey); }
    private String internalGetOriginalModifiedKey(Object possiblyModifiedKey) { return mCurrentModifiedMap == null ? null : mCurrentModifiedMap.get(possiblyModifiedKey); }

    private boolean internalHasKeyValue(String keyName) { return mCurrentGroupMap != null && mCurrentGroupMap.containsKey(keyName); }
    private Object internalGetValue(String keyName) { return mCurrentGroupMap == null ? null : mCurrentGroupMap.get(keyName); }

    private void internalPushValue(
            String keyName,
            Object value,
            boolean useModified) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "[PushValue] KeyName=" + keyName + " Value=" + value + " Use Modified=" + useModified);

        if(useModified && mCurrentModifiedMap != null) mCurrentModifiedMap.put(value, keyName);
        if(mCurrentGroupMap != null) mCurrentGroupMap.put(keyName, value);
    }

    private <T> T internalGetValue(
            String keyName,
            boolean useModifiedMap) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "[GetValue] KeyName=" + keyName + " Use Modified=" + useModifiedMap);

        if(useModifiedMap) {
            if(internalIsModified(keyName)) {
                String originalKey = internalGetOriginalModifiedKey(keyName);   //Prevents Modifying a Already Modified Value
                return DynamicType.tryCast(internalGetValue(originalKey));      //So Return the Modified Object already cached from the Original Key
                                                                                //More Common with Types of Strings like IpAddresses IDs etc
            }
        }

        return DynamicType.tryCast(internalGetValue(keyName));
    }

    private void internalSetGroupFocus(String groupName) { internalSetGroupFocus(groupName, true); }
    private void internalSetGroupFocus(String groupName, boolean useModifiedMap) {
        if(groupName != null && !groupName.equals(mCurrentGroup)) {
            mCurrentGroup = groupName;
            mCurrentGroupMap = mMaps.get(groupName);
            mCurrentModifiedMap = mModifiedMaps.get(groupName);

            if(mCurrentGroupMap == null) {
                mCurrentGroupMap = new HashMap<>();
                mMaps.put(groupName, mCurrentGroupMap);
            }

            if(useModifiedMap && mCurrentModifiedMap == null) {
                mCurrentModifiedMap = new HashMap<>();
                mModifiedMaps.put(groupName, mCurrentModifiedMap);
            }
        }
    }
}
