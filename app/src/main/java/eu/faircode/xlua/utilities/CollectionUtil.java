package eu.faircode.xlua.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class CollectionUtil {
    private static final String TAG = "XLua.CollectionUtil";

    public static <T> T getRandomElement(Collection<T> items) { return getRandomElement(items, null, false); }
    public static <T> T getRandomElement(Collection<T> items, T defaultItem) { return getRandomElement(items, defaultItem, false); }
    public static <T> T getRandomElement(Collection<T> items, T defaultItem, boolean forceClearAfter) {
        if(items == null || items.isEmpty()) return defaultItem;
        T e = getRandomElement(new ArrayList<>(items), defaultItem, forceClearAfter);
        if(forceClearAfter) items.clear();
        return e;
    }

    public static <T> T getRandomElement(List<T> items) { return getRandomElement(items, null, false); }
    public static <T> T getRandomElement(List<T> items, T defaultItem) { return getRandomElement(items, defaultItem, false); }
    public static <T> T getRandomElement(List<T> items, T defaultItem, boolean forceClearAfter) {
        if(items == null || items.isEmpty()) return defaultItem;
        if(items.size() == 1) return items.get(0);
        T e = items.get(RandomGenerator.nextInt(0, items.size()));
        if(forceClearAfter) items.clear();
        return e;
    }

    public static boolean isAllTrue(List<Boolean> results) {
        if(results == null || results.isEmpty()) return false;
        for(int i = 0; i < results.size(); i++)
            if(!results.get(i)) return false;
        return true;
    }

    public static boolean isAllTrue(boolean[] results) {
        if(results == null || results.length < 1) return false;
        for(int i = 0; i < results.length; i++)
            if(!results[i]) return false;
        return true;
    }

    public static boolean isEmptyValuesOrInvalid(Collection<String> collection) {
        if(!isValid(collection))
            return true;

        for(String s : collection) {
            if(!s.isEmpty())
                return false;
        }

        return true;
    }

    public static boolean isValid(Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }
    public static boolean isValid(Collection<?> collection) { return collection != null && !collection.isEmpty(); }

    public static List<String> cleanEmpty(List<String> list) {
        return null;
    }

    public static String[] cleanEmpty(String[] arr) {
        if(arr == null) return null;
        List<String> sList = new ArrayList<>();
        for(String s : arr) {
            if(Str.isValidNotWhitespaces(s))
                sList.add(s);
        }

        return sList.toArray(new String[0]);
    }

    public static ArrayList<?> collectionToArrayList(Collection<?> c) { return new ArrayList<>(c); }
    public static ArrayList<?> setToArrayList(Set<?> s) { return new ArrayList<>(s); }


    /*public static Set<?> toSet(Object o) {
        // Handle the case where the object is null
        if (o == null) { Log.e(TAG, "TO SET OBJECT IS NULL"); return new HashSet<>(); }
        // Handle the case where the object is a Collection
        if (o instanceof Collection) { return new HashSet<>((Collection<?>) o); }
        else if (o.getClass().isArray()) {
            Object[] array = (Object[]) o; // This cast is safe for Object arrays but not for primitive arrays
            return new HashSet<>(Arrays.asList(array));
        }
        else { return new HashSet<>(); }
    }

    public static ArrayList<?> toArrayList(Object o) {
        if (o == null) { Log.e(TAG, "TO ARRAY LIST OBJECT IS NULL"); new ArrayList<>(); }
        if (o instanceof Collection) {
            return new ArrayList<>(((Collection<?>) o));
        } else if (o.getClass().isArray()) {
            return new ArrayList<>(Arrays.asList((Object[]) o));
        } else {
            return new ArrayList<>();
        }
    }*/

    public static int getSize(Object o) {
        if (o == null) return 0;
        if (o instanceof Collection) {
            return ((Collection<?>) o).size();
        } else if (o instanceof Map) {
            return ((Map<?, ?>) o).size();
        } else if (o.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(o);
        } else {
            //throw new IllegalArgumentException("Unsupported object type.");
            return 0;
        }
    }

    public static List<String> getVerifiedStrings(List<String> list, boolean useContains, String... badStrs) { return getVerifiedStrings(list, useContains, Arrays.asList(badStrs)); }
    public static List<String> getVerifiedStrings(List<String> list, boolean useContains, List<String> badList) {
        List<String> strListClean = new ArrayList<>();
        if(useContains) {
            for(String s : list) {
                boolean found = false;
                for(String sRemove : badList) {
                    if(s.contains(sRemove)) {
                        found = true;
                        break;
                    }
                } if(!found) strListClean.add(s);
            }
        }else {
            for(String s : list) {
                boolean found = false;
                for(String sRemove : badList) {
                    if(s.equals(sRemove)) {
                        found = true;
                        break;
                    }
                } if(!found) strListClean.add(s);
            }
        }

        return strListClean;
    }
}
