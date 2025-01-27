package eu.faircode.xlua.x.hook.interceptors.hardware.cpu.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomCpuVmVar extends RandomElement {
    public static IRandomizer create() { return new RandomCpuVmVar(); }

    public static final String[] CPU_CORES = {
            // ARM Cortex-A Series (Big cores)
            "cortex-a77s",
            "cortex-a77",
            "cortex-a76ae",
            "cortex-a76",
            "cortex-a75",
            "cortex-a73",
            "cortex-a72",
            "cortex-a71",
            "cortex-a70",
            "cortex-a65ae",
            "cortex-a65",
            "cortex-a57",
            "cortex-a55",
            "cortex-a53",
            "cortex-a35",
            "cortex-a34",
            "cortex-a32",
            "cortex-a17",
            "cortex-a15",
            "cortex-a9",
            "cortex-a8",
            "cortex-a7",
            "cortex-a5",

            // ARM New Generations
            "cortex-x4",
            "cortex-x3",
            "cortex-x2",
            "cortex-x1",

            // ARM Neoverse
            "neoverse-n1",
            "neoverse-e1",
            "neoverse-v1",

            // Qualcomm Custom Cores
            "kryo-680",
            "kryo-670",
            "kryo-585",
            "kryo-560",
            "kryo-485",
            "kryo-460",
            "kryo-385",
            "kryo-280",
            "kryo-260",
            "kryo-240",
            "kryo",
            "krait",
            "scorpion",

            // Samsung Custom Cores
            "mongoose-m5",
            "mongoose-m4",
            "mongoose-m3",
            "mongoose-m2",
            "mongoose-m1",
            "exynos-m1",

            // Apple Custom Cores
            "firestorm",
            "icestorm",
            "avalanche",
            "blizzard",
            "vortex",
            "tempest",
            "monsoon",
            "mistral",

            // AMD
            "zen",
            "zen2",
            "zen3",

            // Intel
            "silvermont",
            "goldmont",
            "tremont",
            "airmont",

            // Others
            "denver",        // NVIDIA
            "carmel",        // NVIDIA
            "ThunderX",      // Cavium
            "falkor",        // Qualcomm Datacenter

            // Generic Types
            "big",
            "medium",
            "little",
            "prime",
            "power",
            "efficiency"
    };

    public RandomCpuVmVar() {
        super("CPU Dalvik VM Variant");
        bindSetting("cpu.dalvik.vm.variant");
    }

    @Override
    public String generateString() { return RandomGenerator.nextElement(CPU_CORES); }
}