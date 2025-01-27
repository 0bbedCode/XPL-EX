package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.etc;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanHwdUtils;

public class RandomHardwareCameraApp extends RandomElement {
    public RandomHardwareCameraApp() {
        super("Hardware Camera Package Name");
        putSettings(RandomizersCache.SETTING_HARDWARE_CAMERA_APP);
        putOptions(RandomOptionString.generate(RanHwdUtils.CAMERA_PACKAGE_NAMES));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}