package eu.faircode.xlua.x.ui.core.util;

import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.ui.core.view_registry.SettingSharedRegistry;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;
import eu.faircode.xlua.x.xlua.settings.random.RandomNullElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionNullElement;

/*
    ToDo: Make a Randomizer that pops up with a Prompt like to set the longitue lattitude in pro app like that one!
 */
public class UiRandomUtils {
    private static final String TAG = "XLua.UiRandomUtils";

    //invoke
    public static boolean spinnerSelection(Spinner spSelector, SettingsContainer container, SharedRegistry shared) {
        if(spSelector == null || container == null)
            return false;

        IRandomizer randomizer = (IRandomizer)spSelector.getSelectedItem();
        if(randomizer == null) {
            Log.e(TAG, "Error Selected Randomizer is NULL some how....");
            return false;
        }

        if(randomizer instanceof RandomOptionNullElement) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Option Selected is a NULL or Default ignoring...");

            return false;
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Selected Randomizer Spinner Option=" + randomizer.getDisplayName());

        try {
            /*RandomizerSessionContext ctx = RandomizerSessionContext.create();
            for(SettingHolder setting : getSettingHolders(container.getSettings(), shared)) {
                ctx.stack.push(setting.getName());
                //handle "options"
                randomizer.randomize(ctx);
                //This THIS is ONLY for when invoked spinner, NOT when the randomizer button is clicked
                //Set Randomizer Cache


                String value = ctx.getValue(setting.getName());
                setting.setNewValue(value);
                setting.ensureUiUpdated(value);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Spinner Option Selected, Option=%s  Updated Setting Value, Setting=%s  New Value=%s  Old Value=", randomizer.getDisplayName(), setting.getName(), value, setting.getValue()));
            }*/

            return true;
        }catch (Exception e) {
            Log.e(TAG, "Error Selecting Spinner Option Randomizer, Error=" + e);
            return false;
        }
    }

    public static void initRandomizer(
            ArrayAdapter<IRandomizer> adapterRandomizer,
            Spinner spSelector,
            SettingsContainer container,
            SettingSharedRegistry sharedRegistry) {

        if(!sharedRegistry.hasRandomizers() || container == null) {
            Log.e(TAG, "Invalid Input for [initRandomizer], Make sure Adapters, Lists and Objects are Not Null or Empty!");
            return;
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Initializing Randomizer Spinner, Adapter Count Before=%s  Randomizers Count=%s  Setting Container=%s", adapterRandomizer.getCount(), sharedRegistry.getRandomizersMap().size(), container.getContainerName()));

        adapterRandomizer.clear();
        IRandomizer targetRandomizer = sharedRegistry.getRandomizer(container.getSettings());
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Starting Spinner Logic Loop Target Randomizer=%s  Setting Container=%s", Str.noNL(targetRandomizer) , container.getContainerName()));


        //PS we can add the "null" fillers here
        //So other spots do not need to Consider it

        if(targetRandomizer == null || !targetRandomizer.hasOptions()) {
            adapterRandomizer.add(RandomNullElement.create());
            adapterRandomizer.addAll(sharedRegistry.getRandomizersMap().values());

            if(targetRandomizer != null && !(targetRandomizer instanceof RandomNullElement)) {
                for(int i = 0; i < adapterRandomizer.getCount(); i++) {
                    IRandomizer aRan = adapterRandomizer.getItem(i);
                    if(!(aRan instanceof RandomNullElement) && targetRandomizer.equals(aRan)) {
                        spSelector.setSelection(i);
                        if(DebugUtil.isDebug())
                            Log.d(TAG, "Set Randomizer Adapter Position to=" + i + " Display Name=" + aRan.getDisplayName());

                        break;
                    }
                }
            }
        }
        else if(targetRandomizer.hasOptions()) {
            String targetValue = null;
            boolean allSame = true;
            for(SettingHolder holder : getSettingHolders(container.getSettings(), sharedRegistry)) {
                if(targetValue != null) {
                    if(!targetValue.equalsIgnoreCase(holder.getNewValue())) {
                        allSame = false;
                        break;
                    }
                } else {
                    targetValue = holder.getNewValue();
                }
            }

            adapterRandomizer.addAll(targetRandomizer.getOptions());
            if(targetValue != null && allSame) {
                for(int i = 0; i < adapterRandomizer.getCount(); i++) {
                    IRandomizer op = adapterRandomizer.getItem(i);
                    if(op == null || op instanceof RandomOptionNullElement)
                        continue;

                    String val = op.getRawValue();
                    if(targetValue.equalsIgnoreCase(val)) {
                        spSelector.setSelection(i);
                        if(DebugUtil.isDebug())
                            Log.d(TAG, "Set Randomizer Adapter Option Position to=" + i + " Val=" + val + " Display Name=" + op.getDisplayName());

                        break;
                    }
                }
            }
        }
    }

    public static List<SettingHolder> getSettingHolders(List<SettingHolder> settings, SharedRegistry registry) {
        List<SettingHolder> enabled = new ArrayList<>();
        for(SettingHolder holder : settings) {
            if(registry != null && registry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, holder.getSharedId())) {
                enabled.add(holder);
            }
        }

        if(enabled.isEmpty())
            return settings;

        return enabled;
    }

}
