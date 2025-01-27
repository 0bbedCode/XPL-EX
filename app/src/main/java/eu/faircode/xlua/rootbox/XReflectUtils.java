package eu.faircode.xlua.rootbox;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import eu.faircode.xlua.BuildConfig;

public class XReflectUtils {
    private static final String TAG = "XLua.rootbox.XReflectUtils";

    public static Class<?> getClassFor(String classPath) {
        try {
            if(!classExists(classPath))
                return null;

            return Class.forName(classPath);
        }catch (Exception e) {
            Log.e(TAG, "Failed to reflect class... " + classPath + "\n" + e);
            return null;
        }
    }

    public static Method getMethodFor(String classPath, String methodName, Class<?>... params) {
        try {
            Class<?> clzz = getClassFor(classPath);
            if(clzz == null) {
                Log.e(TAG, "Class For name: " + classPath + " is null... " + methodName);
                return null;
            }

            try {
                //Gets public methods
                return clzz.getMethod(methodName, params);
            }catch (Exception e) {
                Log.e(TAG, "Failed to get method using generic method: " + methodName + " => " + classPath);
            }

            //Gets all methods
            return clzz.getDeclaredMethod(methodName, params);
        }catch (Exception e) {
            Log.e(TAG, "Failed to reflect method... " + methodName + " => " + classPath + " \n" + e);
            return null;
        }
    }

    public static Method getMethodFor(Class<?> clzz, String methodName) {
        try {
            if(clzz == null) {
                Log.e(TAG, "Class For name: " + clzz.getName() + " is null... " + methodName);
                return null;
            }

            try {
                Method[] methods = clzz.getMethods();
                for(Method m : methods) {
                    if(m.getName().equals(methodName)) {
                        Log.i(TAG, "Found Method via Reflect: " + methodName + " => " + clzz.getName());
                        return m;
                    }
                }
            }catch (Exception e) {
                Log.e(TAG, "Failed to get method using generic method: " + methodName + " => " + clzz.getName());
            }

            Method[] decMethods = clzz.getDeclaredMethods();
            for(Method m : decMethods) {
                if(m.getName().equals(methodName)) {
                    Log.i(TAG, "Found Method via Reflect2: " + methodName + " => " + clzz.getName());
                    return m;
                }
            }

            return null;
        }catch (Exception e) {
            Log.e(TAG, "Failed to reflect method... " + methodName + " => " + clzz.getName() + " \n" + e);
            return null;
        }
    }

    public static Field getFieldFor(String classPath, String fieldName, boolean setAccessible) {
        Class<?> clzz = getClassFor(classPath);
        if(clzz == null) {
            Log.e(TAG, "Class is null: " + classPath + " for field: " + fieldName);
            return null;
        }

        return getFieldFor(clzz, fieldName, setAccessible);
    }

    public static Field getFieldFor(Class<?> clzz, String fieldName, boolean setAccessible) {
        try {
            Field f =  clzz.getDeclaredField(fieldName);
            if(setAccessible)
                f.setAccessible(true);
            return f;
        }catch (Exception e) {
            Log.e(TAG, "Failed to get Field From Reflect: " + fieldName + " class:" + clzz.getName());
            return null;
        }
    }

    public static Method getMethodFor(String classPath, String methodName) {
        try {
            Class<?> clzz = getClassFor(classPath);
            if(clzz == null) {
                Log.e(TAG, "Class For name: " + classPath + " is null... " + methodName);
                return null;
            }

            try {
                Method[] methods = clzz.getMethods();
                for(Method m : methods) {
                    if(m.getName().equals(methodName)) {
                        if(BuildConfig.DEBUG)
                            Log.i(TAG, "Found Method via Reflect: " + methodName + " => " + classPath);
                        return m;
                    }
                }
            }catch (Exception e) {
                Log.e(TAG, "Failed to get method using generic method: " + methodName + " => " + classPath);
            }

            Method[] decMethods = clzz.getDeclaredMethods();
            for(Method m : decMethods) {
                if(m.getName().equals(methodName)) {
                    if(BuildConfig.DEBUG)
                        Log.i(TAG, "Found Method via Reflect2: " + methodName + " => " + classPath);
                    return m;
                }
            }

            return null;
        }catch (Exception e) {
            Log.e(TAG, "Failed to reflect method... " + methodName + " => " + classPath + " \n" + e);
            return null;
        }
    }

    public static boolean classExists(String classPath) {
        try {
            Class<?> c = Class.forName(classPath);
            Log.i(TAG, "Class exists was able to reflect: " + classPath);
            return true;
        }catch (Exception e) {
            Log.e(TAG, "Failed to reflect class: " + classPath + "\n" + e);
            return false;
        }
    }

    public static boolean methodExists(String classPath, String methodName) { return getMethodFor(classPath, methodName) != null; }
    public static boolean fieldExists(String str, String str2) { return getFieldFor(str, str2, true) != null; }
}
