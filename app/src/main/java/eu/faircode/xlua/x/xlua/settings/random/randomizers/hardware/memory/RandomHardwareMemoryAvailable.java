package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.memory;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionInt;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomHardwareMemoryAvailable extends RandomElement {
    public RandomHardwareMemoryAvailable() {
        super("Hardware Memory Available (GB)");
        putSettings(RandomizersCache.SETTING_HARDWARE_MEMORY_AVAILABLE);
        putOptions(RandomOptionInt.generateRange(1, 26));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}