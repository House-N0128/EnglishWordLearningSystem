package com.example.wordlearningapp;

import android.content.Intent;
import android.graphics.Color;
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
    private LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthManager.get().isLoggedIn() || !"admin".equals(AuthManager.get().getRole())) {
            AuthManager.get().clearAuth();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Root scroll
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xFFf2f8fc);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setBackgroundColor(0xFF318af8);
        header.setPadding(32, 24, 32, 24);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView adminName = new TextView(this);
        adminName.setText("管理员：" + AuthManager.get().getUserId());
        adminName.setTextSize(16);
        adminName.setTextColor(0xFFFFFFFF);
        adminName.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        header.addView(adminName);

        TextView logout = new TextView(this);
        logout.setText("退出");
        logout.setTextSize(14);
        logout.setTextColor(0xFF318af8);
        logout.setBackgroundColor(0xFFFFFFFF);
        logout.setPadding(28, 12, 28, 12);
        GradientDrawable lgBg = new GradientDrawable();
        lgBg.setColor(0xFFFFFFFF);
        lgBg.setCornerRadius(40);
        logout.setBackground(lgBg);
        logout.setOnClickListener(v -> {
            AuthManager.get().clearAuth();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        header.addView(logout);
        root.addView(header);

        // Content
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(24, 20, 24, 24);
        content.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Title
        TextView title = new TextView(this);
        title.setText("平台数据总览");
        title.setTextSize(19);
        title.setTextColor(0xFF318af8);
        title.setPadding(0, 0, 0, 16);
        content.addView(title);

        // Stats 2x2
        LinearLayout statGrid = new LinearLayout(this);
        statGrid.setOrientation(LinearLayout.VERTICAL);

        LinearLayout row1 = makeRow();
        tvUsers = makeStat(row1, "总用户数");
        tvBooks = makeStat(row1, "总词书数");
        statGrid.addView(row1);

        LinearLayout row2 = makeRow();
        tvWords = makeStat(row2, "总单词数");
        tvToday = makeStat(row2, "今日学习记录");
        statGrid.addView(row2);

        content.addView(statGrid);

        // Bookmarks section
        TextView bookTitle = new TextView(this);
        bookTitle.setText("词书列表");
        bookTitle.setTextSize(16);
        bookTitle.setTextColor(0xFF318af8);
        bookTitle.setPadding(0, 24, 0, 12);
        content.addView(bookTitle);

        bookList = new LinearLayout(this);
        bookList.setOrientation(LinearLayout.VERTICAL);
        content.addView(bookList);

        // Quick actions
        TextView actTitle = new TextView(this);
        actTitle.setText("快捷操作");
        actTitle.setTextSize(16);
        actTitle.setTextColor(0xFF318af8);
        actTitle.setPadding(0, 24, 0, 12);
        content.addView(actTitle);

        addActionBtn("👥  用户管理", v -> startActivity(new Intent(this, UserManageActivity.class)));
        addActionBtn("📚  词书管理", v -> startActivity(new Intent(this, WordBooksActivity.class)));
        addActionBtn("📝  单词管理", v -> startActivity(new Intent(this, WordSearchActivity.class)));

        root.addView(content);
        scroll.addView(root);
        setContentView(scroll);

        loadData();
    }

    private LinearLayout makeRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 0, 0, 12);
        return row;
    }

    private TextView makeStat(LinearLayout parent, String label) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackgroundColor(0xFFFFFFFF);
        card.setPadding(16, 20, 16, 20);
        GradientDrawable cd = new GradientDrawable();
        cd.setColor(0xFFFFFFFF);
        cd.setCornerRadius(24);
        card.setBackground(cd);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        cp.setMargins(0, 0, 12, 0);
        card.setLayoutParams(cp);

        TextView num = new TextView(this);
        num.setText("-");
        num.setTextSize(22);
        num.setTextColor(0xFF318af8);
        num.setGravity(Gravity.CENTER);
        card.addView(num);

        TextView lbl = new TextView(this);
        lbl.setText(label);
        lbl.setTextSize(12);
        lbl.setTextColor(0xFF8899aa);
        lbl.setGravity(Gravity.CENTER);
        lbl.setPadding(0, 6, 0, 0);
        card.addView(lbl);

        parent.addView(card);
        return num;
    }

    private void addActionBtn(String text, View.OnClickListener listener) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextSize(16);
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackgroundColor(0xFF318af8);
        btn.setPadding(20, 16, 20, 16);
        btn.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF318af8);
        bg.setCornerRadius(24);
        btn.setBackground(bg);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0, 0, 0, 12);
        btn.setLayoutParams(bp);
        btn.setOnClickListener(listener);
        content.addView(btn);
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
                        for (int i = 0; i < books.size(); i++) {
                            JsonObject b = books.get(i).getAsJsonObject();
                            LinearLayout item = new LinearLayout(this);
                            item.setOrientation(LinearLayout.HORIZONTAL);
                            item.setBackgroundColor(0x19318af8);
                            item.setPadding(20, 14, 20, 14);
                            GradientDrawable ib = new GradientDrawable();
                            ib.setColor(0x19318af8);
                            ib.setCornerRadius(16);
                            item.setBackground(ib);
                            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            ip.setMargins(0, 0, 0, 10);
                            item.setLayoutParams(ip);

                            TextView nm = new TextView(this);
                            nm.setText(b.has("wordBookName") ? b.get("wordBookName").getAsString() : "");
                            nm.setTextSize(15);
                            nm.setTextColor(0xFF318af8);
                            nm.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                            item.addView(nm);

                            TextView cnt = new TextView(this);
                            cnt.setText((b.has("wordCount") ? b.get("wordCount").getAsInt() : 0) + "词");
                            cnt.setTextSize(13);
                            cnt.setTextColor(0xFF276bab);
                            item.addView(cnt);

                            bookList.addView(item);
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "加载失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
