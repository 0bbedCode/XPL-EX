package eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanAndUtils;

public class RandomAndroidEtcBuildRomVariant  extends RandomElement {
    public RandomAndroidEtcBuildRomVariant() {
        super("Android ETC ROM Variant");
        putSettings(RandomizersCache.SETTING_ANDROID_ETC_BUILD_ROM_VARIANT);
        putOptions(RandomOptionString.generate(RanAndUtils.DEFAULT_BUILD_TAGS));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}