package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.SoC;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanHwdUtils;

public class RandomSocCpuAbiList extends RandomElement {
    public RandomSocCpuAbiList() {
        super("Hardware CPU ABI List (32/64)");
        putSettings(RandomizersCache.SETTING_SOC_CPU_ABI_LIST);
        putOptions(RandomOptionString.generate(RanHwdUtils.CPU_ABI_LIST));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}