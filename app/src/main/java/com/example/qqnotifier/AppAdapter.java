package com.example.qqnotifier;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {

    public interface OnAppClickListener {
        void onAppClick(AppInfo app);
    }

    private List<AppInfo> appList;
    private OnAppClickListener listener;

    public AppAdapter(List<AppInfo> appList, OnAppClickListener listener) {
        this.appList = appList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo currentApp = appList.get(position);
        holder.appName.setText(currentApp.getAppName());
        holder.appIcon.setImageDrawable(currentApp.getIcon());
        holder.itemView.setOnClickListener(v -> listener.onAppClick(currentApp));
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public static class AppViewHolder extends RecyclerView.ViewHolder {
        public ImageView appIcon;
        public TextView appName;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.image_app_icon);
            appName = itemView.findViewById(R.id.text_app_name);
        }
    }
}