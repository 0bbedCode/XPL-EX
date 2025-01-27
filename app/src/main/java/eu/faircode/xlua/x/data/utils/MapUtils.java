package eu.faircode.xlua.x.data.utils;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MapUtils {
    private MapUtils() {
        // Prevent instantiation
    }

    /**
     * Creates an immutable Map with a single entry
     * @param key The key
     * @param value The value
     * @return An immutable Map containing the single entry
     */
    @NonNull
    public static <K, V> Map<K, V> of(@NonNull K key, V value) {
        return Collections.singletonMap(key, value);
    }

    /**
     * Creates a mutable HashMap with the given entries
     * @param entries Array of MapEntry objects defining key-value pairs
     * @return A new HashMap containing the specified entries
     */
    @NonNull
    @SafeVarargs
    public static <K, V> HashMap<K, V> create(@NonNull Entry<K, V>... entries) {
        HashMap<K, V> map = new HashMap<>(entries.length);
        for (Entry<K, V> entry : entries) {
            map.put(entry.key, entry.value);
        }
        return map;
    }

    /**
     * Creates an Entry object for use with the create method
     * @param key The key
     * @param value The value
     * @return A MapEntry containing the key-value pair
     */
    @NonNull
    public static <K, V> Entry<K, V> entry(@NonNull K key, V value) {
        return new Entry<>(key, value);
    }

    /**
     * Convenience method to create a list with a single element
     */
    @NonNull
    public static <T> List<T> list(T item) {
        return Collections.singletonList(item);
    }

    /**
     * Convenience method to create a list with multiple elements
     */
    @NonNull
    @SafeVarargs
    public static <T> List<T> list(T... items) {
        return Arrays.asList(items);
    }

    /**
     * Helper class to hold key-value pairs
     */
    public static class Entry<K, V> {
        final K key;
        final V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}