package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.etc;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionNullElement;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomHardwareEfuse extends RandomElement {
    public RandomHardwareEfuse() {
        super("Hardware ETC E-Fuse");
        putSettings(RandomizersCache.SETTING_HARDWARE_EFUSE);
        putOptions(RandomOptionNullElement.create(),
                RandomOptionString.create("Green Safe", "1"),
                RandomOptionString.create("Red Blown", "0"));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}