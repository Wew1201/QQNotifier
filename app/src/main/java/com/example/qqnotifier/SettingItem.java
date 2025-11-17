package com.example.qqnotifier;
public class SettingItem {
    private long id;
    private String title;
    private String description;
    private boolean isEnabled;
    private String targetPackageName;
    private String filterKeyword;

    // 构造函数
    public SettingItem(long id, String title, String description, boolean isEnabled, String targetPackageName, String filterKeyword) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isEnabled = isEnabled;
        this.targetPackageName = targetPackageName;
        this.filterKeyword = filterKeyword;
    }

    // --- 下面全是Getter和Setter方法，用于获取和设置类的属性 ---

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public String getTargetPackageName() {
        return targetPackageName;
    }

    public void setTargetPackageName(String targetPackageName) {
        this.targetPackageName = targetPackageName;
    }

    public String getFilterKeyword() {
        return filterKeyword;
    }

    public void setFilterKeyword(String filterKeyword) {
        this.filterKeyword = filterKeyword;
    }
}