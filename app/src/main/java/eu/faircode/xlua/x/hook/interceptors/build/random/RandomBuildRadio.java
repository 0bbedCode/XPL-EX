package eu.faircode.xlua.x.hook.interceptors.build.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomBuildRadio extends RandomElement {
    public static IRandomizer create() { return new RandomBuildRadio(); }
    public RandomBuildRadio() {
        super("Build Radio Version");
        bindSetting("soc.baseband.board.radio.version");
    }

    @Override
    public String generateString() { return String.valueOf(RandomGenerator.nextInt(100, 999)); }
}