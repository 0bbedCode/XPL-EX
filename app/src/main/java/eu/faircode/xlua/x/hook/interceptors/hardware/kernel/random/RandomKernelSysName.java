package eu.faircode.xlua.x.hook.interceptors.hardware.kernel.random;

import eu.faircode.xlua.utilities.RandomStringGenerator;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomKernelSysName extends RandomElement {
    public static IRandomizer create() { return new RandomKernelSysName(); }
    public static final String[] SYS_NAMES = new String[] {
            "Linux",
            "Unix",
            "BSD",
            "XNU",
            "random" };

    public RandomKernelSysName() {
        super("Kernel Sys Name (Linux, Unix...)");
        bindSetting("android.kernel.sys.name");
    }

    @Override
    public String generateString() {
        String nodeName = SYS_NAMES[RandomGenerator.nextInt(0, SYS_NAMES.length)];
        if ("random".equals(nodeName))
            return RandomStringGenerator.generateRandomAlphanumericString(RandomGenerator.nextInt(5, 15), RandomStringGenerator.LOWER_LETTERS);
        return nodeName;
    }
}