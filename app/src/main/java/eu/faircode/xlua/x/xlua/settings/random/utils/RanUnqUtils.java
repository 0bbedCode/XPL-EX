package eu.faircode.xlua.x.xlua.settings.random.utils;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;

public class RanUnqUtils {


    public static String bluetoothAddress(RandomizerSessionContext session) {
        return hardwareAddress();
    }

    public static String mac(RandomizerSessionContext session) {
        return hardwareAddress();
    }

    public static String hardwareAddress() {
        String rawString = RandomGenerator.generateRandomHexString(12).toUpperCase();
        StringBuilder sb = new StringBuilder();
        int rawLen = rawString.length();
        for(int i = 0; i < rawString.length(); i += 2) {
            sb.append(rawString, i, Math.min(i + 2, rawLen));
            if(i + 2 < rawLen)
                sb.append(":");
        }

        return sb.toString();
    }
}
