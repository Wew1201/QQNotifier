package com.example.qqnotifier;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

    // --- 防抖动变量 ---
    private String lastProcessedKey = "";
    private long lastProcessedTime = 0;
    private static final long MIN_INTERVAL = 1000;

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

        // 1. 基础信息获取
        String packageName = sbn.getPackageName();
        Notification notification = sbn.getNotification();
        if (notification == null) return;

        Bundle extras = notification.extras;
        String title = extras.getString(Notification.EXTRA_TITLE, "");
        String text = extras.getString(Notification.EXTRA_TEXT, "");

        // --- 2. 防抖动检查 ---
        String currentKey = packageName + "|" + title + "|" + text;
        long currentTime = System.currentTimeMillis();

        if (currentKey.equals(lastProcessedKey) && (currentTime - lastProcessedTime < MIN_INTERVAL)) {
            Log.d(TAG, "检测到重复通知，已拦截: " + title);
            return;
        }

        lastProcessedKey = currentKey;
        lastProcessedTime = currentTime;

        // --- 3. 读取配置 ---
        List<SettingItem> allSettings = repository.getSettings();
        List<SettingItem> enabledSettings = new ArrayList<>();
        for (SettingItem item : allSettings) {
            if (item.isEnabled()) {
                enabledSettings.add(item);
            }
        }

        if (enabledSettings.isEmpty()) {
            return;
        }

        // --- 4. 获取原始通知的关键数据 ---
        PendingIntent originalIntent = notification.contentIntent;

        Log.d(TAG, "收到通知 -> 来自: " + packageName + ", 标题: " + title);

        for (SettingItem setting : enabledSettings) {
            if (setting.getTargetPackageName().equals(packageName)) {
                if (isMatch(title, text, setting.getFilterKeyword())) {
                    Log.d(TAG, "通知命中了策略: '" + setting.getTitle() + "'");

                    // 1. 发送我们的转发通知 (直接使用原始Intent，确保跳转成功)
                    forwardNotification(title, text, originalIntent);

                    // 2. [核心修改] 发送完后，立刻销毁原始通知，防止堆积
                    // 注意：有些系统可能会限制连续操作，加一个try-catch保护
                    try {
                        cancelNotification(sbn.getKey());
                        Log.d(TAG, "已自动销毁原始通知");
                    } catch (Exception e) {
                        Log.e(TAG, "销毁原始通知失败", e);
                    }

                    return;
                }
            }
        }
    }

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

    // --- 回归简单的转发逻辑 ---
    private void forwardNotification(String title, String text, PendingIntent contentIntent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MY_APP_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true);

        // 直接设置原始的跳转动作，不再使用中间人
        if (contentIntent != null) {
            builder.setContentIntent(contentIntent);
        }

        int notificationId = (int) System.currentTimeMillis();
        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "转发通知成功！ID: " + notificationId);
        } else {
            Log.e(TAG, "NotificationManager 未初始化，转发失败！");
        }
    }
}