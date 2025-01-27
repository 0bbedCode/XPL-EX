package eu.faircode.xlua.x.xlua.settings.interfaces;

public interface IUiElement {
    String getLabelText();
    boolean hasSubLabel();
    String getSubLabelText();
    boolean hasDescription();
    String getDescription();

    void ensureHasDescription(String description);
}
