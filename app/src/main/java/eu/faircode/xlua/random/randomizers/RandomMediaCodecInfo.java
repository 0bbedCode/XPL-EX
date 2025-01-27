package eu.faircode.xlua.random.randomizers;

import java.util.ArrayList;
import java.util.List;


import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomMediaCodecInfo {
    private static final String[] PREFIXES = {"OMX.", "c2."};
    private static final String[] MANUFACTURERS = {"google", "qcom", "Exynos", "MTK", "Nvidia", "android"};
    private static final String[] CODEC_TYPES = {"h264", "avc", "hevc", "vp8", "vp9", "mpeg4", "aac"};
    private static final String[] FUNCTIONS = {"decoder", "encoder"};

    private static final String[] VIDEO_MIME_TYPES = {
            "video/avc", "video/hevc", "video/mp4v-es", "video/x-vnd.on2.vp8", "video/x-vnd.on2.vp9"
    };
    private static final String[] AUDIO_MIME_TYPES = {
            "audio/mp4a-latm", "audio/mpeg", "audio/opus", "audio/flac", "audio/3gpp"
    };

    public static String generateName() {
        String prefix = getRandomElement(PREFIXES);
        String manufacturer = getRandomElement(MANUFACTURERS);
        String codecType = getRandomElement(CODEC_TYPES);
        String function = getRandomElement(FUNCTIONS);

        return String.format("%s%s.%s.%s", prefix, manufacturer, codecType, function);
    }

    public static String[] generateSupportedTypes() {
        int numTypes = RandomGenerator.nextInt(1, 4); // Generate 1 to 3 types
        List<String> types = new ArrayList<>();
        for (int i = 0; i < numTypes; i++) {
            if (RandomGenerator.nextBoolean()) {
                types.add(getRandomElement(VIDEO_MIME_TYPES));
            } else {
                types.add(getRandomElement(AUDIO_MIME_TYPES));
            }
        }

        return types.toArray(new String[0]);
    }

    private static String getRandomElement(String[] array) {
        return array[RandomGenerator.nextInt(array.length)];
    }
}
