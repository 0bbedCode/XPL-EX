package eu.faircode.xlua.x.xlua;

import eu.faircode.xlua.x.Str;

/*
    Used for when Sharing Code / Libs across Projects, the Prefix does not need to be changed manually in all class Tags
    Just update it here
 */
public class LibUtil {
    public static final String PREFIX = "XLua";
    public static final String METHOD_DELIMITER = "::";

    public static String generateTag(Class<?> clazz) { return generateTag(clazz.getSimpleName()); }
    public static String generateTag(String className) { return Str.combineEx(PREFIX, Str.PERIOD, className); }

    public static String generateTag(Class<?> clazz, String methodName) { return generateTag(clazz.getSimpleName(), methodName); }
    public static String generateTag(String className, String methodName) { return Str.combineEx(PREFIX, Str.PERIOD, className, METHOD_DELIMITER, methodName); }

}
