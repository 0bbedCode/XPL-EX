package eu.faircode.xlua.x.data.utils.random;

public class RandomStringHelper {
    public static final String NUMERIC_CHARS = "0123456789";
    public static final String ALPHA_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String ALPHA_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String ALPHA_LOWER = "abcdefghijklmnopqrstuvwxyz";
    public static final String HEX_CHARS = "0123456789ABCDEF";

    public static final String ALPHA_NUMERIC_CHARS = NUMERIC_CHARS + ALPHA_CHARS;
    public static final String ALPHA_NUMERIC_UPPERCASE_CHARS = NUMERIC_CHARS + ALPHA_UPPER;
    public static final String ALPHA_NUMERIC_LOWERCASE_CHARS = NUMERIC_CHARS + ALPHA_LOWER;

    private static String getCharacterSet(RandomStringKind kind) {
        switch (kind) {
            case NUMERIC: return NUMERIC_CHARS;
            case ALPHA_NUMERIC_UPPERCASE: return ALPHA_NUMERIC_UPPERCASE_CHARS;
            case ALPHA_NUMERIC_LOWERCASE: return ALPHA_NUMERIC_LOWERCASE_CHARS;
            case HEX: return HEX_CHARS;
            case ALPHA_NUMERIC:
            default:
                return ALPHA_NUMERIC_CHARS;
        }
    }


    public static String generateString(IRandomizerProvider provider, RandomStringKind kind, int origin, int bound) {
        if (origin > bound) { int temp = origin; origin = bound; bound = temp; }
        if (origin < 1) origin = 1; // Default origin to 1 if below minimum
        int length = provider.nextInt(origin, bound);
        return generateString(provider, kind, length);
    }

    public static String generateString(IRandomizerProvider provider, RandomStringKind kind, int length) {
        if (length < 1) length = 1; // Default minimum length to 1
        String chars = getCharacterSet(kind);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(provider.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static String generateString(IRandomizerProvider provider, int length) {
        return generateString(provider, RandomStringKind.ALPHA_NUMERIC, length);
    }

    public static String generateString(IRandomizerProvider provider, RandomStringKind kind) {
        return generateString(provider, kind, 16); // Default length to 16
    }

    public static String generateString(IRandomizerProvider provider) {
        return generateString(provider, RandomStringKind.ALPHA_NUMERIC, 16); // Default kind and length
    }

    public static String generateStringWithOriginAndBound(IRandomizerProvider provider, int origin, int bound) {
        return generateString(provider, RandomStringKind.ALPHA_NUMERIC, origin, bound);
    }

    public static String generateStringWithOriginAndBound(IRandomizerProvider provider, RandomStringKind kind, int origin, int bound) {
        return generateString(provider, kind, origin, bound);
    }

    public static String generateString(IRandomizerProvider provider, int origin, int bound) {
        return generateString(provider, RandomStringKind.ALPHA_NUMERIC, origin, bound);
    }
}