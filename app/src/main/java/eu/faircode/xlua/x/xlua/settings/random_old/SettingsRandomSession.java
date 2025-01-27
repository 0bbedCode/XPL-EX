package eu.faircode.xlua.x.xlua.settings.random_old;

import eu.faircode.xlua.x.xlua.settings.NameInformationMap;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingsContext;

/**
 *
 * Hmm So another point where its nice to have context is so we know what INPUT text / modify to push and apply to object
 * What if we just wanted to Randomize all ones in the Group but some of them are not enabled
 * Then for that some we don't push certain changes ???? maybe ..
 * We can Consume "SettingsRandomContext Map" map, or we can Directly inherit it on Object
 * So Random Session is Similar to Context ?
 *
 * class TypeOne : IValid:
 *      bool isValid()
 *      ... rest of functions
 *
 * class TypeTwo : NameMap:
 *      bool isValid()
 *      op get(x)
 *      op put(x)
 *
 *  Two would be easier if we dont want to "re-implement" the logic for mapped name pairs
 *  Well either way Two Would not cause any issues if One was like that ? they both are not used in a way of parent ... ?
 *  Lets just keep going
 *
 * Directory inheriting it seems not good
 *
 * ToDo: Make code more clear or / add Systems for Sessions, Repos, for patterned types like those ? some interface perhaps ?
 *
 * I think we dont even need this then if:
 * NameInformationMap<SettingHolder> settings,
 * NameInformationMap<SettingHolder> enabled,   ... in SettingsContext ? we can do
 * NameInformationMap<SettingsRandomContext> randomized;    //Clean when done ? or leave ? ye fuck this class xD
 *                                                            This will save code, and more easier to follow and work with already made Randomizers
 */
public class SettingsRandomSession extends NameInformationMap<SettingRandomContext> {
    private static final String TAG = "XLua.SettingsRandomSession";
    private final SettingsContext context;
    //This is just NameInformation map but with a Twist ?
    //Dont over think it, SettingsContext has these maps as Fields "NameInformationMap<SettingHolder> settings, NameInformationMap<SettingHolder> enabled ..."

    //@Override
    public boolean isValid() { return context != null; }    //Valid check On Context ?

    public SettingsRandomSession(SettingsContext context) {
        this.context = context;
    }













    /*public SettingRandomContext createContext(SettingHolder holder, boolean cache) {

    }

    public SettingRandomContext generateContextWrapper(String settingName) {
        if(TextUtils.isEmpty(settingName)) return null;
        if(randomized.containsKey(settingName)) return randomized.get(settingName);

        SettingHolder holder = context.getSetting(settingName);
        if(holder == null) {
            Log.e(TAG, "Error, failed to find the Holder for the setting [" + settingName + "] ... Failed to Create Random Context Wrapper. Stack=" + RuntimeUtils.getStackTraceSafeString());
            return null;
        }



        SettingRandomContext randomContext = new SettingRandomContext(holder);
        randomized.put(ho)
    }


    public boolean wasSettingRandomized(SettingHolder holder) { return holder != null && randomized.containsKey(holder.getName()); }
    public boolean wasSettingRandomized(String settingName) { return randomized.containsKey(settingName); }
    public void pushRandomizedRandomized(SettingHolder holder) {
        if(holder != null) {
            randomized.put(holder.getName(), holder);
            //ToDo more ?
        }
    }*/

    public SettingHolder getSetting(String settingName) { return context.getSetting(settingName); }
    //public SettingHolder getEnabledSetting(String settingName) { return context }

    //from "randomize(CONTEXT)" think...
    //have some take give back hold functions ? Take Some Settings, Put Some back if not needed, and hold the ones that are being used ?

    /**
     *  Hmm some bases can "consume" its alt type without new for re construction of the base.
     *
     *  interface IType:
     *      get()   //This is like "getSetting" how Randomize Context can also use that function or how we consume Name Info as "INameInformation"
     *
     *  class TypeA inherits IType: //Base ?
     *      get()
     *
     *  class TypeB inherits IType:
     *      TypeB(consume information)
     *      get()
     *
     *  Hmm this is already super similar, or (I think better) create some custom mapper class ?
     *  Fuck these checks if they put in NULL Data that is their issue !
     *
     */
}
