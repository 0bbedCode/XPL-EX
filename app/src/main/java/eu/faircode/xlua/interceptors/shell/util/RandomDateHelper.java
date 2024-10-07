package eu.faircode.xlua.interceptors.shell.util;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

public class RandomDateHelper {
    public static int getCurrentYear() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return calendar.get(Calendar.YEAR);
    }

    public static String generateRandomTimeZoneOffset() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        // Generate a random offset between -12 and +14 hours
        int offsetHours = rand.nextInt(-12, 15);
        // Generate random minutes (0, 15, 30, 45)
        int offsetMinutes = rand.nextInt(4) * 15;
        // Determine the sign
        String sign = offsetHours < 0 ? "-" : "+";
        // Ensure we use the absolute value for formatting
        offsetHours = Math.abs(offsetHours);
        return String.format("%s%02d%02d", sign, offsetHours, offsetMinutes);
    }

    public static String generateRandomNanoseconds() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        // Generate a random number between 0 and 999999999
        int nanos = rand.nextInt(1_000_000_000);
        // Format the number to always have 9 digits
        return String.format("%09d", nanos);
    }

    public static String generateRandomHours() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int hours = rand.nextInt(24); // 0 to 23
        return String.format("%02d", hours);
    }

    public static String generateRandomMinutes() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int minutes = rand.nextInt(60); // 0 to 59
        return String.format("%02d", minutes);
    }

    public static String generateRandomSeconds() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int seconds = rand.nextInt(60); // 0 to 59
        return String.format("%02d", seconds);
    }

    public static String generateRandomHundredths() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int hundredths = rand.nextInt(100); // 0 to 99
        return String.format("%02d", hundredths);
    }

    public static int generateRandomMonth() {
        return ThreadLocalRandom.current().nextInt(1, 13); // 1 to 12
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
        return ThreadLocalRandom.current().nextInt(1, maxDay + 1); // 1 to maxDay
    }

    public static boolean isLeapYear(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }

    public static int generateRandomYear(int startYear, int endYear) {
        return ThreadLocalRandom.current().nextInt(startYear, endYear + 1);
    }

    public static String formatAsTwoDigits(int number) {
        return String.format("%02d", number);
    }

    public static long generateLastModified() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // Epoch time (January 1, 1970)
        final long EPOCH = 0L;
        // Current time in milliseconds
        final long NOW = System.currentTimeMillis();
        // Probability of returning 0L (to simulate file not existing or I/O error)
        final double PROBABILITY_OF_ZERO = 0.05; // 5% chance

        // Small chance to return 0L
        if (random.nextDouble() < PROBABILITY_OF_ZERO) {
            return 0L;
        }

        // Generate a random timestamp between epoch and now
        return random.nextLong(EPOCH, NOW + 1);
    }
}
