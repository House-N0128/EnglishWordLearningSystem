package com.example.wordlearningapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonObject;

public class AdminMainActivity extends AppCompatActivity {

    private LinearLayout content;
    private TextView tvUsers, tvBooks, tvWords, tvToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        if (!AuthManager.get().isLoggedIn() || !"admin".equals(AuthManager.get().getRole())) {
            AuthManager.get().clearAuth();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        ((TextView) findViewById(R.id.toolbar_title)).setText("管理员后台");
        content = findViewById(R.id.content_area);

        findViewById(R.id.toolbar_back).setOnClickListener(v -> {
            AuthManager.get().clearAuth();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        buildUI();
        loadStats();
    }

    private void buildUI() {
        // Welcome
        TextView welcome = new TextView(this);
        welcome.setText("管理员: " + AuthManager.get().getUserId());
        welcome.setTextSize(18);
        welcome.setTextColor(0xFF318af8);
        welcome.setGravity(Gravity.CENTER);
        welcome.setPadding(0, 16, 0, 20);
        content.addView(welcome);

        // Stats grid - 2x2
        LinearLayout statGrid = new LinearLayout(this);
        statGrid.setOrientation(LinearLayout.VERTICAL);
        statGrid.setPadding(0, 0, 0, 24);
        content.addView(statGrid);

        LinearLayout row1 = makeStatRow();
        tvUsers = makeStatCard(row1, "总用户数", "-");
        tvBooks = makeStatCard(row1, "总词书数", "-");
        statGrid.addView(row1);

        LinearLayout row2 = makeStatRow();
        tvWords = makeStatCard(row2, "总单词数", "-");
        tvToday = makeStatCard(row2, "今日学习", "-");
        statGrid.addView(row2);

        // Action buttons
        addButton("📋 词书管理", () -> startActivity(new Intent(this, WordBooksActivity.class)));
        addButton("📝 单词管理", () -> {
            // Navigate to word search as quick word management entry
            startActivity(new Intent(this, WordSearchActivity.class));
        });
        addButton("👥 用户管理", () -> startActivity(new Intent(this, UserManageActivity.class)));

        // Logout
        Button btnLogout = new Button(this);
        btnLogout.setText("退出登录");
        btnLogout.setTextColor(0xFF318af8);
        btnLogout.setTextSize(16);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 32, 0, 0);
        btnLogout.setLayoutParams(lp);
        btnLogout.setOnClickListener(v -> {
            AuthManager.get().clearAuth();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        content.addView(btnLogout);
    }

    private LinearLayout makeStatRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 0, 0, 12);
        return row;
    }

    private TextView makeStatCard(LinearLayout parent, String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackgroundColor(0xFFFFFFFF);
        card.setPadding(16, 16, 16, 16);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        cp.setMargins(0, 0, 12, 0);
        card.setLayoutParams(cp);

        TextView num = new TextView(this);
        num.setText(value);
        num.setTextSize(22);
        num.setTextColor(0xFF318af8);
        num.setGravity(Gravity.CENTER);
        card.addView(num);

        TextView lbl = new TextView(this);
        lbl.setText(label);
        lbl.setTextSize(12);
        lbl.setTextColor(0xFF8899aa);
        lbl.setGravity(Gravity.CENTER);
        lbl.setPadding(0, 4, 0, 0);
        card.addView(lbl);

        parent.addView(card);
        return num;
    }

    private void addButton(String text, Runnable action) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackgroundColor(0xFF318af8);
        btn.setTextSize(16);
        btn.setPadding(16, 14, 16, 14);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0, 0, 0, 12);
        btn.setLayoutParams(bp);
        btn.setOnClickListener(v -> action.run());
        content.addView(btn);
    }

    private void loadStats() {
        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().get("/api/admin/stats");
                if (res.get("code").getAsInt() == 200) {
                    JsonObject s = res.getAsJsonObject("data");
                    runOnUiThread(() -> {
                        tvUsers.setText(String.valueOf(s.has("userCount") ? s.get("userCount").getAsInt() : 0));
                        tvBooks.setText(String.valueOf(s.has("wordBookCount") ? s.get("wordBookCount").getAsInt() : 0));
                        tvWords.setText(String.valueOf(s.has("wordCount") ? s.get("wordCount").getAsInt() : 0));
                        tvToday.setText(String.valueOf(s.has("todayRecordCount") ? s.get("todayRecordCount").getAsInt() : 0));
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "加载统计失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
