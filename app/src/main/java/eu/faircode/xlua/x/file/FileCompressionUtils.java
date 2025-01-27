package eu.faircode.xlua.x.file;

import android.util.Log;

import java.io.*;
import java.util.zip.*;

import eu.faircode.xlua.x.Str;

public class FileCompressionUtils {
    private static final String TAG = "XLua.FileCompressionUtils";

    /**
     * Compresses the contents of a folder, including all subfolders and files, into a single ZIP file.
     *
     * @param sourceFolder The folder to compress.
     * @param zipFile      The output ZIP file where the compressed data will be stored.
     */
    public static void compressFolder(File sourceFolder, File zipFile) {
        try {
            if (!sourceFolder.exists()) {
                throw new FileNotFoundException("Source folder does not exist: " + sourceFolder.getAbsolutePath());
            }
            try (FileOutputStream fos = new FileOutputStream(zipFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                zipFolder(sourceFolder, sourceFolder.getName(), zos);
            }
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to Compress Folder! Source=%s  ZipFile=%s  Error=%s", sourceFolder.getAbsolutePath(), zipFile.getAbsolutePath(), e));
        }
    }

    /**
     * Recursively compresses a folder and its contents into the ZIP file.
     *
     * @param folder       The current folder being processed.
     * @param parentFolder The relative path of the parent folder within the ZIP file.
     * @param zos          The ZipOutputStream used to write the compressed data.
     */
    private static void zipFolder(File folder, String parentFolder, ZipOutputStream zos) {
        try {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    String zipEntryName = parentFolder + "/" + file.getName();
                    if (file.isDirectory()) {
                        zipFolder(file, zipEntryName, zos); // Recursively zip subfolders
                    } else {
                        try (FileInputStream fis = new FileInputStream(file)) {
                            zos.putNextEntry(new ZipEntry(zipEntryName));
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = fis.read(buffer)) > 0) {
                                zos.write(buffer, 0, length);
                            }
                            zos.closeEntry();
                        }
                    }
                }
            }
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to Zip Folder [%s] Parent=[%s] Error=%s", folder.getAbsolutePath(), parentFolder, e));
        }
    }

    /**
     * Decompresses the contents of a ZIP file into the specified target folder.
     *
     * @param zipFile      The ZIP file to decompress.
     * @param targetFolder The destination folder where the decompressed files and folders will be extracted.
     */
    public static void decompressZip(File zipFile, File targetFolder) {
        try {
            if (!zipFile.exists()) {
                throw new FileNotFoundException("ZIP file does not exist: " + zipFile.getAbsolutePath());
            }
            if (!targetFolder.exists() && !targetFolder.mkdirs()) {
                throw new IOException("Failed to create target folder: " + targetFolder.getAbsolutePath());
            }

            try (FileInputStream fis = new FileInputStream(zipFile);
                 ZipInputStream zis = new ZipInputStream(fis)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    File newFile = new File(targetFolder, entry.getName());
                    if (entry.isDirectory()) {
                        if (!newFile.mkdirs()) {
                            throw new IOException("Failed to create directory: " + newFile.getAbsolutePath());
                        }
                    } else {
                        try (FileOutputStream fos = new FileOutputStream(newFile)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, length);
                            }
                        }
                    }
                    zis.closeEntry();
                }
            }
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to Decompress Zip, %s Target=%s  Error=%s", zipFile.getAbsolutePath(), targetFolder.getAbsolutePath(), e));
        }
    }
}
