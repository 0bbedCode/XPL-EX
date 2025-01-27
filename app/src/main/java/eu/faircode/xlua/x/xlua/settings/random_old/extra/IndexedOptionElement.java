package eu.faircode.xlua.x.xlua.settings.random_old.extra;

import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class IndexedOptionElement extends RandomElement {
    public static IRandomizer create(String option, int index) { return new IndexedOptionElement(option, index); }

    private int mIndex = 0;
    public IndexedOptionElement(String option, int index) {
        super(option + " (" + index + ")");
        mIndex = index;
    }

    @Override
    public String generateString() { return String.valueOf(mIndex); }
}
