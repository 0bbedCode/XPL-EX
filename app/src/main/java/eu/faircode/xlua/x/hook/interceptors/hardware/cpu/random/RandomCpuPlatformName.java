package eu.faircode.xlua.x.hook.interceptors.hardware.cpu.random;

import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomCpuPlatformName extends RandomElement {
    public static IRandomizer create() { return new RandomCpuPlatformName(); }
    public static final Map<String, String> PLATFORM_TO_CHIPSET = new HashMap<String, String>() {{
        // Qualcomm Snapdragon Platforms
        put("msmnile", "SD 855/855+/860");
        put("kona", "SD 865/865+/870");
        put("lahaina", "SD 888/888+");
        put("taro", "SD 8 Gen 1");
        put("kalama", "SD 8 Gen 2");
        put("pineapple", "SD 8 Gen 3");
        put("bengal", "SD 460");
        put("SM6125", "SD 665");
        put("trinket", "SD 665");
        put("lito", "SD 765G/768G");
        put("atoll", "SD 720G");
        put("sm6150", "SD 675");
        put("sdm710", "SD 710");
        put("sdm845", "SD 845");
        put("msm8998", "SD 835");
        put("msm8996", "SD 820/821");
        put("msm8956", "SD 650/652");
        put("msm8953", "SD 625");
        put("msm8937", "SD 430");
        put("msm8917", "SD 425");

        // MediaTek Platforms
        put("mt6833", "Dimensity 700");
        put("mt6853", "Dimensity 720");
        put("mt6873", "Dimensity 800U");
        put("mt6885", "Dimensity 1000");
        put("mt6889", "Dimensity 1000+");
        put("mt6893", "Dimensity 1200");
        put("mt6895", "Dimensity 8100");
        put("mt6983", "Dimensity 9000");
        put("mt6879", "Dimensity 8000");
        put("mt6768", "Helio G85/G88");
        put("mt6785", "Helio G90T");
        put("mt6771", "Helio P60/P70");
        put("mt6765", "Helio G35/P35");
        put("mt6762", "Helio P22/G25");

        // Samsung Exynos Platforms
        put("universal9825", "Exynos 9825");
        put("universal9820", "Exynos 9820");
        put("universal9810", "Exynos 9810");
        put("universal9611", "Exynos 9611");
        put("universal7904", "Exynos 7904");
        put("universal7884", "Exynos 7884");
        put("universal7880", "Exynos 7880");
        put("universal2100", "Exynos 2100");
        put("universal2200", "Exynos 2200");

                // Google Tensor
                put("gs101", "Tensor G1");
                put("gs201", "Tensor G2");
                put("gs301", "Tensor G3");

                // Unisoc
                put("ums512", "T612");
                put("ums510", "T610");
                put("sp9863a", "SC9863A");

                // HiSilicon
                put("kirin990", "Kirin 990");
                put("kirin985", "Kirin 985");
                put("kirin980", "Kirin 980");
                put("kirin970", "Kirin 970");
    }};


    public RandomCpuPlatformName() {
        super("CPU Platform Name");
        bindSetting("soc.board.config.code.name");
    }

    @Override
    public String generateString() {
        int index = RandomGenerator.nextInt(0, PLATFORM_TO_CHIPSET.size());
        return ListUtil.copyToList(PLATFORM_TO_CHIPSET.keySet()).get(index);
    }
}
