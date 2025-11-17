package com.example.qqnotifier;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SettingsRepository {

    private static final String PREFS_NAME = "app_settings_storage";
    private static final String SETTINGS_KEY = "settings_list_json";


    private SharedPreferences sharedPreferences;

    private SettingsRepository repository;
    private Gson gson;

    public SettingsRepository(Context context) {
        // 使用 ApplicationContext 防止内存泄漏
        this.sharedPreferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    // 从SharedPreferences读取完整的配置列表
    public List<SettingItem> getSettings() {
        String json = sharedPreferences.getString(SETTINGS_KEY, null);
        if (json == null) {
            // 如果是第一次，返回一个包含默认配置的初始列表
            return createDefaultSettings();
        }

        // 使用Gson将JSON字符串反序列化为List<SettingItem>
        Type type = new TypeToken<ArrayList<SettingItem>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // 将完整的配置列表保存到SharedPreferences
    public void saveSettings(List<SettingItem> settings) {
        // 使用Gson将List<SettingItem>序列化为JSON字符串
        String json = gson.toJson(settings);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SETTINGS_KEY, json);
        editor.apply();
    }

    // 创建初始的默认配置
    private List<SettingItem> createDefaultSettings() {
        List<SettingItem> defaultList = new ArrayList<>();
        defaultList.add(new SettingItem(System.currentTimeMillis(), "转发QQ特别关心", "转发包含[特别关心]字样的QQ消息", true, "com.tencent.mobileqq", "[特别关心]"));
        // 保存一次，以便下次能读到
        saveSettings(defaultList);
        return defaultList;
    }

}

