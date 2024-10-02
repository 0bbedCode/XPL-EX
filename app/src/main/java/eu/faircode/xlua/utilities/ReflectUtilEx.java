package eu.faircode.xlua.utilities;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import eu.faircode.xlua.Str;

public class ReflectUtilEx {
    private static final String JAVA_LANG = "java.lang";



    private static final String TAG = "XLua.ReflectUtil";
    private static final String GL_PATTEN = ".*\\.GL\\d{2}$";
    private static final String GLES_PATTERN = ".*\\.GLES\\d{2}$";

    public static boolean isReflectError(Throwable fe) {
        return  fe instanceof NoSuchFieldException ||
                fe instanceof NoSuchMethodException ||
                fe instanceof ClassNotFoundException ||
                fe instanceof NoClassDefFoundError;
    }

    public static boolean returnTypeIsValid(Class<?> compareType, Class<?> returnType) {
        if(ReflectUtilEx.isReturnTypeNullOrVoid(compareType) && ReflectUtilEx.isReturnTypeNullOrVoid(returnType))
            return true;

        if(ReflectUtilEx.isReturnTypeNullOrVoid(compareType) || ReflectUtilEx.isReturnTypeNullOrVoid(returnType))
            return false;

        return compareType.isAssignableFrom(returnType);
    }


    public static boolean sameTypes(Class<?> a, Class<?> b) {
        String aType = a.getName().toLowerCase();
        String bType = b.getName().toLowerCase();
        if(aType.startsWith(JAVA_LANG) || bType.startsWith(JAVA_LANG)) {
            //Log.e(TAG, "ATYPE=" + aType + " BTYPE=" + bType);
            if(aType.contains(".")) aType = Str.getLastString(aType, ".");
            if(bType.contains(".")) bType = Str.getLastString(bType, ".");
            //.e(TAG, "ANAME=" + aType + " BNAME=" + bType);
            if(aType.startsWith("bool")) return bType.startsWith("bool");
            if(aType.startsWith("int")) return bType.startsWith("int");
            if(aType.equals("long")) return bType.equals(aType);
            if(aType.equals("float")) return bType.equals(aType);
            if(aType.equals("double")) return bType.equals(aType);
            if(aType.equals("short")) return bType.equals(aType);
            if(aType.equals("string")) return bType.equals(aType);
        }

        return false;
    }


    public static boolean extendsGpuClass(Class<?> clazz) {
        Class<?> superClass = clazz.getSuperclass();
        if(superClass == null)
            return false;

        String name = superClass.getName();
        if(!name.startsWith("android.opengl."))
            return false;


        Log.i(TAG, "Is Opengl Class: " + name);

        return name.endsWith("GL") ||
                name.endsWith("GLES") ||
                name.matches(GL_PATTEN) ||
                name.matches(GLES_PATTERN);

        /*return clazz.isAssignableFrom(GL10.class) ||
                clazz.isAssignableFrom(GL11.class) ||
                clazz.isAssignableFrom(GLES10.class) ||
                clazz.isAssignableFrom(GLES11.class) ||
                clazz.isAssignableFrom(GLES20.class) ||
                clazz.isAssignableFrom(GLES30.class) ||
                clazz.isAssignableFrom(GLES31.class) ||
                clazz.isAssignableFrom(GLES32.class) ||
                clazz.isAssignableFrom(GLES11.class);*/
    }

    public static boolean isReturnTypeNullOrVoid(Class<?> clss) {
        return clss == null || clss == void.class || clss == Void.TYPE;
    }

    public static Class<?> resolveClass(String name, ClassLoader loader) throws ClassNotFoundException {
        //make this better as well as the type resolving better for everything clean up Hook classes now when tommrow
        if ("boolean".equalsIgnoreCase(name) || "bool".equalsIgnoreCase(name))
            return boolean.class;
        else if ("byte".equalsIgnoreCase(name))
            return byte.class;
        else if ("char".equalsIgnoreCase(name))
            return char.class;
        else if ("short".equalsIgnoreCase(name))
            return short.class;
        else if ("int".equalsIgnoreCase(name) || "integer".equalsIgnoreCase(name))
            return int.class;
        else if ("long".equalsIgnoreCase(name))
            return long.class;
        else if ("float".equalsIgnoreCase(name))
            return float.class;
        else if ("double".equalsIgnoreCase(name))
            return double.class;

        else if ("boolean[]".equalsIgnoreCase(name))
            return boolean[].class;
        else if ("byte[]".equalsIgnoreCase(name))
            return byte[].class;
        else if ("char[]".equalsIgnoreCase(name))
            return char[].class;
        else if ("short[]".equalsIgnoreCase(name))
            return short[].class;
        else if ("int[]".equalsIgnoreCase(name))
            return int[].class;
        else if ("long[]".equalsIgnoreCase(name))
            return long[].class;
        else if ("float[]".equalsIgnoreCase(name))
            return float[].class;
        else if ("double[]".equalsIgnoreCase(name))
            return double[].class;

        else if ("void".equalsIgnoreCase(name))
            return Void.TYPE;

        else
            return Class.forName(name, false, loader);
    }

    public static Field resolveField(Class<?> cls, String name, Class<?> type) throws NoSuchFieldException {
        try {
            Class<?> c = cls;
            while (c != null && !c.equals(Object.class))
                try {
                    Field field = c.getDeclaredField(name);
                    if (!field.getType().equals(type))
                        throw new NoSuchFieldException();
                    return field;
                } catch (NoSuchFieldException ex) {
                    for (Field field : c.getDeclaredFields()) {
                        if (!name.equals(field.getName()))
                            continue;

                        if (!field.getType().equals(type))
                            continue;

                        Log.i(TAG, "Resolved field=" + field);
                        return field;
                    }
                }
            throw new NoSuchFieldException(name);
        } catch (NoSuchFieldException ex) {
            Class<?> c = cls;
            while (c != null && !c.equals(Object.class)) {
                Log.i(TAG, c.toString());
                for (Method method : c.getDeclaredMethods())
                    Log.i(TAG, "- " + method.toString());
                c = c.getSuperclass();
            }
            throw ex;
        }
    }

    public static Member resolveMember(Class<?> cls, String name, Class<?>[] params) throws NoSuchMethodException {
        if(cls == null)
            throw new NoSuchMethodException("Class is NULL for the Method...");

        boolean exists = false;
        try {
            Class<?> c = cls;
            //hmm ?
            while (!c.equals(Object.class))
                try {
                    if (name == null || TextUtils.isEmpty(name))
                        return c.getDeclaredConstructor(params);
                    else
                        return c.getDeclaredMethod(name, params);
                } catch (NoSuchMethodException ex) {
                    for (Member member : name == null ? c.getDeclaredConstructors() : c.getDeclaredMethods()) {
                        if (name != null && !name.equals(member.getName()))
                            continue;

                        exists = true;

                        Class<?>[] mparams = (name == null
                                ? ((Constructor) member).getParameterTypes()
                                : ((Method) member).getParameterTypes());

                        if (mparams.length != params.length)
                            continue;

                        boolean same = true;
                        for (int i = 0; i < mparams.length; i++) {
                            if (!mparams[i].isAssignableFrom(params[i])) {
                                same = false;
                                break;
                            }
                        }
                        if (!same)
                            continue;

                        Log.i(TAG, "Resolved member=" + member);
                        return member;
                    }
                    c = c.getSuperclass();
                    if (c == null)
                        throw ex;
                }
            throw new NoSuchMethodException(name);
        } catch (NoSuchMethodException ex) {
            Class<?> c = cls;
            while (c != null && !c.equals(Object.class)) {
                Log.i(TAG, c.toString());
                for (Member member : name == null ? c.getDeclaredConstructors() : c.getDeclaredMethods())
                    if (!exists || name == null || name.equals(member.getName()))
                        Log.i(TAG, "    " + member.toString());
                c = c.getSuperclass();
            }
            throw ex;
        }
    }




    /**
     * Creates a Array using Reflection
     *
     * @param className Full Class Path + Name of Array Element Type
     * @param size The Size of the Array
     * @return A Object of the created Array
     */
    public static Object createArray(String className, int size) { return createArray(getClassType(className), size); }

    /**
     * Creates a Array using Reflection
     *
     * @param classType Class Type of the Array Element
     * @param size The Size of the Array
     * @return A Object of the created Array
     */
    public static Object createArray(Class<?> classType, int size) {
        try {
            if(classType == null)
                return null;

            //Integer.TYPE;
            return Array.newInstance(classType, size);
        }catch (Exception e) {
            Log.e(TAG, "Failed to Create Reflect Array! \n" + e + "\n" + Log.getStackTraceString(e));
            return null;
        }
    }

    /**
     * Dynamically Get Class Type
     *
     * @param className Full Class Path + Name of Array Element Type
     * @return Returns a Class Type used for Reflection
     */
    public static Class<?> getClassType(String className) {
        try {
            return Class.forName(className);
        }catch (Exception e) {
            Log.e(TAG, "Failed to Grab Class Type! \n" + e + "\n" + Log.getStackTraceString(e));
            return null;
        }
    }

    /**
     * Dynamically Check if a Java Method Exists
     * Works on Static, Private, Public, Instance
     *
     * @param className Full Class Path + Name of Array Element Type
     * @param methodName Method to Check if Exists
     * @return Returns True if Method Exists, False if Not
     */
    public static boolean javaMethodExists(String className, String methodName) {
        try {
            // Load the class
            Class<?> clazz = Class.forName(className);
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods)
                if (method.getName().equals(methodName))
                    return true; // Method found
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Class not found: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "An error occurred: " + e.getMessage());
        }
        return false; // Method not found
    }
}
