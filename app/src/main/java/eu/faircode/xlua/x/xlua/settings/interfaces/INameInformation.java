package eu.faircode.xlua.x.xlua.settings.interfaces;

import java.util.List;

import eu.faircode.xlua.x.data.interfaces.IValidator;
import eu.faircode.xlua.x.xlua.settings.NameInformation;

public interface INameInformation extends IValidator {
    boolean hasNameInformation();
    NameInformation getNameInformation();

    int getIndex();
    String getName();
    String getNameNice();
    String getNameNiceNoNumericEnding();

    String getGroup();

    boolean hasParent();
    boolean hasChildren();
    List<NameInformation> getChildrenNames();

    NameInformation getParentNameInformation();
    String getParentName();
    String getParentNameNice();
    String getParentNameNiceNoNumericEnding();
}
