package eu.faircode.xlua.x.xlua.database;

import android.content.Context;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import de.robv.android.xposed.XposedBridge;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XposedUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.file.FileApi;
import eu.faircode.xlua.x.file.FileEx;
import eu.faircode.xlua.x.file.FileTransfer;
import eu.faircode.xlua.x.file.FileUtils;
import eu.faircode.xlua.x.file.ModePermission;
import eu.faircode.xlua.x.file.UnixAccessBuilder;
import eu.faircode.xlua.x.file.UnixAccessControl;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.XposedUtility;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;

public class DatabasePathUtil {
    private static final String TAG = LibUtil.generateTag(DatabasePathUtil.class);

    public static final long MAX_FILE_SIZE = 2L * 1024 * 1024L * 1024L;

    public static final String NEW_DIRECTORY = FileApi.buildPath("data", "misc");
    public static final String OLD_DIRECTORY = FileApi.buildPath("system", "data");

    public static final String NEW_PREFIX = "xplex-";
    public static final String OLD_PREFIX = "xlua";
    public static final String X_NAME = "xlua.db";

    public static final UnixAccessControl DEFAULT_ACCESS = UnixAccessBuilder.create()
            .setOwnerUid(Process.SYSTEM_UID)
            .setGroupUid(Process.SYSTEM_UID)
            .setOwnerMode(ModePermission.READ_WRITE_EXECUTE)
            .setGroupMode(ModePermission.READ_WRITE_EXECUTE)
            .setOtherMode(ModePermission.NONE)
            .build();

    public static FileEx getDatabaseDirectory(Context context) {
        if (context != null && XposedUtil.isVirtualXposed())
            return new FileEx(context.getFilesDir());

        FileEx newBaseDirectory = FileEx.createFromDirectory(NEW_DIRECTORY);
        List<FileEx> newFolders = FileUtils.sortFromLastModified(findXplDatabaseFolders(newBaseDirectory,
                true,
                true,
                NEW_PREFIX,
                true,
                DEFAULT_ACCESS));

        if(ListUtil.isValid(newFolders)) {
            FileEx first = ListUtil.getFirst(newFolders);
            logD(Str.fm("Found Database Folder! Using Folder [%s] as XPL-EX base Folder for Databases!", first.getAbsolutePath()));
            return first;
        }

        List<FileEx> oldFolders = FileUtils.combineTwoLists(
                findXplDatabaseFolders(FileEx.createFromDirectory(OLD_DIRECTORY), true, true, NEW_PREFIX, true, DEFAULT_ACCESS),
                findXplDatabaseFolders(FileEx.createFromDirectory(OLD_DIRECTORY), true, true, OLD_PREFIX, false, DEFAULT_ACCESS),
                true);

        if(DebugUtil.isDebug())
            XposedUtility.logW_xposed(TAG, Str.fm("Missing XPL-EX Database Folder! Parent Directory=[%s] Found in old Directory [%] Count=%s",
                    NEW_DIRECTORY,
                    OLD_DIRECTORY,
                    ListUtil.size(oldFolders)));

        if(!ListUtil.isValid(oldFolders)) {
            FileEx newDirectory = newBaseDirectory.openSub(Str.combine(NEW_PREFIX, UUID.randomUUID().toString()));
            newDirectory.mkdirs();
            newDirectory.takeFullControl(DEFAULT_ACCESS, true);
            if(DebugUtil.isDebug())
                XposedUtility.logD_xposed(TAG, Str.fm("Failed to Find XPL-EX Database Folder in [%s] and in [%s] now creating and using Folder [%s] Created=%s",
                        NEW_DIRECTORY,
                        OLD_DIRECTORY,
                        newDirectory.getAbsolutePath(),
                        String.valueOf(newDirectory.isDirectory())));

            return newDirectory;
        } else {
            FileEx firstDirectory = ListUtil.getFirst(oldFolders);
            //We can also just force it a new name ?
            //We can also create it here should we ?
            FileEx newDirectory = firstDirectory.getName().equalsIgnoreCase(OLD_PREFIX) ?
                    newBaseDirectory.openSub(Str.combine(NEW_PREFIX, UUID.randomUUID().toString())) :
                    newBaseDirectory.openSub(firstDirectory.getName());

            newDirectory.mkdirs();
            int copied = FileTransfer.copyDirectory(firstDirectory, newDirectory, true, DEFAULT_ACCESS);
            if(DebugUtil.isDebug())
                XposedUtility.logD_xposed(TAG, Str.fm("Failed to Find XPL-EX Database Folder in [%s] copying from the old Folder [%s] to [%s] Copied Count=%s Is Directory=%s",
                        NEW_DIRECTORY,
                        firstDirectory.getAbsolutePath(),
                        newDirectory.getAbsolutePath(),
                        String.valueOf(copied),
                        String.valueOf(newDirectory.isDirectory())));

            return newDirectory;
        }
    }

    public static List<FileEx> findXplDatabaseFolders(
            FileEx source,
            boolean hasXDatabase,
            boolean deleteEmpty,
            String prefix,
            boolean startsWith,
            UnixAccessControl access) {
        List<FileEx> files = new ArrayList<>();
        if(!source.isDirectory() || Str.isEmpty(prefix)) {
            logE(Str.fm("Source is not a Directory [%s] is Directory ? %s or Prefix [%s] is Empty / Null! ",
                    source.getAbsolutePath(),
                    String.valueOf(source.isDirectory()),
                    Str.toStringOrNull(prefix)));
            return files;
        }

        FileEx[] subFiles = source.listFilesEx();
        if(!ArrayUtils.isValid(subFiles)) {
            logE(Str.fm("No Sub Files in Directory [%s] Prefix=%s",
                    source.getAbsolutePath(),
                    prefix));
            return files;
        }

        if(DebugUtil.isDebug())
            logD(Str.fm("Starting Database Folder search in Directory [%s] Prefix [%s] HasXDatabase Flag=%s Delete If Empty Flag=%s Starts With Flag=%s",
                    source.getAbsolutePath(),
                    prefix,
                    String.valueOf(hasXDatabase),
                    String.valueOf(deleteEmpty),
                    String.valueOf(startsWith)));

        String prefixLower = prefix.toLowerCase();
        for(FileEx sub : subFiles) {
            String name = sub.getName();
            if(Str.isEmpty(name) || !sub.isDirectory())
                continue;

            if((startsWith && name.toLowerCase().startsWith(prefixLower)) || (!startsWith && name.equalsIgnoreCase(prefixLower))) {
                sub.takeFullControl(access, true);
                if(hasXDatabase) {
                    FileEx database = sub.openSub(X_NAME);
                    if(!database.isFile()) {
                        if(deleteEmpty) {
                            boolean res = sub.delete();
                            if(DebugUtil.isDebug())
                                logD(Str.fm("Deleted Empty XPL Directory [%s] from [%s] Prefix [%s] Result=%s",
                                        sub.getName(),
                                        source.getAbsolutePath(),
                                        prefix,
                                        res));
                        }

                        logD(Str.fm("Found XPL Directory [%s] in Source Directory [%s] with Prefix [%s] but it lacks a [xlua.db] File! Skipping",
                                sub.getName(),
                                source.getAbsolutePath(),
                                prefix));
                        continue;
                    }
                }

                files.add(sub);
                if(DebugUtil.isDebug())
                    logD(Str.fm("Found XPL Directory [%s] in Source Directory [%s] Prefix=%s",
                            sub.getName(),
                            source.getAbsolutePath(),
                            prefix));
            }
        }

        return files;
    }

    public static void logD(String msg) { XposedUtility.logD_xposed(TAG, msg); }
    public static void logI(String msg) { XposedUtility.logI_xposed(TAG, msg); }
    public static void logE(String msg) { XposedUtility.logE_xposed(TAG, msg); }
    public static void logW(String msg) { XposedUtility.logW_xposed(TAG, msg); }
}
