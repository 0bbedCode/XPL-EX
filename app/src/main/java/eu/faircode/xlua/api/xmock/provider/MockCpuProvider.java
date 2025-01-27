package eu.faircode.xlua.api.xmock.provider;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.cpu.MockCpu;
import eu.faircode.xlua.api.xmock.database.MockCpuManager;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class MockCpuProvider {
    private static final boolean makeSureOneSelected = false;

    private static List<MockCpu> selectedMapsCache = new ArrayList<>();
    private static List<String> mapNamesCache = new ArrayList<>();

    private static final Object lock = new Object();

    private static final String TAG = "XLua.XMockCpuApi";

    public static XResult putMockCpuMap(XDatabaseOld db, String cpuName, boolean selected) {
        XResult res = XResult.create().setMethodName("putMockCpuMap");
        synchronized (lock) {
            if(selected) {
                MockCpu map = MockCpuManager.getMap(db, cpuName, true);
                map.setSelected(true);
                if(!MockCpuManager.insertCpuMap(db, map))
                    return res.setFailed("Failed to set CPU map=[" + cpuName + "]");

                selectedMapsCache.add(map);
            }else {
                for(int i = 0; i < selectedMapsCache.size(); i++) {
                    MockCpu map = selectedMapsCache.get(i);
                    if(map.getName().equalsIgnoreCase(cpuName)) {
                        map.setSelected(false);
                        if(!MockCpuManager.insertCpuMap(db, map))
                            return res.setFailed("Failed to set CPU map=[" + cpuName + "]");

                        selectedMapsCache.remove(map);
                        return res.setSucceeded();
                    }
                }

                //If not in cache
                MockCpu map = MockCpuManager.getMap(db, cpuName, true);
                map.setSelected(false);
                if(!MockCpuManager.insertCpuMap(db, map))
                    return res.setFailed("Failed to set CPU map out of cache=[" + cpuName + "]");

            }
            return res.setSucceeded();
        }
    }

    public static MockCpu getSelectedCpuMap(Context context, XDatabaseOld db) {
        if(mapNamesCache.isEmpty()) {
            initCache(context, db);
            if(mapNamesCache.isEmpty())
                return MockCpu.EMPTY_DEFAULT;
        }

        synchronized (lock) {
            if(selectedMapsCache.size() == 1)
                return selectedMapsCache.get(0);
            else if(selectedMapsCache.size() > 1) {
                return selectedMapsCache.get(RandomGenerator.nextInt(0, selectedMapsCache.size()));
            }
        }

        String name = mapNamesCache.get(RandomGenerator.nextInt(0, mapNamesCache.size()));
        if(DebugUtil.isDebug())
            Log.d(TAG, "cpu map size selected is (0) selecting a Random Map, map=[" + name + "]");
        return MockCpuManager.getMap(db, name, true);
    }

    public static void initCache(Context context, XDatabaseOld db) {
        synchronized (lock) {
            if(mapNamesCache.isEmpty()) {
                List<MockCpu> selected = new ArrayList<>();
                List<String> allMaps = new ArrayList<>();

                Collection<MockCpu> localMaps = MockCpuManager.getCpuMaps(context, db);

                for(MockCpu map : localMaps) {
                    allMaps.add(map.getName());
                    if(map.isSelected()) {
                        selected.add(map);
                    }
                }

                selectedMapsCache = selected;
                mapNamesCache = allMaps;
            }
        }
    }
}
