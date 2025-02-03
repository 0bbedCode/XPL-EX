
package eu.faircode.xlua.x.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.ui.dialogs.ConfigCreateDialog;
import eu.faircode.xlua.x.xlua.configs.XPConfig;

public class ConfUtils {
    private static final String TAG = "ConfUtils";

    public static final int REQUEST_OPEN_CONFIG = 1001;
    public static final int REQUEST_SAVE_CONFIG = 1002;


    public static Intent createOpenConfigIntentEx() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Allow any file type initially
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        //final int takeFlags = data.getFlags()
        //        & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        // Grant read and persistable URI permissions
        //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        return intent;
    }

      //public static Intent createOpenFileIntent() { return createOpenFileIntent("*/*"); }
    /*public static Intent createOpenFileIntent(String extension) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(extension); // Use "image/*" for images, "application/pdf" for PDF, etc.
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return intent;
    }
     */


    /*
                            startActivityForResult(
                            Intent.createChooser(UiUtil.createOpenFileIntent(), getResources().getString(R.string.title_select_file)),
                                PICK_FILE_REQUEST_CODE);
     */

    /**
     * Creates an intent for opening JSON files
     */
    public static Intent createOpenConfigIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Set MIME type to JSON and include fallbacks
        String[] mimeTypes = {
                "application/json",
                "text/json",
                "text/plain"
        };
        intent.setType("*/*"); // Allow any file type initially
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes); // Specify supported MIME types

        // Grant read and persistable URI permissions
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        return intent;
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
     * Starts file picker for opening config JSON files
     */
    public static void startConfigFilePicker(@NonNull Fragment fragment) {
        try {
            Intent intent = Intent.createChooser(
                    createOpenConfigIntent(),
                    fragment.getString(R.string.title_select_file)
            );
            fragment.startActivityForResult(intent, REQUEST_OPEN_CONFIG);
        } catch (Exception e) {
            Log.e(TAG, "Error starting config file picker", e);
        }
    }

    /**
     * Starts file picker for saving config JSON files
     */
    public static void startConfigSavePicker(@NonNull Fragment fragment, String defaultName) {
        try {
            Intent intent = Intent.createChooser(
                    createSaveConfigIntent(defaultName),
                    fragment.getString(R.string.title_save_file)
            );
            fragment.startActivityForResult(intent, REQUEST_SAVE_CONFIG);
        } catch (Exception e) {
            Log.e(TAG, "Error starting config save picker", e);
        }
    }

    /**
     * Reads a config from JSON file
     */
    @Nullable
    public static XPConfig readConfigFromUri(@NonNull Context context, @NonNull Uri uri) {
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
            return XPConfig.fromJsonString(stringBuilder.toString());

        } catch (Exception e) {
            Log.e(TAG, "Error reading config from URI: " + uri, e);
            return null;
        }
    }

    /**
     * Writes a config to JSON file
     */
    public static boolean writeConfigToUri(@NonNull Context context, @NonNull Uri uri,
                                           @NonNull XPConfig config) {
        try {
            ParcelFileDescriptor pfd = context.getContentResolver()
                    .openFileDescriptor(uri, "w");
            if (pfd == null) return false;

            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(config.toJSONString()
                    .getBytes(StandardCharsets.UTF_8));

            fileOutputStream.close();
            pfd.close();
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error writing config to URI: " + uri, e);
            return false;
        }
    }

    /**
     * Opens config in ConfigCreateDialog for editing
     */
    public static void openConfigInEditor(XPConfig config, int uid, String packageName,
                                          FragmentManager fragmentManager, Context context) {
        ConfigCreateDialog.create()
                .setApp(uid, packageName)
                .setConfigs(context, false)
                .setSettings(config.settings)
                .setHookIds(config.hooks)
                .show(fragmentManager, context.getString(R.string.title_config_modify));
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
            Log.e(TAG, "Error taking persistable permissions", e);
        }
    }
}