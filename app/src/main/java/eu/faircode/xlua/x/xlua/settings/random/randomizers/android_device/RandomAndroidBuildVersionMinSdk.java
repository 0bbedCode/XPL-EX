package eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device;

import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.runtime.BuildInfo;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionInt;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomAndroidBuildVersionMinSdk  extends RandomElement {
    public RandomAndroidBuildVersionMinSdk() {
        super("Android Build Version Min SDK");
        putSettings(RandomizersCache.SETTING_ANDROID_BUILD_VERSION_MIN_SDK);
        putOptions(RandomOptionInt.generateOptions(true, ArrayUtils.generate(6, 15, (v) -> BuildInfo.getApiLevelFromVersion((double) v))));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //ToDO make more advance
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}