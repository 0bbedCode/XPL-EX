package eu.faircode.xlua.x.data;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.api.hook.XLuaHook;

public class FilterHooksHolder {
    public static FilterHooksHolder create() { return new FilterHooksHolder(); }

    private final Object mLock = new Object();
    private final List<XLuaHook> baseHooks = new ArrayList<>();
    private final List<XLuaHook> ruleHooks = new ArrayList<>();

    public boolean hasRules() { synchronized (mLock) { return !ruleHooks.isEmpty(); } }
    public boolean hasBases() { synchronized (mLock) { return !baseHooks.isEmpty(); } }

    public List<XLuaHook> getBaseHooks() { synchronized (mLock) { return new ArrayList<>(baseHooks); } }
    public List<XLuaHook> getRuleHooks() { synchronized (mLock) { return new ArrayList<>(ruleHooks); } }

    public int baseCount() { synchronized (mLock) { return baseHooks.size(); } }
    public int ruleCount() { synchronized (mLock) { return ruleHooks.size(); } }

    public void addBase(XLuaHook hook) {
        synchronized (mLock) {
            if(!baseHooks.contains(hook)) {
                hook.setIsEnabled(true);
                baseHooks.add(hook);
            }
        }
    }

    public void addRule(XLuaHook hook) {
        synchronized (mLock) {
            if(!ruleHooks.contains(hook))
                ruleHooks.add(hook);
        }
    }

    public void removeRule(XLuaHook hook) {
        synchronized (mLock) {
            if(!ruleHooks.contains(hook))
                ruleHooks.remove(hook);
        }
    }
}
