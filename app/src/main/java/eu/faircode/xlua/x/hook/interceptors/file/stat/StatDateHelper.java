package eu.faircode.xlua.x.hook.interceptors.file.stat;

import android.util.Log;



import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.hook.interceptors.zone.RandomDateHelper;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class StatDateHelper {
    private static final String TAG = "XLua.StatDateHelper";
    public static final long BACKUP_TIME = StatUtils.parseTimestamp("2016-01-01 18:00:00.000000000")[0];

    public static String generateFakeCreation(
            String originalValue,
            long lastModifiedSeconds,
            long lastAccessSeconds,
            long romCreatedSeconds,
            String offset) {
        long maxSeconds = Math.min(lastModifiedSeconds, lastAccessSeconds);
        long maxYear = RandomDateHelper.CURRENT_YEAR;
        long maxMonth = RandomDateHelper.CURRENT_MONTH;
        long maxDay = RandomDateHelper.CURRENT_DAY;
        if(maxSeconds != 0) {
            long[] times =  StatUtils.parseDateFromSeconds(maxSeconds);
            maxYear = times[0];
            maxMonth = times[1];
            maxDay = times[2];
        }

        long minSeconds = romCreatedSeconds > 0 ? romCreatedSeconds : BACKUP_TIME;
        return generateDate(originalValue, minSeconds, (int)maxYear, (int)maxMonth, (int)maxDay, offset);
    }


    public static String generateFakeDateModify(
            String originalValue,
            long lastAccessTimeSeconds,
            long createdSeconds,
            long romCreatedSeconds,
            String offset) {
        long maxYear = RandomDateHelper.CURRENT_YEAR;
        long maxMonth = RandomDateHelper.CURRENT_MONTH;
        long maxDay = RandomDateHelper.CURRENT_DAY;
        long min = 0;

        if(lastAccessTimeSeconds > 0) {
            long[] times =  StatUtils.parseDateFromSeconds(lastAccessTimeSeconds);
            maxYear = times[0];
            maxMonth = times[1];
            maxDay = times[2];
        }

        if(createdSeconds > 0) min = createdSeconds;
        else if(romCreatedSeconds > 0) min = romCreatedSeconds;
        else min = BACKUP_TIME;

        return generateDate(originalValue, min, (int)maxYear, (int)maxMonth, (int)maxDay, offset);
    }

    public static String generateFakeDateAccess(
            String originalValue,
            long lastModifiedSeconds,
            long createdSeconds,
            long romCreatedSeconds,
            String offset) {
        //Priority
        //
        //Modify Date
        //Created Date
        //Rom Year
        //2016
        //
        //1969-12-31 18:00:00.000000000
        //2024-10-04 11:52:56.023000000
        //2016-10-04 11:52:56.23
        long min = 0;
        if(lastModifiedSeconds > 0) min = lastModifiedSeconds;
        else if(createdSeconds > 0) min = createdSeconds;
        else if(romCreatedSeconds > 0) min = romCreatedSeconds;
        else min = BACKUP_TIME;
        
        return generateDate(originalValue, min, RandomDateHelper.CURRENT_YEAR, RandomDateHelper.CURRENT_MONTH, RandomDateHelper.CURRENT_DAY, offset);
    }

    public static String generateDate(
            String originalValue,
            long minTotalSeconds,
            int maxYear,
            int maxMonth,
            int maxDay,
            String offset) {
        StringBuilder date = new StringBuilder();
        long[] parts = StatUtils.parseDateFromSeconds(minTotalSeconds);

        int minYear = Math.min((int)parts[0], maxYear);
        maxYear = Math.max((int)parts[0], maxYear);
        if(DebugUtil.isDebug()) {
            Log.d(TAG, StrBuilder.create()
                    .ensureOneNewLinePer(true)
                    .appendFieldLine("Original Value", originalValue)
                    .appendFieldLine("Min Total Seconds", minTotalSeconds)
                    .appendFieldLine("Max Year", maxYear)
                    .appendFieldLine("Max Month", maxMonth)
                    .appendFieldLine("Max Day", maxDay)
                    .appendFieldLine("Max Year Math Max", maxYear)
                    .appendFieldLine("Min Year", parts[0])
                    .appendFieldLine("Min Month", parts[1])
                    .appendFieldLine("Min Day", parts[2])
                    .appendFieldLine("Min Year Math Min", minYear)
                    //.appendFieldLine("Fake Year", year)
                    .toString(true));
        }

        long year = maxYear == minYear ? maxYear : RandomGenerator.nextLong(minYear, maxYear);
        date.append(String.valueOf(year));
        date.append("-");
        if(year == maxYear) {
            int minMonth = Math.min((int)parts[1], maxMonth);
            maxMonth = Math.max((int)parts[1], maxMonth);
            long month = minMonth == maxMonth ? maxMonth : RandomGenerator.nextLong(minMonth, maxMonth);
            date.append(StatUtils.ensureFormatedNumber(month));
            date.append("-");
            if(month == maxMonth) {
                date.append(StatUtils.ensureFormatedNumber(RandomDateHelper.generateRandomDayCurrent((int)month, (int)year, maxDay)));
            } else {
                date.append(StatUtils.ensureFormatedNumber(RandomDateHelper.generateRandomDay((int)month, (int)year)));
            }
        }
        else {
            int month = RandomDateHelper.generateRandomMonth();
            date.append(StatUtils.ensureFormatedNumber(month));
            date.append("-");
            date.append(StatUtils.ensureFormatedNumber(RandomDateHelper.generateRandomDay(month, (int)year)));
        }

        if(originalValue.contains(" ")) {
            date.append(" ");
            String[] splt = originalValue.split(" ");
            String lowHalf = splt[1];
            if(lowHalf.contains(":")) {
                String[] lowHalfParts = lowHalf.split(":");
                StringBuilder lowBuild = new StringBuilder();
                int sz = lowHalfParts.length - 1;
                for(int j = 0; j < lowHalfParts.length; j++) {
                    String part = lowHalfParts[j];
                    if(part.contains(".")) {
                        String[] decimalParts = part.split("\\.");
                        if(decimalParts.length > 1) {
                            //.xxxxxxxxx
                            String decimalEnd = decimalParts[1];
                            lowBuild.append(RandomDateHelper.generateRandomSeconds());
                            lowBuild.append(".");                   //56.x
                            if(decimalEnd.length() > 2) {
                                //boolean allZeros = ThreadLocalRandom.current().nextBoolean();
                                //lowBuild.append(allZeros ? "000000000" : RandomDateHelper.generateRandomNanoseconds());
                                lowBuild.append(RandomDateHelper.generateRandomNanoseconds());
                            } else {
                                lowBuild.append(RandomDateHelper.generateRandomHundredths());
                            }
                        } else {
                            lowBuild.append(RandomDateHelper.generateRandomSeconds());
                        }
                    } else {
                        switch(j) {
                            case 0:
                                lowBuild.append(RandomDateHelper.generateRandomHours());
                                break;
                            case 1:
                                lowBuild.append(RandomDateHelper.generateRandomMinutes());
                                break;
                            default:
                                lowBuild.append(generateNumber(10, 18));
                                break;
                        }
                    }
                    if(j != sz)
                        lowBuild.append(":");
                }

                date.append(lowBuild);
            } else {
                date.append(RandomDateHelper.generateRandomHours());
            }

            if(splt.length == 3 && offset != null) {
                date.append(" ");
                date.append(offset);
            }
        }

        return date.toString();
    }

    private static String generateNumber(int low, int high) {
        int num = RandomGenerator.nextInt(low, high);
        if(num <= 9) {
            return "0" + String.valueOf(num);
        } else {
            return String.valueOf(num);
        }
    }
}
