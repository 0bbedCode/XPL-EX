package eu.faircode.xlua.x.hook.interceptors.build.random;

import eu.faircode.xlua.utilities.RandomStringGenerator;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomRomName extends RandomElement {
    public static IRandomizer create() { return new RandomRomName(); }
    public static final String[] ROM_NAMES = {
            // Popular Active ROMs
            "AOSP",         // Android Open Source Project
            "LineageOS",    // Formerly CyanogenMod
            "YAAP",         // Yet Another AOSP Project
            "PixelOS",      // AOSP with Pixel features
            "crDroid",      // Custom ROM Droid
            "Evolution X",  // Gaming focused ROM
            "Pixel Experience", // Pixel-like ROM
            "ArrowOS",      // Clean AOSP
            "DotOS",        // Droid on Time
            "Havoc-OS",     // Feature rich ROM
            "PixelPlusUI",  // PPUI
            "Project Elixir", // Clean + Features
            "Project Blaze", // Performance focused
            "SuperiorOS",   // Feature rich
            "Spark OS",     // Clean + Customization

            // Historical/Legacy ROMs
            "CyanogenMod",  // The original custom ROM
            "AOKP",         // Android Open Kang Project
            "Paranoid Android", // AOSPA
            "MIUI",         // Started as custom ROM before Xiaomi
            "SlimRoms",     // Lightweight ROM
            "OmniROM",      // Developer focused
            "Resurrection Remix", // RR
            "Dirty Unicorns", // DU

            // GSI ROMs (Generic System Images)
            "PHH-Treble",   // Universal GSI
            "ErfanGSI",     // Multi-port GSI
            "Descendant",   // GSI focused

            // Gaming Focused
            "Octavi OS",    // Gaming + Features
            "Corvus OS",    // Gaming optimized
            "ProtonAOSP",   // Performance focused

            // Unique/Niche ROMs
            "CalyxOS",      // Privacy focused
            "GrapheneOS",   // Security focused
            "DivestOS",     // De-Googled + Security
            "/e/OS",        // De-Googled ecosystem
            "AospExtended", // AEX
            "POSP",         // Potato Open Sauce Project
            "MSM Extended", // MSM-Xtended
            "Nusantara",    // Indonesian community ROM
            "Bootleggers",  // Feature rich + Themes
            "Ancient OS",   // Customization focused
            "Syberia OS",   // Clean + Performance
            "Cherish OS",   // Feature rich
            "Cipher OS",    // Clean + Features
            "Project Sakura", // Anime themed
            "Derpfest",     // Feature rich

            // Vendor Based
            "OxygenOS",     // OnePlus
            "ColorOS",      // OPPO
            "MIUI",         // Xiaomi
            "OneUI",        // Samsung
            "RealmeUI",     // Realme
            "FuntouchOS",   // Vivo
            "HiOS",         // Tecno
            "JoyUI",        // BlackShark
            "MagicUI",      // Honor
            "ZenUI",        // Asus
            "random"
    };

    public RandomRomName() {
        super("Build ROM Name");
        bindSetting("android.rom.name");
    }

    @Override
    public String generateString() { return RandomStringGenerator.randomStringIfRandomElse(ROM_NAMES[RandomGenerator.nextInt(0, ROM_NAMES.length)]); }
}
