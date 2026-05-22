package com.example.wordlearningapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CollectionsActivity extends AppCompatActivity {

    private LinearLayout listArea;
    private EditText etSearch;
    private JsonArray allCollections;

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
        topBar.setGravity(Gravity.CENTER);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        GradientDrawable tbBg = new GradientDrawable();
        tbBg.setColor(0xFF318af8);
        tbBg.setCornerRadii(new float[]{dp(18), dp(18), dp(18), dp(18), 0, 0, 0, 0});
        topBar.setBackground(tbBg);
        TextView barTitle = new TextView(this);
        barTitle.setText("我的收藏");
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
        title.setText("我的收藏");
        title.setTextSize(18);
        title.setTextColor(0xFF318af8);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 0, 0, dp(20));
        main.addView(title);

        // Search row
        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        searchRow.setPadding(0, 0, 0, dp(17));

        etSearch = new EditText(this);
        etSearch.setHint("搜索收藏单词（拼写）");
        etSearch.setTextSize(15);
        etSearch.setSingleLine(true);
        etSearch.setPadding(dp(12), dp(10), dp(12), dp(10));
        etSearch.setBackgroundColor(0xFFf6f8fc);
        GradientDrawable edBg = new GradientDrawable();
        edBg.setColor(0xFFf6f8fc);
        edBg.setCornerRadius(dp(8));
        edBg.setStroke(1, 0xFFc7d9ee);
        etSearch.setBackground(edBg);
        LinearLayout.LayoutParams edp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        edp.gravity = Gravity.CENTER_VERTICAL;
        etSearch.setLayoutParams(edp);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { filterCollections(); }
            @Override public void afterTextChanged(android.text.Editable e) {}
        });
        searchRow.addView(etSearch);

        TextView btnSearch = new TextView(this);
        btnSearch.setText("搜索");
        btnSearch.setTextSize(16);
        btnSearch.setTextColor(0xFFFFFFFF);
        btnSearch.setGravity(Gravity.CENTER);
        btnSearch.setPadding(dp(18), dp(10), dp(18), dp(10));
        GradientDrawable sbBg = new GradientDrawable();
        sbBg.setColor(0xFF318af8);
        sbBg.setCornerRadius(dp(19));
        btnSearch.setBackground(sbBg);
        LinearLayout.LayoutParams sbp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sbp.setMargins(dp(11), 0, 0, 0);
        btnSearch.setLayoutParams(sbp);
        btnSearch.setOnClickListener(v -> filterCollections());
        searchRow.addView(btnSearch);
        main.addView(searchRow);

        listArea = new LinearLayout(this);
        listArea.setOrientation(LinearLayout.VERTICAL);
        main.addView(listArea);

        scroll.addView(main);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        // ===== BOTTOM NAVBAR =====
        root.addView(makeUserNavbar(3));

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
                JsonObject res = ApiClient.get().get("/api/collections");
                if (res.has("code") && res.get("code").getAsInt() == 200 && res.has("data")) {
                    allCollections = res.getAsJsonArray("data");
                } else {
                    allCollections = new JsonArray();
                }
                runOnUiThread(() -> filterCollections());
            } catch (Exception e) {
                allCollections = new JsonArray();
                runOnUiThread(() -> filterCollections());
            }
        }).start();
    }

    private void filterCollections() {
        listArea.removeAllViews();
        if (allCollections == null || allCollections.size() == 0) {
            TextView emp = new TextView(this);
            emp.setText("暂无收藏");
            emp.setTextSize(14);
            emp.setTextColor(0xFF8899aa);
            emp.setGravity(Gravity.CENTER);
            emp.setPadding(0, dp(40), 0, 0);
            listArea.addView(emp);
            return;
        }

        String kw = etSearch.getText().toString().trim().toLowerCase();
        for (JsonElement e : allCollections) {
            if (!e.isJsonObject()) continue;
            JsonObject c = e.getAsJsonObject();
            String spelling = c.has("englishSpelling") ? c.get("englishSpelling").getAsString() : "";
            String phonetic = c.has("phoneticSymbol") && !c.get("phoneticSymbol").isJsonNull() ? c.get("phoneticSymbol").getAsString() : "";
            String wordId = c.has("wordId") ? c.get("wordId").getAsString() : "";
            String date = "";
            if (c.has("collectionTime") && !c.get("collectionTime").isJsonNull()) {
                String fd = c.get("collectionTime").getAsString();
                date = fd.length() >= 10 ? fd.substring(0, 10) : fd;
            }

            if (!kw.isEmpty() && !spelling.toLowerCase().contains(kw)) continue;

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setBackgroundColor(0xFFFFFFFF);
            card.setPadding(dp(13), dp(15), dp(13), dp(15));
            card.setGravity(Gravity.CENTER_VERTICAL);
            GradientDrawable cd = new GradientDrawable();
            cd.setColor(0xFFFFFFFF);
            cd.setCornerRadius(dp(13));
            card.setBackground(cd);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cp.setMargins(0, 0, 0, dp(14));
            card.setLayoutParams(cp);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.HORIZONTAL);
            info.setGravity(Gravity.CENTER_VERTICAL);
            info.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            TextView sp = new TextView(this);
            sp.setText(spelling);
            sp.setTextSize(16);
            sp.setTextColor(0xFF318af8);
            sp.setTypeface(null, Typeface.BOLD);
            info.addView(sp);

            if (!phonetic.isEmpty()) {
                TextView ph = new TextView(this);
                ph.setText(" [" + phonetic + "] ");
                ph.setTextSize(13);
                ph.setTextColor(0xFF547bbc);
                info.addView(ph);
            }

            if (!date.isEmpty()) {
                TextView dt = new TextView(this);
                dt.setText(date);
                dt.setTextSize(12);
                dt.setTextColor(0xFF6578a0);
                dt.setPadding(dp(8), 0, 0, 0);
                info.addView(dt);
            }

            card.addView(info);

            LinearLayout actions = new LinearLayout(this);
            actions.setOrientation(LinearLayout.HORIZONTAL);

            TextView btnDetail = smallBtn("详情", 0xFF318af8, 0xFFFFFFFF);
            btnDetail.setOnClickListener(v -> {
                Intent in = new Intent(this, WordDetailActivity.class);
                in.putExtra("wordId", wordId);
                in.putExtra("fromCollection", true);
                startActivity(in);
            });
            actions.addView(btnDetail);

            TextView btnCancel = smallBtn("取消", 0xFFe0e6f2, 0xFF318af8);
            btnCancel.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("确认取消")
                    .setMessage("确定取消收藏 \"" + spelling + "\"？")
                    .setPositiveButton("确定", (d, w) -> {
                        new Thread(() -> {
                            try {
                                JsonObject body = new JsonObject();
                                body.addProperty("wordId", wordId);
                                ApiClient.get().delete("/api/collections/remove", body);
                                runOnUiThread(() -> { Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show(); loadData(); });
                            } catch (Exception ex) {}
                        }).start();
                    }).setNegativeButton("取消", null).show());
            actions.addView(btnCancel);

            card.addView(actions);
            listArea.addView(card);
        }
    }

    private TextView smallBtn(String text, int bgColor, int textColor) {
        TextView b = new TextView(this);
        b.setText(text);
        b.setTextSize(14);
        b.setTextColor(textColor);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(14), dp(7), dp(14), dp(7));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(bgColor);
        bg.setCornerRadius(dp(18));
        b.setBackground(bg);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.setMargins(dp(8), 0, 0, 0);
        b.setLayoutParams(bp);
        return b;
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
