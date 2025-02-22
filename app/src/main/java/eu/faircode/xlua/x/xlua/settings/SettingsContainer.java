package eu.faircode.xlua.x.xlua.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.ui.core.interfaces.IDiffFace;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.hook.data.AssignmentData;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;
import eu.faircode.xlua.x.xlua.settings.interfaces.NameInformationTypeBase;

//If settings is (1) then disable child Setting Check Box as it will use the Parent Check Box

/**
 * This class is responsible for Holding the Settings, less of a Group Container like "Network", "SoC" or "Unique" but more so exists to Hold Settings that can Have multiple values
 * Some Settings like "CELL" Settings can have multiple values as some devices have more than just (1) SIM Cards so we need to represent possible multiple different values for the same settings for different indexes
 * Some Settings like "SoC" Do not have Multiple Values as you typically only have one "SoC"
 * Settings with Multiple values will be represented with ".[]" in the Default Settings Map JSON File in this Project (not actual configs that are exported and imported)
 *
 * Example:
 *      cell.operator.display.name.[1,2]    The carrier name for the SIM Card in this day and age we can have more than one SIM card so (1, 2) to represent dual SIM
 *                                          Its not Zero Based Indexing to avoid Confusion when and if being printed to the UI for users of XPL-EX
 *                                          NOTE DO NOT Store the Value of the Setting in the Database with "[]" instead we store the values as:
 *                                              cell.operator.display.name.1    (if set)
 *                                              cell.operator.display.name.2    (if set)
 *                                          The JSON with Defaults Map built within this Project is the only thing that will contain "[]"
 *
 * For settings that do not have the possibility of "multiple" values then the Container will just have a List size for "settings" as just (1) as its Containing just one Setting the main one
 *
 */
public class SettingsContainer extends NameInformationTypeBase implements IDiffFace, IIdentifiableObject {
    private static final String TAG = LibUtil.generateTag(SettingsContainer.class);

    public static boolean isContainerSetting(String settingName) { return SettingsGlobals.endsWithArrayPattern(settingName); }
    public static SettingsContainer create(String settingName) { return new SettingsContainer(settingName);  }
    public static SettingsContainer create(NameInformation nameInformation) { return new SettingsContainer(nameInformation); }

    private String containerName;
    private String description;
    private final LinkedHashMap<Integer, SettingHolder> settings = new LinkedHashMap<>();

    //public String assignmentDataString = null;  //ToDO: Replace this with the actual data ? or make functions get actual data ?

    public final AssignmentData data = new AssignmentData();

    public static String sharedContainerName(String name) {
        return Str.combine("con::", name);
    }


    public String getContainerName() { return containerName; }
    public String getDescription() { return description; }
    public boolean isSingleSetting() { return settings.size() == 1; }
    public boolean hasSettings() { return !settings.isEmpty(); }

    @Override
    public String getObjectId() { return "con::" + nameInformation.name; }

    @Override
    public void setId(String id) {
        this.containerName = id;
    }

    public int getSettingsCount() { return this.settings.size(); }

    public List<String> getAllNames() {
        List<String> names = new ArrayList<>();
        if(nameInformation != null) {
            names.add(nameInformation.name);
            if(nameInformation.hasChildren()) {
                for(NameInformation n : nameInformation.getChildrenNames()) {
                    if(!TextUtils.isEmpty(n.name) && !names.contains(n.name)) {
                        names.add(n.name);
                    }
                }
            }
        }
        return names;
    }

    public List<SettingHolder> getSettings() { return new ArrayList<>(settings.values()); }
    public void ensureDescription(String description) { if(this.description == null && !TextUtils.isEmpty(description)) this.description = description; }

    public SettingsContainer(SettingHolder singleSetting) {
        consumeSingleSetting(singleSetting);
    }

    public SettingsContainer(String settingName) { this(new NameInformation(settingName)); }
    public SettingsContainer(NameInformation nameInformation) {
        if(nameInformation != null) {
            super.bindNameInformation(nameInformation);
            this.containerName = nameInformation.nameNice;
            nameInformation.prepareChildrenHolders(settings);
        }
    }

    public SettingsContainer(NameInformation nameInformation, String containerName) {
        if(nameInformation != null) {
            super.bindNameInformation(nameInformation);
            this.containerName = containerName;
            nameInformation.prepareChildrenHolders(settings);
        }
    }

    public void consumeSingleSetting(SettingHolder holder) {
        if(holder != null) {
            super.bindNameInformation(holder.getNameInformation());
            this.containerName = getNameNice();
            this.settings.clear();
            this.settings.put(0, holder);
            this.description = holder.getDescription();
        }
    }

    public void finalizeContainer() {
        if(hasChildren()) {
            List<NameInformation> childNames = nameInformation.getChildrenNames();
            for(Map.Entry<Integer, SettingHolder> en : settings.entrySet()) {
                int index = en.getKey();
                SettingHolder holder = en.getValue();
                if(holder == null) {
                    for(NameInformation nameInfo : childNames) {
                        if(nameInfo.index == index) {
                            holder = new SettingHolder(nameInformation, null, "");
                            settings.put(nameInfo.index, holder);   //Is this fine ?
                            break;
                        }
                    }
                }
            }
        }
    }

    public void updateChild(SettingHolder child, SettingPacket setting) {
        if(child != null) {
            if(setting != null) {
                child.setValue(setting.value);
                if(Str.isEmpty(child.getDescription()) && !Str.isEmpty(setting.description))
                    child.setDescription(setting.description);
            }

            NameInformation childNameInformation = child.getNameInformation();
            if(childNameInformation != null)
                childNameInformation.parentNameInformation = nameInformation;

            settings.put(child.getIndex(), child);
        }
    }

    public void ensureHasChild(SettingHolder child) {
        if(child != null) {
            ensureDescription(child.getDescription());
            SettingHolder cache = settings.get(child.getIndex());
            if(cache != null)
                SettingsUtil.copyHolder(child, cache, true);

            else settings.put(child.getIndex(), child);
        }
    }

    /*public void queOrBindChildrenSettings(SettingsOrganizer organizer) {
        if(organizer != null && nameInformation.hasChildren()) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Starting the Process of Adding this Parents Children Settings to the Que, this Name Information=" + this);

            for(NameInformation nameInfo : nameInformation.getChildrenNames()) {
                String name = nameInfo.name;
                SettingHolder holder = organizer.getFromAwaitingQue(name);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.ensureNoDoubleNewLines(
                            "Que for Child=" + name + " Setting Holder is Null for Child ? " + Str.isNullAsString(holder) +
                                    "\nHolder=" + Str.toStringOrNull(holder) +
                                    "\nName Info=" + Str.toStringOrNull(nameInfo)));

                if(holder != null) {
                    settings.put(nameInfo.index, holder);
                    ensureDescription(holder.getDescription());
                    organizer.removeFromAwaitingQue(name);      //We found our child setting
                } else {
                    SettingHolder queHolder = new SettingHolder(nameInfo, null, null);
                    queHolder.wasCreatedFromContainer = true;
                    settings.put(nameInfo.index, queHolder);
                    organizer.pushToAwaitingQue(queHolder);
                }
            }
        }
    }*/

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof String) return ((String)obj).equalsIgnoreCase(this.containerName);
        if(obj instanceof SettingsContainer) {
            SettingsContainer comp = (SettingsContainer) obj;
            return this.containerName.equalsIgnoreCase(comp.containerName) && this.getGroup().equalsIgnoreCase(comp.getGroup());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return containerName.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Container Name", this.containerName)
                .appendFieldLine("Description", this.description)
                .appendFieldLine("Settings Count", this.settings.size())
                .appendFieldLine("Is Single Setting", this.isSingleSetting())
                .appendLine(Str.toStringOrNull(this.getNameInformation()))
                .toString(true);
    }

    //@Override
    //public String getId() {
    //    return getContainerName();
    //}

    @Override
    public boolean areItemsTheSame(IDiffFace newItem) {
        return newItem instanceof SettingsContainer &&
                this.getName().equalsIgnoreCase(((SettingsContainer) newItem).getName());
    }

    @Override
    public boolean areContentsTheSame(IDiffFace newItem) {
        if(newItem instanceof SettingsContainer) {
            SettingsContainer other = (SettingsContainer) newItem;
            return this.settings.size() == other.settings.size() &&
                    this.getName().equalsIgnoreCase(other.getName()) &&
                    this.description != null ?
                    this.description.equals(other.description) :
                    other.description == null;
        }
        return false;
    }

    @Override
    public Object getChangePayload(IDiffFace newItem) {
        if (!(newItem instanceof SettingsContainer)) return null;
        SettingsContainer other = (SettingsContainer) newItem;

        Bundle diff = new Bundle();
        if (!TextUtils.equals(this.description, other.description)) {
            diff.putString("description", other.description);
        }
        if (!TextUtils.equals(this.containerName, other.containerName)) {
            diff.putString("containerName", other.containerName);
        }
        if (this.settings.size() != other.settings.size()) {
            diff.putBoolean("settingsSizeChanged", true);
        }
        return diff.isEmpty() ? null : diff;
    }
}
