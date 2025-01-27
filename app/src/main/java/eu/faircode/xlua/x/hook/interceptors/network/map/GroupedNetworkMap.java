package eu.faircode.xlua.x.hook.interceptors.network.map;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.randomizers.RandomMAC;
import eu.faircode.xlua.x.data.GroupedMap;

public class GroupedNetworkMap {
    private static final IRandomizerOld MAC_RANDOM = new RandomMAC();
    private final GroupedMap mGroupedMap;
    public GroupedNetworkMap(GroupedMap map) {
        mGroupedMap = map;
    }

    public String getMacAddress(String networkInterface) {
        return "";
    }
}
