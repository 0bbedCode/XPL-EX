package eu.faircode.xlua.x.xlua.settings.interfaces;

import eu.faircode.xlua.x.data.interfaces.IValidator;
import eu.faircode.xlua.x.xlua.settings.NameInformation;

public interface IValueDescriptor extends IValidator {
    String getName();
    String getNameNice();
    String getNameNiceNoNumericEnding();
    NameInformation getNameInformation();

    String getValue();
    String getNewValue();
    String getDescription();
}
