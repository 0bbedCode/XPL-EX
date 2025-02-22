package eu.faircode.xlua.x.data.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Linq {
    public interface IIWhereFirst<T> { boolean metCondition(T item); }

    public static <T> List<T> toList(T[] arr) {
        List<T> items = new ArrayList<>();
        if(arr == null || arr.length < 1)
            return items;

        for(T item : arr) {
            if(item != null) {
                items.add(item);
            }
        }

        return items;
    }

    public static <T> T firstWhere(Collection<T> items, T defaultValue, IIWhereFirst<T> whereCondition) {
        T first = firstWhere(items, whereCondition);
        return first == null ? defaultValue : first;
    }

    public static <T> T firstWhere(Collection<T> items, IIWhereFirst<T> whereCondition) {
        if(items == null || items.isEmpty())
            return null;

        if(whereCondition == null)
            return null;

        for(T item : items) {
            if(item != null) {
                if(whereCondition.metCondition(item))
                    return item;
            }
        }

        return null;
    }

    public static <T> T firstWhereOrDefault(T[] items, T defaultValue, IIWhereFirst<T> whereCondition) {
        T first = firstWhereArray(items, whereCondition);
        return first == null ? defaultValue : first;
    }

    public static <T> T firstWhereArray(T[] items, IIWhereFirst<T> whereCondition) {
        if(items == null || items.length < 1)
            return null;

        if(whereCondition == null)
            return null;

        for(T item : items) {
            if(item != null) {
                if(whereCondition.metCondition(item))
                    return item;
            }
        }

        return null;
    }



    public interface IIMapWhereFirstReturnKey<K, V> { boolean metCondition(K key, V value); }
    public interface IIMapWhereFirstReturnValue<K, V> { boolean metCondition(K key, V value); }

    /**
     * Finds the first key in a map where the condition is met
     * Returns null if map is null/empty or no match found
     */
    public static <K, V> K firstWhereKey(Map<K, V> map, IIMapWhereFirstReturnKey<K, V> whereCondition) {
        if (map == null || map.isEmpty() || whereCondition == null) {
            return null;
        }

        for (Map.Entry<K, V> entry : map.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            if (key != null && value != null) {
                if (whereCondition.metCondition(key, value)) {
                    return key;
                }
            }
        }

        return null;
    }

    /**
     * Finds the first key in a map where the condition is met
     * Returns defaultKey if map is null/empty or no match found
     */
    public static <K, V> K firstWhereKey(Map<K, V> map, K defaultKey, IIMapWhereFirstReturnKey<K, V> whereCondition) {
        K first = firstWhereKey(map, whereCondition);
        return first == null ? defaultKey : first;
    }

    /**
     * Finds the first value in a map where the condition is met
     * Returns null if map is null/empty or no match found
     */
    public static <K, V> V firstWhereValue(Map<K, V> map, IIMapWhereFirstReturnValue<K, V> whereCondition) {
        if (map == null || map.isEmpty() || whereCondition == null) {
            return null;
        }

        for (Map.Entry<K, V> entry : map.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            if (key != null && value != null) {
                if (whereCondition.metCondition(key, value)) {
                    return value;
                }
            }
        }

        return null;
    }

    /**
     * Finds the first value in a map where the condition is met
     * Returns defaultValue if map is null/empty or no match found
     */
    public static <K, V> V firstWhereValue(Map<K, V> map, V defaultValue, IIMapWhereFirstReturnValue<K, V> whereCondition) {
        V first = firstWhereValue(map, whereCondition);
        return first == null ? defaultValue : first;
    }
}
