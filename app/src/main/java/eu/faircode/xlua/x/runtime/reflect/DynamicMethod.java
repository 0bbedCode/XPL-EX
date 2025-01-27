package eu.faircode.xlua.x.runtime.reflect;

import android.util.Log;

import androidx.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DynamicMethod {
    private static final String TAG = "ObbedCode.XP.DynamicMethod";

    private final Method mMethod;
    private Object mInstance;

    public boolean isValid() { return mMethod != null; }

    public DynamicMethod(String className, String methodName, Class<?>... paramTypes) { this(ReflectUtil.tryGetClassForName(className), methodName, paramTypes); }
    public DynamicMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) { this(ReflectUtil.tryGetMethodEx(clazz, methodName, paramTypes)); }
    public DynamicMethod(Method method) {
        this.mMethod = method;
    }

    public DynamicMethod bindInstance(Object instance) { this.mInstance = instance; return this; }

    public Object getInstance() { return this.mInstance; }

    public DynamicMethod setAccessible(boolean accessible) {
        try {
            this.mMethod.setAccessible(accessible);
            return this;
        }catch (Exception e) {
            Log.e(TAG, "[setAccessible] Error Setting Accessibility: set:" + accessible);
            return this;
        }
    }

    public <T> T staticInvoke(Object ... args) throws InvocationTargetException, IllegalAccessException { return DynamicType.convertValue(mMethod.invoke(null, args)); }
    public <T> T tryStaticInvoke(Object... args) {
        try {
            return DynamicType.convertValue(mMethod.invoke(null, args));
        }catch (Exception e) {
            Log.e(TAG, "[tryStaticInvoke] Error Invoking Method static: " + e.getMessage());
            return null;
        }
    }

    public <T> T instanceInvokeEx(Object instance, Object... args) throws InvocationTargetException, IllegalAccessException { return DynamicType.convertValue(mMethod.invoke(instance, args)); }
    public <T> T tryInstanceInvokeEx(Object instance, Object... args) {
        try {
            return DynamicType.convertValue(mMethod.invoke(instance, args));
        }catch (Exception e) {
            Log.e(TAG, "[tryInstanceInvokeEx] Error Invoking Method Instance: " + e.getMessage());
            return null;
        }
    }

    public <T> T instanceInvoke(Object... args) throws InvocationTargetException, IllegalAccessException { return DynamicType.convertValue(mMethod.invoke(mInstance, args)); }
    public <T> T tryInstanceInvoke(Object... args) {
        try {
            return DynamicType.convertValue(mMethod.invoke(mInstance, args));
        }catch (Exception e) {
            Log.e(TAG, "[tryInstanceInvoke] Error Invoking Method Instance: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(mMethod == null) return false;
        if(obj instanceof String) {
            String s = (String)obj;
            return mMethod.getName().equalsIgnoreCase(s);
        }

        if(obj instanceof DynamicField) {
            DynamicMethod m = (DynamicMethod) obj;
            if(m.isValid()) {
                return m.mMethod.getName().equalsIgnoreCase(mMethod.getName());
            }
        }

        return false;
    }
}
