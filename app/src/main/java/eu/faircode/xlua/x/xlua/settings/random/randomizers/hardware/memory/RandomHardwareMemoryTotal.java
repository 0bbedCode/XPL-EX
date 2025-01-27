package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.memory;

import eu.faircode.xlua.x.data.utils.DateUtils;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionInt;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomHardwareMemoryTotal extends RandomElement {
    public RandomHardwareMemoryTotal() {
        super("Hardware Memory Total (GB)");
        putSettings(RandomizersCache.SETTING_HARDWARE_MEMORY_TOTAL);
        putOptions(RandomOptionInt.generateRange(1, 26));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}