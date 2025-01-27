package eu.faircode.xlua.x.ui.adapters;

import java.util.Map;
import java.util.WeakHashMap;

public class ExpandableStateFactory {
    ///public interface

    public static ExpandableStateFactory create() { return new ExpandableStateFactory(); }
    public static final ObjectState UNKNOWN = ObjectState.EXPANDED_UNKNOWN;
    public static final ObjectState EXPANDED_TRUE = ObjectState.STATE_TRUE;
    public static final ObjectState EXPANDED_FALSE = ObjectState.STATE_FALSE;

    private final Map<String, ObjectState> elements = new WeakHashMap<>();
    //private final ExpandableState DEFAULT_IF_NULL_OPPOSITE;
    //lets keep it simple for now

    //lets work off of a call back system


    //We can invoke "onDataStateChanged(something, currentState)" ?



    public ObjectState get(String elementId) { return elements.get(elementId); }
    public boolean has(String elementId) { return elements.containsKey(elementId); }

    public void setTrue(String elementId) { elements.put(elementId, EXPANDED_TRUE); }
    public void setFalse(String elementId) { elements.put(elementId, EXPANDED_FALSE); }
    public void putOrSet(String elementId, ObjectState state) { elements.put(elementId, state); }



    /*public ExpandableStateFactory(ExpandableState defaultIfNull) { DEFAULT_IF_NULL_OPPOSITE = defaultIfNull; }

    //public void flip(String elementId) { flip(elementId, DEFAULT_IF_NULL_OPPOSITE); }

    public void flip(IExpandable expandable, ExpandableState oppositeOfState) {

    }


    public void flip(String elementId) { flip(elementId, get(elementId)); }
    public void flip(String elementId, ExpandableState oppositeOfState) {
        if(elementId == null) return;
        ExpandableState opFlag = oppositeOfState.toOpposite(DEFAULT_IF_NULL_OPPOSITE);
        set(elementId, opFlag);
    }


    /*public boolean isExpanded(IExpandable expandable) { return isExpanded(expandable.getId()); }
    public boolean isExpanded(String elementId) {
        Boolean val = elements.get(elementId);
        return val != null && !Boolean.FALSE.equals(val);
    }*/

    //public boolean */

    /*public boolean set(IExpandable expandable, boolean isExpanded) { return set(expandable.getId(), isExpanded); }
    public boolean set(String elementId, boolean isExpanded) {
        if(elementId == null) return false;
        Boolean oldValue = elements.get(elementId);
        elements.put(elementId, isExpanded);
        return oldValue != null && oldValue;    //Return the Old Value, as you are setting the "new" value
    }

    public ExpandableState flip(String elementId, boolean ifValueIsNullValue) {
        if(elementId == null) return UNKNOWN;   //False
        ExpandableState currentState = ExpandableState.of(elements.get(elementId));
        boolean value = currentState.toBoolValue(ifValueIsNullValue);

        if(currentState == UNKNOWN) {
            elements.put()
        }

    }


    public ExpandableState flipUnless(String elementId, boolean unlessNullValue, boolean returnOld) {
        if(elementId == null) return UNKNOWN;
        ExpandableState value = ExpandableState.of(elements.get(elementId));
        boolean op = value.toOpposite();

        elements.put(elementId, op);



        if(value == UNKNOWN) {
            elements.put(elementId, unlessNullValue);
            return returnOld ? value : ExpandableState.of(unlessNullValue);
        }


        if(value == EXPANDED) {
            elements.put(elementId, false);
            return returnOld ?
        }


        if(value == null) {
            elements.put(elementId, unlessNullValue);
            return returnOld ? state : ExpandableState.;
        }

    }

    public boolean flipUnlessReturnOld(String elementId, boolean unlessNullValue) {
        if(elementId == null) return false;
        Boolean oldValue = elements.get(elementId);
        if(oldValue == null) {
            elements.put(elementId, unlessNullValue);
            return unlessNullValue;
        }

        boolean newValue = !oldValue;
        elements.put(elementId, newValue);              //Swap flag from last / original State
        return oldValue;                                //We May want to Return Old Value, as we Don't know the Old Value
    }

    public boolean flipUnless(String elementId, boolean unlessNullValue) {
        if(elementId == null) return false;
        Boolean oldValue = elements.get(elementId);
        if(oldValue == null) {
            elements.put(elementId, unlessNullValue);
            return unlessNullValue;
        }

        boolean newValue = !oldValue;
        elements.put(elementId, newValue);              //Swap flag from last / original State
        return newValue;                                //Return the current value
    }*/
}
