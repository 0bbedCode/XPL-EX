package eu.faircode.xlua.hooks;

import android.content.Context;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.lang.reflect.Field;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.XParam;

public class LuaHookWrapper {
    public final Globals globals;
    public final LuaClosure closure;
    public final LuaValue func;
    public final LuaValue[] args;
    public final boolean isMemberOrMethod;

    public LuaHookWrapper(
            Context context,
            XLuaHook hook,
            Map<String, String> settings,
            Map<String, Integer> propSettings,
            Map<String, String> propMaps,
            Prototype compiledScript,
            String function,
            XC_MethodHook.MethodHookParam param,
            Globals globals,
            String key,
            boolean useDefault,
            String packageName) {
        isMemberOrMethod = true;
        this.globals = globals;
        closure = new LuaClosure(compiledScript, globals);
        closure.call();
        func = globals.get(function);
        args = new LuaValue[]{
                CoerceJavaToLua.coerce(hook),
                CoerceJavaToLua.coerce(new XParam(context, param, settings, propSettings, propMaps, key, useDefault, packageName))
        };
    }

    public LuaHookWrapper(
            Context context,
            XLuaHook hook,
            Map<String, String> settings,
            Map<String, Integer> propSettings,
            Map<String, String> propMaps,
            Prototype compiledScript,
            Field field,
            String key,
            boolean useDefault,
            String packageName) {
        isMemberOrMethod = false;
        globals = XHookUtil.getHookGlobals(context, hook, settings, propSettings, propMaps, key, useDefault, packageName);
        closure = new LuaClosure(compiledScript, globals);
        closure.call();
        func = globals.get("after");
        args = new LuaValue[]{
                CoerceJavaToLua.coerce(hook),
                CoerceJavaToLua.coerce(new XParam(context, field, settings, propSettings, propMaps, key, useDefault, packageName))
        };
    }

    public boolean isValid() { return func != null && !func.isnil(); }
    public Varargs invoke() {
        if(!isValid()) return null;
        return func.invoke(args);
    }

    public static LuaHookWrapper createMember(
            Context context,
            XLuaHook hook,
            Map<String, String> settings,
            Map<String, Integer> propSettings,
            Map<String, String> propMaps,
            Prototype compiledScript,
            String function,
            XC_MethodHook.MethodHookParam param,
            Globals globals,
            String key,
            boolean useDefault,
            String packageName) {
        return new LuaHookWrapper(context, hook, settings, propSettings, propMaps, compiledScript, function, param, globals, key, useDefault, packageName);
    }

    public static LuaHookWrapper createField(
            Context context,
            XLuaHook hook,
            Map<String, String> settings,
            Map<String, Integer> propSettings,
            Map<String, String> propMaps, Prototype compiledScript,
            Field field,
            String key,
            boolean useDefault,
            String packageName) {
        return new LuaHookWrapper(context, hook, settings, propSettings, propMaps, compiledScript, field, key, useDefault, packageName);
    }
}
