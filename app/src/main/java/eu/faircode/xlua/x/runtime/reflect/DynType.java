package eu.faircode.xlua.x.runtime.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class DynType<T> {
    private T t;

    public static <T> Class<?> getFirstNonNullElementClass(T[] array) {
        if (array == null || array.length == 0) {
            return null; // Array is null or empty
        }

        for (T element : array) {
            if (element != null) {
                return element.getClass(); // Return the Class of the first non-null element
            }
        }

        return null; // No non-null elements found
    }

    public Class<T> getClazzSuper() {
        try {
            Type superclass = getClass().getGenericSuperclass();
            if (superclass instanceof ParameterizedType)
                return (Class<T>) ((ParameterizedType) superclass).getActualTypeArguments()[0];
             else
                return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    public Class<T> getClazzField() {
        try {
            Field field = this.getClass().getDeclaredField("t");
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType)
                return (Class<T>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
            else if (genericType instanceof Class<?>)
                return (Class<T>) genericType;
        } catch (Exception ignore) { }
        return null;
    }

    public Class<T> getClazz() {
        Class<T> one = getClazzSuper();
        return one == null ? getClazzField() : one;
    }
}
