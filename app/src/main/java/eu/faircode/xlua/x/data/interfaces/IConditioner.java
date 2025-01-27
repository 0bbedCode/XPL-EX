package eu.faircode.xlua.x.data.interfaces;

public interface IConditioner<T> {
    boolean meetsCondition(T value);
    T recondition(T value);
}
