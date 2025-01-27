package eu.faircode.xlua.x.hook.interceptors.zone.random;

import eu.faircode.xlua.utilities.RandomUtil;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomDateFour extends RandomElement {
    public static IRandomizer create() { return new RandomDateFour(); }
    private static final String FORMAT = "%s-%s-%s %s:%s:%s";

    public RandomDateFour() {
        super("Random Date Unix TimeStamp");
        bindSettings("android.build.date.utc", "random.date.epoch");
    }

    @Override
    public String generateString() {
        return String.valueOf(RandomUtil.convertStringDateToEpoch(
                String.format(FORMAT,
                        RandomUtil.getIntEnsureFormat(1, 30),
                        RandomUtil.getMonthNumberFormatted(),
                        RandomUtil.getInt(1999, 2030),
                        RandomUtil.getIntEnsureFormat(1, 24),
                        RandomUtil.getIntEnsureFormat(1, 60),
                        RandomUtil.getIntEnsureFormat(1, 60)), RandomUtil.DD_MM_YYYY__HHMMSS));
    }
}
