package com.example.qqnotifier;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class EditSettingActivity extends AppCompatActivity {

    private TextInputEditText editTextTitle;

    private TextInputEditText editTextKeyword;
    private Button buttonSave;
    private SettingsRepository repository;

    private boolean isEditMode = false; // 标记当前是新建模式还是编辑模式
    private long editingItemId = -1; // 如果是编辑模式，记录正在编辑的item的ID

    private TextView textSelectedApp;
    private Button buttonChooseApp;
    private String selectedPackageName = ""; // 用于存储选择结果

    // --- 新增 ActivityResultLauncher ---
    private final ActivityResultLauncher<Intent> appPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String appName = data.getStringExtra("selected_app_name");
                    selectedPackageName = data.getStringExtra("selected_package_name");
                    textSelectedApp.setText(appName + "\n(" + selectedPackageName + ")");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_setting);

        // --- 1. 必须在所有逻辑判断之前，无条件初始化所有控件！ ---
        Toolbar toolbar = findViewById(R.id.toolbar_edit);
        editTextTitle = findViewById(R.id.edit_text_title);
        editTextKeyword = findViewById(R.id.edit_text_keyword);
        buttonSave = findViewById(R.id.button_save);
        textSelectedApp = findViewById(R.id.text_selected_app);
        buttonChooseApp = findViewById(R.id.button_choose_app);

        // --- 2. 初始化 Toolbar 和 Repository ---
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        repository = new SettingsRepository(this);

        // --- 3. 检查启动意图，并根据模式执行不同操作 ---
        if (getIntent().hasExtra("setting_id")) {
            isEditMode = true;
            editingItemId = getIntent().getLongExtra("setting_id", -1);
            getSupportActionBar().setTitle("修改配置");
            loadSettingData(); // 此时所有控件都已确保被初始化
        } else {
            isEditMode = false;
            getSupportActionBar().setTitle("新建配置");
        }

        // --- 4. 设置按钮点击事件 ---
        buttonChooseApp.setOnClickListener(v -> {
            Intent intent = new Intent(this, AppPickerActivity.class);
            appPickerLauncher.launch(intent);
        });
        buttonSave.setOnClickListener(v -> saveSetting());
    }

    // 如果是编辑模式，加载已有数据显示到输入框
    private void loadSettingData() {
        if (isEditMode && editingItemId != -1) {
            List<SettingItem> settings = repository.getSettings();
            for (SettingItem item : settings) {
                if (item.getId() == editingItemId) {
                    editTextTitle.setText(item.getTitle());

                    // 这是新的、正确的显示包名的方式
                    selectedPackageName = item.getTargetPackageName();
                    // 更好的体验是同时显示AppName，我们暂时先只显示包名
                    textSelectedApp.setText(selectedPackageName);

                    editTextKeyword.setText(item.getFilterKeyword());
                    break;
                }
            }
        }
    }

    private void saveSetting() {
        String title = editTextTitle.getText().toString().trim();
        String packageName = selectedPackageName;
        String keyword = editTextKeyword.getText().toString().trim();

        if (title.isEmpty() || packageName.isEmpty() || keyword.isEmpty()) {
            Toast.makeText(this, "所有字段都不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        List<SettingItem> currentSettings = repository.getSettings();

        if (isEditMode) {
            // --- 编辑模式的保存逻辑 ---
            for (SettingItem item : currentSettings) {
                if (item.getId() == editingItemId) {
                    item.setTitle(title);
                    item.setTargetPackageName(packageName);
                    item.setFilterKeyword(keyword);
                    // 注意：我们不在这里修改isEnabled状态，那个由列表页的开关控制
                    break;
                }
            }
        } else {
            // --- 新建模式的保存逻辑 ---
            SettingItem newItem = new SettingItem(System.currentTimeMillis(), title, "", true, packageName, keyword);
            currentSettings.add(newItem);
        }

        repository.saveSettings(currentSettings);
        Toast.makeText(this, "保存成功!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}