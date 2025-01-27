package eu.faircode.xlua.x.data.utils;

import java.util.Calendar;
import java.util.TimeZone;

public class DateUtils {
    public static final int CURRENT_YEAR = getCurrentYear();
    public static final int CURRENT_MONTH = getCurrentMonth();
    public static final int CURRENT_DAY = getCurrentDay();


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

    public static int getCurrentHour() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static int getCurrentMinute() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return calendar.get(Calendar.MINUTE);
    }

    public static int getCurrentSecond() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return calendar.get(Calendar.SECOND);
    }

    public static int getMaxDaysInMonth(int month) {
        switch (month) {
            case 4: case 6: case 9: case 11:
                return 30;
            case 2:
                return 29; // Assuming we want to support leap years
            default:
                return 31;
        }
    }
}
