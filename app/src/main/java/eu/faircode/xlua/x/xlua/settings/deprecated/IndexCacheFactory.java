package eu.faircode.xlua.x.xlua.settings.deprecated;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;

public class IndexCacheFactory {
    private static final String TAG = "XLua.IndexCacheFactory";
    //Have a system so it knows if its not a parent then instead of completing or creating the SettingsExtended or something
    //The IndexCache gets Put into a Que awaiting its Parent, so wait til someone claims it
    //Hash Map of Categories or Friendly Names, List for the Value once it finds
    //Something like that lets cook
    //Add Condition setter for settings maybe ??
    //
    //Make the Code to set the setting color in the UiComponentHolder, and rename it to UiSettingComponentHolder
    //
    //Maybe maybe also be able to control index for each setting on the actual setting if you don't want to use the index control ?
    //So Have some Popup thing with check boxes u can check all or some that relate to the setting in question, this is PERFECT !!!
    //
    //Make a String Function to Iterate through a Collection/ Array of Objects them toString them all with Dividers
    //
    //Have further item check , ye sure they r not null but IValidator.isValid() ?
    //

    private final Map<Integer, IndexCache> mIndexCache = new HashMap<>();
    private IndexCache mParentIndex;
    private IndexCache mCurrentIndex;
    private int mFactoryHash = 0;

    public boolean hasParent() { synchronized (mIndexCache) { return internalHasParent(); } }
    public boolean hasCurrent() { synchronized (mIndexCache) { return internalHasCurrent(); } }
    public boolean hasHash() { synchronized (mIndexCache) { return internalHasHash(); } }

    public boolean isCurrentParent() { synchronized (mIndexCache) { return internalIsCurrentParent(); } }
    public boolean isCurrentChild() { synchronized (mIndexCache) { return internalIsCurrentChild(); } }

    public int getCurrentIndex() { synchronized (mIndexCache) { return internalGetCurrentIndex(); } }
    public IndexCache getCurrent() { synchronized (mIndexCache) { return internalGetCurrent(); } }
    public IndexCache getParent() { synchronized (mIndexCache) { return internalGetParent(); } }

    public Map<Integer, IndexCache> getIndexCaches() { synchronized (mIndexCache) { return mIndexCache; } }

    public String getGroupName() { synchronized (mIndexCache) { return internalGetGroupName(); } }

    private boolean isFactoryReady() { synchronized (mIndexCache) { return internalEnsureFactoryIsReady(); } }

    public boolean isChildOfThisFactory(IndexCache indexCache) { synchronized (mIndexCache) { return internalIsChildOfThisFactory(indexCache); } }
    public boolean isCurrentIndex(int index) { synchronized (mIndexCache) { return internalIsCurrentIndex(index); } }

    public IndexCacheFactory() { }
    public IndexCacheFactory(IndexCache parent, SettingExtendedOld parentHolder) {
        if(ensureIsCached(parent) && parentHolder != null) {
            ensureSettingHolderIsUpdated(parentHolder);
        }
    }

    public void ensureSettingHolderIsUpdated(SettingExtendedOld settingHolder) { synchronized (mIndexCache) { internalEnsureSettingHolderIsAligned(settingHolder); } }
    public void ensureCacheIsUpdated(SettingExtendedOld settingHolder) { synchronized (mIndexCache) { internalEnsureCacheIsAligned(settingHolder, false); } }

    public boolean performIndexUpdate(int newIndex) { return performIndexUpdate(newIndex, null, true); }
    public boolean performIndexUpdate(int newIndex, SettingExtendedOld settingHolder) { return performIndexUpdate(newIndex, settingHolder, false); }
    public boolean performIndexUpdate(int newIndex, SettingExtendedOld settingHolder, boolean ignoreCacheAlignment) { synchronized (mIndexCache) { return internalPerformIndexUpdate(newIndex, settingHolder, ignoreCacheAlignment); } }

    public boolean ensureIsCached(IndexCache indexItem) { return ensureIsCached(indexItem, false); }
    public boolean ensureIsCached(IndexCache indexItem, boolean forceOverride) {
        if(!SettingsHelperOld.isIndexCacheValid(indexItem)) {
            Log.w(TAG, "Index Cache Item is Invalid or Null, Factory with the name of [" + internalGetGroupName() + "] with hash [" + mFactoryHash + "] Rejected the invalid Index Cache Item:\n" + Str.toStringOrNull(indexItem));
            return false;
        }

        synchronized (mIndexCache) {
            if(!internalHasParent() && !SettingsHelperOld.isIndexCacheParent(indexItem)) {
                Log.w(TAG, "Index Factory is Missing a Parent Index Cached Item and the given Index Cached Item to ensure is cached if part of group is not a parent! Given Index Cache Item:\n" + Str.toStringOrNull(indexItem));
                return false;
            }

            if(!internalIsChildOfThisFactory(indexItem)) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Given Index Cached Item to ensure is Cached if is part of group is not part of this Factory's Group. This Factory's Group Name [" + internalGetGroupName() + "] and Hash [" + mFactoryHash + "], given Index Item Cache:\n" + Str.toStringOrNull(indexItem));

                return false;
            }

            int index = indexItem.getIndex();
            if(DebugUtil.isDebug())
                Log.d(TAG, "Given Index Cached Item Qualifies for this Factory. Item:\n" + Str.toStringOrNull(indexItem) + "\n" + this);

            if(!forceOverride && mIndexCache.containsKey(index)) {
                Log.w(TAG, "Given Index Cached Item is Already added to this Factory! Item:" + Str.toStringOrNull(indexItem) + "\n" + this);
                return false;
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Given Index Cached Item Does not exist, adding to this Factory! Item:" + Str.toStringOrNull(indexItem) + "\n" + this);

            mIndexCache.put(index, indexItem);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Added Given Index Cached Item to this Factory! Item:\n" + Str.toStringOrNull(indexItem) + "\n" + this);

            if(indexItem.isParent()) {
                if(!internalHasParent()) {
                    mParentIndex = indexItem;
                    mFactoryHash = indexItem.getGroupHashCode();
                    //Init settings Holder
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Updated Factory Hash to: " + mFactoryHash + " and Parent Cache Index Item:\n" + Str.toStringOrNull(mParentIndex) + "\n" + this);
                }

                if(!internalHasCurrent()) {
                    mCurrentIndex = indexItem;
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Updated Factory Current Cache Index Item:" + Str.toStringOrNull(mCurrentIndex) + "\n" + this);
                }
            }

            return true;
        }
    }

    public boolean hasIndex(IndexCache indexItem) { return SettingsHelperOld.isIndexCacheValid(indexItem) && hasIndex(indexItem.getIndex()); }
    public boolean hasIndex(int index) {
        if(index < 0) return false;
        synchronized (mIndexCache) { return mIndexCache.containsKey(index); }
    }

    public IndexCache getIndex(int index) {
        if(index < 0) return null;
        synchronized (mIndexCache) { return mIndexCache.get(index); }
    }

    private IndexCache internalGetCurrent() { return mCurrentIndex; }
    private IndexCache internalGetParent() { return mParentIndex; }

    private String internalGetGroupName() { return internalHasParent() ? mParentIndex.friendlyName : ""; }
    private int internalGetCurrentIndex() { return SettingsHelperOld.getIndexFromCache(mCurrentIndex); }
    private boolean internalIsCurrentChild() { return SettingsHelperOld.isIndexCacheChild(mCurrentIndex); }
    private boolean internalIsCurrentParent() { return SettingsHelperOld.isIndexCacheParent(mCurrentIndex); }
    private boolean internalHasHash() { return mFactoryHash > 0; }
    private boolean internalIsChildOfThisFactory(IndexCache indexCache) {  return internalHasParent() && internalHasHash() && SettingsHelperOld.isIndexCacheParentsChild(mParentIndex, indexCache); }
    private boolean internalHasParent() {  return mParentIndex != null && SettingsHelperOld.isIndexCacheParent(mParentIndex);  }
    private boolean internalHasCurrent() { return SettingsHelperOld.isIndexCacheValid(mCurrentIndex); }

    private boolean internalIsCurrentIndex(int index) { return internalGetCurrentIndex() == index; }


    private boolean internalPerformIndexUpdate(int newIndex, SettingExtendedOld settingHolder) { return internalPerformIndexUpdate(newIndex, settingHolder, false); }
    private boolean internalPerformIndexUpdate(int newIndex, SettingExtendedOld settingHolder, boolean ignoreCacheAlignment) {
        if(newIndex < 0) {
            Log.e(TAG, "The Index Requested from the Factory [" + newIndex + "] is Invalid!\n" + this);
            return false;
        }

        if(newIndex == internalGetCurrentIndex()) {
            Log.w(TAG, "The Index of wanted Update [" + newIndex + "] is Already set as the Current Index, ignoring...\n" + this);
            return false;
        }

        if(!internalEnsureFactoryIsReady())
            return false;

        if(!mIndexCache.containsKey(newIndex)) {
            Log.w(TAG, "Factory Does not Contain wanted Index [" + newIndex + "]\n" + this);
            return false;
        }

        IndexCache newCurrent = mIndexCache.get(newIndex);
        if(newCurrent == null) {
            Log.e(TAG, "Factory Critical Error, Some how the Index Cache Item is null ??? Index=" + newIndex + "\n" + this + "\nSetting Holder:" + Str.toStringOrNull(settingHolder));
            return false;
        }

        internalEnsureCacheIsAligned(settingHolder, ignoreCacheAlignment);
        if(DebugUtil.isDebug())
            Log.d(TAG, "Factory Performing a Index Cache Update! Old:\n" + Str.toStringOrNull(mCurrentIndex) + "\nNew:" + Str.toStringOrNull(newCurrent));

        mCurrentIndex = newCurrent;
        internalEnsureSettingHolderIsAligned(settingHolder);
        if(DebugUtil.isDebug())
            Log.d(TAG, "Finished Performing a Factory Index Cache Update!\n" + this);

        return true;
    }

    private void internalEnsureSettingHolderIsAligned(SettingExtendedOld settingHolder) {
        if(internalHasCurrent() && settingHolder != null) {
            mCurrentIndex.updateSettingHolder(settingHolder);
        }
    }

    private void internalEnsureCacheIsAligned(SettingExtendedOld settingsHolder, boolean ignoreCacheAlignment) {
        if(!ignoreCacheAlignment && internalHasCurrent() && settingsHolder != null && mCurrentIndex.isDifferentValues(settingsHolder)) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Updating Factory Cache to Reflect of the Holder, Factory Group Name [" + internalGetGroupName() + "] " +
                        "hash [" + mFactoryHash + "] " +
                        "Item at Index [" + mCurrentIndex.getIndex() + "] " +
                        "with the Name of [" + mCurrentIndex.getName() + "] " + StrBuilder.create().ensureOneNewLinePer(true)
                        .appendFieldLine("Old Original Value", mCurrentIndex.getOriginalValue())
                        .appendFieldLine("Old New Value", mCurrentIndex.getNewValue())
                        .appendFieldLine("New Original Value", settingsHolder.originalValue)
                        .appendFieldLine("New New Value", settingsHolder.newValue)
                        .toString(true));

            mCurrentIndex.originalValue = settingsHolder.originalValue;
            mCurrentIndex.newValue = settingsHolder.newValue;
        }
    }

    private boolean internalEnsureFactoryIsReady() {
        if(mParentIndex == null) {
            if(mCurrentIndex.isParent()) {
                mParentIndex = mCurrentIndex;
                mFactoryHash = mCurrentIndex.getGroupHashCode();
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Ensured check fixed errors to Update Factory Hash to: " + mFactoryHash + " and Parent Cache Index Item:\n" + Str.toStringOrNull(mParentIndex) + "\n" + this);
            } else {
                Log.e(TAG, "Critical Error in Factory, its Missing a Parent Index Cached Item ? Requires a parent!\n" + this);
                return false;
            }
        }

        if(mCurrentIndex == null) {
            this.mCurrentIndex = mParentIndex;
            if(DebugUtil.isDebug())
                Log.d(TAG, "Ensured Check fixed errors to Update Factory Current Cache Index Item:" + Str.toStringOrNull(mCurrentIndex) + "\n" + this);
        }

        return true;
    }

    @Override
    public int hashCode() { return mFactoryHash; }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendDividerTitleLine("Factory")
                .appendField("Group Name", internalGetGroupName())
                .appendField("Group Hash", mFactoryHash)
                .appendField("Index Count", mIndexCache.size())
                .appendFieldLine("Current Index", internalGetCurrentIndex())
                .appendField("Has Parent", internalHasParent())
                .appendDividerTitleLine("Parent Index")
                .appendLine(Str.toStringOrNull(mParentIndex))
                .appendDividerTitleLine("Current Index")
                .appendField("Has Current", internalHasCurrent())
                .appendDividerLine()
                .appendLine(Str.toStringOrNull(mCurrentIndex))
                .toString(true);
    }
}
