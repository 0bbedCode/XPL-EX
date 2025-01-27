package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.ran.RanGpuUtils;

public class RandomSocGpuEglImplementor extends RandomElement {
    public RandomSocGpuEglImplementor() {
        super("Hardware GPU EGL Implementor");
        putSettings(RandomizersCache.SETTING_SOC_GPU_EGL_IMPLEMENTOR);
        putOptions(RandomOptionString.generate(RanGpuUtils.GPU_EGL_IMPLEMENTORS));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}