package eu.faircode.xlua.hooks;

import android.app.ActivityManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.telephony.SmsManager;

import org.json.JSONArray;
import org.luaj.vm2.Globals;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import eu.faircode.xlua.BuildConfig;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.hook.XLuaHookAssets;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.utilities.ReflectUtilEx;
import eu.faircode.xlua.utilities.StreamUtil;
import eu.faircode.xlua.utilities.StringUtil;

public class XHookUtil {
    public static Globals getHookGlobals(
            Context context,
            XLuaHook hook,
            Map<String, String> settings,
            Map<String, Integer> propSettings,
            Map<String, String> propMaps,
            String key,
            boolean useDefault,
            String packageName) {
        Globals globals = JsePlatform.standardGlobals();
        // base, bit32, coroutine, io, math, os, package, string, table, luajava
        if (BuildConfig.DEBUG) globals.load(new DebugLib());
        //This will create the logger and hook instance
        globals.set("log", new LuaLog(context.getPackageName(), context.getApplicationInfo().uid, hook.getId()));
        globals.set("hook", new LuaHook(context, settings, propSettings, propMaps, key, useDefault, packageName));
        return new LuaLocals(globals);
    }

    public static boolean isField(String methodName) { return methodName != null && methodName.startsWith("#"); }


    public static LuaHookResolver resolveTargetHook(Context context, XLuaHook hook) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {

        Class<?> cls = Class.forName(hook.getResolvedClassName(), false, context.getClassLoader());
        String methodName = hook.getMethodName();
        boolean isPreInit = false;
        if (methodName != null) {
            isPreInit =  methodName.startsWith("!");
            if(isPreInit) methodName = methodName.substring(1);
            String[] m = methodName.split(":");
            if (m.length > 1) {
                Field field = cls.getField(m[0]);
                Object obj = field.get(null);
                cls = obj.getClass();
            }
            methodName = m[m.length - 1]; 
        }

        Class<?>[] paramTypes = XHookUtil.getParameterTypes(hook.getParameterTypes(), context);
        final Class<?> returnType = !StringUtil.isValidString(hook.getReturnType())
                ? null : ReflectUtilEx.resolveClass(hook.getReturnType(), context.getClassLoader());

        return new LuaHookResolver(cls, methodName, paramTypes, returnType, isPreInit);
    }

    public static Class<?>[] getParameterTypes(String[] p, Context context) throws ClassNotFoundException {
        Class<?>[] paramTypes = new Class[p.length];
        for (int i = 0; i < p.length; i++)
            paramTypes[i] = ReflectUtilEx.resolveClass(p[i], context.getClassLoader());

        return paramTypes;
    }

    public static Prototype compileScript(Map<LuaScriptHolder, Prototype> prototypes, XLuaHook hook) {
        if(hook == null) {
            XLog.e("Hooks is NULL not compiling...", new Throwable(), true);
            return null;
        }

        InputStream is = null;
        try {
            LuaScriptHolder sh = new LuaScriptHolder(hook.getLuaScript());
            if (prototypes.containsKey(sh))
                return prototypes.get(sh);
            else {
                is = new ByteArrayInputStream(sh.script.getBytes());
                Prototype compiledScript = LuaC.instance.compile(is, "script");
                prototypes.put(sh, compiledScript);
                return compiledScript;
            }
        }catch (Exception e) {
            XLog.e("Error Compiling Hook Script. Hook=" + hook.getId(), e, true);
            return null;
        } finally {
            StreamUtil.close(is);
        }
    }

    public static ArrayList<XLuaHook> readHooks(String apk) {
        XLog.i("Reading all Hooks in JSON...");
        ZipFile zipFile = null;
        ArrayList<XLuaHook> hooks_all = new ArrayList<>();
        try {
            zipFile = new ZipFile(apk);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().startsWith("assets/") && entry.getName().endsWith("hooks.json")) {
                    XLog.i("Found Entry for [hooks.json]: " + entry.getName());
                    ArrayList<XLuaHook> read_hooks = readHooksFromEntry(entry, zipFile);
                    if(!read_hooks.isEmpty()) hooks_all.addAll(read_hooks);
                }
            }
        }catch (Exception e){ XLog.e("Failed to read Hooks.", e);
        } finally { StreamUtil.close(zipFile);
        } return hooks_all;
    }

    public static ArrayList<XLuaHook> readHooksFromEntry(ZipEntry entry, ZipFile zipFile) {
        XLog.i("Parsing Hooks From Entry [" + entry + "]");
        ArrayList<XLuaHook> hooks = new ArrayList<>();
        String entryName = entry.getName();
        String entryPath = entryName.substring(0, entryName.length() - 11);
        boolean isBuiltIn = entryName.endsWith("assets/hooks.json");
        XLog.i("Parsing Zip Entry [" + entryPath + "] Is Built In [" + isBuiltIn + "]");
        InputStream is = null;
        try {
            is = zipFile.getInputStream(entry);
            String json = new Scanner(is).useDelimiter("\\A").next();
            JSONArray jArray = new JSONArray(json);
            for(int i = 0; i < jArray.length(); i++) {
                XLuaHookAssets hookAsset = new XLuaHookAssets();
                hookAsset.fromJSONObject(jArray.getJSONObject(i));
                if (hookAsset.getLuaScript().startsWith("@")) {
                    hookAsset.setIsBuiltIn(isBuiltIn || hookAsset.isBuiltin());
                    String luaContents = getLuaScript(zipFile, entryPath, hookAsset.getLuaScript().substring(1) + ".lua");
                    if(luaContents == null) {
                        XLog.e("Failed to Init Hook: " + hookAsset.getId() + " Entry=" + entry, new Throwable(), true);
                        continue;
                    }
                    hookAsset.setLuaScript(luaContents);
                }
                hooks.add(hookAsset);
            }
        }catch (Exception e) {  XLog.e("Failed to Read Hooks from ZIP Entry=" + entry, e, true);
        }finally { StreamUtil.close(is);
        } return hooks;
    }

    public static String getLuaScript(ZipFile zipFile, String path, String scriptName) {
        String entryLua = path + "/" + scriptName;
        XLog.i("Searching for LUA Script in: " + entryLua + " path=" + path + " script name=" + scriptName);
        ZipEntry luaEntry = zipFile.getEntry(entryLua);
        if (luaEntry == null && !path.equals("assets")) return getLuaScript(zipFile, "assets", scriptName);
        else {
            InputStream lis = null;
            try {
                lis = zipFile.getInputStream(luaEntry);
                return new Scanner(lis).useDelimiter("\\A").next();
            } catch (Exception e) { XLog.e("Failed to find LUA Script: " + entryLua, e);
            }finally { StreamUtil.close(lis); }
        } return null;
    }

    public static String resolveClassName(Context context, String className) {
        if ("android.app.ActivityManager".equals(className)) {
            Object service = context.getSystemService(ActivityManager.class);
            if (service != null) return service.getClass().getName();

        } else if ("android.appwidget.AppWidgetManager".equals(className)) {
            Object service = context.getSystemService(AppWidgetManager.class);
            if (service != null) return service.getClass().getName();

        } else if ("android.media.AudioManager".equals(className)) {
            Object service = context.getSystemService(AudioManager.class);
            if (service != null) return service.getClass().getName();

        } else if ("android.hardware.camera2.CameraManager".equals(className)) {
            Object service = context.getSystemService(CameraManager.class);
            if (service != null) return service.getClass().getName();

        } else if ("android.content.ContentResolver".equals(className)) {
            return context.getContentResolver().getClass().getName();

        } else if ("android.content.pm.PackageManager".equals(className)) {
            return context.getPackageManager().getClass().getName();

        } else if ("android.hardware.SensorManager".equals(className)) {
            Object service = context.getSystemService(SensorManager.class);
            if (service != null) return service.getClass().getName();

        } else if ("android.telephony.SmsManager".equals(className)) {
            Object service = SmsManager.getDefault();
            if (service != null) return service.getClass().getName();

        } else if ("android.telephony.TelephonyManager".equals(className)) {
            Object service = context.getSystemService(Context.TELEPHONY_SERVICE);
            if (service != null) return service.getClass().getName();

        } else if ("android.net.wifi.WifiManager".equals(className)) {
            Object service = context.getSystemService(Context.WIFI_SERVICE);
            if (service != null) return service.getClass().getName();
        } return null;
    }
}
