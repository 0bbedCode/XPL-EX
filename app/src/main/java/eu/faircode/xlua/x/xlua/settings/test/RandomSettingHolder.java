package eu.faircode.xlua.x.xlua.settings.test;

import android.content.Context;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;

//we got this
//Shared reg
//and how "assignments" are handled
//Some app system with events dunno

public class RandomSettingHolder {
    public IRandomizer randomizer;
    public SettingHolder holder;

    private boolean _blockUpdate = false;
    private boolean _wasRandomized = false;
    private String randomizeValue;

    public boolean wasRandomized() { return _wasRandomized; }

    public void setBlockUpdate(boolean blockUpdate) {
        this._blockUpdate = blockUpdate;
    }

    public void ensureEmpty() {
        this._wasRandomized  = false;
        //this._blockUpdate = false;
        this.randomizeValue = null;
    }

    public void setValue(String value) {
        _wasRandomized = true;
        randomizeValue = value;
    }

    public void setValueEx(String value) {
        if(!_wasRandomized) {
            _wasRandomized = true;
            randomizeValue = value;
        }
    }

    public String getValue(boolean getOriginalValueIfRandomValueIsNull) {
        return this._wasRandomized ? this.randomizeValue : this.holder.getValue();
    }

    public void updateHolder(boolean updateEvenIfNull, boolean ensureUIUpdated, Context context, SharedRegistry sharedRegistry) {
        if(!this._blockUpdate) {
            if(this.holder != null) {
                if(updateEvenIfNull || this.randomizeValue != null) {
                    this.holder.setNewValue(this.randomizeValue);
                    if(ensureUIUpdated) {
                        this.holder.ensureUiUpdated(this.randomizeValue == null ? Str.EMPTY : this.randomizeValue);
                        this.holder.setNameLabelColor(context);
                        this.holder.notifyUpdate(sharedRegistry);
                    }
                }
            }
        }
    }

    public boolean hasRandomizer() { return randomizer != null; }
    public boolean isParentRandomizer() { return randomizer != null && randomizer.isParentControl(); }

    public String id() { return holder != null ? holder.getObjectId() : null; }
    public String name() { return holder != null ? holder.getName() : null; }

    public boolean isChecked(SharedRegistry sharedRegistry) { return sharedRegistry != null && sharedRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, holder.getObjectId()); }
    public boolean isChecked(SharedViewControl sharedViewControl) { return sharedViewControl != null && sharedViewControl.isChecked(SharedViewControl.G_SETTINGS, holder.getObjectId()); }
}
