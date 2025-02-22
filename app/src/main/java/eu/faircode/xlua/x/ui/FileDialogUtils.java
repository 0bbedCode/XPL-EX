package eu.faircode.xlua.x.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.x.xlua.LibUtil;

public class FileDialogUtils {
    private static final String TAG = LibUtil.generateTag(FileDialogUtils.class);

    // Request codes for file operations
    public static final int REQUEST_OPEN_FILE = 1001;
    public static final int REQUEST_SAVE_FILE = 1002;

    /**
     * Creates base intent for opening files
     */
    public static Intent createOpenFileIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Allow any file type for compatibility
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return intent;
    }

    /**
     * Creates base intent for saving files
     */
    public static Intent createSaveFileIntent(String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, ensureJsonExtension(fileName));
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        return intent;
    }

    /**
     * Starts file picker for opening files
     */
    public static void startFilePicker(@NonNull Fragment fragment, int requestCode) {
        try {
            Intent intent = Intent.createChooser(
                    createOpenFileIntent(),
                    fragment.getString(R.string.title_select_file)
            );
            fragment.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            if (DebugUtil.isDebug()) {
                Log.e(TAG, "Error starting file picker", e);
            }
        }
    }

    /**
     * Starts file picker for saving files
     */
    public static void startFileSaver(@NonNull Fragment fragment, String fileName, int requestCode) {
        try {
            Intent intent = Intent.createChooser(
                    createSaveFileIntent(fileName),
                    fragment.getString(R.string.title_save_file)
            );
            fragment.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            if (DebugUtil.isDebug()) {
                Log.e(TAG, "Error starting file saver", e);
            }
        }
    }

    /**
     * Reads content from a URI as string
     */
    @Nullable
    public static String readFileContent(@NonNull Context context, @NonNull Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            inputStream.close();
            return stringBuilder.toString();

        } catch (Exception e) {
            if (DebugUtil.isDebug()) {
                Log.e(TAG, "Error reading from URI: " + uri, e);
            }
            return null;
        }
    }

    /**
     * Writes content to a URI
     */
    public static boolean writeFileContent(@NonNull Context context, @NonNull Uri uri, @NonNull String content) {
        try {
            ParcelFileDescriptor pfd = context.getContentResolver()
                    .openFileDescriptor(uri, "w");
            if (pfd == null) return false;

            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(content.getBytes(StandardCharsets.UTF_8));

            fileOutputStream.close();
            pfd.close();
            return true;

        } catch (Exception e) {
            if (DebugUtil.isDebug()) {
                Log.e(TAG, "Error writing to URI: " + uri, e);
            }
            return false;
        }
    }

    /**
     * Takes persistable URI permissions if needed
     */
    public static void takePersistablePermissions(@NonNull Context context, @NonNull Uri uri) {
        try {
            int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION |
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            context.getContentResolver().takePersistableUriPermission(uri, takeFlags);
        } catch (Exception e) {
            if (DebugUtil.isDebug()) {
                Log.e(TAG, "Error taking persistable permissions", e);
            }
        }
    }

    /**
     * Validates file name has .json extension
     */
    public static boolean isJsonFile(String fileName) {
        return !TextUtils.isEmpty(fileName) &&
                fileName.toLowerCase().endsWith(".json");
    }

    /**
     * Ensures filename has .json extension
     */
    private static String ensureJsonExtension(String fileName) {
        if (!isJsonFile(fileName)) {
            return fileName + ".json";
        }
        return fileName;
    }
}