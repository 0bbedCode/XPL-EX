package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.etc;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanHwdUtils;

public class RandomHardwareNfcKind extends RandomElement {
    public RandomHardwareNfcKind() {
        super("Hardware ETC NFC Chip Kind");
        putSettings(RandomizersCache.SETTING_HARDWARE_NFC_KIND);
        putOptions(RandomOptionString.generate(RanHwdUtils.NFC_CONTROLLERS));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}