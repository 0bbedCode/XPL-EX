package eu.faircode.xlua.x.runtime;
import android.util.Log;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.process.ProcessUtils;
import eu.faircode.xlua.x.runtime.reflect.DynamicMethod;
import eu.faircode.xlua.x.runtime.reflect.ReflectUtil;
import eu.faircode.xlua.x.xlua.LibUtil;

public class RuntimeUtils {
    private static final String TAG = LibUtil.generateTag(RuntimeUtils.class);

    private static final DynamicMethod nativeFillInStackTrace;
    private static final DynamicMethod nativeGetStackTrace;
    private static final DynamicMethod internalGetStackTrace;
    static {
        HiddenApi.bypassHiddenApiRestrictions();
        nativeFillInStackTrace = new DynamicMethod(Throwable.class, "nativeFillInStackTrace").setAccessible(true);
        nativeGetStackTrace = new DynamicMethod(Throwable.class, "nativeGetStackTrace", Object.class).setAccessible(true);
        internalGetStackTrace = new DynamicMethod(ReflectUtil.tryGetMethodEx(Throwable.class, "getOurStackTrace", "getInternalStackTrace")).setAccessible(true);
    }

    //Try look for IXposedHookLoadPackage ?
    //Or for me com.obbedcode.xplex.hook.XposedEntry
    public static boolean stackTraceContains(String className, String methodName, boolean useOr) {
        StackTraceElement[] elements = getStackTraceSafe();
        if(elements == null)
            return false;

        //Copied pasted to even reduce time by skipping if checks but doing if checks once
        String cComp = className != null ? className.toLowerCase() : null;
        String mComp = methodName != null ? methodName.toLowerCase() : null;
        boolean doClass = cComp != null;
        boolean doMethod = mComp != null;
        if(doClass && doMethod) {
            if(useOr) {
                for(int i = 0; i < elements.length; i++) {
                    StackTraceElement e = elements[i];
                    String c = e.getClassName().toLowerCase();
                    String m = e.getMethodName().toLowerCase();
                    if(c.contains(cComp) || m.contains(mComp))
                        return true;
                }
            }else {
                for(int i = 0; i < elements.length; i++) {
                    StackTraceElement e = elements[i];
                    String c = e.getClassName().toLowerCase();
                    String m = e.getMethodName().toLowerCase();
                    if(c.contains(cComp) && m.contains(mComp))
                        return true;
                }
            }
        } else {
            if(doClass) {
                for(int i = 0; i < elements.length; i++) {
                    StackTraceElement e = elements[i];
                    String c = e.getClassName().toLowerCase();
                    if(c.contains(cComp))
                        return true;
                }
            } else if(doMethod) {
                for(int i = 0; i < elements.length; i++) {
                    StackTraceElement e = elements[i];
                    String m = e.getMethodName().toLowerCase();
                    if(m.contains(mComp))
                        return true;
                }
            }
        } return false;
    }

    public static String getStackTraceSafeString() { return getStackTraceSafeString(null); }
    public static String getStackTraceSafeString(Throwable thr) {
        StackTraceElement[] elements = getStackTraceSafe(thr);
        StringBuilder sb = new StringBuilder();
        if(elements != null) {
            for (int i = 0; i < elements.length; i++) {
                StackTraceElement e = elements[i];
                sb.append(" >> ").append(e.getClassName()).append(":").append(e.getMethodName()).append("(").append(e.getFileName()).append(":").append(e.getLineNumber()).append(")").append("\n");
            }
        } return sb.toString();
    }

    /*
        When no Throwable given:
            java.lang.reflect.Method.invoke
            eu.faircode.xlua.x.runtime.reflect.DynamicMethod:tryStaticInvoke
            eu.faircode.xlua.x.runtime.RuntimeUtils:getStackTraceSafe()
            eu.faircode.xlua.x.runtime.RuntimeUtils:getStackTraceSafeString()
            eu.faircode.xlua.x.runtime.RuntimeUtils:getStackTraceSafeString()
     */

    public static String getMethodName(StackTraceElement element) { return element == null ? "null" : element.getMethodName(); }
    public static String getClassName(StackTraceElement element) { return element == null ? "null" : element.getClassName(); }

    public static StackTraceElement getLast(Throwable thr) {
        StackTraceElement[] elements = getStackTraceSafe(thr);
        if(elements == null || elements.length == 0)
            return null;

        if(elements.length == 1)
            return elements[0];

        return elements[1];
    }


    public static StackTraceElement[] filterTrace(StackTraceElement[] stack) {
        if(!ArrayUtils.isValid(stack))
            return stack;

        List<StackTraceElement> toList = Arrays.asList(stack);
        String targetClassName = RuntimeUtils.class.getName();

        int sz = toList.size();
        int lastIndex = -1;
        for (int i = 0; i < stack.length; i++) {
            if (stack[i].getClassName().equalsIgnoreCase(targetClassName)) {
                lastIndex = i;
            }
        }

        if (lastIndex == -1 || lastIndex >= stack.length - 1)
            return stack;

        List<StackTraceElement> rest = toList.subList(lastIndex + 1, sz);
        return ArrayUtils.toArray(rest, StackTraceElement.class);
    }


    public static StackTraceElement[] getStackTraceSafe() { return getStackTraceSafe(null); }
    public static StackTraceElement[] getStackTraceSafe(Throwable thr) {
        if(thr == null) {
            thr = new Throwable();
            StackTraceElement[] elements = internalGetStackTraceSafe(thr);
            return filterTrace(elements);
        }

        return internalGetStackTraceSafe(thr);
    }


    public static StackTraceElement[] internalGetStackTraceSafe(Throwable thr) {
        //https://android.googlesource.com/platform/prebuilts/fullsdk/sources/android-29/+/refs/heads/androidx-core-release/java/lang/Throwable.java
        //https://cs.android.com/android/platform/superproject/main/+/main:libcore/ojluni/src/main/java/java/lang/Throwable.java
        //https://github.com/xdtianyu/android-6.0.0_r1/blob/master/libcore/luni/src/main/java/java/lang/Throwable.java




        if(!HiddenApi.bypassHiddenApiRestrictions()) {
            Log.e(TAG, "Failed to bypass Hidden API Restrictions, used for [getStackTraceSafe]. Using fall back generic API.");
            return thr == null ? new Throwable().getStackTrace() : thr.getStackTrace();
        }

        if(thr != null) {
            if(internalGetStackTrace.isValid()) {
                StackTraceElement[] elements = internalGetStackTrace.tryInstanceInvokeEx(thr);
                if(elements != null && elements.length > 0)
                    return elements;
                else {
                    Log.e(TAG, "Failed to Get Stack Trace Custom Instance, it returned 0 or Null...");
                }
            } else {
                Log.e(TAG, "Failed to Get Stack Trace Custom Instance, seems Instance method is Null. Is Valid: " + internalGetStackTrace.isValid());
            }
            return thr.getStackTrace();
        } else {
            /* This is a Cool manual approach but I mean just pass in a Throwable ? */
            if(nativeFillInStackTrace.isValid() && nativeGetStackTrace.isValid()) {
                Object backTrace = nativeFillInStackTrace.tryStaticInvoke();
                if(backTrace != null) {
                    StackTraceElement[] elements = nativeGetStackTrace.tryStaticInvoke(backTrace);
                    if(elements != null && elements.length > 0)
                        return elements;
                    else {
                        Log.e(TAG, "Failed to Get Stack Trace Custom Static, it returned 0 or Null...");
                    }
                }
            } else {
                Log.e(TAG, "Failed to Get Stack Trace Custom Static, one of the Methods is Invalid. [nativeFillInStackTrace] is valid: " + nativeFillInStackTrace.isValid() + " [nativeGetStackTrace] is valid: " + nativeGetStackTrace.isValid());
            }
            return getStackTraceSafe(new Throwable());
        }
    }

    public static String getProperty(String propName) {
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            return ProcessUtils.getProcessOutput(p);
        }catch (Exception e) {
            Log.e(TAG, "Error getting prop:" + propName);
            return null;
        }
    }

    public static String executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuilder output = new StringBuilder();
            String line;

            // Read standard output
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Read error output
            while ((line = errorReader.readLine()) != null) {
                output.append("Error: ").append(line).append("\n");
            }

            // Wait for process to complete
            process.waitFor();

            return output.toString().trim();

        } catch (IOException | InterruptedException e) {
            return "Error executing command: " + e.getMessage();
        }
    }

    public static String executeCommand(String... args) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(args);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuilder output = new StringBuilder();
            String line;

            // Read standard output
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Read error output
            while ((line = errorReader.readLine()) != null) {
                output.append("Error: ").append(line).append("\n");
            }

            // Wait for process to complete
            process.waitFor();

            return output.toString().trim();

        } catch (IOException | InterruptedException e) {
            return "Error executing command: " + e.getMessage();
        }
    }
}
