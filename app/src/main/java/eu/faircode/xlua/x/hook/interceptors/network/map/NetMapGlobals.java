package eu.faircode.xlua.x.hook.interceptors.network.map;

//Have a Identity Core
//Where do you live
//WHat is your ISP
//What cell carrier ?
//Too much for now thats new app shit

public class NetMapGlobals {
    public static final String SETTING_MAC = "";
    public static final String SETTING_ISP = "";
    public static final String SETTING_WLAN_0_IP_ADDRESS = "";
    public static final String SETTING_WLAN_0_MAC_ADDRESS = "";

    //Override
    public static final String SETTING_WLAN_0_GATEWAY = "";


    public static class NetworkIdentity {
        public String gateway;
        //public String
    }

    //FOR NOW have the user set this lets keep focused on Network side of things
    public static class TimeZoneIdentity {
        public String country;
        public String countryIso;
        public String language;
        public String languageIso;
        public String languageTag;
        public String region;
        public String timeZoneOffset;
        public String timeZoneDisplayName;
        public String timeZoneId;

        public TimeZoneIdentity(String country) {
            //So lets only use the country then go from there
            //Ez
        }
    }

    public static class CellIdentity {
        public int mnc;
        public int mcc;
        public boolean isESim;
        public boolean isGsm;
        public boolean isCdma;

        public long lac;
        public long cid;

        public CellIdentity(String country, String carrier) {

        }
    }
}
