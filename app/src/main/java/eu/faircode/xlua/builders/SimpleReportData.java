package eu.faircode.xlua.builders;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.api.xstandard.interfaces.IJsonSerial;
import eu.faircode.xlua.builders.objects.Bundler;
import eu.faircode.xlua.builders.objects.Contenter;
import eu.faircode.xlua.builders.objects.Cursorer;
import eu.faircode.xlua.builders.objects.Jsoner;
import eu.faircode.xlua.builders.objects.Parceler;

public class SimpleReportData  implements IJsonSerial {
    public static SimpleReportData create() { return new SimpleReportData(); }
    public String function;
    public int restricted;
    public long duration;
    public String exception;
    public String old;
    public String nNew;

    public SimpleReportData() { }
    public SimpleReportData(Bundle b) { fromBundle(b); }

    @Override
    public List<ContentValues> createContentValuesList() { return null; }

    @Override
    public void fromContentValuesList(List<ContentValues> contentValues) { }

    @Override
    public ContentValues createContentValues() { return write(Contenter.create()).toContentValues(); }

    @Override
    public void fromContentValues(ContentValues contentValue) { read(Contenter.create(contentValue)); }

    @Override
    public void fromCursor(Cursor cursor) { read(Cursorer.create(cursor)); }

    @Override
    public String toJSON() throws JSONException { return toJSONObject().toString(2); }

    @Override
    public JSONObject toJSONObject() throws JSONException { return write(Jsoner.create()).toJson(); }

    @Override
    public void fromJSONObject(JSONObject obj) throws JSONException { read(Jsoner.create(obj)); }

    @Override
    public Bundle toBundle() { return write(Bundler.create()).toBundle(); }

    @Override
    public void fromBundle(Bundle bundle) { read(Bundler.create(bundle)); }

    @Override
    public void fromParcel(Parcel in) { read(Parceler.create(in)); }

    @Override
    public void writeToParcel(Parcel dest, int flags) { write(Parceler.create(dest, flags)); }

    public void read(IIOFace face) {
        this.function = face.rString("function");
        this.restricted = face.rInt("restricted", 0);
        this.duration = face.rLong("duration");
        this.exception = face.rString("exception");
        this.old = face.rString("old");
        this.nNew = face.rString("new");
    }

    public IIOFace write(IIOFace face) {
        return face
                .wString("function", this.function)
                .wInt("restricted", this.restricted)
                .wLong("duration", this.duration)
                .wString("exception", this.exception)
                .wString("old", this.old)
                .wString("new", this.nNew);
    }

    @NonNull
    @Override
    public String toString() {
        return new StringBuilder()
                .append("\nfunction=").append(this.function).append(Str.NEW_LINE)
                .append("restricted=").append(this.restricted).append(Str.NEW_LINE)
                .append("duration=").append(this.duration).append(Str.NEW_LINE)
                .append("exception=").append(this.exception).append(Str.NEW_LINE)
                .append("old=").append(this.old).append(Str.NEW_LINE)
                .append("new=").append(this.nNew).append(Str.NEW_LINE)
                .toString();
    }
}
