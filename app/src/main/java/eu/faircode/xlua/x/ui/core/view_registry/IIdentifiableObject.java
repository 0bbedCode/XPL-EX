package eu.faircode.xlua.x.ui.core.view_registry;

public interface IIdentifiableObject {
    default String getSharedId() { return null; }
    default void setId(String id) { }


    default boolean consumeId(Object o) { return false; }

    default String getCategory() {
        return null;
    }
}
