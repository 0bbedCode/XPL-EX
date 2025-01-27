package eu.faircode.xlua.x.xlua.interfaces;

import android.os.Parcel;
import android.os.Parcelable;

public interface IParcelType extends Parcelable {
    void fromParcel(Parcel in);
}
