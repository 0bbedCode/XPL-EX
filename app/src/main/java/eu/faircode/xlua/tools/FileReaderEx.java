package eu.faircode.xlua.tools;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructStat;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public class FileReaderEx {
    private static final String TAG = "XLua.FileReader";

    public static FileDescriptor openFile(String path, boolean readOnly) {
        int flags = readOnly ? OsConstants.O_RDONLY : (OsConstants.O_RDWR | OsConstants.O_CREAT);
        //int mode = 0666;  // Default file mode (rw-rw-rw-)
        try {
            //turn Os.open(path, flags, 0);
            //From Libcore.IoBridge => fd = Libcore.os.open(path, flags, 0666);
            return Os.open(path, flags, 0666);
        } catch (ErrnoException e) {
            Log.e(TAG, "Failed to Open File as Read Only: " + path + " Error: " + e);
            return null;  // Return -1 to indicate an error
        }
    }

    public static void closeQuietly(FileDescriptor fd) {
        try {
            Os.close(fd);
        } catch (ErrnoException ignored) { }
    }

    public static String readFileAsString(FileDescriptor fd, boolean leaveOpen) {
        try {
            return new FileReaderEx(fd, leaveOpen).readFully(leaveOpen).toString(StandardCharsets.UTF_8);
        }catch (Exception ignored) { }
        return "";
    }

    public static String readFileAsString(String absolutePath, boolean leaveOpen) throws IOException {
        try {
            return new FileReaderEx(absolutePath, leaveOpen).readFully(leaveOpen).toString(StandardCharsets.UTF_8);
        }catch (Exception ignored) { }
        return "";
    }

    private FileDescriptor fd;
    private boolean unknownLength;

    private byte[] bytes;
    private int count;

    public FileReaderEx(FileDescriptor descriptor, boolean leaveOpen) throws IOException {
        int capacity;
        try {
            //final StructStat stat = Libcore.os.fstat(fd);
            final StructStat stat = Os.fstat(fd);
            // Like RAF & other APIs, we assume that the file size fits
            // into a 32 bit integer.
            capacity = (int) stat.st_size;
            if (capacity == 0) {
                unknownLength = true;
                capacity = 8192;
            }
        } catch (ErrnoException exception) {
            if(!leaveOpen)
                closeQuietly(fd);
            //throw exception.rethrowAsIOException();
            //throw exception;
            throw new IOException();
        }

        bytes = new byte[capacity];
    }

    public FileReaderEx(String absolutePath, boolean leaveOpen) throws IOException {
        // We use IoBridge.open because callers might differentiate
        // between a FileNotFoundException and a general IOException.
        //
        // NOTE: This costs us an additional call to fstat(2) to test whether
        // "absolutePath" is a directory or not. We can eliminate it
        // at the cost of copying some code from IoBridge.open.
        try {
            //fd = IoBridge.open(absolutePath, O_RDONLY);
            fd = openFile(absolutePath, true);
            if(fd == null) throw new FileNotFoundException();
        } catch (FileNotFoundException fnfe) {
            throw fnfe;
        }

        int capacity;
        try {
            //final StructStat stat = Libcore.os.fstat(fd);
            final StructStat stat = Os.fstat(fd);
            // Like RAF & other APIs, we assume that the file size fits
            // into a 32 bit integer.
            capacity = (int) stat.st_size;
            if (capacity == 0) {
                unknownLength = true;
                capacity = 8192;
            }
        } catch (ErrnoException exception) {
            if(!leaveOpen)
                closeQuietly(fd);
            //throw exception.rethrowAsIOException();
            //throw exception;
            throw new IOException();
        }

        bytes = new byte[capacity];
    }

    public FileReaderEx readFully(boolean leaveOpen) throws IOException {
        int read;
        int capacity = bytes.length;
        try {
            while ((read = Os.read(fd, bytes, count, capacity - count)) != 0) {
                count += read;
                if (count == capacity) {
                    if (unknownLength) {
                        // If we don't know the length of this file, we need to continue
                        // reading until we reach EOF. Double the capacity in preparation.
                        final int newCapacity = capacity * 2;
                        byte[] newBytes = new byte[newCapacity];
                        System.arraycopy(bytes, 0, newBytes, 0, capacity);
                        bytes = newBytes;
                        capacity = newCapacity;
                    } else {
                        // We know the length of this file and we've read the right number
                        // of bytes from it, return.
                        break;
                    }
                }
            }



            return this;
        } catch (ErrnoException e) {
            //throw e.rethrowAsIOException();
            throw new IOException();
        } finally {
            if(!leaveOpen)
                closeQuietly(fd);   //Do note this closes
        }
    }

    //@FindBugsSuppressWarnings("EI_EXPOSE_REP")
    public byte[] toByteArray() {
        if (count == bytes.length) {
            return bytes;
        }
        byte[] result = new byte[count];
        System.arraycopy(bytes, 0, result, 0, count);
        return result;
    }

    public String toString(Charset cs) {
        return new String(bytes, 0, count, cs);
    }
}
