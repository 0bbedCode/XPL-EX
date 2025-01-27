package eu.faircode.xlua.x.hook.interceptors.hardware.cpu.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomCpuProcessorCount extends RandomElement {
    public static IRandomizer create() { return new RandomCpuProcessorCount(); }
    public RandomCpuProcessorCount() {
        super("CPU Processor Count");
        bindSetting("cpu.processor.count");
    }

    @Override
    public String generateString() { return String.valueOf(RandomGenerator.nextInt(3, 22)); }
}
