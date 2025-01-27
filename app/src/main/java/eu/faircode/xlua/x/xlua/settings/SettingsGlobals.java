package eu.faircode.xlua.x.xlua.settings;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.faircode.xlua.x.data.utils.MapUtils;

public class SettingsGlobals {
    public static final List<String> SPECIAL_SETTING_TYPES = Arrays.asList("list", "bool", "boolean", "parent", "control");
    public static final List<String> IS_BUILT_IN_SETTING = Arrays.asList("mcc", "mnc", "lac", "cid");
    public static final HashMap<String, String> SETTINGS_RESOLVER_MAP = MapUtils.create(
            MapUtils.entry("Imei", "IMEI"),
            MapUtils.entry("Ssid", "SSID"),
            MapUtils.entry("Soc", "SoC"),

            MapUtils.entry("Boolean", ""),
            MapUtils.entry("Bool", ""),
            MapUtils.entry("List", ""),
            MapUtils.entry("Parent", "")

            /*MapUtils.entry("1", "One"),
            MapUtils.entry("2", "Two"),
            MapUtils.entry("3", "Three"),
            MapUtils.entry("4", "Four"),
            MapUtils.entry("5", "Five"),
            MapUtils.entry("6", "Six"),
            MapUtils.entry("7", "Seven"),
            MapUtils.entry("8", "Eight"),
            MapUtils.entry("9", "Nine"),
            MapUtils.entry("0", "Zero")*/

    );


    private static final Pattern ARRAY_PATTERN = Pattern.compile(".*\\.\\[\\s*\\d+\\s*(?:,\\s*\\d+\\s*)*\\]$");

    // Pattern to extract numbers from brackets
    private static final Pattern NUMBERS_PATTERN = Pattern.compile("\\d+");

    /**
     * Checks if a string ends with a pattern like ".[numbers]" using regex
     */
    public static boolean endsWithArrayPattern(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        return ARRAY_PATTERN.matcher(input).matches();
    }


    /**
     * Get the base string (everything before ".[")
     * Returns null if pattern is invalid
     */
    public static String getBaseString(String input) {
        if (!endsWithArrayPattern(input)) {
            return input;
        }

        int lastIndex = input.lastIndexOf(".[");
        if (lastIndex == -1) {
            return null;
        }

        return input.substring(0, lastIndex);
    }

    /**
     * Get the array part (everything inside brackets including whitespace)
     * Returns null if pattern is invalid
     */
    public static String getArrayString(String input) {
        if (!endsWithArrayPattern(input)) {
            return null;
        }

        int startIndex = input.lastIndexOf(".[") + 2;
        int endIndex = input.length() - 1;

        return input.substring(startIndex, endIndex);
    }

    public static List<String> indexesToSettingNames(String baseSetting, List<String> parts) {
        if(parts == null || parts.isEmpty() || TextUtils.isEmpty(baseSetting)) return null;
        List<String> settings = new ArrayList<>(parts.size());
        try {
            for(String p : parts) {
                if(p.isEmpty()) continue;
                int v = Integer.parseInt(p);
                settings.add(baseSetting + "." + p);
            }
        }catch (Exception ignored) { }
        return settings;
    }


    /**
     * Get array of integers from the bracket notation
     * Returns null if pattern is invalid
     */
    public static List<Integer> getArrayNumbers(String input) {
        String arrayPart = getArrayString(input);
        if (arrayPart == null) {
            return null;
        }

        List<Integer> numbers = new ArrayList<>();
        Matcher matcher = NUMBERS_PATTERN.matcher(arrayPart);

        while (matcher.find()) {
            numbers.add(Integer.parseInt(matcher.group()));
        }

        return numbers;
    }

}
