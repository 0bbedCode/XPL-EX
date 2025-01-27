package eu.faircode.xlua.x.data.utils.random.providers;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.faircode.xlua.x.data.utils.random.IRandomizerProvider;
import eu.faircode.xlua.x.data.utils.random.RandomProviderKind;
import eu.faircode.xlua.x.data.utils.random.RandomSeedMode;


import java.security.SecureRandom;

import java.util.concurrent.TimeUnit;


public class RandomSecureProvider extends RandomProviderBase implements IRandomizerProvider {
    private SecureRandom secureRandom;
    private RandomSeedMode seedMode = RandomSeedMode.DEFAULT;
    private long lastSeedTime;
    private long operationCount;

    private static final String[] SECURE_ALGORITHMS = {"SHA1PRNG", "NativePRNG"};
    private static final long RESEED_OPERATIONS = 10000;

    /**
     * Creates a minimal RandomSecureProvider with default settings.
     * No timing or reseeding mechanisms are initialized for optimal performance.
     */
    public RandomSecureProvider() { this.secureRandom = new SecureRandom(); }

    /**
     * Creates a RandomSecureProvider with specified mode.
     * Only initializes timing and reseeding mechanisms if paranoid mode is enabled.
     * @param mode The seed mode to use (DEFAULT or PARANOID)
     */
    public RandomSecureProvider(RandomSeedMode mode) {
        this.seedMode = mode;
        if (mode == RandomSeedMode.PARANOID) {
            this.secureRandom = new SecureRandom();
            this.lastSeedTime = System.nanoTime();
            reSeed();
        } else {
            this.secureRandom = new SecureRandom();
        }
    }

    /**
     * Gets the current seed mode.
     * @return The current RandomSeedMode
     */
    @Override
    public RandomSeedMode getSeedMode() { return seedMode; }

    /**
     * Sets the seed mode. If changing to paranoid mode, initializes timing
     * and performs initial reseeding.
     * @param mode The new RandomSeedMode to use
     */
    @Override
    public void setSeedMode(RandomSeedMode mode) {
        if (this.seedMode != mode) {
            this.seedMode = mode;
            if (mode == RandomSeedMode.PARANOID) {
                this.lastSeedTime = System.nanoTime();
                reSeed();
            }
        }
    }

    /**
     * Returns the kind of random provider.
     * @return RandomProviderKind.SECURE
     */
    @Override
    public RandomProviderKind getKind() { return RandomProviderKind.SECURE; }

    /**
     * Enhanced reseeding mechanism that creates a new SecureRandom instance
     * with enhanced entropy gathering from multiple sources.
     */
    @Override
    public void reSeed() {
        List<byte[]> entropySources = new ArrayList<>();

        // Gather entropy from different algorithms
        for (String algorithm : SECURE_ALGORITHMS) {
            try {
                SecureRandom tempRandom = SecureRandom.getInstance(algorithm);
                byte[] entropy = tempRandom.generateSeed(32);
                entropySources.add(entropy);
            } catch (Exception ignored) {}
        }

        // Add system-specific entropy
        ByteBuffer systemEntropy = ByteBuffer.allocate(32);
        systemEntropy.putLong(System.nanoTime());
        systemEntropy.putLong(System.currentTimeMillis());
        systemEntropy.putLong(Runtime.getRuntime().freeMemory());
        systemEntropy.putLong(Thread.currentThread().getId());
        entropySources.add(systemEntropy.array());

        // Create new SecureRandom instance and mix entropy
        SecureRandom newRandom = new SecureRandom();
        for (byte[] entropy : entropySources) {
            newRandom.nextBytes(entropy);
            for (int i = 0; i < 3; i++) {
                newRandom.nextBytes(new byte[16]);
            }
        }

        this.secureRandom = newRandom;
        this.lastSeedTime = System.nanoTime();
    }

    /**
     * Gets the current SecureRandom instance, handling reseeding if in paranoid mode.
     * Only tracks operations and checks reseeding in paranoid mode.
     */
    private SecureRandom getSecureRandom() {
        if (seedMode == RandomSeedMode.PARANOID) {
            operationCount++;
            if (operationCount % RESEED_OPERATIONS == 0 ||
                    System.nanoTime() - lastSeedTime > TimeUnit.MINUTES.toNanos(5)) {
                reSeed();
            }
        }
        return secureRandom;
    }


    /**
     * Generates a random byte value between Byte.MIN_VALUE and Byte.MAX_VALUE
     * @return A random byte value
     */
    @Override
    public byte nextByte() {
        byte[] single = new byte[1];
        getSecureRandom().nextBytes(single);
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
        getSecureRandom().nextBytes(bytes);
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
        // Use multiple bytes for better entropy
        byte[] bytes = new byte[4];
        getSecureRandom().nextBytes(bytes);

        // Combine multiple random sources
        int value = 0;

        // Use different bytes for diversity
        value ^= bytes[0] & 1;                     // First byte lowest bit
        value ^= (bytes[1] >>> 4) & 1;            // Second byte middle bit
        value ^= (bytes[2] >>> 7) & 1;            // Third byte highest bit
        value ^= bytes[3] < 0 ? 1 : 0;            // Fourth byte sign

        // Add double-based randomization
        value ^= nextDouble() < 0.5 ? 1 : 0;

        return value % 2 == 1;
    }

    // Random generation methods
    @Override
    public short nextShort() { return (short) getSecureRandom().nextInt(Short.MAX_VALUE + 1); }

    @Override
    public short nextShort(short bound) { return bound <= 0 ? nextShort() : (short) getSecureRandom().nextInt(bound); }

    @Override
    public short nextShort(short origin, short bound) {
        if (origin > bound) { short temp = origin; origin = bound; bound = temp; }
        if (bound <= 0) return nextShort();
        return (short) (origin + getSecureRandom().nextInt(bound - origin));
    }

    @Override
    public int nextInt() { return getSecureRandom().nextInt(); }

    @Override
    public int nextInt(int bound) { return bound <= 0 ? nextInt() : getSecureRandom().nextInt(bound); }

    @Override
    public int nextInt(int origin, int bound) {
        if (origin > bound) { int temp = origin; origin = bound; bound = temp; }
        if (bound <= 0) return nextInt();
        return origin + getSecureRandom().nextInt(bound - origin);
    }

    @Override
    public long nextLong() { return getSecureRandom().nextLong(); }

    @Override
    public long nextLong(long bound) { return bound <= 0 ? nextLong() : Math.abs(getSecureRandom().nextLong()) % bound; }

    @Override
    public long nextLong(long origin, long bound) {
        if (origin > bound) { long temp = origin; origin = bound; bound = temp; }
        if (bound <= 0) return nextLong();
        return origin + Math.abs(getSecureRandom().nextLong()) % (bound - origin);
    }

    @Override
    public float nextFloat() { return getSecureRandom().nextFloat(); }

    @Override
    public float nextFloat(float bound) { return bound <= 0 ? nextFloat() : getSecureRandom().nextFloat() * bound; }

    @Override
    public float nextFloat(float origin, float bound) {
        if (origin > bound) { float temp = origin; origin = bound; bound = temp; }
        if (bound <= 0) return nextFloat();
        return origin + getSecureRandom().nextFloat() * (bound - origin);
    }

    @Override
    public double nextDouble() { return getSecureRandom().nextDouble(); }

    @Override
    public double nextDouble(double bound) { return bound <= 0 ? nextDouble() : getSecureRandom().nextDouble() * bound; }

    @Override
    public double nextDouble(double origin, double bound) {
        if (origin > bound) { double temp = origin; origin = bound; bound = temp; }
        if (bound <= 0) return nextDouble();
        return origin + getSecureRandom().nextDouble() * (bound - origin);
    }

    @Override
    public boolean nextBoolean() { return getSecureRandom().nextBoolean(); }
}