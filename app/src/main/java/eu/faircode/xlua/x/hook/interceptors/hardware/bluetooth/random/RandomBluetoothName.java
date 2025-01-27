package eu.faircode.xlua.x.hook.interceptors.hardware.bluetooth.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomBluetoothName extends RandomElement {
    public static IRandomizer create() { return new RandomBluetoothName(); }
    public RandomBluetoothName() {
        super("Bluetooth Name");
        bindSetting("bluetooth.name");
    }

    @Override
    public String generateString() { return RandomGenerator.generateRandomAlphanumericString(RandomGenerator.nextInt(6, 45)); }
}
