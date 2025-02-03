package eu.faircode.xlua.x.xlua.settings.random.randomizers.apps;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionNullElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

/*

    public static final String RAND_ONCE = "%random.once%";
    public static final String RAND_ALWAYS = "%random.always%";

    public static final String NOW_ONCE = "%now.once%";
    public static final String NOW_ALWAYS = "%now.always%";


    public static final String INSTALL_OFFSET_SETTING = "apps.install.time.offset";
    public static final String UPDATE_OFFSET_SETTING = "apps.update.time.offset";

    public static final String INSTALL_GROUP = "installTime";
    public static final String UPDATE_GROUP = "updateTime";
 */

public class RandomAppTime extends RandomElement {
    public RandomAppTime() {
        super("Apps Time Offset");
        putSettings(
                RandomizersCache.SETTING_APP_INSTALL_TIME_OFFSET,
                RandomizersCache.SETTING_APP_UPDATE_TIME_OFFSET,
                RandomizersCache.SETTING_APP_CURRENT_INSTALL_TIME_OFFSET,
                RandomizersCache.SETTING_APP_CURRENT_UPDATE_TIME_OFFSET);
        putOptions(
                RandomOptionNullElement.create(),
                RandomOptionString.create("Manual Input", ""),
                RandomOptionString.create("Random Once", "%random.once%"),
                RandomOptionString.create("Random Always", "%random.always%"),
                RandomOptionString.create("Now Once", "%now.once%"),
                RandomOptionString.create("Now Always", "%now.always%"));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        randomOption().randomize(context);
    }
}