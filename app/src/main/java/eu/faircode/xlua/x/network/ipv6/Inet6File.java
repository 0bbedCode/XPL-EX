package eu.faircode.xlua.x.network.ipv6;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.GroupedMap;
import eu.faircode.xlua.x.data.interfaces.IGroupedMapHolder;
import eu.faircode.xlua.x.network.NetUtils;

public class Inet6File implements IGroupedMapHolder {
    private final List<Inet6FileLine> mIpv6Lines = new ArrayList<>();
    private String mOriginalData;
    private String mCopyData;
    private GroupedMap mMap;

    public boolean hasLines() { return !mIpv6Lines.isEmpty(); }
    public String getOriginalData() { return Str.createCopy(mOriginalData); }
    public String getCopyData() { return mCopyData; }
    public void addLine(Inet6FileLine line) { if(line.isValid()) mIpv6Lines.add(line); }
    public List<Inet6FileLine> getLines() { return mIpv6Lines; }

    @Override
    public GroupedMap getGroupedMap() { return mMap; }

    @Override
    public void setGroupedMap(GroupedMap map) { mMap = map; }

    @Override
    public boolean hasMap() { return mMap != null; }

    public Inet6File(String contents) {
        if(contents != null && !contents.isEmpty()) {
            mOriginalData = Str.createCopy(contents);
            mCopyData = Str.createCopy(contents);
            for(String p : Str.split(contents, Str.NEW_LINE, true))
                addLine(Inet6FileLine.fromStringLine(p));
        }
    }

    private String getMappedKey(String interfaceName, String key, String valueIfNotExists) {
        if(!hasMap()) return valueIfNotExists;
        return mMap.getValueOrDefault(interfaceName, key, valueIfNotExists);
    }

    public String getMappedKey(String interfaceName, String key, IRandomizerOld randomizer) {
        if(!hasMap()) return randomizer.generateString();
        return mMap.getValueOrRandomize(interfaceName, key, randomizer);
    }

    public void replaceAddresses(String wlan0LocalLink, String wlan0Global, String dummy0LocalLink, GroupedMap map) {
        for(Inet6FileLine line : mIpv6Lines) {
            if(line.isPermanent()) {
                if(line.isWlan0()) {
                    if(wlan0LocalLink != null && line.isLinkLocal()) {
                        String val = getMappedKey(line.deviceName, line.addressNormalized, wlan0LocalLink);
                        mCopyData = mCopyData.replaceAll(line.address, NetUtils.convertIpv6ToIfInet6Format(val));
                    } if(wlan0Global != null && line.isGlobal()) {
                        String val = getMappedKey(line.deviceName, line.addressNormalized, wlan0Global);
                        mCopyData = mCopyData.replaceAll(line.address, NetUtils.convertIpv6ToIfInet6Format(val));
                    } else {
                        String val = getMappedKey(line.deviceName, line.addressNormalized, line.info);
                        mCopyData = mCopyData.replaceAll(line.address, NetUtils.convertIpv6ToIfInet6Format(val));
                    }
                } else if(line.isDummy0() && dummy0LocalLink != null && line.isLinkLocal()) {
                    String val = getMappedKey(line.deviceName, line.addressNormalized, dummy0LocalLink);
                    mCopyData = mCopyData.replaceAll(line.address, NetUtils.convertIpv6ToIfInet6Format(val));
                } else {
                    //check if the address is even valid ?
                    String val = getMappedKey(line.deviceName, line.addressNormalized, line.info);
                    mCopyData = mCopyData.replaceAll(line.address, NetUtils.convertIpv6ToIfInet6Format(val));
                }
            }
        }
    }
}
