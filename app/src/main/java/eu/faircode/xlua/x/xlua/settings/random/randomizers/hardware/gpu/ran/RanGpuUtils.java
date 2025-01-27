package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.ran;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;

public class RanGpuUtils {



    public static final String[] GPU_EGL_IMPLEMENTORS = {
            "adreno",           // Qualcomm Adreno
            "mali",            // ARM Mali
            "powervr",         // Imagination PowerVR
            "nvidia",          // NVIDIA Tegra
            "vivante",         // Vivante GC
            "videocore",       // Broadcom VideoCore
            "intel",           // Intel HD/Iris
            "freedreno",       // Open source Adreno driver
            "lima",            // Open source Mali driver
            "panfrost",        // Another open source Mali driver
            "tegra",           // Alternative NVIDIA name
            "samsung",         // Samsung custom implementation
            "llvmpipe",        // Software rendering
            "swiftshader",     // Google's software renderer
            "android",         // Generic Android implementation
            "mesa"             // Mesa3D implementation
    };

    // Common vendor-specific variations
    public static final String[] GPU_VENDOR_VARIANTS = {
            // Qualcomm variants
            "qualcomm",
            "qcom",
            "qualcomm_adreno",

            // ARM variants
            "arm",
            "arm_mali",
            "mali-t",
            "mali-g",

            // PowerVR variants
            "img_powervr",
            "imagination",
            "powervr_rogue",

            // NVIDIA variants
            "nvidia_tegra",
            "geforce"
    };



    public static final String[] GPU_VULKAN_IMPLEMENTORS = {
            // Qualcomm
            "adreno",
            "qcom",
            "qualcomm",
            "qualcomm_adreno",

            // ARM
            "mali",
            "arm",
            "mali-g",         // New Mali GPUs
            "mali-t",         // Older Mali GPUs
            "arm_mali",
            "bifrost",        // Mali Bifrost architecture
            "valhall",        // Mali Valhall architecture

            // Samsung
            "samsung",

            // NVIDIA
            "nvidia",
            "tegra",

            // PowerVR
            "powervr",
            "imagination",
            "img",
            "powervr_rogue",

            // Intel
            "intel",
            "intel_graphics",

            // Software/Generic
            "llvmpipe",        // Software rendering
            "swiftshader",     // Google's software Vulkan implementation
            "lavapipe",        // Mesa software Vulkan implementation

            // Open Source Drivers
            "freedreno",       // Open source Adreno
            "turnip",          // Another open source Adreno
            "panfrost",        // Open source Mali
            "lima"             // Open source for older Mali
    };

    public static final String[] GPU_GLES_VERSION_CODES = {
            // Common versions
            "196610",      // OpenGL ES 3.2
            "196609",      // OpenGL ES 3.1
            "196608",      // OpenGL ES 3.0
            "131072",      // OpenGL ES 2.0
            "65536",       // OpenGL ES 1.1

            // Qualcomm Adreno specific
            "19660",       // Adreno 6xx series
            "19661",       // Adreno 650/660
            "19662",       // Adreno 680/690
            "19670",       // Adreno 7xx series

            // Mali specific
            "18500",       // Mali-G series
            "18510",       // Mali-G7x
            "18520",       // Mali-G8x
            "18530",       // Mali-G31/G52

            // PowerVR specific
            "19550",       // PowerVR Series9
            "19551",       // PowerVR Rogue
            "19555",       // PowerVR newer series

            // Common vendor neutral codes
            "458752",      // Generic OpenGL ES 2.0
            "458753",      // Generic OpenGL ES 3.0
            "458754",      // Generic OpenGL ES 3.1
            "458755"       // Generic OpenGL ES 3.2
    };



    public static final String[] GPU_GLES_VENDORS = {
            // Qualcomm
            "Qualcomm",
            "QUALCOMM",
            "Adreno (TM) ###",    // Where ### is GPU version
            "Qualcomm Technologies, Inc.",
            "QCOM",

            // ARM
            "ARM",
            "Mali",
            "Mali-T###",          // Where ### is GPU version
            "Mali-G###",
            "ARM Mali-###",

            // PowerVR
            "PowerVR",
            "Imagination",
            "IMG PowerVR",
            "PowerVR Rogue",
            "POWERVR",

            // NVIDIA
            "NVIDIA",
            "NVIDIA Corporation",
            "Tegra",

            // Intel
            "Intel",
            "Intel Inc.",
            "Intel Open Source Technology Center",

            // Software/Generic
            "Android",
            "Android Emulator",
            "Google",
            "SwiftShader",
            "Mesa/X.org",         // X can be version number
            "Gallium ##.#",       // ## can be version

            // Others
            "Broadcom",
            "Vivante Corporation",
            "Apple GPU",
            "Samsung",
            "llvmpipe",
            "virgl"
    };



    public static final String[] GPU_GLES_RENDERERS = {
            // Qualcomm Adreno
            "Adreno (TM) 200",
            "Adreno (TM) 305",
            "Adreno (TM) 320",
            "Adreno (TM) 330",
            "Adreno (TM) 405",
            "Adreno (TM) 418",
            "Adreno (TM) 420",
            "Adreno (TM) 430",
            "Adreno (TM) 505",
            "Adreno (TM) 506",
            "Adreno (TM) 508",
            "Adreno (TM) 509",
            "Adreno (TM) 512",
            "Adreno (TM) 530",
            "Adreno (TM) 540",
            "Adreno (TM) 610",
            "Adreno (TM) 612",
            "Adreno (TM) 616",
            "Adreno (TM) 618",
            "Adreno (TM) 620",
            "Adreno (TM) 630",
            "Adreno (TM) 640",
            "Adreno (TM) 642",
            "Adreno (TM) 650",
            "Adreno (TM) 660",
            "Adreno (TM) 730",
            "Adreno (TM) 740",

            // ARM Mali
            "Mali-200",
            "Mali-400 MP",
            "Mali-T604",
            "Mali-T628",
            "Mali-T720",
            "Mali-T760",
            "Mali-T820",
            "Mali-T830",
            "Mali-T860",
            "Mali-T880",
            "Mali-G31",
            "Mali-G51",
            "Mali-G52",
            "Mali-G57",
            "Mali-G68",
            "Mali-G71",
            "Mali-G72",
            "Mali-G76",
            "Mali-G77",
            "Mali-G78",
            "Mali-G88",
            "Mali-G610",
            "Mali-G710",

            // PowerVR
            "PowerVR SGX 530",
            "PowerVR SGX 540",
            "PowerVR SGX 544",
            "PowerVR Rogue G6200",
            "PowerVR Rogue GE8100",
            "PowerVR Rogue GE8300",
            "PowerVR Rogue GE8320",
            "PowerVR Series9446",

            // NVIDIA
            "NVIDIA Tegra",
            "Tegra K1",
            "Tegra X1",

            // Software Renderers
            "Android Emulator",
            "SwiftShader",
            "llvmpipe",
            "Gallium",
            "Mesa",
            "Virgl",

            // Generic
            "OpenGL ES Emulator",
            "Android GPU emulator",

            // Intel
            "Intel HD Graphics",
            "Intel(R) HD Graphics",

            // Apple (for reference)
            "Apple GPU",
            "Apple A-series GPU"
    };



    public static final String[] GPU_GLES_VERSION_STRINGS = {
            // Qualcomm Adreno
            "OpenGL ES 3.2 V@530.0 (GIT@0x9b22474, abc123)",
            "OpenGL ES 3.2 V@510.0 (GIT@0x8b6bf24, I2064b6553)",
            "OpenGL ES V@530.50 (GIT@af2a560751)",
            "OpenGL ES 3.2 V@0502.0 (GIT@0x9e11455)",
            "OpenGL ES-CM 1.1 V@123.0 (GIT@Idf45c40)",
            "OpenGL ES 3.2 Adreno (TM) 650.0 (GIT@xxxx)",
            "OpenGL ES 3.0 V@84.0 (GIT@Id2c5433)",

            // Mali
            "OpenGL ES 3.2 Mali-G78",
            "OpenGL ES 3.1 Mali-G52",
            "OpenGL ES 3.2 build 1.r9p0-01rel0",
            "OpenGL ES 3.1 Mali-T860",
            "OpenGL ES 3.2 Mali-G610 r1p0-02eac0",

            // PowerVR
            "OpenGL ES 3.2 build 1.9@5064661",
            "OpenGL ES 3.1 build 1.7@4912062",
            "OpenGL ES 2.0 build 2.1@4850556",

            // Generic Versions
            "OpenGL ES 2.0",
            "OpenGL ES 3.0",
            "OpenGL ES 3.1",
            "OpenGL ES 3.2",

            // Software/Emulator
            "OpenGL ES 3.0 SwiftShader (git-HASH)",
            "OpenGL ES 2.0 (ANGLE 2.1.0.dec065540d5f)",
            "OpenGL ES 3.0 (ANGLE 2.1.0.f85283f27167)",

            // Extended Version Info
            "OpenGL ES 3.2 V@0560.0 (GIT@a12b34c, I123456)",
            "OpenGL ES 3.2 V@0570.0 (GIT@def789, I789012)",
            "OpenGL ES-CM 1.1 V@145.0 (GIT@Iabcdef)",
            "OpenGL ES 3.2 build 2.r14p0-01rel0.123456",
            "OpenGL ES 3.2 V@400.0 (GIT@0x123456, Engineering)",
            "OpenGL ES 3.2 V@500.0 (GIT@0xabcdef, Release)"
    };

    public static String socBaseboardConfigName(RandomizerSessionContext session) {
        return RandomGenerator.nextString(5, 26);
    }

    //Check if null or not
    public static String socBasebandBoardRadioVersion(RandomizerSessionContext session) {
        return RandomGenerator.nextString(5, 26);
    }



    public static final String[] GPU_BOARD_CONFIG_NAMES = {
            // Qualcomm Common
            "cherokee",       // Common Snapdragon high-end
            "california",     // Newer Snapdragon platforms
            "waipio",        // Snapdragon 8 Gen 1
            "taro",          // Another SD 8 Gen 1 variant
            "lahaina",       // Snapdragon 888
            "kona",          // Snapdragon 865
            "msmnile",       // Snapdragon 855
            "sdm845",        // Snapdragon 845
            "bengal",        // Snapdragon 662/460

            // Qualcomm Mid/Low End
            "hawao",         // Mid-range platform
            "holi",          // Entry level platform
            "blair",         // Budget platform
            "khaje",         // Low-end platform

            // MediaTek
            "mt6893",        // Dimensity high-end
            "mt6885",        // Dimensity mid-range
            "mt6833",        // Dimensity low-end
            "mt6781",        // Helio series

            // Samsung
            "exynos2100",    // High end Exynos
            "exynos990",     // Previous gen
            "exynos850",     // Mid range

            // Others
            "gs101",         // Google Tensor
            "gs201",         // Google Tensor G2
            "piton",         // Various platforms
            "monaco",        // Various platforms
            "phoenix"        // Various platforms
    };
}
