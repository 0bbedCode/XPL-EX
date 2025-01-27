package eu.faircode.xlua.x.xlua.settings.random.utils;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;

public class RanHwdUtils {
    public static final String[] NFC_CONTROLLERS = {
            "I2C",                  // Basic I2C NFC controller
            "PN544",               // NXP/Philips controller, common in older devices
            "PN547",               // NXP mid-range controller
            "PN548",               // NXP controller
            "PN553",               // NXP newer generation
            "PN557",               // NXP modern controller
            "PN65T",               // NXP secure element + NFC
            "PN66T",               // NXP secure element + NFC newer gen
            "PN80T",               // NXP's high-end controller
            "PN81A",               // NXP latest generation
            "SN100",               // NXP secure element series
            "SN110",               // NXP newer secure element series
            "SN220",               // NXP latest secure element series
            "BCM2079x",           // Broadcom NFC controller
            "BCM20795",           // Broadcom newer series
            "BCM20797",           // Broadcom latest gen
            "CXD2235AGG",         // Sony NFC controller
            "SEC",                // Samsung Electronics Controller
            "ST21NFCD",           // STMicroelectronics controller
            "ST54J",              // STMicroelectronics newer series
            "RC531",              // Samsung controller
            "NQ310",              // Qualcomm NFC controller
            "NQ330",              // Qualcomm newer gen
            "NQ4XX",              // Qualcomm latest series
            "SGP30"               // Sensirion controller
    };

    public static final String[] NFC_CONTROLLER_INTERFACES = {
            "nqx.default",         // Qualcomm NFC interface
            "nxp.default",         // NXP default interface
            "nxp.nci",            // NXP NCI interface
            "nxp.ese",            // NXP ESE interface
            "samsung.nfc",        // Samsung NFC interface
            "sec.default",        // Samsung (SEC) default interface
            "bcm.default",        // Broadcom default interface
            "st.default",         // STMicroelectronics default
            "st21nfc.default",    // ST21 series interface
            "pn54x.default",      // NXP PN54x series interface
            "pn553.default",      // NXP PN553 specific interface
            "sn100.default",      // NXP SN100 series interface
            "android.hardware.nfc@1.0-impl",  // AOSP default implementation
            "android.hardware.nfc@1.1-impl",  // AOSP 1.1 implementation
            "android.hardware.nfc@1.2-impl",  // AOSP 1.2 implementation
            "android.hardware.secure_element@1.0-impl"  // Secure Element implementation
    };

    public static final String[] FINGERPRINT_SENSOR_INTERFACES = {
            "gdx",           // Goodix
            "fps_hal",       // FPC (Fingerprint Cards)
            "elan",          // ELAN
            "silead",        // Silead
            "cdfinger",      // Chipone
            "focal",         // Focal
            "fpc",           // Fingerprint Cards alternate interface
            "sunwave",       // Sunwave
            "qfp",           // Qualcomm Fingerprint
            "gf",            // Goodix alternate interface
            "vfinger",       // Validity
            "synaptics"      // Synaptics
    };

    public static final String[] FINGERPRINT_MANUFACTURERS = {
            "goodix",                // Goodix Technology
            "fingerprint_cards",     // FPC (Fingerprint Cards AB)
            "elan_fingerprint",      // ELAN Microelectronics
            "silead_fingerprint",    // Silead Inc
            "chipone",              // Chipone Technology
            "focal_fingerprint",     // Focal Tech Systems
            "fpc_fingerprint",       // FPC alternate name
            "sunwave_fp",           // Sunwave Communications
            "qualcomm_fingerprint", // Qualcomm
            "validity_fingerprint", // Validity Sensors
            "synaptics_fingerprint" // Synaptics Inc
    };

    public static final String[] CAMERA_PACKAGE_NAMES = {
            "com.android.camera",                    // AOSP default camera
            "com.android.camera2",                   // AOSP camera2 API implementation
            "com.google.android.GoogleCamera",       // Google Camera (GCam)
            "com.sec.android.app.camera",            // Samsung Camera
            "com.huawei.camera",                     // Huawei Camera
            "com.sonyericsson.android.camera",       // Sony Camera
            "com.motorola.camera",                   // Motorola Camera
            "com.oneplus.camera",                    // OnePlus Camera
            "com.oppo.camera",                       // OPPO Camera
            "com.oplus.camera",                      // OPLUS/Realme Camera
            "com.vivo.camera",                       // Vivo Camera
            "com.mediatek.camera",                   // MediaTek Camera
            "com.asus.camera",                       // ASUS Camera
            "com.lge.camera",                        // LG Camera
            "com.xiaomi.camera",                     // Xiaomi Camera
            "com.miui.camera",                       // MIUI Camera
            "org.codeaurora.snapcam",               // Snapdragon Camera
            "com.hmdglobal.camera2",                // Nokia Camera
            "com.tcl.camera",                        // TCL Camera
            "com.zte.camera",                        // ZTE Camera
            "com.lenovo.camera",                     // Lenovo Camera
            "com.honor.camera",                      // Honor Camera
            "com.nothing.camera"                     // Nothing Phone Camera
    };

    // Map interface names to their full manufacturer names
    /*public static final Map<String, String> FINGERPRINT_INTERFACE_TO_MANUFACTURER = Map.of(
            "gdx", "goodix",
            "fps_hal", "fingerprint_cards",
            "elan", "elan_fingerprint",
            "silead", "silead_fingerprint",
            "cdfinger", "chipone",
            "focal", "focal_fingerprint",
            "fpc", "fpc_fingerprint",
            "sunwave", "sunwave_fp",
            "qfp", "qualcomm_fingerprint",
            "gf", "goodix",
            "vfinger", "validity_fingerprint",
            "synaptics", "synaptics_fingerprint"
    );*/

    public static final String[] SOC_MODELS = {
            // Qualcomm Snapdragon
            "SM8550-AB",  // Snapdragon 8 Gen 2
            "SM8450",     // Snapdragon 8 Gen 1
            "SM8350",     // Snapdragon 888
            "SM8250",     // Snapdragon 865
            "SM8150",     // Snapdragon 855
            "SM7325",     // Snapdragon 778G
            "SM7225",     // Snapdragon 750G
            "SM6375",     // Snapdragon 695
            "SM6225",     // Snapdragon 680
            "SDM845",     // Snapdragon 845
            "SDM835",     // Snapdragon 835
            "MSM8998",    // Snapdragon 835 variant
            "MSM8996",    // Snapdragon 820
            "MSM8953",    // Snapdragon 625

            // MediaTek
            "MT6983",     // Dimensity 9000
            "MT6895",     // Dimensity 8100
            "MT6893",     // Dimensity 1200
            "MT6885",     // Dimensity 1000
            "MT6833",     // Dimensity 700
            "MT6879",     // Dimensity 920
            "MT6877",     // Dimensity 900
            "MT6853",     // Dimensity 720
            "MT6873",     // Dimensity 800
            "MT6785",     // Helio G90T
            "MT6768",     // Helio P65/G85
            "MT6771",     // Helio P60/P70

            // Samsung Exynos
            "S5E9925",    // Exynos 2200
            "S5E9820",    // Exynos 990
            "S5E9810",    // Exynos 982x
            "S5E9610",    // Exynos 9610
            "S5E8825",    // Exynos 850

            // Google Tensor
            "GS201",      // Tensor G2
            "GS101",      // Tensor

            // Unisoc
            "T610",       // Unisoc Tiger T610
            "T612",       // Unisoc Tiger T612
            "T700",       // Unisoc Tiger T700
            "T740"        // Unisoc Tiger T740
    };

    public static String gpsModelName(RandomizerSessionContext session) {
        return RandomGenerator.nextString(8, 28);
    }














    /*public static final String[] SOC_MODELS = {
            // Qualcomm Snapdragon
            "SM8550-AB",  // Snapdragon 8 Gen 2
            "SM8450",     // Snapdragon 8 Gen 1
            "SM8350",     // Snapdragon 888
            "SM8250",     // Snapdragon 865
            "SM8150",     // Snapdragon 855
            "SM7325",     // Snapdragon 778G
            "SM7225",     // Snapdragon 750G
            "SM6375",     // Snapdragon 695
            "SM6225",     // Snapdragon 680
            "SDM845",     // Snapdragon 845
            "SDM835",     // Snapdragon 835
            "MSM8998",    // Snapdragon 835 variant
            "MSM8996",    // Snapdragon 820
            "MSM8953",    // Snapdragon 625

            // MediaTek
            "MT6983",     // Dimensity 9000
            "MT6895",     // Dimensity 8100
            "MT6893",     // Dimensity 1200
            "MT6885",     // Dimensity 1000
            "MT6833",     // Dimensity 700
            "MT6879",     // Dimensity 920
            "MT6877",     // Dimensity 900
            "MT6853",     // Dimensity 720
            "MT6873",     // Dimensity 800
            "MT6785",     // Helio G90T
            "MT6768",     // Helio P65/G85
            "MT6771",     // Helio P60/P70

            // Samsung Exynos
            "S5E9925",    // Exynos 2200
            "S5E9820",    // Exynos 990
            "S5E9810",    // Exynos 982x
            "S5E9610",    // Exynos 9610
            "S5E8825",    // Exynos 850

            // Google Tensor
            "GS201",      // Tensor G2
            "GS101",      // Tensor

            // Unisoc
            "T610",       // Unisoc Tiger T610
            "T612",       // Unisoc Tiger T612
            "T700",       // Unisoc Tiger T700
            "T740"        // Unisoc Tiger T740
    };*/

    public static String socModel(RandomizerSessionContext session) {
        return RandomGenerator.nextElement(SOC_MODELS);
    }

    public static final String[] SOC_CODENAMES = {
            // Qualcomm Snapdragon
            "kalama",       // Snapdragon 8 Gen 2
            "taro",         // Snapdragon 8 Gen 1
            "lahaina",      // Snapdragon 888/888+
            "kona",         // Snapdragon 865/865+
            "msmnile",      // Snapdragon 855/855+
            "sdm845",       // Snapdragon 845
            "msm8998",      // Snapdragon 835
            "msm8996",      // Snapdragon 820/821
            "bengal",       // Snapdragon 662/460
            "trinket",      // Snapdragon 665
            "sm6125",       // Snapdragon 665
            "sdm710",       // Snapdragon 710
            "sdm660",       // Snapdragon 660
            "msm8953",      // Snapdragon 625
            "msm8937",      // Snapdragon 430
            "sm6150",       // Snapdragon 675
            "lito",         // Snapdragon 765G/768G
            "atoll",        // Snapdragon 730G

            // MediaTek
            "mt6893",       // Dimensity 1200
            "mt6885",       // Dimensity 1000
            "mt6873",       // Dimensity 800
            "mt6853",       // Dimensity 720
            "mt6833",       // Dimensity 700
            "mt6771",       // Helio P60/P70
            "mt6768",       // Helio P65/G85
            "mt6765",       // Helio P35/G35
            "mt6761",       // Helio A22

            // Samsung Exynos
            "exynos9825",   // Exynos 9825
            "exynos9820",   // Exynos 9820
            "exynos9810",   // Exynos 9810
            "exynos8895",   // Exynos 8895
            "exynos8890",   // Exynos 8890
            "exynos7885",   // Exynos 7885

            // Google Tensor
            "gs201",        // Tensor G2
            "gs101",        // Tensor

            // Unisoc
            "ums512",       // T700 series
            "ums510",       // T610 series
            "ums9230"       // T616/T606
    };

    public static final String[] SOC_MANUFACTURER_IDS = {
            "qcom",      // Qualcomm
            "mtk",       // MediaTek
            "samsung",   // Samsung
            "slsi",      // Samsung LSI (alternative ID)
            "exynos",    // Samsung Exynos (alternative ID)
            "google",    // Google
            "sprd",      // Spreadtrum/Unisoc
            "unisoc",    // Unisoc (newer ID)
            "amlogic",   // Amlogic
            "kirin",     // Huawei HiSilicon
            "broadcom",  // Broadcom
            "nvidia",    // NVIDIA
            "intel",     // Intel
            "rk",        // RockChip
            "mrvl",      // Marvell
            "ti"         // Texas Instruments
    };


    public static final int[] SOC_PROCESSOR_COUNT = {
            1,
            2,
            3,
            4,
            5,
            6,
            7,
            8,
    };


    public static final String[] PROCESSOR_ARCHITECTURES = {
            // ARM 64-bit
            "aarch64",      // ARM 64-bit (ARM v8)
            "arm64",        // Alternative name for ARM 64-bit
            "arm64-v8a",    // Android specific ARM 64-bit

            // ARM 32-bit
            "arm",          // ARM 32-bit
            "armv7",        // ARM v7
            "armv7l",       // ARM v7 little-endian
            "armv7a",       // ARM v7a
            "armv8l",       // ARM v8 32-bit mode
            "armeabi",      // Android ARM EABI
            "armeabi-v7a",  // Android ARM v7 EABI

            // x86 Architectures
            "x86",          // 32-bit x86
            "i686",         // x86 32-bit specific
            "x86_64",       // x86 64-bit
            "amd64",        // Alternative name for x86_64

            // MIPS (Legacy)
            "mips",         // 32-bit MIPS
            "mips64",       // 64-bit MIPS

            // RISC-V (Emerging)
            "riscv",        // RISC-V base
            "riscv64",      // 64-bit RISC-V
            "riscv32"       // 32-bit RISC-V
    };






    public static String socManId(RandomizerSessionContext session) {
        return RandomGenerator.nextElement(SOC_MANUFACTURER_IDS);
    }

    public static String socCodeNames(RandomizerSessionContext session) {
        return RandomGenerator.nextElement(SOC_CODENAMES);
    }






    public static final String[] CPU_INSTRUCTION_SET_32 = {
            "cortex-a7",      // Entry-level 32-bit processor
            "cortex-a8",      // Single-core 32-bit processor
            "cortex-a9",      // Multi-core 32-bit processor
            "cortex-a15",     // High-performance 32-bit processor
            "cortex-a17",     // Mid-range 32-bit processor
            "cortex-a32",     // Last dedicated 32-bit Cortex-A processor
            "cortex-a35",     // Entry-level ARMv8 32-bit mode
            "cortex-a53",     // Entry/Mid-level ARMv8 32-bit mode
            "cortex-a55",     // Entry/Mid-level ARMv8.2 32-bit mode
            "cortex-a57",     // High-performance ARMv8 32-bit mode
            "cortex-a72",     // High-performance ARMv8 32-bit mode
            "cortex-a73",     // High-efficiency ARMv8 32-bit mode
            "cortex-a75",     // High-performance ARMv8.2 32-bit mode
            "cortex-a76",     // High-performance ARMv8.2 32-bit mode
            "cortex-a77",     // High-performance ARMv8.2 32-bit mode
            "cortex-a78",     // High-performance ARMv8.2 32-bit mode
            "kryo",           // Qualcomm Custom 32-bit
            "kryo-585",       // Qualcomm Custom newer 32-bit
            "denver",         // NVIDIA Custom 32-bit
            "denver2"         // NVIDIA Custom newer 32-bit
    };

    public static final String[] CPU_INSTRUCTION_SET_64 = {
            "kryo",           // Qualcomm Custom 64-bit
            "kryo-585",       // Qualcomm Custom newer 64-bit
            "kryo-680",       // Qualcomm Latest Custom 64-bit
            "kryo-780",       // Qualcomm Latest+ Custom 64-bit
            "cortex-a53",     // Entry-level ARMv8 64-bit
            "cortex-a55",     // Entry-level ARMv8.2 64-bit
            "cortex-a57",     // High-performance ARMv8 64-bit
            "cortex-a72",     // High-performance ARMv8 64-bit
            "cortex-a73",     // High-efficiency ARMv8 64-bit
            "cortex-a75",     // High-performance ARMv8.2 64-bit
            "cortex-a76",     // High-performance ARMv8.2 64-bit
            "cortex-a77",     // High-performance ARMv8.2 64-bit
            "cortex-a78",     // High-performance ARMv8.2 64-bit
            "cortex-a710",    // ARMv9 64-bit
            "cortex-a715",    // Latest ARMv9 64-bit
            "cortex-x1",      // Ultra high-performance ARMv8.2 64-bit
            "cortex-x2",      // Ultra high-performance ARMv9 64-bit
            "cortex-x3",      // Latest Ultra high-performance ARMv9 64-bit
            "denver",         // NVIDIA Custom 64-bit
            "denver2",        // NVIDIA Custom newer 64-bit
            "carmel"          // NVIDIA Latest Custom 64-bit
    };





    // Full ABI list (like ro.product.cpu.abilist)
    public static final String[] CPU_ABI_LIST = {
            "arm64-v8a,armeabi-v7a,armeabi",          // Common ARM 64+32 combo
            "arm64-v8a,armeabi-v7a",                  // Modern ARM 64+32 combo
            "x86_64,x86,arm64-v8a,armeabi-v7a",       // x86 emulator with ARM
            "x86_64,x86",                             // Pure x86 64+32
            "arm64-v8a",                              // Pure ARM64
            "armeabi-v7a,armeabi",                    // Legacy ARM 32-bit
            "x86",                                    // Legacy x86 32-bit
            "mips64,mips",                            // Legacy MIPS (rare)
            "riscv64"                                 // RISC-V 64-bit (emerging)
    };

    // 32-bit ABI list (like ro.product.cpu.abilist32)
    public static final String[] CPU_ABI_LIST_32 = {
            "armeabi-v7a,armeabi",    // Common ARM 32-bit
            "armeabi-v7a",            // Modern ARM 32-bit
            "x86",                    // x86 32-bit
            "armeabi",                // Legacy ARM
            "mips"                    // Legacy MIPS
    };

    // 64-bit ABI list (like ro.product.cpu.abilist64)
    public static final String[] CPU_ABI_LIST_64 = {
            "arm64-v8a",              // ARM 64-bit
            "x86_64",                 // x86 64-bit
            "mips64",                 // MIPS 64-bit
            "riscv64"                 // RISC-V 64-bit
    };

    // Individual ABIs (for reference)
    public static final String[] CPU_ABI_SINGLES = {
            "arm64-v8a",      // ARM 64-bit
            "armeabi-v7a",    // ARM 32-bit (modern)
            "armeabi",        // ARM 32-bit (legacy)
            "x86_64",         // x86 64-bit
            "x86",            // x86 32-bit
            "mips64",         // MIPS 64-bit
            "mips",           // MIPS 32-bit
            "riscv64"         // RISC-V 64-bit
    };




}
