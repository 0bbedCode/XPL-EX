package eu.faircode.xlua.x.xlua.settings.random_old.extra;

import eu.faircode.xlua.x.hook.interceptors.build.random.RandomBaseOs;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class OptionFiller extends RandomElement {
    public static final IRandomizer INSTANCE = create();
    public static IRandomizer create() { return new RandomBaseOs(); }

    public OptionFiller() { super("Select Option"); }

    //Something null ?
    @Override
    public String generateString() { return ""; }
}
