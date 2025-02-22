package eu.faircode.xlua.x.file;

import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.utilities.StreamUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.XposedUtility;

public class FileTransfer {
    private final static String TAG = LibUtil.generateTag(FileTransfer.class);

    //Moved to XPOSED LOG
    public static int copyDirectory(
            FileEx copyFrom,
            FileEx copyTo,
            boolean recursive,
            UnixAccessControl access) {
        int copiedCount = 0;
        try {
            if(!copyFrom.isDirectory())
                throw new Exception("Directory Copy from does not exist!");

            if(copyTo.isFile())
                throw new Exception("Copying a Directory to a File Destination Error!");

            if(copyTo.getAbsolutePath().startsWith(copyFrom.getAbsolutePath()))
                throw new Exception("Cannot copy a directory into itself or its subdirectories!");

            if(!copyTo.isDirectory()) {
                //Meaning it does not exist so create it
                if(DebugUtil.isDebug())
                    Log.w(TAG, Str.fm("Creating Directory [%s] so contents from [%s] can be copied over!",
                            copyTo.getAbsolutePath(),
                            copyFrom.getDirectory()));

                if(!copyTo.mkdirs())
                    throw new Exception("Failed to Create the Copy To Directory!");

                if(!copyTo.isDirectory())
                    throw new Exception("Failed to Find / Create the Copy To Directory!");
            }

            //Take full Control if not null for both Directories
            copyTo.takeFullControl(access, recursive);
            copyFrom.takeFullControl(access, recursive);
            FileEx[] subFiles = copyFrom.listFilesEx();
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Copying [%s] Contents to [%s]! Sub File Count in From=%s",
                        copyFrom.getAbsolutePath(),
                        copyTo.getAbsolutePath(),
                        ArrayUtils.safeLength(subFiles)));

            if(!ArrayUtils.isValid(subFiles)) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("No sub Files from Directory [%s] to Directory [%s]",
                            copyFrom.getAbsolutePath(),
                            copyTo.getAbsolutePath()));
                return 1;
            }

            for(FileEx file : subFiles) {
                FileEx destination = copyTo.openSub(file.getName());
                if(file.isFile()) {
                    if(copyFile(file, destination)) {
                        copiedCount++;
                        destination.takeFullControl(access, false);
                    } else {
                        if(DebugUtil.isDebug())
                            Log.w(TAG, Str.fm("Failed to Copy File [%s] to [%s]",
                                    file.getAbsolutePath(),
                                    destination.getAbsolutePath()));
                    }
                } else {
                    copiedCount += copyDirectory(file, destination, recursive, access);
                    destination.takeFullControl(access, recursive);
                }
            }
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Error Copying [%s] to [%s] Recursive [%s], Error=%s",
                    Str.toStringOrNull(copyFrom),
                    Str.toStringOrNull(copyTo),
                    recursive,
                    e));
        }

        if(DebugUtil.isDebug())
            XposedUtility.logD_xposed(TAG, Str.fm("Copied: [%s] Directory to [%s] Copied Count=%s",
                    copyFrom.getAbsolutePath(),
                    copyTo.getAbsolutePath(),
                    copiedCount));

        return copiedCount;
    }

    public static boolean copyFile(FileEx src, FileEx dst) {
        InputStream in = null;
        OutputStream out = null;
        boolean success = false;

        try {
            // Validate input parameters
            if (src == null || !src.isFile() || dst == null)
                throw new IllegalArgumentException("Input Args to Copy file to File are invalid or bad!");

            // Create parent directories if they don't exist
            FileEx parentDir = dst.getParentEx();
            if (parentDir != null && !parentDir.exists())
                if (!parentDir.mkdirs())
                    throw new IOException("Failed to create parent directories for destination file");

            // Open streams and copy
            in = new FileInputStream(src);
            out = new FileOutputStream(dst);

            byte[] buf = new byte[8192]; // Increased buffer size for better performance
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            // Ensure all data is written to disk
            out.flush();
            success = true;
        } catch (Exception e) {
            Log.e(TAG, String.format("Failed to Copy File [%s] to [%s] Error=%s",
                    String.valueOf(src),
                    String.valueOf(dst),
                    e.getMessage()));
            // If copy fails, try to delete partially copied file
            if (dst != null && dst.exists())
                dst.delete();

        } finally {
            StreamUtil.close(out);
            StreamUtil.close(in);
        }

        // Verify the copy was successful
        return success && dst.isFile() && dst.length() > 0;
    }
}
