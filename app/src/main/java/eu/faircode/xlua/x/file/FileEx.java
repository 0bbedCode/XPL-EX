package eu.faircode.xlua.x.file;

import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.runtime.reflect.DynamicMethod;
import eu.faircode.xlua.x.xlua.LibUtil;

public class FileEx extends File {
    private static final String TAG = LibUtil.generateTag(FileEx.class);

    public static FileEx createFromFile(String file) { return new FileEx(file, false, false); }
    public static FileEx createFromDirectory(String directory) { return new FileEx(directory, false, false);  }

    //Method cpTo = XReflectUtils.getMethodFor("android.os.FileUtils", "copy", File.class, File.class);
    private static final DynamicMethod COPY_FILE_METHOD = new DynamicMethod("android.os.FileUtils", "copy", File.class, File.class)
            .setAccessible(true);

    public static final String SELF_FD_PATH = FileApi.buildPath("proc", "self", "fd") + separator;
    public static final String SDCARD_PATH = FileApi.buildPath("sdcard") + separator;

    public FileEx(int descriptorNumber) { super(FileUtils.readSymbolicLink( SELF_FD_PATH + descriptorNumber)); }
    public FileEx(String file) { super(ensureFormat(file, false, true)); }
    public FileEx(String file, boolean readAsSymbolic, boolean parseIfFileDescriptor) { super(ensureFormat(file, readAsSymbolic, parseIfFileDescriptor));  }
    public FileEx(File file) { super(file.getAbsolutePath()); }

    public FileEx openSub(String name) { return new FileEx(FileApi.buildPath(getAbsolutePath(), name)); }

    @Override
    public boolean delete() { return FileApi.deleteFileOrDirectoryForcefully(getAbsolutePath()); }

    @NonNull
    @Override
    public String getCanonicalPath() throws IOException { return FileUtils.readSymbolicLink(getAbsolutePath(), true); }

    @NonNull
    @Override
    public File getCanonicalFile() throws IOException { return new File(getCanonicalPath()); }

    @Override
    public boolean exists() { return FileApi.exists(getAbsolutePath()); }

    @NonNull
    @SuppressWarnings("unused")
    public String readVirtualFileContents(Charset chars) { return exists() ? FileUtils.readVirtualFileContentsAsString(getAbsolutePath(), chars) : ""; }

    @NonNull
    @SuppressWarnings("unused")
    public String readFileContents(Charset chars) { return exists() ? FileUtils.readFileContentsAsString(getAbsolutePath(), chars) : ""; }

    @Override
    public boolean isFile() { return FileApi.isFile(getAbsolutePath()) || super.isFile(); }

    @Override
    public boolean isDirectory() { return FileApi.isDirectory(getAbsolutePath()) || super.isDirectory(); }

    @Override
    public boolean mkdirs() {
        if(super.mkdirs()) return true;
        FileApi.mkdirs(getAbsolutePath());
        return exists();
    }

    public FileEx getDirectory() { return new FileEx(FileApi.getParent(getAbsolutePath())); }

    public FileEx getParentEx() { return getParentEx(false); }
    public FileEx getParentEx(boolean isFileOverride) {
        if(isFileOverride || isFile())
            return new FileEx(FileApi.getParent(FileApi.getParent(getAbsolutePath())));

        return new FileEx(FileApi.getParent(getAbsolutePath()));
    }

    public Pair<Integer, Integer> fileAndDirectoryCount(boolean recursive) {
        int file = 0;
        int directory = 0;

        FileEx[] files = listFilesEx();
        if(ArrayUtils.isValid(files)) {
            for(FileEx f : files) {
                if(f.isDirectory()) {
                    directory++;
                    if(recursive) {
                        Pair<Integer, Integer> res = f.fileAndDirectoryCount(true);
                        file += res.first;
                        directory += res.second;
                    }
                }
                else if(f.isFile()) {
                    file++;
                }
            }
        }
        return Pair.create(file, directory);
    }

    public FileEx[] listFilesEx() {
        File[] files = super.listFiles();
        if(!ArrayUtils.isValid(files))
            return null;

        FileEx[] files_new = new FileEx[files.length];
        for(int i = 0; i < files.length; i++) {
            files_new[i] = new FileEx(files[i]);
        }

        return files_new;
    }

    public void takeOwnership() { FileApi.chown(getAbsolutePath(), Process.myUid(), Process.myUid(), isDirectory()); }
    public void takeOwnership(boolean recursive) { takeOwnership(Process.myUid(), Process.myUid(), recursive); }

    public void takeOwnership(int userId, int groupId) { takeOwnership(userId, groupId, false); }
    public void takeOwnership(int userId, int groupId, boolean recursive) {  FileApi.chown(getAbsolutePath(), userId, groupId, recursive); }


    public void takeFullControl(UnixAccessControl permissions, boolean recursive) {
        if(permissions != null) {
            takeOwnership(permissions.getOwnerId(), permissions.getGroupId(), recursive);
            setPermissions(
                    permissions.ownerMode,
                    permissions.groupMode,
                    permissions.otherMode,
                    recursive);
        }
    }

    public void setPermissions(int mode) { setPermissions(mode, false); }
    public void setPermissions(int mode, boolean recursive) { FileApi.chmod(getAbsolutePath(), mode, recursive); }

    public void setPermissions(ModePermission ownerPermissions, ModePermission groupPermissions, ModePermission otherPermissions) { setPermissions(ownerPermissions, groupPermissions, otherPermissions, true); }
    public void setPermissions(ModePermission ownerPermissions, ModePermission groupPermissions, ModePermission otherPermissions, boolean recursive) {
        FileApi.chmod(getAbsolutePath(), UnixAccessBuilder.create()
                .setOwnerMode(ownerPermissions)
                .setGroupMode(groupPermissions)
                .setOtherMode(otherPermissions)
                .getMode(), recursive);
    }

    public static String ensureFormat(String fileOrDirectory, boolean readAsSymbolic, boolean parseIfFileDescriptor) {
        if(TextUtils.isEmpty(fileOrDirectory))
            return separator;

        fileOrDirectory = fileOrDirectory.trim();
        if(TextUtils.isEmpty(fileOrDirectory))
            return separator;

        String del = FileApi.getPathDelimiter(fileOrDirectory, true);
        //String cleaned = Str.trim(fileOrDirectory, del, true, false);
        String cleaned = Str.trimEx(fileOrDirectory, true, true, true, true, del);
        if(TextUtils.isEmpty(cleaned))
            return fileOrDirectory; //Maybe just separator

        //something/something/something
        //or
        //something
        String withStart = del + cleaned;
        if(DebugUtil.isDebug())
            Log.d(TAG, "FILE_EX(" + withStart + ")");

        if(readAsSymbolic) {
            return FileUtils.readSymbolicLink(withStart, true);
        } else {
            if(parseIfFileDescriptor) {
                if(cleaned.length() > 4) {
                    String low = cleaned.toLowerCase();
                    if(low.startsWith("proc") && low.contains(del + "fd" + del)) {
                        List<String> pts = FileApi.getParts(low);
                        if(pts.size() == 4) {
                            // =>  /proc/somePid/fd/fileNumber
                            if(pts.get(pts.size() - 2).equalsIgnoreCase("fd")) {
                                String link = FileUtils.readSymbolicLink(withStart, true);
                                return link.isEmpty() ? withStart : link;
                            }
                        }
                    }
                }

                if(!cleaned.contains(del)) {
                    char[] chars = cleaned.toCharArray();
                    boolean isAllNum = true;
                    for(int i = 0; i < chars.length; i++) {
                        if(!Character.isDigit(chars[i])) {
                            isAllNum = false;
                            break;
                        }
                    }

                    if(isAllNum) {
                        String fd = SELF_FD_PATH + cleaned;
                        if(FileApi.exists(fd)) {
                            String link = FileUtils.readSymbolicLink(fd, true);
                            return link.isEmpty() ? withStart : link;
                        }
                    }
                }
            }
        }

        return withStart;
    }
}
