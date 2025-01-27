package eu.faircode.xlua.x.hook.interceptors.hardware.cpu.random;

import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomCpuManufacturer extends RandomElement {
    public static IRandomizer create() { return new RandomCpuManufacturer(); }
    public RandomCpuManufacturer() {
        super("CPU Manufacturer Name");
        bindSetting("cpu.soc.manufacturer");
    }

    @Override
    public String generateString() {
        int index = RandomGenerator.nextInt(0, RandomCpuHardwareName.MANUFACTURERS_TO_SHORT.size());
        return ListUtil.copyToList( RandomCpuHardwareName.MANUFACTURERS_TO_SHORT.values()).get(index);
    }
}
