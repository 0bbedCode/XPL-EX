package eu.faircode.xlua.x.xlua.settings.random.randomizers.unique;

import android.util.Log;

import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

//unique.gsm.subscription.id.[1,2]
//274299888888765
//I think this is the one that is specific ? someone had issues with it something with last few numbers not being legit ?

/*
    310150123456789
    MCC: 310 (United States)
    MNC: 150 (AT&T)
    MSIN: 123456789
 */

public class RandomSubscriptionId extends RandomElement {
    private static final String TAG = LibUtil.generateTag(RandomSubscriptionId.class);

    public RandomSubscriptionId() {
        super("Unique Cell Subscription ID");
        putIndexSettings(RandomizersCache.SETTING_UNIQUE_SUB_ID, 1, 2);
        putRequirementsAsIndex(
                RandomizersCache.SETTING_CELL_OPERATOR_MCC,
                RandomizersCache.SETTING_CELL_OPERATOR_MNC,
                RandomizersCache.SETTING_CELL_MSIN);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        String name = context.stack.pop();
        if(!context.wasRandomized(name)) {
            List<String> req = context.resolveRequirements(getRequirements(name));
            String mcc = context.getValue(req.get(0));  //MCC
            String mnc = context.getValue(req.get(1));  //MNC
            String msn = context.getValue(req.get(2));  //MSN
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Randomizing Subscription ID [%s] MCC [%s] MNC [%S] MSN [%s]", name, mcc, mnc, msn));

            context.pushSpecial(name, Str.combineEx(mcc, mnc, msn));
        }
    }
}