package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;


import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomStringGenerator;
import eu.faircode.xlua.utilities.RandomUtil;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomBuildUser implements IRandomizerOld {
    private static final String[] DEFAULT_BUILD_USERS = new String[] {
            "jenkins",
            "buildbot",
            "android-build",
            "ido",
            "god",
            "random"
    };

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() {  return "android.build.user"; }

    @Override
    public String getName() {
        return "Build User";
    }

    @Override
    public String getID() {
        return "%build_user%";
    }

    @Override
    public String generateString() {
        String user = DEFAULT_BUILD_USERS[RandomGenerator.nextInt(0, DEFAULT_BUILD_USERS.length)];
        if (user.equals("random"))
            return RandomStringGenerator.generateRandomLetterString(RandomUtil.getInt(3, 10), RandomStringGenerator.LOWER_LETTERS);
        return user;
    }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return null; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}
