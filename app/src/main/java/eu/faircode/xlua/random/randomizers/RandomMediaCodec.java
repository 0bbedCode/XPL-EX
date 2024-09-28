package eu.faircode.xlua.random.randomizers;

import java.util.concurrent.ThreadLocalRandom;

public class RandomMediaCodec {
    private static final String[] PREFIXES = {"OMX.", "c2."};
    private static final String[] MANUFACTURERS = {"google", "qcom", "Exynos", "MTK", "Nvidia", "android"};
    private static final String[] CODEC_TYPES = {"h264", "avc", "hevc", "vp8", "vp9", "mpeg4", "aac"};
    private static final String[] FUNCTIONS = {"decoder", "encoder"};

    public static String generateName() {
        String prefix = getRandomElement(PREFIXES);
        String manufacturer = getRandomElement(MANUFACTURERS);
        String codecType = getRandomElement(CODEC_TYPES);
        String function = getRandomElement(FUNCTIONS);
        return String.format("%s%s.%s.%s", prefix, manufacturer, codecType, function);
    }

    private static String getRandomElement(String[] array) {
        return array[ThreadLocalRandom.current().nextInt(array.length)];
    }
}
