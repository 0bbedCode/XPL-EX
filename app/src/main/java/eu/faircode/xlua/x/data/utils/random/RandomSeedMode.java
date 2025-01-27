package eu.faircode.xlua.x.data.utils.random;

public enum RandomSeedMode {
    DEFAULT(0),
    PARANOID(1);
    private final int value;
    RandomSeedMode(int value) { this.value = value; }
    public int getValue() { return value; }
}
