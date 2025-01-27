package eu.faircode.xlua.x.hook.interceptors.hardware.kernel.random;

import eu.faircode.xlua.utilities.RandomUtil;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomKernelRelease extends RandomElement {
    public static IRandomizer create() { return new RandomKernelRelease(); }
    private static final String FORMAT = "%s.%s.%s";

    public RandomKernelRelease() {
        super("Kernel Release");
        bindSetting("android.kernel.release");
    }

    @Override
    public String generateString() {
        return String.format(FORMAT,
                RandomUtil.getInt(1, 5),
                RandomUtil.getInt(10, 99),
                RandomUtil.getInt(100, 999));
    }
}