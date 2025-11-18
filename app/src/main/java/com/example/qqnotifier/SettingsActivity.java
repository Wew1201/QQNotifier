package com.example.qqnotifier;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import com.google.android.material.appbar.AppBarLayout;


public class SettingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SettingsAdapter adapter;
    private SettingsRepository repository;
    private List<SettingItem> settingItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 关键：在 setContentView 之前调用，让 Activity 的窗口自己处理 Insets
        // EdgeToEdge.enable(this); // 如果你的minSdk是21+，这是一个更现代的选择，但我们用手动方式确保兼容性
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // --- 1. 获取所有视图引用 ---
        final AppBarLayout appBar = findViewById(R.id.app_bar);
        final RecyclerView recyclerView = findViewById(R.id.recycler_view_settings);
        View mainContent = findViewById(R.id.main_content);
        Toolbar toolbar = findViewById(R.id.toolbar);

        // --- 2. 设置Toolbar ---
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

//        // --- 3. 核心修正：这是处理 WindowInsets 的标准、可靠方式 ---
//        ViewCompat.setOnApplyWindowInsetsListener(mainContent, (v, insets) -> {
//            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
//            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
//
//            // 获取原始的内边距，以防万一它有自己的padding
//            int originalAppBarPaddingTop = appBar.getPaddingTop();
//            int originalRecyclerPaddingBottom = recyclerView.getPaddingBottom();
//
//            // 在原始内边距的基础上，增加系统栏的高度
//            appBar.setPadding(appBar.getPaddingLeft(), originalAppBarPaddingTop + statusBarHeight, appBar.getPaddingRight(), appBar.getPaddingBottom());
//            recyclerView.setPadding(recyclerView.getPaddingLeft(), recyclerView.getPaddingTop(), recyclerView.getPaddingRight(), originalRecyclerPaddingBottom + navBarHeight);
//
//            // 很重要：返回一个空的 Insets，表示我们已经“消费”了这个事件，不希望系统再为子视图做多余的处理
//            return WindowInsetsCompat.CONSUMED;
//        });

        // --- 4. 初始化数据和列表 (这部分逻辑不变) ---
        repository = new SettingsRepository(this);
        this.recyclerView = recyclerView; // 确保成员变量被赋值
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        settingItemList = repository.getSettings();
        adapter = new SettingsAdapter(settingItemList, repository);
        this.recyclerView.setAdapter(adapter);
    }

    // onResume 保持不变，它会在页面返回时调用 refreshSettingsList
    @Override
    protected void onResume() {
        super.onResume();
        refreshSettingsList();
    }

    // --- 修正 refreshSettingsList 方法 ---
    private void refreshSettingsList() {
        // 确保 repository 和 settingItemList 都不是 null
        if (repository == null || settingItemList == null || adapter == null) {
            return; // 如果尚未初始化，则不执行任何操作
        }

        List<SettingItem> latestSettings = repository.getSettings();
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