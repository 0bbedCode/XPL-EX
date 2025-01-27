package eu.faircode.xlua.x.hook.filter;

import java.util.HashMap;

import eu.faircode.xlua.api.hook.XLuaHook;

//FilterFactory ?
public class FilterHook extends XLuaHook {
    public static final String XP_PRIVACY_COLLECTION = "PrivacyEx";

    public static FilterHook create(String filterKind) { return new FilterHook(filterKind); }
    public static FilterHook create(String filterKind, String collection) { return new FilterHook(filterKind, collection); }

    public String filterKind;
    public String collection;
    public HashMap<String, String> methods = new HashMap<>();

    public FilterHook(String filterKind) { this(filterKind, XP_PRIVACY_COLLECTION); }
    public FilterHook(String filterKind, String collection) {
        this.filterKind = filterKind;
        this.collection = collection;
    }

    public FilterHook bindMethod(Class<?> clazz, String methodName) { return bindMethod(clazz.getName(), methodName); }
    public FilterHook bindMethod(String className, String methodName) {
        methods.put(className, methodName);
        return this;
    }

    public boolean isFilterOrInterceptHook(XLuaHook hook) {
        return false;
    }
}
