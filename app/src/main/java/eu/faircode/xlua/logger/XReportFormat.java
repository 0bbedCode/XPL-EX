package eu.faircode.xlua.logger;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Member;

public class XReportFormat {
    public static String exception(Exception exception, Context optionalContext) {
        return new StringBuilder()
                .append("Exception:\n")
                .append(exception == null ? "null" : Log.getStackTraceString(exception)).append("\n")
                .append("Package:\n")
                .append(optionalContext == null ? "null package" : optionalContext.getPackageName()).append(":")
                .append(optionalContext == null ? "0" : optionalContext.getApplicationInfo().uid).append("\n")
                .toString();
    }

    public static String field(Field field) {
        return new StringBuilder()
                .append("Field:\n")
                .append(field.toString()).append("\n")
                .toString();
    }

    public static String member(Member member, String function, Object[] args, Object result) {
        return new StringBuilder()
                .append("Method:\n")
                .append(function).append(" ").append(member.toString()).append("\n\n")
                .append("Arguments:\n")
                .append(args(args)).append("\n")
                .append("Return:\n")
                .append(result(result))
                .toString();
    }

    private static String args(Object[] args) {
        if(args == null || args.length < 1) return "null or empty";
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < args.length; i++) {
            sb.append(i).append(": ");
            if(args[i] == null)
                sb.append("null\n");
            else {
                sb.append(args[i].toString())
                        .append(" (")
                        .append(args[i].getClass().getName())
                        .append(")\n");
            }
        } return sb.toString();
    }

    private static String result(Object res) {
        if(res == null) return "null";
        return new StringBuilder()
                .append(res.toString()).append(" (")
                .append(res.getClass().getName()).append(")\n")
                .toString();
    }
}
