package eu.faircode.xlua.x.network.ipv6;

import android.text.TextUtils;

import java.net.Inet6Address;
import java.util.Collections;
import java.util.List;


import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.network.NetRandom;
import eu.faircode.xlua.x.network.NetUtils;

public class Inet6AddressInfo implements IRandomizerOld {
    public boolean isLinkLocal;
    public boolean isGlobal;
    public boolean isTemporary;
    public boolean isDeprecated;
    public boolean isUniqueLocal;
    public String originalAddress;

    public boolean isPermanent() { return !isTemporary; }

    public boolean isLikelyToBeUsed() {
        // Prioritize global, non-temporary addresses
        return isGlobal && !isTemporary && !isDeprecated;
    }

    public Inet6AddressInfo() {  }
    public Inet6AddressInfo(boolean isLinkLocal, boolean isGlobal, boolean isTemporary, boolean isDeprecated, boolean isUniqueLocal) {
        this.isLinkLocal = isLinkLocal;
        this.isGlobal = isGlobal;
        this.isTemporary = isTemporary;
        this.isDeprecated = isDeprecated;
        this.isUniqueLocal = isUniqueLocal;

    }
    public Inet6AddressInfo(Inet6FileLine line) {
        isLinkLocal = line.scope == 0x20;
        isGlobal = line.scope == 0x00;
        //Check these fucking flags
        isTemporary = (line.flags & 0x01) != 0;
        //We should not use the deprecated flag seem unstable
        isDeprecated = (line.flags & 0x20) != 0;  // Assuming 0x20 flag for deprecated
        isUniqueLocal = line.address.toLowerCase().startsWith("fd");
        originalAddress = NetUtils.convertIfInet6ToIpv6Format(line.address);
    }

    public Inet6AddressInfo(Inet6Address address) {
        isLinkLocal = address.isLinkLocalAddress();
        isGlobal = !address.isSiteLocalAddress() && !address.isLinkLocalAddress();
        isUniqueLocal = address.getHostAddress().startsWith("fd");
        // Heuristic for temporary address (not always accurate)
        isTemporary = !address.getHostAddress().contains("ff:fe");
        originalAddress = address.getHostAddress();
    }

    @Override
    public boolean isSetting(String settingName) { return false; }

    @Override
    public String getSettingName() { return ""; }

    @Override
    public String getName() { return ""; }

    @Override
    public String getID() { return ""; }

    @Override
    public String generateString() { return NetUtils.convertIfInet6ToIpv6Format(generateRandomAddress()); }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return Collections.emptyList(); }

    public String generateRandomAddress() {
        if(!TextUtils.isEmpty(originalAddress)) return NetRandom.randomizeIPv6(originalAddress);
        StringBuilder addressBuilder = new StringBuilder();
        if (isLinkLocal) {
            addressBuilder.append("fe80");
            for (int i = 0; i < 6; i++) {
                addressBuilder.append(String.format("%04x", RandomGenerator.nextInt(65536)));
            }
        } else if (isUniqueLocal) {
            addressBuilder.append("fd");
            addressBuilder.append(String.format("%02x", RandomGenerator.nextInt(256)));
            for (int i = 0; i < 7; i++) {
                addressBuilder.append(String.format("%04x", RandomGenerator.nextInt(65536)));
            }
        } else {
            // Global or default
            addressBuilder.append("2");
            addressBuilder.append(String.format("%03x", RandomGenerator.nextInt(4096)));
            for (int i = 0; i < 7; i++) {
                addressBuilder.append(String.format("%04x", RandomGenerator.nextInt(65536)));
            }
        }

        if (!isTemporary && !isLinkLocal) {
            // For non-temporary addresses, insert ff:fe in the middle
            int insertIndex = 20;
            addressBuilder.insert(insertIndex, "fffe");
        }

        // Ensure the address is exactly 32 characters long (128 bits)
        while (addressBuilder.length() < 32) {
            addressBuilder.append("0");
        }
        if (addressBuilder.length() > 32) {
            addressBuilder.setLength(32);
        }

        return addressBuilder.toString();
    }

    @Override
    public String toString() {
        return "IPV6_AddressInfo{" +
                "isLinkLocal=" + isLinkLocal +
                ", isGlobal=" + isGlobal +
                ", isTemporary=" + isTemporary +
                ", isDeprecated=" + isDeprecated +
                ", isUniqueLocal=" + isUniqueLocal +
                '}';
    }
}