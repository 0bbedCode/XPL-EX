package eu.faircode.xlua.x.hook.interceptors.file.stat;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class StatUtils {
    private static final String TAG = "XLua.StatUtils";


    public static boolean is1969Year(String dateString) {
        if(dateString == null) return false;
        String t = dateString.trim();
        return t.startsWith("1969");
    }

    public static String generateDeviceId() {
        // Generate major number (1-255)
        int majorNumber = RandomGenerator.nextInt(1, 256);
        // Generate minor number (0-255)
        int minorNumber = RandomGenerator.nextInt(256);
        // Combine major and minor numbers
        int combinedNumber = (majorNumber << 8) | minorNumber;
        // Format the output
        String hexRepresentation = String.format("%xh", combinedNumber);
        @SuppressLint("DefaultLocale") String decimalRepresentation = String.format("%dd", combinedNumber);
        return hexRepresentation + "/" + decimalRepresentation;
    }

    public static long parseDeviceId(String deviceStr) {
        // Split "801h/2049d" into ["801h", "2049d"]
        String[] parts = deviceStr.split("/");
        // Parse hex part (801h)
        String hexPart = parts[0].toLowerCase().replace("h", "");
        long hexValue = Long.parseLong(hexPart, 16);  // Parse as base 16
        // Parse decimal part (2049d)
        String decPart = parts[1].toLowerCase().replace("d", "");
        long decValue = Long.parseLong(decPart);      // Parse as base 10
        return decValue; // or hexValue - they represent same number in different formats
    }

    public static String deviceIdToString(long deviceId) {
        String hexPart = Long.toHexString(deviceId) + "h";
        String decPart = deviceId + "d";
        return hexPart + "/" + decPart;
    }

    public static boolean isYear2008(long seconds) { return getYearFromSeconds(seconds) == 2008; }
    public static boolean isYear1969(long seconds) { return getYearFromSeconds(seconds) == 1969; }
    public static boolean isYear1969Or2008(long seconds) { return isYear1969(seconds) || isYear2008(seconds); }

    public static String ensureFormatedNumber(long number) {
        if(number > 9)
            return String.valueOf(number);

        return "0" + number;
    }

    public static String findOffset(String line) {
        Pattern pattern = Pattern.compile("\\s([+-](?:0[0-9]|1[0-2])(?:[03][0-9]|45))$");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);  // Returns just the offset without the space
        }
        return null;
    }

    public static String lastModifiedToString(long milliseconds) {
        // Convert from milliseconds to separate seconds and nanos
        long seconds = milliseconds / 1000;
        long nanos = (milliseconds % 1000) * 1_000_000; // Convert remaining millis to nanos

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        String baseStr = sdf.format(cal.getTime());

        return String.format("%s.%09d", baseStr, nanos);
    }

    public static long stringToLastModified(String dateStr) throws ParseException {
        // Split into date part and nanos part
        String[] parts = dateStr.split("\\.");

        // Parse the main date part
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        Date date = sdf.parse(parts[0]);
        long millis = date.getTime();

        // Add nanoseconds converted to milliseconds
        if (parts.length > 1) {
            String nanoStr = parts[1].substring(0, Math.min(parts[1].length(), 9));
            nanoStr = String.format("%-9s", nanoStr).replace(' ', '0');
            long nanos = Long.parseLong(nanoStr);
            millis += nanos / 1_000_000; // Convert nanos to millis
        }

        return millis;
    }

    public static long[] parseDateFromSeconds(long seconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(seconds * 1000L);

        // Extract year, month, and day
        long year = cal.get(Calendar.YEAR);
        long month = cal.get(Calendar.MONTH) + 1; // Month is zero-based, so add 1
        long day = cal.get(Calendar.DAY_OF_MONTH);

        // Return as long array
        return new long[]{year, month, day};
    }

    public static long getYearFromSeconds(long seconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(seconds * 1000L);
        return (long)cal.get(Calendar.YEAR);
    }

    @SuppressLint({"DefaultLocale", "SimpleDateFormat"})
    public static String timespecToString(long seconds, long nanos) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(seconds * 1000L);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        String baseStr = sdf.format(cal.getTime());

        return String.format("%s.%09d", baseStr, nanos);
    }

    @SuppressLint("SimpleDateFormat")
    public static String statTimeToString(long seconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(seconds * 1000L);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());

        return sdf.format(cal.getTime());
    }

    @SuppressLint("SimpleDateFormat")
    public static long[] parseTimestamp(String timestamp) {
        long[] result = new long[2];
        if(TextUtils.isEmpty(timestamp)) return result;
        try {
            // Remove time zone info if present
            String[] parts = timestamp.split(" ");
            String dateTimePart = parts[0] + " " + parts[1]; // Main date-time part

            // Prepare the time and fractional seconds handling
            String fractionalSeconds = "0";
            if (dateTimePart.length() > 19) { // Fractional part is present
                String nanoPart = dateTimePart.substring(20);
                fractionalSeconds = nanoPart.contains("+") ? nanoPart.split("\\+")[0] : nanoPart;
            }

            // Parse main date-time part up to seconds
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = dateFormat.parse(dateTimePart);
            result[0] = date.getTime() / 1000; // Seconds

            // Convert fractional seconds part to nanoseconds (adjust to ensure 9 digits)
            result[1] = Long.parseLong(fractionalSeconds + "000000000".substring(fractionalSeconds.length()));
        } catch (ParseException | NumberFormatException e) {
            Log.e(TAG, "Error Parsing TimeStamp String to Seconds and Nano Seconds, stamp=" + timestamp + " Error=" + e);
        }
        return result;
    }
}
