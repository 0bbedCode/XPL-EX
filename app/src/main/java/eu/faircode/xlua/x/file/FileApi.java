package eu.faircode.xlua.x.file;

import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructStat;
import android.text.TextUtils;
import android.util.Log;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.utilities.StreamUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.runtime.RuntimeUtils;


public class FileApi {
    private static final String TAG = "XLua.FileApi";

    public static ChmodModeBuilder customChmodMode() { return ChmodModeBuilder.create(); }

    //0770 is mode from default XPL-EX (7 + 7 + 0) (RWX[Owner], RWX[Owner], None[Other])
    //Issue is if a ROOT File manager create dir it CAN cause permissions issues given ROOT > SYSTEM
    public static final int MODE_ALL_RWX__777 = customChmodMode()
            .setOwnerPermissions(ModePermission.READ_WRITE_EXECUTE) //7
            .setGroupPermissions(ModePermission.READ_WRITE_EXECUTE) //7
            .setOtherPermissions(ModePermission.READ_WRITE_EXECUTE) //7
            .getMode();                                             //777

    public static final int MODE_ALL_RW__666 = customChmodMode()
            .setOwnerPermissions(ModePermission.READ_WRITE)         //6
            .setGroupPermissions(ModePermission.READ_WRITE)         //6
            .setOtherPermissions(ModePermission.READ_WRITE)         //6
            .getMode();                                             //666

    public static final int MODE_ALL_R_444 = customChmodMode()
            .setOwnerPermissions(ModePermission.READ)               //4
            .setGroupPermissions(ModePermission.READ)               //4
            .setOtherPermissions(ModePermission.READ)               //4
            .getMode();                                             //444

    public static final int MODE_SOME_RW__770 = customChmodMode()
            .setOwnerPermissions(ModePermission.READ_WRITE_EXECUTE)
            .setGroupPermissions(ModePermission.READ_WRITE_EXECUTE)
            .setOtherPermissions(ModePermission.NONE)
            .getMode();
            
    public static void chmod(String fileOrDirectory, int mode, boolean recursive) {
        executeCommand("chmod " + (recursive ? "-R " : "") + mode + " " + fileOrDirectory);
    }


    /**
     * Copies all files and subdirectories from one directory to another using a shell command.
     *
     * @param fromDirectory The source directory to copy from.
     * @param toDirectory   The destination directory to copy to.
     */
    public static void cp_directory_to(String fromDirectory, String toDirectory) {
        if (fromDirectory == null || fromDirectory.trim().isEmpty() ||
                toDirectory == null || toDirectory.trim().isEmpty()) {
            Log.e(TAG, "Source or destination directory is null or empty.");
            return;
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "CP -R FROM:[" + fromDirectory + "] TO:[" + toDirectory + "]");

        java.lang.Process process = null;
        BufferedReader reader = null;
        BufferedReader errorReader = null;

        try {
            // Use an array to pass the command and arguments
            String[] command = {"cp", "-r", fromDirectory, toDirectory};

            // Execute the command
            process = Runtime.getRuntime().exec(command);

            // Capture the standard output
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Capture the standard error
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                Log.d(TAG, "cp command executed successfully.");
                if (output.length() > 0) {
                    Log.d(TAG, "Output: " + output.toString().trim());
                }
            } else {
                Log.e(TAG, "cp command failed with exit code: " + exitCode);
                if (errorOutput.length() > 0) {
                    Log.e(TAG, "Error: " + errorOutput.toString().trim());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while executing cp command: " + e.getMessage(), e);
        } finally {
            // Close readers to avoid resource leaks
            try {
                if (reader != null) reader.close();
                if (errorReader != null) errorReader.close();
            } catch (Exception e) {
                Log.e(TAG, "Exception while closing readers: " + e.getMessage(), e);
            }

            // Destroy the process
            if (process != null) {
                process.destroy();
            }
        }
    }



    /**
     * Executes the `mkdir -p <directory>` command using the shell and logs the output.
     *
     * @param directory The directory path to create.
     */
    public static void mkdirs(String directory) {
        if (directory == null || directory.trim().isEmpty()) {
            Log.e(TAG, "Directory path is null or empty.");
            return;
        }

        java.lang.Process process = null;
        BufferedReader reader = null;
        BufferedReader errorReader = null;

        try {
            // Use an array to pass the command and arguments
            String[] command = {"mkdir", "-p", directory};

            // Execute the command
            process = Runtime.getRuntime().exec(command);

            // Capture the standard output
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Capture the standard error
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                Log.d(TAG, "mkdir command executed successfully.");
                if (output.length() > 0) {
                    Log.d(TAG, "Output: " + output.toString().trim());
                }
            } else {
                Log.e(TAG, "mkdir command failed with exit code: " + exitCode);
                if (errorOutput.length() > 0) {
                    Log.e(TAG, "Error: " + errorOutput.toString().trim());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while executing mkdir command: " + e.getMessage(), e);
        } finally {
            // Close readers to avoid resource leaks
            try {
                if (reader != null) reader.close();
                if (errorReader != null) errorReader.close();
            } catch (Exception e) {
                Log.e(TAG, "Exception while closing readers: " + e.getMessage(), e);
            }

            // Destroy the process
            if (process != null) {
                process.destroy();
            }
        }
    }



    /**
     * Get permissions, owner details, and UID of a file or directory.
     *
     * @param fileOrDirectory The file or directory path.
     * @return A ChmodModeBuilder containing permissions, owner, and UID information, or null if an error occurs.
     */
    public static ChmodModeBuilder getPermissionsOfFileOrDirectory(String fileOrDirectory) {
        if (fileOrDirectory == null || fileOrDirectory.trim().isEmpty()) {
            Log.e(TAG, "Path is null or empty.");
            return null;
        }

        java.lang.Process process = null;
        BufferedReader reader = null;

        try {
            // Execute the `ls -ld` command to retrieve permission and owner info
            String[] command = {"ls", "-ld", fileOrDirectory};
            process = Runtime.getRuntime().exec(command);

            // Read the command output
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine(); // Only the first line is needed
            if (output == null || output.isEmpty()) {
                Log.e(TAG, "Failed to retrieve permissions for: " + fileOrDirectory);
                return null;
            }

            if (DebugUtil.isDebug())
                Log.d(TAG, "ls -ld [" + fileOrDirectory + "] Output=" + output);

            // Parse the `ls -ld` output
            String[] parts = output.split("\\s+");
            /*if (parts.length < 9) {
                Log.e(TAG, "Unexpected ls -ld output: " + output);
                return null;
            }*/

            // Handle dynamic file path position
            int pathIndex = parts.length - 1; // The file or directory path is always the last item
            String filePath = parts[pathIndex]; // Extract the file path

            // Parse permissions and owner/group details
            String permissionsString = parts[0]; // drwxr-xr-x
            String owner = parts[2]; // Owner username
            String group = parts[3];

            // Parse permissions
            int ownerPermissions = parsePermission(permissionsString.charAt(1), permissionsString.charAt(2), permissionsString.charAt(3));
            int groupPermissions = parsePermission(permissionsString.charAt(4), permissionsString.charAt(5), permissionsString.charAt(6));
            int otherPermissions = parsePermission(permissionsString.charAt(7), permissionsString.charAt(8), permissionsString.charAt(9));

            // Resolve UID from owner string using the `id` command
            int uid = resolveUid(owner);
            int guid = resolveUid(group);

            // Create and populate a ChmodModeBuilder object
            ChmodModeBuilder builder = ChmodModeBuilder.create()
                    .setOwnerPermissions(ModePermission.fromValue(ownerPermissions))
                    .setGroupPermissions(ModePermission.fromValue(groupPermissions))
                    .setOtherPermissions(ModePermission.fromValue(otherPermissions))
                    .setOwner(owner)
                    .setUid(uid)
                    .setGroup(group)
                    .setGuid(guid);

            Log.d(TAG, "Retrieved permissions: " + builder.getMode() + ", owner: " + builder.getOwner() + ", UID: " + builder.getUid());
            return builder;

        } catch (Exception e) {
            Log.e(TAG, "Exception while getting permissions: " + e.getMessage(), e);
            return null;
        } finally {
            // Close resources
            try {
                if (reader != null) reader.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing reader: " + e.getMessage(), e);
            }
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * Parse permission characters (e.g., rwx) into an integer value (e.g., 7 for rwx).
     */
    private static int parsePermission(char read, char write, char execute) {
        int permission = 0;
        if (read == 'r') permission += 4;
        if (write == 'w') permission += 2;
        if (execute == 'x') permission += 1;
        return permission;
    }

    /**
     * Resolve the UID of an owner string using the `id` command.
     *
     * @param owner The owner string (e.g., "u0_a203").
     * @return The UID as an integer, or -1 if the command fails.
     */
    private static int resolveUid(String owner) {
        java.lang.Process process = null;
        BufferedReader reader = null;

        try {
            // Execute the `id <owner>` command
            String[] command = {"id", owner};
            process = Runtime.getRuntime().exec(command);

            // Read the command output, e.g., "uid=10203(u0_a203) gid=10203(u0_a203) groups=10203(u0_a203)"
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine(); // Only the first line is needed
            if (output == null || output.isEmpty()) {
                Log.e(TAG, "Failed to resolve UID for owner: " + owner);
                return -1;
            }

            // Parse the UID from the output
            int uidStartIndex = output.indexOf("uid=") + 4;
            int uidEndIndex = output.indexOf("(", uidStartIndex); // UID ends before the '(' character
            if (uidStartIndex != -1 && uidEndIndex != -1) {
                return Integer.parseInt(output.substring(uidStartIndex, uidEndIndex));
            } else {
                Log.e(TAG, "Unexpected id output format: " + output);
                return -1;
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception while resolving UID: " + e.getMessage(), e);
            return -1;
        } finally {
            // Close resources
            try {
                if (reader != null) reader.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing reader: " + e.getMessage(), e);
            }
            if (process != null) {
                process.destroy();
            }
        }
    }


    //public static void chown(String fileOrDirectory, UserId userId, UserId groupId, boolean recursive) {
    //    chown(fileOrDirectory, userId.getValue(), groupId.getValue(), recursive);
    //}

    public static void chown(String fileOrDirectory, int userId, int groupId, boolean recursive) {
        String u = userId == -1 ? "" : String.valueOf(userId);
        String g = groupId == -1 ? "" : String.valueOf(groupId);
        executeCommand("chown " + (recursive ? "-R " : "" ) + u + ":" + g + " " + fileOrDirectory);
    }

    public static void chown(String fileOrDirectory, String user, String group, boolean recursive) {
        String u = user == null ? "" : user;
        String g = group == null ? "" : group;
        executeCommand("chown " + (recursive ? "-R " : "" ) + u + ":" + g + " " + fileOrDirectory);
    }

    public static void rmDirectoryForcefully(String directory) { rmDirectoryForcefully(directory, Process.myUid(), Process.myUid()); }
    //public static void rmDirectoryForcefully(String directory, UserId userId, UserId groupId) { rmDirectoryForcefully(directory, userId.getValue(), groupId.getValue()); }
    public static void rmDirectoryForcefully(String directory, int userId, int groupId) {
        chown(directory, userId, groupId, true);
        chmod(directory, MODE_ALL_RWX__777, true);
        rm(directory, true);
    }

    public static void rmFileForcefully(String file) {  rmFileForcefully(file, Process.myUid(), Process.myUid()); }
    //public static void rmFileForcefully(String file, UserId userId, UserId groupId) { rmFileForcefully(file, userId.getValue(), groupId.getValue()); }
    public static void rmFileForcefully(String file, int userId, int groupId) {
        chown(file, userId, groupId, false);
        chmod(file, MODE_ALL_RWX__777, false);
        rm(file, false);
    }

    public static boolean deleteFileOrDirectoryForcefully(String fileOrDirectory) {
        if(exists(fileOrDirectory)) {
            File f = new File(fileOrDirectory);
            //Should we use "myUid" or Hardcode one ?
            if(isDirectory(fileOrDirectory) || f.isDirectory())
                rmDirectoryForcefully(fileOrDirectory);
            else if(isFile(fileOrDirectory) || f.isFile())
                rmFileForcefully(fileOrDirectory);
            else {
                rmDirectoryForcefully(fileOrDirectory);
                rmDirectoryForcefully(fileOrDirectory);
            } return exists(fileOrDirectory);
        } return true;
    }

    public static boolean exists(String fileOrDirectory) {
        File f = new File(fileOrDirectory);
        try { return Os.stat(fileOrDirectory) != null || f.isFile() || f.isDirectory(); } catch (Exception ignored) {   }
        return f.isFile() || f.isDirectory();
    }

    public static boolean isDirectory(String directory) {
        if(directory == null) return false;
        try {
            StructStat stat = Os.stat(directory);
            return OsConstants.S_ISDIR(stat.st_mode);
        }catch (Exception ignored) { }
        return false;
    }

    public static boolean isFile(String file) {
        if(file == null) return false;
        try {
            StructStat stat = Os.stat(file);
            return OsConstants.S_ISREG(stat.st_mode);
        }catch (Exception ignored) { }
        return false;
    }

    public static void rm(String fileOrDirectory, boolean recursive) {
        //-r : Removes directories and their content recursively.
        //-f : Forces the removal of all files or directories.
        executeCommand("rm " + (recursive ? "-rf " : "-f") + fileOrDirectory);
    }

    public static String getParent(String fileOrDirectory) {
        if(fileOrDirectory == null) return File.separator;
        String delimiter = getPathDelimiter(fileOrDirectory);
        if(delimiter == null) return fileOrDirectory;
        List<String> parts = getParts(fileOrDirectory, delimiter);
        if(parts.isEmpty() || parts.size() == 1) return delimiter;
        StringBuilder sb = new StringBuilder();
        sb.append(delimiter);
        int sz = parts.size() - 2;
        for(int i = 0; i < parts.size() - 1; i++) {
            sb.append(parts.get(i));
            if(i != sz) {
                sb.append(delimiter);
            }
        } return sb.toString();
    }

    public static List<String> getParts(String fileOrDirectory) { return getParts(fileOrDirectory, null); }

    public static List<String> getParts(String fileOrDirectory, String overrideDelimiter) {
        List<String> parts = new ArrayList<>();
        if(fileOrDirectory != null && !fileOrDirectory.isEmpty()) {
            String del = overrideDelimiter == null ? getPathDelimiter(fileOrDirectory) : overrideDelimiter;
            if(del != null) {
                String trimmed = Str.trim(fileOrDirectory, del, true, false);
                if(!trimmed.contains(del)) {
                    parts.add(trimmed);
                    return parts;
                }

                String[] splits = trimmed.split(Pattern.quote(del));
                for(String s : splits)
                    if(!s.isEmpty())
                        parts.add(s);
            }
        } return parts;
    }

    public static String buildPath(List<String> paths, String separator) {
        if(paths != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(separator);
            int lst = paths.size() - 1;
            for(int i = 0; i < paths.size(); i++) {
                sb.append(paths.get(i));
                if(i != lst) sb.append(separator);
            } return sb.toString();
        } return null;
    }

    //MEat Emoji Hex Bytes = (F0 9F A5 A9)

    public static String buildPath(String... paths) { return buildPath(false, paths); }
    public static String buildPath(boolean endInSeparator, String... paths) {
        if (paths != null && paths.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < paths.length; i++) {
                String p = paths[i];
                if (!TextUtils.isEmpty(p)) {
                    if (sb.length() > 0 && !sb.toString().endsWith(File.separator))
                        sb.append(File.separator);

                    sb.append(p.startsWith(File.separator) ? p.substring(1) : p);
                }
            }

            if (endInSeparator && sb.length() > 0 && sb.charAt(sb.length() - 1) != File.separatorChar)
                sb.append(File.separator);

            if(sb.length() > 0 && sb.charAt(0) != File.separatorChar)
                return StrBuilder.create(sb.length() + 1).append(File.separator).append(sb).toString();

            return sb.toString();
        }
        return null;
    }


    public static FileDescriptor generateFakeFileDescriptor(String contents) {
        File mockFile = generateTempFakeFile(contents);
        if(mockFile == null)
            return null;
        try {
            ParcelFileDescriptor pFileDescriptor = ParcelFileDescriptor.open(mockFile, ParcelFileDescriptor.MODE_READ_ONLY);//0x10000000
            return pFileDescriptor.getFileDescriptor();
        }catch (Exception e) {
            Log.e(TAG,"Failed to Create Fake File Descriptor!! Error: " + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            return null;
        }
    }


    public static File generateTempFakeFile(String contents) {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            File temp = File.createTempFile("temp", null);
            fos = new FileOutputStream(temp, false);
            osw = new OutputStreamWriter(fos);
            osw.write(contents);
            return temp;
        }catch (Exception e) {
            Log.e(TAG, "Failed to Create Fake File! Error: " + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e)) ;
            return null;
        }finally {
            StreamUtil.close(osw, true);
            StreamUtil.close(fos);
        }
    }

    public static String getPathDelimiter(String path) { return getPathDelimiter(path, false); }
    public static String getPathDelimiter(String path, boolean useDefaultIfNull) { return path.contains(File.separator) ? File.separator : File.separator.equals("/") ? useDefaultIfNull ? File.separator : null : path.contains("/") ? "/" : useDefaultIfNull ? File.separator : null; }

    private static void executeCommand(String command) {
        try {
            Runtime.getRuntime().exec(command).waitFor();
        }catch (Exception e) {
            if(DebugUtil.isDebug())
                Log.e(TAG, "[executeCommand] Failed: " + e.getMessage());
        }
    }


    public static FileEx findFirst(FileEx searchDir, String searchTerm, boolean recurse, boolean ignoreDirectories, boolean ignoreFiles) {
        if (DebugUtil.isDebug())
            Log.d(TAG, "Starting Search for first find on Term, " + searchTerm + " Path=" + searchDir.getAbsolutePath());

        List<FileEx> files = find(searchDir, searchTerm, recurse, ignoreDirectories, ignoreFiles, true);
        if(files.isEmpty())
            return null;

        return files.iterator().next();
    }

    public static List<FileEx> find(FileEx searchDir, String searchTerm, boolean recurse, boolean ignoreDirectories, boolean ignoreFiles, boolean stopOnFirstResult) {
        List<FileEx> files = new ArrayList<>();
        if(ignoreDirectories && ignoreFiles) {
            Log.e(TAG, "Error Failed to find, Both flags for Ignore are set! Term=" + searchTerm);
            return files;
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Searching for Files Containing: " + searchTerm + " in: " + searchDir.getAbsolutePath() + " R:" + recurse + " Ignore Directories:" + ignoreDirectories + " Ignore Files:" + ignoreFiles + " Stop First:" + stopOnFirstResult);

        try {
            String searchLow = searchTerm.toLowerCase();
            FileEx baseDir = new FileEx(searchDir.getAbsoluteFile());
            FileEx[] items = baseDir.listFilesEx();
            if(DebugUtil.isDebug())
                Log.d(TAG, "Searching [" + ArrayUtils.safeLength(items) + "] for Search Term:" + searchTerm + " Search Dir:" + searchDir.getAbsolutePath() + " R:" + recurse);

            if(ArrayUtils.isValid(items)) {
                for(FileEx file : items) {
                    String name = file.getName().toLowerCase();
                    boolean isDir = file.isDirectory();
                    if((!ignoreDirectories && isDir) || (!ignoreFiles && file.isFile())) {
                        if(name.contains(searchLow)) {
                            files.add(file);
                            if(DebugUtil.isDebug())
                                Log.d(TAG, "Found Search File, IsFile=" + (!isDir) + " Name=" + file.getName() + " Search Term=" + searchLow + " R:" + recurse + " Stop First:" + stopOnFirstResult);

                            if(stopOnFirstResult)
                                return files;
                        }
                    }

                    if(isDir && recurse) {
                        ListUtil.addAllIfValid(files, find(file, searchTerm, true, false, ignoreFiles, stopOnFirstResult));
                        if(stopOnFirstResult && !files.isEmpty())
                            return files;
                    }
                }
            }

            return files;
        }catch (Exception e) {
            Log.e(TAG, "Failed to Find [" + searchTerm + "] in search path [" + searchDir.getAbsolutePath() + "] Error=" + e);
            return files;
        }
    }
}
