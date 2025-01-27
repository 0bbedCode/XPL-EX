package eu.faircode.xlua.x.data.utils.random;

import java.util.Collection;
import java.util.List;

public interface IRandomizerProvider {
    RandomSeedMode getSeedMode();
    void setSeedMode(RandomSeedMode mode);

    RandomProviderKind getKind();
    void reSeed();

    short nextShort();
    short nextShort(short bound);
    short nextShort(short origin, short bound);

    int nextInt();
    int nextInt(int bound);
    int nextInt(int origin, int bound);

    long nextLong();
    long nextLong(long bound);
    long nextLong(long origin, long bound);

    float nextFloat();
    float nextFloat(float bound);
    float nextFloat(float origin, float bound);

    double nextDouble();
    double nextDouble(double bound);
    double nextDouble(double origin, double bound);

    boolean nextBoolean();
    boolean chance();
    boolean chance(int percentage);

    byte nextByte();
    byte[] nextBytes();
    byte[] nextBytes(int length);
    byte[] nextBytes(int origin, int bound);


    String nextString();
    String nextString(int length); // Changed parameter name to "length"
    String nextString(int origin, int bound); // Changed parameters to "origin" and "bound"
    String nextString(RandomStringKind kind);
    String nextString(RandomStringKind kind, int length); // Changed parameter name to "length"
    String nextString(RandomStringKind kind, int origin, int bound); // Changed parameters to "origin" and "bound"

    // Added new string generation methods
    String nextStringHex();
    String nextStringHex(int length);
    String nextStringHex(int origin, int bound);

    String nextStringNumeric();
    String nextStringNumeric(int length);
    String nextStringNumeric(int origin, int bound);

    String nextStringAlpha();
    String nextStringAlpha(int length);
    String nextStringAlpha(int origin, int bound);


    <T> T nextElement(T[] array);
    <T> T nextElement(T[] array, int startIndex);
    <T> T nextElement(T[] array, int origin, int bound);
    <T> T nextElement(Collection<T> collection);
    <T> T nextElement(Collection<T> collection, int startIndex);
    <T> T nextElement(Collection<T> collection, int origin, int bound);


    <T> List<T> nextElements(T[] array);
    <T> List<T> nextElements(Collection<T> collection);

    //Add Clean up functions / whatever if needed ??
}
