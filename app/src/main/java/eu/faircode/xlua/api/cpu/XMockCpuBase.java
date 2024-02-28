package eu.faircode.xlua.api.cpu;

import androidx.annotation.NonNull;



public class XMockCpuBase {
    protected String name;
    protected String model;
    protected String manufacturer;
    protected String contents;
    protected Boolean selected;

    public XMockCpuBase() { }
    public XMockCpuBase(String name, String model, String manufacturer, String contents, Boolean selected) {
        setName(name);
        setModel(model);
        setManufacturer(manufacturer);
        setContents(contents);
        setSelected(selected);
    }

    public XMockCpuBase setName(String name) {
        if(name != null) this.name = name;
        return this;
    }

    public XMockCpuBase setModel(String model) {
        if(model != null) this.model = model;
        return this;
    }

    public XMockCpuBase setManufacturer(String manufacturer) {
        if(manufacturer != null) this.manufacturer = manufacturer;
        return this;
    }

    public XMockCpuBase setContents(String contents) {
        if(contents != null) this.contents = contents;
        return this;
    }

    public XMockCpuBase setSelected(Boolean selected) {
        if(selected != null) this.selected = selected;
        return this;
    }

    public String getName() { return this.name; }
    public String getModel() { return this.model; }
    public String getManufacturer() { return this.manufacturer; }
    public String getContents() { return this.contents; }
    public Boolean getSelected() { return this.selected; }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof XMockCpuBase))
            return false;
        XMockCpuBase other = (XMockCpuBase) obj;
        return this.getName().equals(other.getName());
    }

    @NonNull
    @Override
    public String toString() {
        return name + "::" + model;
    }
}
