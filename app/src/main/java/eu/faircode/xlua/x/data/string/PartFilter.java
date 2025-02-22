package eu.faircode.xlua.x.data.string;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.MapUtils;

public class PartFilter {
    public static PartFilter create() { return new PartFilter(); }

    public static final PartFilter DEFAULT_NUMERIC_MAPPER = PartFilter.create()
            .useFirstLetterCaps(true)
            .useUpperPair(true)
            .useNumericMapper(true);

    public static final List<String> UPPER_PAIRS = Arrays.asList("Tz", "Getprop", "Ipc", "Meminfo", "Lcd", "Gles", "Va", "Ls", "Stat", "Uuid", "Wifi", "Vpn", "Ex", "Io", "Su", "Arch", "Cpuid", "Db", "Egl", "Ab", "Gms",  "Hw", "Sms", "Icc", "No", "Sys", "Isp", "Cid", "Lac", "Mac", "Net", "Ad", "Drm", "Gsf", "Lcc", "Meid", "Imei", "Bssid", "Ssid", "Esim", "Sim", "Sku", "Lac", "Cid", "Msin", "Mnc", "Mcc", "Adb", "Os", "Utc", "Abi", "Gps", "Dns", "Vm", "Id", "Gsm", "Cpu", "Gpu", "Fp", "Rom", "Nfc", "Soc", "Url", "Dev", "Sdk", "Iso");

    public static final HashMap<String, String> NUMBER_RESOLVER_MAP = MapUtils.create(
            MapUtils.entry("1", "One"),
            MapUtils.entry("2", "Two"),
            MapUtils.entry("3", "Three"),
            MapUtils.entry("4", "Four"),
            MapUtils.entry("5", "Five"),
            MapUtils.entry("6", "Six"),
            MapUtils.entry("7", "Seven"),
            MapUtils.entry("8", "Eight"),
            MapUtils.entry("9", "Nine"),
            MapUtils.entry("0", "Zero")
    );

    public static final HashMap<String, String> NUMBER_RESOLVER_MAP_REVERSE = MapUtils.create(
            MapUtils.entry("One", "1"),
            MapUtils.entry("Two", "2"),
            MapUtils.entry("Three", "3"),
            MapUtils.entry("Four", "4"),
            MapUtils.entry("Five", "5"),
            MapUtils.entry("Six", "6"),
            MapUtils.entry("Seven", "7"),
            MapUtils.entry("Eight", "8"),
            MapUtils.entry("Nine", "9"),
            MapUtils.entry("Zero", "0")
    );

    private boolean capitalizeFirstLetters = true;
    private boolean useNumericMapper = false;
    private boolean useReverseNumericMapper = false;
    private boolean useUpperPairs = false;

    private final Map<String, String> mapper = new HashMap<>();

    public PartFilter useFirstLetterCaps(boolean useFirstLetterCaps) {
        this.capitalizeFirstLetters = useFirstLetterCaps;
        return this;
    }

    public PartFilter useNumericMapper(boolean useNumericMapper) {
        this.useNumericMapper = useNumericMapper;
        return this;
    }

    public PartFilter useReverseNumericMapper(boolean useReverseNumericMapper) {
        this.useReverseNumericMapper = useReverseNumericMapper;
        return this;
    }

    public PartFilter useUpperPair(boolean useUpperPairs) {
        this.useUpperPairs = useUpperPairs;
        return this;
    }

    public PartFilter parsePart(List<String> parts, int index) {
        String part = parts.get(index);

        if(Str.isEmpty(part)) {
            parts.remove(index);
            return this;
        }

        String cap = Str.capitalizeFirstLetter(part);
        if(useNumericMapper) {
            if(Str.isNumeric(part)) {
                String resolved = NUMBER_RESOLVER_MAP.get(part);
                if(!Str.isEmpty(resolved)) {
                    parts.set(index, resolved);
                    return this;
                }
            }
        }
        else if(useReverseNumericMapper) {
            String resolved = NUMBER_RESOLVER_MAP_REVERSE.get(cap);
            if(!Str.isEmpty(resolved)) {
                parts.set(index, resolved);
                return this;
            }
        }

        if(useUpperPairs) {
            if(UPPER_PAIRS.contains(cap)) {
                parts.set(index, cap.toUpperCase());
                return this;
            }
        }

        if(capitalizeFirstLetters) {
            parts.set(index, cap);
            return this;
        }

        return this;
    }
}
