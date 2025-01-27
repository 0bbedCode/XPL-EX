package eu.faircode.xlua.x.hook.interceptors.file.stat;

import android.system.StructStat;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;


import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.runtime.reflect.DynamicField;
import eu.faircode.xlua.x.data.string.StrBuilder;


public class StatMockSettings {
    private static final String TAG = "XLua.StatMockSettings";

    public enum TimeKind {
        ACCESS(0),
        MODIFY(4),
        CHANGE(7);

        private final int value;
        TimeKind(int value) { this.value = value; }
        public int getValue() { return value; }
    }

    public static final DynamicField TIME_STRUCT_SEC_FIELD = new DynamicField("android.system.StructTimespec", "tv_sec")
            .setAccessible(true);

    public static final DynamicField TIME_STRUCT_NANO_FIELD = new DynamicField("android.system.StructTimespec", "tv_nsec")
            .setAccessible(true);

    public static final DynamicField STAT_DEV_FIELD = new DynamicField(StructStat.class, "st_dev")
            .setAccessible(true);

    public static final DynamicField STAT_INODE_FIELD = new DynamicField(StructStat.class, "st_ino")
            .setAccessible(true);

    public static final DynamicField STAT_ACCESS_FIELD = new DynamicField(StructStat.class, "st_atime")
            .setAccessible(true);

    public static final DynamicField STAT_ACCESS_S_FIELD = new DynamicField(StructStat.class, "st_atim")
            .setAccessible(true);

    public static final DynamicField STAT_MODIFICATION_FIELD = new DynamicField(StructStat.class, "st_mtime")
            .setAccessible(true);

    public static final DynamicField STAT_MODIFICATION_S_FIELD = new DynamicField(StructStat.class, "st_mtim")
            .setAccessible(true);

    public static final DynamicField STAT_CHANGE_FIELD = new DynamicField(StructStat.class, "st_ctime")
            .setAccessible(true);

    public static final DynamicField STAT_CHANGE_S_FIELD = new DynamicField(StructStat.class, "st_ctim")
            .setAccessible(true);

    public String file;
    public long startRomSeconds = 0;    //android.build.date.utc
    public String offset = null;        //zone.timezone

    public String deviceId;
    public long devId;

    public long inode;

    public long lastAccessTimeSeconds;
    public long lastAccessTimeNanoSeconds;
    public String lastAccessTimeStamp;

    public long lastModifiedTimeSeconds;
    public long lastModifiedTimeNanoSeconds;
    public String lastModifiedTimeStamp;

    public long lastChangeTimeSeconds;
    public long lastChangeTimeNanoSeconds;
    public String lastChangeTimeStamp;

    public long createdSeconds = 0;
    public String createdTimeStamp = null;

    private void ensureOffset(String originalLineOrData) {
        //RandomDateHelper.generateRandomTimeZoneOffset();
        if(offset == null) offset = StatUtils.findOffset(originalLineOrData);
    }

    public StatMockSettings() { }
    public StatMockSettings(String file) {
        this.file = file;
    }


    public void setTimeZoneOffset(XParam param) { setRomStartSeconds(param.getSetting("region.timezone")); }
    public void setTimeZoneOffset(String settingsValue) {
        if(TextUtils.isEmpty(settingsValue))
            return;

        if(DebugUtil.isDebug())
            Log.d(TAG, "Setting TimeZone Offset=" + settingsValue);

        StringBuilder full = new StringBuilder();
        boolean foundOp = false;
        char[] chars = settingsValue.toCharArray();
        for(char c : chars) {
            if(full.length() == 5)
                break;

            if(!foundOp && (c == '-' || c == '+')) {
                foundOp = true;
                full.append(c);
            } else if(foundOp) {
                if(c != ':') {
                    if(!Character.isDigit(c)) {
                        break;
                    } else {
                        full.append(c);
                    }
                }
            }
        }

        if(full.length() == 5) {
            //Good
            offset = full.toString();
        }
    }

    public void setRomStartSeconds(XParam param) { setRomStartSeconds(param.getSetting("android.build.date.utc")); }
    public void setRomStartSeconds(String settingsValue) {
        if(TextUtils.isEmpty(settingsValue))
            return;

        if(DebugUtil.isDebug())
            Log.d(TAG, "Setting ROM Start Seconds before Parsing=" + settingsValue);

        StringBuilder part = new StringBuilder();
        StringBuilder full = new StringBuilder();
        char[] chars = settingsValue.toCharArray();
        for(char c : chars) {
            if(!Character.isDigit(c)) {
                this.startRomSeconds = 0;
                //Maybe get the prop ?
                break;
            } else {
                if(c == '0') {
                    part.append(c);
                } else {
                    if(part.length() > 0) {
                        full.append(part);
                        part = new StringBuilder();
                    }

                    full.append(c);
                }
            }
        }

        if(full.length() == 0)
            return;

        if(DebugUtil.isDebug())
            Log.d(TAG, "Setting ROM Start Seconds Parsed=" + full.toString());


        this.startRomSeconds = Long.parseLong(full.toString());
    }

    public String getField(String fieldName, String originalValue) {
        String f = internalGetField(fieldName, originalValue);
        if(DebugUtil.isDebug()) Log.d(TAG, "[getField] Field Name=" + fieldName + " Original Value=" + originalValue + " New Value=" + f);
        return f;
    }

    private String internalGetField(String fieldName, String originalValue) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "[internalGetField] Field Name=" + fieldName + " Original Value=" + originalValue);

        if(StatUtils.is1969Year(originalValue)) return originalValue;
        switch(fieldName) {
            case "inode":
                if(inode == 0) inode = RandomGenerator.nextLong(300, 9000000);
                return String.valueOf(inode);
            case "device":
                if(TextUtils.isEmpty(deviceId)) {
                    deviceId = StatUtils.generateDeviceId();
                    devId = StatUtils.parseDeviceId(deviceId);
                }

                return deviceId;
            case "access":
                ensureOffset(originalValue);
                if(TextUtils.isEmpty(lastAccessTimeStamp)) {
                    lastAccessTimeStamp = StatDateHelper.generateFakeDateAccess(
                            originalValue,
                            lastModifiedTimeSeconds,
                            createdSeconds,
                            startRomSeconds,
                            offset);
                    long[] times = StatUtils.parseTimestamp(lastChangeTimeStamp);
                    lastAccessTimeSeconds = times[0];
                    lastAccessTimeNanoSeconds = times[1];
                }

                return lastAccessTimeStamp;
            case "modify":
                ensureOffset(originalValue);
                if(TextUtils.isEmpty(lastModifiedTimeStamp)) {
                    lastModifiedTimeStamp = StatDateHelper.generateFakeDateModify(
                            originalValue,
                            lastAccessTimeSeconds,
                            createdSeconds,
                            startRomSeconds,
                            offset);
                    long[] times = StatUtils.parseTimestamp(lastModifiedTimeStamp);
                    lastModifiedTimeSeconds = times[0];
                    lastModifiedTimeNanoSeconds = times[1];
                }

                return lastModifiedTimeStamp;
            case "change": //Last when Inode Number was Changed we can have it as Birth or just a Random Modify
            case "birth":
            case "create":
                ensureOffset(originalValue);
                if(TextUtils.isEmpty(createdTimeStamp) || TextUtils.isEmpty(lastChangeTimeStamp)) {
                    createdTimeStamp = StatDateHelper.generateFakeCreation(
                            originalValue,
                            lastModifiedTimeSeconds,
                            lastAccessTimeSeconds,
                            startRomSeconds,
                            offset);
                    lastChangeTimeStamp = createdTimeStamp;
                    long[] times = StatUtils.parseTimestamp(createdTimeStamp);
                    lastChangeTimeSeconds = times[0];
                    lastChangeTimeNanoSeconds = times[1];

                    createdSeconds = times[0];
                }

                return createdTimeStamp;
            default:
                return originalValue;
        }
    }

    public void cleanStructure(Object obj) { cleanStructure(obj, null); }
    public void cleanStructure(Object obj, XParam param) {
        if(obj != null) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Received the STAT Struct!");

            StrBuilder orgValue = new StrBuilder();
            StrBuilder newValue = new StrBuilder();
            orgValue.appendFieldLine("File", this.file);
            newValue.appendFieldLine("File", this.file);

            long devId = STAT_DEV_FIELD.tryGetValueInstanceEx(obj, 0L);
            if(devId != 0) {
                if(DebugUtil.isDebug()) Log.d(TAG, "Struct STAT Device Id=" + devId);
                if(TextUtils.isEmpty(this.deviceId)) getField("device", String.valueOf(devId));
                STAT_DEV_FIELD.trySetValueInstanceEx(obj, this.devId);
                if(param != null || DebugUtil.isDebug()) {
                    orgValue.appendFieldLine("DeviceId", devId);
                    newValue.appendFieldLine("DeviceId", this.devId);
                }
            }

            long iNode = STAT_INODE_FIELD.tryGetValueInstanceEx(obj, 0L);
            if(iNode != 0) {
                if(DebugUtil.isDebug()) Log.d(TAG, "Struct STAT Inode=" + iNode);
                if(this.inode == 0) getField("inode", String.valueOf(iNode));
                STAT_INODE_FIELD.trySetValueInstanceEx(obj, this.inode);
                if(param != null || DebugUtil.isDebug()) {
                    orgValue.appendFieldLine("Inode", iNode);
                    newValue.appendFieldLine("Inode", this.inode);
                }
            }

            long[] access = getTimes(obj, TimeKind.ACCESS);
            if(access[0] > 0) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Struct STAT access, Seconds=" + access[0] +  " Nano Seconds=" + access[1] + " Time Stamp=" + StatUtils.timespecToString(access[0], access[1]));

                if(TextUtils.isEmpty(this.lastAccessTimeStamp)) getField("access", StatUtils.timespecToString(access[0], access[1]));
                setTimes(obj, TimeKind.ACCESS, access[0], access[1]);
                if(param != null || DebugUtil.isDebug()) {
                    orgValue.appendFieldLine("Access", StatUtils.timespecToString(access[0], access[1]));
                    newValue.appendFieldLine("Access", this.lastAccessTimeStamp);
                }
            }

            long[] modified = getTimes(obj, TimeKind.MODIFY);
            if(modified[0] > 0) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Struct STAT modified, Seconds=" + modified[0] +  " Nano Seconds=" + modified[1] + " Time Stamp=" + StatUtils.timespecToString(modified[0], modified[1]));

                if(TextUtils.isEmpty(this.lastModifiedTimeStamp)) getField("modify", StatUtils.timespecToString(modified[0], modified[1]));
                setTimes(obj, TimeKind.MODIFY, modified[0], modified[1]);
                if(param != null || DebugUtil.isDebug()) {
                    orgValue.appendFieldLine("Modify", StatUtils.timespecToString(modified[0], modified[1]));
                    newValue.appendFieldLine("Modify", this.lastModifiedTimeStamp);
                }
            }

            long[] change = getTimes(obj, TimeKind.CHANGE);
            if(modified[0] > 0) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Struct STAT Change, Seconds=" + change[0] +  " Nano Seconds=" + change[1] + " Time Stamp=" + StatUtils.timespecToString(change[0], change[1]));

                if(TextUtils.isEmpty(this.lastChangeTimeStamp)) getField("change", StatUtils.timespecToString(change[0], change[1]));
                setTimes(obj, TimeKind.CHANGE, change[0], change[1]);
                if(param != null || DebugUtil.isDebug()) {
                    orgValue.appendFieldLine("Change", StatUtils.timespecToString(change[0], change[1]));
                    newValue.appendFieldLine("Change", this.lastChangeTimeStamp);
                }
            }

            if(param != null) {
                param.setOldResult(orgValue.toString());
                param.setNewResult(newValue.toString());
            }

            if(DebugUtil.isDebug()) {
                Log.d(TAG, StrBuilder.create()
                        .ensureOneNewLinePer(true)
                        .appendFieldLine("Old=", orgValue.toString(true))
                        .appendFieldLine("New=", newValue.toString(true))
                        .toString(true));
            }
        }
    }

    public static void setTimes(Object instance, TimeKind kind, long seconds, long nanoSeconds) {
        if(instance == null) return;
        DynamicField org = null;
        DynamicField timeStruct = null;
        switch (kind) {
            case ACCESS:
                org = STAT_ACCESS_FIELD;
                timeStruct = STAT_ACCESS_S_FIELD;
                break;
            case CHANGE:
                org = STAT_CHANGE_FIELD;
                timeStruct = STAT_CHANGE_S_FIELD;
                break;
            case MODIFY:
                org = STAT_MODIFICATION_FIELD;
                timeStruct = STAT_MODIFICATION_S_FIELD;
                break;
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Changing STAT Struct Time Stamps, Kind=" + kind.name() + " Seconds=" + seconds + " Nano Seconds=" + nanoSeconds);

        if(timeStruct != null && timeStruct.isValid()) {
            Object ts = timeStruct.tryGetValueInstanceEx(instance);
            if(ts != null) {
                TIME_STRUCT_NANO_FIELD.trySetValueInstanceEx(ts, nanoSeconds);
                TIME_STRUCT_SEC_FIELD.trySetValueInstanceEx(ts, seconds);
                timeStruct.trySetValueInstanceEx(instance, ts);
            }
        }

        if(org != null && org.isValid())
            org.trySetValueInstanceEx(instance, seconds);
    }

    public static long[] getTimes(Object instance, TimeKind kind) {
        if(instance == null) return new long[] { 0, 0 };
        DynamicField org = null;
        DynamicField timeStruct = null;
        switch (kind) {
            case ACCESS:
                org = STAT_ACCESS_FIELD;
                timeStruct = STAT_ACCESS_S_FIELD;
                break;
            case CHANGE:
                org = STAT_CHANGE_FIELD;
                timeStruct = STAT_CHANGE_S_FIELD;
                break;
            case MODIFY:
                org = STAT_MODIFICATION_FIELD;
                timeStruct = STAT_MODIFICATION_S_FIELD;
                break;
        }

        long nanos = 0;
        long seconds = 0;
        if(timeStruct != null && timeStruct.isValid()) {
            Object ts = timeStruct.tryGetValueInstanceEx(instance);
            if(ts != null) {
                nanos = TIME_STRUCT_NANO_FIELD.tryGetValueInstanceEx(ts, 0L);
                seconds = TIME_STRUCT_SEC_FIELD.tryGetValueInstanceEx(ts, 0L);
            }
        }

        if(seconds == 0 && org != null && org.isValid()) {
            seconds = org.tryGetValueInstanceEx(instance, 0L);
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Getting STAT Struct Time Stamps, Kind=" + kind.name() + " Seconds=" + seconds + " Nano Seconds=" + nanos);


        return new long[] { seconds, nanos };
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("File", this.file)
                .appendFieldLine("Start Rom Seconds", this.startRomSeconds)
                .appendFieldLine("DeviceId", this.deviceId)
                .appendFieldLine("DevId", this.devId)
                .appendFieldLine("Last Access Seconds", this.lastAccessTimeSeconds)
                .appendFieldLine("Last Access Nano Seconds", this.lastAccessTimeNanoSeconds)
                .appendFieldLine("Last Access", this.lastAccessTimeStamp)
                .appendFieldLine("Last Modified Seconds", this.lastModifiedTimeSeconds)
                .appendFieldLine("Last Modified Nano Seconds", this.lastModifiedTimeNanoSeconds)
                .appendFieldLine("Last Modified", this.lastModifiedTimeStamp)
                .appendFieldLine("Last Change Seconds", this.lastChangeTimeSeconds)
                .appendFieldLine("Last Change Nano Seconds", this.lastChangeTimeNanoSeconds)
                .appendFieldLine("Last Change", this.lastChangeTimeStamp)
                .appendFieldLine("Created Seconds", this.createdSeconds)
                .appendFieldLine("Created", this.createdTimeStamp)
                .toString(true);
    }
}