package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.UUID;

import eu.faircode.xlua.random.IRandomizer;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomStringGenerator;

public class RandomUUID implements IRandomizer {
    //google.advertisingid
    //Now its "ad.id"
    //84630630-u4ls-k487-f35f-h37afe0pomwq
    //00000000-0000-0000-0000-000000000000

    //{ "name": "unique.google.advertising.id", "description": "Google Advertising ID", "defaultValue": "84630630-u4ls-k487-f35f-h37afe0pomwq" },
    //{ "name": "unique.facebook.advertising.id", "description": "Face Advertising ID", "defaultValue": "84630630-u4ls-k487-f35f-h37afe0pomwq" },
    //{ "name": "unique.open.anon.advertising.id", "description": "OAID Open Anonymous Advertising ID", "defaultValue": "84630630-u4ls-k487-f35f-h37afe0pomwq" },
    //{ "name": "unique.guid.uuid", "description": "GUID_uuid. Android/.GUID_uuid To be honest dosnt seem common", "defaultValue": "c651fde4-6ea1-4a41-882c-59bc2e94571d" },
    //f8c3de3d-1fea-4d7c-a8b0-29f63c4c3454
    //

    @Override
    public boolean isSetting(String setting) {
        return setting.equalsIgnoreCase(getSettingName()) ||
                "unique.google.advertising.id".equalsIgnoreCase(setting) ||
                "unique.facebook.advertising.id".equalsIgnoreCase(setting) ||
                "unique.open.anon.advertising.id".equalsIgnoreCase(setting) ||
                "unique.guid.uuid".equalsIgnoreCase(setting) ||
                "unique.boot.id".equalsIgnoreCase(setting);
    }

    @Override
    public String getSettingName() { return "ad.id"; }

    @Override
    public String getName() {
        return "OAID/ADID (UUID)";
    }

    @Override
    public String getID() {
        return "%ad_id%";
    }

    @Override
    public String generateString() {
        return UUID.randomUUID().toString();
        /*return (RandomStringGenerator.generateRandomHexString(8) +
                "-" +
                RandomStringGenerator.generateRandomHexString(4) +
                "-" +
                RandomStringGenerator.generateRandomNumberString(4) +
                "-" +
                RandomStringGenerator.generateRandomNumberString(4)+
                "-" +
                RandomStringGenerator.generateRandomHexString(12)).toLowerCase();*/
    }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return null; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}
