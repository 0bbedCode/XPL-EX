package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.etc;

import eu.faircode.xlua.x.data.utils.DateUtils;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionInt;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanHwdUtils;

public class RandomHardwareGpsModelYear extends RandomElement {
    public RandomHardwareGpsModelYear() {
        super("Hardware ETC GPS Model Year");
        putSettings(RandomizersCache.SETTING_HARDWARE_GPS_MODEL_YEAR);
        putOptions(RandomOptionInt.generateRange(2016, DateUtils.getCurrentYear()));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}