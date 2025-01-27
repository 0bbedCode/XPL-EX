package eu.faircode.xlua.x.xlua.settings.random.randomizers.cell;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionNullElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomSIMCountyCode extends RandomElement {
    public static final Map<String, String> COUNTRY_NAME_TO_NUMERIC_CODE = Collections.unmodifiableMap(
            new HashMap<String, String>() {{
                put("United States", "1");
                put("Canada", "1");
                put("India", "91");
                put("United Kingdom", "44");
                put("Australia", "61");
                put("China", "86");
                put("Germany", "49");
                put("France", "33");
                put("Japan", "81");
                put("South Korea", "82");
                put("Brazil", "55");
                put("South Africa", "27");
                put("Nigeria", "234");
                put("Russia", "7");
                put("Italy", "39");
                put("Spain", "34");
                put("Mexico", "52");
                put("Saudi Arabia", "966");
                put("United Arab Emirates", "971");
                put("Argentina", "54");
                put("Chile", "56");
                put("Colombia", "57");
                put("Egypt", "20");
                put("Indonesia", "62");
                put("Malaysia", "60");
                put("Philippines", "63");
                put("Singapore", "65");
                put("Thailand", "66");
                put("Turkey", "90");
                put("Vietnam", "84");
                put("Pakistan", "92");
                put("Bangladesh", "880");
                put("Sweden", "46");
                put("Norway", "47");
                put("Finland", "358");
                put("Denmark", "45");
                put("Iceland", "354");
                put("New Zealand", "64");
                put("Switzerland", "41");
                put("Belgium", "32");
                put("Netherlands", "31");
                put("Austria", "43");
                put("Poland", "48");
                put("Czech Republic", "420");
                put("Hungary", "36");
                put("Greece", "30");
                put("Portugal", "351");
                put("Ireland", "353");
                put("Israel", "972");
            }}
    );

    public RandomSIMCountyCode() {
        super("CELL SIM Country Code");
        putIndexSettings(RandomizersCache.SETTING_CELL_SIM_COUNTRY_CODE, 1, 2);
        putOption(RandomOptionNullElement.create());
        for(Map.Entry<String, String> entry : COUNTRY_NAME_TO_NUMERIC_CODE.entrySet())
            putOption(RandomOptionString.create(entry.getKey(), entry.getValue()));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //Since this is an indexable setting, out "name" should already be appended to the stack
        randomOption().randomize(context);
    }
}