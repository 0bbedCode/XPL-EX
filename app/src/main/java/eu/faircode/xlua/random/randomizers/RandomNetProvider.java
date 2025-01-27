package eu.faircode.xlua.random.randomizers;

import android.text.Editable;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.DataNullElement;
import eu.faircode.xlua.random.elements.DataStringElement;
//import eu.faircode.xlua.x.random.ILinkParent;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.network.NetInfoGenerator;

/*public class RandomNetProvider implements IRandomizerOld, ILinkParent {
    private final List<ISpinnerElement> providers = new ArrayList<>();
    public RandomNetProvider() {
        providers.add(DataNullElement.EMPTY_ELEMENT);
        for(String e : NetInfoGenerator.PROVIDERS) providers.add(DataStringElement.create(e));
    }

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() {  return "network.parent.control.isp"; }

    @Override
    public String getName() {
        return "Network ISP";
    }

    @Override
    public String getID() {
        return "%network_isp%";
    }

    @Override
    public String generateString() { return providers.get(RandomGenerator.nextInt(1, providers.size())).getValue(); }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return this.providers; }

    @NonNull
    @Override
    public String toString() { return getName(); }

    @Override
    public List<LuaSettingExtended> randomizeSettings(List<LuaSettingExtended> settings) {
        List<LuaSettingExtended> newList = new ArrayList<>();
        NetInfoGenerator gen = new NetInfoGenerator();
        for(LuaSettingExtended s : settings) {
            String name = s.getName();
            if(!name.startsWith("__deprecated/network")) {
                newList.add(s);
                continue;
            }

            if(!s.isSameNameAsDisplayCache()) {
                Editable e = s.getInputTextBox() != null ? s.getInputTextBox().getText() : null;
                if(DebugUtil.isDebug())
                    Log.d("XLua.RandomNetProvider", "Setting Name=" + s.getName() + " V=" + s.getValue() + " M=" + s.getModifiedValue() + " T=" + (e == null ? "null" : e.toString()));

                newList.add(s);
                continue;
            }

            if(name.equalsIgnoreCase(getSettingName())) {
                s.setModifiedValue(gen.getProvider(), true);
            } else {
                switch (name) {
                    case "network.dns":
                        s.setModifiedValue(gen.getDnsServers().get(0), true);
                        break;
                    case "network.dns.list":
                        s.setModifiedValue(Str.joinList(gen.getDnsServers(), ","), true);
                        break;
                    case "network.domains":
                        s.setModifiedValue(gen.getDomain(), true);
                        break;
                    case "network.gateway":
                        s.setModifiedValue(gen.getGateway(), true);
                        break;
                    case "network.host.name":
                    case "network.host.address":
                        s.setModifiedValue(gen.getIpv4Address(), true);
                        break;
                    case "network.netmask":
                        s.setModifiedValue(gen.getNetmask(), true);
                        break;
                    case "network.routes":
                        s.setModifiedValue(Str.joinList(gen.getRoutes(), ","), true);
                        break;
                    case "network.dhcp.server":
                        s.setModifiedValue(gen.getDhcpServer(), true);
                        break;
                    default:
                        newList.add(s);
                        break;
                }
            }
        }

        return newList;
    }
}*/
