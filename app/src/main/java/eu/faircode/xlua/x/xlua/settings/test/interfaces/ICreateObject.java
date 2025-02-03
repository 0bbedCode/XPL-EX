package eu.faircode.xlua.x.xlua.settings.test.interfaces;

public interface ICreateObject {
    default void setId(String id) { }
    default String getId() { return null; }
}
