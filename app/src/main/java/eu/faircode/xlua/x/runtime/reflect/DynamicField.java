package eu.faircode.xlua.x.runtime.reflect;

import android.util.Log;

import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.List;

import eu.faircode.xlua.logger.XLog;

public class DynamicField {
    private static final String TAG = "ObbedCode.XP.DynamicField";

    public static DynamicField create(String className, String fieldName) { return new DynamicField(className, fieldName); }
    public static DynamicField create(Class<?> clazz, String fieldName) { return new DynamicField(clazz, fieldName); }

    private final Field mField;
    private Object mInstance;

    public Field getField() { return mField; }
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

    public boolean trySetValueInstanceEx(Object instance, Object v) {
        try {
            mField.set(instance, v);
            return true;
        }catch (Exception e) {
            XLog.e(TAG, "[trySetValueInstance] Failed: " + e.getMessage());
            return false;
        }
    }

    public <T> T getValueStatic() throws IllegalAccessException { return DynamicType.convertValue(mField.get(null)); }
    public <T> T tryGetValueStatic() {
        try {
            return  DynamicType.convertValue(mField.get(null));
        }catch (Exception e) {
            Log.e(TAG, "[tryGetValueStatic] Failed: " + e.getMessage());
            return null;
        }
    }

    public <T> T getValueInstance() throws IllegalAccessException { return  DynamicType.convertValue(mField.get(mInstance)); }
    public <T> T tryGetValueInstance() {
        try {
            return  DynamicType.convertValue(mField.get(mInstance));
        }catch (Exception e) {
            Log.e(TAG, "[tryGetValueInstance] Failed: " + e.getMessage());
            return null;
        }
    }

    public <T> T getValueInstanceEx(Object instance) throws IllegalAccessException { return  DynamicType.convertValue(mField.get(instance)); }
    public <T> T tryGetValueInstanceEx(Object instance) {
        try {
            return  DynamicType.convertValue(mField.get(instance));
        }catch (Exception e) {
            Log.e(TAG, "[tryGetValueInstanceEx] Failed: " + e.getMessage());
            return null;
        }
    }

    public <T> T tryGetValueInstanceEx(Object instance, T def) {
        try {
            return DynamicType.convertValue(mField.get(instance));
        }catch (Exception e) {
            Log.e(TAG, "[tryGetValueInstanceEx] Failed: " + e.getMessage());
            return def;
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(mField == null) return false;
        if(obj instanceof String) {
            String s = (String)obj;
            return mField.getName().equalsIgnoreCase(s);
        }

        if(obj instanceof DynamicField) {
            DynamicField f = (DynamicField) obj;
            if(f.isValid()) {
                return f.mField.getName().equalsIgnoreCase(mField.getName());
            }
        }

        return false;
    }
}
