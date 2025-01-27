package eu.faircode.xlua;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdapterDatabase extends RecyclerView.Adapter<AdapterDatabase.ViewHolder> {
    private static final String TAG = "XLua.ADDatabase";
    private List<XDatabaseOld> dbs = new ArrayList<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public class ViewHolder extends RecyclerView.ViewHolder
            implements
            View.OnClickListener {

        final View itemView;

        final TextView tvDatabaseName;
        final TextView tvDatabasePath;
        final Button btDeleteDatabase;
        final Button btExportDatabase;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            tvDatabaseName = itemView.findViewById(R.id.tvDatabaseName);
            tvDatabasePath = itemView.findViewById(R.id.tvDatabasePath);
            btDeleteDatabase = itemView.findViewById(R.id.btDeleteDatabase);
            btExportDatabase = itemView.findViewById(R.id.btExportDatabase);
        }

        public void onClick(View view) {
            Log.i(TAG, "onClick");
            final XDatabaseOld prop = dbs.get(getAdapterPosition());
            int id = view.getId();
            String name = prop.getName();

            Log.i(TAG, "onClick=" + id + "==" + name);

            switch (view.getId()) {
                case R.id.itemViewDatabase:

                    break;
            }
        }
    }

    AdapterDatabase() { setHasStableIds(true); }

    void set(List<XDatabaseOld> db_vals) {
        dbs.clear();
        Log.i(TAG, "Set has Init=" + dbs.size());
        dbs.addAll(db_vals);
        Log.i(TAG, "Internal Count=" + dbs.size());
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) { return dbs.get(position).hashCode(); }

    @Override
    public int getItemCount() { return dbs.size(); }

    @Override
    public AdapterDatabase.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AdapterDatabase.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.db, parent, false));
    }

    @Override
    public void onBindViewHolder(final AdapterDatabase.ViewHolder holder, int position) {
        XDatabaseOld db = dbs.get(position);

        holder.tvDatabaseName.setText(db.getName());
        holder.tvDatabasePath.setText(db.getPath());
    }
}
