package com.example.qqnotifier;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Button buttonOpenSettings;
    private TextView textViewStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 使用我们刚刚修改的布局文件
        setContentView(R.layout.activity_main);

        // 通过ID找到布局文件中的控件
        buttonOpenSettings = findViewById(R.id.button_open_settings);
        textViewStatus = findViewById(R.id.text_view_status);

        // 为按钮设置点击事件监听器
        buttonOpenSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 当按钮被点击时，跳转到系统的通知使用权设置页面
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivity(intent);
            }
        });
    }

    // 当用户从设置页面返回到我们的App时，会调用 onResume 方法
    @Override
    protected void onResume() {
        super.onResume();
        // 每次返回时，都检查一次权限状态
        updatePermissionStatus();
    }

    // 检查并更新权限状态的自定义方法
    private void updatePermissionStatus() {
        if (isNotificationListenerEnabled()) {
            textViewStatus.setText("当前权限状态：已开启");
            textViewStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            textViewStatus.setText("当前权限状态：未开启");
            textViewStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    // 判断我们的服务是否被用户授权的方法
    private boolean isNotificationListenerEnabled() {
        // 获取系统中所有被授权的通知监听器
        Set<String> enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(this);
        // 判断我们的应用包名是否在其中
        return enabledListeners.contains(getPackageName());
    }
}