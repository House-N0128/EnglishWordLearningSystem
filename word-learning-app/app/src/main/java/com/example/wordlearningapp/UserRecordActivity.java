package com.example.wordlearningapp;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserRecordActivity extends AppCompatActivity {

    private LinearLayout tableBody;
    private TextView tvStats, tvUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String userId = getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("userName");
        if (userId == null) { finish(); return; }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFf2f8fc);

        // ===== TOP BAR =====
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(0xFF318af8);
        topBar.setPadding(dp(6), 0, dp(18), 0);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        GradientDrawable tbBg = new GradientDrawable();
        tbBg.setColor(0xFF318af8);
        tbBg.setCornerRadii(new float[]{0, 0, 0, 0, dp(18), dp(18), dp(18), dp(18)});
        topBar.setBackground(tbBg);

        TextView btnBack = new TextView(this);
        btnBack.setText("←");
        btnBack.setTextSize(22);
        btnBack.setTextColor(0xFFFFFFFF);
        btnBack.setTypeface(null, Typeface.BOLD);
        btnBack.setPadding(dp(5), 0, dp(11), 0);
        btnBack.setOnClickListener(v -> finish());
        topBar.addView(btnBack);

        TextView barTitle = new TextView(this);
        barTitle.setText("用户学习记录");
        barTitle.setTextSize(18);
        barTitle.setTextColor(0xFFFFFFFF);
        barTitle.setTypeface(null, Typeface.BOLD);
        topBar.addView(barTitle);
        root.addView(topBar);

        // ===== SCROLLABLE MAIN =====
        ScrollView scroll = new ScrollView(this);
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(16), dp(20), dp(16), dp(20));

        // User info box
        LinearLayout infoBox = new LinearLayout(this);
        infoBox.setOrientation(LinearLayout.VERTICAL);
        infoBox.setBackgroundColor(0xFFFFFFFF);
        infoBox.setPadding(dp(14), dp(14), dp(14), dp(14));
        GradientDrawable ibBg = new GradientDrawable();
        ibBg.setColor(0xFFFFFFFF);
        ibBg.setCornerRadius(dp(12));
        infoBox.setBackground(ibBg);
        infoBox.setElevation(dp(1));

        tvUserInfo = new TextView(this);
        tvUserInfo.setTextSize(15);
        tvUserInfo.setTextColor(0xFF273245);
        tvUserInfo.setLineSpacing(dp(6), 1);
        infoBox.addView(tvUserInfo);
        main.addView(infoBox);

        // Stats title
        TextView sectionTitle = new TextView(this);
        sectionTitle.setText("学习统计");
        sectionTitle.setTextSize(18);
        sectionTitle.setTextColor(0xFF318af8);
        sectionTitle.setTypeface(null, Typeface.BOLD);
        sectionTitle.setPadding(0, dp(18), 0, dp(10));
        main.addView(sectionTitle);

        // Stats bar
        tvStats = new TextView(this);
        tvStats.setTextSize(15);
        tvStats.setTextColor(0xFF273245);
        tvStats.setPadding(0, 0, 0, dp(12));
        main.addView(tvStats);

        // Record table title
        TextView recordTitle = new TextView(this);
        recordTitle.setText("学习记录明细");
        recordTitle.setTextSize(18);
        recordTitle.setTextColor(0xFF318af8);
        recordTitle.setTypeface(null, Typeface.BOLD);
        recordTitle.setPadding(0, dp(18), 0, dp(10));
        main.addView(recordTitle);

        // Table
        LinearLayout table = new LinearLayout(this);
        table.setOrientation(LinearLayout.VERTICAL);
        table.setBackgroundColor(0xFFFFFFFF);
        GradientDrawable tblBg = new GradientDrawable();
        tblBg.setColor(0xFFFFFFFF);
        tblBg.setCornerRadius(dp(12));
        table.setBackground(tblBg);
        table.setElevation(dp(1));

        // Table header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setBackgroundColor(0xFFf7faff);
        header.setPadding(0, dp(11), 0, dp(11));
        String[] cols = {"日期", "学习词书", "学习单词数"};
        float[] weights = {1.5f, 1.5f, 1f};
        for (int i = 0; i < cols.length; i++) {
            TextView th = new TextView(this);
            th.setText(cols[i]);
            th.setTextSize(14);
            th.setTextColor(0xFF318af8);
            th.setTypeface(null, Typeface.BOLD);
            th.setGravity(Gravity.CENTER);
            th.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weights[i]));
            header.addView(th);
        }
        table.addView(header);

        tableBody = new LinearLayout(this);
        tableBody.setOrientation(LinearLayout.VERTICAL);
        table.addView(tableBody);
        main.addView(table);

        scroll.addView(main);
        root.addView(scroll);

        setContentView(root);

        // Load user info first
        tvUserInfo.setText("用户ID: " + userId + "\n昵称: " + (userName != null ? userName : "-"));
        loadData(userId);
    }

    private void loadData(String userId) {
        new Thread(() -> {
            try {
                // Fetch user profile for register time
                JsonObject infoRes = ApiClient.get().get("/api/admin/users?keyword=" + userId);
                if (infoRes.get("code").getAsInt() == 200) {
                    JsonArray users = infoRes.getAsJsonArray("data");
                    for (int i = 0; i < users.size(); i++) {
                        JsonObject u = users.get(i).getAsJsonObject();
                        if (userId.equals(u.has("userId") ? u.get("userId").getAsString() : "")) {
                            String name = u.has("userName") && !u.get("userName").isJsonNull() ? u.get("userName").getAsString() : "";
                            String regTime = u.has("registerTime") && !u.get("registerTime").isJsonNull() ? u.get("registerTime").getAsString() : "";
                            if (regTime.length() > 10) regTime = regTime.substring(0, 10);
                            String finalRegTime = regTime;
                            String finalName = name;
                            runOnUiThread(() -> tvUserInfo.setText(
                                    "用户ID: " + userId +
                                    "\n昵称: " + (finalName.isEmpty() ? "-" : finalName) +
                                    "\n注册时间: " + (finalRegTime.isEmpty() ? "-" : finalRegTime)));
                            break;
                        }
                    }
                }

                // Fetch learning records
                JsonObject res = ApiClient.get().get("/api/admin/records?userId=" + userId);
                if (res.get("code").getAsInt() == 200) {
                    JsonArray data = res.getAsJsonArray("data");
                    Map<String, JsonObject> agg = new LinkedHashMap<>();
                    for (int i = 0; i < data.size(); i++) {
                        JsonObject r = data.get(i).getAsJsonObject();
                        String date = r.has("learningDate") ? r.get("learningDate").getAsString() : "";
                        String book = r.has("learnedWordBookId") ? r.get("learnedWordBookId").getAsString() : "unknown";
                        String key = date + "|" + book;
                        if (!agg.containsKey(key)) {
                            JsonObject e = new JsonObject();
                            e.addProperty("date", date);
                            e.addProperty("book", book);
                            e.addProperty("count", 1);
                            agg.put(key, e);
                        } else {
                            agg.get(key).addProperty("count", agg.get(key).get("count").getAsInt() + 1);
                        }
                    }
                    List<JsonObject> list = new ArrayList<>(agg.values());
                    list.sort((a, b) -> b.get("date").getAsString().compareTo(a.get("date").getAsString()));

                    // Calculate stats
                    int totalWords = 0;
                    for (JsonObject r : list) totalWords += r.get("count").getAsInt();
                    int days = list.size();
                    final int finalTotal = totalWords;
                    final int finalAvg = days > 0 ? totalWords / days : 0;

                    runOnUiThread(() -> {
                        tvStats.setText("总学习单词数: " + finalTotal + "  |  日均学习数: " + finalAvg);

                        tableBody.removeAllViews();
                        float[] w = {1.5f, 1.5f, 1f};
                        for (int i = 0; i < list.size(); i++) {
                            JsonObject r = list.get(i);
                            String date = r.has("date") ? r.get("date").getAsString() : "";
                            String book = r.has("book") ? r.get("book").getAsString() : "未知";
                            int count = r.has("count") ? r.get("count").getAsInt() : 0;

                            LinearLayout row = new LinearLayout(this);
                            row.setOrientation(LinearLayout.HORIZONTAL);
                            row.setPadding(0, dp(11), 0, dp(11));
                            String[] vals = {date, book, String.valueOf(count)};
                            for (int j = 0; j < vals.length; j++) {
                                TextView td = new TextView(this);
                                td.setText(vals[j]);
                                td.setTextSize(14);
                                td.setTextColor(0xFF318af8);
                                td.setGravity(Gravity.CENTER);
                                td.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, w[j]));
                                row.addView(td);
                            }

                            // Divider
                            View div = new View(this);
                            div.setBackgroundColor(0xFFf2f2fc);
                            LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);

                            tableBody.addView(row);
                            if (i < list.size() - 1) tableBody.addView(div);
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> tvStats.setText("加载失败"));
            }
        }).start();
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
