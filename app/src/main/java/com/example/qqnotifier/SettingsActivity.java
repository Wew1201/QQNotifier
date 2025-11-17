package com.example.qqnotifier;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Intent;


public class SettingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView; // 确保这是成员变量
    private SettingsAdapter adapter;
    private SettingsRepository repository;
    private List<SettingItem> settingItemList; // 持有数据列表的引用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = new SettingsRepository(this);
        recyclerView = findViewById(R.id.recycler_view_settings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 首次加载数据并创建Adapter
        settingItemList = repository.getSettings();
        adapter = new SettingsAdapter(settingItemList, repository);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 当页面返回时，只刷新数据，不创建新Adapter
        refreshSettingsList();
    }

    private void refreshSettingsList() {
        // 从Repository重新获取最新的数据
        List<SettingItem> latestSettings = repository.getSettings();
        // 更新Adapter持有的数据列表
        settingItemList.clear();
        settingItemList.addAll(latestSettings);
        // 通知Adapter数据已变更，请刷新界面
        adapter.notifyDataSetChanged();
    }


    private void loadSettingsData() {
        settingItemList = new ArrayList<>();

        // --- 从 SharedPreferences 中读取保存的状态 (核心改动) ---
        SharedPreferences prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE);

        // --- 配置1：QQ特别关心 ---
        long qqSettingId = 1;
        // 读取ID为1的配置的开关状态，如果找不到，默认值为 true
        boolean isQqEnabled = prefs.getBoolean("setting_enabled_" + qqSettingId, true);
        settingItemList.add(new SettingItem(
                qqSettingId,
                "转发QQ特别关心",
                "转发包含[特别关心]字样的QQ消息",
                isQqEnabled, // 使用从sp中读取的值
                "com.tencent.mobileqq",
                "[特别关心]"
        ));

        // --- 配置2：微信红包 ---
        long wechatSettingId = 2;
        // 读取ID为2的配置的开关状态，如果找不到，默认值为 false
        boolean isWechatEnabled = prefs.getBoolean("setting_enabled_" + wechatSettingId, false);
        settingItemList.add(new SettingItem(
                2,
                "转发微信红包",
                "转发包含[微信红包]字样的微信消息",
                isWechatEnabled, // 使用从sp中读取的值
                "com.tencent.mm",
                "[微信红包]"
        ));
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            // 点击了添加按钮，跳转到编辑页
            Intent intent = new Intent(this, EditSettingActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}