package eu.faircode.xlua.utilities;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.zip.ZipFile;

public class StreamUtil {

    public static void close(BufferedReader buf) {
        try {
             buf.close();
        }catch (Exception ignored) { }
    }

    public static void close(FileOutputStream fos) { close(fos, false); }
    public static void close(FileOutputStream fos, boolean flush) {
        if(fos == null) return;
        if(flush) try { fos.flush(); }catch (Exception ignored) { }
        try { fos.close(); } catch (Exception ignored) { }
    }

    public static void close(OutputStreamWriter osw) { close(osw, false); }
    public static void close(OutputStreamWriter osw, boolean flush) {
        if(osw == null) return;
        if(flush) try { osw.flush(); }catch (Exception ignored) { }
        try { osw.close(); }catch (Exception ignored) { }
    }

    public static void close(ZipFile zf) {
        if(zf == null) return;
        try { zf.close(); } catch (Exception ignore) { }
    }

    public static void close(InputStream is) {
        if(is == null) return;
        try { is.close(); } catch (Exception ignore) { }
    }
}
