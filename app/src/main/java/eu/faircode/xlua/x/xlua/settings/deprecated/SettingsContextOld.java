package eu.faircode.xlua.x.xlua.settings.deprecated;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;

public class SettingsContextOld {
    public static SettingsContextOld create(List<SettingExtendedOld> settings) { return new SettingsContextOld(settings); }
    private final List<SettingExtendedOld> mOriginalList;
    private List<SettingRandomContextOld> mGlobalList;
    private List<SettingRandomContextOld> mCurrentList;
    private List<SettingRandomContextOld> mParentControllers;
    private boolean mHasRandomizedParents = false;
    private boolean mIgnoreDisabledFlag = false;

    private final Map<String, String> mSavedSettings = new HashMap<>();

    public SettingsContextOld(List<SettingExtendedOld> settingsList) {
        this.mOriginalList = new ArrayList<>(settingsList);
        this.mGlobalList = toRandomContextList(settingsList);
        this.mCurrentList = new ArrayList<>(this.mGlobalList);
        this.mParentControllers = getParents(true);
    }

    public boolean hasValidOriginalList() { return ListUtil.isValid(mOriginalList); }
    public boolean isValid() { return ListUtil.isValid(mCurrentList); }
    public boolean hasIndex(int index) { return index > -1 && ListUtil.isValid(this.mCurrentList, index + 1); }
    public int getCount() { return mCurrentList != null ?  mCurrentList.size() : -1; }
    public boolean ignoreDisabled() { return this.mIgnoreDisabledFlag; }

    public SettingsContextOld putSavedSettingValue(String settingName, String value) {
        synchronized (mSavedSettings) {
            mSavedSettings.put(settingName, value);
            return this;
        }
    }

    public String getSavedSettingValue(String settingName, IRandomizer randomizer, boolean putIfNotExists) {
        synchronized (mSavedSettings) {
            if(!mSavedSettings.containsKey(settingName) && putIfNotExists) {
                String v = randomizer.generateString();
                mSavedSettings.put(settingName, v);
                return v;
            }

            return mSavedSettings.get(settingName);
        }
    }

    public String getSavedSettingValue(String settingName, String defaultValue, boolean putIfNotExists) {
        synchronized (mSavedSettings) {
            if(!mSavedSettings.containsKey(settingName) && putIfNotExists) {
                mSavedSettings.put(settingName, defaultValue);
                return defaultValue;
            }

            return mSavedSettings.get(settingName);
        }
    }

    public SettingsContextOld setIgnoreDisabledFlag(boolean ignoreIfDisabled) {
        this.mIgnoreDisabledFlag = ignoreIfDisabled;
        return this;
    }

    public SettingRandomContextOld atIndex(int index) {
        synchronized (mOriginalList) {
            return hasIndex(index )? this.mCurrentList.get(index) : null;
        }
    }

    public SettingRandomContextOld getParent(String parentName) {
        if(TextUtils.isEmpty(parentName)) return null;
        synchronized (mOriginalList) {
            if(!ListUtil.isValid(mParentControllers)) return null;
            for(SettingRandomContextOld s : mParentControllers) {
                if(s != null && s.equals(parentName))
                    return s;
            }

            return null;
        }
    }

    public SettingsContextOld ensureParentsRandomized() {
        if(mHasRandomizedParents) return this;
        mHasRandomizedParents = true;
        for(SettingRandomContextOld parent : mParentControllers) parent.randomize(this);
        return this;
    }

    public SettingRandomContextOld getFirst(String settingName) { return getFirst(settingName, false); }
    public SettingRandomContextOld getFirst(String settingName, boolean includeOriginalList) {
        if(TextUtils.isEmpty(settingName)) return null;
        synchronized (mOriginalList) {
            if(isValid()) {
                if(mCurrentList != null) {
                    for(SettingRandomContextOld s : mCurrentList) {
                        if(s != null && s.equals(settingName))
                            return s;
                    }
                }

                if(mParentControllers != null) {
                    for(SettingRandomContextOld p : mParentControllers) {
                        if(p != null && p.equals(settingName))
                            return p;
                    }
                }

                if(includeOriginalList && mGlobalList != null) {
                    for(SettingRandomContextOld s : mGlobalList) {
                        if(s != null && s.equals(settingName))
                            return s;
                    }
                }
            }
        }

        return null;
    }

    public SettingsContextOld removeNonEnabled() { return removeByState(false); }

    public SettingsContextOld removeEnabled() { return removeByState(true); }

    public SettingsContextOld removeByState(boolean isEnabled) {
        synchronized (mOriginalList) {
            if(isValid()) {
                for(int i = mCurrentList.size() - 1; i >= 0; i--) {
                    SettingRandomContextOld set = mCurrentList.get(i);
                    if(set.isEnabled() == isEnabled)
                        mCurrentList.remove(i);
                }
            }
        }

        return this;
    }

    public SettingsContextOld reset() {
        synchronized (mOriginalList) {
            this.mGlobalList = toRandomContextList(this.mOriginalList);
            this.mCurrentList = new ArrayList<>(this.mGlobalList);
            this.mHasRandomizedParents = false;
            this.mSavedSettings.clear();
            this.mParentControllers.clear();
        }

        this.mParentControllers = getParents(true);
        return this;
    }

    public Map<String, SettingRandomContextOld> filterOutToMap(String... settings) { return toRandomContextMap(filterOut(settings)); }
    public List<SettingRandomContextOld> filterOut(List<String> settings) { return ListUtil.isValid(settings) ? filterOut(settings.toArray(new String[0])) : new ArrayList<SettingRandomContextOld>(); }
    public List<SettingRandomContextOld> filterOut(String... settings) {
        synchronized (mOriginalList) {
            if(!ArrayUtils.isValid(settings) || !isValid()) return new ArrayList<>();
            List<SettingRandomContextOld> filtered = new ArrayList<>();
            for(String s : settings) {
                if(!TextUtils.isEmpty(s)) {
                    for(int i = mCurrentList.size() - 1; i >= 0; i--) {
                        SettingRandomContextOld set = mCurrentList.get(i);
                        if(set != null && set.equals(s)) {
                            filtered.add(set);
                            mCurrentList.remove(i);
                            break;
                        }
                    }
                }
            }

            return filtered;
        }
    }

    public Map<String, SettingRandomContextOld> getSettingsToMap(String... settings) { return toRandomContextMap(getSettings(settings)); }
    public List<SettingRandomContextOld> getSettings(List<String> settings) { return ListUtil.isValid(settings) ? getSettings(settings.toArray(new String[0])) : new ArrayList<SettingRandomContextOld>(); }
    public List<SettingRandomContextOld> getSettings(String... settings) {
        synchronized (mOriginalList) {
            if(!ArrayUtils.isValid(settings) || !isValid()) return new ArrayList<>();
            List<SettingRandomContextOld> filtered = new ArrayList<>();
            for(String s : settings) {
                if(!TextUtils.isEmpty(s)) {
                    for(int i = mCurrentList.size() - 1; i >= 0; i--) {
                        SettingRandomContextOld set = mCurrentList.get(i);
                        if(set != null && set.equals(s)) {
                            filtered.add(set);
                            break;
                        }
                    }
                }
            }

            return filtered;
        }
    }

    //Oh oh ye
    //Have like CFX (before/after) tags or attributes or something
    //to ensure they are invoked before or after

    public List<SettingRandomContextOld> getParents() { return getParents(true); }
    public List<SettingRandomContextOld> getParents(boolean removeParentFromCurrentList) {
        synchronized (mOriginalList) {
            if(!isValid()) return new ArrayList<>();
            List<SettingRandomContextOld> filtered = new ArrayList<>();
            for(int i = mGlobalList.size() - 1; i >= 0; i--) {
                SettingRandomContextOld set = mGlobalList.get(i);
                if(set != null && set.isParentControl()) {
                    filtered.add(set);
                    if(removeParentFromCurrentList && mCurrentList.contains(set))
                        mCurrentList.remove(i);
                    break;
                }
            }

            return filtered;
        }
    }

    public List<SettingRandomContextOld> getCopy() {
        synchronized (mOriginalList) {
            return !isValid() ? new ArrayList<SettingRandomContextOld>() : new ArrayList<>(this.mCurrentList);
        }
    }

    public static List<SettingExtendedOld> toSettingsList(Map<String, SettingExtendedOld> map) { return ListUtil.isValid(map) ? new ArrayList<>(map.values()) : new ArrayList<SettingExtendedOld>(); }
    public static Map<String, SettingExtendedOld> toSettingsMap(List<SettingExtendedOld> settings) {
        if(!ListUtil.isValid(settings)) return new HashMap<>();
        Map<String, SettingExtendedOld> map = new HashMap<>();
        for(SettingExtendedOld s : settings) {
            if(s != null) {
                map.put(s.getNameWithoutIndex(), s);
            }
        }

        return map;
    }

    public static Map<String, SettingRandomContextOld> toRandomContextMap(List<SettingRandomContextOld> settings) {
        if(!ListUtil.isValid(settings)) return new HashMap<>();
        Map<String, SettingRandomContextOld> map = new HashMap<>();
        for(SettingRandomContextOld s : settings) {
            if(s != null && s.isValid()) {
                map.put(s.getName(), s.getSetting().createRandomContext());
            }
        }

        return map;
    }

    public static Map<String, SettingRandomContextOld> toRandomContextMap(Map<String, SettingExtendedOld> settings) {
        if(!ListUtil.isValid(settings)) return new HashMap<>();
        Map<String, SettingRandomContextOld> map = new HashMap<>();
        for(Map.Entry<String, SettingExtendedOld> entry : settings.entrySet())
            map.put(entry.getKey(), entry.getValue().createRandomContext());

        return map;
    }

    public static List<SettingRandomContextOld> toRandomContextList(List<SettingExtendedOld> settings) {
        if(!ListUtil.isValid(settings)) return new ArrayList<>();
        List<SettingRandomContextOld> list = new ArrayList<>();
        for(SettingExtendedOld s : settings)
            list.add(s.createRandomContext());

        return list;
    }
}
