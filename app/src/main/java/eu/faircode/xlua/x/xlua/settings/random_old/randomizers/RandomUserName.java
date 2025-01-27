package eu.faircode.xlua.x.xlua.settings.random_old.randomizers;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

//account.user.name
public class RandomUserName extends RandomElement {
    public static final String[] NAMES = new String[] {
            "random",
            "god",
            "linux",
            "debian",
            "current",
            "user",
            "0",
            "1"
    };

    public static RandomUserName create() { return new RandomUserName(); }
    public RandomUserName() {
        super("User Profile Name (local)");
        bindSetting("account.user.name");
    }

    @Override
    public String generateString() {
        String name = NAMES[RandomGenerator.nextInt(0, NAMES.length)];
        return name.equalsIgnoreCase("random") ? RandomGenerator.nextString(RandomGenerator.nextInt(6, 14)) : name;
    }
}