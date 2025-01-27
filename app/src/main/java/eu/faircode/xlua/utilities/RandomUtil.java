package eu.faircode.xlua.utilities;

import java.text.SimpleDateFormat;
import java.util.Date;

import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomUtil {
    // Array containing abbreviated versions of time zones
    public static final String[] TIMEZONE_AB = {
            "GMT", "UTC", "EST", "EDT", "CST", "CDT", "MST", "MDT",
            "PST", "PDT", "AKST", "AKDT", "HST", "HAST", "HADT",
            "SST", "SDT", "AST", "ADT", "NST", "NDT", "IST", "JST",
            "KST", "CEST", "CET", "EET", "EEST", "AEST", "AEDT",
            "ACST", "ACDT", "AWST", "WET", "WEST", "CAT", "SAST",
            "HKT", "NZST", "NZDT", "MSK", "IDT", "IRST", "IRDT", "GST", "EAT", "WAT", "WAST"
    };

    // Array containing full names of time zones
    public static final String[] TIMEZONE_FULL = {
            "Greenwich Mean Time", "Coordinated Universal Time", "Eastern Standard Time",
            "Eastern Daylight Time", "Central Standard Time", "Central Daylight Time",
            "Mountain Standard Time", "Mountain Daylight Time", "Pacific Standard Time",
            "Pacific Daylight Time", "Alaska Standard Time", "Alaska Daylight Time",
            "Hawaii Standard Time", "Hawaii-Aleutian Standard Time", "Hawaii-Aleutian Daylight Time",
            "Samoa Standard Time", "Samoa Daylight Time", "Atlantic Standard Time",
            "Atlantic Daylight Time", "Newfoundland Standard Time", "Newfoundland Daylight Time",
            "Indian Standard Time", "Japan Standard Time", "Korea Standard Time",
            "Central European Summer Time", "Central European Time", "Eastern European Time",
            "Eastern European Summer Time", "Australian Eastern Standard Time",
            "Australian Eastern Daylight Time", "Australian Central Standard Time",
            "Australian Central Daylight Time", "Australian Western Standard Time",
            "Western European Time", "Western European Summer Time", "Central Africa Time",
            "South Africa Standard Time", "Hong Kong Time", "New Zealand Standard Time",
            "New Zealand Daylight Time", "Moscow Standard Time", "Israel Daylight Time",
            "Iran Standard Time", "Iran Daylight Time", "Gulf Standard Time",
            "East Africa Time", "West Africa Time", "West Africa Summer Time"
    };

    public static final String[] WEEK_DAYS_AB = new String[] { "Mon", "Tue", "Wed", "Thur", "Fri", "Sat", "Sun" };
    public static final String[] WEEK_DAYS_FULL = new String[] { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

    public static final String[] MONTHS_AB = new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
    public static final String[] MONTHS_FULL = new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };

    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String DD_MM_YYYY__HHMMSS = "dd-MM-yyyy HH:mm:ss";

    public static String getTimeZone() { return TIMEZONE_FULL[RandomGenerator.nextInt(0, TIMEZONE_FULL.length)]; }
    public static String getTimeZoneAbbreviated() { return TIMEZONE_AB[RandomGenerator.nextInt(0, TIMEZONE_AB.length)];  }

    public static String getWeekDayAbbreviated() { return WEEK_DAYS_AB[RandomGenerator.nextInt(0, WEEK_DAYS_AB.length)]; }
    public static String getWeekDay() { return WEEK_DAYS_FULL[RandomGenerator.nextInt(0, WEEK_DAYS_FULL.length)]; }

    public static int getWeekDayNumber() { return getInt(1, WEEK_DAYS_AB.length + 1); }
    public static String getWeekDayNumberFormatted() { return getIntEnsureFormat(1, WEEK_DAYS_AB.length + 1); }

    public static String getMonthAbbreviated() { return MONTHS_AB[RandomGenerator.nextInt(0, MONTHS_AB.length)]; }
    public static String getMonth() { return MONTHS_FULL[RandomGenerator.nextInt(0, MONTHS_FULL.length)]; }

    public static int getMonthNumber() { return getInt(1, MONTHS_AB.length + 1); }
    public static String getMonthNumberFormatted() { return getIntEnsureFormat(1, MONTHS_AB.length + 1); }

    public static int getInt(int originStart, int boundEndBefore) { return RandomGenerator.nextInt(originStart, boundEndBefore); }

    public static String getIntEnsureFormat(int originStart, int bound) {
        int random = RandomGenerator.nextInt(originStart, bound);
        String startStr = String.valueOf(originStart);
        String boundStr = String.valueOf(bound);
        String rStr = String.valueOf(random);
        if(rStr.length() == boundStr.length())
            return rStr;

        if(startStr.length() == boundStr.length() || startStr.length() > boundStr.length() || originStart > bound)
            return rStr;

        int neededZeros = boundStr.length() - rStr.length();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < neededZeros; i++)
            sb.append("0");

        sb.append(rStr);
        return sb.toString();
    }

    public static long convertStringDateToEpoch(String dateString, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date date = sdf.parse(dateString);
            return date.getTime() / 1000;
        }catch (Exception e) {
            XLog.e("Failed to Convert String Date to Epoch: str=" + dateString + " format=" + format, e, true);
            return 1713580253;
        }
    }

    // Array of phone models
    public static final String[] PHONE_MODELS = {
            "Google Nexus One", "Google Nexus S", "Google Galaxy Nexus", "Google Nexus 4",
            "Google Nexus 5", "Google Nexus 6", "Google Nexus 5X", "Google Nexus 6P",
            "Google Pixel", "Google Pixel XL", "Google Pixel 2", "Google Pixel 2 XL",
            "Google Pixel 3", "Google Pixel 3 XL", "Google Pixel 3a", "Google Pixel 3a XL",
            "Google Pixel 4", "Google Pixel 4 XL", "Google Pixel 4a", "Google Pixel 5",
            "OnePlus One", "OnePlus 2", "OnePlus 3", "OnePlus 5", "OnePlus 6", "OnePlus 7 Pro",
            "Samsung Galaxy S4", "HTC One M8", "Motorola Moto X", "Xiaomi Mi 3",
            "Samsung Galaxy Note 20 Ultra", "OnePlus 7T", "OnePlus 8 Pro", "Xiaomi Mi 5",
            "Xiaomi Redmi Note 7", "LG G4", "LG V20", "HTC One M9", "Motorola Moto G7",
            "Sony Xperia Z3", "Asus Zenfone 6 (2019)", "Realme 3 Pro", "Samsung Galaxy S10",
            "Sony Xperia XZ Premium", "Huawei P30 Pro", "Motorola Droid Turbo", "OnePlus Nord",
            "HTC 10", "Samsung Galaxy S7 Edge", "Sony Xperia X", "Xiaomi Mi 9", "Huawei Mate 20 Pro",
            "Realme X2 Pro", "Samsung Galaxy Z Flip", "Motorola Moto Z", "LG G7 ThinQ",
            "Nokia 8.1", "Oppo Find X", "Asus ROG Phone 2", "Samsung Galaxy A50"
    };

    // Array of codenames
    public static final String[] PHONE_CODENAMES = {
            "Passion", "Soju", "Maguro", "Mako", "Hammerhead", "Shamu", "Bullhead", "Angler",
            "Sailfish", "Marlin", "Walleye", "Taimen", "Blueline", "Crosshatch", "Sargo", "Bonito",
            "Flame", "Coral", "Sunfish", "Redfin", "Bacon", "Oneplus2", "Oneplus3", "Cheeseburger",
            "Enchilada", "Guacamole", "Jflte", "M8", "Ghost", "Cancro", "Canvas", "Hotdog", "Instantnoodlep",
            "Gemini", "Lavender", "P1", "Elsa", "Hima", "River", "Leo", "I01WD", "RMX1851", "Beyond1",
            "Maple", "VOGUE", "Quark", "Avicii", "Perfume", "Hero2lte", "Suzuran", "Cepheus", "Lay-L29",
            "RMX1931", "Bloom", "Griffin", "Judy", "Phoenix", "PAHM00", "I001D", "A50"
    };

    public static String getPhoneCodeName() { return PHONE_CODENAMES[RandomGenerator.nextInt(0, PHONE_CODENAMES.length)]; }
    public static String getPhoneModel() { return PHONE_MODELS[RandomGenerator.nextInt(0, PHONE_MODELS.length)]; }

    // Array of Android version code names
    /*public static final String[] androidCodeNames = {
            "No codename", "No codename", "Cupcake", "Donut",
            "Eclair", "Eclair", "Eclair", "Froyo",
            "Gingerbread", "Gingerbread", "Honeycomb", "Honeycomb",
            "Honeycomb", "Ice Cream Sandwich", "Ice Cream Sandwich", "Jelly Bean",
            "Jelly Bean", "Jelly Bean", "KitKat", "Lollipop",
            "Lollipop", "Marshmallow", "Nougat", "Nougat",
            "Oreo", "Oreo", "Pie", "Android 10",
            "Android 11", "Android 12", "Android 13", "Android 14"
    };

    // Array of Android version numbers
    String[] androidVersionNumbers = {
            "1.0", "1.1", "1.5", "1.6",
            "2.0", "2.0.1", "2.1", "2.2",
            "2.3", "2.3.3", "3.0", "3.1",
            "3.2", "4.0", "4.0.3", "4.1",
            "4.2", "4.3", "4.4", "5.0",
            "5.1", "6.0", "7.0", "7.1",
            "8.0", "8.1", "9.0", "10",
            "11", "12", "13", "14"
    };*/



}
