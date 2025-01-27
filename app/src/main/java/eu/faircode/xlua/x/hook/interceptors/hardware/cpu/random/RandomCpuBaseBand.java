package eu.faircode.xlua.x.hook.interceptors.hardware.cpu.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomCpuBaseBand extends RandomElement {
    public static IRandomizer create() { return new RandomCpuBaseBand(); }

    public static final String[] BASEBAND_VALUES = {
            // Qualcomm
            "mdm",          // Mobile Device Modem
            "msm",          // Mobile Station Modem
            "apq",          // Application Processor Qualcomm (No modem)
            "sdm",          // Snapdragon Mobile
            "sm",           // Snapdragon Mobile newer naming

            // Samsung
            "exynos",       // Samsung Exynos modems
            "ss333",        // Samsung specific
            "shannon",      // Samsung Shannon modems

            // MediaTek
            "mt",           // MediaTek
            "mtk",          // MediaTek alternative

            // Huawei
            "kirin",        // Huawei Kirin
            "balong",       // Huawei Balong modems

            // Intel
            "xmm",          // Intel XMM modems
            "pmb",          // Intel baseband

            // Apple
            "intel",        // Intel modems used in iPhones
            "qualcomm",     // Qualcomm modems used in iPhones

            // Generic/Others
            "unknown",      // Unknown/Not specified
            "none",         // No baseband (Wi-Fi only devices)
            "dummy",        // Test/Development value
            "csfb",         // Circuit Switched Fallback
            "svlte",        // Simultaneous Voice and LTE
            "svdo",         // Simultaneous Voice and DO
            "sglte",        // Simultaneous GSM and LTE

            // Architecture specific
            "sdx",          // Snapdragon X modem series
            "tdscdma",      // TD-SCDMA specific
            "hspa",         // HSPA specific
            "edge",         // EDGE specific
    };

    public RandomCpuBaseBand() {
        super("CPU Base Band Model");
        bindSetting("soc.baseband.board.config.name");
    }

    @Override
    public String generateString() { return RandomGenerator.nextElement(BASEBAND_VALUES); }
}
