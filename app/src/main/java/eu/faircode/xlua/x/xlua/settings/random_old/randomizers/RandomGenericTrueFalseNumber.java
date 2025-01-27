package eu.faircode.xlua.x.xlua.settings.random_old.randomizers;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomGenericTrueFalseNumber extends RandomElement {
    public static IRandomizer create() { return new RandomGenericTrueFalseNumber(); }
    public RandomGenericTrueFalseNumber() {
        super("Random True/False Numeric (0 = False, 1 = True)");
        bindSettings(
                "android.rom.adb.secure",
                "android.rom.secure",
                "android.rom.debuggable",
                "android.rom.allow.mock",
                "android.rom.flash.locked",
                "hardware.efuse");
    }

    @Override
    public String generateString() { return RandomGenerator.nextBoolean() ? "1" : "0"; }
}