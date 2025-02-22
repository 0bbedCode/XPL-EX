package eu.faircode.xlua.x.file;

import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.Linq;
import eu.faircode.xlua.x.data.utils.MapUtils;

public class UnixUserUtils {
    public static final String DEFAULT_NAME = "error";

    public static final String USER_ROOT = "root";
    public static final String USER_SYSTEM = "system";
    public static final String USER_SHELL = "shell";
    public static final String USER_NOBODY = "nobody";
    public static final String USER_MEDIA_RW = "media_rw";
    public static final String USER_BLUETOOTH = "bluetooth";
    public static final String USER_WIFI = "wifi";
    public static final String USER_RADIO = "radio";
    public static final String USER_INPUT = "input";
    public static final String USER_GRAPHICS = "graphics";
    public static final String USER_LOG = "log";
    public static final String USER_SECURITY = "security";
    public static final String USER_ADB = "adb";
    public static final String USER_APP_ZYGOTE = "app_zygote";

    public static final Map<UnixUserId, String> ANDROID_UID_LIST = new HashMap<UnixUserId, String>() {{
        put(UnixUserId.ROOT,       USER_ROOT);        // 0
        put(UnixUserId.SYSTEM,     USER_SYSTEM);      // 1000
        put(UnixUserId.SHELL,      USER_SHELL);       // 2000
        put(UnixUserId.NOBODY,     USER_NOBODY);      // 9999
        put(UnixUserId.MEDIA_RW,   USER_MEDIA_RW);    // 1023
        put(UnixUserId.BLUETOOTH,  USER_BLUETOOTH);   // 1002
        put(UnixUserId.WIFI,       USER_WIFI);        // 1010
        put(UnixUserId.RADIO,      USER_RADIO);       // 1001
        put(UnixUserId.INPUT,      USER_INPUT);       // 1004
        put(UnixUserId.GRAPHICS,   USER_GRAPHICS);    // 1003
        put(UnixUserId.LOG,        USER_LOG);         // 1007
        put(UnixUserId.SECURITY,   USER_SECURITY);    // 1009
        put(UnixUserId.ADB,        USER_ADB);         // 1041
        put(UnixUserId.APP_ZYGOTE, USER_APP_ZYGOTE);  // 1015
    }};

    public static String getName(int uid) { return MapUtils.get(ANDROID_UID_LIST, UnixUserId.fromValue(uid), String.valueOf(uid)); }
    public static String getName(UnixUserId uid) { return MapUtils.get(ANDROID_UID_LIST, uid, String.valueOf(uid.getValue())); }

    public static UnixUserId getUid(String name) {
        if(Str.isEmpty(name))
            return UnixUserId.NONE;

        String nameLowered = name.trim().toLowerCase();
        boolean isNumeric = Str.isNumeric(nameLowered);
        if(!isNumeric && nameLowered.contains("zygote"))
            return UnixUserId.APP_ZYGOTE;

        return Linq.firstWhereKey(ANDROID_UID_LIST, UnixUserId.NONE,
                (k, v) -> v.equalsIgnoreCase(nameLowered) || (isNumeric && String.valueOf(k).equalsIgnoreCase(nameLowered)));
    }
}
