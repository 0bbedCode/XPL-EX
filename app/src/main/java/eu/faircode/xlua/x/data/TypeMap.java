package eu.faircode.xlua.x.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.faircode.xlua.x.Str;

public class TypeMap {
    public static TypeMap create() { return new TypeMap(); }

    private final HashMap<String, List<String>> mTypeMap = new HashMap<>();

    public boolean hasDefinition(String className, String methodName) {
        synchronized (mTypeMap) {
            List<String> methods = mTypeMap.get(className);
            if(methods == null)
                return false;

            String t = methodName.trim();
            for(String m : methods)
                if(m.equalsIgnoreCase(t))
                    return true;

            return false;
        }
    }

    public TypeMap add(Class<?> clazz, String... methods) {
        if(clazz == null) return this;
        return add(clazz.getName(), methods);
    }

    public TypeMap add(String className, String... methods) {
        synchronized (mTypeMap) {
            List<String> list = mTypeMap.get(className);
            if(list == null) {
                list = new ArrayList<>();
                mTypeMap.put(className, list);
            }

            for(String m : methods) {
                if(!Str.isValidNotWhitespaces(m)) continue;
                String cleaned = Str.trim(m, " ", true, true);
                if(!list.contains(cleaned)) {
                    list.add(cleaned);
                }
            }

            return this;
        }
    }
}
