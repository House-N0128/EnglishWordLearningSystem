package com.example.wordlearningapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonObject;

public class AdminEditProfileActivity extends AppCompatActivity {

    private EditText etUserId, etPhone, etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthManager.get().isLoggedIn() || !"admin".equals(AuthManager.get().getRole())) {
            AuthManager.get().clearAuth();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFf2f8fc);

        // ===== TOP BAR =====
        getWindow().setStatusBarColor(0xFF318af8);
        int statusBarH = 0;
        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) statusBarH = getResources().getDimensionPixelSize(resId);

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(0xFF318af8);
        topBar.setPadding(dp(12), statusBarH + dp(8), dp(16), dp(8));
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48) + statusBarH));

        TextView btnBack = new TextView(this);
        btnBack.setText("←");
        btnBack.setTextSize(22);
        btnBack.setTextColor(0xFFFFFFFF);
        btnBack.setTypeface(null, Typeface.BOLD);
        btnBack.setPadding(0, 0, dp(12), 0);
        btnBack.setOnClickListener(v -> finish());
        topBar.addView(btnBack);

        TextView barTitle = new TextView(this);
        barTitle.setText("编辑个人信息");
        barTitle.setTextSize(17);
        barTitle.setTextColor(0xFFFFFFFF);
        barTitle.setTypeface(null, Typeface.BOLD);
        barTitle.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        topBar.addView(barTitle);
        root.addView(topBar);

        // ===== FORM =====
        ScrollView scroll = new ScrollView(this);
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(16), dp(24), dp(16), dp(24));

        // Account
        TextView lblAccount = new TextView(this);
        lblAccount.setText("账号");
        lblAccount.setTextSize(13);
        lblAccount.setTextColor(0xFF8899aa);
        lblAccount.setPadding(0, 0, 0, dp(6));
        form.addView(lblAccount);

        etUserId = new EditText(this);
        etUserId.setHint("请输入管理员账号");
        etUserId.setTextSize(15);
        etUserId.setPadding(dp(14), dp(12), dp(14), dp(12));
        etUserId.setBackgroundColor(0xFFf6f8fc);
        GradientDrawable uidBg = new GradientDrawable();
        uidBg.setColor(0xFFf6f8fc);
        uidBg.setCornerRadius(dp(8));
        uidBg.setStroke(1, 0xFFc7d9ee);
        etUserId.setBackground(uidBg);
        form.addView(etUserId);

        // Phone
        TextView lblPhone = new TextView(this);
        lblPhone.setText("手机号");
        lblPhone.setTextSize(13);
        lblPhone.setTextColor(0xFF8899aa);
        lblPhone.setPadding(0, dp(18), 0, dp(6));
        form.addView(lblPhone);

        etPhone = new EditText(this);
        etPhone.setHint("请输入11位手机号");
        etPhone.setTextSize(15);
        etPhone.setPadding(dp(14), dp(12), dp(14), dp(12));
        etPhone.setBackgroundColor(0xFFf6f8fc);
        GradientDrawable phBg = new GradientDrawable();
        phBg.setColor(0xFFf6f8fc);
        phBg.setCornerRadius(dp(8));
        phBg.setStroke(1, 0xFFc7d9ee);
        etPhone.setBackground(phBg);
        form.addView(etPhone);

        // Email
        TextView lblEmail = new TextView(this);
        lblEmail.setText("邮箱");
        lblEmail.setTextSize(13);
        lblEmail.setTextColor(0xFF8899aa);
        lblEmail.setPadding(0, dp(18), 0, dp(6));
        form.addView(lblEmail);

        etEmail = new EditText(this);
        etEmail.setHint("请输入邮箱地址");
        etEmail.setTextSize(15);
        etEmail.setPadding(dp(14), dp(12), dp(14), dp(12));
        etEmail.setBackgroundColor(0xFFf6f8fc);
        GradientDrawable emBg = new GradientDrawable();
        emBg.setColor(0xFFf6f8fc);
        emBg.setCornerRadius(dp(8));
        emBg.setStroke(1, 0xFFc7d9ee);
        etEmail.setBackground(emBg);
        form.addView(etEmail);

        // Save button
        TextView btnSave = new TextView(this);
        btnSave.setText("保存修改");
        btnSave.setTextSize(16);
        btnSave.setTextColor(0xFFFFFFFF);
        btnSave.setGravity(Gravity.CENTER);
        btnSave.setPadding(0, dp(14), 0, dp(14));
        GradientDrawable saveBg = new GradientDrawable();
        saveBg.setColor(0xFF318af8);
        saveBg.setCornerRadius(dp(22));
        btnSave.setBackground(saveBg);
        LinearLayout.LayoutParams sbp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sbp.setMargins(0, dp(28), 0, 0);
        btnSave.setLayoutParams(sbp);
        btnSave.setOnClickListener(v -> saveProfile());
        form.addView(btnSave);

        scroll.addView(form);
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        setContentView(root);
        loadCurrentProfile();
    }

    private void loadCurrentProfile() {
        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().get("/api/admin/info");
                if (res.get("code").getAsInt() == 200) {
                    JsonObject data = res.getAsJsonObject("data");
                    runOnUiThread(() -> {
                        etUserId.setText(getStr(data, "userId"));
                        etPhone.setText(getStr(data, "phoneNumber"));
                        etEmail.setText(getStr(data, "email"));
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "加载信息失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private String getStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    private void saveProfile() {
        String userId = etUserId.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (userId.isEmpty()) {
            Toast.makeText(this, "账号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!phone.isEmpty() && !phone.matches("\\d{11}")) {
            Toast.makeText(this, "请输入正确的11位手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!email.isEmpty() && !email.contains("@")) {
            Toast.makeText(this, "请输入正确的邮箱地址", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = AuthManager.get().getUserId();
        new Thread(() -> {
            try {
                // 如果账号有变化，先更新账号
                if (!userId.equals(currentUserId)) {
                    JsonObject nameBody = new JsonObject();
                    nameBody.addProperty("adminName", userId);
                    JsonObject nameRes = ApiClient.get().put("/api/admin/updateName", nameBody);
                    int nameCode = nameRes.has("code") ? nameRes.get("code").getAsInt() : -1;
                    if (nameCode != 200) {
                        String nameMsg = nameRes.has("message") ? nameRes.get("message").getAsString() : "账号修改失败";
                        runOnUiThread(() -> Toast.makeText(AdminEditProfileActivity.this, nameMsg, Toast.LENGTH_SHORT).show());
                        return;
                    }
                    // 更新本地存储的账号
                    AuthManager.get().setAuth(userId, "admin");
                }

                // 更新手机号和邮箱
                JsonObject body = new JsonObject();
                body.addProperty("phoneNumber", phone);
                body.addProperty("email", email);
                JsonObject res = ApiClient.get().put("/api/admin/profile", body);
                int code = res.has("code") ? res.get("code").getAsInt() : -1;
                String msg = res.has("message") ? res.get("message").getAsString() : "操作失败";
                runOnUiThread(() -> {
                    Toast.makeText(AdminEditProfileActivity.this, msg, Toast.LENGTH_SHORT).show();
                    if (code == 200) {
                        finish();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(AdminEditProfileActivity.this, "网络错误", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
