package eu.faircode.xlua.utilities;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.util.UUID;

import eu.faircode.xlua.api.cpu.MockCpu;

//Make this more global , Take in contents then do work
//No need for specific params just CONTENTS
public class MockFileUtil {
    private static final String TAG = "XLua.XMockCpuUtils";

    public static FileDescriptor generateFakeBootUUIDDescriptor() {
        Log.i(TAG, "MOCK FileDescriptor Boot UUID");
        return FileUtil.generateFakeFileDescriptor(UUID.randomUUID().toString());
    }

    public static File generateFakeBootUUIDFile() {
        Log.i(TAG, "MOCK FileDescriptor Boot UUID File");
        return FileUtil.generateTempFakeFile(UUID.randomUUID().toString());
    }

    public static FileDescriptor generateFakeFileDescriptor(MockCpu map) {
        Log.i(TAG, "MOCK FileDescriptor For: " + map);
        return FileUtil.generateFakeFileDescriptor(map.getContents());
    }

    public static File generateFakeFile(MockCpu map) {
        Log.i(TAG, "MOCK File For: " + map);
        return FileUtil.generateTempFakeFile(map.getContents());
    }
}
