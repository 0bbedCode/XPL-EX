package eu.faircode.xlua.x.xlua;

import android.os.Bundle;

public interface IBundleData {
    void populateFromBundle(Bundle b);
    void populateBundle(Bundle b);

    Bundle toBundle();
}
