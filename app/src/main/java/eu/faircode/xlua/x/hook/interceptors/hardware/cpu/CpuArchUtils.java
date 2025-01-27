package eu.faircode.xlua.x.hook.interceptors.hardware.cpu;

import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.x.data.utils.ListUtil;

public class CpuArchUtils {
    public static boolean isArm64(String abi) { return abi.contains("arm64") || abi.contains("aarch64"); }
    public static boolean isArm(String abi) { return abi.contains("armeabi") || abi.contains("armv"); }
    public static boolean isBig(String abi) { return abi.startsWith("x") || abi.startsWith("amd") || abi.startsWith("i"); }
    public static boolean is64(String abi) { return abi.contains("x64") || abi.contains("_64") || abi.contains("amd64"); }

    public static final List<String> ABI_ARM_64 = ListUtil.toSingleList("arm64-v8a");
    public static final List<String> ABI_ARM = Arrays.asList("armeabi-v7a", "armeabi");
    public static final List<String> ABI_ALL_COMMON = Arrays.asList("arm64-v8a", "armeabi-v7a", "x86_64", "x86");
    public static final List<String> ABI_64 = Arrays.asList("arm64-v8a", "x86_64", "mips64", "riscv64");
    public static final List<String> ABI_32 = Arrays.asList("armeabi-v7a", "armeabi", "x86", "mips");
    public static final List<String> ABI_ALL_UNCOMMON = Arrays.asList(
            "arm64-v8a-hwasan", // HWASan-enabled arm64-v8a
            "armeabi-v7a-hard", // Hardware FPU armeabi-v7a
            "mips-r2",
            "mips-r6"
    );

    public static final List<String> ARM_ARCHITECTURES = Arrays.asList(
            // ARM architectures
            "aarch64",  // 64-bit ARM
            "arm64",    // Alternative name for 64-bit ARM
            "armv7l",   // 32-bit ARM
            "armv6l",   // Older ARM
            "armv5l"   // Older ARM
    );

    public static final List<String> x86_ARCHITECTURES = Arrays.asList(
            // x86 architectures
            "x86_64",   // 64-bit x86
            "amd64",    // Alternative name for x86_64
            "i686",     // 32-bit x86
            "i586",     // Older x86
            "i486",     // Older x86
            "i386"     // Oldest x86
    );

    public static final List<String> POWER_PC_ARCHITECTURES = Arrays.asList(
            // PowerPC
            "ppc64le",  // PowerPC 64-bit Little Endian
            "ppc64",    // PowerPC 64-bit
            "ppc"       // PowerPC 32-bit
    );

    public static final List<String> SPARC_ARCHITECTURES = Arrays.asList(
            // SPARC
            "sparc64",  // SPARC 64-bit
            "sparc"    // SPARC 32-bit
    );

    public static final List<String> MIPS_ARCHITECTURES = Arrays.asList(
            // MIPS
            "mips64",   // MIPS 64-bit
            "mips"     // MIPS 32-bit
    );

    public static final List<String> IBM_ARCHITECTURES = Arrays.asList(
            // IBM
            "s390x",    // IBM System z
            "s390"     // Older IBM System z
    );

    public static final List<String> RISK_V_ARCHITECTURES = Arrays.asList(
            // RISC-V
            "riscv64",  // RISC-V 64-bit
            "riscv32"  // RISC-V 32-bit
    );

    public static final List<String> OTHER_ARCHITECTURES = Arrays.asList(
            // Other
            "ia64",     // Intel Itanium
            "alpha",    // DEC Alpha
            "loongarch64", // LoongArch 64-bit
            "m68k"      // Motorola 68k
    );


    public static boolean isCommonAbi(String abi) {
        if(abi == null) return false;
        return ABI_ALL_COMMON.contains(abi);
    }

    public static String getInstructionSetArchitecture(String abi) {
        if(abi == null) return null;
        return isArm(abi) ?
                "armeabi" :
                isArm64(abi) ? "aarch64" :
                        isBig(abi) ? is64(abi) ? "x86_64" : "x86" : null;
    }

    public static List<String> getAbiList(String abi) {
        if(abi == null) return null;
        boolean isArm64 = isArm64(abi);
        return isBig(abi) ?
                ListUtil.toSingleList(abi) :
                ListUtil.combine(ABI_ARM_64, ABI_ARM, isArm64, isArm64);
    }

    public static List<String> getAbiList32(String abi) {
        if(abi == null) return null;
        return isBig(abi) ?
                ListUtil.toSingleList("x86") :
                ListUtil.copyToList(ABI_ARM);
    }

    public static List<String> getAbiList64(String abi) {
        if(abi == null) return null;
        return !is64(abi) && !isArm64(abi) ? ListUtil.<String>emptyList() :
                isBig(abi) ?
                        ListUtil.toSingleList("x86_64") : ListUtil.copyToList(ABI_ARM_64);
    }

}
