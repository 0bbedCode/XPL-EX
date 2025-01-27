package eu.faircode.xlua.x.hook.interceptors.hardware.cpu.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.hook.interceptors.hardware.cpu.CpuArchUtils;
import eu.faircode.xlua.x.xlua.settings.deprecated.SettingsContextOld;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.ILinkParent;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random_old.extra.OptionElementEx;

public class RandomCpuAbi extends RandomElement implements ILinkParent  {
    public static IRandomizer create() { return new RandomCpuAbi(); }
    public RandomCpuAbi() {
        super("CPU Abi");
        bindSetting("cpu.abi");
        setIsParentControlOverride(true);
        bindOptions(
                OptionElementEx.create("arm64", "arm64-v8a"),
                OptionElementEx.create("arm", "armeabi-v7a"),
                OptionElementEx.create("x64/64 Bit", "x86_64"),
                OptionElementEx.create("x86/32 Bit", "x86"));
    }

    @Override
    public String generateString() { return RandomGenerator.nextElement(CpuArchUtils.ABI_ALL_COMMON);  }

    @Override
    public String randomize(SettingsContextOld context) {
        /*SettingRandomContextOld parent = getCurrentSettingContext(context);
        String value = null;
        if(parent != null) {
            boolean needsRandomization = !parent.hasRandomized();
            value = parent.getValue(CpuArchUtils.ABI_ALL_COMMON, needsRandomization, context.ignoreDisabled());
            if(needsRandomization) {
                context.putSavedSettingValue("cpu.abilist", Str.joinList(CpuArchUtils.getAbiList(value)));
                context.putSavedSettingValue("cpu.abilist32", Str.joinList(CpuArchUtils.getAbiList32(value)));
                context.putSavedSettingValue("cpu.abilist64", Str.joinList(CpuArchUtils.getAbiList64(value)));
                context.putSavedSettingValue("cpu.arch", CpuArchUtils.getInstructionSetArchitecture(value));
            }
        }

        return value == null ? generateString() : value;*/
        return null;
    }
}
