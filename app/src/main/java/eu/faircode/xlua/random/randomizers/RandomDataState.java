package eu.faircode.xlua.random.randomizers;

import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.DataNullElement;
import eu.faircode.xlua.random.elements.DataNameValueElement;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomDataState implements IRandomizerOld {
    private static final List<Integer> dataCodes33Higher = Arrays.asList(
            -1,                                     //DATA_ENABLED_REASON_UNKNOWN           (Api 33)
            TelephonyManager.DATA_DISCONNECTED,     //DATA_ENABLED_REASON_USER              (Api 1) DATA_DISCONNECTED   | (Api 31) DATA_ENABLED_REASON_USER
            1,                                      //DATA_ENABLED_REASON_POLICY            (Api 1) DATA_CONNECTING     | (Api 31) DATA_ENABLED_REASON_POLICY
            2,                                      //DATA_ENABLED_REASON_CARRIER           (Api 1) DATA_CONNECTED      | (Api 31) DATA_ENABLED_REASON_CARRIER
            TelephonyManager.DATA_SUSPENDED,        //DATA_ENABLED_REASON_THERMAL           (Api 1) DATA_SUSPENDED      | (Api 31) DATA_ENABLED_REASON_THERMAL
            4,                                      //DATA_DISCONNECTING                    (Api 4) DATA_ACTIVITY_DORMANT | (Api 30) DATA_DISCONNECTING
            5                                       //DATA_HANDOVER_IN_PROGRESS             (Api 33)
    );

    private final List<ISpinnerElement> dataStates = new ArrayList<>();
    public RandomDataState() {
        int code = android.os.Build.VERSION.SDK_INT;
        dataStates.add(DataNullElement.EMPTY_ELEMENT);
        if(code >= 33) dataStates.add(DataNameValueElement.create("DATA_ENABLED_REASON_UNKNOWN", -1));
        dataStates.add(DataNameValueElement.create(code >= 31 ? "DATA_ENABLED_REASON_USER" : "DATA_DISCONNECTED", 0));
        dataStates.add(DataNameValueElement.create(code >= 31 ? "DATA_ENABLED_REASON_POLICY" : "DATA_CONNECTING", 1));
        dataStates.add(DataNameValueElement.create(code >= 31 ? "DATA_ENABLED_REASON_CARRIER" : "DATA_CONNECTED", 2));
        dataStates.add(DataNameValueElement.create(code >= 31 ? "DATA_ENABLED_REASON_THERMAL" : "DATA_SUSPENDED", 3));
        dataStates.add(DataNameValueElement.create(code >= 30 ? "DATA_DISCONNECTING" : "DATA_ACTIVITY_DORMANT", 4));
        if(code >= 33) dataStates.add(DataNameValueElement.create("DATA_HANDOVER_IN_PROGRESS", 5));
    }

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() {  return "gsm.data.state"; }

    @Override
    public String getName() {
        return "GSM Data State";
    }

    @Override
    public String getID() {
        return "%gsm_data_state%";
    }

    @Override
    public String generateString() {
        ISpinnerElement el = dataStates.get(RandomGenerator.nextInt(1, dataStates.size()));
        return el.getValue();
    }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return this.dataStates; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}
