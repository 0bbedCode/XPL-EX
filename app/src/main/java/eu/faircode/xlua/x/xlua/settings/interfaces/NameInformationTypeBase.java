package eu.faircode.xlua.x.xlua.settings.interfaces;

import java.util.List;

import eu.faircode.xlua.x.xlua.settings.NameInformation;

public class NameInformationTypeBase implements INamedObjectTest {
    protected NameInformation nameInformation;

    public NameInformationTypeBase() { }
    public NameInformationTypeBase(NameInformation nameInformation) { this.nameInformation = nameInformation; }

    protected void bindNameInformation(NameInformation nameInformation) { if(nameInformation != null) this.nameInformation = nameInformation; }
    protected void bindNameInformationIfNull(NameInformation nameInformation) { if(nameInformation != null && this.nameInformation == null) this.nameInformation = nameInformation; }

    @Override
    public boolean hasNameInformation() { return nameInformation != null; }

    @Override
    public String getName() { return nameInformation.name; }

    @Override
    public String getNameNice() { return nameInformation.nameNice; }

    @Override
    public String getNameNiceNoNumericEnding() { return nameInformation.nameNiceNoNumericEnding; }

    @Override
    public String getGroup() { return nameInformation.group; }

    @Override
    public int getIndex() { return nameInformation.index; }

    @Override
    public boolean hasChildren() { return nameInformation.hasChildren(); }

    @Override
    public boolean hasParent() { return nameInformation.hasParent(); }

    @Override
    public List<NameInformation> getChildrenNames() { return nameInformation.getChildrenNames(); }

    @Override
    public NameInformation getParentNameInformation() { return nameInformation.parentNameInformation; }

    @Override
    public String getParentName() { return nameInformation.parentNameInformation.name; }

    @Override
    public String getParentNameNice() { return nameInformation.parentNameInformation.nameNice; }

    @Override
    public String getParentNameNiceNoNumericEnding() { return nameInformation.parentNameInformation.nameNiceNoNumericEnding; }

    @Override
    public NameInformation getNameInformation() { return nameInformation; }

    @Override
    public boolean isValid() { return nameInformation != null; }

    @Override
    public boolean hasPassed() {
        return false;
    }
}
