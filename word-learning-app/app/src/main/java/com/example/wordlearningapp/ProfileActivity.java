package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonObject;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvUserId, tvPhone, tvEmail, tvRegisterTime, tvLastLoginTime, tvAccountStatus;
    private TextView tvBack;
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
        loadData();
    }

    private void initViews() {
        tvBack = findViewById(R.id.tv_back);
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
        tvBack.setOnClickListener(v -> finish());

        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserChangePassword1Activity.class);
            intent.putExtra("mode", "change");
            startActivity(intent);
        });

        btnDeleteAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, UserDeleteAccountActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            AuthManager.get().clearAuth();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void loadData() {
        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().get("/api/user/profile");
                if (res.get("code").getAsInt() == 200) {
                    JsonObject u = res.getAsJsonObject("data");
                    runOnUiThread(() -> updateUI(u));
                } else {
                    runOnUiThread(() -> showError("加载失败"));
                }
            } catch (Exception e) {
                runOnUiThread(() -> showError("加载失败"));
            }
        }).start();
    }

    private void updateUI(JsonObject u) {
        tvUsername.setText(getStr(u, "userName"));
        tvUserId.setText(getStr(u, "userId"));
        tvPhone.setText(getStr(u, "phoneNumber"));
        tvEmail.setText(getStr(u, "email"));
        tvRegisterTime.setText(trimDate(getStr(u, "registerTime")));
        tvLastLoginTime.setText(trimDate(getStr(u, "lastLoginTime")));
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
