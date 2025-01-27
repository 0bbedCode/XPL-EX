package eu.faircode.xlua.x;

import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.LibUtil;

/*
    ToDO: Clean this damn class up after release
 */
public class Str {
    private static final String TAG = LibUtil.generateTag(Str.class);

    public static final String EMPTY = "";
    public static final String ASTERISK = "*";
    public static final String COLLEN = ":";
    public static final String NEW_LINE = "\n";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String COMMA = ",";
    public static final String PERIOD = ".";
    public static final String WHITE_SPACE = " ";
    private static final Pattern DOUBLE_NEWLINE_PATTERN = Pattern.compile("\n\\s*\n");

    public static final int MOBILE_SAFE_LENGTH = 40;
    public static final int MIN_DIVIDER_LENGTH = 4;  // Minimum to have at least "-|-"
    public static final char DEFAULT_DIVIDER = '-';
    public static final String DEFAULT_TEXT = "";

    public static final Character SPACE_CHAR = ' ';

    public static final String[] CONTROL_CHARS = new String[] {
            "\n",   // Line feed (new line)
            "\r",   // Carriage return
            "\t",   // Tab
            "\f",   // Form feed
            "\b",   // Backspace
            "\0"    // Null character
    };

    // If you really need the quote, use it without escape:
    public static final String[] ESCAPE_CHARS = new String[] {
            "\n",   // Line feed (new line)
            "\r",   // Carriage return
            "\t",   // Tab
            "\f",   // Form feed
            "\b",   // Backspace
            "\0",   // Null character
            "\"",   // Double quote
            "'",    // Single quote (no escape needed)
            "\\"    // Backslash
    };

    //formattedString.replaceAll("(?m)\\n{2,}", "\n");


    //Clean these three
    public static String cleanDoubleNewLines(String input) { return DOUBLE_NEWLINE_PATTERN.matcher(input).replaceAll("\n"); }
    public static String noNewLineEnding(String input) { return input.endsWith("\n") ? input.substring(0, input.length() - 1) : input; }
    public static String ensureNoDoubleNewLines(String input) {
        Matcher matcher = DOUBLE_NEWLINE_PATTERN.matcher(input);
        if(matcher.matches()) return ensureNoDoubleNewLines(matcher.replaceAll("\n"));
        return input;
    }

    public static boolean equalsObject(Object o, String compareAgainst) { return equalsObject(o, compareAgainst, true); }
    public static boolean equalsObject(Object o, String compareAgainst, boolean caseSensitive) {
        if(!(o instanceof String)) return false;
        return caseSensitive ? ((String)o).equals(compareAgainst) : ((String)o).equalsIgnoreCase(compareAgainst);
    }

    public static String noNL(Object o) { return noNL(o, " "); }
    public static String noNL(Object o, String replaceString) {
        String s = ensureNoDoubleNewLines(toStringOrNull(o));
        return s.replaceAll("\n", replaceString);
    }

    public static String fm(String str, Object... objects) { return fm(true, false, str, objects); }
    public static String fm(boolean noDoubleNewLines, String str, Object... objects) { return fm(noDoubleNewLines, false, str, objects); }
    public static String fm(boolean noDoubleNewLines, boolean newLineBetweenEach, String str, Object... objects) {
        try {
            if(!ArrayUtils.isValid(objects) || TextUtils.isEmpty(str)) return str;
            Object[] os = new Object[objects.length];
            for(int i = 0; i < objects.length; i++) {
                Object o = objects[i];
                if(o == null) os[i] = "null";
                else {
                    os[i] = o;
                }
            }

            String formattedString = String.format(str, os);
            if (newLineBetweenEach) {
                String[] placeholders = str.split("%s");
                StringBuilder newLineFormatted = new StringBuilder();
                for (int i = 0; i < placeholders.length; i++) {
                    newLineFormatted.append(placeholders[i]);
                    if (i < objects.length) {
                        newLineFormatted.append(objects[i]);
                        newLineFormatted.append("\n"); // Append a newline after each object
                    }
                }
                formattedString = newLineFormatted.toString().trim(); // Remove any trailing newline
            }

            return noDoubleNewLines ? ensureNoDoubleNewLines(formattedString) : formattedString;
        }catch (Exception e) {
            Log.e(TAG, "Failed to format String! Make sure you have correct number of objects for each insert of %s ! Objects Count=" + ArrayUtils.safeLength(objects) + " String=[" + str + "] >> Error=" + e);
            return Str.combineEx("[FORMAT_EXCEPTION]::[", str, "]::[FORMAT_EXCEPTION]");
        }
    }

    public static String e(Throwable e, boolean stack) {
        StringBuilder sb = new StringBuilder();
        sb.append("Error=").append(e.getMessage());
        if(stack) {
            sb.append("\n")
                    .append("Stack:")
                    .append("\n")
                    .append(RuntimeUtils.getStackTraceSafeString(e));
        }

        return cleanDoubleNewLines(sb.toString());
    }


    public static String isNullAsString(Object v) { return v == null ? "False" : "True"; }

    @SuppressWarnings("StringOperationCanBeSimplified")
    public static String createCopy(String x) { return new String(x); }

    //Have a T Combine list thing ? T can be like InetAddress (assuming it has a toString)

    public static String getNonNullOrEmptyString(String a, String b) { return TextUtils.isEmpty(a) ? b : a; }


    public static String getNonNullString(String a, String b) { return a == null ? b : a; }

    //Make a ALL version
    public static boolean areEqualsAnyIgnoreCase(String a, String... compareItems) {
        if(a == null) {
            for(String c : compareItems) if(c == null) return true;
            return false;
        }

        for(String c : compareItems)
            if(a.equalsIgnoreCase(c))
                return true;

        return false;
    }

    public static boolean areEqualAny(String a, String... compareItems) {
        for(String c : compareItems)
            if(Objects.equals(a, c))
                return true;

        return false;
    }

    public static boolean areEqual(String a, String b) { return Objects.equals(a, b); }
    public static boolean areEqualIgnoreCase(String a, String b) { return a == null ? b == null : a.equalsIgnoreCase(b); }

    public static boolean areEqual(String a, String b, boolean caseSensitive, boolean treatNullAndEmptyAsSame) {
        /*return treatNullAndEmptyAsSame ?
                (TextUtils.isEmpty(a) && TextUtils.isEmpty(b)) || caseSensitive ? areEqual(a, b) : areEqualIgnoreCase(a, b) :
                caseSensitive ? areEqual(a, b) : areEqualIgnoreCase(a, b);*/

        if(treatNullAndEmptyAsSame) {
            if(TextUtils.isEmpty(a) && TextUtils.isEmpty(b))
                return true;
        } else {
            if(a == null && b == null)
                return true;
        }

        if(a == null || b == null)
            return false;

        return caseSensitive ? a.equalsIgnoreCase(b) : a.equals(b);
    }

    public static String ensureIsNotNullOrDefault(String s, String defaultValue) { return s == null ? defaultValue : s; }

    public static String ensureIsValidOrDefault(String s, String defaultValue) { return isValidNotWhitespaces(s) ? s : defaultValue; }

    public static String ensureIsValidOrNull(String s) { return isValidNotWhitespaces(s) ? s : null; }

    public static CharSequence convertToUTF8CharSequence(String input) {
        byte[] utf8Bytes = input.getBytes(StandardCharsets.UTF_8);
        return new String(utf8Bytes, StandardCharsets.UTF_8);
    }

    public static String subStringIndexOf(String data, String indexOfData, boolean cutOffAfterIndexData) {
        if (data == null || indexOfData == null || indexOfData.isEmpty()) return data;
        int indexOf = data.indexOf(indexOfData);
        if (indexOf == -1) return data;
        if (cutOffAfterIndexData) {
            int startIndex = indexOf + indexOfData.length();
            return data.substring(startIndex);
        } else {
            return data.substring(indexOf);
        }
    }
    public static String[] split(String data, String delimiter, boolean ensureEachLineValid) { return split(data, delimiter, ensureEachLineValid, false); }
    public static String[] split(String data, String delimiter, boolean ensureEachLineValid, boolean trimEach) {
        if(data == null || data.isEmpty()) return new String[0];
        String[] parts = data.split(Pattern.quote(delimiter));
        if(ensureEachLineValid) {
            List<String> partsCleaned = new ArrayList<>();
            for(String p : parts) {
                if(!isValidNotWhitespaces(p)) continue;
                partsCleaned.add(trimEach ? p.trim() : p);
            }

            return partsCleaned.toArray(new String[0]);
        } else if(trimEach) {
            List<String> partsCleaned = new ArrayList<>();
            for(String p : parts) {
                String t = p.trim();
                if(t.isEmpty()) continue;
                partsCleaned.add(t);
            }

            return partsCleaned.toArray(new String[0]);
        }

        return parts;
    }

    public static String createFilledCopy(String str, String fillChar) {
        if(str == null) return fillChar;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < str.length(); i++)
            sb.append(fillChar);
        return sb.toString();
    }

    //NOTE WHEN USING THIS ALSO TRIMS WHITE SPACE!
    public static String combineWithDelimiter(String str1, String str2, String delimiter, boolean allowDup) {
        StringBuilder sb = new StringBuilder();
        if(!TextUtils.isEmpty(str1))
            sb.append(trim(str1, delimiter, true, true));
        if(!TextUtils.isEmpty(str2)) {
            sb.append(delimiter);
            sb.append(trim(str2, delimiter, true, true));

            if(allowDup) {
                String[] parts = sb.toString().split(delimiter);
                List<String> parsed = new ArrayList<>();
                for(String p : parts) {
                    if(!parsed.contains(p))
                        parsed.add(p);
                }

                return Str.joinList(parsed, delimiter);
            }
        }



        return sb.toString();
    }

    public static boolean isEmpty(String s) { return s == null || s.isEmpty(); }
    public static boolean isAnyEmpty(String... strings) {
        if(strings != null) {
            for(String s : strings) {
                if(s == null || s.isEmpty())
                    return true;
            }
        }

        return false;
    }

    public static String combineEx(Object... objects) { return combineEx(false, objects); }
    public static String combineEx(boolean appendNullIfNull, Object... objects) {
        StringBuilder sb = new StringBuilder();
        if(ArrayUtils.isValid(objects)) {
            for(Object o : objects) {
                if(o instanceof String) {
                    sb.append((String) o);
                    continue;
                }

                //This is all kinda silly :P
                if(o == null) {
                    if(appendNullIfNull) {
                       sb.append("null");
                    }
                } else {
                    sb.append(String.valueOf(o));
                }
            }
        }

        return sb.toString();
    }

    public static String combine(String str1, String str2) { return combine(str1, str2, false); }
    public static String combine(String str1, String str2, boolean useNewLine) {
        StringBuilder sb = new StringBuilder();
        sb.append(str1);
        if(useNewLine) sb.append(NEW_LINE);
        sb.append(str2);
        return sb.toString();
    }

    //Move this
    public static String hookToJsonString(XLuaHook hook) {
        try {
             return hook.toJSON();
        }catch (Exception ignored) { }
        return "Name=" + hook.getName() + " Id=" + hook.getId() + " Class=" + hook.getClassName();
    }

    /*public static String repeatChar(char c, int repeatTimes) {
        if(repeatTimes < 2) return String.valueOf(c);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < repeatTimes; i++)
            sb.append(c);

        return sb.toString();
    }*/

    public static String toString(Object o) { return toString(o, null); }
    public static String toString(Object o, String defaultValueIfNull) {
        if(o == null)
            return defaultValueIfNull;

        if(o instanceof String)
            return (String) o;

        try {
            return String.valueOf(o);
        }catch (Exception ignored) {
            return defaultValueIfNull;
        }
    }

    public static String toString(EditText ti) { return toString(ti, null); }
    public static String toString(EditText ti, String defaultIfNull) {
        if(ti == null)
            return defaultIfNull;
        try {
            Editable et = ti.getText();
            if(et == null)
                return defaultIfNull;

            return et.toString();
        }catch (Exception e) {
            return defaultIfNull;
        }
    }

    public static String toStringOrNull(Object o) {
        //
        if(o != null) {
            try {
                return String.valueOf(o);
            }catch (Exception ignored) { }
        }
        return "null";
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

    public static boolean containsAny(String s, String... values) {
        if(TextUtils.isEmpty(s)) return false;
        String low = s.toLowerCase();
        for(String v : values) {
            if(v != null) {
                if(low.contains(v.toLowerCase()))
                    return true;
            }
        }

        return false;
    }

    public static boolean hasChars(String s, char... cs) {
        if(s == null) return false;
        for(int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            for (char cr : cs)
                if (c == cr) return true;
        } return false;
    }

    public static int tryParseInt(String v, int defaultValue) {
        try {
            return Integer.parseInt(v);
        }catch (Exception ignored) {
            return defaultValue;
        }
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
        if (delimiter == null || delimiter.isEmpty()) return defaultValue != null ? defaultValue : str;
        String trim = trimEx(str, delimiter);
        if (TextUtils.isEmpty(trim)) return defaultValue;       //if default value is Null ?
        if (!trim.contains(delimiter)) return trim;
        String[] split = trim.split(Pattern.quote(delimiter));
        return split.length > 0 ? split[0] : defaultValue;
    }

    public static String getParentPath(String input) {
        if(input == null) return null;
        if(input.contains(File.separator)) {
            int lastSeparatorIndex = input.lastIndexOf(File.separator);
            return lastSeparatorIndex > 0 ? input.substring(0, lastSeparatorIndex) : null;
        }

        return input;
    }

    public static int getEndInteger(String str) { return getEndInteger(str, -1); }
    public static int getEndInteger(String str, int defaultValue) {
        String cleaned = trimControlChars(str);
        if(TextUtils.isEmpty(cleaned) || !Character.isDigit(cleaned.charAt(cleaned.length() - 1)))
            return defaultValue;

        StringBuilder sb = new StringBuilder();
        char[] chars = cleaned.toCharArray();
        for(int i = chars.length - 1; i >= 0; i--) {
            char c = chars[i];
            if(Character.isDigit(c)) {
               sb.append(c);
            } else {
                break;
            }
        }

        return tryGetIntegerFromString(sb.reverse().toString(), defaultValue);
    }

    public static int tryGetIntegerFromString(String s, int defaultValue) {
        if(TextUtils.isEmpty(s)) return defaultValue;
        String t = trimStart(s, "0");
        if(TextUtils.isEmpty(t))  return 0;
        try {
            if(s.length() >= 9) {
                try {
                    return Integer.parseInt(t.substring(t.length() - 9));
                }catch (Exception e) {
                    return Integer.parseInt(t.substring(t.length() - 8));
                }
            } else {
                return Integer.parseInt(t);
            }
        }catch (Exception outerException) {
            return defaultValue;
        }
    }

    public static String repeatString(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < count; i++) sb.append(s);
        return sb.toString();
    }

    public static String getLastStringEx(String str, String delimiter) {
        // Check for null or empty inputs
        if (str == null || str.isEmpty() || delimiter == null || delimiter.isEmpty()) {
            return str;
        }

        //We don't need to use Pattern.quote as none of these functions use RegEx
        str = trimEx(str, true, true, delimiter);

        // Check if the delimiter is in the string
        int lastIndex = str.lastIndexOf(delimiter);
        if (lastIndex == -1) {
            return str; // Return the original string if delimiter is not found
        }

        // Extract and return the last substring after the delimiter
        return str.substring(lastIndex + delimiter.length());
    }


    public static String getLastString(String str, String delimiter) { return getLastString(str, delimiter, null); }
    public static String getLastString(String str, String delimiter, String defaultValue) {
        str = trim(str, delimiter, true, false);
        if(delimiter == null || delimiter.isEmpty()) return defaultValue != null ? defaultValue : str;
        if(str == null || str.isEmpty()) return defaultValue;
        if(!str.contains(delimiter)) return str;
        String[] sp = str.split(Pattern.quote(delimiter));
        return sp.length > 0 ? sp[sp.length - 1] : defaultValue;
    }

    public static String trim(String s, String trimPrefix, boolean ensureTrimmed, boolean trimWhitespace) {
        //Stop using this version ? use the ex version then rename ex to this ?
        if (s == null || s.isEmpty() || trimPrefix == null || trimPrefix.isEmpty()) {
            return s;
        }

        //Make a version so it does multiple trim prefixes "String trimPrefixes..." like "trim("\n\n\t\n\tSomething\n\t\n\t\t\n\n\t\n", "\n", "\n", true, true)

        if(trimWhitespace) {
            s = s.trim();
            if (!s.contains(trimPrefix)) {
                return s;
            }
        }

        int start = 0;
        int end = s.length();
        int prefixLength = trimPrefix.length();

        // Trim from the start
        if (ensureTrimmed) {
            while (start < end && s.startsWith(trimPrefix, start)) {
                start += prefixLength;
            }
        } else if (s.startsWith(trimPrefix)) {
            start = prefixLength;
        }

        // Trim from the end
        if (ensureTrimmed) {
            while (end > start && s.startsWith(trimPrefix, end - prefixLength)) {
                end -= prefixLength;
            }
        } else if (end > prefixLength && s.startsWith(trimPrefix, end - prefixLength)) {
            end -= prefixLength;
        }

        // Only create a new string if we actually trimmed something
        return (start > 0 || end < s.length()) ? trimWhitespace ? s.substring(start, end).trim() : s.substring(start, end) : s;
    }

    /**
     * Trim all Control Chars, \n \t \r \b \0
     */
    public static String trimControlChars(String s) { return trimEx(s, true, true, true, true, CONTROL_CHARS); }


    /**
     * Simplest form - trim both ends with defaults (ensureTrimmed=true, trimWhitespace=true)
     */
    public static String trimEx(String s, String... trimPrefixes) { return trimEx(s, true, true, true, true, trimPrefixes); }

    /**
     * Trims specific end(s) with default options (ensureTrimmed=true, trimWhitespace=true)
     */
    public static String trimEx(String s, boolean trimStart, boolean trimEnd, String... trimPrefixes) { return trimEx(s, true, true, trimStart, trimEnd, trimPrefixes); }

    /**
     * Trims start only with default options (ensureTrimmed=true, trimWhitespace=true)
     */
    public static String trimStart(String s, String... trimPrefixes) { return trimEx(s, true, true, true, false, trimPrefixes); }

    /**
     * Trims end only with default options (ensureTrimmed=true, trimWhitespace=true)
     */
    public static String trimEnd(String s, String... trimPrefixes) { return trimEx(s, true, true, false, true, trimPrefixes); }

    /**
     * Complete trim with all options
     */
    @SuppressWarnings("ConstantConditions")
    public static String trimEx(String s,
                                boolean ensureTrimmed,
                                boolean trimWhitespace,
                                boolean trimStart,
                                boolean trimEnd,
                                String... trimPrefixes) {
        if (s == null || s.isEmpty() || trimPrefixes == null || trimPrefixes.length == 0) {
            return s;
        }

        List<String> validPrefixes = new ArrayList<>();
        if(trimPrefixes != null) {
            for(String p : trimPrefixes) {
                if(!TextUtils.isEmpty(p))
                    validPrefixes.add(p);
            }
        }

        if(trimWhitespace && !validPrefixes.contains(WHITE_SPACE))
            validPrefixes.add(WHITE_SPACE);

        if(validPrefixes.isEmpty())
            return s;

        trimPrefixes = validPrefixes.toArray(new String[0]);

        String previousString;
        do {
            previousString = s;

            for (String prefix : trimPrefixes) {
                if (prefix == null || prefix.isEmpty()) continue;

                int start = 0;
                int end = s.length();
                int prefixLength = prefix.length();

                if (trimStart) {
                    if (ensureTrimmed) {
                        while (start < end && s.startsWith(prefix, start)) {
                            start += prefixLength;
                        }
                    } else if (s.startsWith(prefix)) {
                        start = prefixLength;
                    }
                }

                if (trimEnd) {
                    if (ensureTrimmed) {
                        while (end > start && s.startsWith(prefix, end - prefixLength)) {
                            end -= prefixLength;
                        }
                    } else if (end > prefixLength && s.startsWith(prefix, end - prefixLength)) {
                        end -= prefixLength;
                    }
                }

                if (start > 0 || end < s.length()) {
                    s = s.substring(start, end);
                }
            }
        } while (ensureTrimmed && !s.equals(previousString));

        return s;
    }


    public static List<String> splitToList(String str) { return splitToList(str, ","); }
    public static List<String> splitToList(String str, String delimiter) {
        if(TextUtils.isEmpty(str))
            return new ArrayList<>();

        if(!str.contains(delimiter))
            return ListUtil.toSingleList(str);

        String[] split = str.split(Pattern.quote(delimiter));
        return Arrays.asList(split);
    }

    public static String joinArray(String[] arr) { return joinArray(arr, ","); }
    public static String joinArray(String[] arr, String delimiter) {
        if(arr == null) return "";

        if(DebugUtil.isDebug())
            Log.d("XLua.Str", "Joining String Array Length=" + arr.length + " Delimiter=" + delimiter);

        StringBuilder sb = new StringBuilder();
        int sz = arr.length - 1;
        for(int i = 0; i < arr.length; i++) {
            String l = arr[i];
            //if(!isValidNotWhitespaces(l)) continue;
            if(TextUtils.isEmpty(l)) continue;
            sb.append(l);
            if(i != sz) {
                sb.append(delimiter);
            }
        }

        return sb.toString();
    }

    //public static final List<String> STRING_NUMBERS = Arrays.asList("")

    public static boolean isNumeric(String s) {
        if(TextUtils.isEmpty(s)) return false;
        char[] chars = s.toCharArray();
        for(char c : chars) {
            if(!Character.isDigit(c)) return false;
        }

        return true;
    }

    public static boolean isAlphabet(String s) {
        if(TextUtils.isEmpty(s)) return false;
        char[] chars = s.toCharArray();
        for(char c : chars) {
            if(!Character.isAlphabetic(c)) return false;
        }

        return true;
    }

    public static String getItemAt(String[] array, int index, String defaultValue) {
        if(array == null || array.length <= index)
            return defaultValue;

        String v = array[index];
        return v == null ? defaultValue : v;
    }

    public static boolean isSize(String s, int wantedCount) { return s != null && s.length() == wantedCount;  }
    public static int getSize(String s) { return s == null ? -1 : s.length(); }

    public static boolean isSpecialChar(char c) { return c == '\n' || c == '\t' || c == '\b' || c == ' ' || c == '\r'; }

    public static String joinList(List<String> list) { return joinList(list, ","); }
    public static String joinList(List<String> list, String delimiter) {
        if(list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < list.size(); i++) {
            String l = list.get(i);
            if(TextUtils.isEmpty(l)) continue;
            if(sb.length() > 0) sb.append(delimiter);
            sb.append(l);
        }

        return sb.toString();
    }



    /*
    public static String joinList(List<String> list, String delimiter, int startIndex, int endIndex) {
        if(list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < list.size(); i++) {
            String l = list.get(i);
            if(TextUtils.isEmpty(l)) continue;
            if(sb.length() > 0) sb.append(delimiter);
            sb.append(l);
        }

        return sb.toString();
    }*/


    public static boolean toBool(String str) { return toBool(str, false); }
    public static boolean toBool(String str, Boolean defaultValue) {
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

    public static String ensureEndsWith(String s, String ending) {
        return s == null ? ending : s.endsWith(ending) ? s : s + ending;
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


    /**
     * Creates a divider with centered text. Handles all edge cases safely.
     *
     * @param dividerChar The character to use for the divider (or default if invalid)
     * @param text The text to center in the divider (or empty if null)
     * @return A string with the text centered between divider characters
     */
    public static String dividerWithTitle(char dividerChar, String text) {
        // Validate/default the divider char (avoid control chars, zero-width chars, etc)
        char safeChar = isSafeDividerChar(dividerChar) ? dividerChar : DEFAULT_DIVIDER;

        // Normalize text input
        String safeText = normalizeDividerText(text);

        // If text is empty, return simple divider
        if (safeText.isEmpty()) {
            return repeatChar(safeChar, MOBILE_SAFE_LENGTH);
        }

        try {
            // Add spaces around text for padding, with bounds checking
            String paddedText = " " + safeText + " ";

            // Safety check for string length overflow
            if (paddedText.length() >= MOBILE_SAFE_LENGTH - MIN_DIVIDER_LENGTH) {
                // Text too long - truncate and add ellipsis
                int maxLength = MOBILE_SAFE_LENGTH - MIN_DIVIDER_LENGTH - 3; // space for "..."
                if (maxLength < 1) {
                    return repeatChar(safeChar, MOBILE_SAFE_LENGTH); // fallback to simple divider
                }
                paddedText = " " + safeText.substring(0, maxLength) + "... ";
            }

            // Calculate remaining space for dividers
            int remainingSpace = Math.max(MIN_DIVIDER_LENGTH,
                    MOBILE_SAFE_LENGTH - paddedText.length());

            // Calculate divider lengths, ensuring left side is smaller when uneven
            int rightDivLength = Math.max(MIN_DIVIDER_LENGTH/2, (remainingSpace + 1) / 2);
            int leftDivLength = Math.max(MIN_DIVIDER_LENGTH/2, remainingSpace - rightDivLength);

            // Final safety check for total length
            String result = repeatChar(safeChar, leftDivLength) +
                    paddedText +
                    repeatChar(safeChar, rightDivLength);

            // Ensure we don't exceed MOBILE_SAFE_LENGTH
            if (result.length() > MOBILE_SAFE_LENGTH) {
                return result.substring(0, MOBILE_SAFE_LENGTH);
            }

            return result;

        } catch (Exception e) {
            // Absolute fallback - return simple divider
            return repeatChar(safeChar, MOBILE_SAFE_LENGTH);
        }
    }

    /**
     * Safe character repeater that handles negative numbers and other edge cases
     */
    public static String repeatChar(char c, int length) {
        // Handle invalid lengths
        if (length <= 0) return "";
        if (length == 1) return String.valueOf(c);

        try {
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length && i < MOBILE_SAFE_LENGTH; i++) {
                sb.append(c);
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(DEFAULT_DIVIDER);
        }
    }

    /**
     * Validates and normalizes input text
     */
    public static String normalizeDividerText(@Nullable String text) {
        if (text == null) return DEFAULT_TEXT;
        // Remove control characters and trim
        String cleaned = text.replaceAll("[\\p{Cntrl}]", "").trim();
        // If empty after cleaning, return default
        if (cleaned.isEmpty()) return DEFAULT_TEXT;
        return cleaned;
    }

    /**
     * Checks if a character is safe to use as a divider
     */
    public static boolean isSafeDividerChar(char c) {
        // Avoid control characters, zero-width characters, and other problematic chars
        return c > 32 && c < 127 && // Basic ASCII printable
                c != 127 && // DEL
                Character.isValidCodePoint(c) &&
                !Character.isSpaceChar(c) &&
                !Character.isISOControl(c);
    }

    /**
     * Convenience method with default divider char
     */
    //public static String withText(String text) {
    //    return withText(DEFAULT_DIVIDER, text);
    //}
}
