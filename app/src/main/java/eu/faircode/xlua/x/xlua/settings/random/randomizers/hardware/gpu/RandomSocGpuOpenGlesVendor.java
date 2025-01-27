package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.ran.RanGpuUtils;

public class RandomSocGpuOpenGlesVendor extends RandomElement {
    public RandomSocGpuOpenGlesVendor() {
        super("Hardware GPU OpenGLES Vendor");
        putSettings(RandomizersCache.SETTING_SOC_GPU_OPEN_GLES_VENDOR);
        putOptions(RandomOptionString.generate(RanGpuUtils.GPU_GLES_VENDORS));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}