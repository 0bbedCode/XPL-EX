package eu.faircode.xlua.x.data.utils.random;

import android.annotation.SuppressLint;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import eu.faircode.xlua.x.data.utils.DateUtils;

/*
    ToDO: Make a Randomize Function that takes in "RandomDate" as like a base min date etc or current day
 */

@SuppressLint("DefaultLocale")
public class RandomDate {
    private static final String TAG = "XLua.RandomDate";

    public static final int MIN_EPOC_YEAR = 1969;
    public static final int ANDROID_RELEASE_YEAR = 2008;

    public static final String STAT_FORMAT_WITH_ZONE = "%04d-%02d-%02d %02d:%02d:%02d.%03d%06d %s";
    public static final String STAT_FORMAT = "%04d-%02d-%02d %02d:%02d:%02d.%03d%06d";

    // Static format constants
    public static final String ANDROID_BUILD_FORMAT = "EEE MMM d HH:mm:ss z yyyy";
    public static final String ANDROID_BUILD_FORMAT_ONE = "%04d.%02d.%02d";
    public static final String ANDROID_BUILD_FORMAT_TWO = "%04d%02d%02d";

    public static final String KERNEL_BUILD_FORMAT = "SMP PREEMPT EEE MMM d HH:mm:ss z yyyy";
    public static final String ISO_DATE_FORMAT = "%04d-%02d-%02d";



    private static final RandomDate DEFAULT_DATE = new RandomDate() {{
        year = MIN_EPOC_YEAR;
        month = 1;
        day = 1;
        hour = 0;
        minute = 0;
        second = 0;
        milliseconds = 0;
        nanoSeconds = 0;
    }};


    public int year;
    public int month;
    public int day;
    public int hour;
    public int minute;
    public int second;
    public long milliseconds;  // 0-999
    public long nanoSeconds;   // 0-999999 (represents sub-millisecond precision)

    public boolean isCurrentDay() {
        return this.year == DateUtils.getCurrentYear() &&
                this.month == DateUtils.getCurrentMonth() &&
                this.day == DateUtils.getCurrentDay();
    }

    public RandomDate() { }
    public RandomDate(int minimumYear, int maximumYear) {
        this.year = nextYear(minimumYear, maximumYear);
        randomize();
    }

    private RandomDate(RandomDate other) {
        this.year = other.year;
        this.month = other.month;
        this.day = other.day;
        this.hour = other.hour;
        this.minute = other.minute;
        this.second = other.second;
        this.milliseconds = other.milliseconds;
        this.nanoSeconds = other.nanoSeconds;
    }

    // Convert to ISO date format (YYYY-MM-DD)
    public String toIsoDate() {
        return String.format(ISO_DATE_FORMAT, year, month, day);
    }


    // Convert to STAT format timestamp string with timezone
    public String toStatStamp(String timezoneOffset) {
        return String.format(STAT_FORMAT_WITH_ZONE,
                year, month, day, hour, minute, second,
                milliseconds, nanoSeconds, timezoneOffset);
    }

    // Convert to STAT format timestamp string without timezone

    public String toStatStamp() {
        return String.format(STAT_FORMAT,
                year, month, day, hour, minute, second,
                milliseconds, nanoSeconds);
    }

    // Convert to standard Android build date format (Tue Oct 3 17:38:39 IDT 2023)
    public String toDate() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(year, month - 1, day, hour, minute, second);

        SimpleDateFormat formatter = new SimpleDateFormat(ANDROID_BUILD_FORMAT, Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        return formatter.format(calendar.getTime());
    }

    // Convert to Android build date format one (YYYY.MM.DD)
    public String toDateOne() {
        return String.format(ANDROID_BUILD_FORMAT_ONE, year, month, day);
    }

    // Convert to Android build date format two (YYYYMMDD)
    public String toDateTwo() {
        return String.format(ANDROID_BUILD_FORMAT_TWO, year, month, day);
    }


    // Convert to kernel build date format (SMP PREEMPT Tue Oct 3 17:44:36 IDT 2023)
    public String toKernelDate() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(year, month - 1, day, hour, minute, second);

        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        return "SMP PREEMPT " + formatter.format(calendar.getTime());
    }


    // Get total milliseconds since epoch for this date
    public long getDateMilliseconds() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(year, month - 1, day, hour, minute, second);
        return calendar.getTimeInMillis() + milliseconds; // Add our sub-second milliseconds
    }

    // Get nanoseconds portion for timespec
    public long getDateNanoSeconds_timespec_stat() {
        return (milliseconds * 1000000L) + nanoSeconds;
    }

    // Get Unix timestamp seconds for this date
    public long getDateSeconds() {
        return getDateMilliseconds() / 1000L;
    }

    public String getDayOfWeek() { return getDayOfWeek(false); }
    public String getDayOfWeek(boolean abbreviated) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(year, month - 1, day);

        String[] fullNames = {
                "Sunday", "Monday", "Tuesday", "Wednesday",
                "Thursday", "Friday", "Saturday"
        };

        String[] shortNames = {
                "Sun", "Mon", "Tue", "Wed",
                "Thu", "Fri", "Sat"
        };

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // Calendar.DAY_OF_WEEK is 1-based
        return abbreviated ? shortNames[dayOfWeek] : fullNames[dayOfWeek];
    }


    public void randomize() {
        this.month = nextMonth();
        this.day = nextDay(month);

        // Generate time components
        if(isCurrentDay()) {
            int currentHour = DateUtils.getCurrentHour();
            this.hour = nextHour(1, currentHour);

            if(this.hour == currentHour) {
                int currentMinute = DateUtils.getCurrentMinute();
                this.minute = nextMinute(0, currentMinute);

                if(this.minute == currentMinute) {
                    int currentSecond = DateUtils.getCurrentSecond();
                    this.second = nextSecond(0, currentSecond);

                    // If we're in the current second, restrict milliseconds
                    if(this.second == currentSecond) {
                        long currentMillis = System.currentTimeMillis() % 1000;
                        this.milliseconds = nextMillisecond(0, currentMillis);

                        // If we're in the current millisecond, restrict nanoseconds
                        if(this.milliseconds == currentMillis) {
                            long currentNanos = System.nanoTime() % 1000000;
                            this.nanoSeconds = nextSubMillisecond(0, currentNanos);
                        } else {
                            this.nanoSeconds = nextSubMillisecond();
                        }
                    } else {
                        this.milliseconds = nextMillisecond();
                        this.nanoSeconds = nextSubMillisecond();
                    }
                } else {
                    this.second = nextSecond();
                    this.milliseconds = nextMillisecond();
                    this.nanoSeconds = nextSubMillisecond();
                }
            } else {
                this.minute = nextMinute();
                this.second = nextSecond();
                this.milliseconds = nextMillisecond();
                this.nanoSeconds = nextSubMillisecond();
            }
        } else {
            this.hour = nextHour();
            this.minute = nextMinute();
            this.second = nextSecond();
            this.milliseconds = nextMillisecond();
            this.nanoSeconds = nextSubMillisecond();
        }
    }

    // Milliseconds generation (0-999)
    public static long nextMillisecond() { return nextMillisecond(0, 999); }
    public static long nextMillisecond(long maximumMillis) { return nextMillisecond(0, maximumMillis); }
    public static long nextMillisecond(long minimumMillis, long maximumMillis) {
        minimumMillis = Math.max(0, Math.min(minimumMillis, 999));
        maximumMillis = Math.max(0, Math.min(maximumMillis, 999));

        if (minimumMillis > maximumMillis) {
            long temp = minimumMillis;
            minimumMillis = maximumMillis;
            maximumMillis = temp;
        }

        return RandomGenerator.nextLong(minimumMillis, maximumMillis);
    }

    // Sub-millisecond nanoseconds (0-999999)
    public static long nextSubMillisecond() { return nextSubMillisecond(0, 999999); }
    public static long nextSubMillisecond(long maximumNano) { return nextSubMillisecond(0, maximumNano); }
    public static long nextSubMillisecond(long minimumNano, long maximumNano) {
        // For sub-millisecond precision, we only want 0-999999
        minimumNano = Math.max(0, Math.min(minimumNano, 999999));
        maximumNano = Math.max(0, Math.min(maximumNano, 999999));

        if (minimumNano > maximumNano) {
            long temp = minimumNano;
            minimumNano = maximumNano;
            maximumNano = temp;
        }

        // 50% chance of zero nanoseconds for cleaner timestamps
        if (RandomGenerator.nextBoolean()) {
            return 0L;
        }

        return RandomGenerator.nextLong(minimumNano, maximumNano);
    }

    public static int nextYear() { return nextYear(ANDROID_RELEASE_YEAR, DateUtils.getCurrentYear()); }
    public static int nextYear(int minimumYear) { return nextYear(minimumYear, DateUtils.getCurrentYear()); }
    public static int nextYear(int minimumYear, int maximumYear) { return RandomGenerator.nextInt(minimumYear, maximumYear); }

    public static int nextMonth() { return RandomGenerator.nextInt(1, 12); }
    public static int nextMonth(int minimumMonth, int maxMonth) { return RandomGenerator.nextInt(minimumMonth, maxMonth); }

    public static int nextDay() { return nextDay(nextMonth()); }
    public static int nextDay(int month) { return nextDay(month, 1, DateUtils.getMaxDaysInMonth(month)); }
    public static int nextDay(int month, int minimumDay) { return nextDay(month, minimumDay, DateUtils.getMaxDaysInMonth(month)); }
    public static int nextDay(int month, int minimumDay, int maximumDay) {
        month = Math.min(Math.max(month, 1), 12);
        int monthMaxDays = DateUtils.getMaxDaysInMonth(month);

        if (minimumDay > maximumDay) {
            int temp = minimumDay;
            minimumDay = maximumDay;
            maximumDay = temp;
        }

        minimumDay = Math.max(1, minimumDay);
        maximumDay = Math.min(maximumDay, monthMaxDays);

        return RandomGenerator.nextInt(minimumDay, maximumDay);
    }

    public static int nextHour() { return nextHour(1, 23); }  // Fixed to 23 instead of 24
    public static int nextHour(int maximumHour) { return nextHour(1, maximumHour); }
    public static int nextHour(int minimumHour, int maximumHour) {
        minimumHour = Math.max(1, Math.min(minimumHour, 23));
        maximumHour = Math.max(1, Math.min(maximumHour, 23));

        if (minimumHour > maximumHour) {
            int temp = minimumHour;
            minimumHour = maximumHour;
            maximumHour = temp;
        }

        return RandomGenerator.nextInt(minimumHour, maximumHour);
    }

    public static int nextMinute() { return nextMinute(0, 59); }
    public static int nextMinute(int maximumMinute) { return nextMinute(0, maximumMinute); }
    public static int nextMinute(int minimumMinute, int maximumMinute) {
        minimumMinute = Math.max(0, Math.min(minimumMinute, 59));
        maximumMinute = Math.max(0, Math.min(maximumMinute, 59));

        if (minimumMinute > maximumMinute) {
            int temp = minimumMinute;
            minimumMinute = maximumMinute;
            maximumMinute = temp;
        }

        return RandomGenerator.nextInt(minimumMinute, maximumMinute);
    }

    public static int nextSecond() { return nextSecond(0, 59); }
    public static int nextSecond(int maximumSecond) { return nextSecond(0, maximumSecond); }
    public static int nextSecond(int minimumSecond, int maximumSecond) {
        minimumSecond = Math.max(0, Math.min(minimumSecond, 59));
        maximumSecond = Math.max(0, Math.min(maximumSecond, 59));

        if (minimumSecond > maximumSecond) {
            int temp = minimumSecond;
            minimumSecond = maximumSecond;
            maximumSecond = temp;
        }

        return RandomGenerator.nextInt(minimumSecond, maximumSecond);
    }

    // Convert to STAT format timestamp string
    @SuppressLint("DefaultLocale")
    public String toTimestampString(String timezoneOffset) {
        return String.format("%04d-%02d-%02d %02d:%02d:%02d.%03d%06d %s",
                year, month, day, hour, minute, second,
                milliseconds,  // 3 digits (0-999)
                nanoSeconds,   // 6 digits (0-999999)
                timezoneOffset);
    }


    // Parse from kernel date format
    public static RandomDate fromKernelDate(String kernelDate) {
        try {
            // Remove the "SMP PREEMPT " prefix if present
            String dateStr = kernelDate.replaceFirst("SMP PREEMPT ", "").trim();

            SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.ENGLISH);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = formatter.parse(dateStr);
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTime(date);

            RandomDate randomDate = new RandomDate();
            randomDate.year = calendar.get(Calendar.YEAR);
            randomDate.month = calendar.get(Calendar.MONTH) + 1;
            randomDate.day = calendar.get(Calendar.DAY_OF_MONTH);
            randomDate.hour = calendar.get(Calendar.HOUR_OF_DAY);
            randomDate.minute = calendar.get(Calendar.MINUTE);
            randomDate.second = calendar.get(Calendar.SECOND);
            randomDate.milliseconds = calendar.get(Calendar.MILLISECOND);
            randomDate.nanoSeconds = 0;

            return randomDate;
        } catch (Exception e) {
            System.out.println("Error parsing kernel date: " + e.getMessage());
            return new RandomDate(DEFAULT_DATE);
        }
    }

    // Convert to StructTimespec
    /*public StructTimespec toStructTimespec() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(year, month - 1, day, hour, minute, second);
        long tv_sec = cal.getTimeInMillis() / 1000;

        // Combine milliseconds and nanoseconds into total nanoseconds
        long tv_nsec = (milliseconds * 1000000) + nanoSeconds;

        return new StructTimespec(tv_sec, tv_nsec);
    }*/

    // Safe parser that never throws
    public static RandomDate fromStatStamp(String statStamp) {
        // Handle null, empty, or invalid basic input
        if (statStamp == null || statStamp.trim().isEmpty() || statStamp.trim().equals("-")) {
            Log.e(TAG, "Invalid input format, returning default date");
            return new RandomDate(DEFAULT_DATE); // Return copy of default
        }

        RandomDate date = new RandomDate();
        String timestamp = statStamp.trim();

        try {
            // Extract main timestamp part by removing timezone if present
            String[] parts = timestamp.split("\\s+");
            String datePart = parts[0];
            String timePart = parts.length > 1 ? parts[1] : "00:00:00";

            // Handle various date formats
            String[] datePieces = datePart.split("[-/.]");
            if (datePieces.length >= 3) {
                date.year = parseIntSafely(datePieces[0], DEFAULT_DATE.year);
                date.month = parseIntSafely(datePieces[1], DEFAULT_DATE.month);
                date.day = parseIntSafely(datePieces[2], DEFAULT_DATE.day);
            } else {
                Log.e(TAG, "Invalid date format, using default date components");
                date.year = DEFAULT_DATE.year;
                date.month = DEFAULT_DATE.month;
                date.day = DEFAULT_DATE.day;
            }

            // Handle time part
            String[] timePieces = timePart.split(":");
            if (timePieces.length >= 3) {
                date.hour = parseIntSafely(timePieces[0], DEFAULT_DATE.hour);
                date.minute = parseIntSafely(timePieces[1], DEFAULT_DATE.minute);

                // Handle seconds and sub-seconds
                String[] secondParts = timePieces[2].split("\\.");
                date.second = parseIntSafely(secondParts[0], DEFAULT_DATE.second);

                if (secondParts.length > 1) {
                    String subseconds = secondParts[1];
                    // Pad or truncate to ensure proper length
                    subseconds = String.format("%-9s", subseconds).replace(' ', '0');

                    date.milliseconds = parseLongSafely(subseconds.substring(0, 3), DEFAULT_DATE.milliseconds);
                    date.nanoSeconds = parseLongSafely(subseconds.substring(3, 9), DEFAULT_DATE.nanoSeconds);
                } else {
                    date.milliseconds = DEFAULT_DATE.milliseconds;
                    date.nanoSeconds = DEFAULT_DATE.nanoSeconds;
                }
            } else {
                Log.e(TAG, "Invalid time format, using default time components");
                date.hour = DEFAULT_DATE.hour;
                date.minute = DEFAULT_DATE.minute;
                date.second = DEFAULT_DATE.second;
                date.milliseconds = DEFAULT_DATE.milliseconds;
                date.nanoSeconds = DEFAULT_DATE.nanoSeconds;
            }

            // Normalize all values to valid ranges
            normalizeDate(date);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing timestamp: " + e.getMessage() + ", returning default date");
            return new RandomDate(DEFAULT_DATE);
        }

        return date;
    }

    // Helper method to normalize date values to valid ranges
    private static void normalizeDate(RandomDate date) {
        date.year = Math.max(MIN_EPOC_YEAR, date.year);
        date.month = Math.min(Math.max(1, date.month), 12);
        date.day = Math.min(Math.max(1, date.day), DateUtils.getMaxDaysInMonth(date.month));
        date.hour = Math.min(Math.max(0, date.hour), 23);
        date.minute = Math.min(Math.max(0, date.minute), 59);
        date.second = Math.min(Math.max(0, date.second), 59);
        date.milliseconds = Math.min(Math.max(0, date.milliseconds), 999);
        date.nanoSeconds = Math.min(Math.max(0, date.nanoSeconds), 999999);
    }

    // Safe integer parser
    private static int parseIntSafely(String value, int defaultValue) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse integer value: " + value + ", using default: " + defaultValue);
            return defaultValue;
        }
    }

    // Safe long parser
    private static long parseLongSafely(String value, long defaultValue) {
        try {
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            Log.e(TAG,"Failed to parse long value: " + value + ", using default: " + defaultValue);
            return defaultValue;
        }
    }

    public static RandomDate fromSeconds(long seconds) {
        return fromMilliseconds(seconds * 1000L);
    }

    // Create RandomDate from milliseconds timestamp
    public static RandomDate fromMilliseconds(long milliseconds) {
        RandomDate date = new RandomDate();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(milliseconds);

        date.year = calendar.get(Calendar.YEAR);
        date.month = calendar.get(Calendar.MONTH) + 1;
        date.day = calendar.get(Calendar.DAY_OF_MONTH);
        date.hour = calendar.get(Calendar.HOUR_OF_DAY);
        date.minute = calendar.get(Calendar.MINUTE);
        date.second = calendar.get(Calendar.SECOND);
        date.milliseconds = calendar.get(Calendar.MILLISECOND);
        date.nanoSeconds = 0; // Millisecond timestamp doesn't have nano precision

        return date;
    }

    // Create RandomDate from separate seconds and nanoseconds (timespec format)
    public static RandomDate fromTimespec(long seconds, long nanoSeconds) {
        // First convert seconds to milliseconds and create base date
        RandomDate date = fromMilliseconds(seconds * 1000L);

        // Then handle the nanoseconds portion
        date.milliseconds = (int)(nanoSeconds / 1000000L); // First 3 digits become milliseconds
        date.nanoSeconds = nanoSeconds % 1000000L;         // Remaining 6 digits are nanoseconds

        normalizeDate(date); // Ensure all values are in valid ranges
        return date;
    }

    // Create RandomDate from both milliseconds and additional nanoseconds
    public static RandomDate fromMillisecondsAndNanos(long milliseconds, long nanoSeconds) {
        RandomDate date = fromMilliseconds(milliseconds);
        date.nanoSeconds = nanoSeconds % 1000000L; // Only take the sub-millisecond portion
        normalizeDate(date);
        return date;
    }

    // Parse from Android build date formats
    public static RandomDate fromDate(String buildDate) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(ANDROID_BUILD_FORMAT, Locale.ENGLISH);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = formatter.parse(buildDate);
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTime(date);

            RandomDate randomDate = new RandomDate();
            randomDate.year = calendar.get(Calendar.YEAR);
            randomDate.month = calendar.get(Calendar.MONTH) + 1;
            randomDate.day = calendar.get(Calendar.DAY_OF_MONTH);
            randomDate.hour = calendar.get(Calendar.HOUR_OF_DAY);
            randomDate.minute = calendar.get(Calendar.MINUTE);
            randomDate.second = calendar.get(Calendar.SECOND);
            randomDate.milliseconds = calendar.get(Calendar.MILLISECOND);
            randomDate.nanoSeconds = 0; // Standard format doesn't include nanos

            return randomDate;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing Android build date: " + e.getMessage());
            return new RandomDate(DEFAULT_DATE);
        }
    }

    public static RandomDate fromDateOne(String buildDate) {
        try {
            String[] parts = buildDate.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid format");
            }

            RandomDate date = new RandomDate();
            date.year = Integer.parseInt(parts[0]);
            date.month = Integer.parseInt(parts[1]);
            date.day = Integer.parseInt(parts[2]);
            date.hour = 0;
            date.minute = 0;
            date.second = 0;
            date.milliseconds = 0;
            date.nanoSeconds = 0;

            normalizeDate(date);
            return date;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing Android build date one: " + e.getMessage());
            return new RandomDate(DEFAULT_DATE);
        }
    }

    public static RandomDate fromDateTwo(String buildDate) {
        try {
            if (buildDate.length() != 8) {
                throw new IllegalArgumentException("Invalid format");
            }

            RandomDate date = new RandomDate();
            date.year = Integer.parseInt(buildDate.substring(0, 4));
            date.month = Integer.parseInt(buildDate.substring(4, 6));
            date.day = Integer.parseInt(buildDate.substring(6, 8));
            date.hour = 0;
            date.minute = 0;
            date.second = 0;
            date.milliseconds = 0;
            date.nanoSeconds = 0;

            normalizeDate(date);
            return date;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing Android build date two: " + e.getMessage());
            return new RandomDate(DEFAULT_DATE);
        }
    }

    // Parse from ISO date format (YYYY-MM-DD)
    public static RandomDate fromIsoDate(String isoDate) {
        try {
            String[] parts = isoDate.split("-");
            if (parts.length != 3) {
                Log.e(TAG, "Invalid ISO date format, using default date");
                return new RandomDate(DEFAULT_DATE);
            }

            RandomDate date = new RandomDate();
            date.year = Integer.parseInt(parts[0]);
            date.month = Integer.parseInt(parts[1]);
            date.day = Integer.parseInt(parts[2]);
            date.hour = 0;
            date.minute = 0;
            date.second = 0;
            date.milliseconds = 0;
            date.nanoSeconds = 0;

            normalizeDate(date);
            return date;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing ISO date: " + e.getMessage());
            return new RandomDate(DEFAULT_DATE);
        }
    }

    // Helper to validate date components
    private static boolean isValidDate(RandomDate date) {
        return date.year >= MIN_EPOC_YEAR &&
                date.month >= 1 && date.month <= 12 &&
                date.day >= 1 && date.day <= DateUtils.getMaxDaysInMonth(date.month) &&
                date.hour >= 0 && date.hour <= 23 &&
                date.minute >= 0 && date.minute <= 59 &&
                date.second >= 0 && date.second <= 59 &&
                date.milliseconds >= 0 && date.milliseconds <= 999 &&
                date.nanoSeconds >= 0 && date.nanoSeconds <= 999999;
    }
}
