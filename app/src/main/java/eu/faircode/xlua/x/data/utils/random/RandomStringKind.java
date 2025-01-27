package eu.faircode.xlua.x.data.utils.random;

public enum RandomStringKind {
    NUMERIC(0),
    ALPHA_NUMERIC(1),
    ALPHA_NUMERIC_UPPERCASE(2),
    ALPHA_NUMERIC_LOWERCASE(3),
    HEX(4);
    private final int value;
    RandomStringKind(int value) { this.value = value; }
    public int getValue() { return value; }
}
