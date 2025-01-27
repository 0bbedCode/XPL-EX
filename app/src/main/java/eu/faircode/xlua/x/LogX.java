package eu.faircode.xlua.x;

import android.text.TextUtils;
import android.util.Log;

import java.util.Collection;

import eu.faircode.xlua.logger.XReport;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.runtime.RuntimeUtils;

public class LogX {
    public static void d(String tag, String msg, boolean ensureNoDoubleNewLines) {
        if(ensureNoDoubleNewLines)
            Log.d(tag, Str.ensureNoDoubleNewLines(msg));
        else
            Log.d(tag, msg);
    }


    //public void test() {
    //    XReport.install(null, null, null);
    //}

    public static void dFS(String tag, String msg, Object... objects) {
        String formattedMessage = String.format(msg, objects);
        Log.d(tag, Str.ensureNoDoubleNewLines(Str.ensureIsNotNullOrDefault(formattedMessage, "null")));
    }

    public static String errorInput(Object... params) {
        StackTraceElement last = RuntimeUtils.getLast(new Throwable());

        StringBuilder sb = new StringBuilder();
        sb.append("Error bad Input Arguments function=[").append(RuntimeUtils.getMethodName(last)).append("] Check:");
        if(ArrayUtils.isValid(params)) {
            for(int i = 0; i < params.length; i++) {
                Object o = params[i];
                sb.append("\n  <").append(i).append(">:[").append("is null=").append(o == null ? "true" : "false").append("]");
                if(o instanceof String) {
                    String s = (String)o;
                    sb.append(":[str isEmpty=").append(TextUtils.isEmpty(s)).append("]:[str size=").append(s.length()).append("]");
                }else if(o instanceof Collection) {
                    sb.append(":[col size=").append(((Collection)o).size()).append("]");
                }
            }
        } else {
            sb.append("null");
        }

        String msg = sb.toString();
        //Log.e(tag, msg);
        return msg;
    }
}
