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
import java.util.WeakHashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.hook.filter.HookRepository;
import eu.faircode.xlua.hooks.XHookUtil;

//package eu.faircode.xlua;

import java.util.Collection;

import eu.faircode.xlua.hooks.LuaHookWrapper;
import eu.faircode.xlua.hooks.LuaScriptHolder;
import eu.faircode.xlua.hooks.XReporter;
import eu.faircode.xlua.hooks.LuaHookResolver;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.logger.XReport;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.utilities.ReflectUtilEx;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.GlobalCommandBridge;
import eu.faircode.xlua.x.xlua.commands.call.GetBridgeVersionCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetAssignedHooksExCommand;
import eu.faircode.xlua.x.xlua.hook.PackageHookContext;

/*
    ToDO:
        [>] Add the remaining remapped Entries
        [>] Ensure use Proper abstraction, Returning Collection is useless as Caller function Can cast to get it as Collection so have it return something like list
        [>] When doing "toString" function ensure the "appendFieldLine" takes in the Constant for the Field Name if available
        [>] Make a System that for one compresses more of the fragment bullshit
        [>] Make a Application shared system
        [>] Clean up shared
        [>] Make a System for like "onDataEvent(Object o)"
 */

public class XLua implements IXposedHookZygoteInit, IXposedHookLoadPackage {
    private static final String TAG = LibUtil.generateTag(XLua.class);
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
        GetBridgeVersionCommand.init();
        // https://android.googlesource.com/platform/frameworks/base/+/master/packages/SettingsProvider/src/com/android/providers/settings/SettingsProvider.java
        Class<?> clsSet = Class.forName("com.android.providers.settings.SettingsProvider", false, lpparam.classLoader);
        XposedBridge.hookMethod(
                clsSet.getMethod("call", String.class, String.class, Bundle.class),
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        GlobalCommandBridge.handeCall(param, lpparam.packageName);
                    }
                });

        // Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
        XposedBridge.hookMethod(
                clsSet.getMethod("query", Uri.class, String[].class, String.class, String[].class, String.class),
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        GlobalCommandBridge.handleQuery(param, lpparam.packageName);
                    }
                });
    }


    private void hookAndroid(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // https://android.googlesource.com/platform/frameworks/base/+/master/services/core/java/com/android/server/am/ActivityManagerService.java
        Class<?> clsAM = Class.forName("com.android.server.am.ActivityManagerService", false, lpparam.classLoader);
        XposedBridge.hookAllMethods(clsAM, "systemReady", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    XposedBridge.log(TAG + " Preparing system");
                    Context context = getContext(param.thisObject);
                    GetBridgeVersionCommand.init();
                    hookPackage(lpparam, Process.myUid(), context);
                } catch (Throwable ex) {
                    Log.e(TAG, Log.getStackTraceString(ex));
                    XposedBridge.log(ex);
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    XposedBridge.log(TAG + " System ready");
                    Context context = getContext(param.thisObject);

                    // Store current module version
                    PackageInfo pi = context.getPackageManager().getPackageInfo(BuildConfig.APPLICATION_ID, 0);
                    version = pi.versionCode;

                    // public static UserManagerService getInstance()
                    Class<?> clsUM = Class.forName("com.android.server.pm.UserManagerService", false, param.thisObject.getClass().getClassLoader());
                    Object um = clsUM.getDeclaredMethod("getInstance").invoke(null);
                    //  public int[] getUserIds()
                    int[] userids = (int[]) um.getClass().getDeclaredMethod("getUserIds").invoke(um);

                    // Listen for package changes
                    for (int userid : userids) {
                        //Log.i(TAG, "Registering package listener user=" + userid);
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
                            if(DebugUtil.isDebug())
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
        PackageHookContext app = PackageHookContext.create(lpparam, uid, context);
        if(DebugUtil.isDebug())
            Log.d(TAG, "Hook Package created App Context=" + app);

        Collection<XLuaHook> hooks = GetAssignedHooksExCommand.get(context, true, uid, lpparam.packageName);
        if(DebugUtil.isDebug())
            Log.d(TAG, "Hook Package got Assigned Hooks, Size=" + ListUtil.size(hooks));

        //String version = GetBridgeVersionCommand.get(context);
        //if(DebugUtil.isDebug())
        //    Log.d(TAG, "Command Bridge Version=" + version + " App Version=" + BuildConfig.VERSION_NAME);

        Map<LuaScriptHolder, Prototype> scriptPrototype = new HashMap<>();
        PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        for(final XLuaHook hook : HookRepository.create().initializeHooks(context, hooks, app.settings).getHooks()) {
            try {
                if(!hook.isAvailable(pInfo.versionCode)) {
                    Log.w(TAG, "Hook is not compatible with Target SDK: " + hook.getObjectId());
                    continue;
                }

                //get time & Compile Script
                final long install = SystemClock.elapsedRealtime();
                final Prototype compiledScript = XHookUtil.compileScript(scriptPrototype, hook);
                final LuaHookResolver target = XHookUtil.resolveTargetHook(context, hook);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Created Target Hook=" + target);

                if(target.isField()) {
                    final Field field = target.tryGetAsField(true);
                    if(field != null) {
                        try {
                            if (target.paramTypes.length > 0)  throw new NoSuchFieldException("Field with parameters");
                            long run = SystemClock.elapsedRealtime();
                            LuaHookWrapper luaField = LuaHookWrapper.createField(
                                    context,
                                    hook,
                                    app.settings,
                                    app.buildPropSettings,
                                    app.buildPropMaps,
                                    compiledScript,
                                    field,
                                    app.temporaryKey,
                                    app.useDefault,
                                    app.packageName);

                            if(!luaField.isValid()) {
                                Log.e(TAG, Str.fm("Skipping Field Hook Field: %s is not a After Hook, Field Hooks can not be before Hooks!", field.getName()));
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
                        if(DebugUtil.isDebug())
                            Log.d(TAG, "Hook Member Name=" + member.getName());

                        if(target.hasMismatchReturn(member)) {
                            Log.e(TAG, Str.fm("Hook has an Invalid Return Type, Hook id=%s Return Type=%s", hook.getObjectId(), hook.getReturnType()));
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
                                                    app.settings,
                                                    app.buildPropSettings,
                                                    app.buildPropMaps,
                                                    app.temporaryKey,
                                                    app.useDefault,
                                                    app.packageName));

                                        Globals globals = threadGlobals.get(thread);
                                        luaMember = LuaHookWrapper
                                                .createMember(
                                                        context,
                                                        hook,
                                                        app.settings,
                                                        app.buildPropSettings,
                                                        app.buildPropMaps,
                                                        compiledScript,
                                                        function,
                                                        param,
                                                        globals,
                                                        app.temporaryKey,
                                                        app.useDefault,
                                                        app.packageName);

                                        if(!luaMember.isValid()) {
                                            if(BuildConfig.DEBUG)
                                                Log.w(TAG, Str.fm("Lua Member is Not Valid [%s] Most likely not a after or before, function=%s", target.methodName, function));
                                            return;
                                        }
                                    }

                                    Varargs result = luaMember.invoke();
                                    //Log.d(TAG, "Result=" + Str.toStringOrNull(result));
                                    XReport.usage(hook, result, run, function, context);
                                }catch (Exception ex) {
                                    synchronized (threadGlobals) {
                                        threadGlobals.remove(Thread.currentThread());
                                    }
                                    XReport.memberException(context, ex, hook, member, function, param);
                                }
                            }
                        });
                    }
                    else
                        Log.e(TAG, Str.fm("Member is NULL, Hook Name=%s Id=%s", hook.getName(), hook.getObjectId()));
                }


                //LogX.dFS();
                //if(DebugUtil.isDebug())


                //XReport.install();
                if (BuildConfig.DEBUG)
                    XReport.install(hook, install, context);

            }catch (Throwable fe) {
                if (hook.isOptional() && ReflectUtilEx.isReflectError(fe))
                    XLog.e("Optional Hook=" + hook.getObjectId() + " class=" + fe.getClass().getName(), fe, true);
                else
                    XReport.installException(hook, fe, context);
            }
        }
    }
}
