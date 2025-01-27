package eu.faircode.xlua.x.data.utils.random;


import java.util.Collection;
import java.util.List;


import eu.faircode.xlua.x.data.utils.DateUtils;
import eu.faircode.xlua.x.data.utils.random.providers.RandomGenericProvider;
import eu.faircode.xlua.x.data.utils.random.providers.RandomSecureProvider;
import eu.faircode.xlua.x.data.utils.random.providers.RandomThreadLocalProvider;

public final class RandomGenerator {
    private static RandomSeedMode seedMode = RandomSeedMode.DEFAULT;
    private static RandomProviderKind providerKind = RandomProviderKind.SECURE;
    private static boolean syncChanges = true;  // Default to sync for backward compatibility

    /**
     * Controls whether changes to provider kind and seed mode affect existing instances
     * @param sync If true, existing provider instances are updated; if false, only new instances are affected
     */
    public static void setSyncChanges(boolean sync) { syncChanges = sync; }

    /**
     * Sets the random provider kind
     * If sync is enabled, recreates all existing provider instances
     * If sync is disabled, only affects new provider instances
     * @param kind The provider kind to use
     */
    public static void setProvider(RandomProviderKind kind) {
        providerKind = kind;
        if (syncChanges) {
            randomProvider.set(createNewProvider());
        }
    }

    /**
     * Sets the seed generation mode
     * If sync is enabled, updates all existing provider instances
     * If sync is disabled, only affects new provider instances
     * @param mode The seed mode to use
     */
    public static void setSeedGenerationMode(RandomSeedMode mode) {
        seedMode = mode;
        if (syncChanges) {
            randomProvider.get().setSeedMode(mode);
        }
    }

    /**
     * Removes the provider for the current thread only
     * Safe to call from any thread, won't affect other threads
     * You DO not need to call to this as Java Garbage Collection System Cleans it up once the Thread Dies
     */
    public static void clearCurrentThread() { randomProvider.remove(); }

    /**
     * Attempts to clear all provider instances
     * Note: Only affects threads that have used RandomGenerator
     * New requests will create fresh providers
     * Do not need to use this, but here if you want to.
     */
    public static void clearAllProviders() { try { randomProvider.remove(); } catch (Exception ignored) { } }

    /**
     * This is used to Give Each Thread their OWN Randomizer Object (Random, ThreadLocalRandom, or SecureRandom)
     * When that Same Thread calls to any Static Function in here to Randomize Data it will use the Random Object that was originally created for it
     * If that thread Dies the Garbage Collection System should Flush it out / Dispose of it
     * If SYNC is Disabled any Changes to the Randomizer Provider Types or Modes will not be Applied to Already Created Random Object only for the ones that get Created after the changes
     * If SYNC is Enabled any Changes to the Randomizer Provider Types or Modes will be Applied to all already Created Random Objects
     */
    private static final ThreadLocal<IRandomizerProvider> randomProvider = new ThreadLocal<IRandomizerProvider>() {
        @Override
        protected IRandomizerProvider initialValue() { return createNewProvider(); }
    };

    /**
     * Creates a new Random Object Provider for the Thread that is Requesting it (Thread that is trying to Generate Random Data)
     * Once its created it will be Cached in for later use within that Thread Unless that Thread Dies
     */
    private static IRandomizerProvider createNewProvider() {
        switch (providerKind) {
            case GENERIC: return new RandomGenericProvider(seedMode);
            case THREAD_LOCAL: return new RandomThreadLocalProvider(seedMode);
            case SECURE:
            default: return new RandomSecureProvider(seedMode);
        }
    }

    /**
     * Get a new Instance or Already cached in Instance of a Random Object either (Random, ThreadLocalRandom, or SecureRandom)
     * If the requesting thread already had a Random Object Created for it, it will be Cached in for later use unless that Thread Dies then it will be disposed of
     */
    private static IRandomizerProvider getRandom() { return randomProvider.get(); }

    public static boolean nextBoolean() { return getRandom().nextBoolean(); }

    public static boolean chance() { return getRandom().chance(); }
    public static boolean chance(int chance) { return getRandom().chance(chance); }

    public static byte nextByte() { return getRandom().nextByte(); }
    public static byte[] nextBytes() { return getRandom().nextBytes(); }
    public static byte[] nextBytes(int bound) { return getRandom().nextBytes(bound); }
    public static byte[] nextBytes(int origin, int bound) { return getRandom().nextBytes(origin, bound); }

    public static short nextShort() { return getRandom().nextShort(); }
    public static short nextShort(short bound) { return getRandom().nextShort(bound); }
    public static short nextShort(short origin, short bound) { return getRandom().nextShort(origin, bound); }

    public static int nextInt() { return getRandom().nextInt(); }
    public static int nextInt(int origin, int bound) { return getRandom().nextInt(origin, bound); }
    public static int nextInt(int bound) { return getRandom().nextInt(bound); }

    public static long nextLong() { return getRandom().nextLong(); }
    public static long nextLong(long bound) { return getRandom().nextLong(bound); }
    public static long nextLong(long origin, long bound) { return getRandom().nextLong(origin, bound); }

    public static double nextDouble() { return getRandom().nextDouble(); }
    public static double nextDouble(double bound) { return getRandom().nextDouble(bound); }
    public static double nextDouble(double origin, double bound) { return getRandom().nextDouble(origin, bound); }

    public static float nextFloat() { return getRandom().nextFloat(); }
    public static float nextFloat(float bound) { return getRandom().nextFloat(bound); }
    public static float nextFloat(float origin, float bound) { return getRandom().nextFloat(origin, bound); }

    public static String nextString() { return getRandom().nextString(); }
    public static String nextString(int length) { return getRandom().nextString(RandomStringKind.ALPHA_NUMERIC, Math.max(1, length)); }
    public static String nextString(int origin, int bound) { return getRandom().nextString(RandomStringKind.ALPHA_NUMERIC, origin, bound); }
    public static String nextString(RandomStringKind kind) { return getRandom().nextString(kind); }
    public static String nextString(RandomStringKind kind, int length) { return getRandom().nextString(kind, length); }
    public static String nextString(RandomStringKind kind, int origin, int bound) { return getRandom().nextString(kind, origin, bound); }
    public static String nextStringHex(int length) { return getRandom().nextString(RandomStringKind.HEX, length); }
    public static String nextStringHex(int origin, int bound) { return getRandom().nextString(RandomStringKind.HEX, origin, bound); }
    public static String nextStringNumeric(int length) { return getRandom().nextString(RandomStringKind.NUMERIC, length); }
    public static String nextStringNumeric(int origin, int bound) { return getRandom().nextString(RandomStringKind.NUMERIC, origin, bound); }
    public static String nextStringAlpha(int length) { return getRandom().nextString(RandomStringKind.ALPHA_NUMERIC, length); }
    public static String nextStringAlpha(int origin, int bound) { return getRandom().nextString(RandomStringKind.ALPHA_NUMERIC, origin, bound); }

    public static  <T> List<T> nextElements(T[] array) { return getRandom().nextElements(array); }
    public static  <T> List<T> nextElements(Collection<T> collection) { return getRandom().nextElements(collection); }

    public static <T> T nextElement(T[] array) { return getRandom().nextElement(array); }
    public static <T> T nextElement(T[] array, int startIndex) { return getRandom().nextElement(array, startIndex); }
    public static <T> T nextElement(T[] array, int origin, int bound) { return getRandom().nextElement(array, origin, bound); }
    public static <T> T nextElement(Collection<T> collection) { return getRandom().nextElement(collection); }
    public static <T> T nextElement(Collection<T> collection, int startIndex) { return getRandom().nextElement(collection, startIndex); }
    public static <T> T nextElement(Collection<T> collection, int origin, int bound) { return getRandom().nextElement(collection, origin, bound); }

    public static String randomStringIfRandomElse(String inputString) {
        if ("random".equalsIgnoreCase(inputString)) {
            return getRandom().nextString(RandomStringKind.ALPHA_NUMERIC, 6, 25);
        }
        return inputString;
    }

    public static String generateRandomHexString(int length) {
        return getRandom().nextString(RandomStringKind.HEX, length);
    }

    public static String generateRandomNumberString(int length) {
        return getRandom().nextString(RandomStringKind.NUMERIC, length);
    }

    public static String generateRandomAlphanumericString(int length) {
        return getRandom().nextString(RandomStringKind.ALPHA_NUMERIC, length);
    }

    public static String generateRandomAlphanumericString(int length, String letters) {
        // Since we can't modify the interface, we'll use the existing method
        return getRandom().nextString(RandomStringKind.ALPHA_NUMERIC, length);
    }

    public static String generateRandomLetterString(int length, String letters) {
        // Similarly, this might need refactoring later
        return getRandom().nextString(RandomStringKind.ALPHA_NUMERIC, length);
    }


    public static RandomDate nextDate() { return nextDate(RandomDate.ANDROID_RELEASE_YEAR, DateUtils.getCurrentYear()); }
    public static RandomDate nextDate(int minYear, int maxYear) {
        return new RandomDate(minYear, maxYear);
    }

    //Generate a OVERALL random Date , take in Constraints like year
    //    private static final String FORMAT = "%s-%s-%s %s:%s:%s";

}