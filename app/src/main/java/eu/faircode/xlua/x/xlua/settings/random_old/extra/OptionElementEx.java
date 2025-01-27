package eu.faircode.xlua.x.xlua.settings.random_old.extra;

import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class OptionElementEx extends RandomElement {
    private final String mValue;
    public static IRandomizer create(String option, String value) { return new OptionElementEx(option, value); }
    public OptionElementEx(String option, String value) {
        super(option);
        this.mValue = value;
    }

    @Override
    public String generateString() { return mValue; }
}
