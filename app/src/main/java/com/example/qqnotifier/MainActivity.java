package com.example.qqnotifier;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Button buttonRequestNotificationPermission;
    private Button buttonOpenListenerSettings;

    private Button buttonGoToSettings;
    private TextView textViewStatus;

    // 注册一个权限请求的回调
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // 用户同意了权限
                    Toast.makeText(this, "发送通知权限已获取！", Toast.LENGTH_SHORT).show();
                } else {
                    // 用户拒绝了权限
                    Toast.makeText(this, "您拒绝了发送通知权限", Toast.LENGTH_SHORT).show();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 通过ID找到新的控件
        buttonRequestNotificationPermission = findViewById(R.id.button_request_notification_permission);
        buttonOpenListenerSettings = findViewById(R.id.button_open_listener_settings);
        textViewStatus = findViewById(R.id.text_view_status);

        // --- 为新按钮设置点击事件 ---
        buttonRequestNotificationPermission.setOnClickListener(v -> {
            // 检查安卓版本，因为这个权限只在 Android 13 (API 33) 及以上版本需要动态请求
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // 检查是否已经有权限
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "已经拥有发送通知权限", Toast.LENGTH_SHORT).show();
                } else {
                    // 发起权限请求
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            } else {
                // 对于旧版本系统，权限是默认授予的
                Toast.makeText(this, "当前系统版本无需动态请求此权限", Toast.LENGTH_SHORT).show();
            }
        });

        // --- 为旧按钮设置点击事件 ---
        buttonOpenListenerSettings.setOnClickListener(v -> {
            // 跳转到系统的通知使用权设置页面
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        });

        buttonGoToSettings = findViewById(R.id.button_go_to_settings);

// 为配置按钮设置点击事件
        buttonGoToSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次返回时，都检查一次通知读取服务的权限状态
        updateListenerPermissionStatus();
    }

    private void updateListenerPermissionStatus() {
        if (isNotificationListenerEnabled()) {
            textViewStatus.setText("服务权限状态：已开启");
            textViewStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            textViewStatus.setText("服务权限状态：未开启");
            textViewStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private boolean isNotificationListenerEnabled() {
        Set<String> enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(this);
        return enabledListeners.contains(getPackageName());
    }

}