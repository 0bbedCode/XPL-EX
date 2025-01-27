package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.etc;

import eu.faircode.xlua.x.data.utils.DateUtils;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionInt;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomHardwareCameraCount extends RandomElement {
    public RandomHardwareCameraCount() {
        super("Hardware Camera Count");
        putSettings(RandomizersCache.SETTING_HARDWARE_CAMERA_COUNT);
        putOptions(RandomOptionInt.generateRange(0, 6));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}