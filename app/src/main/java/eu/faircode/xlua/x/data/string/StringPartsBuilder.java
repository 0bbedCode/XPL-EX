package eu.faircode.xlua.x.data.string;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.MapUtils;
import eu.faircode.xlua.x.xlua.LibUtil;

@SuppressWarnings("CopyConstructorMissesField")
public class StringPartsBuilder {
    private static final String TAG = LibUtil.generateTag(StringPartsBuilder.class);



    private static final IStringPartRules ALPHA_NUMERIC = new IStringPartRules() {
        @Override
        public boolean isDelimiterKind(char c) { return !Character.isDigit(c) && !Character.isAlphabetic(c); }

        @Override
        public String cleanPart(String s) { return TextUtils.isEmpty(s) ? null : s; }
    };



    private StringCharBlock mBlock;
    private final List<String> mParts = new ArrayList<>();
    private IStringPartRules mRules;
    private IStringPartTrimmer mTrimmer;
    private Map<String, String> mResolverMap;
    private String mLastString = null;

    private boolean mCapitalizeFirstLetter = false;
    private char mBreakOn = '\0';
    private char mStartOn = '\0';
    private List<StringPartsBuilder> mBrokenParts = new ArrayList<>();

    public static StringPartsBuilder createOnlyAlphaNumeric() { return create(ALPHA_NUMERIC); }

    public static StringPartsBuilder create() { return new StringPartsBuilder(); }
    public static StringPartsBuilder create(IStringPartRules rules) { return new StringPartsBuilder(rules); }
    public static StringPartsBuilder create(IStringPartRules rules, IStringPartTrimmer trimmer) { return new StringPartsBuilder(rules, trimmer); }
    public static StringPartsBuilder create(IStringPartRules rules, IStringPartTrimmer trimmer, boolean capitalizeFirstLetters) { return new StringPartsBuilder(rules, trimmer, capitalizeFirstLetters); }
    public static StringPartsBuilder create(StringPartsBuilder source) { return new StringPartsBuilder(source); }

    public StringPartsBuilder rules(IStringPartRules rules) { this.mRules = rules; return this; }
    public StringPartsBuilder trimmer(IStringPartTrimmer trimmer) { this.mTrimmer = trimmer; return this; }
    public StringPartsBuilder capitalizeFirstLetter(boolean capitalizeFirstLetter) { this.mCapitalizeFirstLetter = capitalizeFirstLetter; return this; }
    public StringPartsBuilder breakOnAndStartOn(char breakOnChar, char startOnChar) { this.mBreakOn = breakOnChar; this.mStartOn = startOnChar; return this; }
    public StringPartsBuilder resolverMap(Map<String, String> resolverMap) { this.mResolverMap = resolverMap; return this; }

    public StringPartsBuilder(StringPartsBuilder createCopyOf, boolean copyBuffer) {
        if(createCopyOf != null) {
            mBlock = copyBuffer ? new StringCharBlock(createCopyOf.mBlock) : new StringCharBlock(createCopyOf.mBlock.getBufferSize());
            mParts.addAll(createCopyOf.mParts);
            //Finish this, we should not use it for now
        }
    }

    public StringPartsBuilder() { }
    public StringPartsBuilder(IStringPartRules rules) { this.mRules = rules; }
    public StringPartsBuilder(IStringPartRules rules, IStringPartTrimmer trimmer) { this.mRules = rules; this.mTrimmer = trimmer; }
    public StringPartsBuilder(IStringPartRules rules, IStringPartTrimmer trimmer, boolean capitalizeFirstLetters) { this.mRules = rules; this.mTrimmer = trimmer; this.mCapitalizeFirstLetter = capitalizeFirstLetters; }
    public StringPartsBuilder(StringPartsBuilder source) {
        if(source != null) {
            this.mBlock = StringCharBlock.create(source.mBlock);
            ListUtil.addAllIfValid(this.mParts, source.mParts);
            copyNonImportant(source);
        }
    }

    public StringPartsBuilder copyNonImportant(StringPartsBuilder source) {
        if(source != null) {
            this.mRules = source.mRules;
            this.mTrimmer = source.mTrimmer;
            this.mResolverMap = source.mResolverMap;
            this.mCapitalizeFirstLetter = source.mCapitalizeFirstLetter;
            this.mBreakOn = source.mBreakOn;
            this.mStartOn = source.mStartOn;
        }

        return this;
    }

    public String getString() { return Str.joinList(mParts, " "); }
    public String getString(String delimiter) { return Str.joinList(mParts, delimiter); }

    public String getFirstPart() { return !mParts.isEmpty() ? mParts.get(0) : null; }
    public String getFirstPart(String defaultString) { return !mParts.isEmpty() ? mParts.get(0) : defaultString; }

    public String getLastPart() { return !mParts.isEmpty() ? mParts.get(mParts.size() - 1) : null; }
    public String getLastPart(String defaultString) { return !mParts.isEmpty() ? mParts.get(mParts.size() - 1) : defaultString; }

    public String getPart(int index) { return mParts.size() > index ? mParts.get(index) : null;  }
    public String getPart(int index, String defaultString) { return mParts.size() > index ? mParts.get(index) : defaultString; }

    public boolean hasParts() { return !mParts.isEmpty(); }
    public int partsCount() { return mParts.size(); }

    public String getOriginalString() { return mLastString; }

    public List<String> joinFirstBrokenParts() { return !mBrokenParts.isEmpty() ? mBrokenParts.get(0).getParts() : null; }
    public List<String> getLastBrokenParts() { return !mBrokenParts.isEmpty() ? mBrokenParts.get(mBrokenParts.size() - 1).getParts() : null; }
    public List<String> joinAllBrokenParts() {
        List<String> parts = new ArrayList<>();
        for(StringPartsBuilder ps : mBrokenParts) ListUtil.addAllIfValid(parts, ps.joinAllBrokenParts());
        return parts;
    }

    public Map<String, String> getResolverMap() { return mResolverMap; }

    public List<String> getParts() { return mParts; }
    public List<String> getParts(IStringPartTrimmer trimmer) {
        if(mParts.isEmpty()) return mParts;
        IStringPartTrimmer trimEvent = trimmer == null ? mTrimmer : trimmer;
        return trimEvent != null ? trimEvent.trimParts(mParts) : mParts;
    }

    public StringPartsBuilder resolveParts(PartFilter partFilter) {
        if(partFilter != null && !mParts.isEmpty()) {
            for (int i = mParts.size() - 1; i >= 0; i--){
                partFilter.parsePart(mParts, i);
            }
        }

        return this;
    }

    public StringPartsBuilder resolveParts() { return resolveParts(mResolverMap); }
    public StringPartsBuilder resolveParts(Map<String, String> resolverMap) {
        if(resolverMap != null && !mParts.isEmpty()) {
            for (int i = mParts.size() - 1; i >= 0; i--){
                String p = mParts.get(i);


                if(resolverMap.containsKey(p)) {
                    String resolved = resolverMap.get(p);
                    if(TextUtils.isEmpty(resolved))
                        mParts.remove(i);
                    else
                        mParts.set(i, resolved);
                }
            }
        }

        return this;
    }

    public int ensureIsEmpty() { return mBlock.getCurrentIndex() > 0 ? mBlock.reset(mParts, mCapitalizeFirstLetter, mRules, null) : 0; }
    public int ensureIsEmpty(Map<String, String> resolverMap) {
        return mBlock.getCurrentIndex() > 0 ? mBlock.reset(mParts, mCapitalizeFirstLetter, mRules, resolverMap) : 0; }

    public StringPartsBuilder ensureBlockIsReady(String s) { return ensureBlockIsReady(Str.getSize(s)); }
    public StringPartsBuilder ensureBlockIsReady(int size) {
        if(mBlock == null) mBlock = new StringCharBlock(size);
        else mBlock.ensureSize(size);
        return this;
    }

    public StringPartsBuilder trimStartParts(String trimString) { return trimParts(trimString, false, true, false); }
    public StringPartsBuilder trimStartParts(String trimString, boolean keepGoing) { return trimParts(trimString,false, true, keepGoing); }
    public StringPartsBuilder trimEndParts(String trimString) { return trimParts(trimString,true, false, false); }
    public StringPartsBuilder trimEndParts(String trimString, boolean keepGoing) { return trimParts(trimString,true, false, keepGoing); }
    public StringPartsBuilder trimParts(String trimString, boolean atEnd, boolean atStart) { return trimParts(trimString, atEnd, atStart, false); }
    public StringPartsBuilder trimParts(String trimString, boolean atEnd, boolean atStart, boolean keepGoing) {
        int totalSz = mParts.size();
        boolean keepGoingEnd = true;
        boolean keepGoingStart = true;
        do {
            if(atEnd && totalSz > 0 && keepGoingEnd) {
                int lastIndex = mParts.size() - 1;
                if(mParts.get(lastIndex).equalsIgnoreCase(trimString)) {
                    mParts.remove(lastIndex);
                    totalSz--;
                } else {
                    keepGoingEnd = false;
                }
            }

            if(atStart && totalSz > 0 && keepGoingStart) {
                if(mParts.get(0).equalsIgnoreCase(trimString)) {
                    mParts.remove(0);
                    totalSz--;
                } else {
                    keepGoingStart = false;
                }
            }
        } while (keepGoing && (keepGoingStart || keepGoingEnd) && totalSz > 0);
        return this;
    }

    public StringPartsBuilder trimStartNumericParts() { return trimNumericParts(false, true, false); }
    public StringPartsBuilder trimStartNumericParts(boolean keepGoing) { return trimNumericParts(false, true, keepGoing); }
    public StringPartsBuilder trimEndNumericParts() { return trimNumericParts(true, false, false); }
    public StringPartsBuilder trimEndNumericParts(boolean keepGoing) { return trimNumericParts(true, false, keepGoing); }
    public StringPartsBuilder trimNumericParts(boolean atEnd, boolean atStart) { return trimNumericParts(atEnd, atStart, false); }
    public StringPartsBuilder trimNumericParts(boolean atEnd, boolean atStart, boolean keepGoing) {
        int totalSz = mParts.size();
        do {
            boolean removedAny = false;

            if(atEnd && totalSz > 0) {
                int lastIndex = mParts.size() - 1;
                String last = mParts.get(lastIndex);
                if(PartFilter.NUMBER_RESOLVER_MAP_REVERSE.containsKey(last) || Str.isNumeric(last)) {
                    mParts.remove(lastIndex);
                    totalSz--;
                    removedAny = true;
                }
            }

            if(atStart && totalSz > 0) {
                String first = mParts.get(0);
                if(PartFilter.NUMBER_RESOLVER_MAP_REVERSE.containsKey(first) || Str.isNumeric(first)) {
                    mParts.remove(0);
                    totalSz--;
                    removedAny = true;
                }
            }

            if(!keepGoing || !removedAny) break;

        } while (totalSz > 0);
        return this;
    }

    public StringPartsBuilder trimStartAlphabetParts() { return trimAlphabetParts(false, true, false); }
    public StringPartsBuilder trimStartAlphabetParts(boolean keepGoing) { return trimAlphabetParts(false, true, keepGoing); }
    public StringPartsBuilder trimEndAlphabetParts() { return trimAlphabetParts(true, false, false); }
    public StringPartsBuilder trimEndAlphabetParts(boolean keepGoing) { return trimAlphabetParts(true, false, keepGoing); }
    public StringPartsBuilder trimAlphabetParts(boolean atEnd, boolean atStart) { return trimAlphabetParts(atEnd, atStart, false); }
    public StringPartsBuilder trimAlphabetParts(boolean atEnd, boolean atStart, boolean keepGoing) {
        int totalSz = mParts.size();
        do {
            if(atEnd && totalSz > 0) {
                int lastIndex = mParts.size() - 1;
                if(Str.isAlphabet(mParts.get(lastIndex))) {
                    mParts.remove(lastIndex);
                    totalSz--;
                }
            }

            if(atStart && totalSz > 0) {
                if(Str.isAlphabet(mParts.get(0))) {
                    mParts.remove(0);
                    totalSz--;
                }
            }
        } while (keepGoing && totalSz > 0);
        return this;
    }

    public StringPartsBuilder parseStringParts(String s) { return parseStringParts(s, 0); }
    public StringPartsBuilder parseStringParts(String s, int stopAtPartCount) {
        if(TextUtils.isEmpty(s)) return this;
        mLastString = s;
        parseChars(s.toCharArray(), 0, stopAtPartCount);
        return this;
    }

    public int parseChars(char[] chars, int startIndex, int stopAtPartCount) {
        if(chars == null) return 0;
        int len = chars.length;

        if(len < 1 || startIndex >= len) return 0;
        ensureBlockIsReady(len);
        int last = len - 1;
        int i = startIndex;

        for(; i < len; i++) {
            char c = chars[i];
            if(!mRules.isDelimiterKind(c) && mBlock.appendUnsafe(c)) {
                if(i != last)
                    continue;
            }

            if(ensureIsEmpty(mResolverMap) > 0 && stopAtPartCount > 0 && mParts.size() >= stopAtPartCount)
                break;

            if(c == mStartOn && startIndex > 0) {
                if(DebugUtil.isDebug()) Log.d(TAG, "Found the Part Breaker Char, " + c + " I=" + i + " Full String=" + new String(chars));
                break;
            }

            if(c == mBreakOn && i < last) {
                if(DebugUtil.isDebug()) Log.d(TAG, "Found the Part Starter Char, " + c + " I=" + i + " Full String=" + new String(chars));
                StringPartsBuilder brokenPart = create().copyNonImportant(this);
                mBrokenParts.add(brokenPart);
                i = i + 1;
                int consumed = brokenPart.parseChars(chars, i, 0);
                i =  consumed > 0 ? consumed - 1 : i;
                if(DebugUtil.isDebug()) Log.d(TAG, "Finished Parsing Block from Start Char, " + c + " New Index=" + i + " Full String=" + new String(chars));
            }
        }

        return i;
    }
}
