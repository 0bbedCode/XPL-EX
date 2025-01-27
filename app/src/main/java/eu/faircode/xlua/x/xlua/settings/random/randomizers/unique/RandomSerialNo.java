package eu.faircode.xlua.x.xlua.settings.random.randomizers.unique;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomSerialNo extends RandomElement {
    public RandomSerialNo() {
        //No relation to ICC ID, it links to the prop ? gsm.serial
        //djhwlmt51pd30ar4ymtk
        //Cant find much on this
        //https://newandroidbook.com/ddb/SonyXperiaXA1UltraDual/getprop.txt
        //https://github.com/erleizh/build.prop/blob/master/OPPO%20R11.txt
        super("CELL GSM Band Serial");
        putIndexSettings(RandomizersCache.SETTING_UNIQUE_SIM_SERIAL, 1, 2);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushSpecial(context.stack.pop(), RandomGenerator.nextStringNumeric(9, 20));
    }
}
