package eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.ran.RanGpuUtils;

public class RandomSocBluetoothBoardConfigName extends RandomElement {
    public RandomSocBluetoothBoardConfigName() {
        super("Hardware Bluetooth Board Config Name");
        putSettings(RandomizersCache.SETTING_SOC_BLUETOOTH_BOARD_CONFIG_NAME);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushValue(context.stack.pop(), RanGpuUtils.socBasebandBoardRadioVersion(context));
    }
}