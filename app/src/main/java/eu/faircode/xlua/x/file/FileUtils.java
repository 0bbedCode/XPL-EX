package eu.faircode.xlua.x.file;

import android.os.Binder;
import android.system.Os;
import android.system.StructStat;
import android.util.Log;

import androidx.annotation.NonNull;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.LibUtil;

public class FileUtils {
    private static final String TAG = LibUtil.generateTag(FileUtils.class);

    public static String getAbsolutePath(FileEx file) { return file == null ? "null" : file.getAbsolutePath(); }

    public static List<FileEx> combineTwoLists(List<FileEx> one, List<FileEx> two, boolean organizeFromLastModified) {
        List<FileEx> items = new ArrayList<>();
        List<String> added = new ArrayList<>();

        if(ListUtil.isValid(one)) {
            for(FileEx f : one) {
                if(f != null && !added.contains(f.getAbsolutePath())) {
                    added.add(f.getAbsolutePath());
                    items.add(f);
                }
            }
        }

        if(ListUtil.isValid(two)) {
            for(FileEx f : two) {
                if(f != null && !added.contains(f.getAbsolutePath())) {
                    added.add(f.getAbsolutePath());
                    items.add(f);
                }
            }
        }

        return organizeFromLastModified ? sortFromLastModified(items) : items;
    }

    public static List<FileEx> sortFromLastModified(List<FileEx> files) {
        if(!ListUtil.isValid(files)) return files;
        Collections.sort(files, (file1, file2) -> Long.compare(file2.lastModified(), file1.lastModified()));
        return files;
    }

    public static int getModeValue(UnixAccessControl accessControl) { return accessControl != null ? getModeValue(accessControl.ownerMode, accessControl.groupMode, accessControl.otherMode) : 0; }
    public static int getModeValue(ModePermission ownerMode, ModePermission groupMode, ModePermission otherMode) { return getModeValue(ownerMode.getValue(), groupMode.getValue(), otherMode.getValue()); }
    public static int getModeValue(int ownerMode, int groupMode, int otherMode) { return (ownerMode * 100) + (groupMode * 10) + otherMode; }

    /**
     * Combines two permission modes if they don't overlap.
     * @param currentMode Current numeric permission value
     * @param newMode New permission to add
     * @return Combined permission value if modes don't overlap, otherwise current value
     */
    public static int combinePermissionModes(int currentMode, ModePermission newMode) {
        if ((currentMode & newMode.getValue()) == 0) {
            return currentMode + newMode.getValue();
        }
        return currentMode;
    }

    /**
     * Combines two permission modes if they don't overlap.
     * @param currentMode Current permission
     * @param newMode New permission to add
     * @return Combined ModePermission if modes don't overlap, otherwise current permission
     */
    public static ModePermission combinePermissionModes(ModePermission currentMode, ModePermission newMode) {
        if ((currentMode.getValue() & newMode.getValue()) == 0) {
            return ModePermission.fromValue(currentMode.getValue() + newMode.getValue());
        }
        return currentMode;
    }

    /**
     * Reads the target of a symbolic link. This will attempt to try to use the Canonical Method first read below example of when why to use Canonical method.
     * You can use the readSymbolicLink(String, bool) Overload to specify second parameter for Canonical
     *
     * <p>
     *     Canonical if selected as a param to try will remove/resolve any trailing periods like "../".
     *     If you were to resolve example "/proc/self/fd/10" Non Canonical output would look something like "../SomeFolder/SomeFile.txt".
     *     If you were to resolve example "/proc/self/fd/10" with Canonical output it would look something like "/sdcard/SomeFolder/SomeFile.txt" it resolves the ".."
     * </p>
     *
     * @param pathFile Target File with Path to resolve the Symbolic Link for.
     * @return The target path of the symbolic link, or null if the link cannot be read.
     */
    public static String readSymbolicLink(String pathFile) { return readSymbolicLink(pathFile, true); }

    /**
     * Reads the target of a symbolic link.
     *
     * <p>
     *     Canonical if selected as a param to try will remove/resolve any trailing periods like "../".
     *     If you were to resolve example "/proc/self/fd/10" Non Canonical output would look something like "../SomeFolder/SomeFile.txt".
     *     If you were to resolve example "/proc/self/fd/10" with Canonical output it would look something like "/sdcard/SomeFolder/SomeFile.txt" it resolves the ".."
     * </p>
     *
     * @param pathFile Target File with Path to resolve the Symbolic Link for.
     * @param tryCanonical Try using the Canonical Method first else use Os.readlink if failed and or set to false.
     * @return The target path of the symbolic link, or null if the link cannot be read.
     */
    @NonNull
    public static String readSymbolicLink(String pathFile, boolean tryCanonical)  {
        long oldId = Binder.clearCallingIdentity();
        try {
            if (!existsBypassPermissionsCheck(pathFile)) {
                Log.e(TAG, "Failed to Read File Symbolic Link, as File Does not exist. File: " + pathFile);
                return Str.EMPTY;
            }
            if(tryCanonical) {
                try {
                    File file = new File(pathFile);
                    String link = file.getCanonicalPath();
                    if(Str.isValid(link))
                        return link;
                }catch (Exception e) {
                    Log.e(TAG, "Error Reading Canonical Path (Symbolic Link Extended) Using [File.getCanonicalPath]. File: " + pathFile + " Error: " +  e.getMessage() + " Trying Backup [Os.readlink]");
                }
            }
            try {
                return Os.readlink(pathFile);
            } catch (Exception e) {
                Log.e(TAG, "Error Reading Symbolic Link using [Os.readlink].  File: " + pathFile + " Error: " + e.getMessage());
                return Str.EMPTY;
            }
        }finally {
            Binder.restoreCallingIdentity(oldId);
        }
    }

    /**
     * Read target File contents / Data as a UTF8 String.
     *
     * @param pathFile The File including its full Path.
     * @return The contents of the file as a String.
     */
    public static String readFileContentsAsString(String pathFile) { return readFileContentsAsString(pathFile,  StandardCharsets.UTF_8); }

    /**
     * Read target File contents / Data as a String.
     *
     * @param pathFile The File including its full Path.
     * @param charSet Set the Char Set / Encoding of String Data
     * @return The contents of the file as a String.
     */
    @NonNull
    public static String readFileContentsAsString(String pathFile, Charset charSet) {
        File file = new File(pathFile);
        if (!existsBypassPermissionsCheck(pathFile)) {
            Log.e(TAG, "Failed to read File Contents as String, as File does not Exist. File: " + pathFile);
            return Str.EMPTY;
        }

        try  (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            return new String(data, charSet);
        }catch (Exception e) {
            Log.e(TAG, "Error Reading File Contents as a String. File: " + pathFile + " CharSet: " + charSet.name() + " Error: " + e.getMessage());
            return Str.EMPTY;
        }
    }

    /**
     * Read target File contents / Data as a UTF8 String. This is to Read Virtual Files on the System
     *
     * @param pathFile The File including its full Path.
     * @return The contents of the file as a String.
     */
    public static String readVirtualFileContentsAsString(String pathFile) { return readVirtualFileContentsAsString(pathFile, StandardCharsets.UTF_8); }

    /**
     * Read target File contents / Data as a String. This is to Read Virtual Files on the System
     *
     * @param pathFile The File including its full Path.
     * @param charSet Set the Char Set / Encoding of String Data
     * @return The contents of the file as a String.
     */
    @NonNull
    public static String readVirtualFileContentsAsString(String pathFile, Charset charSet) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(pathFile), charSet))) {
            int c;
            while ((c = reader.read()) != -1) {
                if (c == 0) {
                    content.append(' ');//or New Line
                } else {
                    content.append((char) c);
                }
            }

            return content.toString().trim();
        } catch (IOException e) {
            Log.i(TAG, "Failed to Read: " + pathFile +  " Has Read: " + hasRead(pathFile) + " Error: " + e.getMessage());
            return content.toString();
        }
    }

    /**
     * Check if you have Read access to a File
     *
     * @param pathFile The File including its full Path.
     * @return True if can read else false
     */
    public static boolean hasRead(String pathFile) {
        try {
            StructStat stat = Os.stat(pathFile);
            if ((stat.st_mode & 0400) == 0)
                return false;
            return true;
        }catch (Exception ignored) {  }
        return false;
    }

    /**
     * This will Bypass Stupid Fucking Android File.exists() issues
     * Some permissions thing despite it ironically detecting if its a directory or file there for it exists...
     * I tried clearing Caller UID but not help, Despite the Process being (1000) android (system_server)
     * Try new File("/proc/3/cwd").exists() returns False but .isDirectory() returns true....
     *
     * @param fileOrDirectory File or Directory you want to check if it exists
     * @return The contents of the file as a String.
     */
    public static boolean existsBypassPermissionsCheck(String fileOrDirectory) {
        File f = new File(fileOrDirectory);
        try {
            return Os.stat(fileOrDirectory) != null || f.isFile() || f.isDirectory();
        } catch (Exception ignored) {   }
        return f.isFile() || f.isDirectory();
    }


    /**
     * Write a string to a file.
     *
     * @param file   The FileEx object representing the file to write to.
     * @param data   The string data to write.
     * @param append Whether to append to the file (true) or overwrite it (false).
     * @return True if the operation succeeds, false otherwise.
     */
    public static boolean writeStringToFile(FileEx file, String data, boolean append) {
        if (file == null || data == null) {
            Log.d(TAG, "Invalid parameters: file or data is null.");
            return false;
        }

        BufferedWriter writer = null;

        try {
            // Check if the file exists
            if (!file.exists()) {
                Log.d(TAG, "File does not exist, creating file: " + file.getAbsolutePath());
                if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    Log.d(TAG, "Failed to create parent directories: " + file.getParentFile().getAbsolutePath());
                    return false;
                }

                if (!file.createNewFile()) {
                    Log.d(TAG, "Failed to create file: " + file.getAbsolutePath());
                    return false;
                }
            }

            // Open the file in append or overwrite mode
            writer = new BufferedWriter(new FileWriter(file, append));
            writer.write(data);
            writer.flush();
            Log.d(TAG, "Successfully wrote to file: " + file.getAbsolutePath());
            return true;

        } catch (IOException e) {
            Log.d(TAG, "Error writing to file: " + file.getAbsolutePath() + " - " + e.getMessage());
            return false;
        } finally {
            // Ensure the writer is closed
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    Log.d(TAG, "Error closing file writer: " + e.getMessage());
                }
            }
        }
    }
}
