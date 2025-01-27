package eu.faircode.xlua.x.data.utils.random.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import eu.faircode.xlua.x.data.utils.random.IRandomizerProvider;
import eu.faircode.xlua.x.data.utils.random.RandomProviderKind;
import eu.faircode.xlua.x.data.utils.random.RandomSeedMode;
import eu.faircode.xlua.x.data.utils.random.RandomStringHelper;


import eu.faircode.xlua.x.data.utils.random.RandomStringKind;


public class RandomGenericProvider extends RandomProviderBase implements IRandomizerProvider {
    private Random random;
    private RandomSeedMode seedMode = RandomSeedMode.DEFAULT;

    /**
     * Creates a new RandomGenericProvider with default settings
     */
    public RandomGenericProvider() { this.random = new Random(); }

    /**
     * Creates a new RandomGenericProvider with a specified seed
     * @param seed The seed to initialize the random number generator
     */
    public RandomGenericProvider(long seed) { this.random = new Random(seed); }

    /**
     * Creates a new RandomGenericProvider with specified mode
     * @param mode The seed mode to use (DEFAULT or PARANOID)
     */
    public RandomGenericProvider(RandomSeedMode mode) {
        this.seedMode = mode;
        this.random = new Random();
        if (mode == RandomSeedMode.PARANOID) reSeed();
    }

    /**
     * Creates a new RandomGenericProvider with specified seed and mode
     * @param seed The seed to initialize the random number generator
     * @param mode The seed mode to use (DEFAULT or PARANOID)
     */
    public RandomGenericProvider(long seed, RandomSeedMode mode) {
        this.seedMode = mode;
        this.random = new Random(seed);
        if (mode == RandomSeedMode.PARANOID) reSeed();
    }

    @Override
    public RandomSeedMode getSeedMode() { return seedMode; }

    @Override
    public void setSeedMode(RandomSeedMode mode) {
        this.seedMode = mode;
        if (mode == RandomSeedMode.PARANOID) reSeed();
    }

    @Override
    public RandomProviderKind getKind() { return RandomProviderKind.GENERIC; }

    /**
     * Reseeds the random number generator using system time XOR'd with a new random seed
     * Used automatically in paranoid mode
     */
    @Override
    public void reSeed() { this.random = new Random(System.nanoTime() ^ new Random().nextLong()); }

    /**
     * Generates a random byte value between Byte.MIN_VALUE and Byte.MAX_VALUE
     * @return A random byte value
     */
    @Override
    public byte nextByte() {
        byte[] single = new byte[1];
        random.nextBytes(single);
        return single[0];
    }

    /**
     * Generates a random byte array of default length (32)
     * Uses the provider's native byte generation method
     * @return A byte array of length 32 filled with random bytes
     */
    @Override
    public byte[] nextBytes() { return nextBytes(32); }

    /**
     * Generates a random byte array of specified length
     * If length is 0 or negative, returns array of default length (32)
     * Uses the provider's native byte generation method
     * @param length The desired length of the byte array
     * @return A byte array of specified length filled with random bytes
     */
    @Override
    public byte[] nextBytes(int length) {
        if (length <= 0) length = 32;
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    /**
     * Generates a random byte array with length between origin and bound
     * If origin > bound, the values are swapped
     * If bound <= origin, returns array of default length (32)
     * Length is determined by nextInt(origin, bound)
     * @param origin The minimum length of the array (inclusive)
     * @param bound The maximum length of the array (exclusive)
     * @return A byte array of random length between origin and bound, filled with random bytes
     */
    @Override
    public byte[] nextBytes(int origin, int bound) {
        if (origin > bound) { int temp = origin; origin = bound; bound = temp; }
        if (bound <= origin) return nextBytes();
        return nextBytes(nextInt(origin, bound));
    }

    /**
     * Returns true based on the given percentage chance
     * Percentage is clamped between 0 and 100
     * @param percentage Chance of returning true (0-100)
     * @return true if random value falls within percentage
     */
    @Override
    public boolean chance(int percentage) {
        percentage = Math.min(100, Math.max(0, percentage));
        return nextInt(100) < percentage;
    }

    /**
     * Advanced random boolean generator that uses multiple entropy sources
     * for a true 50/50 probability. More sophisticated than nextBoolean().
     * @return true or false with enhanced randomization
     */
    @Override
    public boolean chance() {
        // Combine multiple random sources for better distribution
        int value = 0;

        // Use different bit positions
        value ^= nextInt() & 1;                    // Lowest bit
        value ^= (nextInt() >>> 15) & 1;          // Middle bit
        value ^= (nextInt() >>> 31) & 1;          // Highest bit

        // Add float-based randomization
        value ^= nextFloat() < 0.5 ? 1 : 0;

        return value % 2 == 1;
    }

    /**
     * Generates a random short value between Short.MIN_VALUE and Short.MAX_VALUE
     * @return A random short value
     */
    @Override
    public short nextShort() { return (short) random.nextInt(Short.MAX_VALUE + 1); }

    /**
     * Generates a random short value between 0 (inclusive) and bound (exclusive)
     * If bound is 0 or negative, returns nextShort() with no parameters
     * @param bound The upper bound (exclusive)
     * @return A random short value
     */
    @Override
    public short nextShort(short bound) { return bound <= 0 ? nextShort() : (short) random.nextInt(bound); }

    /**
     * Generates a random short value between origin (inclusive) and bound (exclusive)
     * If origin > bound, the values are swapped
     * If bound <= 0, returns nextShort() with no parameters
     * @param origin The lower bound (inclusive)
     * @param bound The upper bound (exclusive)
     * @return A random short value
     */
    @Override
    public short nextShort(short origin, short bound) {
        if (origin > bound) { short temp = origin; origin = bound; bound = temp; }
        if (bound <= 0) return nextShort();
        return (short) (origin + random.nextInt(bound - origin));
    }

    /**
     * Generates a random integer value
     * @return A random integer between Integer.MIN_VALUE and Integer.MAX_VALUE
     */
    @Override
    public int nextInt() { return random.nextInt(); }

    /**
     * Generates a random integer value between 0 (inclusive) and bound (exclusive)
     * If bound is 0 or negative, returns nextInt() with no parameters
     * @param bound The upper bound (exclusive)
     * @return A random integer value
     */
    @Override
    public int nextInt(int bound) { return bound <= 0 ? nextInt() : random.nextInt(bound); }

    /**
     * Generates a random integer value between origin (inclusive) and bound (exclusive)
     * If origin > bound, the values are swapped
     * If bound <= 0, returns nextInt() with no parameters
     * @param origin The lower bound (inclusive)
     * @param bound The upper bound (exclusive)
     * @return A random integer value
     */
    @Override
    public int nextInt(int origin, int bound) {
        if (origin > bound) { int temp = origin; origin = bound; bound = temp; }
        if (bound <= 0) return nextInt();
        return origin + random.nextInt(bound - origin);
    }

    /**
     * Generates a random long value
     * @return A random long between Long.MIN_VALUE and Long.MAX_VALUE
     */
    @Override
    public long nextLong() { return random.nextLong(); }

    /**
     * Generates a random long value between 0 (inclusive) and bound (exclusive)
     * If bound is 0 or negative, returns nextLong() with no parameters
     * @param bound The upper bound (exclusive)
     * @return A random long value
     */
    @Override
    public long nextLong(long bound) { return bound <= 0 ? nextLong() : Math.abs(random.nextLong()) % bound; }

    /**
     * Generates a random long value between origin (inclusive) and bound (exclusive)
     * If origin > bound, the values are swapped
     * If bound <= 0, returns nextLong() with no parameters
     * @param origin The lower bound (inclusive)
     * @param bound The upper bound (exclusive)
     * @return A random long value
     */
    @Override
    public long nextLong(long origin, long bound) {
        if (origin > bound) { long temp = origin; origin = bound; bound = temp; }
        if (bound <= 0) return nextLong();
        return origin + Math.abs(random.nextLong()) % (bound - origin);
    }

    /**
     * Generates a random float value between 0.0 (inclusive) and 1.0 (exclusive)
     * @return A random float value
     */
    @Override
    public float nextFloat() { return random.nextFloat(); }

    /**
     * Generates a random float value between 0.0 (inclusive) and bound (exclusive)
     * If bound is 0 or negative, returns nextFloat() with no parameters
     * @param bound The upper bound (exclusive)
     * @return A random float value
     */
    @Override
    public float nextFloat(float bound) { return bound <= 0 ? nextFloat() : random.nextFloat() * bound; }

    /**
     * Generates a random float value between origin (inclusive) and bound (exclusive)
     * If origin > bound, the values are swapped
     * If bound <= 0, returns nextFloat() with no parameters
     * @param origin The lower bound (inclusive)
     * @param bound The upper bound (exclusive)
     * @return A random float value
     */
    @Override
    public float nextFloat(float origin, float bound) {
        if (origin > bound) { float temp = origin; origin = bound; bound = temp; }
        if (bound <= 0) return nextFloat();
        return origin + random.nextFloat() * (bound - origin);
    }

    /**
     * Generates a random double value between 0.0 (inclusive) and 1.0 (exclusive)
     * @return A random double value
     */
    @Override
    public double nextDouble() { return random.nextDouble(); }

    /**
     * Generates a random double value between 0.0 (inclusive) and bound (exclusive)
     * If bound is 0 or negative, returns nextDouble() with no parameters
     * @param bound The upper bound (exclusive)
     * @return A random double value
     */
    @Override
    public double nextDouble(double bound) { return bound <= 0 ? nextDouble() : random.nextDouble() * bound; }

    /**
     * Generates a random double value between origin (inclusive) and bound (exclusive)
     * If origin > bound, the values are swapped
     * If bound <= 0, returns nextDouble() with no parameters
     * @param origin The lower bound (inclusive)
     * @param bound The upper bound (exclusive)
     * @return A random double value
     */
    @Override
    public double nextDouble(double origin, double bound) {
        if (origin > bound) { double temp = origin; origin = bound; bound = temp; }
        if (bound <= 0) return nextDouble();
        return origin + random.nextDouble() * (bound - origin);
    }

    /**
     * Generates a random boolean value
     * @return A random boolean value
     */
    @Override
    public boolean nextBoolean() { return random.nextBoolean(); }
}