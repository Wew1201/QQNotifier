package com.example.qqnotifier;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppPickerActivity extends AppCompatActivity implements AppAdapter.OnAppClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_app_picker);

        Toolbar toolbar = findViewById(R.id.toolbar_app_picker);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view_apps);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 注意：在真实项目中，这个耗时操作应该放在后台线程
        List<AppInfo> installedApps = getInstalledApps();
        AppAdapter adapter = new AppAdapter(installedApps, this);
        recyclerView.setAdapter(adapter);
    }

    private List<AppInfo> getInstalledApps() {
        PackageManager pm = getPackageManager();
        List<AppInfo> apps = new ArrayList<>();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            // 过滤掉系统应用
            if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                String appName = packageInfo.loadLabel(pm).toString();
                apps.add(new AppInfo(appName, packageInfo.packageName, packageInfo.loadIcon(pm)));
            }
        }
        // 按应用名称排序
        Collections.sort(apps, (a1, a2) -> a1.getAppName().compareToIgnoreCase(a2.getAppName()));
        return apps;
    }

    @Override
    public void onAppClick(AppInfo app) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_app_name", app.getAppName());
        resultIntent.putExtra("selected_package_name", app.getPackageName());
        setResult(Activity.RESULT_OK, resultIntent);
        finish(); // 关闭当前页面，返回结果
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}