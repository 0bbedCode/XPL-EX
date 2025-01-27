package eu.faircode.xlua.x.hook.interceptors.hardware.bluetooth.random;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomBluetoothSOC extends RandomElement {
    //Make these as "options" but also use a system like parent type
    //Maybe in a way Random want "context" and take advantage of Context
    //randomWithContext(ContextObject)
    //Most will ignore the context but ones like this will not
    //Context being a Object storing the List of Settings etc
    //Also work on for context needed ones a Ordering System / so like a function "ensureHasBeenRandomized(targetSetting)"
    //As this one requires a SOC model to align too, some session ? oh ye take in a list of requested to randomize
    //Like the Que then Order from most important or not if can or else remove ones controlled by parent if parent present etc
    //Maybe MAYBE in settings have parent setting first then drop down child settings ??
    public static IRandomizer create() { return new RandomBluetoothSOC(); }

    public static final Map<String, List<String>> BLUETOOTH_SOCS = new HashMap<>();

    static {
        // Qualcomm/CSR
        BLUETOOTH_SOCS.put("QUALCOMM", Arrays.asList(
                "QCA6390", "QCA6490", "QCA6750",
                "CSR8510", "CSR8675", "CSR8670",
                "WCN3660", "WCN3680", "WCN3990",
                "WCN6750", "WCN6855", "WCN7850"
        ));

        // Broadcom
        BLUETOOTH_SOCS.put("BROADCOM", Arrays.asList(
                "BCM4330", "BCM4334", "BCM4335",
                "BCM43362", "BCM4339", "BCM43430",
                "BCM4345", "BCM4345C0", "BCM4356",
                "BCM4358", "BCM4359", "BCM43602",
                "BCM4375", "BCM4377", "BCM4387"
        ));

        // MediaTek
        BLUETOOTH_SOCS.put("MEDIATEK", Arrays.asList(
                "MT6630", "MT6632", "MT6635",
                "MT7621", "MT7622", "MT7668",
                "MT7921", "MT7922", "MT7961"
        ));

        // Intel
        BLUETOOTH_SOCS.put("INTEL", Arrays.asList(
                "AX200", "AX201", "AX210",
                "AX211", "AX411", "AX1650",
                "AX1690"
        ));

        // Realtek
        BLUETOOTH_SOCS.put("REALTEK", Arrays.asList(
                "RTL8822BE", "RTL8822CE",
                "RTL8852AE", "RTL8852BE",
                "RTL8723DE", "RTL8821CE",
                "RTL8852BE", "RTL8852CE"
        ));

        // Cypress
        BLUETOOTH_SOCS.put("CYPRESS", Arrays.asList(
                "CYW43012", "CYW43438",
                "CYW4343W", "CYW4354",
                "CYW4356", "CYW43455",
                "CYW55572"
        ));

        // Nordic
        BLUETOOTH_SOCS.put("NORDIC", Arrays.asList(
                "nRF51822", "nRF52832",
                "nRF52840", "nRF5340",
                "nRF7002", "nRF7001"
        ));

        // Texas Instruments
        BLUETOOTH_SOCS.put("TI", Arrays.asList(
                "CC2540", "CC2541",
                "CC2564", "CC2640",
                "CC2642", "CC2650"
        ));

        // Samsung
        BLUETOOTH_SOCS.put("SAMSUNG", Arrays.asList(
                "S5N5C20", "S5N5C30",
                "S5N5C40", "S5N5C50"
        ));

        // Airoha
        BLUETOOTH_SOCS.put("AIROHA", Arrays.asList(
                "AB1532", "AB1562",
                "AB1568", "AB1630"
        ));

        // ASR
        BLUETOOTH_SOCS.put("ASR", Arrays.asList(
                "ASR5822", "ASR5826",
                "ASR3601", "ASR3602"
        ));

        // Infineon
        BLUETOOTH_SOCS.put("INFINEON", Arrays.asList(
                "AIROC CYW43012",
                "AIROC CYW43439",
                "AIROC CYW4373",
                "AIROC CYW55572"
        ));
    }

    public RandomBluetoothSOC() {
        super("Bluetooth SOC");
        bindSetting("bluetooth.soc");
    }

    @Override
    public String generateString() {
        return null;
        //return RandomGenerator.randomizeMapValue(BLUETOOTH_SOCS, true);
    }
}
