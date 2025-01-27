package eu.faircode.xlua.x.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.xlua.configs.PathDetails;

public class DirectoryAdapter extends ArrayAdapter<PathDetails> {
    private final LayoutInflater inflater;

    public DirectoryAdapter(Context context, List<PathDetails> paths) {
        super(context, R.layout.list_item_directory, paths);
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        CheckedTextView textView;
        if (convertView == null) {
            textView = (CheckedTextView) inflater.inflate(R.layout.list_item_directory, parent, false);
        } else {
            textView = (CheckedTextView) convertView;
        }

        PathDetails item = getItem(position);
        if (item != null) {
            textView.setText(item.toString());
        }

        return textView;
    }
}