package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonObject;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private TextView tvUsername, tvUserId, tvPhone, tvEmail, tvRegisterTime, tvLastLoginTime, tvAccountStatus;
    private Button btnEditProfile, btnChangePassword, btnDeleteAccount, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        initListeners();
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "页面恢复，刷新用户信息");
        loadData();
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tv_username);
        tvUserId = findViewById(R.id.tv_userId);
        tvPhone = findViewById(R.id.tv_phone);
        tvEmail = findViewById(R.id.tv_email);
        tvRegisterTime = findViewById(R.id.tv_registerTime);
        tvLastLoginTime = findViewById(R.id.tv_lastLoginTime);
        tvAccountStatus = findViewById(R.id.tv_accountStatus);

        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnDeleteAccount = findViewById(R.id.btn_delete_account);
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void initListeners() {
        // 底部导航栏点击事件
        setupBottomNavigation();

        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserChangePassword1Activity.class);
            intent.putExtra("mode", "change");
            startActivity(intent);
        });

        btnDeleteAccount.setOnClickListener(v -> {
            showDeleteAccountDialog();
        });

        btnLogout.setOnClickListener(v -> {
            showLogoutDialog();
        });
    }

    private void setupBottomNavigation() {
        TextView navHome = findViewById(R.id.nav_home);
        TextView navBooks = findViewById(R.id.nav_books);
        TextView navSearch = findViewById(R.id.nav_search);
        TextView navCollection = findViewById(R.id.nav_collection);
        TextView navRecords = findViewById(R.id.nav_records);
        TextView navProfile = findViewById(R.id.nav_profile);

        View.OnClickListener navClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                } else if (id == R.id.nav_books) {
                    startActivity(new Intent(ProfileActivity.this, WordBooksActivity.class));
                } else if (id == R.id.nav_search) {
                    startActivity(new Intent(ProfileActivity.this, WordSearchActivity.class));
                } else if (id == R.id.nav_collection) {
                    startActivity(new Intent(ProfileActivity.this, CollectionsActivity.class));
                } else if (id == R.id.nav_records) {
                    startActivity(new Intent(ProfileActivity.this, StudyRecordsActivity.class));
                }
            }
        };

        navHome.setOnClickListener(navClickListener);
        navBooks.setOnClickListener(navClickListener);
        navSearch.setOnClickListener(navClickListener);
        navCollection.setOnClickListener(navClickListener);
        navRecords.setOnClickListener(navClickListener);

        // 当前页面不设置点击事件
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("注销账号")
                .setMessage("确定要注销账号吗？此操作不可恢复！")
                .setPositiveButton("确定", (dialog, which) -> {
                    startActivity(new Intent(this, UserDeleteAccountActivity.class));
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("退出登录")
                .setMessage("确定要退出登录吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    AuthManager.get().clearAuth();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void loadData() {
        new Thread(() -> {
            try {
                Log.d(TAG, "开始加载用户信息");
                JsonObject res = ApiClient.get().get("/api/user/profile");
                if (res.get("code").getAsInt() == 200) {
                    JsonObject u = res.getAsJsonObject("data");
                    runOnUiThread(() -> updateUI(u));
                } else {
                    runOnUiThread(() -> showError("加载失败"));
                }
            } catch (Exception e) {
                Log.e(TAG, "加载异常: " + e.getMessage(), e);
                runOnUiThread(() -> showError("加载失败: " + e.getMessage()));
            }
        }).start();
    }

    private void updateUI(JsonObject u) {
        tvUsername.setText(getStr(u, "userName"));
        tvUserId.setText(getStr(u, "userId"));
        tvPhone.setText(getStr(u, "phoneNumber"));
        tvEmail.setText(getStr(u, "email"));
        tvRegisterTime.setText(trimDate(getStr(u, "registerTime")));
        tvLastLoginTime.setText(getStr(u, "lastLoginTime"));
        tvAccountStatus.setText(getStr(u, "accountStatus"));
    }

    private String getStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "-";
    }

    private String trimDate(String dt) {
        return dt != null && dt.length() >= 10 ? dt.substring(0, 10) : dt;
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
