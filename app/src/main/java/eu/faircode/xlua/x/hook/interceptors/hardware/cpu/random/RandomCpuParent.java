package eu.faircode.xlua.x.hook.interceptors.hardware.cpu.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.deprecated.SettingsContextOld;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.ILinkParent;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomCpuParent extends RandomElement implements ILinkParent {
    public static IRandomizer create() { return new RandomCpuParent(); }
    public RandomCpuParent() {
        super("CPU Control Parent");
        bindSetting("cpu.info.file");
        setIsParentControlOverride(true);
    }

    @Override
    public String generateString() {
        boolean is64 = RandomGenerator.nextBoolean();
        boolean isBig = RandomGenerator.nextBoolean();
        return isBig ? is64 ? "amd64" : "i686" : is64 ? "aarch64" : "armv71";
    }

    @Override
    public String randomize(SettingsContextOld context) {
        //return super.randomize(context);
        return null;
    }
}
