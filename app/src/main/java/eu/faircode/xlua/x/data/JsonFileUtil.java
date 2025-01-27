package eu.faircode.xlua.x.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.InputStream;

public class JsonFileUtil {
    private static final String TAG = "XLua.JsonFileUtil";

    private static final String[] ACCEPTED_MIME_TYPES = {
            "application/json",
            "text/plain",
            "application/x-javascript",
            "text/javascript",
            "text/json",
            "application/octet-stream"
    };

    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try {
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error Getting File Name, Uri=" + uri.getAuthority() + " Error=" + e + " Stack=" + Log.getStackTraceString(e));
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static boolean isJsonFile(Context context, Uri uri) {
        //Context context = getContext();
        if (context == null) return false;

        // First check the MIME type
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType != null) {
            for (String acceptedType : ACCEPTED_MIME_TYPES) {
                if (acceptedType.equalsIgnoreCase(mimeType)) {
                    return true;
                }
            }
        }

        // Then check file extension
        String fileName = getFileName(context, uri);
        if (fileName != null && fileName.toLowerCase().endsWith(".json")) {
            return true;
        }

        // Finally, peek at content if needed
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                byte[] buffer = new byte[1];
                int read = inputStream.read(buffer);
                inputStream.close();

                if (read > 0) {
                    // Check if file starts with { or [ which indicates JSON
                    return buffer[0] == '{' || buffer[0] == '[';
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error Checking File if JSON, Uri=" + uri.getAuthority() + " Error=" + e + " Stack=" + Log.getStackTraceString(e));
        }
        return false;
    }


}
