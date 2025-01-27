package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.ran.RanGpuUtils;

//GPU_GLES_VENDORS

public class RandomSocGpuOpenGlesVersionEncoded extends RandomElement {
    public RandomSocGpuOpenGlesVersionEncoded() {
        super("Hardware GPU OpenGLES Version Code");
        putSettings(RandomizersCache.SETTING_SOC_GPU_OPEN_GLES_VERSION_ENCODED);
        putOptions(RandomOptionString.generate(RanGpuUtils.GPU_GLES_VERSION_CODES));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}