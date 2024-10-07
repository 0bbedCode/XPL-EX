package eu.faircode.xlua.utilities;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.util.UUID;

import eu.faircode.xlua.BuildConfig;
import eu.faircode.xlua.api.cpu.MockCpu;

//Make this more global , Take in contents then do work
//No need for specific params just CONTENTS
public class MockFileUtil {
    private static final String TAG = "XLua.XMockCpuUtils";

    public static FileDescriptor generateFakeBootUUIDDescriptor(String settingUuid) {
        if(BuildConfig.DEBUG)
            Log.i(TAG, "MOCK FileDescriptor Boot UUID");

        return FileUtil.generateFakeFileDescriptor(settingUuid == null ? UUID.randomUUID().toString() : settingUuid);
    }

    //unique.boot.id
    public static File generateFakeBootUUIDFile(String settingUuid) {
        if(BuildConfig.DEBUG) Log.i(TAG, "MOCK FileDescriptor Boot UUID File");
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
