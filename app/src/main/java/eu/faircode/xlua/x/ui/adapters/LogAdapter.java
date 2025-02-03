package eu.faircode.xlua.x.ui.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.xlua.log.LogPacket;

public class LogAdapter  extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
    private final List<LogPacket> logList;
    private final Context context;

    public LogAdapter(Context context, List<LogPacket> logList) {
        this.context = context;
        this.logList = logList;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.log_item, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogPacket log = logList.get(position);

        // Set log icon, type, and time
        //holder.ivLogIcon.setImageResource(log.getIconResId()); // Assuming a resource ID for icons

        holder.tvLogType.setText(String.valueOf(log.type));
        holder.tvLogTime.setText(log.time > 1 ? formatTime(log.time) : formatTime(System.currentTimeMillis()));

        // Set log category and message
        holder.tvLogCategory.setText(log.category);
        holder.tvLogMessage.setText(log.message);
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    private String formatTime(long timeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }

    public static class LogViewHolder extends RecyclerView.ViewHolder {

        ImageView ivLogIcon;
        TextView tvLogType, tvLogTime, tvLogCategory, tvLogMessage;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLogIcon = itemView.findViewById(R.id.ivLogIcon);
            tvLogType = itemView.findViewById(R.id.tvLogType);
            tvLogTime = itemView.findViewById(R.id.tvLogTime);
            tvLogCategory = itemView.findViewById(R.id.tvLogCategory);
            tvLogMessage = itemView.findViewById(R.id.tvLogMessage);
        }
    }
}
