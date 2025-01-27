package eu.faircode.xlua.random.randomizers;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.x.network.NetUtils;
import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.x.network.ipv6.Inet6AddressInfo;


public class RandomNetAddress6 implements IRandomizerOld {

    private String settingName;
    private Inet6AddressInfo addressInfo;
    private String prefix;

    //[Link Local][Permanent Address] for dummy0
    /*
            260102491a8479f0e11daa40f9218b51 1e 40 00 01    wlan0 [Global][Temporary Address]
            fe800000000000001ce369fffe881083 1e 40 20 80    wlan0 [Link Local][Permanent Address]
            260102491a8479f01ce369fffe881083 1e 40 00 00    wlan0 [Global][Permanent Address]       [Most likely this one]
     */

    public static final String RANDOM_PREFIX = "network.host.address.6";

    public static final List<RandomNetAddress6> INSTANCES = Arrays.asList(
            new RandomNetAddress6("network.host.address.6.wlan0.local.link.perm"),
            new RandomNetAddress6("network.host.address.6.wlan0.global.perm"),
            new RandomNetAddress6("network.host.address.6.dummy0.local.link.perm"));

    public RandomNetAddress6(String settingName) {
        if(settingName.startsWith(RANDOM_PREFIX)) {
            this.settingName = settingName;
            this.addressInfo = new Inet6AddressInfo(
                    settingName.contains("local.link"),     //Is Link Local
                    settingName.contains(".global."),       //Is Global
                    !settingName.contains(".perm"),         //Is Temporary
                    false,                                  //Is Deprecated
                    false);                                 //Is Unique Local

            //afterLs = com.substring(0, index) + com.substring(index + toRemove.length());
            String[] parts = settingName.split("\\.");
            String netFace = parts[4];
            int bits = 0;
            if(this.addressInfo.isLinkLocal) bits += 3;
            if(this.addressInfo.isGlobal) bits += 6;
            if(this.addressInfo.isTemporary) bits += 12;
            else bits += 8;

            this.prefix = netFace + "_" + bits;
        }
    }

    @Override
    public boolean isSetting(String setting) { return getSettingName().equalsIgnoreCase(setting); }

    @Override
    public String getSettingName() {  return settingName; }

    @Override
    public String getName() { return "Host Address (IPV6) " + this.prefix; }

    @Override
    public String getID() {
        return "%ipaddress_six_" + this.prefix + "%";
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String generateString() {
        return NetUtils.convertIfInet6ToIpv6Format(this.addressInfo.generateRandomAddress());
        //StringBuilder sb = new StringBuilder();
        //for (int i = 0; i < 8; i++) {
        //    if (i != 0) {
        //        sb.append(":");
        //    }
        //    sb.append(String.format("%04x", ThreadLocalRandom.current().nextInt(65536)));
        //}
        //return sb.toString();
    }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return null; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}