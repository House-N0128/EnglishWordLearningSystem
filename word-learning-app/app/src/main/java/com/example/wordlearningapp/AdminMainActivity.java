package com.example.wordlearningapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class AdminMainActivity extends AppCompatActivity {

    private TextView tvUsers, tvBooks, tvWords, tvToday;
    private LinearLayout bookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthManager.get().isLoggedIn() || !"admin".equals(AuthManager.get().getRole())) {
            AuthManager.get().clearAuth();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Root layout
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFf2f8fc);

        // ===== TOP BAR: 48px blue =====
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(0xFF318af8);
        topBar.setPadding(dp(16), 0, dp(16), 0);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));

        TextView adminUser = new TextView(this);
        adminUser.setText("管理员：" + AuthManager.get().getUserId());
        adminUser.setTextSize(16);
        adminUser.setTextColor(0xFFFFFFFF);
        adminUser.setTypeface(null, Typeface.BOLD);
        adminUser.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        topBar.addView(adminUser);

        TextView btnLogout = new TextView(this);
        btnLogout.setText("退出登录");
        btnLogout.setTextSize(15);
        btnLogout.setTextColor(0xFF318af8);
        btnLogout.setBackgroundColor(0xFFFFFFFF);
        btnLogout.setPadding(dp(20), dp(7), dp(20), dp(7));
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
        main.setPadding(dp(12), dp(20), dp(12), dp(24));

        // Title
        TextView dashTitle = new TextView(this);
        dashTitle.setText("平台数据总览");
        dashTitle.setTextSize(19);
        dashTitle.setTextColor(0xFF318af8);
        dashTitle.setTypeface(null, Typeface.BOLD);
        dashTitle.setPadding(0, 0, 0, dp(10));
        main.addView(dashTitle);

        // ===== 2x2 STATS GRID =====
        LinearLayout statsGrid = new LinearLayout(this);
        statsGrid.setOrientation(LinearLayout.VERTICAL);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        tvUsers = makeStatCard(row1, "总用户数");
        tvBooks = makeStatCard(row1, "总词书数");
        statsGrid.addView(row1);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        tvWords = makeStatCard(row2, "总单词数");
        tvToday = makeStatCard(row2, "今日新增学习记录");
        statsGrid.addView(row2);

        main.addView(statsGrid);

        // ===== BOOK LIST SECTION =====
        LinearLayout sectionBlock = new LinearLayout(this);
        sectionBlock.setOrientation(LinearLayout.VERTICAL);
        sectionBlock.setBackgroundColor(0xFFFFFFFF);
        sectionBlock.setPadding(dp(15), dp(15), dp(10), dp(15));
        GradientDrawable sbBg = new GradientDrawable();
        sbBg.setColor(0xFFFFFFFF);
        sbBg.setCornerRadius(dp(11));
        sectionBlock.setBackground(sbBg);
        LinearLayout.LayoutParams sbp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sbp.setMargins(0, dp(18), 0, dp(10));
        sectionBlock.setLayoutParams(sbp);

        TextView sectionTitle = new TextView(this);
        sectionTitle.setText("热门词书TOP3");
        sectionTitle.setTextSize(16);
        sectionTitle.setTextColor(0xFF318af8);
        sectionTitle.setTypeface(null, Typeface.BOLD);
        sectionTitle.setPadding(0, 0, 0, dp(14));
        sectionBlock.addView(sectionTitle);

        bookList = new LinearLayout(this);
        bookList.setOrientation(LinearLayout.VERTICAL);
        sectionBlock.addView(bookList);

        main.addView(sectionBlock);

        scroll.addView(main);
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        // ===== BOTTOM NAVBAR =====
        LinearLayout navbar = new LinearLayout(this);
        navbar.setOrientation(LinearLayout.HORIZONTAL);
        navbar.setBackgroundColor(0xFFFFFFFF);
        navbar.setPadding(0, dp(8), 0, dp(12));
        GradientDrawable nbBg = new GradientDrawable();
        nbBg.setColor(0xFFFFFFFF);
        nbBg.setCornerRadii(new float[]{dp(14), dp(14), dp(14), dp(14), 0, 0, 0, 0});
        navbar.setBackground(nbBg);
        navbar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(61)));
        navbar.setGravity(Gravity.CENTER);

        addNavItem(navbar, "", "主页", true, () -> {});
        addNavItem(navbar, "", "用户管理", false, () ->
                startActivity(new Intent(this, UserManageActivity.class)));
        addNavItem(navbar, "", "词书管理", false, () ->
                startActivity(new Intent(this, WordBooksActivity.class)));
        addNavItem(navbar, "", "单词管理", false, () ->
                startActivity(new Intent(this, WordSearchActivity.class)));

        root.addView(navbar);
        setContentView(root);
        loadData();
    }

    private TextView makeStatCard(LinearLayout parent, String label) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackgroundColor(0xFFFFFFFF);
        card.setPadding(0, dp(13), 0, dp(13));
        GradientDrawable cd = new GradientDrawable();
        cd.setColor(0xFFFFFFFF);
        cd.setCornerRadius(dp(13));
        card.setBackground(cd);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        cp.setMargins(0, 0, dp(13), 0);
        card.setLayoutParams(cp);

        TextView num = new TextView(this);
        num.setText("-");
        num.setTextSize(19);
        num.setTextColor(0xFF318af8);
        num.setTypeface(null, Typeface.BOLD);
        num.setGravity(Gravity.CENTER);
        card.addView(num);

        TextView lbl = new TextView(this);
        lbl.setText(label);
        lbl.setTextSize(14);
        lbl.setTextColor(0xFF273245);
        lbl.setGravity(Gravity.CENTER);
        card.addView(lbl);

        parent.addView(card);
        return num;
    }

    private void addNavItem(LinearLayout parent, String icon, String label,
                            boolean active, Runnable action) {
        TextView item = new TextView(this);
        item.setText(icon.isEmpty() ? label : icon + "\n" + label);
        item.setTextSize(active ? 15 : 15);
        item.setTextColor(active ? 0xFF17c2ae : 0xFF318af8);
        item.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
        item.setGravity(Gravity.CENTER);
        item.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        item.setOnClickListener(v -> action.run());
        parent.addView(item);
    }

    private void loadData() {
        new Thread(() -> {
            try {
                JsonObject r = ApiClient.get().get("/api/admin/stats");
                if (r.get("code").getAsInt() == 200) {
                    JsonObject s = r.getAsJsonObject("data");
                    runOnUiThread(() -> {
                        tvUsers.setText(String.valueOf(s.has("userCount") ? s.get("userCount").getAsInt() : 0));
                        tvBooks.setText(String.valueOf(s.has("wordBookCount") ? s.get("wordBookCount").getAsInt() : 0));
                        tvWords.setText(String.valueOf(s.has("wordCount") ? s.get("wordCount").getAsInt() : 0));
                        tvToday.setText(String.valueOf(s.has("todayRecordCount") ? s.get("todayRecordCount").getAsInt() : 0));
                    });
                }

                JsonObject br = ApiClient.get().get("/api/wordbooks");
                if (br.get("code").getAsInt() == 200) {
                    JsonArray books = br.getAsJsonArray("data");
                    runOnUiThread(() -> {
                        bookList.removeAllViews();
                        for (int i = 0; i < Math.min(books.size(), 3); i++) {
                            JsonObject b = books.get(i).getAsJsonObject();
                            LinearLayout item = new LinearLayout(this);
                            item.setOrientation(LinearLayout.HORIZONTAL);
                            GradientDrawable ib = new GradientDrawable();
                            ib.setColor(0x19318af8);
                            ib.setCornerRadius(dp(7));
                            item.setBackground(ib);
                            item.setPadding(dp(10), dp(10), dp(13), dp(10));
                            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            ip.setMargins(0, 0, 0, dp(12));
                            item.setLayoutParams(ip);

                            TextView name = new TextView(this);
                            name.setText(b.has("wordBookName") ? b.get("wordBookName").getAsString() : "");
                            name.setTextSize(15);
                            name.setTextColor(0xFF318af8);
                            name.setTypeface(null, Typeface.BOLD);
                            name.setLayoutParams(new LinearLayout.LayoutParams(
                                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                            item.addView(name);

                            TextView count = new TextView(this);
                            count.setText((b.has("wordCount") ? b.get("wordCount").getAsInt() : 0) + "词");
                            count.setTextSize(13);
                            count.setTextColor(0xFF276bab);
                            item.addView(count);

                            bookList.addView(item);
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "加载失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
