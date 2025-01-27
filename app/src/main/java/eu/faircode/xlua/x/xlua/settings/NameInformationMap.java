package eu.faircode.xlua.x.xlua.settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.settings.interfaces.INameInformation;
import eu.faircode.xlua.x.xlua.settings.interfaces.NameInformationTypeBase;

public class NameInformationMap<T extends NameInformationTypeBase> {
    //Enabled
    //Settings
    //Randomized
    //Consume some base ?
    //Like those

    private final HashMap<String, T> internalMap = new HashMap<>();
    private final HashMap<String, T> internalPassing = new HashMap<>();
    private final HashMap<String, T> internalFailing = new HashMap<>();

    public int size() { return internalMap.size(); }
    public boolean isEmpty() { return internalMap.isEmpty(); }

    private boolean isValidInformation(INameInformation nameInformationObject) { return nameInformationObject != null && nameInformationObject.isValid(); }

    public boolean hasName(String name) { return name != null && internalMap.containsKey(name); }
    public boolean hasName(T nameInformationObject) { return isValidInformation(nameInformationObject) && internalMap.containsKey(nameInformationObject.getName());  }
    public boolean hasName(INameInformation nameInformation) { return isValidInformation(nameInformation) && internalMap.containsKey(nameInformation.getName()); }

    public List<T> getNameInformationList() { return ListUtil.copyToArrayList(internalMap.values()); }

    public T get(String name) { return name != null ? internalMap.get(name) : null; }
    public T get(T nameInformationObject) { return isValidInformation(nameInformationObject) ? internalMap.get(nameInformationObject.getName()) : null; }

    //public T get(int index) { return internalMap. }

    public T remove(T nameInformationObject) { return isValidInformation(nameInformationObject) ? internalMap.get(nameInformationObject.getName()) : null; }

    public T put(T nameInformationObject) {
        if(!isValidInformation(nameInformationObject)) return nameInformationObject;
        String name = nameInformationObject.getName();
        internalMap.put(name, nameInformationObject);
        if(nameInformationObject.hasPassed()) internalPassing.put(name, nameInformationObject);
        else internalFailing.put(name, nameInformationObject);
        return nameInformationObject;
    }

    public void putAll(NameInformationMap<T> map) { internalMap.putAll(map.internalMap); }

    public void clear() { internalMap.clear(); }
    public void clearOutNotPassing() {
        Map<String, T> good = new HashMap<>();
        for(Map.Entry<String, T> entry : internalMap.entrySet()) {
            String name = entry.getKey();
            T value = entry.getValue();
            if(name != null && value.hasPassed())
                good.put(name, value);
        }

        internalMap.clear();
        internalMap.putAll(good);
    }

    public void clearOutInvalid() {
        Map<String, T> good = new HashMap<>();
        for(Map.Entry<String, T> entry : internalMap.entrySet()) {
            String name = entry.getKey();
            T value = entry.getValue();
            if(name != null && value != null && value.isValid()) {
                good.put(name, value);
            }
        }

        internalMap.clear();
        internalMap.putAll(good);
    }
}
