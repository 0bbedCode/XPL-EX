package eu.faircode.xlua.x.ui.core.view_registry;

import androidx.annotation.NonNull;

import java.util.Map;
import java.util.WeakHashMap;

public class RegistryUtils {
    private static final Map<String, Long> HASH_CACHE = new WeakHashMap<>(32);     // Cache for string hashing - uses weak references to avoid memory leaks

    public static void clearCache() { HASH_CACHE.clear(); }

    public static long hashStringToLong_cache(@NonNull String str) {
        Long cached = HASH_CACHE.get(str);
        if (cached != null) {
            return cached;
        }

        // FNV-1a hash - faster than Java's string hash
        long hash = 0xcbf29ce484222325L;
        for (int i = 0; i < str.length(); i++) {
            hash ^= str.charAt(i);
            hash *= 0x100000001b3L;
        }

        HASH_CACHE.put(str, hash);
        return hash;
    }
}
