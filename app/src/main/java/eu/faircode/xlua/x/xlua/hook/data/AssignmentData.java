package eu.faircode.xlua.x.xlua.hook.data;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.x.ui.core.view_registry.SettingSharedRegistry;

/*
        ToDo: build off of this more pls
     */
public class AssignmentData {
    public static final AssignmentData DEFAULT = new AssignmentData();
    public static final String EMPTY_STRING = "---";

    public List<AssignmentState> states = new ArrayList<>();
    public int enabled = 0;
    public int total = 0;
    public boolean hasInit = false;

    private String statsString = null;

    public AssignmentData() { states = new ArrayList<>(); }
    public AssignmentData(int size) { states = new ArrayList<>(size); }

    public void refresh() {
        if(states == null)
            states = new ArrayList<>();

        states.clear();
        enabled = 0;
        total = 0;
        hasInit = false;
        statsString = null;
    }

    public void addAssignment(AssignmentState assignment) {
        if(assignment != null && !states.contains(assignment)) {
            states.add(assignment);
            total++;
            if(assignment.enabled)
                enabled++;

            if(!hasInit)
                hasInit = true;
        }
    }

    @NonNull
    @Override
    public String toString() {
        if(total <= 0)
            return EMPTY_STRING;

        if(TextUtils.isEmpty(statsString))
            statsString = enabled + "/" + total;

        return statsString;
    }
}