package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.etc;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanHwdUtils;

public class RandomHardwareGpsModelName extends RandomElement {
    public RandomHardwareGpsModelName() {
        super("Hardware ETC GPS Model Name");
        putSettings(RandomizersCache.SETTING_HARDWARE_GPS_MODEL_NAME);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushValue(context.stack.pop(), RanHwdUtils.gpsModelName(context));
    }
}