package eu.faircode.xlua.x.runtime.reflect;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.utilities.CollectionUtil;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtils;
import eu.faircode.xlua.x.runtime.RuntimeUtils;

public class Reflection {
    private static final String TAG = "XLua.Reflection";

    public static Class<?> getClassForName(String className) {
        try {
            if(TextUtils.isEmpty(className))
                throw new Exception("Class Name is Null or Empty...");

            return Class.forName(className);
        }catch (Exception e) {
            Log.e(TAG, "Failed to Resolve Class Object for Class Name, Name=" + className + " Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            return null;
        }
    }

    public static List<Class<?>> getClasses(String className, boolean getDeclared) { return getClasses(getClassForName(className), getDeclared); }
    public static List<Class<?>> getClasses(Object o, boolean getDeclared) { return getClasses(o == null ? null : o.getClass(), getDeclared); }
    public static List<Class<?>> getClasses(Class<?> clazz, boolean getDeclared) {
        if(clazz != null) {
            try {
                return ListUtils.combineElements(clazz.getClasses(), getDeclared ? clazz.getDeclaredClasses() : null, getDeclared);
            }catch (Exception e) {
                Log.e(TAG, "Failed to Get Classes for Class [" + ReflectUtil.getObjectTypeOrNull(clazz) + "] " +
                        "Get Declared=" + getDeclared + " Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            }
        }

        return new ArrayList<>();
    }

    public static List<Field> getFields(String className, boolean getDeclared) { return getFields(getClassForName(className), getDeclared); }
    public static List<Field> getFields(Object o, boolean getDeclared) { return getFields(o != null ? o.getClass() : null, getDeclared); }
    public static List<Field> getFields(Class<?> clazz, boolean getDeclared) {
        if(clazz != null) {
            try {
                return ListUtils.combineElements(clazz.getFields(), getDeclared ? clazz.getDeclaredFields() : null, getDeclared);
            }catch (Exception e) {
                Log.e(TAG, "Failed to Get Fields for Class [" + ReflectUtil.getObjectTypeOrNull(clazz) + "] " +
                        "Get Declared=" + getDeclared + " Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            }
        }

        return new ArrayList<>();
    }

    public static List<Method> getMethods(String className, boolean getDeclared) { return getMethods(getClassForName(className), getDeclared);  }
    public static List<Method> getMethods(Object o, boolean getDeclared) { return getMethods(o != null ? o.getClass() : null, getDeclared); }
    public static List<Method> getMethods(Class<?> clazz, boolean getDeclared) {
        if(clazz != null) {
            try {
                return ListUtils.combineElements(clazz.getMethods(), getDeclared ? clazz.getDeclaredMethods() : null, getDeclared);
            }catch (Exception e) {
                Log.e(TAG, "Failed to Get Methods for Class [" + ReflectUtil.getObjectTypeOrNull(clazz) + "] " +
                        "Get Declared=" + getDeclared + " Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            }
        }

        return new ArrayList<>();
    }
}
