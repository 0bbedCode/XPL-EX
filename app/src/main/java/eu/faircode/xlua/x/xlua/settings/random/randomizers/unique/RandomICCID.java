package eu.faircode.xlua.x.xlua.settings.random.randomizers.unique;

import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomICCID extends RandomElement {
    /**
     * ICCID Format: MM CC III NNNNNNNNNND
     * - MM: Major Industry Identifier (89 for telecommunications).
     * - CC: Country Code (based on ITU-T E.164, e.g., 01 = USA, 44 = UK).
     * - III: Issuer Identifier Number (specific to carrier, e.g., 001 = AT&T).
     * - NNNNNNNNN: Unique SIM card identifier (varies in length).
     * - D: Check Digit (calculated using the Luhn algorithm).
     *
     * Example: 89 86 012345678901234
     * - 89: Telecom industry.
     * - 86: China (Country Code).
     * - 012: China Mobile (Issuer).
     * - 34567890123: Unique identifier.
     * - 4: Check Digit.
     */

    public RandomICCID() {
        super("CELL ICC ID (MM + CC + II + NNN...)");
        putIndexSettings(RandomizersCache.SETTING_UNIQUE_ICC_ID, 1, 2);
        putRequirementsAsIndex(RandomizersCache.SETTING_CELL_SIM_COUNTRY_CODE, RandomizersCache.SETTING_CELL_OPERATOR_MNC);
    }

    //return RandomStringGenerator.generateRandomNumberString(18) + RandomStringGenerator.generateRandomLetterString(1, "ABCDEF");
    @Override
    public void randomize(RandomizerSessionContext context) {
        String name = context.stack.pop();
        if(!context.wasRandomized(name)) {
            List<String> req = context.resolveRequirements(getRequirements(name));
            String countryCode = context.getValue(req.get(0));
            String mnc = context.getValue( req.get(1));
            context.pushSpecial(name, generateIccid("89", countryCode, mnc));
        }
    }


    /**
     * Generates a complete ICCID based on the provided MM, CC, and III parts.
     *
     * @param mm  Major Industry Identifier (e.g., "89")
     * @param cc  Country Code (e.g., "91" for India)
     * @param iii Issuer Identifier Number (e.g., "840" for Jio)
     * @return A complete and valid ICCID as a String.
     */
    public static String generateIccid(String mm, String cc, String iii) {
        if(ObjectUtils.anyNull(mm, cc, iii))
            return "error_null";

        // Length of NNNN part: 19 (total length) - 2 (MM) - CC length - 3 (III) - 1 (Check Digit)
        int remainingLength = 19 - mm.length() - cc.length() - iii.length() - 1;

        // Generate random digits for the NNNN part
        StringBuilder nnnn = new StringBuilder();
        for (int i = 0; i < remainingLength; i++) {
            nnnn.append(RandomGenerator.nextInt(10)); // Random digit between 0-9
        }

        // Combine all parts without the check digit
        String iccidWithoutCheckDigit = mm + cc + iii + nnnn;

        // Compute the check digit using Luhn algorithm
        int checkDigit = calculateLuhnCheckDigit(iccidWithoutCheckDigit);

        // Append the check digit to complete the ICCID
        return iccidWithoutCheckDigit + checkDigit;
    }

    /**
     * Calculates the Luhn check digit for a given number as a String.
     *
     * @param number The number (ICCID without the check digit).
     * @return The Luhn check digit.
     */
    private static int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true; // Start alternation from the rightmost digit

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9; // Equivalent to summing the digits of numbers > 9
                }
            }
            sum += digit;
            alternate = !alternate;
        }

        // Calculate the check digit
        return (10 - (sum % 10)) % 10;
    }

}