package eu.faircode.xlua.random.zone;

import java.util.*;
import java.util.TimeZone;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class ZoneGenerator {
    private String country;
    private String countryISO;
    private String language;
    private String languageISO;
    private String languageTag;
    private String region;
    private String timezoneOffset;
    private String timezoneDisplayName;
    private String timezoneID;

    private static final List<Locale> LOCALES = Arrays.asList(Locale.getAvailableLocales());
    private static final List<String> TIMEZONE_IDS = getAvailableTimezoneIds();
    private static final Map<String, List<Locale>> REGION_LOCALES = mapRegionToLocales();

    public ZoneGenerator() {
        generate();
    }

    private static List<String> getAvailableTimezoneIds() {
        String[] ids = TimeZone.getAvailableIDs();
        List<String> filteredIds = new ArrayList<>();
        for (String id : ids) {
            if (id.contains("/")) {
                filteredIds.add(id);
            }
        }
        return filteredIds;
    }

    private static Map<String, List<Locale>> mapRegionToLocales() {
        Map<String, List<Locale>> map = new HashMap<>();
        for (Locale locale : LOCALES) {
            String country = locale.getCountry();
            if (!country.isEmpty()) {
                TimeZone tz = TimeZone.getDefault(); // Use default timezone as a fallback
                String[] availableIDs = TimeZone.getAvailableIDs(tz.getRawOffset());
                if (availableIDs.length > 0) {
                    String region = availableIDs[0].split("/")[0];
                    if (!map.containsKey(region)) {
                        map.put(region, new ArrayList<Locale>());
                    }
                    map.get(region).add(locale);
                }
            }
        }
        return map;
    }

    private void generate() {
        String randomTimezoneId = TIMEZONE_IDS.get(RandomGenerator.nextInt(TIMEZONE_IDS.size()));
        TimeZone timeZone = TimeZone.getTimeZone(randomTimezoneId);
        this.region = randomTimezoneId.split("/")[0];

        Locale randomLocale = getCompatibleLocale(this.region);

        this.country = randomLocale.getDisplayCountry(Locale.ENGLISH);
        this.countryISO = randomLocale.getCountry();
        this.language = randomLocale.getDisplayLanguage(Locale.ENGLISH);
        this.languageISO = randomLocale.getLanguage();
        this.languageTag = randomLocale.toLanguageTag();
        this.timezoneOffset = formatOffset(timeZone.getRawOffset());
        this.timezoneDisplayName = timeZone.getDisplayName(false, TimeZone.LONG, Locale.ENGLISH);
        this.timezoneID = randomTimezoneId;
    }

    private Locale getCompatibleLocale(String region) {
        List<Locale> compatibleLocales = REGION_LOCALES.get(region);
        if (compatibleLocales != null && !compatibleLocales.isEmpty()) {
            return compatibleLocales.get(RandomGenerator.nextInt(compatibleLocales.size()));
        }
        // Fallback to a random locale if no compatible locale is found
        return LOCALES.get(RandomGenerator.nextInt(LOCALES.size()));
    }

    private String formatOffset(int offsetMillis) {
        int hours = Math.abs(offsetMillis / 3600000);
        int minutes = Math.abs((offsetMillis % 3600000) / 60000);
        return String.format(Locale.ENGLISH, "%s%02d:%02d",
                offsetMillis >= 0 ? "+" : "-", hours, minutes);
    }

    // Getters
    public String getCountry() { return country; }
    public String getCountryISO() { return countryISO; }
    public String getLanguage() { return language; }
    public String getLanguageISO() { return languageISO; }
    public String getLanguageTag() { return languageTag; }
    public String getRegion() { return region; }
    public String getTimezoneOffset() { return timezoneOffset; }
    public String getTimezoneDisplayName() { return timezoneDisplayName; }
    public String getTimezoneID() { return timezoneID; }

    @Override
    public String toString() {
        return "TimeZoneInfo{" +
                "country='" + country + '\'' +
                ", countryISO='" + countryISO + '\'' +
                ", language='" + language + '\'' +
                ", languageISO='" + languageISO + '\'' +
                ", languageTag='" + languageTag + '\'' +
                ", region='" + region + '\'' +
                ", timezoneOffset='" + timezoneOffset + '\'' +
                ", timezoneDisplayName='" + timezoneDisplayName + '\'' +
                ", timezoneID='" + timezoneID + '\'' +
                '}';
    }



    //public static void main(String[] args) {
    //    TimeZoneInfoGenerator generator = new TimeZoneInfoGenerator();
    //    System.out.println(generator);
    //}
}