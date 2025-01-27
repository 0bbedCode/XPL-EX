package eu.faircode.xlua.x.xlua.settings;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.api.xmock.XMockQuery;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.settings.deprecated.IndexCache;
import eu.faircode.xlua.x.xlua.settings.deprecated.SettingExtendedOld;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomHelper;
import eu.faircode.xlua.x.xlua.settings.random_old.extra.ParentIndexSettingController;

public class SettingsRepo {
    private static final String TAG = "XLua.SettingsRepo";

    public static List<SettingExtendedOld> getSettings(Context context, int id, String category) {
        List<SettingExtendedOld> settings = new ArrayList<>();
        Map<Integer, List<IndexCache>> groups = new HashMap<>();
        Map<String, List<SettingExtendedOld>> indexControllers = new HashMap<>();
        //Bind randomizers here as well ???
        try {
            Collection<LuaSettingExtended> originalSettings = XMockQuery.getAllSettings(context, id, category);
            for(LuaSettingExtended set : originalSettings) {
                if(set != null && !TextUtils.isEmpty(set.getName())) {
                    if(set.isBuiltIn()) {
                        SettingExtendedOld setEx = new SettingExtendedOld(set);
                        settings.add(setEx);
                        RandomHelper.bindRandomizer(setEx);
                    } else {
                        IndexCache cache = IndexCache.create(set.getName(), set.getValue(), set.getDescription());
                        int groupHash = cache.getGroupHashCode();
                        List<IndexCache> group = groups.get(groupHash);
                        if(group == null) {
                            group = new ArrayList<>();
                            group.add(cache);
                            groups.put(groupHash, group);
                        } else {
                            if(!group.contains(cache)) {
                                group.add(cache);
                            }
                        }
                    }
                }
            }

            //
            //Parse all the Groups, if the Group via Hash has more than One Indexed Cached Item
            //That means that specific setting has multiple indexes, so we then need to create a General Category for all the Settings in the Category (GENERAL)
            //
            //Else if one than its just a single setting
            //
            for(Map.Entry<Integer, List<IndexCache>> cache : groups.entrySet()) {
                List<IndexCache> items = cache.getValue();
                if(items.size() == 1) {
                    SettingExtendedOld set = new SettingExtendedOld(items.get(0));
                    settings.add(set);
                    RandomHelper.bindRandomizer(set);
                } else if(items.size() > 1) {
                    Collections.sort(items, new Comparator<IndexCache>() {
                        @Override
                        public int compare(IndexCache o1, IndexCache o2) {
                            if (o1 == o2) return 0;
                            if (o1 == null) return -1;
                            if (o2 == null) return 1;
                            return Integer.compare(o1.index, o2.index);
                        }
                    });

                    SettingExtendedOld parentSetting = new SettingExtendedOld(items);
                    List<SettingExtendedOld> controlGroup = indexControllers.get(parentSetting.getCategory());
                    if(controlGroup == null) {
                        controlGroup = new ArrayList<>();
                        indexControllers.put(parentSetting.getCategory(), controlGroup);
                    }

                    controlGroup.add(parentSetting);
                }
            }

            //
            //Create the Index Control Settings for each GENERAL Category, then add the Control to the List of Settings and the actual Settings that have Indexes
            //
            for(Map.Entry<String, List<SettingExtendedOld>> controls : indexControllers.entrySet()) {
                List<SettingExtendedOld> parentIndexSettings = controls.getValue();
                settings.add(ParentIndexSettingController.create(controls.getKey(), parentIndexSettings));
                //For each of the Setting Extended find Randomizers for them as they are individual settings just with children aka indexes
                //So find Randomizer best fits it based off of Name Without Index
                for(SettingExtendedOld s : parentIndexSettings) RandomHelper.bindRandomizer(s);
                settings.addAll(parentIndexSettings);
            }

            return settings;
        }catch (Exception e) {
            Log.e(TAG, "Error Getting Settings, Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            return settings;
        }
    }
}
