package eu.faircode.xlua.x.xlua.settings.random_old.extra;

import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class OptionElement extends RandomElement {
    public static IRandomizer create(String option) { return new OptionElement(option); }
    public OptionElement(String option) {
        super(option);
    }

    @Override
    public String generateString() { return getDisplayName(); }
}
