package eu.faircode.xlua.x.hook.interceptors.hardware.cpu.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomCpuArch extends RandomElement {
    public static IRandomizer create() { return new RandomCpuArch(); }
    public RandomCpuArch() {
        super("CPU Arch");
        bindSetting("cpu.arch");
        bindParent("cpu.abi");
    }

    @Override
    public String generateString() {
        boolean is64 = RandomGenerator.nextBoolean();
        boolean isBig = RandomGenerator.nextBoolean();
        return isBig ? is64 ? "amd64" : "i686" : is64 ? "aarch64" : "armv71";
    }
}
