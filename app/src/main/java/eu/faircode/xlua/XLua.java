/*
    This file is part of XPrivacyLua.

    XPrivacyLua is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    XPrivacyLua is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with XPrivacyLua.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2017-2019 Marcel Bokhorst (M66B)
 */

package eu.faircode.xlua;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import org.luaj.vm2.Globals;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.Varargs;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import eu.faircode.xlua.api.properties.MockPropConversions;
import eu.faircode.xlua.api.xlua.XLuaCall;
import eu.faircode.xlua.api.xmock.XMockQuery;
import eu.faircode.xlua.api.xstandard.UserIdentityPacket;
import eu.faircode.xlua.hooks.XHookUtil;

//package eu.faircode.xlua;

import java.util.Collection;

import eu.faircode.xlua.api.xlua.XLuaQuery;
import eu.faircode.xlua.hooks.LuaHookWrapper;
import eu.faircode.xlua.hooks.LuaScriptHolder;
import eu.faircode.xlua.hooks.XReporter;
import eu.faircode.xlua.hooks.LuaHookResolver;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.logger.XReport;
import eu.faircode.xlua.random.GlobalRandoms;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.utilities.ReflectUtilEx;

public class XLua implements IXposedHookZygoteInit, IXposedHookLoadPackage {
    private static final String TAG = "XLua.XCoreStartup";
    public XReporter report = new XReporter();
    public static int version = -1;

    public void initZygote(final IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        Log.i(TAG, "initZygote system=" + startupParam.startsSystemServer + " debug=" + BuildConfig.DEBUG);
    }

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        int uid = Process.myUid();
        Log.i(TAG, "Loaded " + lpparam.packageName + ":" + uid);
        XposedBridge.log(TAG + " Loaded " + lpparam.packageName + ":" + uid);

        if ("android".equals(lpparam.packageName))
            hookAndroid(lpparam);

        if ("com.android.providers.settings".equals(lpparam.packageName))
            hookSettings(lpparam);

        if (!"android".equals(lpparam.packageName) &&
                !lpparam.packageName.startsWith(BuildConfig.APPLICATION_ID)) {
            hookApplication(lpparam);
        }
    }

    private void hookSettings(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // https://android.googlesource.com/platform/frameworks/base/+/master/packages/SettingsProvider/src/com/android/providers/settings/SettingsProvider.java
        Class<?> clsSet = Class.forName("com.android.providers.settings.SettingsProvider", false, lpparam.classLoader);
        XposedBridge.hookMethod(
                clsSet.getMethod("call", String.class, String.class, Bundle.class),
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        // call(String method, String name, Bundle args)
                        /*StringBuilder sb = new StringBuilder();
                        sb.append("CALLING UID: " + Binder.getCallingUid() + "\n");
                        try {
                            sb.append((String)param.args[0] + "\n");
                        }catch (Exception e) { }
                        try {
                            sb.append((String)param.args[1] + "\n");
                        }catch (Exception e) { }
                        try {
                            Bundle b = (Bundle) param.args[2];
                            for(String s : b.keySet()) {
                               sb.append("KEY=" + s + "\n");
                               try {
                                   sb.append("VALUE=" + b.getString(s) + "\n");
                               }catch (Exception ie) { }
                            }
                            //sb.append(b.toString());
                        }catch (Exception e) { }
                        Log.i(TAG, "CALL CALL CALL\n" + sb.toString());*/
                        //GET_secure
                        //android_id

                        //_track_generation
                        //We can target this!!
                        XCommandBridgeStatic.handeCall(param, lpparam.packageName);
                    }
                });

        // Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
        XposedBridge.hookMethod(
                clsSet.getMethod("query", Uri.class, String[].class, String.class, String[].class, String.class),
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        //query(Uri uri, String[] projection, String where, String[] whereArgs,
                        //            String order)
                        /*StringBuilder sb = new StringBuilder();
                        try {
                            Uri uri = (Uri) param.args[0];
                            sb.append("URI => " + uri.getAuthority() + "\n");
                        }catch (Exception e) { }
                        try {
                            String[] projection = (String[]) param.args[1];
                            sb.append("Projection => " +  Str.joinArray(projection) + "\n");
                        }catch (Exception e) { }
                        try {
                            String where = (String)param.args[2];
                            sb.append("Where => " + where + "\n");
                        }catch (Exception e) { }
                        try {
                            String[] whereArgs = (String[]) param.args[3];
                            sb.append("whereArgs => " + Str.joinArray(whereArgs) + "\n");
                        }catch (Exception e) { }
                        try {
                            String order = (String) param.args[4];
                            sb.append("Order => " + order);
                        }catch (Exception e) { }

                        Log.i(TAG, "QUERY QUERY\n" + sb.toString());*/
                        XCommandBridgeStatic.handleQuery(param, lpparam.packageName);
                    }
                });

        /*XposedBridge.hookMethod(
                clsSet.getMethod("getAllSecureSettings", Integer.class, String[].class) ,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        int uid = (int)param.args[0];
                        Log.i(TAG, "GETTING SETTINGS SECURE: " + uid);
                        super.beforeHookedMethod(param);
                    }
                });

        XposedBridge.hookMethod(
                clsSet.getMethod("getSecureSetting", String.class, int.class),
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String name = (String)param.args[0];
                        int uid = (int)param.args[1];
                        Log.i(TAG, "GET SETTING SECURE: " + uid + " > " + name);
                        super.beforeHookedMethod(param);
                    }
                });*/
    }

    private void hookAndroid(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // https://android.googlesource.com/platform/frameworks/base/+/master/services/core/java/com/android/server/am/ActivityManagerService.java
        Class<?> clsAM = Class.forName("com.android.server.am.ActivityManagerService", false, lpparam.classLoader);

        XposedBridge.hookAllMethods(clsAM, "systemReady", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    Log.i(TAG, "Preparing system");
                    XposedBridge.log(TAG + " Preparing system");
                    Context context = getContext(param.thisObject);
                    hookPackage(lpparam, Process.myUid(), context);

                } catch (Throwable ex) {
                    Log.e(TAG, Log.getStackTraceString(ex));
                    XposedBridge.log(ex);
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    Log.i(TAG, "System ready");
                    XposedBridge.log(TAG + " System ready");
                    Context context = getContext(param.thisObject);

                    // Store current module version
                    PackageInfo pi = context.getPackageManager().getPackageInfo(BuildConfig.APPLICATION_ID, 0);
                    version = pi.versionCode;
                    Log.i(TAG, "Module version " + version);

                    // public static UserManagerService getInstance()
                    Class<?> clsUM = Class.forName("com.android.server.pm.UserManagerService", false, param.thisObject.getClass().getClassLoader());
                    Object um = clsUM.getDeclaredMethod("getInstance").invoke(null);

                    //  public int[] getUserIds()
                    int[] userids = (int[]) um.getClass().getDeclaredMethod("getUserIds").invoke(um);

                    // Listen for package changes
                    for (int userid : userids) {
                        Log.i(TAG, "Registering package listener user=" + userid);
                        IntentFilter ifPackageAdd = new IntentFilter();
                        ifPackageAdd.addAction(Intent.ACTION_PACKAGE_ADDED);
                        ifPackageAdd.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
                        ifPackageAdd.addDataScheme("package");
                        XUtil.createContextForUser(context, userid).registerReceiver(new ReceiverPackage(), ifPackageAdd);
                    }
                } catch (Throwable ex) {
                    Log.e(TAG, Log.getStackTraceString(ex));
                    XposedBridge.log(ex);
                }
            }

            @NonNull
            private Context getContext(Object am) throws Throwable {
                // Searching for context
                Context context = null;
                Class<?> cAm = am.getClass();
                while (cAm != null && context == null) {
                    for (Field field : cAm.getDeclaredFields())
                        if (field.getType().equals(Context.class)) {
                            field.setAccessible(true);
                            context = (Context) field.get(am);
                            Log.i(TAG, "Context found in " + cAm + " as " + field.getName());
                            break;
                        }
                    cAm = cAm.getSuperclass();
                }
                if (context == null)
                    throw new Throwable("Context not found");

                return context;
            }
        });
    }

    public void hookApplication(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        final int uid = android.os.Process.myUid();
        final boolean tiramisuOrHigher = (Build.VERSION.SDK_INT >= 33);
        // https://android.googlesource.com/platform/frameworks/base/+/169aeafb2d97b810ae123ad036d0c58336961c55%5E%21/#F1
        Class<?> at = Class.forName(tiramisuOrHigher ? "android.app.Instrumentation" : "android.app.LoadedApk", false, lpparam.classLoader);

        XposedBridge.hookAllMethods(at,
                tiramisuOrHigher ? "newApplication" : "makeApplication", new XC_MethodHook() {
                    private boolean made = false;

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            if(!made) {
                                made = true;
                                Context context = (Application) param.getResult();

                                //Check for isolate process
                                int userid = XUtil.getUserId(uid);
                                int start = XUtil.getUserUid(userid, 99000);
                                int end = XUtil.getUserUid(userid, 99999);
                                boolean isolated = (uid >= start && uid <= end);
                                if (isolated) {
                                    Log.i(TAG, "Skipping isolated " + lpparam.packageName + ":" + uid);
                                    return;
                                }

                                hookPackage(lpparam, uid, context);
                            }
                        }catch (Throwable ex) {
                            Log.e(TAG, Log.getStackTraceString(ex));
                            XposedBridge.log(ex);
                        }
                    }
                });
    }

    public void hookPackage(final XC_LoadPackage.LoadPackageParam lpparam, int uid, final Context context) throws Throwable {
        final String pName = lpparam.packageName;
        final String key = UUID.randomUUID().toString();
        XLog.i("pkg [" + pName + "] key [" + key + "]");

        Collection<XLuaHook> hooks = XLuaQuery.getAssignments(context, pName, uid, true);
        final boolean useDefault = XLuaCall.getSettingBoolean(context, UserIdentityPacket.GLOBAL_USER, pName, "useDefault");

        XLog.i("pkg [" + pName + "] uid [" + uid + "] hook size [" + hooks.size() + "]");

        final Map<String, String> settings = XLuaQuery.getGlobalSettings(context, uid);
        settings.putAll(XLuaQuery.getSettings(context, uid, pName, true));
        GlobalRandoms.bindRandomToSettings(settings);

        final Map<String, Integer> propSettings = MockPropConversions.toMap(XMockQuery.getModifiedProperties(context, uid, pName));
        final Map<String, String> propMaps = XMockQuery.getMockPropMapsMap(context, true, settings, false);

        XLog.i("pkg [" + pName + "] settings size [" + settings.size() + "] properties size [" + propSettings.size() + "] prop maps size [" + propMaps.size() + "]");

        Map<LuaScriptHolder, Prototype> scriptPrototype = new HashMap<>();
        PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        for(final XLuaHook hook : hooks) {
            try {
                if(!hook.isAvailable(pInfo.versionCode)) {
                    XLog.w("Hook is not compatible with Target SDK: " + hook.getId());
                    continue;
                }

                //get time & Compile Script
                final long install = SystemClock.elapsedRealtime();
                final Prototype compiledScript = XHookUtil.compileScript(scriptPrototype, hook);
                final LuaHookResolver target = XHookUtil.resolveTargetHook(context, hook);

                XLog.i("Created Target Hook: " + target);
                if(target.isField()) {
                    final Field field = target.tryGetAsField(true);
                    if(field != null) {
                        try {
                            if (target.paramTypes.length > 0)  throw new NoSuchFieldException("Field with parameters");
                            long run = SystemClock.elapsedRealtime();
                            LuaHookWrapper luaField = LuaHookWrapper.createField(
                                    context,
                                    hook,
                                    settings,
                                    propSettings,
                                    propMaps,
                                    compiledScript,
                                    field,
                                    key,
                                    useDefault,
                                    pName);

                            if(!luaField.isValid()) {
                                XLog.w("Skipping over Field: [" + field.getName() + "] Its not a After Function" );
                                continue;
                            }

                            Varargs result = luaField.invoke();
                            XReport.usage(hook, result, run, XReport.FUNCTION_AFTER, context);
                        }catch (Exception e) {
                            XReport.fieldException(context, e, hook, field);
                        }
                    }
                }else {
                    final Member member = target.tryGetAsMember();
                    if(member != null) {
                        XLog.w("Member Method Name=" + member.getName());
                        if(target.hasMismatchReturn(member)) {
                            XLog.e("Invalid Return Type for Hook: " + hook.getId());
                            continue;
                        }

                        XposedBridge.hookMethod(member, new XC_MethodHook() {
                            private final WeakHashMap<Thread, Globals> threadGlobals = new WeakHashMap<>();

                            @Override
                            protected void beforeHookedMethod(MethodHookParam param)  {
                                execute(param, "before");
                            }

                            @Override
                            protected void afterHookedMethod(MethodHookParam param)  {
                                execute(param, "after");
                            }

                            private void execute(MethodHookParam param, String function) {
                                long run = SystemClock.elapsedRealtime();
                                try {
                                    LuaHookWrapper luaMember;
                                    synchronized (threadGlobals) {
                                        Thread thread = Thread.currentThread();
                                        if (!threadGlobals.containsKey(thread))
                                            threadGlobals.put(thread, XHookUtil.getHookGlobals(
                                                    context,
                                                    hook,
                                                    settings,
                                                    propSettings,
                                                    propMaps,
                                                    key,
                                                    useDefault,
                                                    pName));

                                        Globals globals = threadGlobals.get(thread);
                                        luaMember = LuaHookWrapper
                                                .createMember(
                                                        context,
                                                        hook,
                                                        settings,
                                                        propSettings,
                                                        propMaps,
                                                        compiledScript,
                                                        function,
                                                        param,
                                                        globals,
                                                        key,
                                                        useDefault,
                                                        pName);

                                        if(!luaMember.isValid()) {
                                            if(BuildConfig.DEBUG)
                                                XLog.w("Lua Member is Not Valid [" + target.methodName + "] Most likely not a after or before : now a:" + function);
                                            return;
                                        }
                                    }

                                    Varargs result = luaMember.invoke();
                                    XReport.usage(hook, result, run, function, context);
                                }catch (Exception ex) {
                                    synchronized (threadGlobals) { threadGlobals.remove(Thread.currentThread()); }
                                    XReport.memberException(context, ex, hook, member, function, param);
                                }
                            }
                        });
                    }else XLog.e("Member is NULL. hook=" + hook.getName() + " id=" + hook.getId(), new Throwable(), true);
                }
                if (BuildConfig.DEBUG) XReport.install(hook, install, context);
            }catch (Throwable fe) {
                if (hook.isOptional() && ReflectUtilEx.isReflectError(fe)) XLog.e("Optional Hook=" + hook.getId() + " class=" + fe.getClass().getName(), fe, true);
                else XReport.installException(hook, fe, context);
            }
        }
    }
}
