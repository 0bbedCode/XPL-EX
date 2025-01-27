package eu.faircode.xlua.x.hook.interceptors.hardware.cpu.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomAbiList extends RandomElement {
    public static IRandomizer create() { return new RandomAbiList(); }
    public RandomAbiList() {
        super("CPU Abi List");
        bindSetting("cpu.abilist");
        bindParent("cpu.abi");
    }

    @Override
    public String generateString() {
        boolean is64 = RandomGenerator.nextBoolean();
        boolean isBig = RandomGenerator.nextBoolean();
        //return !isBig ? Str.joinList(ListUtil.combine(RandomCpuAbi.ABI_ARM_64, RandomCpuAbi.ABI_ARM, is64, is64), ",") :
        //        is64 ? "x86_64,x86" : "x86";
        return null;
    }
}
