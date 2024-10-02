package eu.faircode.xlua.utilities.reflect;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class ReflectUtil {
    private static final String TAG = "ObbedCode.XP.ReflectUtil";


    public static int useFieldValueOrDefaultInt(Class<?> clazz, String fieldName, int defaultValue) {
        DynamicField field = new DynamicField(clazz, fieldName).setAccessible(true);
        if(!field.isValid()) return defaultValue;
        Object val = field.tryGetValueStatic();
        if(val == null) return defaultValue;
        try {
            if(val instanceof Integer)
                return (int)val;
        }catch (ClassCastException ignored) { }
        return defaultValue;
    }

    public static Field tryGetFieldEx(Class<?> clazz, String fieldA, String fieldB) {
        Field field = tryGetField(clazz, fieldA, false);
        if(field != null)
            return field;

        return tryGetField(clazz, fieldB, false);
    }

    public static Method tryGetMethodEx(Class<?> clazz, String methodA, String methodB, Class<?>... params) {
        Method method = tryGetMethod(clazz, methodA, params);
        if(method != null)
            return method;

        return tryGetMethod(clazz, methodB, params);
    }

    public static Method tryGetMethod(String className, String methodName, Class<?>... params) {
        try {
            return tryGetMethod(Class.forName(className), methodName, params);
        }catch (Exception e) {
            Log.e(TAG, "[tryGetMethod] Failed to get Method: " + methodName + " Error: " + e.getMessage());
            return null;
        }
    }

    public static Method tryGetMethod(Class<?> clazz, String methodName, Class<?>... params) {
        try {
            return clazz.getMethod(methodName, params);
        }catch (Exception e) {
            try {
                return clazz.getDeclaredMethod(methodName, params);
            }catch (Exception ee) {
                Log.e(TAG, "[tryGetMethod] Failed to get Method: " + methodName + " Error: " + e.getMessage());
                return null;
            }
        }
    }

    public static Class<?> tryGetClassForName(String className) {
        try {
            return Class.forName(className);
        }catch (Exception e) {
            Log.e(TAG, "[tryGetClassForName] Failed to Get Class for name: " + e.getMessage());
            return null;
        }
    }

    public static Field tryGetField(String className, String fieldName, boolean setAccessible) { return tryGetField(tryGetClassForName(className), fieldName, setAccessible); }
    public static Field tryGetField(Class<?> clazz, String fieldName, boolean setAccessible) {
        if(clazz == null) {
            Log.e(TAG, "[tryGetField] Failed as Class was Null: " + fieldName);
            return null;
        }

        try {
            Field field = clazz.getField(fieldName);
            if(setAccessible)
                field.setAccessible(true);

            return field;
        }catch (Exception e) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                if(setAccessible)
                    field.setAccessible(true);

                return field;
            }catch (Exception ee) {
                Log.e(TAG, "[tryGetField] Failed to Get Field: " + fieldName + " Set Accessible: " + setAccessible + " Error: " + ee.getMessage());
                return null;
            }
        }
    }

    public static Object tryGetFieldValueInstance(Object instance, String fieldName) { return tryGetFieldValueInstance(instance, fieldName, false); }
    public static Object tryGetFieldValueInstance(Object instance, String fieldName, boolean setAccessible) {
        if(instance == null) {
            Log.e(TAG, "[tryGetFieldValueInstance] Failed to get Field Value as Instance object is Null: " + fieldName + " Set Accessible: " + setAccessible);
            return null;
        }

        Field field = tryGetField(instance.getClass(), fieldName, setAccessible);
        if(field == null) {
            Log.e(TAG, "[tryGetFieldValueInstance] Failed to get Field Value as Field is Null: " + fieldName + " Set Accessible: " + setAccessible);
            return null;
        }

        try {
            return field.get(instance);
        }catch (Exception e) {
            Log.e(TAG, "[tryGetFieldValueInstance] Failed to get Field Value... Class: " + instance.getClass().getName() + " Field: " + fieldName + " Set Accessible: " + setAccessible);
            return null;
        }
    }

    public static Object tryGetFieldValue(String className, String fieldName) { return tryGetFieldValue(className, fieldName, false); }
    public static Object tryGetFieldValue(String className, String fieldName, boolean setAccessible) { return tryGetFieldValue(tryGetClassForName(className), fieldName, setAccessible); }

    public static Object tryGetFieldValue(Class<?> clazz, String fieldName) { return tryGetFieldValue(clazz, fieldName, false); }
    public static Object tryGetFieldValue(Class<?> clazz, String fieldName, boolean setAccessible) {
        Field field = tryGetField(clazz, fieldName, setAccessible);
        if(field == null) {
            Log.e(TAG, "[tryGetFieldValue] Failed to get Field Value as Field is Null: " + fieldName + " Set Accessible: " + setAccessible);
            return null;
        }

        try {
            return field.get(null);
        }catch (Exception e) {
            Log.e(TAG, "[tryGetFieldValue] Failed to get Field Value... Class: " + clazz.getName() + " Field: " + fieldName + " Set Accessible: " + setAccessible);
            return null;
        }
    }
}
