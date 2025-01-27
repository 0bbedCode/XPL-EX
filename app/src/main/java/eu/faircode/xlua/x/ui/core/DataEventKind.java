package eu.faircode.xlua.x.ui.core;

public enum DataEventKind {
    ON_RESUME(1),
    ON_DISPOSE(2),
    ON_LOADER(3),
    ON_REFRESH(7);

    private final int value;
    DataEventKind(int value) { this.value = value; }
    public int getValue() { return value; }
}
