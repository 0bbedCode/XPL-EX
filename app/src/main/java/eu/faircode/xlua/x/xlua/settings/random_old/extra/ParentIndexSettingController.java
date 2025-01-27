package eu.faircode.xlua.x.xlua.settings.random_old.extra;

import java.util.List;

import eu.faircode.xlua.x.xlua.settings.deprecated.SettingExtendedOld;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomIndexer;

public class ParentIndexSettingController extends SettingExtendedOld {
    public static ParentIndexSettingController create(String category, List<SettingExtendedOld> controlParents) { return new ParentIndexSettingController(category, controlParents); }

    public ParentIndexSettingController(String category, List<SettingExtendedOld> controlParents) {
        super(controlParents, true);
        this.bindRandomizer(RandomIndexer.create(category, controlParents));
        //Make sure names everything is all fine
    }
}
