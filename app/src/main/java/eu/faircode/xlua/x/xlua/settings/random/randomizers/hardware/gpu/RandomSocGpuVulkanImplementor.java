package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.ran.RanGpuUtils;

public class RandomSocGpuVulkanImplementor extends RandomElement {
    public RandomSocGpuVulkanImplementor() {
        super("Hardware GPU Vulkan Implementor");
        putSettings(RandomizersCache.SETTING_SOC_GPU_VULKAN_IMPLEMENTOR);
        putOptions(RandomOptionString.generate(RanGpuUtils.GPU_VULKAN_IMPLEMENTORS));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}