package com.example.qqnotifier;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

public class QQNotificationListener extends NotificationListenerService {

    private static final String TAG = "QQNotifierListener";
    private static final String MY_APP_CHANNEL_ID = "qq_special_care_channel";
    private static final String MY_APP_CHANNEL_NAME = "QQ特别关心转发";

    private NotificationManager notificationManager;
    private SettingsRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        repository = new SettingsRepository(this);
        createNotificationChannel();
        Log.d(TAG, "服务已创建并初始化。");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        // --- 核心改动：不再使用内存缓存，每次都直接从Repository读取最新配置 ---
        List<SettingItem> allSettings = repository.getSettings();
        List<SettingItem> enabledSettings = new ArrayList<>();
        for (SettingItem item : allSettings) {
            if (item.isEnabled()) {
                enabledSettings.add(item);
            }
        }

        if (enabledSettings.isEmpty()) {
            return; // 如果没有任何启用的配置，直接返回
        }

        // --- 后面的逻辑和之前完全一样 ---
        String packageName = sbn.getPackageName();
        Notification notification = sbn.getNotification();
        if (notification == null) return;

        Bundle extras = notification.extras;
        String title = extras.getString(Notification.EXTRA_TITLE, "");
        String text = extras.getString(Notification.EXTRA_TEXT, "");

        Log.d(TAG, "收到通知 -> 来自: " + packageName + ", 标题: " + title);

        for (SettingItem setting : enabledSettings) {
            if (setting.getTargetPackageName().equals(packageName)) {
                if (isMatch(title, text, setting.getFilterKeyword())) {
                    Log.d(TAG, "通知命中了策略: '" + setting.getTitle() + "'");
                    forwardNotification(title, text);
                    return;
                }
            }
        }
    }

    // ... isMatch, createNotificationChannel, forwardNotification 方法保持不变 ...
    private boolean isMatch(String title, String text, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return false;
        }
        return title.contains(keyword) || text.contains(keyword);
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    MY_APP_CHANNEL_ID,
                    MY_APP_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("用于转发自定义的通知");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void forwardNotification(String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MY_APP_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true);

        int notificationId = (int) System.currentTimeMillis();
        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "转发通知成功！ID: " + notificationId);
        } else {
            Log.e(TAG, "NotificationManager 未初始化，转发失败！");
        }
    }
}