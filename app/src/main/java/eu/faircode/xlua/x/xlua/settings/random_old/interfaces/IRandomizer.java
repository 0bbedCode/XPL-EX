package eu.faircode.xlua.x.xlua.settings.random_old.interfaces;

import java.util.List;

import eu.faircode.xlua.x.xlua.settings.SettingsContext;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomizerKind;


/**
 * Interface that will Help with the Randomizer Object. Not Randomizer Provider like (Random, ThreadLocalRandom, or SecureRandom) but the actual Randomizers made for the Settings
 * Since its not "simply generate a random string" this system can be complex to meet the wants of others, or trying to pertain sanity of attributes
 * We break it up into (2) Randomizer Types
 *      Generic,    This will be the Randomizer for things such as IMEI as those cant be a "specific" type based off of other "settings"
 *                  Just a value that can be generated from a Random String Generator that's it nothing more nothing less no more Complexity
 *
 *      Options,    This will be your Randomizer that provides you options to select as it has a certain Data Set for its value
 *                  This can also be your Randomizers that Control Child Settings Output
 *
 *  Do note options is Different from Settings Indexable Types, while both have "parents" they are still different
 *
 * ToDo: look into , i forgot will fill in as I code and remember oh ye wait im remembering slowly oh ye maybe add like the floating alien a floating save button that shows when un saved changes are made , that is more cool
 *
 *
 */
public interface IRandomizer {
    IRandomizer ensureInstance();
    boolean requiresNewInstance();

    String getDisplayName();
    RandomizerKind getKind();

    /**
     * Settings Part of Randomizer
     * Each Randomizer is created usually for a Setting, to randomize... duh..
     */

    boolean containsSetting(String settingName);
    String getSetting();
    String getSetting(int index);
    List<String> getSettings();

    /**
     * Options Can be the Drop Down, menu, allowing you to select an Option for the Data as Some Data has options such as PHONE TYPE (GSM/CDMA)
     * This can also Help tie in Settings that are "parent controllers".
     * Parent Controllers are Settings that Control the Output of their Child Settings. Used for syncing a Category of Settings together.
     * Example of an parent setting is one that Controls your Network Information, instead of Setting the Value for that One Setting and Have the others in the Same category not match up
     * It can determine its children or siblings then invoke them to change to a new value that aligns with yours
     * If you select parent ISP Control (AT&T) then Ensure all Settings that can Reflect attributes of (AT&T) are also notified to update their values to align with (AT&T) ISP like attributes
     * An Option will still be a "IRandomizer" to make things more clean
     * As "IRandomizer" has everything we need to re - use it
     */

    boolean hasOptions();
    boolean isOptionFiller();
    List<IRandomizer> getOptions(boolean filterOutFillers, boolean removeCurrentOptionFromList);
    IRandomizer getSelectedOption();
    IRandomizer randomizeSelectedOption();
    void setSelectedOption(IRandomizer selectedOption);
    void setSelectedOption(String selectedOptionKey);


    /**
     * This is the actual Generating Portion
     * "generateString" Can be Looked at as either (A) generate the Data ensure its in String Form, or (B) just return Constant Data / Option in String form
     * "randomize" Should be used VIA UI as it gives the Randomize Function "context"
     * Context being our little way of seeing all the settings what not
     */
    String generateString();
    boolean randomize(SettingsContext context, String callingSetting);


    //SettingRandomContextOld getCurrentSettingContext(SettingsContextOld context);
}
