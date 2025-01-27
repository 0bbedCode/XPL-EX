package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.SoC;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanHwdUtils;

public class RandomSocCpuAbiList64 extends RandomElement {
    public RandomSocCpuAbiList64() {
        super("Hardware CPU ABI List 64");
        putSettings(RandomizersCache.SETTING_SOC_CPU_ABI_LIST_64);
        putOptions(RandomOptionString.generate(RanHwdUtils.CPU_ABI_LIST_64));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}