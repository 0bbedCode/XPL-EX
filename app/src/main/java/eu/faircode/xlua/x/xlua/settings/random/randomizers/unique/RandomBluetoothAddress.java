package eu.faircode.xlua.x.xlua.settings.random.randomizers.unique;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.ran.RanGpuUtils;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanUnqUtils;

public class RandomBluetoothAddress extends RandomElement {
    public RandomBluetoothAddress() {
        super("Bluetooth Address");
        putSettings(RandomizersCache.SETTING_UNIQUE_BLUETOOTH);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushValue(context.stack.pop(), RanUnqUtils.bluetoothAddress(context));
    }
}