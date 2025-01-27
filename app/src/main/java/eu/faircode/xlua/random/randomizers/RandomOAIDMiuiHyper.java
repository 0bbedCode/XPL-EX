package eu.faircode.xlua.random.randomizers;


//isMiuiOrHyperOs


import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomStringGenerator;
import eu.faircode.xlua.x.runtime.BuildInfo;

public class RandomOAIDMiuiHyper implements IRandomizerOld {
    @Override
    public boolean isSetting(String setting) { return getSettingName().equalsIgnoreCase(setting) && BuildInfo.isMiuiOrHyperOs(); }

    @Override
    public String getSettingName() { return "unique.open.anon.advertising.id"; }

    @Override
    public String getName() { return "OAID Miui/Hyper"; }

    @Override
    public String getID() { return "%oaid_miui_hyper%"; }

    @Override
    public String generateString() { return RandomStringGenerator.generateRandomHexString(16).toLowerCase(); }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return null; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}
