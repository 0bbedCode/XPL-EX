package eu.faircode.xlua.x.data.utils;

import android.util.Log;

import java.util.Collection;
import java.util.concurrent.Callable;

import eu.faircode.xlua.x.data.interfaces.IConditioner;
import eu.faircode.xlua.x.data.interfaces.IValidator;
import eu.faircode.xlua.x.data.interfaces.IValueSelector;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.runtime.RuntimeUtils;

/**
 * ToDo: Design a Value Selection System with Cached Evaluation
 *  ..
 *  Problem: When selecting between values, we often need to:
 *  1. Call a function to get a value
 *  2. Store that value to avoid multiple invocations
 *  3. Apply conditional logic to select between values
 *  ..
 *  Current patterns:
 *  Pattern 1 - Multiple lines:
 *      Object result = expensiveOperation();
 *     result = result == null ? defaultValue : result;
 *  ..
 *  Pattern 2 - Single line but double invocation:
 *      Object result = expensiveOperation() == null ? defaultValue : expensiveOperation();
 *  ..
 *  Proposed solution:
 *  Create ValueUtils.java with methods like:
 *      Object result = valueNotNull(expensiveOperation(), defaultValue);
 *      int number = valueGreater(getNumber(), threshold);
 *  ..
 *  Benefits:
 *  - Single invocation of expensive operations
 *  - Clean, functional syntax
 *  - Reusable comparison logic
 *  - Type-safe operations
 *
 *  ToDo:
 *      [1] Add Equal Compare Checkers
 *      [2] Add Organizer Check
 *      [3] Add Interface like "IObjectSelector" -> "TObject selector(TObject a, TObject b)"
 *                      "ObjectUtils" -> "TObject select(TObject a, TObject b, IObjectSelector::selector)"
 *                      "ObjectUtils" -> "TObject select(IObjectSelector::selector);
 *                      Have Default returners as well
 *      [4] Have some System or Object wrapper tells when the object is bad ??
 *                      ..PoC:
 *                      ObjectWrapper.java
 *                          T badCase;
 *                          T value;
 *                          T defaultGlobalValueIfBad           //Use this as a ALWAYS defaulter ? it can also be used here ?? used to "check" condition of value
 *                          boolean isBad();
 *                          boolean isBad(T replaceIfBad);
 *                          T getValue();
 *                      ObjectWrapper obj = new (something, badCase);
 *                          Something with Atomic Types, look into more
 *      [5] Make Array Utils to Clean out the Invalid / Null
 *      [6] TList valuesNonNull(T...) or T firstNonNullValue(T...), or T firstValueLike(T...)
 *      [7] Make Linq like functions, select, where, first, some, any, last,
 *                                    > Maybe a List Wrapper ? Converts consumes any Types of list ?
 *                                    > Set, ArrayList, Collection, List, etc consumes as Copy or Not ?
 *                                    > Create Copy functions, removeAny(T... bad)
 *                                    > isValid ?
 *                                    > hasAnyNull, hasAny, hasSome, isAll
 *                                    > move, copy, slice, split, sub, first, last
 *      [8] Make unbox and check system clean: PoC (using Maps):
 *                      <KEY_IS_INT_IDENTIFIER, IDENTIFIERS("int", "integer")>
 *                         or
 *                      <IS_INT, CONVERTER_UNBOX_WHATEVER>
 *      [9] Perhaps some advance cast like <TFrom, TCast>
 *
 *  Note:   We do not need to print the Stack Trace within the Throw / Errors as "Exception" or "Throwable" has options to get Stack Trace
 *          So the Caller within the Catch or in the Error Logger what not should already print that as either force or Option
 *
 *          The following functions "valueNonNull" "nullOrDefault" are virtually the same, do the same thing, just naming :P looks cool
 *          These can be use such as like:
 *                  ..
 *                  T object = nullOrDefault(valueNotEqualTooOrLog(getValueA(), getValueB(), 0xBadValue), 0xDefault);
 */
@SuppressWarnings("unchecked")
public class ObjectUtils {
    //public static final IConditioner<Object> NON_NULL_CONDITIONER = (o) -> { return o != null; };

    private static final String TAG = "XLua.ObjectUtils";


    @SafeVarargs
    public static <T> T tryInvokeFirstNonNull(Callable<T>... actions) {
        T val = null;
        for(Callable<T> a : actions) {
            if(a != null) {
                try {
                    val = a.call();
                }catch (Exception ignored) { }
                if(val != null)
                    return val;
            }
        }

        return null;
    }

    public static <T> T tryInvoke(Callable<T> action) {
        try {
            return action.call();
        } catch (Exception ignored) {
            return null;
        }
    }

    public static boolean areAnyNullLog(Object... objects) {
        StrBuilder sb = new StrBuilder().ensureOneNewLinePer(true);
        boolean oneIsNull = false;
        for(int i = 0; i < objects.length; i++) {
            Object o = objects[i];
            if(o == null) {
                sb.appendFieldLine("Object at Index [" + String.valueOf(i) + "]", "Is Null");
                if(!oneIsNull) oneIsNull = true;
            }
        }

        if(oneIsNull) {
            sb.appendFieldLine("Stack Trace", RuntimeUtils.getStackTraceSafeString());
            Log.e(TAG, "Error, One or Some of the Objects passed within [areAnyNullLog] Check appear to be Null. Log=" + sb.toString(true));
        }

        return oneIsNull;
    }

    public static boolean anyNull(Object... objects) {
        for(Object o : objects) {
            if(o == null)
                return true;
            else {
                if(o instanceof String) {
                    if(((String) o).isEmpty())
                        return true;
                }
                else if(o instanceof Collection) {
                    int size = ListUtil.size((Collection<?>) o);
                    if(size < 1)
                        return true;
                }
            }
        }
        return false;
    }

    public static <T> T firstNonNullValue(T... vals) {
        for(T v : vals) if(v != null) return v;
        return null;
    }

    public static <T> T valueNonNull(T a, T b) { return a == null ? b : a; }
    public static <T> T nullOrDefault(T a, T defaultValue) { return a == null ? defaultValue : a; }

    public static <T> T valueNonNullOrThrow(T a, T b) throws Exception {
        T obj = valueNonNull(a, b);
        if(obj == null) throw new Exception("Both values are Null, [valueNotNull]");
        return obj;
    }

    public static <T> T valueNonNullOrLog(T a, T b) {
        T obj  = valueNonNull(a, b);
        if(obj == null) Log.e(TAG, "Error with getting the Non Null Object [valueNonNullOrLog], both Objects are Null! Stack=" + RuntimeUtils.getStackTraceSafeString());
        return obj;
    }

    public static <T> T valueNonNullOrDefault(T a, T b, T defaultValue) {
        /* Kek Ps these function are suppose to help resolve situations like this with two lines of code, compressing it to one xD
        * Ironic the function help with that is doing that, its fine once time as long as main code does not/ main code is one line */
        T obj = valueNonNull(a, b);
        return obj == null ? defaultValue : obj;
    }

    public static <T> T valueNonNullNonBindingParams(Object a, Object b) {
        Object obj = valueNonNull(a, b);
        return tryCast(obj);
    }

    public static <T> T valueNonNullOrThrowNonBindingParams(Object a, Object b) throws Exception {
        Object obj = valueNonNull(a, b);
        if(obj == null) throw new Exception("Both values are Null, [valueNonNullOrThrowNonBindingParams] Stack=" + RuntimeUtils.getStackTraceSafeString());
        return tryCast(obj);
    }

    public static <T> T valueNonNullOrLogNonBindingParams(Object a, Object b) {
        Object obj  = valueNonNull(a, b);
        if(obj == null) Log.e(TAG, "Error with getting the Non Null Object [valueNonNullOrLogNonBindingParams], both Objects are Null! Stack=" + RuntimeUtils.getStackTraceSafeString());
        return tryCast(obj);
    }

    public static <T> T valueNonNullOrDefaultNonBindingParams(Object a, Object b, T defaultValue) {
        Object obj = valueNonNull(a, b);
        return obj == null ? defaultValue : tryCast(obj);
    }

    public static <T> T selectNonBad(T a, T b, IValueSelector<T> selector) { return selector != null ? selector.select(a, b) : valueNonNull(a, b); }


    public static <T> T selectFirstNonBad(IConditioner<T> conditioner, T... values) { return selectFirstNonBad(conditioner, null, values); }
    public static <T> T selectFirstNonBad(IConditioner<T> conditioner, T defaultValue, T... values) {
        for(T v : values) if(v != null && conditioner.meetsCondition(v)) return v;
        return defaultValue;
    }

    public static <T> T throwIfObjectIsNull(T obj) throws Exception {
        if(obj == null) throw new Exception("Error Object is NULL [throwIfObjectIsNull]!");
        return obj;
    }

    public static <T> T logIfNull(T obj) {
        if(obj == null) Log.e(TAG, "Error Object is NULL [logIfNull] Stack=" + RuntimeUtils.getStackTraceSafeString());
        return obj;
    }

    public static <T> T throwIfObjectNotMeetCondition(IConditioner<T> conditioner, T obj) throws Exception {
        if(obj == null || !conditioner.meetsCondition(obj)) throw new Exception("Error Object is NULL [throwIfObjectNotMeetCondition]!");
        return obj;
    }

    public static <T> T logIfNotMeetCondition(IConditioner<T> conditioner, T obj) {
        if(obj == null || !conditioner.meetsCondition(obj)) Log.e(TAG, "Error Object is NULL [logIfNotMeetCondition]! Stack=" + RuntimeUtils.getStackTraceSafeString());
        return obj;
    }

    public static <T> T valueNotEqualTooOrLog(T a, T b, T bad) {
        T notEqual = valueNotEqualToo(a, b, null, bad);
        if(notEqual == null) Log.e(TAG, "Could not Find the Value that is not Equal Too [valueNotEqualTooOrLog] Stack=" + RuntimeUtils.getStackTraceSafeString());
        return notEqual;
    }

    public static <T> T valueNotEqualTooOrThrow(T a, T b, T bad) throws Exception {
        T notEqual = valueNotEqualToo(a, b, null, bad);
        if(notEqual == null) throw new Exception("Could not Find the Value that is not Equal Too [valueNotEqualTooOrThrow]");
        return notEqual;
    }

    public static <T> T valueNotEqualToo(T a, T b, T bad) { return valueNotEqualToo(a, b, null, bad); }
    public static <T> T valueNotEqualToo(T a, T b, T defaultValue, T bad) {
        if(a == null || b == null) {
            T nonNull = valueNonNull(a, b);
            return nonNull == null ? defaultValue : nonNull.equals(bad) ? defaultValue : nonNull;
        }
        return a.equals(bad) ? b.equals(bad) ? defaultValue : b : a;
    }

    public static <T> T valueNotEqualTooAnyOrThrow(T a, T b, T... badValues) throws Exception {
        T notEqual = valueNotEqualTooAny(a, b, null, badValues);
        if(notEqual == null) throw new Exception("Could not Find the Value that is not Equal Too Any [valueNotEqualTooAnyOrThrow]");
        return notEqual;
    }

    public static <T> T valueNotEqualTooAnyOrLog(T a, T b, T... badValues) {
        T notEqual = valueNotEqualTooAny(a, b, null, badValues);
        if(notEqual == null) Log.e(TAG,"Could not Find the Value that is not Equal Too Any [valueNotEqualTooAnyOrLog] Stack=" + RuntimeUtils.getStackTraceSafeString());
        return notEqual;
    }

    public static <T> T valueNotEqualTooAny(T a, T b, T... badValues) { return valueNotEqualTooAny(a, b, null, badValues); }
    public static <T> T valueNotEqualTooAny(T a, T b, T defaultValue, T... badValues) {
        if(!ArrayUtils.isValid(badValues)) return defaultValue;
        boolean aIsGood = a != null;
        boolean bIsGood = b != null;
        for(T bad : badValues) {
            if(aIsGood) if(a.equals(bad)) aIsGood = false;
            if(bIsGood) if(b.equals(bad)) bIsGood = false;
            if(!aIsGood && !bIsGood) return defaultValue;
        }

        return aIsGood ? a : bIsGood ? b : defaultValue;
    }

    //public static int valueNotEqualToo(int a, int b, )
    //areAnyEqualToo(int... vals, int bad)

    //public static <T> T useValueIfNotEqualsOrInvoke(T a, IConditioner<T> conditioner, T defaultValue) {
        //if(a == null || a.equals(equals)) {
        //    T v = operation();
        //}
    //}

    /*public static <T extends Number> T valueIfBadThenChain(T initial, T badValue, Operation<T> chainedOperation, T defaultIfChainBad) {
        return Objects.equals(initial, badValue) ?
                (chainedOperation != null ? valueGreaterThanZero(chainedOperation.execute(), defaultIfChainBad) : defaultIfChainBad)
                : initial;
    }*/

    public static <T extends Number> T valueGreaterThanZero(T value, T defaultValue) {
        return (value != null && value.doubleValue() > 0) ? value : defaultValue;
    }

    public static <T> boolean isNull(T obj) {
        return obj == null;
    }

    public static <T extends IValidator> boolean isValid(T obj) {
        return obj != null && obj.isValid();
    }

    public static <T, TCast> boolean canCastAs(Object o, Class<TCast> azClazz) {
        return false;//ToDo
    }

    public static <T> T tryCast(Object value) { return tryCast(value, null); }
    public static <T> T tryCast(Object value, T defaultValue) {
        if(value != null) {
            try {
                return (T)value;
            }
            catch (Exception ignored) { }
        }
        return defaultValue;
    }


    public static String isNullAsString(Object o) { return o == null ? "true" : "false"; }

    public static <T> T tryCastAdvance(Object value, Class<T> type) {
        return null;//ToDo
    }

    public static String toStringOrNull(Object value) { return toStringOrNull(value, "null");  }
    public static String toStringOrNull(Object value, String defaultValue) {
        if(value != null) {
            try {
                return String.valueOf(value); //Have checks for lists, arrays, format them etc, move from Str Class or Str calls here ?
            }
            catch (Exception ignored) { }
        }
        return defaultValue;
    }

    //Make into type converter or something class ?
    public static boolean isIntType(Class<?> clazz) {
        String t = clazz.getName().toLowerCase();
        return t.endsWith("int") || t.endsWith("integer");
    }
}
