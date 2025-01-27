package eu.faircode.xlua.x.hook.filter;

import java.util.List;
import java.util.Map;

import eu.faircode.xlua.api.hook.XLuaHook;

public interface IFilterContainer {
    String getGroupName();
    boolean hasSwallowedAsRule(XLuaHook hook);
    boolean hasSwallowedAsDefinition(XLuaHook hook);
    void initializeDefinitions(List<XLuaHook> hooks, Map<String, String> settings);

    List<XLuaHook> getRules();
    List<XLuaHook> getFilterBases();
}
