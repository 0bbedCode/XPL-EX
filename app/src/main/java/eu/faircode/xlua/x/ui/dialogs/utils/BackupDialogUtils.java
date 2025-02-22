package eu.faircode.xlua.x.ui.dialogs.utils;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.x.ui.FileDialogUtils;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.data.XBackup;

public class BackupDialogUtils {
    private static final String TAG = LibUtil.generateTag(BackupDialogUtils.class);

    // Constants specific to backup operations
    public static final int REQUEST_OPEN_BACKUP = 2001;
    public static final int REQUEST_SAVE_BACKUP = 2002;

    // In BackupDialogUtils
    public static void startBackupFilePicker(Activity activity) {
        try {
            Intent intent = Intent.createChooser(
                    FileDialogUtils.createOpenFileIntent(),
                    activity.getString(R.string.title_select_file)
            );
            activity.startActivityForResult(intent, REQUEST_OPEN_BACKUP);
        } catch (Exception e) {
            if (DebugUtil.isDebug()) {
                Log.e(TAG, "Error starting backup file picker from activity", e);
            }
        }
    }

    // Keep existing Fragment version
    public static void startBackupFilePickerFragment(Fragment fragment) {
        try {
            Intent intent = Intent.createChooser(
                    FileDialogUtils.createOpenFileIntent(),
                    fragment.getString(R.string.title_select_file)
            );
            fragment.startActivityForResult(intent, REQUEST_OPEN_BACKUP);
        } catch (Exception e) {
            if (DebugUtil.isDebug()) {
                Log.e(TAG, "Error starting backup file picker from fragment", e);
            }
        }
    }

    public static void startSavePicker(Activity activity, String defaultName) {
        try {
            Intent intent = Intent.createChooser(
                    createSaveConfigIntent(defaultName),
                    activity.getString(R.string.title_save_file)
            );
            activity.startActivityForResult(intent, REQUEST_SAVE_BACKUP);
        } catch (Exception e) {
            Log.e(TAG, "Error starting config save picker", e);
        }
    }



    /**
     * Creates an intent for saving JSON files
     */
    public static Intent createSaveConfigIntent(String defaultName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        // Add fallback MIME types
        String[] mimeTypes = {
                "application/json",
                "text/plain",
                "text/json"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.putExtra(Intent.EXTRA_TITLE, defaultName + ".json");
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        return intent;
    }

    /**
     * Starts file picker for saving backup
     */
    public static void startBackupSaver(@NonNull Fragment fragment, String fileName) {
        FileDialogUtils.startFileSaver(fragment, fileName, REQUEST_SAVE_BACKUP);
    }

    /**
     * Reads an XBackup from file
     */
    @Nullable
    public static XBackup readBackupFromUri(@NonNull Context context, @NonNull Uri uri) {
        try {
            String content = FileDialogUtils.readFileContent(context, uri);
            if (content == null) return null;

            JSONObject jsonObject = new JSONObject(content);
            XBackup backup = new XBackup();
            backup.fromJSONObject(jsonObject);
            return backup;

        } catch (Exception e) {
            if (DebugUtil.isDebug()) {
                Log.e(TAG, "Error reading backup from URI: " + uri, e);
            }
            return null;
        }
    }


    public static boolean writeBackupToUri(@NonNull Context context, @NonNull Uri uri,
                                           @NonNull XBackup backup,
                                           boolean includeDefinitions,
                                           boolean includeScripts,
                                           boolean includeAssignments,
                                           boolean includeSettings) {
        try {
            XBackup toWrite = backup.createFilteredCopy(
                    includeDefinitions,
                    includeScripts,
                    includeAssignments,
                    includeSettings
            );

            return FileDialogUtils.writeFileContent(context, uri, toWrite.toJSON());
        } catch (Exception e) {
            if (DebugUtil.isDebug()) {
                Log.e(TAG, "Error writing backup to URI: " + uri, e);
            }
            return false;
        }
    }
}