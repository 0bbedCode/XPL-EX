package eu.faircode.xlua.x.ui.adapters;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.xlua.log.LogPacket;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
    private final List<LogPacket> logList;
    private final Context context;
    private final UserClientAppContext app;

    public LogAdapter(Context context, List<LogPacket> logList, UserClientAppContext app) {
        this.context = context;
        this.logList = logList;
        this.app = app;
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

        // Reset view state first
        holder.tvLogMessage.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        holder.tvLogMessage.requestLayout();
        // Set log icon, type, and time
        //holder.ivLogIcon.setImageResource(log.getIconResId()); // Assuming a resource ID for icons

        holder.tvLogType.setText(String.valueOf(log.type));
        holder.tvLogTime.setText(log.time > 1 ? formatTime(log.time) : formatTime(System.currentTimeMillis()));

        // Set log category and message
        holder.tvLogCategory.setText(log.category);

        holder.tvLogMessage.setText(log.message);

        if(this.app != null)
            app.setImageView(holder.ivLogIcon, context);

        //TypedValue typedValue = new TypedValue();
        //context.getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, typedValue, true);
        //int height = TypedValue.complexToDimensionPixelSize(typedValue.data, context.getResources().getDisplayMetrics());
        //int iconSize = Math.round(height * context.getResources().getDisplayMetrics().density + 0.5f);
        //UserClientAppContext.attachIcon(context, iconSize, holder.ivLogIcon, );
        //public static void attachIcon(Context context, int iconSize, ImageView imageView, String packageName, int icon)
        //UserClientAppContext()

    }

    private void resetViewHolder(LogViewHolder holder) {
        // Reset message TextView
        holder.tvLogMessage.setText("");
        ViewGroup.LayoutParams params = holder.tvLogMessage.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        holder.tvLogMessage.setLayoutParams(params);

        // Reset other views
        holder.tvLogType.setText("");
        holder.tvLogTime.setText("");
        holder.tvLogCategory.setText("");

        // Force immediate layout
        holder.itemView.requestLayout();
    }

    @Override
    public void onViewRecycled(@NonNull LogViewHolder holder) {
        super.onViewRecycled(holder);
        holder.tvLogMessage.setText("");
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull LogViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        resetViewHolder(holder);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull LogViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            onBindViewHolder(holder, position);
        }
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
