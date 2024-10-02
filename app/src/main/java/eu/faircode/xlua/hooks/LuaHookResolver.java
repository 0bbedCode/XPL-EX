package eu.faircode.xlua.hooks;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.utilities.ReflectUtilEx;

public class LuaHookResolver {
    public final Class<?> clazz;
    public final String methodName;
    public final Class<?>[] paramTypes;
    public final Class<?> returnType;
    public boolean preInit;

    public boolean isConstructor() {
        return methodName == null || TextUtils.isEmpty(methodName);
    }
    public boolean isField() {
        return XHookUtil.isField(methodName);
    }

    public LuaHookResolver(Class<?> clazz, String methodName, Class<?>[] paramTypes, Class<?> returnType, boolean preInit) {
        this.clazz = clazz;
        this.methodName = methodName;
        this.paramTypes = paramTypes;
        this.returnType = returnType;
        this.preInit = preInit;
    }

    public boolean hasMismatchReturn(Member member) throws Throwable {
        if(isConstructor()) return false;
        try {
            return hasMismatchReturn(((Method)member).getReturnType());
        }catch (Exception e) {
            XLog.e("Failed to check Type if MisMatch...", e);
            return true;
        }
    }

    public boolean hasMismatchReturn(Class<?> compareType) {
        if(!isConstructor() && !ReflectUtilEx.returnTypeIsValid(compareType, returnType)) {
            XLog.e("Invalid return type " + compareType + " got, needed: " + returnType);
            //return true;
            return !ReflectUtilEx.sameTypes(compareType, returnType);
        } return false;
    }

    public Member tryGetAsMember() {
        try {
            return ReflectUtilEx.resolveMember(clazz, methodName, paramTypes);
        }catch (NoSuchMethodException e) {
            XLog.e("Failed to resolve Member: " + this , e, false);
            return null;
        }
    }

    public Field tryGetAsField(boolean setAccessible) {
        try {
            Field field = ReflectUtilEx.resolveField(clazz, methodName.substring(1), returnType);
            if(setAccessible) field.setAccessible(true);
            return field;
        }catch (Exception e) {
            XLog.e("Failed to Resolve Field: " + this, e, false);
            return null;
        }
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if(clazz != null) {
            b.append("class=");
            b.append(clazz.getName());
        }

        if(methodName != null) {
            b.append(" member=");
            b.append(isConstructor() ? "constructor" : methodName);
        }

        if(returnType != null) {
            b.append(" return=");
            b.append(returnType.getName());
        }

        if(!isField()) {
            if(paramTypes != null) {
                int ln = paramTypes.length;
                if(ln > 0) {
                    b.append("\nparams=(");
                    int nd = ln - 1;
                    for(int i = 0; i < ln; i++) {
                        b.append(paramTypes[i].getName());
                        if(i != nd) {
                            b.append(",");
                        }else {
                            b.append(")");
                        }
                    }
                }
            }
        }

        return b.toString();
    }
}
