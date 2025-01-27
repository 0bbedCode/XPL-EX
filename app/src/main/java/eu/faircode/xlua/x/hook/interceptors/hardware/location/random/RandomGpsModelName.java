package eu.faircode.xlua.x.hook.interceptors.hardware.location.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomGpsModelName extends RandomElement {
    public static final String[] GPS_DEVICE_MODELS = {
            // Qualcomm GPS Receivers
            "WCN3990 GPS",
            "WCN3980 GPS",
            "WCN3950 GPS",
            "QCA1530 GNSS",
            "QCA1530C GNSS",
            "QCA6584 GNSS",
            "QCA6574 GNSS",
            "SM8250 Integrated GPS",
            "SM8350 Integrated GPS",
            "SM8450 Integrated GPS",

            // Broadcom GPS Receivers
            "BCM47755 GNSS",
            "BCM47754 GNSS",
            "BCM4775X GNSS",
            "BCM4774X GNSS",
            "BCM47531 GNSS",
            "BCM47521 GNSS",
            "BCM4752 GPS",
            "BCM4751 GPS",

            // MediaTek GPS Receivers
            "MT6893 GPS",
            "MT6885 GPS",
            "MT6877 GPS",
            "MT6833 GPS",
            "MT6789 GPS",
            "MT3339 GPS",
            "MT3337 GPS",
            "MT3333 GPS",
            "MT3332 GPS",

            // Samsung Exynos GPS
            "Exynos 2200 Integrated GPS",
            "Exynos 2100 Integrated GPS",
            "Exynos 1080 Integrated GPS",
            "Exynos 990 Integrated GPS",
            "Exynos 980 Integrated GPS",

            // Sony GPS Receivers
            "CXD5603GF GNSS",
            "CXD5602GF GNSS",
            "CXD5601GF GNSS",

            // u-blox GPS Modules
            "NEO-M8N GNSS",
            "NEO-M9N GNSS",
            "ZED-F9P GNSS",
            "MAX-M8Q GPS",
            "MAX-M10S GPS",

            // SiRF GPS Chipsets
            "SiRFstarV",
            "SiRFstar IV",
            "SiRFstar III",

            // Intel GPS
            "PMB 5750 GPS",
            "PMB 5745 GPS",
            "XMM 7560 Integrated GPS",

            // HiSilicon GPS
            "Kirin 9000 Integrated GPS",
            "Kirin 990 Integrated GPS",
            "Kirin 980 Integrated GPS",
            "Kirin 970 Integrated GPS"
    };


    public static RandomGpsModelName create() { return new RandomGpsModelName(); }
    public RandomGpsModelName() {
        super("GPS Hardware Model Name");
        bindSetting("hardware.gps.model.name");
    }

    @Override
    public String generateString() { return GPS_DEVICE_MODELS[RandomGenerator.nextInt(0, GPS_DEVICE_MODELS.length)]; }
}