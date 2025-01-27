package eu.faircode.xlua.x.data.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *
 * @param <T> Type for Generic to Get
 */
public class DynamicTUtils<T> {
    public Class<T> getClazz() {
        Type superclass = getClass();
        Class<T> aClass = (Class<T>) ((ParameterizedType) superclass).getActualTypeArguments()[0];
        return aClass;
    }

    public Class<?> getClazzFree() {
        Type superclass = getClass();
        Class<?> aClass = (Class<?>) ((ParameterizedType) superclass).getActualTypeArguments()[0];
        return aClass;
    }
}
