package eu.faircode.xlua.logger;

import android.util.Log;

import androidx.annotation.NonNull;

import eu.faircode.xlua.utilities.CollectionUtil;
import eu.faircode.xlua.utilities.StringUtil;

public class XBasicLog {
    private static final String END_CLASS_LOG = ".XLog";
    private static final String END_CLASS = ".XBasicLog";

    private static final String CLASS_TAG = "XLua.XLog";

    public String tag;
    public String message;

    public static XBasicLog create(
            String tag,
            String msg,
            Throwable exception,
            String className,
            String methodName,
            boolean dumpStack,
            boolean getLastCall) {

        StringBuilder sb = new StringBuilder();
        StackTraceElement[] els = dumpStack ? getTrace(exception) : null;
        StackTraceElement last = ((className == null || methodName == null) && getLastCall) ? getCaller(els) : null;

        if(getLastCall || methodName != null || className != null) {
            String c = className == null && last != null ? last.getClassName() : className;
            String m = methodName == null && last != null ? last.getMethodName() : methodName;
            if(c != null) sb.append("Class=").append(c).append("\n");
            if(m != null) sb.append("Method=").append(m).append("\n");
        }

        if(msg != null) sb.append("Message=").append(msg).append("\n");
        if(exception != null) sb.append("Exception=").append(exception.getMessage()).append("\n");
        if(dumpStack) {
            sb.append("Stack=\n");
            for (int i = 0; i < els.length; i++) {
                StackTraceElement e = els[i];
                sb.append(" >> ").append(e.getClassName()).append(":").append(e.getMethodName()).append("(").append(e.getFileName()).append(":").append(e.getLineNumber()).append(")").append("\n");
                //sb.append("[C][").append(e.getClassName()).append("][M][").append(e.getMethodName()).append("\n");
            }
        }

        if(tag == null) tag = last == null ? CLASS_TAG : "XLua." + StringUtil.getLastString(last.getClassName(), ".", CLASS_TAG);

        XBasicLog log = new XBasicLog();
        log.message = sb.toString();
        log.tag = tag;
        return log;
    }

    public static StackTraceElement[] getTrace(Throwable thr) {
        if(thr == null) return Thread.currentThread().getStackTrace();
        StackTraceElement[] els = thr.getStackTrace();
        return els.length > 0 ? els : Thread.currentThread().getStackTrace();
    }

    public static StackTraceElement getCaller(StackTraceElement[] elements) {
        StackTraceElement[] els = elements == null ? Thread.currentThread().getStackTrace() : elements;
        //for (int i = els.length - 1; i >= 0; i--) {
        for(int i = 0; i < els.length; i++) {
            StackTraceElement el = els[i];
            String c = el.getClassName();
            if(c.equalsIgnoreCase("java.lang.Thread"))
                continue;

            if(c.equalsIgnoreCase("dalvik.system.VMStack"))
                continue;

            if(c.endsWith(END_CLASS) || c.endsWith(END_CLASS_LOG))
                continue;



            if(!c.equalsIgnoreCase("java.lang.Thread") && !c.endsWith(END_CLASS) && !c.equals(END_CLASS_LOG)) {
                return el;
            }
        }

        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return new StringBuilder()
                .append("[").append(tag).append("]\n")
                .append(message)
                .toString();
    }
}
