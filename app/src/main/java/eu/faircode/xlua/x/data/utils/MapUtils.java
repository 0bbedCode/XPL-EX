package eu.faircode.xlua.x.data.utils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
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
     * Finds the first key associated with the given value in the map.
     * Returns null if the map is null, empty, or value is not found.
     * Never throws exceptions.
     *
     * @param map The map to search in
     * @param value The value to look up
     * @return The first key associated with the value, or null if not found/invalid map
     */
    public static <K, V> K getKeyByValue(Map<K, V> map, V value) {
        if (map == null || map.isEmpty() || value == null) {
            return null;
        }

        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Finds the first key associated with the given value in the map.
     * Returns defaultKey if the map is null, empty, or value is not found.
     * Never throws exceptions.
     *
     * @param map The map to search in
     * @param value The value to look up
     * @param defaultKey The key to return if value not found/invalid map
     * @return The first key associated with the value, or defaultKey if not found/invalid map
     */
    public static <K, V> K getKeyByValue(Map<K, V> map, V value, K defaultKey) {
        if (map == null || map.isEmpty() || value == null) {
            return defaultKey;
        }

        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return defaultKey;
    }

    /**
     * Finds all keys associated with the given value in the map.
     * Returns empty list if the map is null, empty, or value is not found.
     * Never throws exceptions.
     *
     * @param map The map to search in
     * @param value The value to look up
     * @return List of keys associated with the value, or empty list if not found/invalid map
     */
    public static <K, V> List<K> getAllKeysByValue(Map<K, V> map, V value) {
        if (map == null || map.isEmpty() || value == null) {
            return new ArrayList<>();
        }

        List<K> keys = new ArrayList<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    /**
     * Retrieves a value from the map for the given key.
     * Returns null if the map is null, empty, or key is not found.
     * Never throws exceptions.
     *
     * @param map The map to search in
     * @param key The key to look up
     * @return The value associated with the key, or null if not found/invalid map
     */
    public static <K, V> V get(Map<K, V> map, K key) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        return map.get(key);
    }

    /**
     * Retrieves a value from the map for the given key.
     * Returns the defaultValue if the map is null, empty, or key is not found.
     * Does not modify the original map.
     * Never throws exceptions.
     *
     * @param map The map to search in
     * @param key The key to look up
     * @param defaultValue The value to return if map is invalid or key not found
     * @return The value associated with the key, or defaultValue if not found/invalid map
     */
    public static <K, V> V get(Map<K, V> map, K key, V defaultValue) {
        if (map == null || map.isEmpty()) {
            return defaultValue;
        }
        V value = map.get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Retrieves a value from the map for the given key.
     * Returns the defaultValue if the map is null or key is not found.
     * If setDefaultIfMissing is true and the map is valid:
     *   - Adds defaultValue if key not found
     *   - Adds defaultValue if map is empty
     * Never throws exceptions.
     *
     * @param map The map to search in
     * @param key The key to look up
     * @param defaultValue The value to use if key is not found
     * @param setDefaultIfMissing If true, adds defaultValue to map when key not found
     * @return The value associated with the key, or defaultValue if not found/invalid map
     */
    public static <K, V> V get(Map<K, V> map, K key, V defaultValue, boolean setDefaultIfMissing) {
        if (map == null) {
            return defaultValue;
        }

        if (setDefaultIfMissing && key != null) {
            if (map.isEmpty() || !map.containsKey(key)) {
                map.put(key, defaultValue);
                return defaultValue;
            }
        }

        V value = map.get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Retrieves a value from the map for the given key.
     * If the map is valid and the key is not found, adds the defaultValue to the map.
     * Returns defaultValue if map is null.
     * Equivalent to get(map, key, defaultValue, true).
     * Never throws exceptions.
     *
     * @param map The map to search in
     * @param key The key to look up
     * @param defaultValue The value to use if key is not found
     * @return The value associated with the key, or defaultValue if not found/invalid map
     */
    public static <K, V> V getAndSetDefault(Map<K, V> map, K key, V defaultValue) {
        return get(map, key, defaultValue, true);
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