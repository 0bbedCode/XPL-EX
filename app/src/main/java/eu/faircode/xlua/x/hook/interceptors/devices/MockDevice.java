package eu.faircode.xlua.x.hook.interceptors.devices;

import android.util.Log;
import android.view.InputDevice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.hook.interceptors.devices.random.RandomDeviceDescriptor;
import eu.faircode.xlua.x.hook.interceptors.devices.random.RandomDeviceProductId;
import eu.faircode.xlua.x.hook.interceptors.devices.random.RandomDeviceVendorId;
import eu.faircode.xlua.x.runtime.reflect.DynamicField;

public class MockDevice {
    private static final String TAG = "XLua.MockDevice";

    //This seems to be the SHA1 Hash ? I never seen it be something else ????
    public static final DynamicField FIELD_DESCRIPTOR = new DynamicField(InputDevice.class, "mDescriptor")
            .setAccessible(true);

    public static final DynamicField FIELD_NAME = new DynamicField(InputDevice.class, "mName")
            .setAccessible(true);

    public static final DynamicField FIELD_VENDOR_ID = new DynamicField(InputDevice.class, "mVendorId")
            .setAccessible(true);

    public static final DynamicField FIELD_PRODUCT_ID = new DynamicField(InputDevice.class, "mProductId")
            .setAccessible(true);


    //public InputDeviceIdentifier getIdentifier() {
    //    return mIdentifier;
    //}

    public String name;
    public String descriptor;
    public int id;
    public int vendorId;
    public int productId;

    public InputDeviceType type;

    public String fakeName;
    public String fakeDescriptor;
    public String fakeDescriptorHash;
    public int fakeVendorId;
    public int fakeProductId;

    public MockDevice(InputDevice inputDevice) {
        name = inputDevice.getName();
        descriptor = inputDevice.getDescriptor();
        id = inputDevice.getId();
        vendorId = inputDevice.getVendorId();
        productId = inputDevice.getProductId();

        type = InputDeviceType.fromDevice(inputDevice);

        fakeName = type.getRandomMockName();
        fakeDescriptor = type.getRandomMockDescriptor();
        fakeDescriptorHash = RandomDeviceDescriptor.generateHash(fakeDescriptor);
        fakeVendorId = RandomDeviceVendorId.getVendorIdFromDescriptorOrName(fakeDescriptor, fakeName);
        fakeProductId = RandomDeviceProductId.getProductId(fakeVendorId, type, fakeName);
        //set the vendor ID based off of the randomized stuff / vendor
    }

    public boolean spoofInputDevice(InputDevice inputDevice) {
        if(!inputDevice.getDescriptor().equalsIgnoreCase(fakeDescriptor) && !inputDevice.getDescriptor().equalsIgnoreCase(fakeDescriptorHash)) {
            boolean setFakeDesc = FIELD_DESCRIPTOR.trySetValueInstanceEx(inputDevice, fakeDescriptorHash);
            boolean setFakeName = FIELD_NAME.trySetValueInstanceEx(inputDevice, fakeName);
            boolean setFakeVend = FIELD_VENDOR_ID.trySetValueInstanceEx(inputDevice, fakeVendorId);
            boolean setFakeProd = FIELD_PRODUCT_ID.trySetValueInstanceEx(inputDevice, fakeProductId);

            if(DebugUtil.isDebug())
                Log.d(TAG, "Set Fake Descriptor ? " + (setFakeDesc) + " Set Fake Name ? " + (setFakeName) + " Set Fake Vendor ID ? " + (setFakeVend) + " Set Fake Product ID ? " + (setFakeProd));

            return setFakeDesc || setFakeName || setFakeVend || setFakeProd;
        }

        return false;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == null) return false;

        if(obj instanceof String) {
            String v = (String)obj;
            return v.equalsIgnoreCase(descriptor) ||
                    v.equalsIgnoreCase(String.valueOf(id)) ||
                    v.equalsIgnoreCase(fakeDescriptor) ||
                    v.equalsIgnoreCase(fakeName) ||
                    v.equalsIgnoreCase(name) ||
                    v.equalsIgnoreCase(fakeDescriptorHash);
        }

        if(obj instanceof MockDevice) {
            MockDevice d = (MockDevice) obj;
            return descriptor.equalsIgnoreCase(d.descriptor) ||
                    id == d.id ||
                    fakeDescriptor.equalsIgnoreCase(d.fakeDescriptor) ||
                    fakeDescriptor.equalsIgnoreCase(d.descriptor) ||
                    fakeName.equalsIgnoreCase(d.name) ||
                    fakeDescriptorHash.equalsIgnoreCase(d.descriptor);
        }

        if(obj instanceof Integer) {
            int idd = (int)obj;
            return idd == id;
        }

        if(obj instanceof InputDevice) {
            InputDevice ipd = (InputDevice) obj;
            String d = ipd.getDescriptor();
            String n = ipd.getName();
            return d.equalsIgnoreCase(descriptor) ||
                    ipd.getId() == id ||
                    d.equalsIgnoreCase(fakeDescriptor) ||
                    n.equalsIgnoreCase(fakeName) ||
                    n.equalsIgnoreCase(name) ||
                    d.equalsIgnoreCase(fakeDescriptorHash);
        }

        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("ID", id)
                .appendFieldLine("Name", name)
                .appendFieldLine("Descriptor", descriptor)
                .appendFieldLine("Vendor ID", vendorId)
                .appendFieldLine("Product ID", productId)
                .appendFieldLine("Fake Name", fakeName)
                .appendFieldLine("Fake Descriptor", fakeDescriptor)
                .appendFieldLine("Fake Descriptor Hash", fakeDescriptorHash)
                .appendFieldLine("Fake Vendor ID", fakeVendorId)
                .appendFieldLine("Fake Product ID", fakeProductId)
                .toString(true);
    }
}
