package eu.faircode.xlua.x.runtime.reflect;

import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;

public class DynamicType {
    private static final String TAG = "XLua.DynamicType";


    public static boolean classImplementInterface(Class<?> clazz, Class<?> interfaceClass) {
        if (clazz == null || interfaceClass == null || !interfaceClass.isInterface()) {
            if(DebugUtil.isDebug())
                Log.e(TAG, "Error Type does not Inherit Interface! Type=" + Str.toStringOrNull(clazz) + " Interface=" + Str.toStringOrNull(interfaceClass));

            return false;
        }
        return interfaceClass.isAssignableFrom(clazz);
    }

    private static Class<?> boxType(Class<?> type) {
        if (type == boolean.class)
            return Boolean.class;
        else if (type == byte.class)
            return Byte.class;
        else if (type == char.class)
            return Character.class;
        else if (type == short.class)
            return Short.class;
        else if (type == int.class)
            return Integer.class;
        else if (type == long.class)
            return Long.class;
        else if (type == float.class)
            return Float.class;
        else if (type == double.class)
            return Double.class;
        return type;
    }

    /*public static <T> T ensureTypeConversion(Object value, Class<?> targetType) {
        if(value == null) return null;
        try {

            if(value instanceof Number) {
                //Its a Number Type
                Number num = (Number)value;
                Class<?> boxTypeOfTarget = boxType(targetType); //Since Value will be Boxed ?

            }

        } catch (Exception e) {
            Log.e(TAG, "Type conversion failed: " + e.getMessage() +
                    " Value type: " + value.getClass().getName() + " Stack=" + Log.getStackTraceString(e));
            return null;
        }
    }*/

    //Finish this
    /*public static <T> T convertType(Object value, Class<?> targetType) {
        if(value == null) return null;
        try {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Dynamic Type, Converting Value Type=" + value.getClass().getName());

            //Assume UnBox FIRST if missing Target Type ?
            if(value instanceof Number) {
                Number num = (Number) value;

            }


        }catch (Exception e) {
            Log.e(TAG, "Type conversion failed: " + e.getMessage() +
                    " Value type: " + value.getClass().getName() + " Stack=" + Log.getStackTraceString(e));
            return null;
        }
    }*/


    @SuppressWarnings("unchecked")
    public static <T> T convertValue(Object value) {
        if (value == null) return null;
        try {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Dynamic Type, Converting Value Type=" + value.getClass().getName());

            // If the desired type is the same as the value type, return directly
            if (((Class<T>)value.getClass()).isInstance(value)) {
                return (T)value;
            }

            // Handle primitive type conversions
            if (value instanceof Number) {
                Number num = (Number)value;

                // Use type literal comparison to determine the target type
                Class<?> targetType = ((Class<T>)value.getClass());
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Dynamic Type is of Number, Target Type=" + targetType.getName());

                if (Integer.class.equals(targetType) || int.class.equals(targetType)) {
                    return (T)Integer.valueOf(num.intValue());
                }
                if (Long.class.equals(targetType) || long.class.equals(targetType)) {
                    return (T)Long.valueOf(num.longValue());
                }
                if (Float.class.equals(targetType) || float.class.equals(targetType)) {
                    return (T)Float.valueOf(num.floatValue());
                }
                if (Double.class.equals(targetType) || double.class.equals(targetType)) {
                    return (T)Double.valueOf(num.doubleValue());
                }
                if (Short.class.equals(targetType) || short.class.equals(targetType)) {
                    return (T)Short.valueOf(num.shortValue());
                }
                if (Byte.class.equals(targetType) || byte.class.equals(targetType)) {
                    return (T)Byte.valueOf(num.byteValue());
                }
            }


            // If no specific conversion is needed, try direct casting
            return (T)value;
        } catch (ClassCastException e) {
            Log.e(TAG, "Type conversion failed: " + e.getMessage() +
                    " Value type: " + value.getClass().getName() + " Stack=" + Log.getStackTraceString(e));
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static  <T> T tryCast(Object o) {
        if(o != null) {
            try {
                return (T)o;
            }catch (Exception ignored) { }
        }
        return null;
    }
}
