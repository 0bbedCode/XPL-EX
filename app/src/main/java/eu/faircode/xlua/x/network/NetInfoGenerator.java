package eu.faircode.xlua.x.network;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class NetInfoGenerator {
    //2001:558:feed::2 = Comcast
    private static final String TAG = "XLua.NetInfoGenerator";

    public static final List<String> PROVIDERS = Arrays.asList("Comcast", "AT&T", "Verizon", "Spectrum", "Cox", "CenturyLink", "Frontier", "Optimum", "Google Fiber", "Windstream");

    public static final List<String> allDnsServers = new ArrayList<>(Arrays.asList(
            "8.8.8.8", "8.8.4.4",                   // Google
            "1.1.1.1", "1.0.0.1",                   // Cloudflare
            "9.9.9.9", "149.112.112.112",           // Quad9
            "208.67.222.222", "208.67.220.220",     // OpenDNS
            "64.6.64.6", "64.6.65.6",               // Verisign
            "84.200.69.80", "84.200.70.40",         // DNS.WATCH
            "8.26.56.26", "8.20.247.20",            // Comodo Secure DNS
            "195.46.39.39", "195.46.39.40",         // SafeDNS
            "77.88.8.8", "77.88.8.1",               // Yandex.DNS
            "176.103.130.130", "176.103.130.131",   // AdGuard DNS
            "156.154.70.1", "156.154.71.1",         // Neustar
            "199.85.126.10", "199.85.127.10",       // Norton ConnectSafe
            "81.218.119.11", "209.88.198.133"       // GreenTeamDNS
    ));

    private final String provider;
    private String ipv4Address;
    private String netmask;
    private String gateway;
    private List<String> routes;
    private String ipv6Address;
    private List<String> dnsServers;
    private String domain;
    private String dhcpServer;

    private static final Map<String, List<String>> PROVIDER_DOMAINS = new HashMap<>();
    static {
        PROVIDER_DOMAINS.put("Comcast", Arrays.asList(
                "hsd1.{state}.comcast.net",
                "hsd2.{state}.comcast.net",
                "business.{state}.comcast.net",
                "cable.comcast.com",
                "{ip-dots}.static.comcast.net"
        ));
        PROVIDER_DOMAINS.put("AT&T", Arrays.asList(
                "lightspeed.{city}.sbcglobal.net",
                "adsl-{ip-dashes}.dsl.{city}.sbcglobal.net",
                "ip{ip-dots}.att.net",
                "mobile-{ip-dashes}.mycingular.net"
        ));
        PROVIDER_DOMAINS.put("Verizon", Arrays.asList(
                "fios-{ip-dots}.hsd1.{state}.verizon.net",
                "pool-{ip-dashes}.{city}.fios.verizon.net",
                "ppp-{ip-dashes}.dsl-east.verizon.net",
                "mobile-{ip-dashes}.verizon.com"
        ));
        PROVIDER_DOMAINS.put("Spectrum", Arrays.asList(
                "cpe-{ip-dashes}.{region}.res.rr.com",
                "cpe-{ip-dashes}.{city}.res.rr.com",
                "dynip-{ip-dashes}.{region}.spectrum.com"
        ));
        PROVIDER_DOMAINS.put("Cox", Arrays.asList(
                "ip{ip-dots}.{region}.cox.net",
                "wsip-{ip-dashes}.{region}.cox.net",
                "static-{ip-dots}.{city}.cox.net"
        ));
        PROVIDER_DOMAINS.put("CenturyLink", Arrays.asList(
                "{ip-dots}.dia.static.qwest.net",
                "{ip-dots}.biz.static.qwest.net",
                "static-{ip-dashes}.{region}.centurylink.net"
        ));
        PROVIDER_DOMAINS.put("Frontier", Arrays.asList(
                "{ip-dots}.frontiernet.net",
                "{ip-dots}.ftn-cpe.net",
                "pool-{ip-dashes}.{city}.fios.ftr.com"
        ));
        PROVIDER_DOMAINS.put("Optimum", Arrays.asList(
                "ool-{ip-hex}.dyn.optonline.net",
                "pool-{ip-dashes}.{region}.east.optimum.net",
                "static-{ip-dots}.{city}.optimum.net"
        ));
        PROVIDER_DOMAINS.put("Google Fiber", Arrays.asList(
                "fiber-{ip-dots}.{city}.googlefiber.net",
                "static.{ip-dots}.clients.your.googlefiber.net",
                "dynamicip-{ip-dashes}.{region}.googlefiber.net"
        ));
        PROVIDER_DOMAINS.put("Windstream", Arrays.asList(
                "{ip-dots}.windstream.net",
                "{ip-dots}.win.windstream.net",
                "static-{ip-dashes}.{region}.windstream.net"
        ));
    }

    private static final String[] STATES = {"il", "ma", "ca", "ny", "tx", "fl", "wa", "ga", "pa", "oh", "nc", "mi", "nj", "va", "az"};
    private static final String[] CITIES = {"chicago", "boston", "losangeles", "newyork", "dallas", "miami", "seattle", "atlanta", "philadelphia", "cleveland", "charlotte", "detroit", "newark", "richmond", "phoenix"};
    private static final String[] REGIONS = {"socal", "norcal", "midwest", "northeast", "southeast", "northwest", "southwest", "midatlantic", "greatplains", "rockies"};

    public NetInfoGenerator() { this(null); }
    public NetInfoGenerator(String provider) {
        this.provider = TextUtils.isEmpty(provider) || !PROVIDERS.contains(provider) ?
                PROVIDERS.get(RandomGenerator.nextInt(PROVIDERS.size())) :
                provider;
        generate();
    }

    private void generate() {
        this.ipv4Address = generateRandomPrivateIPv4();
        this.netmask = generateNetmask();
        this.gateway = generateGateway();
        this.routes = generateRoutes();
        this.ipv6Address = generateIPv6Address();
        this.dnsServers = generateDNSServers();
        this.domain = generateDomain();
        this.dhcpServer = generateDHCPServer();
    }

    private String generateRandomPrivateIPv4() {
        int choice = RandomGenerator.nextInt(3);
        switch (choice) {
            case 0:
                return "10." +
                        RandomGenerator.nextInt(256) + "." +
                        RandomGenerator.nextInt(256) + "." +
                        RandomGenerator.nextInt(1, 255);
            case 1:
                return "172." +
                        RandomGenerator.nextInt(16, 32) + "." +
                        RandomGenerator.nextInt(256) + "." +
                        RandomGenerator.nextInt(1, 255);
            case 2:
                return "192.168." +
                        RandomGenerator.nextInt(256) + "." +
                        RandomGenerator.nextInt(1, 255);
            default:
                throw new IllegalStateException("Unexpected value: " + choice);
        }
    }

    private String generateNetmask() {
        String[] commonNetMasks = {"255.255.255.0", "255.255.0.0", "255.0.0.0"};
        return commonNetMasks[RandomGenerator.nextInt(commonNetMasks.length)];
    }

    private String generateGateway() {
        String[] parts = ipv4Address.split("\\.");
        return parts[0] + "." + parts[1] + "." + parts[2] + ".1";
    }

    private List<String> generateRoutes() {
        List<String> routes = new ArrayList<>();
        routes.add("0.0.0.0/0 via " + gateway);
        routes.add(ipv4Address.substring(0, ipv4Address.lastIndexOf(".")) + ".0/24 dev eth0");
        return routes;
    }

    private String generateIPv6Address() {
        StringBuilder sb = new StringBuilder();
        sb.append("2001:db8:"); // Using the documentation prefix
        for (int i = 0; i < 6; i++) {
            sb.append(String.format("%04x", RandomGenerator.nextInt(65536)));
            if (i < 5) sb.append(":");
        }
        return sb.toString();
    }

    // Add this new method
    private String generateDHCPServer() {
        String[] parts = ipv4Address.split("\\.");
        int choice = RandomGenerator.nextInt(4);

        switch (choice) {
            case 0:
                // Use gateway address as DHCP server
                return gateway;
            case 1:
                // Use common DHCP server address patterns
                return parts[0] + "." + parts[1] + "." + parts[2] + ".254";
            case 2:
                // Another common pattern is .250
                return parts[0] + "." + parts[1] + "." + parts[2] + ".250";
            case 3:
                // Some networks use .2 as DHCP server
                return parts[0] + "." + parts[1] + "." + parts[2] + ".2";
            default:
                return gateway;
        }
    }

    private List<String> generateDNSServers() {
        //List<String> dnsServers = new ArrayList<>();
        //dnsServers.add("8.8.8.8"); // Google's public DNS
        //dnsServers.add("1.1.1.1"); // Cloudflare's public DNS
        // Randomly select 2-4 DNS servers
        int numServers = RandomGenerator.nextInt(2, 5);
        List<String> selectedServers = new ArrayList<>(numServers);
        List<String> allCache = new ArrayList<>(allDnsServers);

        for (int i = 0; i < numServers; i++) {
            if (allCache.isEmpty()) break;
            int index = RandomGenerator.nextInt(allCache.size());
            selectedServers.add(allCache.remove(index));
        }

        // If the provider is specified, potentially add a provider-specific DNS
        if (provider != null) {
            switch (provider) {
                case "Comcast":
                    selectedServers.add("75.75.75.75");
                    break;
                case "AT&T":
                    selectedServers.add("192.168.1.254");
                    break;
                case "Verizon":
                    selectedServers.add("71.242.0.12");
                    break;
                case "Spectrum":
                    selectedServers.add("209.18.47.61");
                    break;
                case "Cox":
                    selectedServers.add("68.105.28.11");
                    break;
                case "CenturyLink":
                    selectedServers.add("205.171.3.25");
                    break;
                case "Google Fiber":
                    selectedServers.add("108.170.247.24");
                    break;
                default:
                    break;
                // Add more provider-specific DNS servers as needed
            }
        }

        return selectedServers;
    }

    private String generateDomain() {
        List<String> domainPatterns = PROVIDER_DOMAINS.get(provider);
        if(domainPatterns == null)
            domainPatterns = PROVIDER_DOMAINS.get(PROVIDERS.get(RandomGenerator.nextInt(PROVIDERS.size())));

        String pattern = domainPatterns.get(RandomGenerator.nextInt(domainPatterns.size()));

        pattern = pattern.replace("{state}", STATES[RandomGenerator.nextInt(STATES.length)]);
        pattern = pattern.replace("{city}", CITIES[RandomGenerator.nextInt(CITIES.length)]);
        pattern = pattern.replace("{region}", REGIONS[RandomGenerator.nextInt(REGIONS.length)]);
        pattern = pattern.replace("{ip-dots}", ipv4Address);
        pattern = pattern.replace("{ip-dashes}", ipv4Address.replace('.', '-'));
        pattern = pattern.replace("{ip-hex}", String.format("%08x", (int)(Long.parseLong(ipv4Address.replace(".", "")) & 0xFFFFFFFFL)));
        if(DebugUtil.isDebug())
            Log.d(TAG, "Domain=" + pattern);

        return pattern;
    }

    // Getters
    public String getProvider() { return provider; }
    public String getIpv4Address() { return ipv4Address; }
    public String getNetmask() { return netmask; }
    public String getGateway() { return gateway; }
    public List<String> getRoutes() { return routes; }
    public String getIpv6Address() { return ipv6Address; }
    public List<String> getDnsServers() { return dnsServers; }
    public String getDomain() { return domain; }
    public String getDhcpServer() { return dhcpServer; }

    @Override
    public String toString() {
        return "NetworkInfoGenerator{" +
                "provider='" + provider + '\'' +
                ", ipv4Address='" + ipv4Address + '\'' +
                ", netmask='" + netmask + '\'' +
                ", gateway='" + gateway + '\'' +
                ", routes=" + routes +
                ", ipv6Address='" + ipv6Address + '\'' +
                ", dnsServers=" + dnsServers +
                ", domain='" + domain + '\'' +
                '}';
    }
}