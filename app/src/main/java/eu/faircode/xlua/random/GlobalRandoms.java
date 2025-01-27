package eu.faircode.xlua.random;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.random.randomizers.NARandomizer;
import eu.faircode.xlua.random.randomizers.Random3DigitNumber;
import eu.faircode.xlua.random.randomizers.Random6DigitNumber;
import eu.faircode.xlua.random.randomizers.RandomNetAddress6;
import eu.faircode.xlua.random.randomizers.RandomNetGateway;
//import eu.faircode.xlua.random.randomizers.RandomNetProvider;
import eu.faircode.xlua.random.randomizers.RandomOAIDMiuiHyper;
import eu.faircode.xlua.random.randomizers.RandomUUID;
import eu.faircode.xlua.random.randomizers.RandomAlphaNumeric;
import eu.faircode.xlua.random.randomizers.RandomAndroidID;
import eu.faircode.xlua.random.randomizers.RandomAndroidVersion;
import eu.faircode.xlua.random.randomizers.RandomBaseOs;
import eu.faircode.xlua.random.randomizers.RandomBluetoothState;
import eu.faircode.xlua.random.randomizers.RandomBoolean;
import eu.faircode.xlua.random.randomizers.RandomBuildID;
import eu.faircode.xlua.random.randomizers.RandomBuildTags;
import eu.faircode.xlua.random.randomizers.RandomBuildType;
import eu.faircode.xlua.random.randomizers.RandomBuildUser;
import eu.faircode.xlua.random.randomizers.RandomCarrierName;
import eu.faircode.xlua.random.randomizers.RandomDNS;
import eu.faircode.xlua.random.randomizers.RandomDRM;
import eu.faircode.xlua.random.randomizers.RandomDataState;
import eu.faircode.xlua.random.randomizers.RandomDateOne;
import eu.faircode.xlua.random.randomizers.RandomDateThree;
import eu.faircode.xlua.random.randomizers.RandomDateTwo;
import eu.faircode.xlua.random.randomizers.RandomDateZero;
import eu.faircode.xlua.random.randomizers.RandomDevCodeName;
import eu.faircode.xlua.random.randomizers.RandomFirebaseId;
import eu.faircode.xlua.random.randomizers.RandomGSF;
import eu.faircode.xlua.random.randomizers.RandomGameID;
import eu.faircode.xlua.random.randomizers.RandomHostName;
import eu.faircode.xlua.random.randomizers.RandomICCID;
import eu.faircode.xlua.random.randomizers.RandomIMEI;
import eu.faircode.xlua.random.randomizers.RandomKernelNodeName;
import eu.faircode.xlua.random.randomizers.RandomKernelSysName;
import eu.faircode.xlua.random.randomizers.RandomKernelRelease;
import eu.faircode.xlua.random.randomizers.RandomKernelVersion;
import eu.faircode.xlua.random.randomizers.RandomMAC;
import eu.faircode.xlua.random.randomizers.RandomMEID;
import eu.faircode.xlua.random.randomizers.RandomMSIN;
import eu.faircode.xlua.random.randomizers.RandomManufacturer;
import eu.faircode.xlua.random.randomizers.RandomMemory;
import eu.faircode.xlua.random.randomizers.RandomNetAddress;
import eu.faircode.xlua.random.randomizers.RandomNetD;
import eu.faircode.xlua.random.randomizers.RandomNetworkType;
import eu.faircode.xlua.random.randomizers.RandomPhoneNumber;
import eu.faircode.xlua.random.randomizers.RandomPhoneType;
import eu.faircode.xlua.random.randomizers.RandomSDKInit;
import eu.faircode.xlua.random.randomizers.RandomSIMCount;
import eu.faircode.xlua.random.randomizers.RandomSIMID;
import eu.faircode.xlua.random.randomizers.RandomSIMState;
import eu.faircode.xlua.random.randomizers.RandomSIMType;
import eu.faircode.xlua.random.randomizers.RandomSSID;
import eu.faircode.xlua.random.randomizers.RandomSerial;
import eu.faircode.xlua.random.randomizers.RandomSimSerial;
import eu.faircode.xlua.random.randomizers.RandomStringOne;
import eu.faircode.xlua.random.randomizers.RandomSubUsage;
import eu.faircode.xlua.random.randomizers.RandomSubscriberID;
import eu.faircode.xlua.random.randomizers.RandomUserAgentManager;
import eu.faircode.xlua.random.randomizers.RandomVoiceMailID;
import eu.faircode.xlua.random.randomizers.RandomDateEpoch;
//import eu.faircode.xlua.random.randomizers.RandomZoneParent;
import eu.faircode.xlua.random.zone.RandomZoneDep;
import eu.faircode.xlua.x.hook.interceptors.hardware.camera.RandomCameraCount;


public class GlobalRandoms {
    private static final Object lock = new Object();
    public static Map<String, IRandomizerOld> randomizers = new Hashtable<>();

    public static void putRandomizer(IRandomizerOld randomizer) { synchronized (lock) { randomizers.put(randomizer.getSettingName(), randomizer); } }
    public static void putRandomizers(List<IRandomizerOld> rs) {
        synchronized (lock) {
            for(IRandomizerOld r : rs)
                randomizers.put(r.getSettingName(), r);
        }
    }

    public static List<IRandomizerOld> getRandomizers() {
        synchronized (lock) {
            if(randomizers.isEmpty()) initRandomizers();
            List<IRandomizerOld> localCopy = new ArrayList<>(randomizers.values());
            localCopy.add(new RandomUserAgentManager());
            //localCopy.add(new RandomNetProvider());
            //localCopy.add(new RandomZoneParent());
            return localCopy;
        }
    }

    public static void initRandomizers() {
        putRandomizer(new NARandomizer());
        putRandomizer(new RandomOAIDMiuiHyper());
        putRandomizer(new RandomAndroidID());
        putRandomizer(new RandomDRM());
        putRandomizer(new RandomGSF());
        putRandomizer(new RandomIMEI());
        putRandomizer(new RandomMAC());
        putRandomizer(new RandomMEID());
        putRandomizer(new RandomUUID());
        putRandomizer(new RandomSimSerial());
        putRandomizer(new RandomSubscriberID());
        putRandomizer(new RandomSSID());
        putRandomizer(new RandomMemory());
        putRandomizer(new RandomDNS());
        putRandomizer(new RandomNetAddress());

        for(IRandomizerOld r : RandomNetAddress6.INSTANCES)
            putRandomizer(r);

        putRandomizer(new RandomNetGateway());
        putRandomizer(new RandomHostName());
        //putRandomizer(new RandomNetProvider());

        putRandomizer(new RandomSerial());
        putRandomizer(new RandomCarrierName());
        putRandomizer(new RandomMSIN());
        putRandomizer(new RandomSIMID());
        putRandomizer(new RandomSIMType());
        putRandomizer(new RandomSubUsage());
        putRandomizer(new RandomSIMState());
        putRandomizer(new RandomSIMCount());
        putRandomizer(new RandomAlphaNumeric());
        putRandomizer(new RandomPhoneType());
        putRandomizer(new Random3DigitNumber());
        putRandomizer(new RandomNetworkType());
        putRandomizer(new RandomDataState());
        putRandomizer(new RandomBluetoothState());
        putRandomizer(new RandomNetD());
        putRandomizer(new RandomICCID());
        putRandomizer(new RandomPhoneNumber());
        putRandomizer(new RandomVoiceMailID());
        putRandomizer(new RandomGameID());
        putRandomizer(new RandomBoolean());
        putRandomizer(new RandomManufacturer());
        putRandomizer(new RandomBaseOs());
        putRandomizer(new RandomDateZero());
        putRandomizer(new RandomDateEpoch());
        putRandomizer(new RandomDateOne());
        putRandomizer(new RandomDateTwo());
        putRandomizer(new RandomBuildID());
        putRandomizer(new RandomDevCodeName());
        putRandomizer(new RandomDateThree());
        putRandomizer(new RandomBuildTags());
        putRandomizer(new RandomBuildType());
        putRandomizer(new RandomBuildUser());
        putRandomizer(new RandomAndroidVersion());
        putRandomizer(new RandomSDKInit());
        putRandomizer(new RandomKernelRelease());
        putRandomizer(new RandomKernelNodeName());
        putRandomizer(new RandomKernelSysName());
        putRandomizer(new RandomKernelVersion());
        putRandomizer(new Random6DigitNumber());

        //
        //
        //

        //putRandomizer(new RandomGpsModelName());
        //putRandomizer(new RandomGpsModelYear());
        //putRandomizer(new RandomCameraCount());

        //
        //
        //



        //Collection Sort these ??????
        putRandomizers(RandomZoneDep.RANDOMIZERS);
        putRandomizer(new RandomFirebaseId());

        putRandomizer(new RandomStringOne());
    }

    public static boolean isRandomName(String name) { return "%random%".equalsIgnoreCase(name) || "%randomize%".equalsIgnoreCase(name); }
    public static void bindRandomToSettings(Map<String, String> settings) {
        List<IRandomizerOld> randomizers = getRandomizers();
        for(Map.Entry<String, String> s : settings.entrySet()) {
            if(isRandomName(s.getValue())) {
                for(IRandomizerOld r : randomizers) {
                    if(r.isSetting(s.getKey())) {
                        String nv = r.generateString();
                        settings.put(s.getKey(), nv);
                        break;
                    }
                }
            }
        }
    }
}
