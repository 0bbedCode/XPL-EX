package eu.faircode.xlua.x.network.randomizers;

import eu.faircode.xlua.x.network.NetInfoGenerator;
import eu.faircode.xlua.x.xlua.settings.SettingsContext;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomNetworkParentControl extends RandomElement {
    private final int SIM_CARD_COUNT = 2;
    public RandomNetworkParentControl() {
        super("");
        //isParentControl = true;
        //No parent setting
        //putSetting("network.parent.control"), we are the parent AND we dont need parents
        //Make sure to ensure single sim if device is
        //ye lets finish this next week
        //I see clear and the vision I got the foundation done
        //we do need to clean up these comments
        //but there are many ways to go about the task given our system but this class should represent a PoC layout
    }

    public boolean randomize(SettingsContext context, String callingSetting) {
        if(context.wasSettingRandomized(callingSetting)) return true;
        ensureParentIsHappy(context);
        context.putRandomized(callingSetting, "invoked", true);
        NetInfoGenerator netInformation = new NetInfoGenerator();
        //If this was dual settings we can still somewhat determine that from here
        //we know though network settings are one

        //Ah yes and ones for Index should be fine ?
        //I mean think about it the only indexable ones rn are CELL
        //We can careless if the TWO instances dont know each other as either way they dont related
        //Hmm either way we will have to generate two here
        //Then set (1) and (2)

        for(int i = 1; i < SIM_CARD_COUNT + 1; i++) {
            for(String setting : getSettings()) {
                String indexName = setting + "." + i;
                //We can either from here or caller remove the (2) index or (1) index ones from the Random list if they are not enabled, easy so many ways of doing this
                switch (setting) {
                    case "network.dhcp.server":
                        context.putRandomized(indexName, netInformation.getDhcpServer(), true);
                        break;
                    case "network.net.mask":
                        context.putRandomized(indexName, netInformation.getNetmask(), true);
                        break;
                }
            }
        }

        //FOR NON INDEXED TYPES
        for(String setting : getSettings()) {
            switch (setting) {
                case "network.dhcp.server":
                    context.putRandomized(setting, netInformation.getDhcpServer(), true);
                    break;
                case "network.net.mask":
                    context.putRandomized(setting, netInformation.getNetmask(), true);
                    break;
            }
        }

        //See this is all so easy i think thats why its confusing your system is too flexible allowing you to do the required tasks many ways that just one
        //this is the concept
        //Super easy
        //For the ones without parent just control them in base element
        //See this should be easy from here :P GG you got all the layout done
        //Now you need to actually start implementing this into the main System
        return false;
    }

}
