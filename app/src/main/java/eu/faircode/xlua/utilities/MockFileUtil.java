package eu.faircode.xlua.utilities;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.util.UUID;

import eu.faircode.xlua.BuildConfig;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.cpu.MockCpu;

//Make this more global , Take in contents then do work
//No need for specific params just CONTENTS
public class MockFileUtil {
    private static final String TAG = "XLua.XMockCpuUtils";

    public static File generateFakeInet6File(String contents) {
        if(DebugUtil.isDebug()) Log.d(TAG, "[MOCK FILE] For Inet6 File");
        return FileUtil.generateTempFakeFile(contents);
    }

    public static FileDescriptor generateFakeBootUUIDDescriptor(String settingUuid) {
        if(DebugUtil.isDebug()) Log.d(TAG, "MOCK FileDescriptor Boot UUID: " + settingUuid);
        return FileUtil.generateFakeFileDescriptor(settingUuid == null ? UUID.randomUUID().toString() : settingUuid);
    }

    public static File generateFakeBootUUIDFile(String settingUuid) {
        if(DebugUtil.isDebug()) Log.d(TAG, "MOCK File Boot UUID File: " + settingUuid);
        return FileUtil.generateTempFakeFile(settingUuid == null ? UUID.randomUUID().toString() : settingUuid);
    }

    public static FileDescriptor generateFakeFileDescriptor(MockCpu map) {
        if(BuildConfig.DEBUG) Log.i(TAG, "MOCK FileDescriptor For: " + map);
        return FileUtil.generateFakeFileDescriptor(map.getContents());
    }

    public static File generateFakeFile(MockCpu map) {
        if(BuildConfig.DEBUG) Log.i(TAG, "MOCK File For: " + map);
        return FileUtil.generateTempFakeFile(map.getContents());
    }
}
