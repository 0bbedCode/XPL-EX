package eu.faircode.xlua.x.hook.interceptors.hardware.cpu.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomAbiList64 extends RandomElement {
    public static IRandomizer create() { return new RandomAbiList64(); }
    public RandomAbiList64() {
        super("CPU Abi List 64");
        bindSetting("cpu.abilist64");
        bindParent("cpu.abi");
    }

    @Override
    public String generateString() {
        boolean is64 = RandomGenerator.nextBoolean();
        boolean isBig = RandomGenerator.nextBoolean();
        //return  !is64 ? "" :
        //        isBig ?
        //                "x86_64" :
        //                Str.joinList(RandomCpuAbi.ABI_ARM_64, ",");
        return null;
    }
}
