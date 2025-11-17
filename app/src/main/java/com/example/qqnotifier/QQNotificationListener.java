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

public class QQNotificationListener extends NotificationListenerService {

    private static final String TAG = "QQNotifierListener";
    private static final String QQ_PACKAGE_NAME = "com.tencent.mobileqq";
    private static final String SPECIAL_CARE_TAG = "[特别关心]";

    // 定义我们自己App发通知用的渠道ID和名称
    private static final String MY_APP_CHANNEL_ID = "qq_special_care_channel";
    private static final String MY_APP_CHANNEL_NAME = "QQ特别关心转发";

    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        // 获取系统通知管理器
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // 创建通知渠道
        createNotificationChannel();
        Log.d(TAG, "服务已创建并初始化。");
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d(TAG, "服务已连接成功！");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        // --- 规则一：判断是不是来自QQ ---
        if (!sbn.getPackageName().equals(QQ_PACKAGE_NAME)) {
            return; // 如果不是QQ的通知，直接跳过，不处理
        }

        // 获取通知的详细信息
        Notification notification = sbn.getNotification();
        if (notification == null) {
            return;
        }

        // 从通知中提取标题和内容
        Bundle extras = notification.extras;
        String title = extras.getString(Notification.EXTRA_TITLE, "");
        String text = extras.getString(Notification.EXTRA_TEXT, "");

        Log.d(TAG, "收到QQ通知 -> 标题: " + title + ", 内容: " + text);

        // --- 规则二：判断是不是特别关心的通知 ---
        if (title.contains(SPECIAL_CARE_TAG) || text.contains(SPECIAL_CARE_TAG)) {
            Log.d(TAG, "匹配到特别关心消息！准备转发...");
            // 如果是，就调用我们的转发方法
            forwardNotification(title, text);
        }
    }

    // 创建我们App用来发通知的渠道（安卓8.0及以上版本需要）
    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    MY_APP_CHANNEL_ID,
                    MY_APP_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH // 确保这是 HIGH
            );
            // 添加描述，让用户在系统设置里能看懂
            channel.setDescription("用于转发QQ特别关心的通知");
            // 确保渠道可以显示横幅通知
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // 转发通知的方法
    private void forwardNotification(String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MY_APP_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // 确保优先级是 HIGH 或 MAX
                .setCategory(NotificationCompat.CATEGORY_MESSAGE) // 告诉系统这是一条“消息”类型的通知
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