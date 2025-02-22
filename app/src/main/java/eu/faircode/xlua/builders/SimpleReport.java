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

public class SimpleReport implements IJsonSerial {
    public String hook;
    public String packageName;
    public int uid;
    public String event;
    public Long time;

    public SimpleReportData data;

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
    public void fromBundle(Bundle bundle) {
        read(Bundler.create(bundle));


    }

    @Override
    public void fromParcel(Parcel in) { read(Parceler.create(in)); }

    @Override
    public void writeToParcel(Parcel dest, int flags) { write(Parceler.create(dest, flags)); }

    public void read(IIOFace face) {
        this.hook = face.rString("hook");
        this.packageName = face.rString("packageName");
        this.uid = face.rInt("uid");
        this.event = face.rString("event");
        this.time = face.rLong("time");
        this.data = new SimpleReportData(face.rBundle("data"));
    }

    public IIOFace write(IIOFace face) {
        return face
                .wString("hook", this.hook)
                .wString("packageName", this.packageName)
                .wInt("uid", this.uid)
                .wString("event", this.event)
                .wLong("time", this.time)
                .wBundle("data", this.data.toBundle());
    }

    @NonNull
    @Override
    public String toString() {
        return new StringBuilder()
                .append("hook=").append(this.hook).append(Str.NEW_LINE)
                .append("package=").append(this.packageName).append(Str.NEW_LINE)
                .append("uid=").append(this.uid).append(Str.NEW_LINE)
                .append("event=").append(this.event).append(Str.NEW_LINE)
                .append("time=").append(this.time).append(Str.NEW_LINE)
                .append("data=").append(this.data.toString()).append(Str.NEW_LINE)
                .toString();
    }
}
