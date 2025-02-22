package eu.faircode.xlua.x.ui.core.view_registry;

import eu.faircode.xlua.x.xlua.database.sql.SQLSnake;

public interface IIdentifiableObject {
    default String getObjectId() { return null; }
    default void setId(String id) { }


    default SQLSnake createSnake() { return null; }

    default boolean consumeId(Object o) { return false; }

    default String getCategory() {
        return null;
    }
}
