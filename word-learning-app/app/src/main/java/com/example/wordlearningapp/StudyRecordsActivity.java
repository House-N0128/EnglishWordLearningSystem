package com.example.wordlearningapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StudyRecordsActivity extends AppCompatActivity {

    private LinearLayout listArea;
    private TextView tvStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFf2f8fc);

        // ===== TOP BAR =====
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(0xFF318af8);
        topBar.setPadding(dp(22), dp(14), dp(22), dp(14));
        GradientDrawable tbBg = new GradientDrawable();
        tbBg.setColor(0xFF318af8);
        tbBg.setCornerRadii(new float[]{dp(18), dp(18), dp(18), dp(18), 0, 0, 0, 0});
        topBar.setBackground(tbBg);
        TextView barTitle = new TextView(this);
        barTitle.setText("我的学习记录");
        barTitle.setTextSize(18);
        barTitle.setTextColor(0xFFFFFFFF);
        barTitle.setTypeface(null, Typeface.BOLD);
        topBar.addView(barTitle);
        root.addView(topBar);

        // ===== SCROLLABLE MAIN =====
        ScrollView scroll = new ScrollView(this);
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(14), dp(30), dp(14), dp(82));

        TextView title = new TextView(this);
        title.setText("学习记录");
        title.setTextSize(18);
        title.setTextColor(0xFF318af8);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 0, 0, dp(18));
        main.addView(title);

        // Stats
        tvStats = new TextView(this);
        tvStats.setTextSize(15);
        tvStats.setTextColor(0xFF333333);
        tvStats.setPadding(0, 0, 0, dp(13));
        main.addView(tvStats);

        // Table header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setBackgroundColor(0xFFf7faff);
        header.setPadding(dp(10), dp(10), dp(10), dp(10));
        String[] cols = {"日期", "词书", "单词数", "操作"};
        float[] weights = {2.5f, 2.5f, 1.5f, 1.5f};
        for (int i = 0; i < cols.length; i++) {
            TextView h = new TextView(this);
            h.setText(cols[i]);
            h.setTextSize(14);
            h.setTextColor(0xFF318af8);
            h.setGravity(Gravity.CENTER);
            h.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weights[i]));
            header.addView(h);
        }
        main.addView(header);

        listArea = new LinearLayout(this);
        listArea.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable lb = new GradientDrawable();
        lb.setColor(0xFFFFFFFF);
        lb.setCornerRadius(dp(12));
        listArea.setBackground(lb);
        listArea.setPadding(dp(3), 0, dp(3), 0);
        main.addView(listArea);

        scroll.addView(main);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        // ===== BOTTOM NAVBAR =====
        root.addView(makeUserNavbar(4));

        setContentView(root);
        loadData();
    }

    private LinearLayout makeUserNavbar(int activeIndex) {
        LinearLayout navbar = new LinearLayout(this);
        navbar.setOrientation(LinearLayout.HORIZONTAL);
        navbar.setBackgroundColor(0xFFFFFFFF);
        navbar.setPadding(0, dp(8), 0, dp(12));
        navbar.setElevation(dp(8));
        GradientDrawable nbBg = new GradientDrawable();
        nbBg.setColor(0xFFFFFFFF);
        nbBg.setCornerRadii(new float[]{dp(16), dp(16), dp(16), dp(16), 0, 0, 0, 0});
        navbar.setBackground(nbBg);
        navbar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(62)));
        navbar.setGravity(Gravity.CENTER);
        String[][] tabs = {{"🏠","首页"},{"📚","词书浏览"},{"🔍","单词查询"},{"⭐","我的收藏"},{"📝","学习记录"},{"👤","个人中心"}};
        Class<?>[] targets = {MainActivity.class, WordBooksActivity.class, WordSearchActivity.class,
                CollectionsActivity.class, StudyRecordsActivity.class, ProfileActivity.class};
        for (int i = 0; i < tabs.length; i++) {
            boolean active = (i == activeIndex);
            TextView tv = new TextView(this);
            tv.setText(tabs[i][0] + "\n" + tabs[i][1]);
            tv.setTextSize(15);
            tv.setTextColor(active ? 0xFF17c2ae : 0xFF318af8);
            tv.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
            tv.setGravity(Gravity.CENTER);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            int idx = i;
            tv.setOnClickListener(v -> startActivity(new Intent(this, targets[idx])));
            navbar.addView(tv);
        }
        return navbar;
    }

    private void loadData() {
        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().get("/api/records/list");
                if (res.get("code").getAsInt() == 200) {
                    JsonArray data = res.getAsJsonArray("data");
                    JsonObject booksRes = ApiClient.get().get("/api/wordbooks");
                    Map<String, String> bookNameMap = new HashMap<>();
                    if (booksRes.get("code").getAsInt() == 200) {
                        JsonArray books = booksRes.getAsJsonArray("data");
                        for (int i = 0; i < books.size(); i++) {
                            JsonObject b = books.get(i).getAsJsonObject();
                            String bid = b.has("wordBookId") ? b.get("wordBookId").getAsString() : "";
                            String bnm = b.has("wordBookName") ? b.get("wordBookName").getAsString() : "";
                            if (!bid.isEmpty()) bookNameMap.put(bid, bnm);
                        }
                    }

                    Map<String, JsonObject> agg = new LinkedHashMap<>();
                    for (int i = 0; i < data.size(); i++) {
                        JsonObject r = data.get(i).getAsJsonObject();
                        String date = r.has("learningDate") ? r.get("learningDate").getAsString() : "";
                        String bookId = r.has("learnedWordBookId") ? r.get("learnedWordBookId").getAsString() : "unknown";
                        String key = date + "|" + bookId;
                        if (!agg.containsKey(key)) {
                            JsonObject entry = new JsonObject();
                            entry.addProperty("date", date);
                            entry.addProperty("bookId", bookId);
                            entry.addProperty("bookName", bookNameMap.getOrDefault(bookId, "未知词书"));
                            entry.addProperty("count", 1);
                            agg.put(key, entry);
                        } else {
                            agg.get(key).addProperty("count", agg.get(key).get("count").getAsInt() + 1);
                        }
                    }
                    List<JsonObject> list = new ArrayList<>(agg.values());
                    java.util.Collections.sort(list, (a, b) ->
                            b.get("date").getAsString().compareTo(a.get("date").getAsString()));

                    runOnUiThread(() -> {
                        tvStats.setText("总学习单词数：" + data.size() + " ｜ 记录天数：" + agg.size());
                        listArea.removeAllViews();
                        float[] weights = {2.5f, 2.5f, 1.5f, 1.5f};
                        int totalWords = 0;
                        for (JsonObject r : list) {
                            String date = r.has("date") ? r.get("date").getAsString() : "";
                            String bookName = r.has("bookName") ? r.get("bookName").getAsString() : "未知";
                            int count = r.has("count") ? r.get("count").getAsInt() : 0;
                            totalWords += count;

                            LinearLayout row = new LinearLayout(this);
                            row.setOrientation(LinearLayout.HORIZONTAL);
                            row.setPadding(dp(10), dp(10), dp(10), dp(10));
                            row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            LinearLayout.LayoutParams sep = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
                            sep.setMargins(dp(10), 0, dp(10), 0);
                            View divider = new View(this);
                            divider.setBackgroundColor(0xFFf5f5fc);
                            divider.setLayoutParams(sep);

                            String[] vals = {date, bookName, String.valueOf(count)};
                            for (int j = 0; j < vals.length; j++) {
                                TextView tv = new TextView(this);
                                tv.setText(vals[j]);
                                tv.setTextSize(14);
                                tv.setTextColor(0xFF318af8);
                                tv.setGravity(Gravity.CENTER);
                                tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weights[j]));
                                row.addView(tv);
                            }

                            TextView detail = new TextView(this);
                            detail.setText("详情");
                            detail.setTextSize(14);
                            detail.setTextColor(0xFFFFFFFF);
                            detail.setGravity(Gravity.CENTER);
                            detail.setPadding(dp(7), dp(7), dp(7), dp(7));
                            GradientDrawable db = new GradientDrawable();
                            db.setColor(0xFF318af8);
                            db.setCornerRadius(dp(17));
                            detail.setBackground(db);
                            detail.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weights[3]));
                            row.addView(detail);

                            listArea.addView(row);
                            listArea.addView(divider);
                        }
                        tvStats.setText("总学习单词数：" + totalWords + " ｜ 记录天数：" + agg.size());
                    });
                } else {
                    runOnUiThread(() -> { tvStats.setText("暂无学习记录"); });
                }
            } catch (Exception e) {
                runOnUiThread(() -> { tvStats.setText("加载失败"); });
            }
        }).start();
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
