package eu.faircode.xlua.x.xlua.settings;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.string.StringCharBlock;
import eu.faircode.xlua.x.data.string.StringPartsBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;


/**
 * This will Give us Information on the Name of Item, it is suppose to be Parsed using the {.StringPartsBuilder} object
 * We want the Regular / Raw Name aka the Name that is not parsed or cleaned, typically the name that appears in Database Entries or JSON Configs within the App Project or what not
 * A Nice Name is the Name of the Object but a Name that can be Displayed to the UI, so a Name that usually only Consists of Alpha Numeric Characters, Capital Letter for each word and a Single Space between each Word
 * A Nice Name without Numeric Ending is just like a Nice Name but if the Nice name Ends with a Number (usually used for Index pointing) then it will not include that
 * The Group will usually be the First String / Word within the Name
 *
 * The Child Names are Names Generated for the Possible Children of the Name, kind of like possible patterns for the given name but in this case possible Indexed Settings
 * We have a Special Settings Kind that Controls multiple Indexes or "values" of the Setting, so some Settings can have more than one value
 * A setting for Example that may have more than one value is something like the CELL settings perhaps "cell.operator.display.name" aka the Name of the Carrier for that Sim Card
 * Since Phones can have more than One Sim Card to resolve the issue of using one setting to represent both SIMs we save settings in the database as like "cell.operator.display.name.1" for the fist SIM and "cell.operator.display.name.2" for the Second SIM
 * In the JSON File that has a Pre Set of the Settings for XPL-EX, settings with Multiple settings look like "cell.operator.display.name.[1,2]" telling that it can have up to (2) setting value for that setting
 * That being said Settings that do not have the possibility of Multiple values will not have any child settings or Index like attributes
 *
 * Example:
 *  name                        -> cool.setting.name.of.some.sort.1
 *  name nice                   -> Cool Setting Name Of Some Sort 1
 *  name nice no numeric ending -> Cool Setting Name Of Some Sort
 *  index                       -> 1    Not going off of Zero Based Indexing, so in the actual Hook Code we have to ensure we match any Zero Based Indexing tags with its Non Zero Based Indexing Setting
 *                                      This is to preserve the "prettiness" of the UI looks confusing and weird when it shows (0) then the next is (1)
 *
 *
 * Rest of Information is just 'extra' info mainly directed at Settings Object, perhaps Soon we can make this class more generalized (to not be specific to one object)
 * Perhaps to better the linking between UI Index and Actual Code Zero based Indexing we can do something like "cool.setting.name.[1:0, 2:1]" Nice name will end with (1) but the actual name will end with (0) ?
 *
 */
public class NameInformation {
    private static final String TAG = "XLua.NameInformation";

    public static final String GROUP_BUILT_IN = "Built-In";
    public int index = 0;
    public String name;
    public String group;

    //public String containerName;
    public String nameNice;
    public String nameNiceNoNumericEnding;

    public NameInformation parentNameInformation = null;

    public boolean endsWithNumber = false;
    public boolean endsWithList = false;
    public boolean endsWithBool = false;
    public boolean endsWithParent = false;
    public boolean isBuiltIn = false;

    public NameInformationKind kind = NameInformationKind.UNKNOWN;

    public List<NameInformation> childrenNameInformation = new ArrayList<>();

    public List<String> childrenNames() {
        List<String> names = new ArrayList<>();
        for(NameInformation info : childrenNameInformation)
            if(!names.contains(info.name))
                names.add(info.name);

        return names;
    }

    public static NameInformation create(String settingName) { return new NameInformation(settingName); }
    public static NameInformation create(StringPartsBuilder parts) { return new NameInformation(parts); }
    public static NameInformation create(NameInformation nameInfo, int index) { return new NameInformation(nameInfo, index); }

    public static NameInformation createRaw(String name) {
        NameInformation information = new NameInformation();
        information.index = 0;
        information.name = name;
        information.group = name;
        information.nameNiceNoNumericEnding = name;
        information.nameNice = name;
        information.kind = NameInformationKind.UNKNOWN;
        return information;
    }

    public String getContainerName() {
        /*
            Duplicate Setting / Single Indexable Setting issue is here
            The organizer mis handles the Container name as it assumes it resolves it as parent name
            Instead since No Parent Info, it will use its Name, creating it as its Own Container
         */
        return hasChildren() ? name : parentNameInformation != null ? parentNameInformation.name : name;
    }

    public NameInformation() {  }
    public NameInformation(String setting) { this(nameToParts(setting)); }
    public NameInformation(StringPartsBuilder parts) { parseParts(parts); }
    public NameInformation(NameInformation parent, int index) {
        if(index > -1 && parent != null) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Creating Named Information for a Child Index Setting. Container name = " + parent.name + " Child Index number (" + index + ")");

            this.index = index;
            this.endsWithNumber = true;
            this.name = parent.name + "." + String.valueOf(index);
            this.group = parent.group;  //lets always set group even if is just that name

            this.nameNice = "[" + index + "]";              //This is the UI view
            this.nameNiceNoNumericEnding = this.nameNice;

            this.isBuiltIn = parent.isBuiltIn;
            this.parentNameInformation = parent;

            this.kind = NameInformationKind.CHILD_HAS_PARENT;
        }
    }

    private void parseParts(StringPartsBuilder parts) {
        if(parts != null && parts.hasParts()) {
            //this.name = parts.getLastBrokenParts().isEmpty() ? parts.getOriginalString() : SettingsGlobals.getBaseString(parts.getOriginalString());
            this.name = parts.getString(".").toLowerCase();
            this.group = parts.partsCount() > 1 ? parts.getFirstPart(GROUP_BUILT_IN) : GROUP_BUILT_IN;

            //new Builder copies the (parts) one then invoke

            //So Container name is always

            //this.containerName = new NameInformation(new StringPartsBuilder().copyNonImportant(parts))

            this.nameNice = parts.trimStartParts(this.group).resolveParts(StringPartsBuilder.NUMBER_RESOLVER_MAP).getString();
            this.nameNiceNoNumericEnding = parts.trimEndNumericParts(true).getString();

            //  we just set container name to name if its a Single
            //  else Container name is a Parents Name without "[]" and Numeric ending,
            //  else its the Child's name without the index number ending
            //if(parts.hasParts()) {
            //    parts.brok
            //}

            ListUtil.addAllIfValid(this.childrenNameInformation, childrenNames(this, parts));

            String org = parts.getOriginalString();
            this.endsWithList = org.endsWith(".list");
            this.endsWithBool = org.endsWith(".bool") || org.endsWith(".boolean");
            this.endsWithParent = org.endsWith(".parent.control");
            this.isBuiltIn = this.group.equalsIgnoreCase(GROUP_BUILT_IN);

            this.endsWithNumber = isLastCharNumeric();
            this.index = this.endsWithNumber ? Str.tryParseInt(String.valueOf(this.name.charAt(this.name.length() - 1))) : 0;

            this.kind = this.hasChildren() ?
                        NameInformationKind.PARENT_HAS_CHILDREN_IS_CONTAINER :
                            this.endsWithNumber ?
                            NameInformationKind.CHILD_HAS_PARENT : NameInformationKind.SINGLE_NO_PARENT;

            if(this.kind == NameInformationKind.PARENT_HAS_CHILDREN_IS_CONTAINER && this.endsWithNumber)
                Log.w(TAG, "Waring Expected Child Setting is Considered a Parent ? " + this);
        }
    }

    public List<NameInformation> getChildrenNames() { return ListUtil.copyToList(childrenNameInformation); }

    public NameInformationKind getKind() { return kind; }

    public boolean hasParent() { return parentNameInformation != null; }
    public boolean hasChildren() { return ListUtil.isValid(childrenNameInformation, 2); }
    public boolean isList() { return name.endsWith("list"); }
    public boolean isBoolean() { return name.endsWith("boolean") || name.endsWith("bool"); }
    public boolean isLastCharNumeric() { return Character.isDigit(name.charAt(name.length() - 1)); }

    public void prepareChildrenHolders(Map<Integer, SettingHolder> map) {
        if(map != null && hasChildren()) {
            for(NameInformation name : childrenNameInformation) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "[prepareChildrenHolders] Preparing Children, this=" + Str.toStringOrNull(this) + " Child=" + Str.toStringOrNull(name));

                //map.put(name.index, null);           //Null Holders as it needs to Fill it in within sorting
                                                        //This class just understands and knows this Name Has Children and the Information of the Children Names
                                                        //Parent Callers are Responsible for the rest

                SettingHolder tempHolder = new SettingHolder(name, null, "");
                map.put(name.index, tempHolder);
            }
        }
    }

    public static StringPartsBuilder nameToParts(String settingName) {
        return StringPartsBuilder.createOnlyAlphaNumeric()
                .breakOnAndStartOn('[', ']')
                .resolverMap(SettingsGlobals.SETTINGS_RESOLVER_MAP)
                .parseStringParts(settingName);
    }

    public static List<NameInformation> childrenNames(NameInformation parent, StringPartsBuilder parts) {
        if(parent == null || parts == null || !parts.hasParts()) return null;
        List<String> lastParts = parts.getLastBrokenParts();
        if(!ListUtil.isValid(lastParts)) return null;
        List<NameInformation> childSettings = new ArrayList<>();
        for(String s : lastParts) {                                         //Get only the last possible "numeric" parts, as a string can have many chunks
            char[] chars = s.toCharArray();
            int sz = chars.length;
            StringCharBlock block = new StringCharBlock(s.length());        //Ensure space
            for(int i = 0; i < sz && block.getCurrentIndex() < 8; i++) {    //We ony want up to (8) Chars as anything past can be more than a "long" number index
                char c = chars[i];
                if(c == '0' && block.getCurrentIndex() > 0) block.appendUnsafe(c);
                else if(Character.isDigit(c)) block.appendUnsafe(c);
            }

            if(block.getCurrentIndex() > 0) {
                int index = Integer.parseInt(block.toString());
                NameInformation nameInfo = new NameInformation(parent, index);
                childSettings.add(nameInfo);
            }
        }

        if(ListUtil.isValid(childSettings, 2) && DebugUtil.isDebug())
            Log.d(TAG, "Name Information was parsed to Have Children.  Count=" + childSettings.size() + " Parent name=" + parent.name);

        return childSettings;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == null) return false;
        if(obj instanceof String) {
            String comp = (String)obj;
            return comp.equalsIgnoreCase(this.name);    //Only Name Compare ?
        }

        if(obj instanceof NameInformation) {
            NameInformation comp = (NameInformation) obj;
            return comp.group.equalsIgnoreCase(this.group)
                    && comp.name.equalsIgnoreCase(this.name) && comp.nameNice.equalsIgnoreCase(this.nameNice) && comp.nameNiceNoNumericEnding.equalsIgnoreCase(this.nameNiceNoNumericEnding);
        }

        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Name", this.name)
                .appendFieldLine("Name Nice", this.nameNice)
                .appendFieldLine("Name Nice without Index Ending", this.nameNiceNoNumericEnding)
                .appendFieldLine("Group", this.group)
                .appendFieldLine("Is Built In", this.isBuiltIn)
                .appendFieldLine("Index", this.index)
                .appendFieldLine("Kind", kind)
                .setDoAppendFlag(this.hasChildren())
                .appendDividerTitleLine("Children")
                .appendCollectionLine(this.childrenNameInformation)
                .appendDividerLine()
                .resetDoAppendFlag()
                .appendFieldLine("Ends with Number", this.endsWithNumber)
                .appendFieldLine("Ends with List", this.endsWithList)
                .appendFieldLine("Ends with Bool", this.endsWithBool)
                .appendFieldLine("Ends with Parent", this.endsWithParent)
                .toString(true);
    }
}
