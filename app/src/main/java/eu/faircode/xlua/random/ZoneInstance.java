package eu.faircode.xlua.random;

import androidx.annotation.NonNull;



import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class ZoneInstance {
    public String country;
    public String countryIso;
    public String languageTag;
    public String languageIso;
    public String timeZoneOffset;
    public String timeZoneId;
    public String timeZoneDisplayName;
    public ZoneInstance() { }

    public void randomizeAll() {
        int selection = RandomGenerator.nextInt(0, 50);
        country = ZoneRandom.TIMEZONE_COUNTRIES[selection];
        countryIso = ZoneRandom.TIMEZONE_ISO[selection];
        languageTag = ZoneRandom.LANGUAGE_TAGS[selection];
        languageIso = ZoneRandom.LANGUAGE_ISO[selection];
        timeZoneOffset = ZoneRandom.TIMEZONE_OFFSETS[selection];
        timeZoneId = ZoneRandom.TIMEZONE_IDS[selection];
        timeZoneDisplayName = ZoneRandom.TIMEZONE_DISPLAY_NAMES[selection];
    }

    @NonNull
    @Override
    public String toString() {
        return new StringBuilder()
                .append(" Country=")
                .append(country).append("\n")
                .append(" Country ISO=")
                .append(countryIso).append("\n")
                .append(" Language Tag=")
                .append(languageTag).append("\n")
                .append(" Language ISO=")
                .append(languageIso).append("\n")
                .append(" TimeZone Offset=")
                .append(timeZoneOffset).append("\n")
                .append(" TimeZone ID=")
                .append(timeZoneId).append("\n")
                .append(" TimeZone DisplayName=")
                .append(timeZoneDisplayName).append("\n")
                .toString();
    }
}
