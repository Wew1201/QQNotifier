package com.example.qqnotifier;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class EditSettingActivity extends AppCompatActivity {

    private TextInputEditText editTextTitle;
    private TextInputEditText editTextPackageName;
    private TextInputEditText editTextKeyword;
    private Button buttonSave;
    private SettingsRepository repository;

    private boolean isEditMode = false; // 标记当前是新建模式还是编辑模式
    private long editingItemId = -1; // 如果是编辑模式，记录正在编辑的item的ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_setting);

        Toolbar toolbar = findViewById(R.id.toolbar_edit);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = new SettingsRepository(this);
        editTextTitle = findViewById(R.id.edit_text_title);
        editTextPackageName = findViewById(R.id.edit_text_package_name);
        editTextKeyword = findViewById(R.id.edit_text_keyword);
        buttonSave = findViewById(R.id.button_save);

        // --- 核心改动：检查启动意图，判断是新建还是编辑 ---
        if (getIntent().hasExtra("setting_id")) {
            isEditMode = true;
            editingItemId = getIntent().getLongExtra("setting_id", -1);
            getSupportActionBar().setTitle("修改配置"); // 修改标题栏文字
            loadSettingData();
        } else {
            isEditMode = false;
            getSupportActionBar().setTitle("新建配置");
        }

        buttonSave.setOnClickListener(v -> saveSetting());
    }

    // 如果是编辑模式，加载已有数据显示到输入框
    private void loadSettingData() {
        if (isEditMode && editingItemId != -1) {
            List<SettingItem> settings = repository.getSettings();
            for (SettingItem item : settings) {
                if (item.getId() == editingItemId) {
                    editTextTitle.setText(item.getTitle());
                    editTextPackageName.setText(item.getTargetPackageName());
                    editTextKeyword.setText(item.getFilterKeyword());
                    break;
                }
            }
        }
    }

    private void saveSetting() {
        String title = editTextTitle.getText().toString().trim();
        String packageName = editTextPackageName.getText().toString().trim();
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