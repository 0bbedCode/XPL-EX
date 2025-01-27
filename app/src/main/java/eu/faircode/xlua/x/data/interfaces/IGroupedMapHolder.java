package eu.faircode.xlua.x.data.interfaces;

import eu.faircode.xlua.x.data.GroupedMap;

public interface IGroupedMapHolder {
    GroupedMap getGroupedMap();
    void setGroupedMap(GroupedMap map);
    boolean hasMap();
}

