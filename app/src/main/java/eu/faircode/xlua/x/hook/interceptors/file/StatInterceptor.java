package eu.faircode.xlua.x.hook.interceptors.file;

import android.util.Log;

import java.io.File;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.interceptors.shell.ShellInterception;
import eu.faircode.xlua.x.data.GroupedMap;
import eu.faircode.xlua.x.hook.interceptors.file.stat.StatContainer;

public class StatInterceptor {
    private static final String TAG = "XLua.StatInterceptor";

    public static boolean interceptStatCommand(String file, String output, ShellInterception results) {
        try {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Cleaning STAT Command Output for File=" + file + " Output=" + output);

            GroupedMap map = results.param.getGroupedMap(GroupedMap.MAP_FILE_TIMES);
            StatContainer container =  map.getValueRaw("stat", file);
            if(container == null) {
                container = new StatContainer(file, results.param);
                map.pushValue("stat", file, container);
            }

            return container.interceptCommand(output, results);
        }catch (Throwable e) {
            Log.e(TAG, "Error Intercepting STAT Command, File=" + file + " Output=" + output + " Error=" + e);
            return false;
        }
    }

    public static boolean interceptFileLastModified(XParam param) {
        try {
            File ths = (File) param.getThis();
            String file = ths.getAbsolutePath();
            if(DebugUtil.isDebug())
                Log.d(TAG, "Cleaning File.lastModified File=" + file);

            GroupedMap map = param.getGroupedMap(GroupedMap.MAP_FILE_TIMES);
            StatContainer container = map.getValueRaw("stat", file);
            if(container == null) {
                container = new StatContainer(file, param);
                map.pushValue("stat", file, container);
            }

            return container.interceptFileLastModified(param);
        }catch (Throwable e) {
            Log.e(TAG, "Error Intercepting File.lastModified! Error: " + e);
            return false;
        }
    }

    public static boolean interceptOsStat(XParam param) {
        try {
            String file = param.tryGetArgument(0, null);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Cleaning STAT Struct on File: " + file);

            GroupedMap map = param.getGroupedMap(GroupedMap.MAP_FILE_TIMES);
            StatContainer container = map.getValueRaw("stat", file);
            if(container == null) {
                container = new StatContainer(file, param);
                map.pushValue("stat", file, container);
            }

            return container.interceptStruct(param);
        }catch (Throwable e) {
            Log.e(TAG, "Error Intercepting OS Stat! Error: " + e);
            return false;
        }
    }
}
