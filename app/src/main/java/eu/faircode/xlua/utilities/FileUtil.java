package eu.faircode.xlua.utilities;

import android.annotation.SuppressLint;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.logger.XLog;


public class FileUtil {
    private static final String TAG = "XLua.FileUtil";
    private static final List<String> DEVICE_FOLDERS = Arrays.asList("/sys/class/fingerprint/fingerprint/name", "/sys/bus/spi/drivers", "/sys/class/sensors", "/proc/bus/input", "/proc/scsi", "/sys/class/block", "/sys/block/sda/device/model", "/vendor/etc", "/sys/project_info", "/sys/android_camera", "/sys/class/camera", "/sys/bus/platform/drivers", "/sys/devices/soc", "/sys/class/fingerprint");

    public static int getDescriptorNumber(FileDescriptor fd) {
        try {
            @SuppressLint("DiscouragedPrivateApi") Method intMethod = fd.getClass().getDeclaredMethod("getInt$");
            int fId = (int) intMethod.invoke(fd);
            return fId;
        } catch (Throwable e) {
            XLog.e("Failed to get File Descriptor Path.", e);
            return 0;
        }
    }

    //       String[] ss = new String[] {"fpc1020", "fpc_fpc1020", "fpc1020", "fpc_fpc1020", "fpc1020", "fp_fpc1020", "fpc1020", "fpc,fpc1020", "fpc1020", "fp_fpc1155", "fpc1145", "fpc1145","fpc1145_device", "fingerprint_fpc", "goodix_fp",  "goodix_fp", "0.goodix_fp", "fpc1145",  "0.fpc1145", "fpc1020",  "fingerprint", "silead_fp", "0.silead_fp", "cdfinger_fp",  "silead_fp", "goodix_fp", "goodix_fingerprint", "cdfinger_fp", "cdfinger_fingerprint", "goodix_fp", "goodix,fingerprint", "fpc1020","fingerprint_fpc", "goodix_fp","fingerprint_goodix", "qbt1000","qbt2000",  "elan",  "elan","elan_elan_fp", "esfp0", "egistec_fp" };
    public static boolean isDeviceDriver(String filePath) {
        if(filePath.contains("sys/devices/system/cpu/")) return false;
        for(String d : DEVICE_FOLDERS) {
            if(filePath.startsWith(d))
                return true;
        } return false;
    }

    public static List<File> filterFileList(List<File> files) {
        List<File> fs = new ArrayList<>();
        for(File f : files)
            if(!FileUtil.isDeviceDriver(f.getPath()))
                fs.add(f);
        return fs.size() == files.size() ? files : fs.isEmpty() ? null : fs;
    }

    public static File[] filterFileArray(File[] files) {
        List<File> fs = new ArrayList<>();
        for(File f : files)
            if(!FileUtil.isDeviceDriver(f.getPath()))
                fs.add(f);
        return fs.size() == files.length ? files : fs.isEmpty() ? null : fs.toArray(new File[0]);
    }


    public static List<String> filterList(List<String> files) {
        List<String> fs = new ArrayList<>();
        for(String s : files)
            if(!FileUtil.isDeviceDriver(s))
                fs.add(s);

        return fs.size() == files.size() ? files : fs.isEmpty() ? null : fs;
    }

    public static String[] filterArray(String[] files) {
        List<String> fs = new ArrayList<>();
        for(String s : files)
            if(!FileUtil.isDeviceDriver(s))
                fs.add(s);
        return fs.size() == files.length ? files : fs.isEmpty() ? null : fs.toArray(new String[0]);
    }

    public static FileDescriptor generateFakeFileDescriptor(String contents) {
        File mockFile = generateTempFakeFile(contents);
        if(mockFile == null)
            return null;
        try {
            ParcelFileDescriptor pFileDescriptor = ParcelFileDescriptor.open(mockFile, ParcelFileDescriptor.MODE_READ_ONLY);//0x10000000
            return pFileDescriptor.getFileDescriptor();
        }catch (Exception e) {
            XLog.e("Failed to Create Fake File Descriptor!!", e, true);
            return null;
        }
    }


    public static File generateTempFakeFile(String contents) {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            File temp = File.createTempFile("temp", null);
            fos = new FileOutputStream(temp, false);
            osw = new OutputStreamWriter(fos);
            osw.write(contents);
            return temp;
        }catch (Exception e) {
            XLog.e("Failed to Create Fake File!", e, true);
            return null;
        }finally {
            StreamUtil.close(osw, true);
            StreamUtil.close(fos);
        }
    }

    public static void printContents(String file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Log.i(TAG, line);
            }
        } catch (Exception e) {
            XLog.e("Error Printing File Contents", e, true);
        }
    }
}
