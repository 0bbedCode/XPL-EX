package eu.faircode.xlua.x.hook.interceptors.hardware.cpu.random;

import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomCpuHardwareName extends RandomElement {
    public static IRandomizer create() { return new RandomCpuHardwareName(); }
    public RandomCpuHardwareName() {
        super("CPU Hardware Name");
        bindSetting("soc.board.manufacturer.id");
    }

    public static final Map<String, String> MANUFACTURERS_TO_SHORT = new HashMap<String, String>() {{
        // Major Mobile SoC Manufacturers
        put("Qualcomm", "qcom");
        put("MediaTek", "mtk");
        put("Samsung", "samsung");
        put("HiSilicon", "huawei");
        put("Apple", "apple");
        put("Unisoc", "sprd");
        put("Google", "google");
        put("Spreadtrum", "sprd");

        // Marketing Names mapped to same short names
        put("Snapdragon", "qcom");
        put("Dimensity", "mtk");
        put("Helio", "mtk");
        put("Exynos", "samsung");
        put("Kirin", "huawei");
        put("Tensor", "google");
        put("Bionic", "apple");

        // Less Common Mobile
        put("RockChip", "rk");
        put("Allwinner", "aw");
        put("Amlogic", "aml");
        put("NovaTek", "ntk");
        put("JLQ", "jlq");

        // Desktop/Laptop
        put("Intel", "intel");
        put("AMD", "amd");

        // Historic/Legacy
        put("Texas Instruments", "ti");
        put("NVIDIA", "nvidia");
        put("Broadcom", "brcm");
        put("Marvell", "mrvl");
        put("VIA", "via");
    }};

    @Override
    public String generateString() {
        int index = RandomGenerator.nextInt(0, MANUFACTURERS_TO_SHORT.size());
        return ListUtil.copyToList(MANUFACTURERS_TO_SHORT.keySet()).get(index);
    }
}
