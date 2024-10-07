package eu.faircode.xlua.interceptors.shell.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import eu.faircode.xlua.BuildConfig;
import eu.faircode.xlua.utilities.StreamUtil;

public class CommandOutputHelper {
    private static final String TAG = "XLua.CommandOutputHelper";

    public static String readProcessOutput(Process process) {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }catch (Exception ignored) { }
        finally { StreamUtil.close(reader); }
        return sb.toString();
    }

    public static class FieldValuePointer {
        public boolean lastWasTime = false;
        public boolean lastWasDevice = false;
        public boolean lastWasInode = false;

        private boolean mExpectingValue = false;
        public String field = null;

        public String timeOffset;
        private final Map<String, Integer> mDates = new HashMap<>();
        private StringBuilder mValueBuilder = new StringBuilder();

        public boolean valueIsEmpty() { return mValueBuilder == null || mValueBuilder.length() == 0; }

        public boolean expectingValue() {  return mExpectingValue; }
        public String getValue(boolean resetValue, boolean resetAll) {
            String val = valueIsEmpty() ? "" : mValueBuilder.toString();
            if(resetValue) mValueBuilder = new StringBuilder();
            if(resetAll) reset();
            return val;
        }

        public boolean partOfValue(char c) {
            boolean appended = false;
            if(lastWasTime) {
                //Accepted Time Stamp Value Chars
                appended = !(c == '-' && mValueBuilder.length() == 0) && (c == ' ' || c == '-' || c == ':' || c == '+' || c == '.' || Character.isDigit(c));
            }
            if(lastWasDevice) {
                //Accepted Device Value Chars
                appended = c == '/' || Character.isDigit(c) || Character.isAlphabetic(c);
            }
            //Accepted Generic Value Chars
            if(!appended) appended = mExpectingValue && Character.isDigit(c);
            if(appended) mValueBuilder.append(c);
            return appended;
        }

        public FieldValuePointer() {
            timeOffset = RandomDateHelper.generateRandomTimeZoneOffset();
            //int androidReleaseYear = 2008; // First Android release
            //2008-12-31 09:00:00.000000000
            //1969-12-31 18:00:00.000000000 (default)
            ThreadLocalRandom rand = ThreadLocalRandom.current();
            int currentYear = RandomDateHelper.getCurrentYear();
            int birthYear = rand.nextInt(2009, currentYear + 1);
            int modifyYear = rand.nextInt(birthYear, currentYear + 1);
            mDates.put("birth", birthYear);
            mDates.put("create", birthYear);
            mDates.put("modify", modifyYear);
            mDates.put("change", rand.nextInt(modifyYear, currentYear + 1));
            mDates.put("access", rand.nextInt(modifyYear, currentYear + 1));
        }

        public int getYearForField() {
            return field == null ?
                    ThreadLocalRandom.current().nextInt(1969, RandomDateHelper.getCurrentYear() + 1) :  mDates.get(field.toLowerCase());
        }

        public void ensureField(String fieldName) {
            if(fieldName != null && fieldName.length() > 3) {
                String fld = fieldName.trim().toLowerCase();
                fld = fld.endsWith(":") && fld.length() > 3 ? fld.substring(0, fld.length() - 1) : fld;
                switch(fld) {
                    case "inode":
                        lastWasInode = true;
                        mExpectingValue = true;
                        field = "Inode";
                        break;
                    case "device":
                        lastWasDevice = true;
                        mExpectingValue = true;
                        field = "Device";
                        break;
                    case "access":
                    case "modify":
                    case "change":
                    case "birth":
                    case "create":
                        lastWasTime = true;
                        mExpectingValue = true;
                        field = fld.substring(0, 1).toUpperCase() + fld.substring(1);
                        break;
                    default:
                        reset();
                        break;
                }
            }
        }

        public void reset() {
            lastWasTime = false;
            lastWasDevice = false;
            mExpectingValue = false;
            lastWasInode = false;
            field = null;
            mValueBuilder = new StringBuilder();
        }
    }

    public static String randomizeStatOutput(String input) {
        if(BuildConfig.DEBUG) {
            Log.w(TAG, input);
        }

        StringBuilder currentChunk = new StringBuilder();
        StringBuilder full = new StringBuilder();

        int lastIndex = input.length() - 1;
        FieldValuePointer ptr = new FieldValuePointer();
        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if(ptr.expectingValue()) {
                boolean added = ptr.partOfValue(c);
                if(!added || i == lastIndex) {
                    if(!ptr.valueIsEmpty()) {
                        //System.out.println("[1] Field: [" + ptr.field + "] Value: [" + ptr.getValue(false, false) + "]");
                        full.append(cleanValue(ptr, ptr.getValue(true, false)));
                    }

                    if(!added)  full.append(c);
                    ptr.reset();
                }

                continue;
            }

            if(c == '\n') {
                if(ptr.expectingValue() || !ptr.valueIsEmpty()) {
                    //System.out.println("[2] Field: [" + ptr.field + "] Value: [" + value + "]");
                    full.append(cleanValue(ptr, ptr.getValue(true, false)));
                    ptr.reset();
                }

                if(currentChunk.length() > 0) {
                    full.append(currentChunk);
                    currentChunk = new StringBuilder();
                }
            }

            if(c == ' ') {
                if(currentChunk.length() > 0) {
                    String cChunk = currentChunk.toString();
                    ptr.ensureField(cChunk);
                    currentChunk = new StringBuilder();
                    full.append(cChunk);
                } else if(!ptr.valueIsEmpty()) {
                    //End of Value like Inode
                    //System.out.println("[3] Field: [" + ptr.field + "] Value: [" + value + "]");
                    full.append(cleanValue(ptr, ptr.getValue(true, false)));
                    ptr.reset();
                }
            }

            if(Character.isAlphabetic(c) || currentChunk.length() > 0) {
                currentChunk.append(c);
                continue;
            }

            full.append(c);
        }

        return full.toString();
    }

    private static String cleanValue(FieldValuePointer ptr, String value) {
        if(ptr.lastWasTime) {
            if(value.length() > 8) {
                //2024-10-04 11:52:56.023000000
                //2016-10-04 11:52:56.233000000 +0300
                String[] halfs = value.split(" ");
                //Split by the space
                StringBuilder dateValue = new StringBuilder();
                //1969-12-31 18:00:00.000000000 (default)

                //Generate a Year
                //int randomYear = generateRandomYear(1969, 2024);
                int year = ptr.getYearForField();
                //System.out.println()
                dateValue.append(year);                                                                                         //Generate the Year
                dateValue.append("-");
                int randomMonth = RandomDateHelper.generateRandomMonth();
                dateValue.append(RandomDateHelper.formatAsTwoDigits(randomMonth));                                                //Generate the Month
                dateValue.append("-");
                dateValue.append(RandomDateHelper.formatAsTwoDigits(RandomDateHelper.generateRandomDay(randomMonth, year)));        //Generate the Day

                if(halfs.length > 1) {
                    //Lower Half => 11:52:56.233000000
                    //              hours, minutes, seconds, nanos
                    dateValue.append(" ");
                    String lowHalf = halfs[1];
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
                                        boolean allZeros = ThreadLocalRandom.current().nextBoolean();
                                        lowBuild.append(allZeros ? "000000000" : RandomDateHelper.generateRandomNanoseconds());
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

                        dateValue.append(lowBuild);
                    } else {
                        dateValue.append(RandomDateHelper.generateRandomHours());
                    }

                    if(halfs.length == 3) {
                        dateValue.append(" ");
                        dateValue.append(ptr.timeOffset);
                    }
                }
                return dateValue.toString();
            } else {
                return generateNumber(100000, 9999999);
            }
        } else if(ptr.lastWasDevice) {
            return generateDeviceId();
        } else if(ptr.lastWasInode) {
            return generateNumber(58, 9999999);
        }

        return value;
    }

    private static String generateNumber(int low, int high) {
        int num = ThreadLocalRandom.current().nextInt(low, high);
        if(num <= 9) {
            return "0" + String.valueOf(num);
        } else {
            return String.valueOf(num);
        }
    }

    private static String generateDeviceId() {
        // Generate major number (1-255)
        int majorNumber = ThreadLocalRandom.current().nextInt(1, 256);
        // Generate minor number (0-255)
        int minorNumber = ThreadLocalRandom.current().nextInt(256);
        // Combine major and minor numbers
        int combinedNumber = (majorNumber << 8) | minorNumber;
        // Format the output
        String hexRepresentation = String.format("%xh", combinedNumber);
        String decimalRepresentation = String.format("%dd", combinedNumber);
        return hexRepresentation + "/" + decimalRepresentation;
    }
}
