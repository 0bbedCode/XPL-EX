package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.etc;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanHwdUtils;

public class RandomHardwareNfcControllerInterface extends RandomElement {
    public RandomHardwareNfcControllerInterface() {
        super("Hardware ETC NFC CTRL Interface");
        putSettings(RandomizersCache.SETTING_HARDWARE_NFC_CONTROLLER_INTERFACE);
        putOptions(RandomOptionString.generate(RanHwdUtils.NFC_CONTROLLER_INTERFACES));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}