package eu.faircode.xlua.x.xlua.database;

import android.content.Context;
import android.util.Log;

import java.util.UUID;

import de.robv.android.xposed.XposedBridge;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.XposedUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.file.ChmodModeBuilder;
import eu.faircode.xlua.x.file.FileApi;
import eu.faircode.xlua.x.file.FileEx;
import eu.faircode.xlua.x.file.ModePermission;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;

public class DatabasePathUtil {
    private static final String TAG = "XLua.DatabasePathUtil";

    public static FileEx getDatabaseFolderOrCreate(Context context) {
        if (context != null && XposedUtil.isVirtualXposed())
            return new FileEx(context.getFilesDir());

        moveDatabasesToMisc();
        //"-" + UUID.randomUUID().toString()

        FileEx base_dir = new FileEx(SQLDatabase.PATH);
        logI("Base Directory for XPL-EX Database: " + base_dir.getAbsolutePath());

        if(base_dir.exists()) {
            FileEx[] sub_files = base_dir.listFilesEx();
            if(ArrayUtils.isValid(sub_files)) {
                for(FileEx f : sub_files) {
                    if(f.getName().startsWith("xplex-")) {
                        f.takeOwnership(true);
                        f.setPermissions(FileApi.MODE_SOME_RW__770, true);
                        logI("Found XPL-EX Directory, chmod(0770) chown(1000) File=" + f.getAbsolutePath());
                        return f;
                    }
                }
            }
        }

        String new_name = FileApi.buildPath(base_dir.getAbsolutePath(), "xplex-" + UUID.randomUUID().toString());
        FileEx new_dir = new FileEx(new_name);

        logW("Created XPL-EX Folder for Databases, path=" + new_dir.getAbsolutePath());

        //Double Check "mkdirs"
        if(!new_dir.mkdirs()) {
            logE("Failed to Create the XPL-EX Directory! Path=" + new_dir.getAbsolutePath());
            return null;
        }

        new_dir.takeOwnership(true);
        new_dir.setPermissions(FileApi.MODE_SOME_RW__770, true);

        logI("Created XPL-EX Folder, Path=" + new_dir.getAbsolutePath());
        return new_dir;
    }

    public static void moveDatabasesToMisc() {
        FileEx dir = new FileEx(SQLDatabase.PATH_OLD);
        FileEx[] subFiles = dir.listFilesEx();

        logI(Str.fm("Searching XPL-EX Old sub Directory, Directory=%s  Sub File Count=%s", dir.getAbsolutePath(), ArrayUtils.safeLength(subFiles)));

        if(!ArrayUtils.isValid(subFiles))
            return;

        for(FileEx file : subFiles) {
            String name = file.getName();
            if(file.isDirectory() && name.startsWith("xplex-")) {
                int oldCount = ArrayUtils.safeLength(file.listFiles());

                logW(Str.fm("Found old XPL-EX Folder! Path=%s  Moving it to the new Folder: %s  File count in old Folder=%s", file.getAbsolutePath(), SQLDatabase.PATH, oldCount));
                if(oldCount < 1)
                    continue;

                file.takeOwnership(true);
                file.setPermissions(FileApi.MODE_SOME_RW__770, true);

                FileEx new_path = new FileEx(SQLDatabase.PATH + file.getName());
                if(new_path.exists()) {
                    logW(Str.fm("Old XPL-EX Folder exists in the new Directory! Skipping! New Path=%s  Old Path=%s", new_path.getAbsolutePath(), file.getAbsolutePath()));
                    continue;
                }

                file.copyToDirectory(new_path, true, true, FileApi.MODE_SOME_RW__770);
                if(!new_path.isDirectory()) {
                    logE(Str.fm("Failed to move old Folder: %s  to the new Folder: %s", file.getAbsolutePath(), new_path.getAbsolutePath()));
                    continue;
                }

                int newCount = ArrayUtils.safeLength(new_path.listFiles());
                if(DebugUtil.isDebug())
                    logE(Str.fm("Copied Old Folder [%s] with [%s] items, to new Folder [%s] with [%s] items", file.getAbsolutePath(), oldCount, new_path.getAbsolutePath(), newCount));

                if(newCount == oldCount) {
                    if(!file.delete()) {
                        logE(Str.fm("Failed to Delete old Folder! Old=%s", file.getAbsolutePath()));
                    }
                }
            }
        }
    }

    public static void logI(String msg) {
        Log.i(TAG, msg);
        XposedBridge.log(msg);
    }

    public static void logE(String msg) {
        Log.e(TAG, msg);
        XposedBridge.log(msg);
    }

    public static void logW(String msg) {
        Log.e(TAG, msg);
        XposedBridge.log(msg);
    }
}
