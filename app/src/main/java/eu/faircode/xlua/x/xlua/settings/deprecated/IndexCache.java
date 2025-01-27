package eu.faircode.xlua.x.xlua.settings.deprecated;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.utilities.SettingUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;

public class IndexCache {
    private static final String TAG = "XLua.IndexCache";

    public int index;
    public String name;
    public String nameWithoutIndex;
    public String friendlyName;

    public String description;

    public String category;
    public String originalValue;
    public String newValue;

    public boolean isParent() { return this.index == 0; }
    public boolean isChild() { return this.index > 0; }
    public boolean isValid() { return this.index > -1 && this.name != null && this.friendlyName != null && this.category != null; }

    public int getIndex() { return this.index; }
    public String getName() { return this.name; }
    public String getFriendlyName() { return this.friendlyName; }
    public String getNameWithoutIndex() { return this.nameWithoutIndex; }
    public String getDescription() { return this.description; }
    public String getCategory() { return this.category; }
    public String getOriginalValue() { return this.originalValue; }
    public String getNewValue() { return this.newValue; }
    public int getGroupHashCode() { return friendlyName != null ? friendlyName.hashCode() : 0; }

    public boolean isIndexerController() { return this.name.equals(String.valueOf(this.index)) && this.friendlyName.equals(String.valueOf(this.index));  }
    public boolean isIndex(int index) { return this.index == index; }

    public IndexCache createIndexController() { return IndexCache.createIndexerControl(this); }

    public boolean isDifferentValues(SettingExtendedOld settingHolder) { return settingHolder != null && TextUtils.equals(settingHolder.originalValue, this.originalValue) && TextUtils.equals(settingHolder.newValue, this.newValue); }

    public void updateSettingHolder(SettingExtendedOld settingHolder) {
        if(settingHolder != null) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Updating Setting Holder Fields to Reflect this Index Cache! Setting=\n" + Str.toStringOrNull(settingHolder) + " New Cache=\n" + this);

            settingHolder.name = this.name;
            settingHolder.originalValue = this.originalValue;
            settingHolder.newValue = this.newValue;
        }
    }

    public static IndexCache createIndexerControl(IndexCache indexCache) {
        IndexCache item = new IndexCache();
        item.index = indexCache.index;
        item.name = String.valueOf(indexCache.index);
        item.nameWithoutIndex = item.name;
        item.friendlyName = item.friendlyName;
        item.originalValue = item.originalValue;
        item.newValue = item.newValue;
        item.category = indexCache.category;
        item.description = "Index Controller for Settings Category " + indexCache.category;
        return item;
    }

    public static IndexCache create(String name, String originalValue) { return create(name, originalValue, "N/A", null); }
    public static IndexCache create(String name, String originalValue, String description) { return create(name, originalValue, description, null); }
    public static IndexCache create(String name, String originalValue, String description, SettingExtendedOld settingHolder) {
        if(name == null) return null;
        IndexCache item = new IndexCache();
        String nameTrimmed = Str.trimControlChars(name);
        String indexString = String.valueOf(item.index);
        item.index = Str.getEndInteger(nameTrimmed, 0);
        item.name = name;
        item.nameWithoutIndex = nameTrimmed.endsWith(indexString) ? Str.trimEx(nameTrimmed.substring(0, nameTrimmed.length() - indexString.length()), ".", " ") : nameTrimmed;
        String cleanName = SettingUtil.cleanSettingName(nameTrimmed);
        item.friendlyName = cleanName.endsWith(indexString) ? Str.trimEx(cleanName.substring(0, cleanName.length() - indexString.length()), ".", " ") : cleanName;
        item.originalValue = originalValue;
        item.category = Str.getFirstString(name, ".");
        item.description = description;
        item.updateSettingHolder(settingHolder);
        return item;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(!this.isValid()) return false;
        if(obj instanceof String) return ((String)obj).equalsIgnoreCase(this.name);
        if(obj instanceof IndexCache) {
            IndexCache toCompare = (IndexCache) obj;
            if(!toCompare.isValid()) return false;
            return this.name.equalsIgnoreCase(toCompare.name) && this.friendlyName.equalsIgnoreCase(toCompare.friendlyName) && this.index == toCompare.getIndex();
        }

        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Index", this.index)
                .appendFieldLine("Name", this.name)
                .appendFieldLine("Category", this.category)
                .appendFieldLine("Friendly Name", this.friendlyName)
                .appendFieldLine("Name Without Index", this.nameWithoutIndex)
                .appendFieldLine("Description", this.description)
                .appendDividerTitleLine("Original Value")
                .appendLine(this.originalValue)
                .appendDividerTitleLine("New Value")
                .appendLine(this.newValue)
                .appendDividerLine()
                .appendFieldLine("IsValid", this.isValid())
                .appendFieldLine("IsParent", this.isParent())
                .appendFieldLine("IsChild", this.isChild())
                .toString(true);
    }
}
