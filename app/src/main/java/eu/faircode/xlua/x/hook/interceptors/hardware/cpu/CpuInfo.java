package eu.faircode.xlua.x.hook.interceptors.hardware.cpu;

import java.util.List;

public class CpuInfo {
    public String name;
    public String model;
    public String baseBand;
    public String architecture;
    public String abi;
    public List<String> abiList;
    public List<String> abiList32;
    public List<String> abiList64;
    public String manufacturer;
    public String manufacturerShort;
    public String platformName;
    public String cpuVmFeatures;
    public String contents;

    public String bluetoothSocModel;


    //In theory this would also control the BLUETOOTH SOC
    //All CPU info
    //And most likely GPU Info
    //"nqx" - For Qualcomm NFC chips for :
}
