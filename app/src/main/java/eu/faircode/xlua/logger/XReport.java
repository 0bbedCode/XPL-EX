package eu.faircode.xlua.logger;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.Varargs;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import de.robv.android.xposed.XC_MethodHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.builders.SimpleReport;
import eu.faircode.xlua.builders.SimpleReportData;
import eu.faircode.xlua.x.xlua.hook.AssignmentLegacy;

public class XReport {
    public static final String EVENT_USE = "use";
    public static final String EVENT_INSTALL = "install";
    public static final String FUNCTION_AFTER = "after";
    public static final String FUNCTION_NONE = "none";
    public static final String COMMAND_METHOD = "report";

    private static final Map<String, Map<String, Bundle>> queue = new HashMap<String, Map<String, Bundle>>();
    private static Timer timer = null;

    public static void fieldException(Context context, Exception exception, XLuaHook hook, Field field) { fieldException(context, exception, hook, field, true); }
    public static void fieldException(Context context, Exception exception, XLuaHook hook, Field field, boolean log) {
        if(log) XLog.e("Field Hook Exception", exception, true);
        exception(hook,
                Str.combine(XReportFormat.exception(exception, context), XReportFormat.field(field)),
                FUNCTION_AFTER, context);
    }

    public static void memberException(Context context, Exception exception, XLuaHook hook, Member member, String function, XC_MethodHook.MethodHookParam param) { memberException(context, exception, hook, member, function, param.args, param.getResult()); }
    public static void memberException(Context context, Exception exception, XLuaHook hook, Member member, String function, XC_MethodHook.MethodHookParam param, boolean log) { memberException(context, exception, hook, member, function, param.args, param.getResult(), log); }

    public static void memberException(Context context, Exception exception, XLuaHook hook, Member member, String function, Object[] args, Object result) { memberException(context, exception, hook, member, function, args, result, true); }
    public static void memberException(Context context, Exception exception, XLuaHook hook, Member member, String function, Object[] args, Object result, boolean log) {
        if(log) XLog.e("Member Hook Exception", (exception == null ? "null error" : exception) + " Hook ID=" + hook.getId() + " Class=" + hook.getClassName() + " Method=" + hook.getMethodName() + " Collection=" + hook.getCollection() + " Group=" + hook.getGroup(), true);
        exception(hook,
                Str.combine(XReportFormat.exception(exception, context), XReportFormat.member(member, function,  args, result)),
                function, context);
    }

    public static void exception(XLuaHook hook, String message, String function, final Context context) {
        SimpleReportData data = new SimpleReportData();
        data.function = function;
        data.exception = message;
        push(hook, EVENT_USE, data, context);
    }

    public static void usage(XLuaHook hook, Varargs result, long startTime, String function, Context context) {
        if (!(result.arg1().checkboolean() && hook.doUsage())) return;
        SimpleReportData data = new SimpleReportData();
        data.function = function;
        data.restricted = 1;
        data.duration = SystemClock.elapsedRealtime() - startTime;
        if (result.narg() > 1) {
            data.old = result.isnil(2) ? null : result.checkjstring(2);
            data.nNew = result.isnil(3) ? null : result.checkjstring(3);
        } push(hook, EVENT_USE, data, context);
    }

    public static void installException(XLuaHook hook, Throwable exception, Context context) { installException(hook, exception, context, true); }
    public static void installException(XLuaHook hook, Throwable exception, Context context, boolean log) {
        if(log) XLog.e("Hook Install Exception", exception, true);
        SimpleReportData data = new SimpleReportData();
        if(exception != null)
            data.exception = exception instanceof LuaError ? exception.getMessage() : Log.getStackTraceString(exception);

        data.function = FUNCTION_NONE;
        push(hook, EVENT_INSTALL, data, context);
    }

    public static void install(XLuaHook hook, long startInstallTime, Context context) {
        SimpleReportData data = new SimpleReportData();
        data.duration = SystemClock.elapsedRealtime() - startInstallTime;
        data.function = FUNCTION_NONE;
        push(hook, EVENT_INSTALL, data, context);
    }

    public static void push(XLuaHook hook, String event, SimpleReportData data, Context context) {
        SimpleReport report = new SimpleReport();
        report.hook = hook.getId();
        report.packageName = context.getPackageName();
        report.uid = context.getApplicationInfo().uid;
        report.event = event;
        report.time = new Date().getTime();
        report.data = data;
        push(report, event, context);
    }

    public static void push(SimpleReport report, String event, final Context context) {
        try {
            synchronized (queue) {
                String key = (report.data.function == null ? Str.ASTERISK : report.data.function) + Str.COLLEN + event;
                if (!queue.containsKey(key))
                    queue.put(key, new HashMap<String, Bundle>());

                Objects.requireNonNull(queue.get(key)).put(report.hook, report.toBundle());
                if (timer == null) {
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        public void run() {
                            List<Bundle> work = new ArrayList<>();
                            synchronized (queue) {
                                for (String key : queue.keySet())
                                    for (String hook : Objects.requireNonNull(queue.get(key)).keySet())
                                        work.add(Objects.requireNonNull(queue.get(key)).get(hook));
                                queue.clear();
                                timer = null;
                            }

                            for (Bundle args : work)
                                XProxyContent.luaCall(context, COMMAND_METHOD, args);
                        }
                    }, 1000);
                }
            }
        }catch (Exception e) {
            XLog.e("Failed to Push Report Message to Database!", e, true);
        }
    }
}
