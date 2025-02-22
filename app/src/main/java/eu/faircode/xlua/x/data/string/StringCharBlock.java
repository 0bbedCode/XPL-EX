package eu.faircode.xlua.x.data.string;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.hook.interceptors.zone.Te;

@SuppressWarnings("StringBufferReplaceableByString")
public class StringCharBlock {
    private static final String TAG = "XLua.StringCharBlock";

    public static StringCharBlock create() { return new StringCharBlock(); }
    public static StringCharBlock create(int size) { return new StringCharBlock(size); }
    public static StringCharBlock create(StringCharBlock source) { return new StringCharBlock(source); }

    private char[] mArray;
    private int mCurrentPointer = 0;
    private int mFirstLetter = -1;

    public String toString(int count) { return count == 0 ? null : new StringBuilder().append(mArray, 0, count).toString(); }

    public int getMinimumSize() { return mArray == null ? 0 : Math.min(mArray.length, mCurrentPointer); }
    public int getBufferRemainingSize() { return mArray == null ? 0 : mArray.length - mCurrentPointer; }
    public int getBufferSize() { return mArray == null ? 0 : mArray.length; }

    public int getCurrentIndex() { return mCurrentPointer; }
    public int getFirstLetterIndex() { return mFirstLetter; }

    public StringCharBlock() { mArray = new char[1]; }
    public StringCharBlock(int size) { this.mArray = new char[Math.max(size, 1)]; }
    public StringCharBlock(StringCharBlock source) {
        if(source != null) {
            this.mArray = ArrayUtils.copy(source.mArray);
            this.mCurrentPointer = source.mCurrentPointer;
            this.mFirstLetter = source.mFirstLetter;
        }
    }

    public boolean appendUnsafe(char c) {
        if(c == '\0') return false;
        if(mFirstLetter < 0 && Character.isAlphabetic(c)) mFirstLetter = mCurrentPointer;
        mArray[mCurrentPointer] = c;
        mCurrentPointer++;
        return true;
    }

    public boolean append(char c) {
        if(c == '\0') return false;
        ensureCanWriteInCurrentIndex();
        if(mFirstLetter < 0 && Character.isAlphabetic(c)) mFirstLetter = mCurrentPointer;
        mArray[mCurrentPointer] = c;
        mCurrentPointer++;
        return true;
    }

    public int reset(List<String> parts) { return reset(parts, false, null, null); }
    public int reset(List<String> parts, boolean capitalizeFirstLetter) { return reset(parts, capitalizeFirstLetter, null, null); }
    public int reset(List<String> parts, boolean capitalizeFirstLetter, IStringPartRules rules) { return reset(parts, capitalizeFirstLetter, rules, null); }
    public int reset(List<String> parts,
                     boolean capitalizeFirstLetter,
                     IStringPartRules rules,
                     Map<String, String> resolverMap) {
        int finalSize = mArray == null ? 0 : Math.min(mArray.length, mCurrentPointer);
        if(finalSize > 0) {
            if(capitalizeFirstLetter)
                mArray[mFirstLetter] = Character.toUpperCase(mArray[mFirstLetter]);

            String part = resolve(getPart(finalSize, rules), resolverMap);
            if(TextUtils.isEmpty(part)) {
               finalSize = 0;
            } else {
                parts.add(part);
            }
        }

        mCurrentPointer = 0;
        mFirstLetter = -1;
        return finalSize;
    }

    private String resolve(String part, Map<String, String> resolverMap) {
        return resolverMap != null && part != null ?
                resolverMap.containsKey(part) ? resolverMap.get(part) : part : part;
    }

    private String getPart(int finalSize, IStringPartRules rules) {
        String s =  new StringBuilder().append(mArray, 0, finalSize).toString();
        return rules != null ? rules.cleanPart(s) : s;
    }

    public void fillBuffer() { fillBuffer('\0', true); }
    public void fillBuffer(char c) { fillBuffer(c, true); }
    public void fillBuffer(char c, boolean zeroIndexNotCurrentIndex) {
        if(mArray != null && mArray.length > 0) {
            int startIx = zeroIndexNotCurrentIndex ?
                    0 : mArray.length > mCurrentPointer ? mCurrentPointer : 0;
            for(int i = startIx; i < mArray.length; i++) {
                mArray[i] = c;
            }
        }
    }

    public void flushBuffer(boolean reallocate) {
        if(reallocate) {
            int sz = mArray != null ? Math.max(mArray.length, 1) : 1;
            mArray = new char[sz];
        } else {
            mArray = null;
        }
    }

    public void ensureCanWriteInCurrentIndex() { ensureSize(mCurrentPointer++); }
    public void ensureSize(int size) {
        if(mArray == null) {
            this.mArray = new char[size];
        } else {
            if(size <= mArray.length) return;
            this.mArray = Arrays.copyOf(mArray, size);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return toString(mCurrentPointer);
    }
}
