package eu.faircode.xlua.api.xmock.provider;

import android.content.Context;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.xmock.database.MockPropManager;
import eu.faircode.xlua.api.properties.MockPropMap;
import eu.faircode.xlua.api.properties.MockPropPacket;
import eu.faircode.xlua.api.properties.MockPropSetting;
import eu.faircode.xlua.utilities.CollectionUtil;

public class MockPropProvider {
    private static String TAG = "XLua.MockPropProvider";

    //KEY=(Property Name) VALUE=Setting it points too
    private static HashMap<String, MockPropMap> mappedProperties = new HashMap<>();
    private static final Object lock = new Object();

    /*public static String getPropertyValue(Context context, XDatabase db, String propertyName,  int user, String packageName) {
        MockPropMap map = mappedProperties.get(propertyName);
        if(map == null)
            return MockUtils.NOT_BLACKLISTED;

        String sName = map.getSettingName();
        String value = LuaSettingsDatabase.getSettingValue(context, db, sName, user, packageName);
        //if(value == null)
        //    value = XMockSettingsProvider.getDefaultSettingValue(context, db, sName);
        //fix

        if(value == null)
            return MockUtils.NOT_BLACKLISTED;

        return value;
    }*/

    public static Collection<MockPropMap> getMockPropMaps(Context context, XDatabaseOld db) {
        initCache(context, db);
        Log.i(TAG, "mapped properties size=" + mappedProperties.size());
        return mappedProperties.values();
    }

    public static XResult putMockPropMap(Context context, XDatabaseOld db, MockPropPacket packet) {
        //Ew wtf is this cancer
        initCache(context, db);
        Log.i(TAG, "Before Mapped Properties=" + mappedProperties.size());
        //packet.resolveUserID();
        XResult res = MockPropManager.putSettingMapForProperty(db, packet);
        if(res.succeeded() && !packet.isDeleteMap()) { synchronized (lock) { mappedProperties.put(packet.getName(), packet.createMap()); } }
        else if(res.succeeded()) { synchronized (lock) { mappedProperties.remove(packet.getName()); } }
        Log.i(TAG, "mock prop map insert result=" + res.getMessage() + " mapped properties after=" + mappedProperties.size());
        return res;
    }

    public static Collection<MockPropSetting> getSettingsForPackage(Context context, XDatabaseOld db, int user, String packageName, boolean getAll) { return getSettingsForPackage(context, db, MockPropPacket.createQueryRequest(user, packageName, getAll));  }
    public static Collection<MockPropSetting> getSettingsForPackage(Context context, XDatabaseOld db, MockPropPacket packet) {
        Log.i(TAG, "Entering [getSettingsForPackage] packet=" + packet);
        int user = packet.getUser();
        String packageName = packet.getCategory();
        boolean getAll = packet.isGetAll();

        Log.i(TAG, "[getSettingsForPackage] db=" + db.getName() + " user=" + user + " pkg=" + packageName + " all=" + getAll);
        initCache(context, db);
        Collection<MockPropSetting> userSettings = MockPropManager.getPropertySettingsForUser(db, user, packageName);
        Log.i(TAG, "[getSettingsForPackage] user settings size=" + userSettings.size());
        if(!getAll) return userSettings;

        HashMap<String, MockPropSetting> allSettings = new HashMap<>(userSettings.size());
        if(CollectionUtil.isValid(userSettings))
            for(MockPropSetting s : userSettings)
                allSettings.put(s.getName(), s);

        Log.i(TAG, "[getSettingsForPackage] user settings (2) =" + allSettings.size() + " mapped properties=" + mappedProperties.size());

        synchronized (lock) {
            for(Map.Entry<String, MockPropMap> e : mappedProperties.entrySet()) {
                String k = e.getKey();
                MockPropMap m = e.getValue();
                if(!allSettings.containsKey(k)) {
                    MockPropSetting mSetting = MockPropSetting.create(user, packageName,  k, m.getSettingName(), MockPropSetting.PROP_NULL);
                    allSettings.put(k, mSetting);
                }
            }
        }

        Log.i(TAG, "[getSettingsForPackage] total user settings (all)=" + allSettings.size());
        return allSettings.values();
    }

    public static void initCache(Context context, XDatabaseOld db) {
        if (!CollectionUtil.isValid(mappedProperties)) {
            synchronized (lock) {
                if (!CollectionUtil.isValid(mappedProperties)) {
                    //Still null even after lock que
                    HashMap<String, MockPropMap> maps = new HashMap<>();
                    Collection<MockPropMap> settings = MockPropManager.forceCheckMapsDatabase(context, db);
                    for (MockPropMap set : settings)
                        maps.put(set.getName(), set);

                    mappedProperties = maps;
                    Log.i(TAG, "mapped settings =" + maps.size());
                }
            }
        }
    }
}
