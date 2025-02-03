package eu.faircode.xlua.x.xlua.settings.test;

public class ItemState {
    public static ItemState create(boolean isChecked, boolean isExpanded) { return new ItemState(isChecked, isExpanded); }

    public final boolean isChecked;
    public final boolean isExpanded;
    public ItemState(boolean isChecked, boolean isExpanded) {
        this.isChecked = isChecked;
        this.isExpanded = isExpanded;
    }
}