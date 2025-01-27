package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.SoC;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanHwdUtils;

public class RandomSocCpuInstructionSet32 extends RandomElement {
    public RandomSocCpuInstructionSet32() {
        //PROCESSOR_ARCHITECTURES
        super("Hardware CPU Instruction Sets 32");
        putSettings(RandomizersCache.SETTING_SOC_CPU_INSTRUCTION_SET_32);
        putOptions(RandomOptionString.generate(RanHwdUtils.CPU_INSTRUCTION_SET_32));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}