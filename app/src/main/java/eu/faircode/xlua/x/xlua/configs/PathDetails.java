package eu.faircode.xlua.x.xlua.configs;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.x.data.string.StrBuilder;

public class PathDetails {
    private static final String TAG = "XLua.PathDetails";

    public String tag;
    public String fullPath;

    public PathDetails() { }
    public PathDetails(String tag, String fullPath) {
        this.tag = tag;
        this.fullPath = fullPath;
    }

    public static PathDetails create(String tag, String fullPath) { return new PathDetails(tag, fullPath); }
    public static PathDetails parseLine(String line) {
        if(TextUtils.isEmpty(line) || !line.contains(":"))
            return null;

        String[] parts = line.split(":");
        PathDetails details = new PathDetails();
        details.tag = parts[0];
        details.fullPath = parts[1];

        return details;
    }

    public static String encodeDetails(List<PathDetails> details) {
        StringBuilder sb = new StringBuilder();
        try {
            for(PathDetails p : details) {
                if(sb.length() > 0)
                    sb.append(",");

                sb.append(p.tag);
                sb.append(":");
                sb.append(p.fullPath);
            }

            String s = sb.toString();
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        }catch (Exception e) {
            Log.e(TAG, "Failed to Encode Path Details, E=" + e);
            return sb.toString();
        }
    }

    public static List<PathDetails> fromEncoded(String encoded) {
        List<PathDetails> paths = new ArrayList<>();
        try {
            byte[] decodedBytes = Base64.decode(encoded, Base64.DEFAULT);
            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);

            if(!decodedString.contains(":") || !decodedString.contains(","))
                throw new Exception("String decoded is not in a valid format!");

            String[] parts = decodedString.split(",");
            for(String p : parts) {
                PathDetails detail = parseLine(p);
                if(detail != null) {
                    paths.add(detail);
                }
            }

            return paths;
        }catch (Exception e) {
            Log.e(TAG, "Failed to Decode Paths, Error=" + e + " String=" + encoded);
            return paths;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return fullPath;
    }
}
