package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.SoC;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionInt;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomSocCpuProcessorCount extends RandomElement {
    public RandomSocCpuProcessorCount() {
        super("Hardware Processor Count");
        putSettings(RandomizersCache.SETTING_SOC_CPU_PROCESSOR_COUNT);
        putOptions(RandomOptionInt.generateRange(0, 6));    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}