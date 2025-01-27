package eu.faircode.xlua.x.data.string;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.runtime.RuntimeUtils;

import java.util.Map;
import java.util.regex.Pattern;

public class StrBuilder {
    private static final Pattern DOUBLE_NEWLINE_PATTERN = Pattern.compile("\n\\s*\n");
    private static final String DIVIDER_DEFAULT = "------------------------";

    //Not really chars but I like the name
    public static final String STR_NEW_LINE = "\n";
    public static final String STR_SPACE = " ";
    public static final String STR_TAB = "\t";
    public static final String STR_COMMA = ",";
    public static final String STR_COLLEN = ":";

    public static StrBuilder create() { return new StrBuilder(); }
    public static StrBuilder create(int capacity) { return new StrBuilder(capacity); }
    public static StrBuilder create(String str) { return new StrBuilder(str); }
    public static StrBuilder create(String str, boolean newLine) { return new StrBuilder(str, newLine); }

    private StringBuilder mSb;
    private boolean ensureNoMoreThanOneNewLine = false;
    private String mDivider;
    private boolean mDoAppend = true;

    public StrBuilder() { mSb = new StringBuilder(); }
    public StrBuilder(int capacity) { mSb = new StringBuilder(capacity); }
    public StrBuilder(String str) { mSb = new StringBuilder(str); }
    public StrBuilder(String str, boolean newLine) {
        mSb = new StringBuilder(str);
        if(newLine) mSb.append(STR_NEW_LINE);
    }

    public StringBuilder getInternalBuilder() { return mSb; }

    private void ensureNoDoubleNewLine() {
        if(ensureNoMoreThanOneNewLine) {
            int len = mSb.length();
            if(len > 2) {
                char one = mSb.charAt(len - 1);
                char two = mSb.charAt(len - 2);
                //White space check ???
                if(one == '\n' && two == '\n') {
                    String cleaned = Str.trim(mSb.toString(), "\n", true, true).replaceAll("\n\n", "");
                    mSb = new StringBuilder(cleaned);
                    mSb.append("\n");   //Just one new Line
                }
            }
        }
    }

    public boolean endsWithCommand() {
        int sz = mSb.length();
        if(sz < 1) return false;
        return mSb.charAt(sz - 1) == ',';
    }

    public boolean isBlockedFromAppending() { return !mDoAppend; }
    public StrBuilder setDoAppendFlag(boolean flag) {  mDoAppend = flag; return this; }
    public StrBuilder resetDoAppendFlag() { mDoAppend = true; return this; }

    public String getDivider() { return TextUtils.isEmpty(mDivider) ? DIVIDER_DEFAULT : mDivider; }
    public char getDividerChar() { return TextUtils.isEmpty(mDivider) ? '-' : mDivider.charAt(0); }

    public StrBuilder setDividerChar(char c) {
        this.mDivider = Str.repeatChar(c, Str.MOBILE_SAFE_LENGTH);
        return this;
    }

    public StrBuilder setDivider(String divider) {
        this.mDivider = divider;
        return this;
    }

    public StrBuilder appendDividerTitleLine(String title) {
        appendLine(Str.dividerWithTitle(getDividerChar(), title));
        return this;
    }

    public StrBuilder appendDividerLine() {
        appendLine(mDivider);
        return this;
    }

    public StrBuilder appendDivider() {
        append(mDivider);
        return this;
    }

    public StrBuilder ensureOneNewLinePer(boolean ensureOnlyOneNewLinePerLine) {
        ensureNoMoreThanOneNewLine = ensureOnlyOneNewLinePerLine;
        return this;
    }

    public StrBuilder newLine() {
        if(mDoAppend) mSb.append(STR_NEW_LINE);
        return this;
    }

    public StrBuilder space() {
        if(mDoAppend) mSb.append(STR_SPACE);
        return this;
    }

    public StrBuilder tab() {
        if(mDoAppend) mSb.append(STR_TAB);
        return this;
    }

    public StrBuilder collen() {
        if(mDoAppend) mSb.append(STR_COLLEN);
        return this;
    }

    public StrBuilder comma() {
        if(mDoAppend) mSb.append(STR_COMMA);
        return this;
    }

    public StrBuilder appendLine(StrBuilder sb) {
        if(mDoAppend) {
            mSb.append(sb.toString());
            mSb.append(STR_NEW_LINE);
        }
        return this;
    }

    public StrBuilder append(StrBuilder sb) {
        if(mDoAppend) mSb.append(sb.toString());
        return this;
    }

    public StrBuilder appendLine(StringBuilder sb) {
        if(mDoAppend) {
            ensureNoDoubleNewLine();
            mSb.append(sb.toString());
            mSb.append(STR_NEW_LINE);
        }
        return this;
    }

    public StrBuilder append(StringBuilder sb) {
        if(mDoAppend) mSb.append(sb.toString());
        return this;
    }

    public StrBuilder appendLine(String s) {
        if(mDoAppend) {
            ensureNoDoubleNewLine();
            mSb.append(s);
            mSb.append(STR_NEW_LINE);
        }
        return this;
    }

    public StrBuilder append(String s) {
        if(mDoAppend) mSb.append(s);
        return this;
    }

    public StrBuilder appendLine(Character c) {
        if(mDoAppend) {
            ensureNoDoubleNewLine();
            mSb.append(c);
            mSb.append(STR_NEW_LINE);
        }
        return this;
    }

    public StrBuilder append(Character c) {
        if(mDoAppend) mSb.append(c);
        return this;
    }

    public StrBuilder appendLine(Boolean b) {
        if(mDoAppend) {
            ensureNoDoubleNewLine();
            mSb.append(b);
            mSb.append(STR_NEW_LINE);
        }
        return this;
    }

    public StrBuilder append(Boolean b) {
        if(mDoAppend) mSb.append(String.valueOf(b));
        return this;
    }

    public StrBuilder appendLine(Integer i) {
        if(mDoAppend) {
            ensureNoDoubleNewLine();
            mSb.append(i);
            mSb.append(STR_NEW_LINE);
        }
        return this;
    }

    public StrBuilder append(Integer i) {
        if(mDoAppend) mSb.append(i);
        return this;
    }

    public StrBuilder appendLine(Object o) {
        if(mDoAppend) {
            ensureNoDoubleNewLine();
            mSb.append(o);
            mSb.append(STR_NEW_LINE);
        }
        return this;
    }

    public StrBuilder append(Object o) {
        if(mDoAppend) {
            mSb.append(o);
        }
        return this;
    }

    public StrBuilder appendAsLines(String[] arr) { append(arr, STR_NEW_LINE); mSb.append(STR_NEW_LINE); return this;  }
    public StrBuilder appendLine(String[] arr) { return appendLine(arr, STR_SPACE); }
    public StrBuilder appendLine(String[] arr, String delimiter) {
        if(mDoAppend) {
            ensureNoDoubleNewLine();
            mSb.append(Str.joinArray(arr, delimiter));
            mSb.append(STR_NEW_LINE);
        }
        return this;
    }

    public StrBuilder append(String[] arr) { return append(arr, STR_SPACE); }
    public StrBuilder append(String[] arr, String delimiter) {
        if(mDoAppend) mSb.append(Str.joinArray(arr, delimiter));
        return this;
    }

    public StrBuilder appendAsLines(List<String> lst) { append(lst, STR_NEW_LINE); mSb.append(STR_NEW_LINE); return this; }
    public StrBuilder appendLine(List<String> lst) { return appendLine(lst, STR_SPACE); }
    public StrBuilder appendLine(List<String> lst, String delimiter) {
        if(mDoAppend) {
            ensureNoDoubleNewLine();
            mSb.append(Str.joinList(lst, delimiter));
            mSb.append(STR_NEW_LINE);
        }
        return this;
    }

    public StrBuilder append(List<String> lst) { return append(lst, STR_SPACE); }
    public StrBuilder append(List<String> lst, String delimiter) {
        if(mDoAppend) mSb.append(Str.joinList(lst, delimiter));
        return this;
    }

    public StrBuilder appendErrorLine(Throwable t, boolean stackTrace) {
        if(mDoAppend) {
            ensureNoDoubleNewLine();
            mSb.append("Exception: ")
                    .append(t.getMessage());
            if(stackTrace)
                mSb.append(STR_NEW_LINE)
                        .append("Stack Trace:")
                        .append(STR_NEW_LINE)
                        .append(RuntimeUtils.getStackTraceSafeString(t));

            mSb.append(STR_NEW_LINE);
        }
        return this;
    }

    public StrBuilder appendError(Throwable t, boolean stackTrace) {
        if(mDoAppend) {
            mSb.append("Exception: ")
                    .append(t.getMessage());
            if(stackTrace)
                mSb.append(STR_NEW_LINE)
                        .append("Stack Trace:")
                        .append(STR_NEW_LINE)
                        .append(RuntimeUtils.getStackTraceSafeString(t));
        }
        return this;
    }


    public StrBuilder appendFieldLine(String fieldName, Map<?,?> map) {
        if(mDoAppend) {
            ensureNoDoubleNewLine();
            StringBuilder sb = new StringBuilder();

            String spacer = Str.repeatString(STR_SPACE, 3);

            int i = 0;
            for(Map.Entry<?,?> entry : map.entrySet()) {
                Object k = entry.getKey();
                Object v = entry.getValue();
                if(sb.length() > 0) sb.append(STR_NEW_LINE);
                sb.append(spacer)
                        .append("[").append(i).append("]::[")
                        .append(Str.toStringOrNull(k)).append("][")
                        .append(Str.toStringOrNull(v)).append("]");
                i++;
            }

            mSb.append(fieldName)
                    .append(STR_COLLEN)
                    .append(STR_NEW_LINE)
                    .append(sb)
                    .append(STR_NEW_LINE);
        }
        return this;
    }

    public StrBuilder appendFieldLine(String fieldName, Object value) {
        if(mDoAppend) {
            ensureNoDoubleNewLine();
            mSb.append(fieldName)
                    .append(STR_COLLEN)
                    .append(STR_SPACE)
                    .append(value)
                    .append(STR_NEW_LINE);
        }
        return this;
    }

    public StrBuilder appendField(String fieldName, Object value) {
        if(mDoAppend) {
            mSb.append(fieldName)
                    .append(STR_COLLEN)
                    .append(STR_SPACE)
                    .append(value);
        }
        return this;
    }


    public StrBuilder appendFieldLine(String fieldName, String value) {
        if(mDoAppend) {
            ensureNoDoubleNewLine();
            if(value != null && value.endsWith("\n")) value = value.substring(0, value.length() - 1);
            mSb.append(fieldName)
                    .append(STR_COLLEN)
                    .append(STR_SPACE)
                    .append(value)
                    .append(STR_NEW_LINE);
        }
        return this;
    }

    public StrBuilder appendField(String fieldName, String value) {
        if(mDoAppend) {
            mSb.append(fieldName)
                    .append(STR_COLLEN)
                    .append(STR_SPACE)
                    .append(value);
        }
        return this;
    }

    public StrBuilder appendStrBytesLine(byte[] bs) {
        if(mDoAppend) {
            ensureNoDoubleNewLine();
            if(bs == null) return this;
            mSb.append(new String(bs));
            mSb.append(STR_NEW_LINE);
        }
        return this;
    }

    public StrBuilder appendStrBytes(byte[] bs) {
        if(bs == null) return this;
        if(mDoAppend) mSb.append(new String(bs));
        return this;
    }

    public StrBuilder appendStrBytesLine(byte[] bs, Charset charSet) {
        if(mDoAppend) {
            ensureNoDoubleNewLine();
            if(bs == null) return this;
            mSb.append(new String(bs, charSet));
            mSb.append(STR_NEW_LINE);
        }
        return this;
    }

    public StrBuilder appendStrBytes(byte[] bs, Charset charSet) {
        if(bs == null) return this;
        if(mDoAppend) mSb.append(new String(bs, charSet));
        return this;
    }

    public StrBuilder appendStrBytesASCIILine(byte[] bs) {
        if(mDoAppend) {
            ensureNoDoubleNewLine();
            if(bs == null) return this;
            mSb.append(new String(bs, StandardCharsets.US_ASCII));
            mSb.append(STR_NEW_LINE);
        }
        return this;
    }

    public StrBuilder appendStrBytesASCII(byte[] bs) {
        if(bs == null) return this;
        if(mDoAppend) mSb.append(new String(bs, StandardCharsets.US_ASCII));
        return this;
    }

    public StrBuilder appendStrBytesUTF16Line(byte[] bs) {
        if(mDoAppend) {
            ensureNoDoubleNewLine();
            if(bs == null) return this;
            mSb.append(new String(bs, StandardCharsets.UTF_16));
            mSb.append(STR_NEW_LINE);
        }
        return this;
    }

    public StrBuilder appendStrBytesUTF16(byte[] bs) {
        if(bs == null) return this;
        if(mDoAppend) mSb.append(new String(bs, StandardCharsets.UTF_16));
        return this;
    }

    public StrBuilder appendStrBytesUTF8Line(byte[] bs) {
        if(mDoAppend) {
            ensureNoDoubleNewLine();
            if(bs == null) return this;
            mSb.append(new String(bs, StandardCharsets.UTF_8));
            mSb.append(STR_NEW_LINE);
        }
        return this;
    }

    public StrBuilder appendStrBytesUTF8(byte[] bs) {
        if(bs == null) return this;
        if(mDoAppend) mSb.append(new String(bs, StandardCharsets.UTF_8));
        return this;
    }

    public StrBuilder appendLine(byte[] bs) {
        if(mDoAppend) {
            ensureNoDoubleNewLine();
            if(bs == null) return this;
            mSb.append(Str.bytesToHex(bs));
            mSb.append(STR_NEW_LINE);
        }
        return this;
    }

    public StrBuilder append(byte[] bs) {
        if(bs == null) return this;
        if(mDoAppend) mSb.append(Str.bytesToHex(bs));
        return this;
    }

    public StrBuilder appendSpace() {
        if(mDoAppend) mSb.append(STR_SPACE);
        return this;
    }

    public <T> StrBuilder appendCollectionLine(Collection<T> collection) {
        if(collection == null || collection.isEmpty()) return this;
        if(mDoAppend) {
            int index = 0;
            for(T item : collection) {
                try {
                    appendLine("ListItem[" + index + "]:");
                    appendLine(Str.toStringOrNull(item));
                    appendLine("ListItem[" + index + "] End...");
                }catch (Exception ignored) { }
                index++;
            }
        }

        return this;
    }


    //public StrBuilder appendMapLine(Map<?, ?> map, boolean appendKeys, boolean appendValues, boolean appendEachEntryOnNewLine) {
    //}

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof String) {
            String s = (String)obj;
            return s.equalsIgnoreCase(this.toString());
        }

        if(obj instanceof StringBuilder) {
            StringBuilder s = (StringBuilder)obj;
            return s.toString().equalsIgnoreCase(this.toString());
        }

        if(obj instanceof StrBuilder) {
            StrBuilder s = (StrBuilder) obj;
            return s.toString().equalsIgnoreCase(this.toString());
        }

        return false;
    }

    @NonNull
    @Override
    public String toString() {
        String s = mSb.toString();
        if(s.endsWith("\n")) return s.substring(0, s.length() - 1);
        return s;
    }

    public String toString(boolean ensureNoDoubleNewLines) {
        String s = this.toString();
        return ensureNoDoubleNewLines ? Str.trim(Str.ensureNoDoubleNewLines(s), STR_NEW_LINE, true, true) : s;
    }
}
