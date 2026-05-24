package com.example.wordlearningapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonObject;

public class AdminProfileActivity extends AppCompatActivity {

    private TextView tvUserId, tvPhone, tvEmail, tvRole, tvCreateTime;

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

        // ===== TOP BAR (same as AdminMainActivity) =====
        getWindow().setStatusBarColor(0xFF318af8);
        int statusBarH = 0;
        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) statusBarH = getResources().getDimensionPixelSize(resId);

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(0xFF318af8);
        topBar.setPadding(dp(16), statusBarH + dp(8), dp(16), dp(8));
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48) + statusBarH));

        TextView adminUser = new TextView(this);
        adminUser.setText("管理员：" + AuthManager.get().getUserId());
        adminUser.setTextSize(16);
        adminUser.setTextColor(0xFFFFFFFF);
        adminUser.setTypeface(null, Typeface.BOLD);
        adminUser.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        topBar.addView(adminUser);

        TextView btnRefresh = new TextView(this);
        btnRefresh.setText("刷新");
        btnRefresh.setTextSize(15);
        btnRefresh.setTextColor(0xFF318af8);
        btnRefresh.setBackgroundColor(0xFFFFFFFF);
        btnRefresh.setPadding(dp(16), dp(7), dp(16), dp(7));
        btnRefresh.setGravity(Gravity.CENTER);
        GradientDrawable rfBg = new GradientDrawable();
        rfBg.setColor(0xFFFFFFFF);
        rfBg.setCornerRadius(dp(16));
        btnRefresh.setBackground(rfBg);
        btnRefresh.setOnClickListener(v -> loadData());
        topBar.addView(btnRefresh);

        TextView btnLogout = new TextView(this);
        btnLogout.setText("退出登录");
        btnLogout.setTextSize(15);
        btnLogout.setTextColor(0xFF318af8);
        btnLogout.setBackgroundColor(0xFFFFFFFF);
        btnLogout.setPadding(dp(16), dp(7), dp(16), dp(7));
        btnLogout.setGravity(Gravity.CENTER);
        GradientDrawable lgBg = new GradientDrawable();
        lgBg.setColor(0xFFFFFFFF);
        lgBg.setCornerRadius(dp(16));
        btnLogout.setBackground(lgBg);
        btnLogout.setOnClickListener(v -> {
            AuthManager.get().clearAuth();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        topBar.addView(btnLogout);
        root.addView(topBar);

        // ===== SCROLLABLE MAIN =====
        ScrollView scroll = new ScrollView(this);
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(16), dp(20), dp(16), dp(24));

        // Info card
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFFFFFFFF);
        card.setPadding(dp(18), dp(20), dp(18), dp(20));
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(0xFFFFFFFF);
        cardBg.setCornerRadius(dp(14));
        card.setBackground(cardBg);

        TextView cardTitle = new TextView(this);
        cardTitle.setText("管理员信息");
        cardTitle.setTextSize(18);
        cardTitle.setTextColor(0xFF318af8);
        cardTitle.setTypeface(null, Typeface.BOLD);
        cardTitle.setPadding(0, 0, 0, dp(18));
        card.addView(cardTitle);

        tvUserId = addInfoRow(card, "账号");
        tvPhone = addInfoRow(card, "手机号");
        tvEmail = addInfoRow(card, "邮箱");
        tvRole = addInfoRow(card, "角色");
        tvCreateTime = addInfoRow(card, "创建时间");

        main.addView(card);

        // Edit profile button
        TextView btnEdit = new TextView(this);
        btnEdit.setText("编辑个人信息");
        btnEdit.setTextSize(15);
        btnEdit.setTextColor(0xFFFFFFFF);
        btnEdit.setGravity(Gravity.CENTER);
        btnEdit.setPadding(0, dp(13), 0, dp(13));
        GradientDrawable editBg = new GradientDrawable();
        editBg.setColor(0xFF318af8);
        editBg.setCornerRadius(dp(22));
        btnEdit.setBackground(editBg);
        LinearLayout.LayoutParams ebp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ebp.setMargins(0, dp(22), 0, dp(10));
        btnEdit.setLayoutParams(ebp);
        btnEdit.setOnClickListener(v -> startActivity(new Intent(this, AdminEditProfileActivity.class)));
        main.addView(btnEdit);

        // Change password button
        TextView btnPwd = new TextView(this);
        btnPwd.setText("修改密码");
        btnPwd.setTextSize(15);
        btnPwd.setTextColor(0xFF318af8);
        btnPwd.setGravity(Gravity.CENTER);
        btnPwd.setPadding(0, dp(13), 0, dp(13));
        GradientDrawable pwdBg = new GradientDrawable();
        pwdBg.setColor(0xFFFFFFFF);
        pwdBg.setCornerRadius(dp(22));
        pwdBg.setStroke(1, 0xFF318af8);
        btnPwd.setBackground(pwdBg);
        LinearLayout.LayoutParams pbp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pbp.setMargins(0, 0, 0, dp(10));
        btnPwd.setLayoutParams(pbp);
        btnPwd.setOnClickListener(v -> showChangePasswordDialog());
        main.addView(btnPwd);

        scroll.addView(main);
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        // ===== BOTTOM NAVBAR =====
        root.addView(makeNavbar());

        setContentView(root);
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private TextView addInfoRow(LinearLayout parent, String label) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(10), 0, dp(10));
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView lbl = new TextView(this);
        lbl.setText(label);
        lbl.setTextSize(14);
        lbl.setTextColor(0xFF8899aa);
        lbl.setLayoutParams(new LinearLayout.LayoutParams(dp(80), ViewGroup.LayoutParams.WRAP_CONTENT));
        row.addView(lbl);

        TextView val = new TextView(this);
        val.setText("-");
        val.setTextSize(14);
        val.setTextColor(0xFF273245);
        val.setTypeface(null, Typeface.BOLD);
        val.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(val);

        parent.addView(row);
        return val;
    }

    private LinearLayout makeNavbar() {
        LinearLayout navbar = new LinearLayout(this);
        navbar.setOrientation(LinearLayout.HORIZONTAL);
        navbar.setBackgroundColor(0xFFFFFFFF);
        navbar.setPadding(0, dp(8), 0, dp(12));
        navbar.setElevation(dp(8));
        navbar.setWeightSum(5f);
        navbar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(61)));
        navbar.setGravity(Gravity.CENTER);

        navItem(navbar, "主页", false, () -> {
            startActivity(new Intent(this, AdminMainActivity.class));
            finish();
        });
        navItem(navbar, "用户管理", false, () -> {
            startActivity(new Intent(this, UserManageActivity.class));
            finish();
        });
        navItem(navbar, "词书管理", false, () ->
                startActivity(new Intent(this, WordBooksActivity.class)));
        navItem(navbar, "单词管理", false, () ->
                startActivity(new Intent(this, WordSearchActivity.class)));
        navItem(navbar, "个人信息", true, () -> {});
        return navbar;
    }

    private void navItem(LinearLayout parent, String label, boolean active, Runnable action) {
        TextView item = new TextView(this);
        item.setText(label);
        item.setTextSize(13);
        item.setTextColor(active ? 0xFF17c2ae : 0xFF8899aa);
        item.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
        item.setGravity(Gravity.CENTER);
        item.setMaxLines(1);
        item.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        item.setOnClickListener(v -> action.run());
        parent.addView(item);
    }

    private void showChangePasswordDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(16), dp(12), dp(16), 0);

        android.widget.EditText etOld = new android.widget.EditText(this);
        etOld.setHint("请输入原密码");
        etOld.setTextSize(14);
        etOld.setPadding(dp(12), dp(10), dp(12), dp(10));
        etOld.setBackgroundColor(0xFFf6f8fc);
        form.addView(etOld);

        android.widget.EditText etNew = new android.widget.EditText(this);
        etNew.setHint("请输入新密码（至少6位）");
        etNew.setTextSize(14);
        etNew.setPadding(dp(12), dp(10), dp(12), dp(10));
        etNew.setBackgroundColor(0xFFf6f8fc);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        np.setMargins(0, dp(10), 0, 0);
        etNew.setLayoutParams(np);
        form.addView(etNew);

        android.widget.EditText etConfirm = new android.widget.EditText(this);
        etConfirm.setHint("请确认新密码");
        etConfirm.setTextSize(14);
        etConfirm.setPadding(dp(12), dp(10), dp(12), dp(10));
        etConfirm.setBackgroundColor(0xFFf6f8fc);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cp.setMargins(0, dp(10), 0, 0);
        etConfirm.setLayoutParams(cp);
        form.addView(etConfirm);

        new AlertDialog.Builder(this)
                .setTitle("修改密码")
                .setView(form)
                .setPositiveButton("确定", (d, w) -> {
                    String oldPwd = etOld.getText().toString().trim();
                    String newPwd = etNew.getText().toString().trim();
                    String confirmPwd = etConfirm.getText().toString().trim();

                    if (oldPwd.isEmpty()) {
                        Toast.makeText(this, "请输入原密码", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPwd.length() < 6) {
                        Toast.makeText(this, "新密码长度至少6位", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newPwd.equals(confirmPwd)) {
                        Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new Thread(() -> {
                        try {
                            JsonObject body = new JsonObject();
                            body.addProperty("oldPassword", oldPwd);
                            body.addProperty("newPassword", newPwd);
                            body.addProperty("confirmPassword", confirmPwd);
                            JsonObject res = ApiClient.get().post("/api/admin/changePassword", body);
                            int code = res.has("code") ? res.get("code").getAsInt() : -1;
                            String msg = res.has("message") ? res.get("message").getAsString() : "操作失败";
                            runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void loadData() {
        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().get("/api/admin/info");
                if (res.get("code").getAsInt() == 200) {
                    JsonObject data = res.getAsJsonObject("data");
                    runOnUiThread(() -> {
                        tvUserId.setText(getStr(data, "userId"));
                        tvPhone.setText(getStr(data, "phoneNumber"));
                        tvEmail.setText(getStr(data, "email"));
                        tvRole.setText("管理员");
                        tvCreateTime.setText(trimDate(getStr(data, "createTime")));
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "加载失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private String getStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "-";
    }

    private String trimDate(String dt) {
        return dt != null && dt.length() >= 10 ? dt.substring(0, 10) : dt;
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
