package eu.faircode.xlua.x.xlua.settings.random.randomizers;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.runtime.reflect.ReflectUtil;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomAndroidBuildCodename;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomAndroidBuildDescription;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomAndroidBuildDisplayId;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomAndroidBuildFingerprint;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomAndroidBuildFlavor;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomAndroidBuildHost;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomAndroidBuildId;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomAndroidBuildIncremental;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomAndroidBuildPatch;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomAndroidBuildTags;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomAndroidBuildVersion;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomAndroidBuildVersionMinSdk;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomAndroidBuildVersionSdk;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomAndroidEtcBuildRomBaseOs;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomAndroidEtcBuildRomUser;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomAndroidEtcBuildRomVariant;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomAndroidEtcBuildRomVersionCodename;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomDeviceBootloader;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomDeviceBrand;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomDeviceCodeName;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomDeviceManufacturer;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomDeviceModel;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomDeviceNickName;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.kernel.RandomAndroidKernelNodeName;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.kernel.RandomAndroidKernelRelease;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.kernel.RandomAndroidKernelSysName;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.kernel.RandomAndroidKernelVersion;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.cell.RandomMCC;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.cell.RandomMNC;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.cell.RandomMSIN;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.cell.RandomSIMCountyCode;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.generic.RandomDateEpocSeconds;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.generic.RandomDateISOThree;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.generic.RandomDateOne;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.generic.RandomDateTwo;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.generic.RandomDateZero;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.SoC.RandomSocBoardConfigCodeName;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.SoC.RandomSocBoardManufacturer;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.SoC.RandomSocBoardManufacturerId;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.SoC.RandomSocBoardModel;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.SoC.RandomSocCpuAbi;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.SoC.RandomSocCpuAbiList;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.SoC.RandomSocCpuAbiList32;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.SoC.RandomSocCpuAbiList64;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.SoC.RandomSocCpuArchitecture;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.SoC.RandomSocCpuInstructionSet32;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.SoC.RandomSocCpuInstructionSet64;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.SoC.RandomSocCpuProcessorCount;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.etc.RandomHardwareCameraApp;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.etc.RandomHardwareCameraCount;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.etc.RandomHardwareEfuse;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.etc.RandomHardwareFpSensor;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.etc.RandomHardwareFpSensorName;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.etc.RandomHardwareGpsModelName;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.etc.RandomHardwareGpsModelYear;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.etc.RandomHardwareNfcControllerInterface;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.etc.RandomHardwareNfcKind;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.RandomSocBasebandBoardConfigName;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.RandomSocBasebandBoardImplementor;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.RandomSocBasebandBoardRadioVersion;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.RandomSocBluetoothBoardConfigName;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.RandomSocGpuEglImplementor;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.RandomSocGpuOpenGlesRenderer;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.RandomSocGpuOpenGlesVendor;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.RandomSocGpuOpenGlesVersion;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.RandomSocGpuOpenGlesVersionEncoded;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.gpu.RandomSocGpuVulkanImplementor;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.memory.RandomHardwareMemoryAvailable;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.memory.RandomHardwareMemoryTotal;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomDhcpServer;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomNetDNS;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomNetDNSList;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomNetDomains;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomNetGateway;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomNetHostAddress;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomNetHostName;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomNetNetmask;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomNetParentControl;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomNetRoutes;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomAndroidId;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomBSSID;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomBluetoothAddress;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomDRMID;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomGSFID;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomICCID;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomIMEI;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomMEID;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomMac;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomNetSSID;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomPhone;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomSIMSerial;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomSerialNo;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomSubscriptionId;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomUUID;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomVoicemailId;

public class RandomizersCache {
    private static final String TAG = LibUtil.generateTag(RandomizersCache.class);


    public static final Class<?> SETTING_GENERIC_DATE_EPOC_TYPE = RandomDateEpocSeconds.class;
    public static final Class<?> SETTING_GENERIC_DATE_ZERO_TYPE = RandomDateZero.class;
    public static final Class<?> SETTING_GENERIC_DATE_ONE_TYPE = RandomDateOne.class;
    public static final Class<?> SETTING_GENERIC_DATE_TWO_TYPE = RandomDateTwo.class;
    public static final Class<?> SETTING_GENERIC_DATE_ISO_TYPE = RandomDateISOThree.class;

    /*
        Device
     */
    public static final String SETTING_PARENT_DEVICE = "device.parent.parent.control";

    public static final String SETTING_DEVICE_BOOTLOADER = "device.bootloader";
    public static final Class<?> SETTING_DEVICE_BOOTLOADER_TYPE = RandomDeviceBootloader.class;

    public static final String SETTING_DEVICE_BRAND = "device.brand";
    public static final Class<?> SETTING_DEVICE_BRAND_TYPE = RandomDeviceBrand.class;

    // Device Manufacturer
    public static final String SETTING_DEVICE_MANUFACTURER = "device.manufacturer";
    public static final Class<?> SETTING_DEVICE_MANUFACTURER_TYPE = RandomDeviceManufacturer.class;

    // Device Nickname
    public static final String SETTING_DEVICE_NICKNAME = "device.nick.name";
    public static final Class<?> SETTING_DEVICE_NICKNAME_TYPE = RandomDeviceNickName.class;

    // Device Model
    public static final String SETTING_DEVICE_MODEL = "device.model";
    public static final Class<?> SETTING_DEVICE_MODEL_TYPE = RandomDeviceModel.class;

    // Device Codename
    public static final String SETTING_DEVICE_CODENAME = "device.codename";
    public static final Class<?> SETTING_DEVICE_CODENAME_TYPE = RandomDeviceCodeName.class;


    /*
        ANDROID
     */

    /*
            Random Date (W MT DY HR:MIN:SC TZ YR)
            Random Date One (YYYY.MM.DD)
            Random Date Two (YYYYMMDD)
            Random Date Three (YYYY-MM-DD)
     */

    // Android Build Date UTC
    public static final String SETTING_ANDROID_BUILD_DATE_EPOC = "android.build.date.utc";
    public static final String SETTING_ANDROID_BUILD_DATE = "android.build.date";
    public static final String SETTING_ANDROID_BUILD_DATE_ONE = "android.build.date.one";
    public static final String SETTING_ANDROID_BUILD_DATE_TWO = "android.build.date.two";

    // Android Build Version
    public static final String SETTING_ANDROID_BUILD_VERSION = "android.build.version";
    public static final Class<?> SETTING_ANDROID_BUILD_VERSION_TYPE = RandomAndroidBuildVersion.class;

    // Android Build Version SDK
    public static final String SETTING_ANDROID_BUILD_VERSION_SDK = "android.build.version.sdk";
    public static final Class<?> SETTING_ANDROID_BUILD_VERSION_SDK_TYPE = RandomAndroidBuildVersionSdk.class;

    // Android Build Version Min SDK
    public static final String SETTING_ANDROID_BUILD_VERSION_MIN_SDK = "android.build.version.min.sdk";
    public static final Class<?> SETTING_ANDROID_BUILD_VERSION_MIN_SDK_TYPE = RandomAndroidBuildVersionMinSdk.class;

    // Android Build Tags
    public static final String SETTING_ANDROID_BUILD_TAGS = "android.build.tags";
    public static final Class<?> SETTING_ANDROID_BUILD_TAGS_TYPE = RandomAndroidBuildTags.class;

    // Android Build Incremental
    public static final String SETTING_ANDROID_BUILD_INCREMENTAL = "android.build.incremental";
    public static final Class<?> SETTING_ANDROID_BUILD_INCREMENTAL_TYPE = RandomAndroidBuildIncremental.class;

    // Android Build Description
    public static final String SETTING_ANDROID_BUILD_DESCRIPTION = "android.build.description";
    public static final Class<?> SETTING_ANDROID_BUILD_DESCRIPTION_TYPE = RandomAndroidBuildDescription.class;

    // Android Build ID
    public static final String SETTING_ANDROID_BUILD_ID = "android.build.id";
    public static final Class<?> SETTING_ANDROID_BUILD_ID_TYPE = RandomAndroidBuildId.class;

    // Android Build Display ID
    public static final String SETTING_ANDROID_BUILD_DISPLAY_ID = "android.build.display.id";
    public static final Class<?> SETTING_ANDROID_BUILD_DISPLAY_ID_TYPE = RandomAndroidBuildDisplayId.class;

    // Android Build Flavor
    public static final String SETTING_ANDROID_BUILD_FLAVOR = "android.build.flavor";
    public static final Class<?> SETTING_ANDROID_BUILD_FLAVOR_TYPE = RandomAndroidBuildFlavor.class;

    // Android Build Host
    public static final String SETTING_ANDROID_BUILD_HOST = "android.build.host";
    public static final Class<?> SETTING_ANDROID_BUILD_HOST_TYPE = RandomAndroidBuildHost.class;

    // Android Build Patch
    public static final String SETTING_ANDROID_BUILD_PATCH = "android.build.patch";
    public static final Class<?> SETTING_ANDROID_BUILD_PATCH_TYPE = RandomAndroidBuildPatch.class;

    // Android Build Codename
    public static final String SETTING_ANDROID_BUILD_CODENAME = "android.build.codename";
    public static final Class<?> SETTING_ANDROID_BUILD_CODENAME_TYPE = RandomAndroidBuildCodename.class;

    // Android Build Fingerprint
    public static final String SETTING_ANDROID_BUILD_FINGERPRINT = "android.build.fingerprint";
    public static final Class<?> SETTING_ANDROID_BUILD_FINGERPRINT_TYPE = RandomAndroidBuildFingerprint.class;

    // Android Build ETC - Base OS
    public static final String SETTING_ANDROID_ETC_BUILD_ROM_BASE_OS = "android.etc.build.rom.base.os";
    public static final Class<?> SETTING_ANDROID_ETC_BUILD_ROM_BASE_OS_TYPE = RandomAndroidEtcBuildRomBaseOs.class;

    // Android Build ETC - ROM User
    public static final String SETTING_ANDROID_ETC_BUILD_ROM_USER = "android.etc.build.rom.user";
    public static final Class<?> SETTING_ANDROID_ETC_BUILD_ROM_USER_TYPE = RandomAndroidEtcBuildRomUser.class;

    // Android Build ETC - ROM Version Codename
    public static final String SETTING_ANDROID_ETC_BUILD_ROM_VERSION_CODENAME = "android.etc.build.rom.version.codename";
    public static final Class<?> SETTING_ANDROID_ETC_BUILD_ROM_VERSION_CODENAME_TYPE = RandomAndroidEtcBuildRomVersionCodename.class;

    // Android Build ETC - ROM Variant
    public static final String SETTING_ANDROID_ETC_BUILD_ROM_VARIANT = "android.etc.build.rom.variant";
    public static final Class<?> SETTING_ANDROID_ETC_BUILD_ROM_VARIANT_TYPE = RandomAndroidEtcBuildRomVariant.class;

    // Android Kernel - System Name
    public static final String SETTING_ANDROID_KERNEL_SYS_NAME = "android.kernel.sys.name";
    public static final Class<?> SETTING_ANDROID_KERNEL_SYS_NAME_TYPE = RandomAndroidKernelSysName.class;

    // Android Kernel - Version
    public static final String SETTING_ANDROID_KERNEL_VERSION = "android.kernel.version";
    public static final Class<?> SETTING_ANDROID_KERNEL_VERSION_TYPE = RandomAndroidKernelVersion.class;

    // Android Kernel - Release
    public static final String SETTING_ANDROID_KERNEL_RELEASE = "android.kernel.release";
    public static final Class<?> SETTING_ANDROID_KERNEL_RELEASE_TYPE = RandomAndroidKernelRelease.class;

    // Android Kernel - Node Name
    public static final String SETTING_ANDROID_KERNEL_NODE_NAME = "android.kernel.node.name";
    public static final Class<?> SETTING_ANDROID_KERNEL_NODE_NAME_TYPE = RandomAndroidKernelNodeName.class;


    //HARDWARE

    // Hardware E-Fuse
    public static final String SETTING_HARDWARE_EFUSE = "hardware.efuse";
    public static final Class<?> SETTING_HARDWARE_EFUSE_TYPE = RandomHardwareEfuse.class;

    // Hardware NFC Kind
    public static final String SETTING_HARDWARE_NFC_KIND = "hardware.nfc.kind";
    public static final Class<?> SETTING_HARDWARE_NFC_KIND_TYPE = RandomHardwareNfcKind.class;

    // Hardware NFC Controller Interface
    public static final String SETTING_HARDWARE_NFC_CONTROLLER_INTERFACE = "hardware.nfc.controller.interface";
    public static final Class<?> SETTING_HARDWARE_NFC_CONTROLLER_INTERFACE_TYPE = RandomHardwareNfcControllerInterface.class;

    // Hardware Fingerprint Sensor
    public static final String SETTING_HARDWARE_FP_SENSOR = "hardware.fp.sensor";
    public static final Class<?> SETTING_HARDWARE_FP_SENSOR_TYPE = RandomHardwareFpSensor.class;

    // Hardware Fingerprint Sensor Name
    public static final String SETTING_HARDWARE_FP_SENSOR_NAME = "hardware.fp.sensor.name";
    public static final Class<?> SETTING_HARDWARE_FP_SENSOR_NAME_TYPE = RandomHardwareFpSensorName.class;

    // Hardware GPS Model Name
    public static final String SETTING_HARDWARE_GPS_MODEL_NAME = "hardware.gps.model.name";
    public static final Class<?> SETTING_HARDWARE_GPS_MODEL_NAME_TYPE = RandomHardwareGpsModelName.class;

    // Hardware GPS Model Year
    public static final String SETTING_HARDWARE_GPS_MODEL_YEAR = "hardware.gps.model.year";
    public static final Class<?> SETTING_HARDWARE_GPS_MODEL_YEAR_TYPE = RandomHardwareGpsModelYear.class;

    // Hardware Camera Count
    public static final String SETTING_HARDWARE_CAMERA_COUNT = "hardware.camera.count";
    public static final Class<?> SETTING_HARDWARE_CAMERA_COUNT_TYPE = RandomHardwareCameraCount.class;

    // Hardware Camera App
    public static final String SETTING_HARDWARE_CAMERA_APP = "hardware.camera.app";
    public static final Class<?> SETTING_HARDWARE_CAMERA_APP_TYPE = RandomHardwareCameraApp.class;

    // Hardware Memory Total
    public static final String SETTING_HARDWARE_MEMORY_TOTAL = "hardware.memory.total";
    public static final Class<?> SETTING_HARDWARE_MEMORY_TOTAL_TYPE = RandomHardwareMemoryTotal.class;

    // Hardware Memory Available
    public static final String SETTING_HARDWARE_MEMORY_AVAILABLE = "hardware.memory.available";
    public static final Class<?> SETTING_HARDWARE_MEMORY_AVAILABLE_TYPE = RandomHardwareMemoryAvailable.class;



    //SOC Info
    // SOC Board Model
    public static final String SETTING_SOC_BOARD_MODEL = "soc.board.model";
    public static final Class<?> SETTING_SOC_BOARD_MODEL_TYPE = RandomSocBoardModel.class;

    // SOC Board Config Code Name
    public static final String SETTING_SOC_BOARD_CONFIG_CODE_NAME = "soc.board.config.code.name";
    public static final Class<?> SETTING_SOC_BOARD_CONFIG_CODE_NAME_TYPE = RandomSocBoardConfigCodeName.class;

    // SOC Board Manufacturer
    public static final String SETTING_SOC_BOARD_MANUFACTURER = "soc.board.manufacturer";
    public static final Class<?> SETTING_SOC_BOARD_MANUFACTURER_TYPE = RandomSocBoardManufacturer.class;

    // SOC Board Manufacturer ID
    public static final String SETTING_SOC_BOARD_MANUFACTURER_ID = "soc.board.manufacturer.id";
    public static final Class<?> SETTING_SOC_BOARD_MANUFACTURER_ID_TYPE = RandomSocBoardManufacturerId.class;

    // SOC CPU Processor Count
    public static final String SETTING_SOC_CPU_PROCESSOR_COUNT = "soc.cpu.processor.count";
    public static final Class<?> SETTING_SOC_CPU_PROCESSOR_COUNT_TYPE = RandomSocCpuProcessorCount.class;

    // SOC CPU Architecture
    public static final String SETTING_SOC_CPU_ARCHITECTURE = "soc.cpu.architecture";
    public static final Class<?> SETTING_SOC_CPU_ARCHITECTURE_TYPE = RandomSocCpuArchitecture.class;

    // SOC CPU Instruction Set 32-bit
    public static final String SETTING_SOC_CPU_INSTRUCTION_SET_32 = "soc.cpu.instruction.set.32";
    public static final Class<?> SETTING_SOC_CPU_INSTRUCTION_SET_32_TYPE = RandomSocCpuInstructionSet32.class;

    // SOC CPU Instruction Set 64-bit
    public static final String SETTING_SOC_CPU_INSTRUCTION_SET_64 = "soc.cpu.instruction.set.64";
    public static final Class<?> SETTING_SOC_CPU_INSTRUCTION_SET_64_TYPE = RandomSocCpuInstructionSet64.class;

    // SOC CPU ABI
    public static final String SETTING_SOC_CPU_ABI = "soc.cpu.abi";
    public static final Class<?> SETTING_SOC_CPU_ABI_TYPE = RandomSocCpuAbi.class;

    // SOC CPU ABI List
    public static final String SETTING_SOC_CPU_ABI_LIST = "soc.cpu.abi.list";
    public static final Class<?> SETTING_SOC_CPU_ABI_LIST_TYPE = RandomSocCpuAbiList.class;

    // SOC CPU ABI List 32-bit
    public static final String SETTING_SOC_CPU_ABI_LIST_32 = "soc.cpu.abi.list.32";
    public static final Class<?> SETTING_SOC_CPU_ABI_LIST_32_TYPE = RandomSocCpuAbiList32.class;

    // SOC CPU ABI List 64-bit
    public static final String SETTING_SOC_CPU_ABI_LIST_64 = "soc.cpu.abi.list.64";
    public static final Class<?> SETTING_SOC_CPU_ABI_LIST_64_TYPE = RandomSocCpuAbiList64.class;

    // SOC CPU Info Dump
    public static final String SETTING_SOC_CPU_INFO_DUMP = "soc.cpu.info.dump";
    //ERROR user input required
    //public static final Class<?> SETTING_SOC_CPU_INFO_DUMP_TYPE = RandomSocCpuInfoDump.class;





    //
    //GPU
    //

    // SOC GPU EGL Implementor
    public static final String SETTING_SOC_GPU_EGL_IMPLEMENTOR = "soc.gpu.egl.implementor";
    public static final Class<?> SETTING_SOC_GPU_EGL_IMPLEMENTOR_TYPE = RandomSocGpuEglImplementor.class;

    // SOC GPU Vulkan Implementor
    public static final String SETTING_SOC_GPU_VULKAN_IMPLEMENTOR = "soc.gpu.vulkan.implementor";
    public static final Class<?> SETTING_SOC_GPU_VULKAN_IMPLEMENTOR_TYPE = RandomSocGpuVulkanImplementor.class;

    // SOC GPU OpenGLES Version Encoded
    public static final String SETTING_SOC_GPU_OPEN_GLES_VERSION_ENCODED = "soc.gpu.open.gles.version.encoded";
    public static final Class<?> SETTING_SOC_GPU_OPEN_GLES_VERSION_ENCODED_TYPE = RandomSocGpuOpenGlesVersionEncoded.class;

    // SOC GPU OpenGLES Vendor
    public static final String SETTING_SOC_GPU_OPEN_GLES_VENDOR = "soc.gpu.open.gles.vendor";
    public static final Class<?> SETTING_SOC_GPU_OPEN_GLES_VENDOR_TYPE = RandomSocGpuOpenGlesVendor.class;

    // SOC GPU OpenGLES Renderer
    public static final String SETTING_SOC_GPU_OPEN_GLES_RENDERER = "soc.gpu.open.gles.renderer";
    public static final Class<?> SETTING_SOC_GPU_OPEN_GLES_RENDERER_TYPE = RandomSocGpuOpenGlesRenderer.class;

    // SOC GPU OpenGLES Version
    public static final String SETTING_SOC_GPU_OPEN_GLES_VERSION = "soc.gpu.open.gles.version";
    public static final Class<?> SETTING_SOC_GPU_OPEN_GLES_VERSION_TYPE = RandomSocGpuOpenGlesVersion.class;



    // SOC GPU GFX Driver Name 1
    public static final String SETTING_SOC_GPU_GFX_DRIVER_NAME_1 = "soc.gpu.gfx.driver.name.1";
    //public static final Class<?> SETTING_SOC_GPU_GFX_DRIVER_NAME_1_TYPE = RandomSocGpuGfxDriverName1.class;
    // SOC GPU GFX Driver Name 2
    public static final String SETTING_SOC_GPU_GFX_DRIVER_NAME_2 = "soc.gpu.gfx.driver.name.2";
    //public static final Class<?> SETTING_SOC_GPU_GFX_DRIVER_NAME_2_TYPE = RandomSocGpuGfxDriverName2.class;



    // SOC Baseband Board Config Name
    public static final String SETTING_SOC_BASEBAND_BOARD_CONFIG_NAME = "soc.baseband.board.config.name";
    public static final Class<?> SETTING_SOC_BASEBAND_BOARD_CONFIG_NAME_TYPE = RandomSocBasebandBoardConfigName.class;

    // SOC Baseband Board Radio Version
    public static final String SETTING_SOC_BASEBAND_BOARD_RADIO_VERSION = "soc.baseband.board.radio.version";
    public static final Class<?> SETTING_SOC_BASEBAND_BOARD_RADIO_VERSION_TYPE = RandomSocBasebandBoardRadioVersion.class;

    // SOC Baseband Board Implementor
    public static final String SETTING_SOC_BASEBAND_BOARD_IMPLEMENTOR = "soc.baseband.board.implementor";
    public static final Class<?> SETTING_SOC_BASEBAND_BOARD_IMPLEMENTOR_TYPE = RandomSocBasebandBoardImplementor.class;

    // SOC Bluetooth Board Config Name
    public static final String SETTING_SOC_BLUETOOTH_BOARD_CONFIG_NAME = "soc.bluetooth.board.config.name";
    public static final Class<?> SETTING_SOC_BLUETOOTH_BOARD_CONFIG_NAME_TYPE = RandomSocBluetoothBoardConfigName.class;


    public static final String SETTING_UNIQUE_BLUETOOTH = "unique.bluetooth.address";
    public static final Class<?> SETTING_UNIQUE_BLUETOOTH_TYPE = RandomBluetoothAddress.class;

    public static final String SETTING_UNIQUE_MAC = "unique.network.mac.address";
    public static final Class<?> SETTING_UNIQUE_MAC_TYPE = RandomMac.class;


    public static final String SETTING_UNIQUE_SUB_ID = "unique.gsm.subscription.id";
    public static final Class<?> SETTING_UNIQUE_SUB_ID_TYPE = RandomSubscriptionId.class;


    public static final String SETTING_CELL_OPERATOR_MCC = "cell.operator.mcc";
    public static final Class<?> SETTING_CELL_OPERATOR_MCC_TYPE = RandomMCC.class;

    public static final String SETTING_CELL_OPERATOR_MNC = "cell.operator.mnc";
    public static final Class<?> SETTING_CELL_OPERATOR_MNC_TYPE = RandomMNC.class;

    public static final String SETTING_CELL_MSIN = "unique.gsm.operator.msin";
    public static final Class<?> SETTING_CELL_MSIN_TYPE = RandomMSIN.class;

    public static final String SETTING_UNIQUE_GSF_ID = "unique.gsf.id";
    public static final Class<?> SETTING_UNIQUE_GSF_ID_TYPE = RandomGSFID.class;


    public static final String SETTING_UNIQUE_SERIAL_NO = "unique.serial.no";
    public static final Class<?> SETTING_UNIQUE_SERIAL_NO_TYPE = RandomSerialNo.class;

    public static final String SETTING_UNIQUE_NET_SSID = "unique.network.ssid";
    public static final Class<?> SETTING_UNIQUE_NET_SSID_TYPE = RandomNetSSID.class;

    public static final String SETTING_UNIQUE_NET_BSSID = "unique.network.bssid";
    public static final Class<?> SETTING_UNIQUE_NET_BSSID_TYPE = RandomBSSID.class;

    public static final String SETTING_UNIQUE_UUID = "unique.guid.uuid";
    public static final String SETTING_UNIQUE_VA_ID = "unique.app.va.id";
    public static final String SETTING_UNIQUE_ANON_ID = "unique.app.anon.id";
    public static final String SETTING_UNIQUE_OPEN_ANON_ID = "unique.open.anon.advertising.id";
    public static final String SETTING_UNIQUE_BOOT_ID = "unique.boot.id";
    public static final String SETTING_UNIQUE_FACEBOOK_ID = "unique.boot.id";
    public static final String SETTING_UNIQUE_GOOGLE_ID = "unique.google.advertising.id";

    public static final Class<?> SETTING_UNIQUE_UUID_TYPE = RandomUUID.class;


    //unique.gsm.phone.number.[1,2]
    public static final String SETTING_UNIQUE_PHONE_NO = "unique.gsm.phone.number";
    public static final Class<?> SETTING_UNIQUE_PHONE_NO_TYPE = RandomPhone.class;

    public static final String SETTING_UNIQUE_ICC_ID = "unique.gsm.icc.id";
    public static final Class<?> SETTING_UNIQUE_ICC_ID_TYPE = RandomICCID.class;

    public static final String SETTING_UNIQUE_MEID = "unique.gsm.meid";
    public static final Class<?> SETTING_UNIQUE_MEID_TYPE = RandomMEID.class;

    public static final String SETTING_UNIQUE_IMEI = "unique.gsm.imei";
    public static final Class<?> SETTING_UNIQUE_IMEI_TYPE = RandomIMEI.class;

    public static final String SETTING_UNIQUE_ANDROID_ID = "unique.android.id";
    public static final Class<?> SETTING_UNIQUE_ANDROID_ID_TYPE = RandomAndroidId.class;

    public static final String SETTING_UNIQUE_SIM_SERIAL = "unique.gsm.sim.serial";
    public static final Class<?> SETTING_UNIQUE_SIM_SERIAL_TYPE = RandomSIMSerial.class;

    public static final String SETTING_CELL_SIM_COUNTRY_CODE = "cell.sim.country.numeric.code";
    public static final Class<?> SETTING_CELL_SIM_COUNTRY_CODE_TYPE = RandomSIMCountyCode.class;

    public static final String SETTING_UNIQUE_DRM_ID = "unique.drm.id";
    public static final Class<?> SETTING_UNIQUE_DRM_ID_TYPE = RandomDRMID.class;

    public static final String SETTING_UNIQUE_VOICEMAIL_ID = "unique.gsm.voicemail.id";
    public static final Class<?> SETTING_UNIQUE_VOICEMAIL_ID_TYPE = RandomVoicemailId.class;


    public static final String SETTING_NET_HOST_NAME = "network.host.name";
    public static final Class<?> SETTING_NET_HOST_NAME_TYPE = RandomNetHostName.class;

    public static final String SETTING_NET_GATEWAY = "network.gateway";
    public static final Class<?> SETTING_NET_GATEWAY_TYPE = RandomNetGateway.class;

    public static final String SETTING_NET_DNS = "network.dns";
    public static final Class<?> SETTING_NET_DNS_TYPE = RandomNetDNS.class;

    public static final String SETTING_NET_DNS_LIST = "network.dns.list";
    public static final Class<?> SETTING_NET_DNS_LIST_TYPE = RandomNetDNSList.class;

    public static final String SETTING_NET_ROUTES = "network.routes";
    public static final Class<?> SETTING_NET_ROUTES_TYPE = RandomNetRoutes.class;

    public static final String SETTING_NET_HOST = "network.host.address";
    public static final Class<?> SETTING_NET_HOST_TYPE = RandomNetHostAddress.class;

    public static final String SETTING_NET_DHCP = "network.dhcp.server";
    public static final Class<?> SETTING_NET_DHCP_TYPE = RandomDhcpServer.class;


    public static final String SETTING_NET_NETMASK = "network.netmask";
    public static final Class<?> SETTING_NET_NETMASK_TYPE = RandomNetNetmask.class;

    public static final String SETTING_NET_DOMAINS = "network.domains";
    public static final Class<?> SETTING_NET_DOMAINS_TYPE = RandomNetDomains.class;

    public static final String SETTING_NET_PARENT_CONTROL = "network.parent.control.isp";
    public static final Class<?> SETTING_NET_PARENT_CONTROL_TYPE = RandomNetParentControl.class;


    private static final Map<String, IRandomizer> randomizers = new HashMap<>();

    //Use View Registry or something Store Copy of Randomizers
    //WHENEVER the Container Binds, it find it in the copy
    //and bind it there

    //Hmm but if they dont "extend" then it will be an issue ?
    //Well lets focus on ..
    //No just set the randomizer object

    //From the UI top we

    public static Map<String, IRandomizer> getCopy() {
        init();
        Map<String, IRandomizer> copy = new HashMap<>(randomizers.size());
        copy.putAll(randomizers);
        //add the more unique ones ?
        return copy;
    }

    public static void init() {
        try {
            synchronized (randomizers) {
                if(randomizers.isEmpty()) {
                    Field[] fields = RandomizersCache.class.getDeclaredFields();
                    for(Field field : fields) {
                        if(!Modifier.isStatic(field.getModifiers()))
                            continue;

                        String name = field.getName().toLowerCase();
                        Class<?> ret = field.getType();
                        if(name.startsWith("setting_") && name.endsWith("_type") && ret == Class.class) {
                            try {
                                Object value = field.get(null);
                                if(!(value instanceof Class<?>))
                                    throw new Exception("Object Value is not Instance of Class<?>");

                                Class<?> classType = (Class<?>) value;
                                IRandomizer randomizer = (IRandomizer) classType.newInstance();

                                for(String setting : randomizer.getSettings())
                                    randomizers.put(setting, randomizer);

                                //Perhaps do some logic here to determine if Special ?

                            }catch (Exception eI) {
                                Log.e(TAG, "Failed to Reflect Field: " + name + " Error=" + eI);
                            }
                        }
                    }
                }
            }
        }catch (Exception e) {
            Log.e(TAG, "Failed to Init Randomizers, error=" + e);
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Exiting Init Randomizers, Count=" + randomizers.size());
    }
}
