package eu.faircode.xlua.x.runtime.reflect;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class DynamicGetSetPairs {
    public static DynamicGetSetPairs create(Class<?> clazz) { return new DynamicGetSetPairs(clazz); }
    public static DynamicGetSetPairs create(String className) { return new DynamicGetSetPairs(className); }

    private String mClass;
    private Class<?> mClazz;
    private boolean mIsResolved = false;
    private final List<DynamicField> mFields = new ArrayList<>();
    private final List<DynamicMethod> mGetMethods = new ArrayList<>();
    private final List<DynamicMethod> mSetMethods = new ArrayList<>();

    public boolean hasBindings() { return !mFields.isEmpty() || !mGetMethods.isEmpty() || !mSetMethods.isEmpty(); }
    public boolean isResolved() { return mIsResolved; }

    public DynamicGetSetPairs(Class<?> clazz) {
        if(clazz != null) {
            mClass = clazz.getName();
            mClazz = clazz;
            mIsResolved = true;
        }
    }

    public DynamicGetSetPairs(String className) {
        if(!TextUtils.isEmpty(className)) {
            className = className.trim();
            mClass = className;
            mClazz = ReflectUtil.tryGetClassForName(className);
            if(mClazz != null) mIsResolved = true;
        }
    }

    public <T> boolean setValueInstance(Object instance, Object... args) {
        T val = args == null || args[0] == null ? null : DynamicType.<T>convertValue(args[0]);
        for(DynamicField f : mFields) {
            if(f.trySetValueInstanceEx(instance, val))
                return true;
        }

        for(DynamicMethod m : mSetMethods) {
            try {
                m.instanceInvokeEx(instance, args);
                return true;
            }catch (Exception ignored) { }
        }

        return false;
    }

    public <T> T getValueInstanceMethodsFirst(Object instance, Object... args) {
        for(DynamicMethod m : mGetMethods) {
            Object val = m.tryInstanceInvokeEx(instance, args);
            T v = DynamicType.convertValue(val);
            if(v != null)
                return v;
        }

        for(DynamicField f : mFields) {
            Object val = f.tryGetValueInstanceEx(instance);
            T v = DynamicType.convertValue(val);
            if(v != null)
                return v;
        }

        return null;
    }

    public <T> T getValueInstance(Object instance, Object... args) {
        for(DynamicField f : mFields) {
            Object val = f.tryGetValueInstanceEx(instance);
            T v = DynamicType.convertValue(val);
            if(v != null)
                return v;
        }

        for(DynamicMethod m : mGetMethods) {
            Object val = m.tryInstanceInvokeEx(instance, args);
            T v = DynamicType.convertValue(val);
            if(v != null)
                return v;
        }

        return null;
    }

    public DynamicGetSetPairs bindField(String fieldName) {
        if(mIsResolved && fieldName != null) {
            synchronized (mFields) {
                DynamicField field = new DynamicField(mClazz, fieldName.trim())
                        .setAccessible(true);
                if(field.isValid()) {
                    for(DynamicField f : mFields)
                        if(f.equals(field))
                            return this;

                    mFields.add(field);
                }
            }
        }
        return this;
    }

    public DynamicGetSetPairs bindSetAndGetMethod(String methodName, Class<?>... params) {
        if(mIsResolved && methodName != null) {
            synchronized (mGetMethods) {
                DynamicMethod method = new DynamicMethod(mClazz, methodName.trim(), params)
                        .setAccessible(true);
                internalBindMethod(method, mSetMethods);
                internalBindMethod(method, mGetMethods);
            }
        }
        return this;
    }

    public DynamicGetSetPairs bindSetMethod(String methodName, Class<?>... params) {
        if(mIsResolved && methodName != null) {
            synchronized (mGetMethods) {
                DynamicMethod method = new DynamicMethod(mClazz, methodName.trim(), params)
                        .setAccessible(true);
                internalBindMethod(method, mSetMethods);
            }
        }
        return this;
    }

    public DynamicGetSetPairs bindGetMethod(String methodName, Class<?>... params) {
        if(mIsResolved && methodName != null) {
            synchronized (mGetMethods) {
                DynamicMethod method = new DynamicMethod(mClazz, methodName.trim(), params)
                        .setAccessible(true);
                internalBindMethod(method, mGetMethods);
            }
        }
        return this;
    }

    private void internalBindMethod(DynamicMethod method, List<DynamicMethod> list) {
        if(method != null && method.isValid() && list != null && mIsResolved) {
            for(DynamicMethod m : list)
                if(m.equals(method))
                    return;

            list.add(method);
        }
    }
}
