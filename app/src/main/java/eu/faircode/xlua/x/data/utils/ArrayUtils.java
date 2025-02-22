package eu.faircode.xlua.x.data.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.runtime.reflect.DynType;

public class ArrayUtils {
    public static interface IOnInt {  int parseRange(int range); }

    public static int[] generate(int start, int end) { return generate(start, end, null); }
    public static int[] generate(int start, int end, IOnInt onInt) {
        List<Integer> ints = new ArrayList<>();
        start = Math.max(start, 0);
        end = Math.max(end, 0);
        if(start != end) {
            if(start > end) {
                int o = start;
                start = end;
                end = o;
            }

            for(int i = start; i < end + 1; i++) {
                if(onInt != null) {
                    int resolved = onInt.parseRange(i);
                    if(!ints.contains(resolved))
                        ints.add(resolved);
                } else {
                    if(!ints.contains(i))
                        ints.add(i);
                }
            }
        }

        int[] values = new int[ints.size()];
        for(int i = 0; i < ints.size(); i++) {
            int val = ints.get(i);
            values[i] = ints.get(i);
        }

        return values;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] reverse(@Nullable T[] array) {
        if(array == null || array.length < 2)
            return array;

        Class<?> clazz = DynType.getFirstNonNullElementClass(array);
        if(clazz == null)
            return array;

        T[] reversed =  (T[]) Array.newInstance(clazz, array.length);
        for (int i = 0, j = array.length - 1; i < array.length; i++, j--) {
            reversed[i] = array[j];
        }

        return reversed;
    }

    /**
     * Creates a copy of the given array
     * @param array The array to copy
     * @return A new array containing all elements from the source array
     */
    @Nullable
    public static <T> T[] copy(@Nullable T[] array) {
        if (array == null) return null;
        return Arrays.copyOf(array, array.length);
    }

    public static boolean isValid(JSONArray array) {
        return array != null && array.length() > 0;
    }

    /**
     * Creates a copy of the given array with a new size
     * @param array The array to copy
     * @param newSize The size of the new array
     * @return A new array containing elements from the source array, truncated or padded with nulls as needed
     */
    @Nullable
    public static <T> T[] copy(@Nullable T[] array, int newSize) {
        if (array == null) return null;
        return Arrays.copyOf(array, newSize);
    }

    // Primitive array copies

    @Nullable
    public static boolean[] copy(@Nullable boolean[] array) {
        if (array == null) return null;
        return Arrays.copyOf(array, array.length);
    }

    @Nullable
    public static boolean[] copy(@Nullable boolean[] array, int newSize) {
        if (array == null) return null;
        return Arrays.copyOf(array, newSize);
    }

    @Nullable
    public static byte[] copy(@Nullable byte[] array) {
        if (array == null) return null;
        return Arrays.copyOf(array, array.length);
    }

    @Nullable
    public static byte[] copy(@Nullable byte[] array, int newSize) {
        if (array == null) return null;
        return Arrays.copyOf(array, newSize);
    }

    @Nullable
    public static char[] copy(@Nullable char[] array) {
        if (array == null) return null;
        return Arrays.copyOf(array, array.length);
    }

    @Nullable
    public static char[] copy(@Nullable char[] array, int newSize) {
        if (array == null) return null;
        return Arrays.copyOf(array, newSize);
    }

    @Nullable
    public static short[] copy(@Nullable short[] array) {
        if (array == null) return null;
        return Arrays.copyOf(array, array.length);
    }

    @Nullable
    public static short[] copy(@Nullable short[] array, int newSize) {
        if (array == null) return null;
        return Arrays.copyOf(array, newSize);
    }

    @Nullable
    public static int[] copy(@Nullable int[] array) {
        if (array == null) return null;
        return Arrays.copyOf(array, array.length);
    }

    @Nullable
    public static int[] copy(@Nullable int[] array, int newSize) {
        if (array == null) return null;
        return Arrays.copyOf(array, newSize);
    }

    @Nullable
    public static long[] copy(@Nullable long[] array) {
        if (array == null) return null;
        return Arrays.copyOf(array, array.length);
    }

    @Nullable
    public static long[] copy(@Nullable long[] array, int newSize) {
        if (array == null) return null;
        return Arrays.copyOf(array, newSize);
    }

    @Nullable
    public static float[] copy(@Nullable float[] array) {
        if (array == null) return null;
        return Arrays.copyOf(array, array.length);
    }

    @Nullable
    public static float[] copy(@Nullable float[] array, int newSize) {
        if (array == null) return null;
        return Arrays.copyOf(array, newSize);
    }

    @Nullable
    public static double[] copy(@Nullable double[] array) {
        if (array == null) return null;
        return Arrays.copyOf(array, array.length);
    }

    @Nullable
    public static double[] copy(@Nullable double[] array, int newSize) {
        if (array == null) return null;
        return Arrays.copyOf(array, newSize);
    }

    /**
     * Converts any array type to an Object array.
     * @param array The source array (can be primitive or object array)
     * @return Object array containing all elements from the source array
     * @throws IllegalArgumentException if parameter is not an array
     */
    public static Object[] toObjectArray(Object array) {
        if (array instanceof Object[]) {
            return (Object[]) array;
        }

        int length = Array.getLength(array);
        Object[] result = new Object[length];
        for (int i = 0; i < length; i++) {
            result[i] = Array.get(array, i);
        }
        return result;
    }

    /**
     * Combines two arrays into a new array. If one array is null, returns the non-null array.
     * If both are null, returns null.
     * @param first First array
     * @param second Second array
     * @return Combined array, or single non-null array, or null if both are null
     */
    @Nullable
    public static <T> T[] combine(@Nullable T[] first, @Nullable T[] second) {
        if (first == null) return second;
        if (second == null) return first;

        @SuppressWarnings("unchecked")
        T[] result = (T[]) Array.newInstance(first.getClass().getComponentType(),
                first.length + second.length);
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * Combines multiple arrays into a new array. Skips null arrays.
     * If all arrays are null, returns null.
     * @param arrays Arrays to combine
     * @return Combined array containing all elements from non-null arrays, or null if all arrays are null
     */
    @Nullable
    @SafeVarargs
    public static <T> T[] combine(@Nullable T[]... arrays) {
        if (arrays == null || arrays.length == 0) return null;

        // Count total length and find first non-null array
        int totalLength = 0;
        Class<?> componentType = null;
        for (T[] array : arrays) {
            if (array != null) {
                totalLength += array.length;
                if (componentType == null) {
                    componentType = array.getClass().getComponentType();
                }
            }
        }

        // If no non-null arrays found
        if (componentType == null) return null;

        // Create result array
        @SuppressWarnings("unchecked")
        T[] result = (T[]) Array.newInstance(componentType, totalLength);

        // Copy all non-null arrays
        int destinationPos = 0;
        for (T[] array : arrays) {
            if (array != null) {
                System.arraycopy(array, 0, result, destinationPos, array.length);
                destinationPos += array.length;
            }
        }

        return result;
    }

    // Primitive array combinations

    /**
     * Combines two boolean arrays, handling null inputs
     */
    @Nullable
    public static boolean[] combine(@Nullable boolean[] first, @Nullable boolean[] second) {
        if (first == null) return second;
        if (second == null) return first;

        boolean[] result = new boolean[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * Combines two byte arrays, handling null inputs
     */
    @Nullable
    public static byte[] combine(@Nullable byte[] first, @Nullable byte[] second) {
        if (first == null) return second;
        if (second == null) return first;

        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * Combines two char arrays, handling null inputs
     */
    @Nullable
    public static char[] combine(@Nullable char[] first, @Nullable char[] second) {
        if (first == null) return second;
        if (second == null) return first;

        char[] result = new char[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * Combines two int arrays, handling null inputs
     */
    @Nullable
    public static int[] combine(@Nullable int[] first, @Nullable int[] second) {
        if (first == null) return second;
        if (second == null) return first;

        int[] result = new int[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * Combines two long arrays, handling null inputs
     */
    @Nullable
    public static long[] combine(@Nullable long[] first, @Nullable long[] second) {
        if (first == null) return second;
        if (second == null) return first;

        long[] result = new long[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * Combines two float arrays, handling null inputs
     */
    @Nullable
    public static float[] combine(@Nullable float[] first, @Nullable float[] second) {
        if (first == null) return second;
        if (second == null) return first;

        float[] result = new float[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * Combines two double arrays, handling null inputs
     */
    @Nullable
    public static double[] combine(@Nullable double[] first, @Nullable double[] second) {
        if (first == null) return second;
        if (second == null) return first;

        double[] result = new double[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * Converts a List of any type T to an array of type T
     * @param list The list to convert
     * @param clazz The class of type T
     * @return An array containing all elements from the list
     * @throws NullPointerException if list or clazz is null
     */
    @NonNull
    public static <T> T[] toArray(@NonNull List<T> list, @NonNull Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T[] array = (T[]) Array.newInstance(clazz, list.size());
        return list.toArray(array);
    }

    /**
     * Converts a List of Integer to a primitive int array
     * @param list The list to convert
     * @return A primitive int array containing all elements from the list
     * @throws NullPointerException if list is null
     */
    @NonNull
    public static int[] toIntArray(@NonNull List<Integer> list) {
        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i) != null ? list.get(i) : 0;
        }
        return result;
    }

    /**
     * Converts a List of Long to a primitive long array
     * @param list The list to convert
     * @return A primitive long array containing all elements from the list
     * @throws NullPointerException if list is null
     */
    @NonNull
    public static long[] toLongArray(@NonNull List<Long> list) {
        long[] result = new long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i) != null ? list.get(i) : 0L;
        }
        return result;
    }

    /**
     * Converts a List of Double to a primitive double array
     * @param list The list to convert
     * @return A primitive double array containing all elements from the list
     * @throws NullPointerException if list is null
     */
    @NonNull
    public static double[] toDoubleArray(@NonNull List<Double> list) {
        double[] result = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i) != null ? list.get(i) : 0.0;
        }
        return result;
    }

    /**
     * Converts a List of Float to a primitive float array
     * @param list The list to convert
     * @return A primitive float array containing all elements from the list
     * @throws NullPointerException if list is null
     */
    @NonNull
    public static float[] toFloatArray(@NonNull List<Float> list) {
        float[] result = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i) != null ? list.get(i) : 0.0f;
        }
        return result;
    }

    /**
     * Converts a List of Boolean to a primitive boolean array
     * @param list The list to convert
     * @return A primitive boolean array containing all elements from the list
     * @throws NullPointerException if list is null
     */
    @NonNull
    public static boolean[] toBooleanArray(@NonNull List<Boolean> list) {
        boolean[] result = new boolean[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = Boolean.TRUE.equals(list.get(i));
        }
        return result;
    }

    /**
     * Checks if an array is valid (not null and has elements)
     * @param array Array to check
     * @return true if array is not null and has at least one element
     */
    public static <T> boolean isValid(@Nullable T[] array) {
        return array != null && array.length > 0;
    }

    /**
     * Checks if an array is valid (not null and has elements)
     * @param array Array to check
     * @param minSize Minimum Size the Array can be
     * @return true if array is not null and has at least one element
     */
    public static <T> boolean isValid(@Nullable T[] array, int minSize) {
        return array != null && array.length >= minSize;
    }


    public static int getFromStringArray(String[] array, int index, int defaultValue) {
        String x = getElementAtIndexOrDefault(array, index, null);
        return x == null ? defaultValue : Str.tryParseInt(x, defaultValue);
    }

    public static <T> T getElementAtIndexOrDefault(@Nullable T[] array, int index, T defaultValue) {
        return array == null || array.length <= index ?
                defaultValue : array[index];
    }

    /**
     * Checks if an array is valid (not null and has elements) and all elements are non-null
     * @param array Array to check
     * @return true if array is not null, has at least one element, and all elements are non-null
     */
    public static <T> boolean isValidStrict(@Nullable T[] array) {
        if (!isValid(array)) return false;

        for (T element : array) {
            if (element == null) return false;
        }
        return true;
    }

    /**
     * Checks if a primitive array is valid (not null and has elements)
     */
    public static boolean isValid(@Nullable boolean[] array) {
        return array != null && array.length > 0;
    }

    public static boolean isValid(@Nullable byte[] array) {
        return array != null && array.length > 0;
    }

    public static boolean isValid(@Nullable char[] array) {
        return array != null && array.length > 0;
    }

    public static boolean isValid(@Nullable short[] array) {
        return array != null && array.length > 0;
    }

    public static boolean isValid(@Nullable int[] array) {
        return array != null && array.length > 0;
    }

    public static boolean isValid(@Nullable long[] array) {
        return array != null && array.length > 0;
    }

    public static boolean isValid(@Nullable float[] array) {
        return array != null && array.length > 0;
    }

    public static boolean isValid(@Nullable double[] array) {
        return array != null && array.length > 0;
    }

    public static int safeLength(JSONArray array) {
        return array == null ? -1 : array.length();
    }

    /**
     * Gets the size of an array, safely handling null arrays
     * @param array Array to check
     * @return Size of array, or 0 if array is null
     */
    public static <T> int safeLength(@Nullable T[] array) {
        return array != null ? array.length : 0;
    }

    // Safe length checks for primitive arrays
    public static int safeLength(@Nullable boolean[] array) {
        return array != null ? array.length : 0;
    }

    public static int safeLength(@Nullable byte[] array) {
        return array != null ? array.length : 0;
    }

    public static int safeLength(@Nullable char[] array) {
        return array != null ? array.length : 0;
    }

    public static int safeLength(@Nullable short[] array) {
        return array != null ? array.length : 0;
    }

    public static int safeLength(@Nullable int[] array) {
        return array != null ? array.length : 0;
    }

    public static int safeLength(@Nullable long[] array) {
        return array != null ? array.length : 0;
    }

    public static int safeLength(@Nullable float[] array) {
        return array != null ? array.length : 0;
    }

    public static int safeLength(@Nullable double[] array) {
        return array != null ? array.length : 0;
    }
}
