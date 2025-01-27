package eu.faircode.xlua.x.hook.interceptors.hardware.kernel.random;


import eu.faircode.xlua.x.hook.interceptors.zone.random.RandomDateZero;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomKernelVersion extends RandomElement {
    public static IRandomizer create() { return new RandomKernelVersion(); }
    public RandomKernelVersion() {
        super("Kernel Version");
        bindSetting("android.kernel.version");
    }

    @Override
    public String generateString() { return "SMP PREEMPT " + RandomDateZero.create().generateString(); }
}