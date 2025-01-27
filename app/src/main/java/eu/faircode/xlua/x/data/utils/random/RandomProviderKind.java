package eu.faircode.xlua.x.data.utils.random;


public enum RandomProviderKind {
    GENERIC(0),
    THREAD_LOCAL(1),
    SECURE(2);
    private final int value;
    RandomProviderKind(int value) { this.value = value; }
    public int getValue() { return value; }
}
