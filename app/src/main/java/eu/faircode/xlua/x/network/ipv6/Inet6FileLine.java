package eu.faircode.xlua.x.network.ipv6;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.network.NetUtils;

public class Inet6FileLine {
    public static Inet6FileLine fromStringLine(String line) { return new Inet6FileLine(line); }

    public String address;
    public String addressNormalized;
    public int deviceNumber;
    public int prefixLength;
    public int scope;
    public int flags;
    public String deviceName;
    public Inet6AddressInfo info;

    public boolean isLinkLocal() { return info.isLinkLocal; }
    public boolean isGlobal() { return info.isGlobal; }
    public boolean isPermanent() { return info.isPermanent(); }

    private boolean mIsValid = true;

    public boolean isValid() { return mIsValid; }
    public boolean isWlan0() { return "wlan0".equalsIgnoreCase(deviceName); }
    public boolean isDummy0() { return "dummy0".equalsIgnoreCase(deviceName); }

    public Inet6FileLine() { }
    public Inet6FileLine(String line) {
        if(!Str.isValidNotWhitespaces(line) || line.length() < 5) {
            mIsValid = false;
            return;
        }

        String lineCleaned = line.trim();
        List<String> parts = new ArrayList<>();
        char[] chars = lineCleaned.toCharArray();
        int finalIndex = chars.length - 1;
        StringBuilder chunk = new StringBuilder();

        for(int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if(c == ' ' || c == '\n' || c == '\b' || c == '\t') {
                if(chunk.length() > 0) {
                    parts.add(chunk.toString());
                    chunk = new StringBuilder();
                }
            } else {
                chunk.append(c);
                if(i == finalIndex) {
                    parts.add(chunk.toString());
                    chunk = new StringBuilder();
                }
            }
        }

        int bottomIndex = 0;
        for(int i = 0; i < parts.size(); i++) {
            String p = parts.get(i);
            if(p == null || p.isEmpty()) continue;
            p = p.trim();
            if(p.isEmpty()) continue;
            switch (bottomIndex) {
                case 0:
                    address = p;
                    bottomIndex++;
                    break;
                case 1:
                    deviceNumber = Integer.parseInt(p, 16);
                    bottomIndex++;
                    break;
                case 2:
                    prefixLength = Integer.parseInt(p, 16);
                    bottomIndex++;
                    break;
                case 3:
                    scope = Integer.parseInt(p, 16);
                    bottomIndex++;
                    break;
                case 4:
                    flags = Integer.parseInt(p, 16);
                    bottomIndex++;
                    break;
                case 5:
                    deviceName = p;
                    break;
                default:
                    break;
            }
        }

        addressNormalized = NetUtils.convertIfInet6ToIpv6Format(address);
        info = new Inet6AddressInfo(this);
    }

    @NonNull
    @Override
    public String toString() {
        return "IPV6_LINE{" +
                "address='" + addressNormalized + '\'' +
                ", deviceNumber=" + deviceNumber +
                ", prefixLength=" + prefixLength +
                ", scope=" + scope +
                ", flags=" + flags +
                ", deviceName='" + deviceName + '\'' +
                ", info=" + info +
                '}';
    }
}
