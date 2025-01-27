package eu.faircode.xlua.x.data.utils.random.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import eu.faircode.xlua.x.data.utils.random.IRandomizerProvider;
import eu.faircode.xlua.x.data.utils.random.RandomStringHelper;
import eu.faircode.xlua.x.data.utils.random.RandomStringKind;

public abstract class RandomProviderBase implements IRandomizerProvider  {
    /**
     * Generates a random alphanumeric string of default length (16).
     * @return A random string
     */
    @Override
    public String nextString() { return RandomStringHelper.generateString(this); }

    /**
     * Generates a random alphanumeric string of specified length.
     * If length <= 0, generates a string of default length (16).
     * @param length The length of the string
     * @return A random string
     */
    @Override
    public String nextString(int length) { return RandomStringHelper.generateString(this, length); }

    /**
     * Generates a random alphanumeric string with length between origin and bound.
     * If origin > bound, the values are swapped.
     * If bound <= 0, generates a string of default length (16).
     * @param origin The minimum length (inclusive)
     * @param bound The maximum length (exclusive)
     * @return A random string
     */
    @Override
    public String nextString(int origin, int bound) { return RandomStringHelper.generateString(this, origin, bound); }

    /**
     * Generates a random string of specified kind with default length (16).
     * @param kind The kind of string to generate (e.g., ALPHA_NUMERIC, HEX)
     * @return A random string
     */
    @Override
    public String nextString(RandomStringKind kind) { return RandomStringHelper.generateString(this, kind); }

    /**
     * Generates a random string of specified kind and length.
     * If length <= 0, generates a string of default length (16).
     * @param kind The kind of string to generate (e.g., ALPHA_NUMERIC, HEX)
     * @param length The length of the string
     * @return A random string
     */
    @Override
    public String nextString(RandomStringKind kind, int length) { return RandomStringHelper.generateString(this, kind, length); }

    /**
     * Generates a random string of specified kind with length between origin and bound.
     * If origin > bound, the values are swapped.
     * If bound <= 0, generates a string of default length (16).
     * @param kind The kind of string to generate (e.g., ALPHA_NUMERIC, HEX)
     * @param origin The minimum length (inclusive)
     * @param bound The maximum length (exclusive)
     * @return A random string
     */
    @Override
    public String nextString(RandomStringKind kind, int origin, int bound) { return RandomStringHelper.generateString(this, kind, origin, bound); }

    /**
     * Generates a random hexadecimal string of default length (16).
     * @return A random hexadecimal string
     */
    @Override
    public String nextStringHex() { return RandomStringHelper.generateString(this, RandomStringKind.HEX); }

    /**
     * Generates a random hexadecimal string of specified length.
     * If length <= 0, generates a string of default length (16).
     * @param length The length of the string
     * @return A random hexadecimal string
     */
    @Override
    public String nextStringHex(int length) { return RandomStringHelper.generateString(this, RandomStringKind.HEX, length); }

    /**
     * Generates a random hexadecimal string with length between origin and bound.
     * If origin > bound, the values are swapped.
     * If bound <= 0, generates a string of default length (16).
     * @param origin The minimum length (inclusive)
     * @param bound The maximum length (exclusive)
     * @return A random hexadecimal string
     */
    @Override
    public String nextStringHex(int origin, int bound) { return RandomStringHelper.generateString(this, RandomStringKind.HEX, origin, bound); }

    /**
     * Generates a random numeric string of default length (16).
     * @return A random numeric string
     */
    @Override
    public String nextStringNumeric() { return RandomStringHelper.generateString(this, RandomStringKind.NUMERIC); }

    /**
     * Generates a random numeric string of specified length.
     * If length <= 0, generates a string of default length (16).
     * @param length The length of the string
     * @return A random numeric string
     */
    @Override
    public String nextStringNumeric(int length) { return RandomStringHelper.generateString(this, RandomStringKind.NUMERIC, length); }

    /**
     * Generates a random numeric string with length between origin and bound.
     * If origin > bound, the values are swapped.
     * If bound <= 0, generates a string of default length (16).
     * @param origin The minimum length (inclusive)
     * @param bound The maximum length (exclusive)
     * @return A random numeric string
     */
    @Override
    public String nextStringNumeric(int origin, int bound) { return RandomStringHelper.generateString(this, RandomStringKind.NUMERIC, origin, bound); }

    /**
     * Generates a random alphanumeric string of default length (16).
     * @return A random alphanumeric string
     */
    @Override
    public String nextStringAlpha() { return RandomStringHelper.generateString(this, RandomStringKind.ALPHA_NUMERIC); }

    /**
     * Generates a random alphanumeric string of specified length.
     * If length <= 0, generates a string of default length (16).
     * @param length The length of the string
     * @return A random alphanumeric string
     */
    @Override
    public String nextStringAlpha(int length) { return RandomStringHelper.generateString(this, RandomStringKind.ALPHA_NUMERIC, length); }

    /**
     * Generates a random alphanumeric string with length between origin and bound.
     * If origin > bound, the values are swapped.
     * If bound <= 0, generates a string of default length (16).
     * @param origin The minimum length (inclusive)
     * @param bound The maximum length (exclusive)
     * @return A random alphanumeric string
     */
    @Override
    public String nextStringAlpha(int origin, int bound) { return RandomStringHelper.generateString(this, RandomStringKind.ALPHA_NUMERIC, origin, bound); }


    /**
     * Selects a random element from an array.
     * Returns null if the array is null or empty.
     * @param array The array to select from
     * @param <T> The type of elements in the array
     * @return A randomly selected element, or null if the array is empty or null
     */
    @Override
    public <T> T nextElement(T[] array) { return array == null || array.length == 0 ? null : array[nextInt(array.length)]; }

    /**
     * Selects a random element from an array within a specific range.
     * Returns null if the array is null or the range is invalid.
     * @param array The array to select from
     * @param origin The start index (inclusive)
     * @param bound The end index (exclusive)
     * @param <T> The type of elements in the array
     * @return A randomly selected element, or null if the array is null or the range is invalid
     */
    @Override
    public <T> T nextElement(T[] array, int origin, int bound) {
        if (array == null || origin >= bound || origin < 0 || bound > array.length)
            return null;

        return array[nextInt(origin, bound)];
    }

    /**
     * Selects a random element from an array starting at a specific index.
     * Returns null if the array is null, empty, or the index is out of bounds.
     * @param array The array to select from
     * @param startIndex The index to start selection from
     * @param <T> The type of elements in the array
     * @return A randomly selected element, or null if the array is null, empty, or the index is invalid
     */
    @Override
    public <T> T nextElement(T[] array, int startIndex) {
        if (array == null || startIndex < 0 || startIndex >= array.length)
            return null;

        return array[nextInt(startIndex, array.length)];
    }

    /**
     * Selects a random element from a collection.
     * Returns null if the collection is null or empty.
     * @param collection The collection to select from
     * @param <T> The type of elements in the collection
     * @return A randomly selected element, or null if the collection is empty or null
     */
    @Override
    public <T> T nextElement(Collection<T> collection) {
        return collection == null || collection.isEmpty() ? null :
                new ArrayList<>(collection).get(nextInt(collection.size()));
    }

    /**
     * Selects a random element from a collection within a specific range.
     * Creates a copy of the collection to access elements by index.
     * Returns null if the collection is null, empty, or the range is invalid.
     * @param collection The collection to select from
     * @param origin The start index (inclusive)
     * @param bound The end index (exclusive)
     * @param <T> The type of elements in the collection
     * @return A randomly selected element, or null if the collection is empty, null, or the range is invalid
     */
    @Override
    public <T> T nextElement(Collection<T> collection, int origin, int bound) {
        if (collection == null || collection.isEmpty() || origin >= bound || origin < 0 || bound > collection.size())
            return null;

        return new ArrayList<>(collection).get(nextInt(origin, bound));
    }

    /**
     * Selects a random element from a collection starting at a specific index.
     * Creates a copy of the collection to access elements by index.
     * Returns null if the collection is null, empty, or the index is out of bounds.
     * @param collection The collection to select from
     * @param startIndex The index to start selection from
     * @param <T> The type of elements in the collection
     * @return A randomly selected element, or null if the collection is empty, null, or the index is invalid
     */
    @Override
    public <T> T nextElement(Collection<T> collection, int startIndex) {
        if (collection == null || collection.isEmpty() || startIndex < 0 || startIndex >= collection.size())
            return null;

        return new ArrayList<>(collection).get(nextInt(startIndex, collection.size()));
    }


    /**
     * Selects a random number of unique elements from the given array.
     * The selected elements are removed from the array during selection to prevent duplicates.
     * If the array is null or empty, an empty list is returned.
     *
     * @param array The source array from which to select elements.
     * @param <T>   The type of elements in the array.
     * @return A list containing the randomly selected elements.
     */
    @Override
    public <T> List<T> nextElements(T[] array) {
        if (array == null || array.length == 0)
            return Collections.emptyList();

        List<T> list = new ArrayList<>(Arrays.asList(array));
        List<T> result = new ArrayList<>();
        int count = nextInt(1, list.size() + 1); // Random count between 1 and list size

        for (int i = 0; i < count; i++) {
            int index = nextInt(list.size());
            result.add(list.remove(index));
        }

        return result;
    }

    /**
     * Selects a random number of unique elements from the given collection.
     * The selected elements are removed from the collection during selection to prevent duplicates.
     * If the collection is null or empty, an empty list is returned.
     *
     * @param collection The source collection from which to select elements.
     * @param <T>        The type of elements in the collection.
     * @return A list containing the randomly selected elements.
     */
    @Override
    public <T> List<T> nextElements(Collection<T> collection) {
        if (collection == null || collection.isEmpty())
            return Collections.emptyList();

        List<T> list = new ArrayList<>(collection);
        List<T> result = new ArrayList<>();
        int count = nextInt(1, list.size() + 1); // Random count between 1 and list size

        for (int i = 0; i < count; i++) {
            int index = nextInt(list.size());
            result.add(list.remove(index));
        }

        return result;
    }
}
