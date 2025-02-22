package eu.faircode.xlua.x.xlua.hook;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.LibUtil;

public class SettingsParseUtil {
    private static final String TAG = LibUtil.generateTag(SettingsParseUtil.class);

    public static List<String> findStringOccurrences(String mainString, String searchString, int maxSubParamCount) {
        List<String> possibleSettings = new ArrayList<>();
        try {
            int index = 0;

            while ((index = mainString.indexOf(searchString, index)) != -1) {
                String remainingPart = mainString.substring(index + searchString.length());
                int indexOfLast = remainingPart.indexOf(")");
                if (indexOfLast != -1) {
                    String line = remainingPart.substring(0, indexOfLast).trim();
                    if (!line.isEmpty()) {
                        processLine(line, possibleSettings, maxSubParamCount);
                    }
                }
                index++; // Move to the next character
            }
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to Find String Occurrences Str=[%s] Search=[%s] Max=[%s] Error=%s", mainString, searchString, maxSubParamCount, e));
        }
        return possibleSettings;
    }

    private static void processLine(String line, List<String> possibleSettings, int maxSubParamCount) {
        if (line.contains(",")) {
            int count = 0;
            for (String sp : line.split(",")) {
                if(Str.isEmpty(sp))
                    continue;

                count++;
                addSettingIfValid(sp.trim(), possibleSettings);
                if(maxSubParamCount > 0 && count == maxSubParamCount)
                    break;
            }
        } else {
            addSettingIfValid(line.trim(), possibleSettings);
        }
    }

    private static void addSettingIfValid(String text, List<String> possibleSettings) {
        if (!Str.isEmpty(text) && text.contains(".") && text.length() > 3) {
            char first = text.charAt(0);
            char last = text.charAt(text.length() - 1);
            if((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                String trimmed = text.substring(1, text.length() - 1);
                if (!Str.isEmpty(trimmed)) {
                    String lowered = trimmed.toLowerCase();
                    if (!possibleSettings.contains(lowered))
                        possibleSettings.add(lowered);
                }
            }
        }
    }
}
