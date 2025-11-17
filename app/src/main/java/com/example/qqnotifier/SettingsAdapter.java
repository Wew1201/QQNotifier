package com.example.qqnotifier;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.Context;
import java.util.List;


public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingViewHolder> {

    private List<SettingItem> settingItems;
    private SettingsRepository repository;

    // 构造函数，接收数据列表
    public SettingsAdapter(List<SettingItem> settingItems, SettingsRepository repository) {
        this.settingItems = settingItems;
        this.repository = repository; // 接收Repository
    }

    // 1. 创建ViewHolder：当RecyclerView需要一个新的列表项视图时调用
    @NonNull
    @Override
    public SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载我们之前设计的 item_setting.xml 布局
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_setting, parent, false);
        return new SettingViewHolder(view);
    }

    // 2. 绑定数据：当RecyclerView需要显示特定位置的数据时调用
    @Override
    public void onBindViewHolder(@NonNull SettingViewHolder holder, int position) {
        SettingItem currentItem = settingItems.get(position);

        holder.title.setText(currentItem.getTitle());
        holder.description.setText(currentItem.getDescription());

        // --- 这是最关键的修正 ---

        // 1. 关键：在设置状态前，先把监听器清空，防止setChecked触发旧的监听器
        holder.enabledSwitch.setOnCheckedChangeListener(null);

        // 2. 根据我们真实的数据模型，设置开关的显示状态
        holder.enabledSwitch.setChecked(currentItem.isEnabled());

        // 3. 在状态设置完毕后，再给开关设置新的监听器
        holder.enabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 因为上一步已经把状态设置好了，所以这个监听器现在只会在用户真正点击时才被触发

            // a. 更新内存中的数据模型
            currentItem.setEnabled(isChecked);

            // b. 通过Repository保存整个列表的最新状态
            if (repository != null) {
                repository.saveSettings(settingItems);
                Context context = holder.itemView.getContext();
                Intent intent = new Intent("com.example.qqnotifier.SETTINGS_UPDATED");
            }
        });

        holder.itemView.setOnLongClickListener(view -> {
            // 当用户长按列表项时，弹出选择对话框

            final Context context = holder.itemView.getContext();
            final int currentPosition = holder.getAdapterPosition();

            // 创建一个包含“修改”和“删除”选项的数组
            final CharSequence[] options = {"修改配置", "删除配置"};

            new AlertDialog.Builder(context)
                    .setTitle("操作选项")
                    .setItems(options, (dialog, which) -> {
                        // which 参数表示用户点击了哪个选项 (0: 修改, 1: 删除)
                        if (which == 0) {
                            // --- 用户选择了“修改” ---
                            Intent intent = new Intent(context, EditSettingActivity.class);
                            // 关键：将当前配置项的ID传递给编辑页面
                            intent.putExtra("setting_id", currentItem.getId());
                            context.startActivity(intent);

                        } else if (which == 1) {
                            // --- 用户选择了“删除” ---
                            // 弹出一个二次确认对话框
                            new AlertDialog.Builder(context)
                                    .setTitle("确认删除")
                                    .setMessage("确定要删除配置 '" + currentItem.getTitle() + "' 吗？")
                                    .setPositiveButton("删除", (d, w) -> {
                                        deleteItem(currentPosition);
                                    })
                                    .setNegativeButton("取消", null)
                                    .show();
                        }
                    })
                    .show();

            return true; // 表示事件已处理
        });

    }
    private void deleteItem(int position) {
        // 1. 从内存中的数据列表里移除这一项
        settingItems.remove(position);

        // 2. 通知RecyclerView，有一个项目在position位置被移除了
        // notifyItemRemoved会有好看的动画效果
        notifyItemRemoved(position);

        // (可选) 如果你想让后续项目的位置也立刻更新，可以调用下面这行
        // notifyItemRangeChanged(position, settingItems.size());

        // 3. 通过Repository保存更新后的、已经删除了项目的列表
        if (repository != null) {
            repository.saveSettings(settingItems);
        }
    }

    // 3. 获取项目总数：告诉RecyclerView我们总共有多少条数据
    @Override
    public int getItemCount() {
        return settingItems.size();
    }

    // ViewHolder类：它就像一个缓存器，持有 item_setting.xml 布局中所有控件的引用
    public static class SettingViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView description;
        public SwitchCompat enabledSwitch;

        public SettingViewHolder(@NonNull View itemView) {
            super(itemView);
            // 通过ID找到布局中的控件
            title = itemView.findViewById(R.id.text_setting_title);
            description = itemView.findViewById(R.id.text_setting_description);
            enabledSwitch = itemView.findViewById(R.id.switch_setting_enabled);
        }
    }
}