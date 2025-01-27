package eu.faircode.xlua.x.hook.interceptors.hardware.camera;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomCameraCount extends RandomElement {
    public static IRandomizer create() { return new RandomCameraCount(); }
    public RandomCameraCount() {
        super("Camera Count");
        bindSetting("hardware.camera.count");
    }

    @Override
    public String generateString() { return String.valueOf(RandomGenerator.nextInt(0, 4)); }
}