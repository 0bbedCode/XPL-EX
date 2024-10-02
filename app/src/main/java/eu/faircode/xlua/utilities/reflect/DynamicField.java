package eu.faircode.xlua.utilities.reflect;

import android.util.Log;

import java.lang.reflect.Field;

import eu.faircode.xlua.logger.XLog;

public class DynamicField {
    private static final String TAG = "ObbedCode.XP.DynamicField";

    private final Field mField;
    private Object mInstance;


    public boolean isValid() { return mField != null; }

    public DynamicField(String className, String fieldName) { this(ReflectUtil.tryGetClassForName(className), fieldName); }
    public DynamicField(Class<?> clazz, String fieldName) { this(ReflectUtil.tryGetField(clazz, fieldName, false)); }
    public DynamicField(Field field) {
        this.mField = field;
    }

    public DynamicField bindInstance(Object instance) { this.mInstance = instance; return this; }
    public DynamicField setAccessible(boolean accessible) {
        try {
            mField.setAccessible(accessible);
            return this;
        }catch (Exception e) {
            Log.e(TAG, "Failed to set Field accessibility: " + accessible + " Error: " + e.getMessage());
            return this;
        }
    }

    public void trySetValueInstance(Object v) {
        try {
            mField.set(mInstance, v);
        }catch (Exception e) {
            XLog.e(TAG, "[trySetValueInstance] Failed: " + e.getMessage());
        }
    }

    public <T> T getValueStatic() throws IllegalAccessException { return (T)mField.get(null); }
    public <T> T tryGetValueStatic() {
        try {
            return (T)mField.get(null);
        }catch (Exception e) {
            Log.e(TAG, "[tryGetValueStatic] Failed: " + e.getMessage());
            return null;
        }
    }

    public <T> T getValueInstance() throws IllegalAccessException { return (T)mField.get(mInstance); }
    public <T> T tryGetValueInstance() {
        try {
            return (T)mField.get(mInstance);
        }catch (Exception e) {
            Log.e(TAG, "[tryGetValueInstance] Failed: " + e.getMessage());
            return null;
        }
    }

    public <T> T getValueInstanceEx(Object instance) throws IllegalAccessException { return (T)mField.get(instance); }
    public <T> T tryGetValueInstanceEx(Object instance) {
        try {
            return (T)mField.get(instance);
        }catch (Exception e) {
            Log.e(TAG, "[tryGetValueInstanceEx] Failed: " + e.getMessage());
            return null;
        }
    }
}
