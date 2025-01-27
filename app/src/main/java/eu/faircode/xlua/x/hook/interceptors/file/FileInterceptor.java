package eu.faircode.xlua.x.hook.interceptors.file;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.GroupedMap;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.runtime.RuntimeUtils;

public class FileInterceptor {
    private static final String TAG = "XLua.FileInterceptor";

    private static final Object lock = new Object();

    public static final String SETTING_BLOCK_FILE_LIST = "storage.block.files";
    public static final String SETTING_BLOCK_DIRECTORY_LIST = "storage.block.directories";
    public static final String SETTING_ALLOW_FILE_LIST = "storage.allow.files";
    public static final String SETTING_ALLOW_DIRECTORY_LIST = "storage.allow.directories";

    public static final HashMap<String, Boolean> FILES = new HashMap<>();
    public static final HashMap<String, Boolean> DIRECTORIES = new HashMap<>();

    public static final List<String> BLOCKED_WILD = new ArrayList<>();
    public static final List<String> ALLOWED_WILD = new ArrayList<>();

    private static final AtomicBoolean hasInit = new AtomicBoolean(false);

    //private static volatile boolean hasInit = false;
    public static void ensureInit(XParam param) {
        if(hasInit.compareAndSet(false, true)) {
            synchronized (lock) {
                String[] allowedFiles = Str.split(param.getSetting(SETTING_ALLOW_FILE_LIST), ",", true);
                if(ArrayUtils.isValid(allowedFiles)) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Allowed Files=" + allowedFiles.length);

                    for(String a : allowedFiles) {
                        if(a.contains("*")) ALLOWED_WILD.add(a.replaceAll("\\*", ""));
                        else FILES.put(a, true);
                    }
                }

                String[] allowedDirectories = Str.split(param.getSetting(SETTING_ALLOW_DIRECTORY_LIST), ",", true);
                if(ArrayUtils.isValid(allowedDirectories)) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Allowed Directories=" + allowedDirectories.length);

                    for(String a : allowedDirectories) {
                        if(a.contains("*")) ALLOWED_WILD.add(a.replaceAll("\\*", ""));
                        else DIRECTORIES.put(a, true);
                    }
                }

                String[] blockedFiles = Str.split(param.getSetting(SETTING_BLOCK_FILE_LIST), ",", true);
                if(ArrayUtils.isValid(blockedFiles)) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Blocked Files=" + blockedFiles.length);

                    for(String b : blockedFiles) {
                        if(b.contains("*")) BLOCKED_WILD.add(b.replaceAll("\\*", ""));
                        else FILES.put(b, false);
                    }
                }

                String[] blockedDirectories = Str.split(param.getSetting(SETTING_BLOCK_DIRECTORY_LIST), ",", true);
                if(ArrayUtils.isValid(blockedDirectories)) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Blocked Directories=" + blockedDirectories.length);

                    for(String b : blockedDirectories) {
                        if(b.contains("*")) BLOCKED_WILD.add(b.replaceAll("\\*", ""));
                        else DIRECTORIES.put(b, false);
                    }
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG, "Finished Creating Cache, Files=" + FILES.size() + " DIRECTORIES=" + DIRECTORIES.size() + " Wild Card Allowed=" + ALLOWED_WILD.size() + " Wild Card Blocked=" + BLOCKED_WILD.size());
            }
        }
    }

    public static boolean interceptOpen(XParam param) {
        try {
            ensureInit(param);
            String item = param.tryGetArgument(0, null);
            if(item != null) {
                boolean isAllowed = isAllowed(Str.getParentPath(item)) && isAllowed(item);
                if(!isAllowed) {
                    param.setOldResult(item + " open(allow)");
                    param.setOldResult(item + " open(block)");
                    param.setResult(new FileNotFoundException(item));
                    return true;
                }
            }
            return false;
        }catch (Throwable e) {
            Log.e(TAG, "Error Intercepting .open(File) Error=" + e);
            return false;
        }
    }

    public static boolean interceptExistsOrIsFileOrDirectory(XParam param) {
        try {
            ensureInit(param);
            Object ths = param.getThis();
            if(ths instanceof File) {
                File object = (File)ths;
                Object res = param.tryGetResult(null);
                if(Boolean.TRUE.equals(res)) {
                    boolean isAllowed = isAllowed(Str.getParentPath(object.getAbsolutePath())) && isAllowed(object.getAbsolutePath());
                    if(!isAllowed) {
                        param.setOldResult(object.getAbsolutePath() + " (Exists) => True");
                        param.setNewResult(object.getAbsolutePath() + " (Exists) => False");
                        param.setResult(false);
                        return true;
                    }
                }
            }
            return false;
        }catch (Throwable e) {
            Log.e(TAG, "Error Intercepting File.exists() ! Error=" + e);
            return false;
        }
    }

    public static boolean interceptList(XParam param) {
        try {
            ensureInit(param);
            Object ths = param.getThis();
            if(ths instanceof File) {
                File object = (File) ths;
                String path = object.getAbsolutePath();
                if(!isAllowed(path)) {
                    Log.w(TAG, "Path is Not Allowed and or Blocked: " + path);
                    param.setResult(null);
                } else {
                    Object result = param.getResult();
                    if(result instanceof String[]) {
                        List<String> allowed = new ArrayList<>();
                        String[] items = (String[]) result;
                        for(String f : items)
                            if(isAllowed(f))
                                allowed.add(f);

                        if(items.length != allowed.size()) {
                            param.setResult(allowed.toArray(new String[0]));
                            return true;
                        }
                    }
                    else if(result instanceof File[]) {
                        List<File> allowed = new ArrayList<>();
                        File[] items = (File[]) result;
                        for(File f : items)
                            if(isAllowed(f.getAbsolutePath()))
                                allowed.add(f);

                        if(items.length != allowed.size()) {
                            param.setResult(allowed.toArray(new File[0]));
                            return true;
                        }
                    } else {
                        Log.e(TAG, "Return Result from File.list() is null...");
                        return false;
                    }
                }
            }
        }catch (Throwable e) {
            Log.e(TAG, "Error Filtering Files! " + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            return false;
        }

        return false;
    }

    public static boolean isAllowed(String item) {
        boolean isAllowed = internalIsAllowed(item);
        if(DebugUtil.isDebug())
            Log.d(TAG, "Is Allowed ? " + item + " Is:" + (isAllowed));

        return isAllowed;
    }

    private static boolean internalIsAllowed(String item) {
        if(item == null) return true;
        Boolean checkOne = FILES.get(item);
        Boolean checkTwo = DIRECTORIES.get(item);
        if(Boolean.TRUE.equals(checkOne))
            return true;
        else if(Boolean.FALSE.equals(checkOne))
            return false;

        if(Boolean.TRUE.equals(checkTwo))
            return true;
        else if(Boolean.FALSE.equals(checkTwo))
            return false;

        for(String a : ALLOWED_WILD)
            if(item.toLowerCase().contains(a))
                return true;

        for(String b : BLOCKED_WILD)
            if(item.toLowerCase().contains(b))
                return false;

        return true;
    }
}
