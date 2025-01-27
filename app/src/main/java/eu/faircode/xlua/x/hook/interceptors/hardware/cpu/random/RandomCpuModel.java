package eu.faircode.xlua.x.hook.interceptors.hardware.cpu.random;

import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomCpuModel extends RandomElement {
    public static IRandomizer create() { return new RandomCpuModel(); }
    public static final Map<String, String> SOC_TO_CHIPSET = new HashMap<String, String>() {{
        // Qualcomm Snapdragon (SM/QCM/SDM Series)
        put("SM8650", "Snapdragon 8 Gen 4");
        put("SM8550", "Snapdragon 8 Gen 2");
        put("SM8475", "Snapdragon 8+ Gen 1");
        put("SM8450", "Snapdragon 8 Gen 1");
        put("SM8350", "Snapdragon 888/888+");
        put("SM8250", "Snapdragon 865/865+/870");
        put("SM8150", "Snapdragon 855/855+");
        put("SM7325", "Snapdragon 778G");
        put("SM7250", "Snapdragon 765/765G/768G");
        put("SM7225", "Snapdragon 750G");
        put("SM7125", "Snapdragon 720G");
        put("SM7150", "Snapdragon 730/730G/732G");
        put("SM6375", "Snapdragon 695");
        put("SM6350", "Snapdragon 690");
        put("SM6325", "Snapdragon 680");
        put("SM6115", "Snapdragon 662");
        put("SM6125", "Snapdragon 665");
        put("SM4375", "Snapdragon 480");
        put("SM4350", "Snapdragon 480");

        // Qualcomm Legacy (MSM Series)
        put("MSM8998", "Snapdragon 835");
                put("MSM8996", "Snapdragon 820/821");
                put("MSM8956", "Snapdragon 650/652");
                put("MSM8953", "Snapdragon 625");
                put("MSM8937", "Snapdragon 430");
                put("MSM8917", "Snapdragon 425");

                // MediaTek Dimensity
                put("MT6983", "Dimensity 9000");
                put("MT6895", "Dimensity 8100");
                put("MT6893", "Dimensity 1200");
                put("MT6889", "Dimensity 1000+");
                put("MT6885", "Dimensity 1000");
                put("MT6873", "Dimensity 800");
                put("MT6853", "Dimensity 720");
                put("MT6833", "Dimensity 700");

                // MediaTek Helio
                put("MT6895", "Helio G95");
                put("MT6785", "Helio G90T");
                put("MT6768", "Helio G85/G88");
                put("MT6771", "Helio P60/P70");
                put("MT6765", "Helio P35/G35");
                put("MT6762", "Helio P22/G25");

                // Samsung Exynos
                put("S5E9925", "Exynos 2200");
                put("S5E9820", "Exynos 9820");
                put("S5E9810", "Exynos 9810");
                put("S5E9609", "Exynos 850");
                put("S5E8825", "Exynos 1080");
                put("S5E8535", "Exynos 850");

                // Google Tensor
                put("GS101", "Tensor G1");
                put("GS201", "Tensor G2");
                put("GS301", "Tensor G3");

                // HiSilicon Kirin
                put("KIRIN9000", "Kirin 9000");
                put("KIRIN990", "Kirin 990");
                put("KIRIN985", "Kirin 985");
                put("KIRIN980", "Kirin 980");
                put("KIRIN970", "Kirin 970");
    }};


    public RandomCpuModel() {
        super("CPU SOC Model");
        bindSetting("soc.board.model");
    }

    @Override
    public String generateString() {
        int index = RandomGenerator.nextInt(0, SOC_TO_CHIPSET.size());
        return ListUtil.copyToList(SOC_TO_CHIPSET.keySet()).get(index);
    }
}