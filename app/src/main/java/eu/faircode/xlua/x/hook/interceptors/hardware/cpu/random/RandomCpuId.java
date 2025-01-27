package eu.faircode.xlua.x.hook.interceptors.hardware.cpu.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomCpuId extends RandomElement {
    public static IRandomizer create() { return new RandomCpuId(); }
    public RandomCpuId() {
        super("CPU ID");
        bindSetting("cpu.cpuid");
    }

    @Override
    public String generateString() { return "0x" + RandomGenerator.generateRandomNumberString(RandomGenerator.nextInt(7, 18)); }
}
