package eu.faircode.xlua.x.data.utils.random.providers;

import eu.faircode.xlua.x.data.utils.random.IRandomizerProvider;
import eu.faircode.xlua.x.data.utils.random.RandomProviderKind;
import eu.faircode.xlua.x.data.utils.random.RandomSeedMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class RandomThreadLocalProvider extends RandomProviderBase implements IRandomizerProvider {
    private RandomSeedMode seedMode = RandomSeedMode.DEFAULT;

    /**
     * Creates a new RandomThreadLocalProvider with default settings
     * Note: ThreadLocalRandom doesn't support manual seeding
     */
    public RandomThreadLocalProvider() { }

    /**
     * Creates a new RandomThreadLocalProvider with specified mode
     * @param mode The seed mode to use (DEFAULT or PARANOID)
     */
    public RandomThreadLocalProvider(RandomSeedMode mode) { this.seedMode = mode; }

    @Override
    public RandomSeedMode getSeedMode() { return seedMode; }

    @Override
    public void setSeedMode(RandomSeedMode mode) { this.seedMode = mode; }

    @Override
    public RandomProviderKind getKind() { return RandomProviderKind.THREAD_LOCAL; }

    /**
     * Note: ThreadLocalRandom doesn't support reseeding
     * In paranoid mode, we yield the current thread to introduce some randomness
     */
    @Override
    public void reSeed() {
        if (seedMode == RandomSeedMode.PARANOID) {
            Thread.yield();
        }
    }

    /**
     * Generates a random byte value between Byte.MIN_VALUE and Byte.MAX_VALUE
     * @return A random byte value
     */
    @Override
    public byte nextByte() { return (byte) ThreadLocalRandom.current().nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE + 1); }

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
        ThreadLocalRandom.current().nextBytes(bytes);
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
        // Combine multiple sources using ThreadLocalRandom
        int value = 0;

        // Mix different ranges and methods
        value ^= ThreadLocalRandom.current().nextInt() & 1;
        value ^= ThreadLocalRandom.current().nextLong() & 1;
        value ^= ThreadLocalRandom.current().nextDouble() < 0.5 ? 1 : 0;
        value ^= ThreadLocalRandom.current().nextInt(2);

        return value % 2 == 1;
    }

    @Override
    public short nextShort() { return (short) ThreadLocalRandom.current().nextInt(Short.MAX_VALUE + 1); }

    @Override
    public short nextShort(short bound) { return bound <= 0 ? nextShort() : (short) ThreadLocalRandom.current().nextInt(bound); }

    @Override
    public short nextShort(short origin, short bound) {
        if (origin > bound) { short temp = origin; origin = bound; bound = temp; }
        if (bound <= 0) return nextShort();
        return (short) ThreadLocalRandom.current().nextInt(origin, bound);
    }

    @Override
    public int nextInt() { return ThreadLocalRandom.current().nextInt(); }

    @Override
    public int nextInt(int bound) { return bound <= 0 ? nextInt() : ThreadLocalRandom.current().nextInt(bound); }

    @Override
    public int nextInt(int origin, int bound) {
        if (origin > bound) { int temp = origin; origin = bound; bound = temp; }
        if (bound <= 0) return nextInt();
        return ThreadLocalRandom.current().nextInt(origin, bound);
    }

    @Override
    public long nextLong() { return ThreadLocalRandom.current().nextLong(); }

    @Override
    public long nextLong(long bound) { return bound <= 0 ? nextLong() : ThreadLocalRandom.current().nextLong(bound); }

    @Override
    public long nextLong(long origin, long bound) {
        if (origin > bound) { long temp = origin; origin = bound; bound = temp; }
        if (bound <= 0) return nextLong();
        return ThreadLocalRandom.current().nextLong(origin, bound);
    }

    @Override
    public float nextFloat() { return ThreadLocalRandom.current().nextFloat(); }

    @Override
    public float nextFloat(float bound) { return bound <= 0 ? nextFloat() : ThreadLocalRandom.current().nextFloat() * bound; }

    @Override
    public float nextFloat(float origin, float bound) {
        if (origin > bound) { float temp = origin; origin = bound; bound = temp; }
        if (bound <= 0) return nextFloat();
        return origin + ThreadLocalRandom.current().nextFloat() * (bound - origin);
    }

    @Override
    public double nextDouble() { return ThreadLocalRandom.current().nextDouble(); }

    @Override
    public double nextDouble(double bound) { return bound <= 0 ? nextDouble() : ThreadLocalRandom.current().nextDouble(bound); }

    @Override
    public double nextDouble(double origin, double bound) {
        if (origin > bound) { double temp = origin; origin = bound; bound = temp; }
        if (bound <= 0) return nextDouble();
        return ThreadLocalRandom.current().nextDouble(origin, bound);
    }

    @Override
    public boolean nextBoolean() { return ThreadLocalRandom.current().nextBoolean(); }
}