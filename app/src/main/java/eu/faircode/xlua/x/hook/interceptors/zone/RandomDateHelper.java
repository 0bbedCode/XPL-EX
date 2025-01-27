package eu.faircode.xlua.x.hook.interceptors.zone;

import android.annotation.SuppressLint;

import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;


import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomDateHelper {
    public static final int CURRENT_YEAR = getCurrentYear();
    public static final int CURRENT_MONTH = getCurrentMonth();
    public static final int CURRENT_DAY = getCurrentDay();


    private static final long MILLIS_PER_SECOND = 1000L;
    private static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
    private static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;

    public static long[] generateEpochTimeStamps(int amountOfTimeStamps, boolean leastToGreatest) {
        long[] times = new long[amountOfTimeStamps];
        for(int i = 0; i < times.length; i++) {
            long t = generateHoursInMilliseconds(2, 48);
            t += generateMinutesInMilliseconds(5, 500);
            t += generateSecondsInMilliseconds(5, 2000);
            t += RandomGenerator.nextLong(500, 2000);   //Low Half
            times[i] = t;
        }

        if(leastToGreatest) Arrays.sort(times);//Smallest to Biggest
        return times;
    }

    public static int getCurrentYear() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return calendar.get(Calendar.YEAR);
    }

    public static int getCurrentMonth() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return calendar.get(Calendar.MONTH);
    }

    public static int getCurrentDay() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    @SuppressLint("DefaultLocale")
    public static String generateRandomTimeZoneOffset() {
        // Generate a random offset between -12 and +14 hours
        int offsetHours = RandomGenerator.nextInt(-12, 15);
        // Generate random minutes (0, 15, 30, 45)
        int offsetMinutes = RandomGenerator.nextInt(4) * 15;
        // Determine the sign
        String sign = offsetHours < 0 ? "-" : "+";
        // Ensure we use the absolute value for formatting
        offsetHours = Math.abs(offsetHours);
        return String.format("%s%02d%02d", sign, offsetHours, offsetMinutes);
    }

    @SuppressLint("DefaultLocale")
    public static String generateRandomNanoseconds() {
        // Generate a random number between 0 and 999999999
        int nanos = RandomGenerator.nextInt(1_000_000_000);
        // Format the number to always have 9 digits
        return String.format("%09d", nanos);
    }

    @SuppressLint("DefaultLocale")
    public static String generateRandomHours() {
        int hours = RandomGenerator.nextInt(24); // 0 to 23
        return String.format("%02d", hours);
    }

    @SuppressLint("DefaultLocale")
    public static String generateRandomMinutes() {
        int minutes = RandomGenerator.nextInt(60); // 0 to 59
        return String.format("%02d", minutes);
    }

    @SuppressLint("DefaultLocale")
    public static String generateRandomSeconds() {
        int seconds = RandomGenerator.nextInt(60); // 0 to 59
        return String.format("%02d", seconds);
    }

    @SuppressLint("DefaultLocale")
    public static String generateRandomHundredths() {
        int hundredths = RandomGenerator.nextInt(100); // 0 to 99
        return String.format("%02d", hundredths);
    }

    public static int generateRandomMonth() {
        return RandomGenerator.nextInt(1, 13); // 1 to 12
    }

    public static int generateRandomDay(int month, int year) {
        int maxDay;
        switch (month) {
            case 2: // February
                maxDay = isLeapYear(year) ? 29 : 28;
                break;
            case 4: case 6: case 9: case 11: // April, June, September, November
                maxDay = 30;
                break;
            default:
                maxDay = 31;
        }
        return RandomGenerator.nextInt(1, maxDay + 1); // 1 to maxDay
    }

    public static int generateRandomDayCurrent(int month, int year, int maxDayInput) {
        int maxDay;
        switch (month) {
            case 2: // February
                maxDay = isLeapYear(year) ? 29 : 28;
                break;
            case 4: case 6: case 9: case 11: // April, June, September, November
                maxDay = 30;
                break;
            default:
                maxDay = 31;
        }
        maxDay = Math.min(maxDayInput, maxDay);
        return RandomGenerator.nextInt(1, maxDay + 1); // 1 to maxDay
    }

    public static boolean isLeapYear(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }

    public static int generateRandomYear(int startYear, int endYear) {
        return RandomGenerator.nextInt(startYear, endYear + 1);
    }

    @SuppressLint("DefaultLocale")
    public static String formatAsTwoDigits(int number) {
        return String.format("%02d", number);
    }

    public static long generateSecondsInMilliseconds(int origin, int bound) {
        int randomSeconds = RandomGenerator.nextInt(origin, bound);
        return randomSeconds * MILLIS_PER_SECOND;
    }

    public static long generateMinutesInMilliseconds(int origin, int bound) {
        int randomMinutes = RandomGenerator.nextInt(origin, bound);
        return randomMinutes * MILLIS_PER_MINUTE;
    }

    public static long generateHoursInMilliseconds(int origin, int bound) {
        int randomHours = RandomGenerator.nextInt(origin, bound);
        return randomHours * MILLIS_PER_HOUR;
    }

    //For Current Time up we can just inline java hook ??
    //Very specific special hook
    public static long generateLastModified() {

        // Epoch time (January 1, 1970)
        final long EPOCH = 0L;
        // Current time in milliseconds
        final long NOW = System.currentTimeMillis();
        // Probability of returning 0L (to simulate file not existing or I/O error)
        final double PROBABILITY_OF_ZERO = 0.05; // 5% chance

        // Small chance to return 0L
        if (RandomGenerator.nextDouble() < PROBABILITY_OF_ZERO) {
            return 0L;
        }

        // Generate a random timestamp between epoch and now
        return RandomGenerator.nextLong(EPOCH, NOW + 1);
    }
}
