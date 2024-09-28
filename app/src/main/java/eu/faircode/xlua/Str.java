package eu.faircode.xlua;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Str {
    public static final String EMPTY = "";
    public static final String ASTERISK = "*";
    public static final String COLLEN = ":";
    public static final String NEW_LINE = "\n";
    public static final String TRUE = "true";
    public static final String FALSE = "false";

    public static final Character SPACE_CHAR = ' ';


    public static String createFilledCopy(String str, String fillChar) {
        if(str == null) return fillChar;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < str.length(); i++)
            sb.append(fillChar);
        return sb.toString();
    }

    public static String combine(String str1, String str2) { return combine(str1, str2, true); }
    public static String combine(String str1, String str2, boolean useNewLine) {
        StringBuilder sb = new StringBuilder();
        sb.append(str1);
        if(useNewLine) sb.append(NEW_LINE);
        sb.append(str2);
        return sb.toString();
    }

    public static boolean isValid(CharSequence s) { return s != null && isValid(s.toString()); }
    public static boolean isValid(String s) { return s != null && !TextUtils.isEmpty(s); }

    public static boolean isValidNotWhitespaces(CharSequence s) { return s != null && isValidNotWhitespaces(s.toString()); }
    public static boolean isValidNotWhitespaces(String s) {
        if(s == null || s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if(!(c == '\n' || c == '\t' || c == '\0' || c == ' ' || c == '\b' || c == '\r' || c == '\f')) return true;
        } return false;
    }

    public static boolean hasChars(String s, char... cs) {
        if(s == null) return false;
        for(int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            for (char cr : cs)
                if (c == cr) return true;
        } return false;
    }

    public static int tryParseInt(String v) {
        try { return Integer.parseInt(v);
        }catch (Exception e) { return 0; }
    }

    public static Double tryParseDouble(String v) {
        try { return Double.parseDouble(v);
        }catch (Exception e) { return 0.0; }
    }

    public static Float tryParseFloat(String v) {
        try { return Float.parseFloat(v);
        }catch (Exception ignored) { return 0.1F; }
    }

    public static Long tryParseLong(String v) {
        try { return Long.parseLong(v);
        }catch (Exception ignored) { return 0L; }
    }

    public static String bytesToHex(byte[] bys) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bys) hexString.append(String.format("%02X ", b));
        return hexString.toString();
    }

    public static String toHex(String input) {
        if (input == null) return null;
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            int charValue = input.charAt(i);
            hexString.append(String.format("%04X ", charValue)); // Uses 4 digits to account for Unicode values
        }

        return hexString.toString().trim();
    }

    public static String getFirstString(String str, String delimiter) { return getFirstString(str, delimiter, null); }
    public static String getFirstString(String str, String delimiter, String defaultValue) {
        String trim = trim(str, delimiter, true);
        if (delimiter == null || delimiter.isEmpty()) return defaultValue != null ? defaultValue : trim;
        if (trim == null || trim.isEmpty()) return defaultValue;
        if (!trim.contains(delimiter)) return trim;
        String[] split = trim.split(Pattern.quote(delimiter));
        return split.length > 0 ? split[0] : defaultValue;
    }


    public static String getLastString(String str, String delimiter) { return getLastString(str, delimiter, null); }
    public static String getLastString(String str, String delimiter, String defaultValue) {
        str = trim(str, delimiter, true);
        if(delimiter == null || delimiter.isEmpty()) return defaultValue != null ? defaultValue : str;
        if(str == null || str.isEmpty()) return defaultValue;
        if(!str.contains(delimiter)) return str;
        String[] sp = str.split(Pattern.quote(delimiter));
        return sp.length > 0 ? sp[sp.length - 1] : defaultValue;
    }

    public static String trim(String s, String trimPrefix, boolean ensureTrimmed) {
        if(s == null || s.isEmpty()) return s;
        s = s.trim();
        if(!s.contains(trimPrefix)) return s;

        if(ensureTrimmed) {
            while (s.startsWith(trimPrefix)) {
                s = s.substring(1);
                s = s.trim();
            }
            while (s.endsWith(trimPrefix)) {
                s = s.substring(0, s.length() - 1);
                s = s.trim();
            }
        }else {
            if(s.startsWith(trimPrefix)) s = s.substring(1);
            if(s.endsWith(trimPrefix)) s = s.substring(0, s.length() - 1);
        }

        return s;
    }

    public static List<String> splitToList(String str) { return splitToList(str, ","); }
    public static List<String> splitToList(String str, String delimiter) {
        if(!isValidNotWhitespaces(str) || !str.contains(delimiter)) return new ArrayList<>();
        String[] splt = str.split(Pattern.quote(delimiter));
        return Arrays.asList(splt);
    }

    public static String joinArray(String[] arr) { return joinArray(arr, ","); }
    public static String joinArray(String[] arr, String delimiter) {
        if(arr == null) return "";
        StringBuilder sb = new StringBuilder();
        int sz = arr.length - 1;
        for(int i = 0; i < arr.length; i++) {
            String l = arr[i];
            if(!isValidNotWhitespaces(l)) continue;
            sb.append(l);
            if(i != sz) {
                sb.append(delimiter);
            }
        }

        return sb.toString();
    }

    public static String joinList(List<String> list) { return joinList(list, ","); }
    public static String joinList(List<String> list, String delimiter) {
        StringBuilder sb = new StringBuilder();
        int sz = list.size() - 1;
        for(int i = 0; i < list.size(); i++) {
            String l = list.get(i);
            if(!isValidNotWhitespaces(l))
                continue;

            sb.append(l);
            if(i != sz) {
                sb.append(delimiter);
            }
        }

        return sb.toString();
    }

    public static Boolean toBoolean(String str) { return toBoolean(str, null); }
    public static Boolean toBoolean(String str, Boolean defaultValue) {
        try {
            if(str == null || TextUtils.isEmpty(str)) return defaultValue;
            str = str.trim().toLowerCase();
            if(str.equals("yes") || str.equals("true") || str.equals("1") || str.equals("checked") || str.equals("enabled") || str.equals("succeed") || str.equals("succeeded")) return true;
            if(str.equals("no") || str.equals("false") || str.equals("0") || str.equals("unchecked") || str.equals("disabled") || str.equals("fail") || str.equals("failed") || str.equals("error")) return false;
            return defaultValue;
        }catch (Exception ex) {
            return defaultValue;
        }
    }
}
