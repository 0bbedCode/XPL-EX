package eu.faircode.xlua.x.hook.filter;

public class SettingPair {
    public String name;
    public String settingName;

    public SettingPair(String name, int index, String[] settings) {
        this.name = name;
        if(settings != null) {
            if(settings.length > index) {
                settingName = settings[index];
            } else {
                settingName = settings[0];
            }
        }
    }
}