package eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionInt;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanAndUtils;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanDevUtils;

public class RandomAndroidBuildVersion extends RandomElement {
    public RandomAndroidBuildVersion() {
        super("Android Build Version");
        putSettings(RandomizersCache.SETTING_ANDROID_BUILD_VERSION);
        putOptions(RandomOptionInt.generateRange(6, 15, true));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}