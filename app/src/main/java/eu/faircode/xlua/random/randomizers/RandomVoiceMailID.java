
package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomStringGenerator;

public class RandomVoiceMailID implements IRandomizerOld {
    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() {  return "unique.gsm.voicemail.id"; }

    @Override
    public String getName() {
        return "Voicemail Alpha Tag";
    }

    @Override
    public String getID() {
        return "%voicemail_alpha_tag%";
    }

    @Override
    public String generateString() { return RandomStringGenerator.generateRandomAlphanumericString(8, RandomStringGenerator.LOWER_LETTERS); }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return null; }

    @NonNull
    @Override
    public String toString() {
        return getName();
    }
}

