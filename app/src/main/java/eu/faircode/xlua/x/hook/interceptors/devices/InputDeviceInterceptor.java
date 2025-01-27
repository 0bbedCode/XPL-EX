package eu.faircode.xlua.x.hook.interceptors.devices;

import android.util.Log;
import android.view.InputDevice;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.data.GroupedMap;
import eu.faircode.xlua.x.data.interfaces.INullableInit;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.runtime.reflect.ReflectUtil;

public class InputDeviceInterceptor {
    private static final String TAG = "XLua.InputDeviceInterceptor";

    public static boolean interceptDevice(XParam param, boolean isResult) {
        try {
            Object object = isResult ? param.getResult() : param.getThis();
            if(!(object instanceof InputDevice))
                throw new Exception("Object is not a InputDevice Object, isResult=" + (isResult) + " Object Type=" + ReflectUtil.getObjectTypeOrNull(object));

            final InputDevice inpDevice = (InputDevice) object;
            if(DebugUtil.isDebug())
                Log.d(TAG, "Is Intercepting InputDevice Object! toString=" + inpDevice.toString());

            param.setOldResult(inpDevice.toString());
            GroupedMap map = param.getGroupedMap(GroupedMap.MAP_DEVICES);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Found Devices MAP!");

            MockDevice mockDevice = map.getValueOrNullableInit(
                    "input",
                    String.valueOf(inpDevice.getId()),
                    new INullableInit() {
                        @Override
                        public Object initGetObject() { return new MockDevice(inpDevice); } });

            if(DebugUtil.isDebug())
                Log.d(TAG, "Got Mock Device from Cache, ID=" + inpDevice.getId() + " Mock toString=" + mockDevice.toString());

            boolean spoofed = mockDevice.spoofInputDevice(inpDevice);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Was InputDevice [" + inpDevice.getId() + "] Spoofed ? " + (spoofed) + " toString=" + inpDevice.toString());

            if(spoofed) {
                param.setNewResult(inpDevice.toString());
                if(isResult) param.setResult(inpDevice);
                return true;
            }

            return false;
        }catch (Throwable e) {
            Log.e(TAG, "Error Intercepting Input Device, Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            return false;
        }
    }

    public static boolean removeExternalDevices(XParam param) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "Invoking Remove External Devices...");

        try {
            int[] devIds = param.tryGetResult(null);
            List<Integer> filteredIds = new ArrayList<>();
            if(!ArrayUtils.isValid(devIds))
                throw new Exception("Input Device ID List Return Value is Null or Empty!");

            GroupedMap map = param.getGroupedMap(GroupedMap.MAP_DEVICES);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Found Devices MAP! For Input Devices List!");

            param.setOldResult("Old Device ID List Size=" + devIds.length);
            for(int id : devIds) {
                try {
                    InputDevice inpDev = InputDevice.getDevice(id);
                    MockDevice mockDev = map.getValueRaw("input.devices", String.valueOf(inpDev.getId()));
                    if(InputDeviceUtils.isBuiltInDevice(mockDev, inpDev)) {
                        if(DebugUtil.isDebug())
                            Log.d(TAG, "[>] Device Passes (its built in) => " + inpDev.toString());

                        filteredIds.add(id);
                    } else {
                        if(DebugUtil.isDebug())
                            Log.d(TAG, "[!] Devices Does not PASS (its external) => " + inpDev.toString());
                    }
                }catch (Exception e) {
                    Log.e(TAG, "Inner Exception getting Device, id=" + id + " Error=" + e);
                }
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Old Device ID List Size=" + devIds.length + " New Device ID List Size=" + filteredIds.size());

            if(devIds.length != filteredIds.size()) {
                param.setNewResult("New Device ID List Size=" + filteredIds.size());
                int[] copyArray = new int[filteredIds.size()];
                for(int i = 0; i < filteredIds.size(); i++)
                    copyArray[i] = filteredIds.get(i);

                param.setResult(copyArray);
                return true;
            }

            return false;
        }catch (Throwable e) {
            Log.e(TAG, "Error Intercepting List of Input Devices from External, Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            return false;
        }
    }
}
