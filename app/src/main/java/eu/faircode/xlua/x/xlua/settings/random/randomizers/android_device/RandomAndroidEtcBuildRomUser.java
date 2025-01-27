package eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanAndUtils;

/*
    ToDO: the "random" element can get its "generator" for its "random" value within this Class
 */
public class RandomAndroidEtcBuildRomUser extends RandomElement {
    public RandomAndroidEtcBuildRomUser() {
        super("Android ETC ROM User");
        putSettings(RandomizersCache.SETTING_ANDROID_ETC_BUILD_ROM_USER);
        putOptions(RandomOptionString.generate(RanAndUtils.DEFAULT_BUILD_USERS));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        ///context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}