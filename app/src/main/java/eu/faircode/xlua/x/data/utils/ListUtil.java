package eu.faircode.xlua.x.data.utils;

import androidx.core.util.Predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import eu.faircode.xlua.x.Str;

public class ListUtil {



    public static <T> List<T> arrayToList(T[] array) {
        List<T> items = new ArrayList<>();
        if(ArrayUtils.isValid(array)) {
            for(T item : array) {
                if(item != null && !items.contains(item))
                    items.add(item);
            }
        }

        return items;
    }


    public interface IIndexable<T> {
        boolean isItem(T item, T itemToFind);
    }

    public interface IMapItem<TItem, TKey, TVal> {
        boolean isValid(TItem item);
        TKey getKey(TItem item);
        TVal getValue(TItem item);
    }

    public interface  IToStringItem<T> {
        String toStringItem(T o);
    }

    public interface IIterateVoid<T> {
        void onItem(T o, int index);
    }

    public interface IIteratePairCondition<TFrom, TTo> {
        boolean isFine(TFrom from);
        TTo get(TFrom from);
    }

    public interface IIterateCondition<T> {
        boolean isFine(T item);
    }

    public interface IIteratePairTo<TFrom, TTo> {
        TTo get(TFrom from);
    }

    public interface IIterateGet<T> {
         T onItem(T o);
    }


    public static <TFrom, TTo> List<TTo> forEachTo(Collection<TFrom> items, IIteratePairTo<TFrom, TTo> onItem) {
        List<TTo> list = new ArrayList<>();
        if(!isValid(items) || onItem == null)
            return list;

        for(TFrom item : items) {
            if(item != null) {
                TTo to = onItem.get(item);
                if(to instanceof String) {
                    if(!Str.isEmpty((String)to) && !list.contains(to))
                        list.add(to);
                } else {
                    if(to != null && !list.contains(to)) {
                        list.add(to);
                    }
                }
            }
        }

        return list;
    }

    public static <T> void filterCondition(List<T> items, IIterateCondition<T> onItem) {
        List<T> list = new ArrayList<>();
        if(!isValid(items) || onItem == null)
            return;

        for(T item : items) {
            if(item != null && onItem.isFine(item)) {
                list.add(item);
            }
        }

        items.clear();
        items.addAll(list);
    }

    public static <T> List<T> forEachCondition(Collection<T> items, IIterateCondition<T> onItem) {
        List<T> list = new ArrayList<>();
        if(!isValid(items) || onItem == null)
            return list;

        for(T item : items) {
            if(item != null && onItem.isFine(item)) {
                list.add(item);
            }
        }

        return list;
    }

    public static <TFrom, TTo> List<TTo> forEachConditionTo(List<TFrom> items, IIteratePairCondition<TFrom, TTo> onItem) {
        List<TTo> list = new ArrayList<>();
        if(!isValid(items) || onItem == null)
            return list;

        for(TFrom item : items) {
            if(item != null && onItem.isFine(item)) {
                TTo to = onItem.get(item);
                if(to != null && !list.contains(to)) {
                    list.add(to);
                }
            }
        }

        return list;
    }

    public static <T> List<T> forEach(List<T> items, IIterateGet<T> event) {
        List<T> list = new ArrayList<>();
        if(items == null || items.isEmpty() || event == null)
            return items;

        for(T item : items) {
            if(item == null)
                continue;

            T ret = event.onItem(item);
            if(!list.contains(ret))
                list.add(ret);
        }

        return list;
    }

    public static <T> void forEachVoid(List<T> items, IIterateVoid<T> event) {
        if(items == null || items.isEmpty() || event == null)
            return;

        int i = 0;
        for(T item : items) {
            event.onItem(item, i);
            i++;
        }
    }


    public static void clear(Map<?, ?> map) {
        try {
            if(map != null && !map.isEmpty())
                map.clear();
        }catch (Exception ignored) {
            //Make read only ?
        }
    }

    public static <T> T getFirst(List<T> items) {
        if(items == null || items.isEmpty())
            return null;

        return items.get(0);
    }

    public static void clear(Collection<?> collection) {
        try {
            if(collection != null && !collection.isEmpty())
                collection.clear();
        }catch (Exception ignored) {
            //Make read only ?
        }
    }

    //Not all needed but fuck it I like it xD
    public static boolean isValid(ArrayList<?> collection) { return collection != null && !collection.isEmpty(); }
    public static boolean isValid(ArrayList<?> collection, int minSize) { return collection != null && collection.size() >= minSize; }
    public static boolean isValid(Set<?> collection) { return collection != null && !collection.isEmpty(); }
    public static boolean isValid(Set<?> collection, int minSize) { return collection != null && collection.size() >= minSize; }
    public static boolean isValid(List<?> collection) { return collection != null && !collection.isEmpty(); }
    public static boolean isValid(List<?> collection, int minSize) { return collection != null && collection.size() >= minSize; }
    public static boolean isValid(Collection<?> collection) { return collection != null && !collection.isEmpty(); }
    public static boolean isValid(Collection<?> collection, int minSize) { return collection != null && collection.size() >= minSize; }
    public static boolean isValid(HashSet<?> set) { return set != null && !set.isEmpty(); }
    public static boolean isValid(HashSet<?> set, int minSize) { return set != null && set.size() >= minSize; }
    public static boolean isValid(LinkedHashSet<?> set) { return set != null && !set.isEmpty(); }
    public static boolean isValid(LinkedHashSet<?> set, int minSize) { return set != null && set.size() >= minSize; }
    public static boolean isValid(LinkedList<?> collection) { return collection != null && !collection.isEmpty(); }
    public static boolean isValid(LinkedList<?> collection, int minSize) { return collection != null && collection.size() >= minSize; }
    public static boolean isValid(TreeSet<?> set) { return set != null && !set.isEmpty(); }
    public static boolean isValid(TreeSet<?> set, int minSize) { return set != null && set.size() >= minSize; }


    /**
     * Recursively clears a collection and its nested sub-collections.
     *
     * @param collection the collection to clear
     */
    public static void clearCollection(Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return;
        }

        for (Object item : collection) {
            if (item instanceof Collection) {
                clearCollection((Collection<?>) item); // Recursively clear nested collections
            } else if (item instanceof Map) {
                clearMap((Map<?, ?>) item); // Handle maps if needed
            }
        }
        collection.clear(); // Clear the main collection
    }

    /**
     * Clears a map and its nested structures (collections or maps), assuming all values
     * are of the same type as the first value.
     *
     * @param map the map to clear. If null or empty, no action is performed.
     */
    public static void clearMap(Map<?, ?> map) { clearMap(map, false); }

    /**
     * Recursively clears a map and its nested structures (collections or maps).
     *
     * @param map the map to clear. If null or empty, no action is performed.
     * @param wildCardSubEntries if true, processes each value individually to allow for
     *                           mixed types (collections and maps). If false, assumes all
     *                           values are of the same type as the first value, for better
     *                           performance.
     */
    public static void clearMap(Map<?, ?> map, boolean wildCardSubEntries) {
        if (map == null || map.isEmpty()) {
            return;
        }

        Collection<?> vals = map.values();
        if(!wildCardSubEntries) {
            Object firstValue = vals.iterator().next();
            if (firstValue instanceof Collection) {
                for (Object value : vals) {
                    clearCollection((Collection<?>) value);
                }
            } else if (firstValue instanceof Map) {
                for (Object value : vals) {
                    clearMap((Map<?, ?>) value, false);
                }
            }
        } else {
            for (Object value : map.values()) {
                if (value instanceof Collection) {
                    clearCollection((Collection<?>) value);
                } else if (value instanceof Map) {
                    clearMap((Map<?, ?>) value, true); // Already determined to be a map
                }
            }
        }

        map.clear(); // Clear the main map
    }


    public static <TItem, TKey, TVal> Map<TKey, TVal> toMap(Collection<TItem> items, IMapItem<TItem, TKey, TVal> inv) {
        HashMap<TKey, TVal> map = new HashMap<>();
        for(TItem i : items) {
            if(i != null && inv.isValid(i)) {
                map.put(inv.getKey(i), inv.getValue(i));
            }
        }

        return map;
    }

    public static <T> List<T> nonNull(List<T> items) { return items == null ? emptyList() : items; }

    public static <T> void removeAt(List<T> items, int index) {
        if(index > -1 && items != null && items.size() > index) {
            items.remove(index);
        }
    }

    public static List<String> toLowerCase(List<String> items) {
        if(items == null || items.isEmpty())
            return items;

        List<String> new_list = new ArrayList<>(items.size());
        for(String s : items) {
            if(s != null) {
                new_list.add(s.toLowerCase());
            }
        }

        return new_list;
    }

    public static <T> int indexOf(List<T> items, T item) { return indexOf(items, item, null); }
    public static <T> int indexOf(List<T> items, T item, IIndexable<T> comparer) {
        if(items != null && !items.isEmpty() && item != null) {
            int possibleIndex = items.indexOf(item);
            if(possibleIndex > -1)
                return possibleIndex;

            if(comparer != null) {
                for(int i = 0; i < items.size(); i++) {
                    T o = items.get(i);
                    if(comparer.isItem(o, item))
                        return i;
                }
            } else {
                for(int i = 0; i < items.size(); i++) {
                    T o = items.get(i);
                    if(item.equals(o))
                        return i;
                }
            }
        }

        return -1;
    }

    public static <T> List<String> toStringList(List<T> items) { return toStringList(items, null); }
    public static <T> List<String> toStringList(List<T> items, IToStringItem<T> to) {
        List<String> list = new ArrayList<>();
        if(ListUtil.isValid(items)) {
            for(T item : items) {
                if(to == null)
                    list.add(Str.toStringOrNull(item));
                else
                    list.add(to.toStringItem(item));
            }
        }

        return list;
    }

    public static <T> T getAtIndex(List<T> items, int index) { return getAtIndex(items, index, null); }
    public static <T> T getAtIndex(List<T> items, int index, T defaultValue) { return items != null && items.size() > index ? items.get(index) : defaultValue; }

    public static <T> List<T> emptyList() { return new ArrayList<>(); }

    // List conversions
    public static <T> List<T> copyToList(Collection<T> collection) { return collection != null ? new ArrayList<>(collection) : new ArrayList<T>(); }
    public static <T> List<T> copyToList(Set<T> set) { return set != null ? new ArrayList<>(set) : new ArrayList<T>(); }
    public static <T> List<T> copyToList(ArrayList<T> arrayList) { return arrayList != null ? new ArrayList<>(arrayList) : new ArrayList<T>(); }

    // ArrayList conversions
    public static <T> ArrayList<T> copyToArrayList(Collection<T> collection) { return collection != null && !collection.isEmpty() ? new ArrayList<>(collection) : new ArrayList<T>(); }
    public static <T> ArrayList<T> copyToArrayList(List<T> list) { return list != null ? new ArrayList<>(list) : new ArrayList<T>(); }
    public static <T> ArrayList<T> copyToArrayList(Set<T> set) { return set != null ? new ArrayList<>(set) : new ArrayList<T>(); }

    // Set conversions
    public static <T> Set<T> copyToSet(Collection<T> collection) { return collection != null ? new HashSet<>(collection) : new HashSet<T>(); }
    public static <T> Set<T> copyToSet(List<T> list) { return list != null ? new HashSet<>(list) : new HashSet<T>(); }
    public static <T> Set<T> copyToSet(ArrayList<T> arrayList) { return arrayList != null ? new HashSet<>(arrayList) : new HashSet<T>(); }

    // HashSet conversions
    public static <T> HashSet<T> copyToHashSet(Collection<T> collection) { return collection != null ? new HashSet<>(collection) : new HashSet<T>(); }
    public static <T> HashSet<T> copyToHashSet(List<T> list) { return list != null ? new HashSet<>(list) : new HashSet<T>(); }
    public static <T> HashSet<T> copyToHashSet(ArrayList<T> arrayList) { return arrayList != null ? new HashSet<>(arrayList) : new HashSet<T>(); }

    // LinkedHashSet conversions
    public static <T> LinkedHashSet<T> copyToLinkedHashSet(Collection<T> collection) { return collection != null ? new LinkedHashSet<>(collection) : new LinkedHashSet<T>(); }
    public static <T> LinkedHashSet<T> copyToLinkedHashSet(List<T> list) { return list != null ? new LinkedHashSet<>(list) : new LinkedHashSet<T>(); }
    public static <T> LinkedHashSet<T> copyToLinkedHashSet(ArrayList<T> arrayList) { return arrayList != null ? new LinkedHashSet<>(arrayList) : new LinkedHashSet<T>(); }

    // LinkedList conversions
    public static <T> LinkedList<T> copyToLinkedList(Collection<T> collection) { return collection != null ? new LinkedList<>(collection) : new LinkedList<T>(); }
    public static <T> LinkedList<T> copyToLinkedList(List<T> list) { return list != null ? new LinkedList<>(list) : new LinkedList<T>(); }
    public static <T> LinkedList<T> copyToLinkedList(Set<T> set) { return set != null ? new LinkedList<>(set) : new LinkedList<T>(); }

    // TreeSet conversions
    public static <T extends Comparable<? super T>> TreeSet<T> copyToTreeSet(Collection<T> collection) { return collection != null ? new TreeSet<>(collection) : new TreeSet<T>(); }
    public static <T extends Comparable<? super T>> TreeSet<T> copyToTreeSet(List<T> list) { return list != null ? new TreeSet<>(list) : new TreeSet<T>(); }
    public static <T extends Comparable<? super T>> TreeSet<T> copyToTreeSet(Set<T> set) { return set != null ? new TreeSet<>(set) : new TreeSet<T>(); }

    // Collection conversions
    public static <T> Collection<T> copyToCollection(List<T> list) { return list != null ? new ArrayList<>(list) : new ArrayList<T>(); }
    public static <T> Collection<T> copyToCollection(Set<T> set) { return set != null ? new ArrayList<>(set) : new ArrayList<T>(); }
    public static <T> Collection<T> copyToCollection(ArrayList<T> arrayList) { return arrayList != null ? new ArrayList<>(arrayList) : new ArrayList<T>(); }

    // Immutable conversions
    public static <T> List<T> copyToImmutableList(Collection<T> collection) { return collection != null ? Collections.unmodifiableList(new ArrayList<>(collection)) : Collections.<T>emptyList(); }
    public static <T> Set<T> copyToImmutableSet(Collection<T> collection) { return collection != null ? Collections.unmodifiableSet(new HashSet<>(collection)) : Collections.<T>emptySet(); }

    public static boolean isValid(Hashtable<?, ?> table) { return table != null && !table.isEmpty(); }
    public static boolean isValid(Hashtable<?, ?> table, int minSize) { return table != null && table.size() >= minSize; }
    public static boolean isValid(HashMap<?, ?> map) { return map != null && !map.isEmpty(); }
    public static boolean isValid(HashMap<?, ?> map, int minSize) { return map != null && map.size() >= minSize; }
    public static boolean isValid(LinkedHashMap<?, ?> map) { return map != null && !map.isEmpty(); }
    public static boolean isValid(LinkedHashMap<?, ?> map, int minSize) { return map != null && map.size() >= minSize; }
    public static boolean isValid(Map<?, ?> map) { return map != null && !map.isEmpty(); }
    public static boolean isValid(Map<?, ?> map, int minSize) { return map != null && map.size() >= minSize; }



    public static int size(Map<?, ?> map) { return map != null ? map.size() : 0; }
    public static int size(Collection<?> collection) { return collection != null ? collection.size() : 0; }

    public static <T> List<T> ensureIsValidOrEmptyList(List<T> list) {
        return list == null ? emptyList() : list;
    }

    public static <T> List<T> toSingleList(T item) {
        List<T> list = new ArrayList<>();
        if(item != null)
            list.add(item);

        return list;
    }

    public static <K, V> boolean addAllIfValid(Map<K, V> baseMap, Map<? extends K, ? extends V> newElements)
    {
        return addAllIfValid(baseMap, newElements, false);
    }

    /**
     * Adds all elements from the newElements map to the baseMap if the newElements map is valid.
     * Optionally clears the original map before adding.
     *
     * @param baseMap       The map to which elements will be added.
     * @param newElements   The map containing elements to add.
     * @param clearOriginal If true, clears the baseMap before adding.
     * @param <K>           The type of keys in the map.
     * @param <V>           The type of values in the map.
     * @return True if elements were added successfully, false otherwise.
     */
    public static <K, V> boolean addAllIfValid(Map<K, V> baseMap, Map<? extends K, ? extends V> newElements, boolean clearOriginal) {
        if (baseMap == null) {
            return false; // Base map is null
        }

        if (clearOriginal) {
            baseMap.clear(); // Clear the base map if specified
        }

        if (!isValid(newElements)) {
            return false; // newElements is not valid
        }

        baseMap.putAll(newElements); // Add all elements from newElements to baseMap
        return !newElements.isEmpty(); // Return true if newElements had at least one entry
    }

    public static <T> boolean addAllIfValid(Collection<T> baseList, Collection<T> newElements, boolean clearOriginal) {
        if(baseList == null)
            return false;

        if(clearOriginal)
            baseList.clear();

        if(!isValid(newElements))
            return false;

        return baseList.addAll(newElements);
    }

    public static <T> boolean addAllIfValid(List<T> baseList, List<T> newElements, boolean clearOriginal) {
        if(baseList == null)
            return false;

        if(clearOriginal)
            baseList.clear();

        if(!isValid(newElements))
            return false;

        int oldSize = baseList.size();
        for(T newElement : newElements)
            if(newElement != null && !baseList.contains(newElement))
                baseList.add(newElement);

        return oldSize != baseList.size();
    }

    public static <T> boolean addAllIfValid(Collection<T> baseList, Collection<T> newElements) {
        if(baseList == null || !isValid(newElements))
            return false;

        return baseList.addAll(newElements);
    }


    public static <T> boolean addAllIfValidEx(List<T> baseList, List<T> newElements) {
        if(baseList == null || !isValid(newElements))
            return false;

        for(T item : newElements) {
            if(item != null && !baseList.contains(item))
                baseList.add(item);
        }

        //return baseList.addAll(newElements);
        return true;
    }


    public static <T> boolean addAllIfValid(List<T> baseList, List<T> newElements) {
        if(baseList == null || !isValid(newElements))
            return false;

        for(T el : newElements) {
            if(el != null && !baseList.contains(el)) {
                baseList.add(el);
            }
        }

        return ListUtil.isValid(baseList);
    }

    public static <T> Collection<T> combine(Collection<T> a, Collection<T> b) { return combine(a, b, true, true); }
    public static <T> Collection<T> combine(Collection<T> a, Collection<T> b, boolean ifTrueCondition) { return combine(a, b, ifTrueCondition, true); }
    public static <T> Collection<T> combine(Collection<T> a, Collection<T> b, boolean ifTrueCondition, boolean useAFirst) {
        Collection<T> items = new ArrayList<>();
        Collection<T> firstHalf = useAFirst ? a : b;
        Collection<T> secondHalf = useAFirst ? b : a;
        if(isValid(firstHalf)) items.addAll(firstHalf);
        if(isValid(secondHalf) && ifTrueCondition) items.addAll(secondHalf);
        return items;
    }

    public static <T> List<T> combine(List<T> a, List<T> b) { return combine(a, b, true, true); }
    public static <T> List<T> combine(List<T> a, List<T> b, boolean ifTrueCondition) { return combine(a, b, ifTrueCondition, true); }
    public static <T> List<T> combine(List<T> a, List<T> b, boolean ifTrueCondition, boolean useAFirst) {
        List<T> items = new ArrayList<>();
        List<T> firstHalf = useAFirst ? a : b;
        List<T> secondHalf = useAFirst ? b : a;
        if(isValid(firstHalf)) items.addAll(firstHalf);
        if(isValid(secondHalf) && ifTrueCondition) items.addAll(secondHalf);
        return items;
    }

    public static <K, V> Map<K, V> filterMap(Map<K, V> map, K badKey, V badValue) { return filterMap(map, badKey, badValue, false, false); }
    public static <K, V, BK> Map<K, V> filterMap(Map<K, V> map, BK badKey) { return filterMap(map, badKey, null, false, true); }
    public static <K, V, BV> Map<K, V> filterMapByValue(Map<K, V> map, BV badValue) { return filterMap(map, null, badValue, true, false); }
    public static <K, V, BK> Map<K, V> filterMap(Map<K, V> map, BK badKey, boolean allowNullKeys) { return filterMap(map, badKey, null, allowNullKeys, true); }
    public static <K, V, BV> Map<K, V> filterMapByValue(Map<K, V> map, BV badValue, boolean allowNullValues) { return filterMap(map, null, badValue, true, allowNullValues); }
    public static <K, V> Map<K, V> filterMap(Map<K, V> map, K badKey, V badValue, boolean allowNull) { return filterMap(map, badKey, badValue, allowNull, allowNull); }
    public static <K, V> Map<K, V> filterMap(Map<K, V> map, Collection<K> badKeys) { return filterMap(map, badKeys, null, false, true); }
    public static <K, V> Map<K, V> filterMapByValue(Map<K, V> map, Collection<V> badValues) { return filterMap(map, null, badValues, true, false); }
    public static <K, V> Map<K, V> filterMap(Map<K, V> map, K[] badKeys) { return filterMap(map, Arrays.asList(badKeys), null, false, true); }
    public static <K, V> Map<K, V> filterMapByValue(Map<K, V> map, V[] badValues) { return filterMap(map, null, Arrays.asList(badValues), true, false); }

    public static <K, V> Map<K, V> filterMapByPredicates(Map<K, V> map, Predicate<K> keyPredicate, Predicate<V> valuePredicate) {
        if (!isValid(map)) return new HashMap<>();
        Map<K, V> filteredMap = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (keyPredicate.test(entry.getKey()) && valuePredicate.test(entry.getValue())) {
                filteredMap.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredMap;
    }

    public static <K, V> Map<K, V> filterMap(Map<K, V> map, final Predicate<K> keyPredicate) {
        return filterMapByPredicates(map, keyPredicate, new Predicate<V>() {
            @Override public boolean test(V v) { return true; }
        });
    }

    public static <K, V> Map<K, V> filterMapByValue(Map<K, V> map, final Predicate<V> valuePredicate) {
        return filterMapByPredicates(map, new Predicate<K>() {
            @Override public boolean test(K k) { return true; }
        }, valuePredicate);
    }

    public static <K, V, BK, BV> Map<K, V> filterMap(Map<K, V> map, BK badKey, BV badValue, boolean allowNullKeys, boolean allowNullValues) {
        Map<K, V> filteredMap = new HashMap<>();
        if(isValid(map)) {
            for(Map.Entry<K, V> item : map.entrySet()) {
                K originalKey = item.getKey();
                V originalValue = item.getValue();
                if(isFine(originalKey, badKey, allowNullKeys) && isFine(originalValue, badValue, allowNullValues)) {
                    filteredMap.put(originalKey, originalValue);
                }
            }
        }

        return filteredMap;
    }

    @SuppressWarnings("unchecked")
    private static <T, BT> boolean isFine(T one, BT two, boolean allowNull) {
        if(one == null || two == null) return one == null && allowNull && two == null;
        // Handle arrays
        if (one.getClass().isArray()) {
            if (two.getClass().isArray()) {
                Object[] oneArr = ArrayUtils.toObjectArray(one);
                Object[] twoArr = ArrayUtils.toObjectArray(two);
                for (Object oneElem : oneArr) {
                    for (Object twoElem : twoArr) {
                        if (Objects.equals(oneElem, twoElem)) return false;
                    }
                }
                return true;
            }


            // Array contains single element
            Object[] arr = ArrayUtils.toObjectArray(one);
            for (Object elem : arr) {
                if (Objects.equals(elem, two)) return false;
            }
            return true;
        }

        // Handle collections
        if (one instanceof Collection) {
            Collection<?> oneCollection = (Collection<?>) one;
            if (two instanceof Collection) {
                Collection<?> twoCollection = (Collection<?>) two;
                for (Object oneElem : oneCollection) {
                    for (Object twoElem : twoCollection) {
                        if (Objects.equals(oneElem, twoElem)) return false;
                    }
                }
                return true;
            }
            // Collection contains single element
            return !oneCollection.contains(two);
        }

        // Handle Maps
        if (one instanceof Map) {
            Map<?, ?> oneMap = (Map<?, ?>) one;
            if (two instanceof Map) {
                Map<?, ?> twoMap = (Map<?, ?>) two;
                for (Map.Entry<?, ?> oneEntry : oneMap.entrySet()) {
                    for (Map.Entry<?, ?> twoEntry : twoMap.entrySet()) {
                        if (Objects.equals(oneEntry.getKey(), twoEntry.getKey()) &&
                                Objects.equals(oneEntry.getValue(), twoEntry.getValue())) return false;
                    }
                }
                return true;
            }
            // Map contains single key or value
            return !oneMap.containsKey(two) && !oneMap.containsValue(two);
        }

        // Handle strings (check if one string contains another)
        if (one instanceof String && two instanceof String) {
            return !((String) one).contains((String) two);
        }

        // Default case: simple equality check
        return !Objects.equals(one, two);
    }
}