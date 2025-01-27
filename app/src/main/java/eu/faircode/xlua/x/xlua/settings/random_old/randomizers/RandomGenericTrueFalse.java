package eu.faircode.xlua.x.xlua.settings.random_old.randomizers;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomGenericTrueFalse extends RandomElement {
    public static IRandomizer create() { return new RandomGenericTrueFalse(); }
    public RandomGenericTrueFalse() {
        super("Random True/False");
        bindSettings(
                "android.rom.root.image.bool",
                "android.system.supports.ab.bool",
                "android.system.treble.enabled.bool");
    }

    @Override
    public boolean containsSetting(String settingName) { return super.containsSetting(settingName) || (settingName != null && settingName.toLowerCase().endsWith("bool")); }

    @Override
    public String generateString() { return RandomGenerator.nextBoolean() ? "True" : "False"; }
}