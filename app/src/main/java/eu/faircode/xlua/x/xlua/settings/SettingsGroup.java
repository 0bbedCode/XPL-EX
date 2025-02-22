package eu.faircode.xlua.x.xlua.settings;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.core.interfaces.IDiffFace;

public class SettingsGroup implements IDiffFace {
    public static SettingsGroup create(String groupName) { return new SettingsGroup(groupName); }
    public static SettingsGroup create(String groupName, List<SettingsContainer> containers) { return new SettingsGroup(groupName, containers); }

    private final String groupName;
    private final List<SettingsContainer> containers = new ArrayList<>();

    public final GroupStats groupStats = new GroupStats();



    public String getGroupName() { return this.groupName; }
    public List<SettingsContainer> getContainers() { return this.containers; }

    public SettingsGroup(String groupName) { this.groupName = groupName; }
    public SettingsGroup(String groupName, List<SettingsContainer> containers) {
        this.groupName = groupName;
        ListUtil.addAllIfValid(this.containers, containers);
    }

    public static List<SettingsGroup> categorizeIntoGroups(List<SettingsContainer> containers) {
        Map<String, List<SettingsContainer>> groups = new HashMap<>();
        for(SettingsContainer container : containers) {
            List<SettingsContainer> groupContainers = groups.get(container.getGroup());
            if(groupContainers == null) {
                groupContainers = new ArrayList<>();
                groupContainers.add(container);
                groups.put(container.getGroup(), groupContainers);
            } else {
                groupContainers.add(container);
            }
        }

        List<SettingsGroup> finalizedGroups = new ArrayList<>();
        for(Map.Entry<String, List<SettingsContainer>> categorized : groups.entrySet()) {
            List<SettingsContainer> cons = categorized.getValue();
            sortSettingsContainers(cons);
            SettingsGroup group = new SettingsGroup(categorized.getKey(), cons);
            finalizedGroups.add(group);
        }

        //Re organize this to

        return finalizedGroups;
    }

    private static int getPriority(String name) {
        // Handle parent.control anywhere in the string
        String low = name.toLowerCase();
        if (low.contains("parent.control")) return 0;
        if (low.contains("allowed.list") || low.contains("allow.list") || name.contains("block.list") || name.contains("blocked.list")) return 1;
        return 2;
    }

    public static void sortSettingsContainers(List<SettingsContainer> containers) {
        Collections.sort(containers, new Comparator<SettingsContainer>() {
            @Override
            public int compare(SettingsContainer o1, SettingsContainer o2) {
                String name1 = o1.getNameInformation().name;
                String name2 = o2.getNameInformation().name;

                int priority1 = getPriority(name1);
                int priority2 = getPriority(name2);

                // If priorities are different, sort by priority
                if (priority1 != priority2) {
                    return Integer.compare(priority1, priority2);
                }

                // If priorities are the same, sort alphabetically
                return name1.compareToIgnoreCase(name2);
            }
        });
    }

    @Override
    public boolean areItemsTheSame(IDiffFace newItem) {
        // Current incorrect implementation compares against SettingsContainer
        return newItem instanceof SettingsGroup &&
                this.getGroupName().equalsIgnoreCase(((SettingsGroup) newItem).getGroupName());
    }

    @Override
    public boolean areContentsTheSame(IDiffFace newItem) {
        if(newItem instanceof SettingsGroup) {
            SettingsGroup other = (SettingsGroup) newItem;
            return this.containers.size() == other.containers.size() &&
                    this.groupName.equalsIgnoreCase(other.groupName);
        }
        return false;
    }

    @Override
    public Object getChangePayload(IDiffFace newItem) {
        if (!(newItem instanceof SettingsGroup)) return null;
        SettingsGroup other = (SettingsGroup) newItem;

        Bundle diff = new Bundle();
        if (!this.groupName.equals(other.groupName)) {
            diff.putString("groupName", other.groupName);
        }
        if (this.containers.size() != other.containers.size()) {
            diff.putBoolean("containersSizeChanged", true);
        }
        return diff.isEmpty() ? null : diff;
    }

    @Override
    public boolean isValid() {
        return groupName != null && !containers.isEmpty();
    }
}
