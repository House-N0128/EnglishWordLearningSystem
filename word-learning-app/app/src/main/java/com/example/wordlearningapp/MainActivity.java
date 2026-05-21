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

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MainActivity extends AppCompatActivity {

    private LinearLayout bookList, recentWords;
    private TextView tvUsername, tvToday, tvTotal, tvBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthManager.get().isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFf2f8fc);

        // ===== TOP BAR =====
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(0xFF318af8);
        topBar.setPadding(dp(22), 0, dp(22), 0);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        GradientDrawable tbBg = new GradientDrawable();
        tbBg.setColor(0xFF318af8);
        tbBg.setCornerRadii(new float[]{dp(18), dp(18), dp(18), dp(18), 0, 0, 0, 0});
        topBar.setBackground(tbBg);

        tvUsername = new TextView(this);
        tvUsername.setText("加载中...");
        tvUsername.setTextSize(18);
        tvUsername.setTextColor(0xFFFFFFFF);
        tvUsername.setTypeface(null, Typeface.BOLD);
        topBar.addView(tvUsername);
        root.addView(topBar);

        // ===== SCROLLABLE MAIN =====
        ScrollView scroll = new ScrollView(this);
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(14), dp(30), dp(14), dp(82));

        // Study stat cards
        LinearLayout cards = new LinearLayout(this);
        cards.setOrientation(LinearLayout.HORIZONTAL);
        cards.setPadding(0, 0, 0, dp(26));

        tvToday = addStatCard(cards, "今日学习");
        tvTotal = addStatCard(cards, "累计学习");
        tvBook = addStatCard(cards, "当前词书");
        main.addView(cards);

        // Recommend title
        TextView recTitle = new TextView(this);
        recTitle.setText("推荐词书");
        recTitle.setTextSize(17);
        recTitle.setTextColor(0xFF318af8);
        recTitle.setTypeface(null, Typeface.BOLD);
        recTitle.setPadding(0, 0, 0, dp(8));
        main.addView(recTitle);

        bookList = new LinearLayout(this);
        bookList.setOrientation(LinearLayout.VERTICAL);
        main.addView(bookList);

        // Recent words title
        TextView recentTitle = new TextView(this);
        recentTitle.setText("最近学习");
        recentTitle.setTextSize(16);
        recentTitle.setTextColor(0xFF318af8);
        recentTitle.setTypeface(null, Typeface.BOLD);
        recentTitle.setPadding(0, dp(18), 0, dp(8));
        main.addView(recentTitle);

        recentWords = new LinearLayout(this);
        recentWords.setOrientation(LinearLayout.HORIZONTAL);
        main.addView(recentWords);

        scroll.addView(main);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        // ===== BOTTOM NAVBAR =====
        root.addView(makeUserNavbar(0));

        setContentView(root);
        loadData();
    }

    private TextView addStatCard(LinearLayout parent, String label) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFFFFFFFF);
        card.setPadding(dp(11), dp(15), dp(11), dp(15));
        card.setGravity(Gravity.CENTER);
        GradientDrawable cd = new GradientDrawable();
        cd.setColor(0xFFFFFFFF);
        cd.setCornerRadius(dp(14));
        card.setBackground(cd);
        card.setElevation(dp(1));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        cp.setMargins(0, 0, dp(14), 0);
        if (parent.getChildCount() > 0) cp.setMargins(dp(14), 0, 0, 0);
        card.setLayoutParams(cp);

        TextView num = new TextView(this);
        num.setText("...");
        num.setTextSize(19);
        num.setTextColor(0xFF318af8);
        num.setTypeface(null, Typeface.BOLD);
        num.setGravity(Gravity.CENTER);
        card.addView(num);

        TextView lb = new TextView(this);
        lb.setText(label);
        lb.setTextSize(15);
        lb.setTextColor(0xFF273245);
        lb.setGravity(Gravity.CENTER);
        card.addView(lb);

        parent.addView(card);
        return num;
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
                JsonObject profileRes = ApiClient.get().get("/api/user/profile");
                if (profileRes.get("code").getAsInt() == 200) {
                    JsonObject user = profileRes.getAsJsonObject("data");
                    String name = user.has("userName") && !user.get("userName").isJsonNull()
                            ? user.get("userName").getAsString() : user.get("userId").getAsString();
                    runOnUiThread(() -> tvUsername.setText(name));
                }

                JsonObject statsRes = ApiClient.get().get("/api/records/stats");
                if (statsRes.get("code").getAsInt() == 200) {
                    JsonObject s = statsRes.getAsJsonObject("data");
                    int today = s.has("todayCount") ? s.get("todayCount").getAsInt() : 0;
                    int total = s.has("totalCount") ? s.get("totalCount").getAsInt() : 0;
                    String bookName = s.has("currentBookName") && !s.get("currentBookName").isJsonNull()
                            ? s.get("currentBookName").getAsString() : "无";
                    runOnUiThread(() -> {
                        tvToday.setText(today + "词");
                        tvTotal.setText(total + "词");
                        tvBook.setText(bookName);
                    });
                }

                JsonObject booksRes = ApiClient.get().get("/api/wordbooks");
                if (booksRes.get("code").getAsInt() == 200) {
                    JsonArray arr = booksRes.getAsJsonArray("data");
                    runOnUiThread(() -> buildBookList(arr));
                }

                JsonObject recentRes = ApiClient.get().get("/api/records/recent");
                if (recentRes.get("code").getAsInt() == 200) {
                    JsonArray arr = recentRes.getAsJsonArray("data");
                    runOnUiThread(() -> buildRecentWords(arr));
                }
            } catch (Exception e) {
                runOnUiThread(() -> tvUsername.setText("加载失败"));
            }
        }).start();
    }

    private void buildBookList(JsonArray arr) {
        bookList.removeAllViews();
        if (arr == null || arr.size() == 0) {
            TextView tv = new TextView(this);
            tv.setText("暂无词书");
            tv.setTextSize(14);
            tv.setTextColor(0xFF8899aa);
            tv.setPadding(dp(16), dp(16), 0, 0);
            bookList.addView(tv);
            return;
        }
        for (JsonElement e : arr) {
            JsonObject b = e.getAsJsonObject();
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(0xFFf6f8fc);
            card.setPadding(dp(14), dp(13), dp(14), dp(13));
            GradientDrawable cd = new GradientDrawable();
            cd.setColor(0xFFf6f8fc);
            cd.setCornerRadius(dp(12));
            card.setBackground(cd);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cp.setMargins(0, 0, 0, dp(14));
            card.setLayoutParams(cp);

            String info = (b.has("wordBookName") ? b.get("wordBookName").getAsString() : "")
                    + " | " + (b.has("difficultyLevel") ? b.get("difficultyLevel").getAsString() : "")
                    + " | " + (b.has("wordCount") ? b.get("wordCount").getAsInt() : 0) + "词";
            TextView infoTv = new TextView(this);
            infoTv.setText(info);
            infoTv.setTextSize(15);
            infoTv.setTextColor(0xFF333333);
            infoTv.setTypeface(null, Typeface.BOLD);
            card.addView(infoTv);

            if (b.has("wordBookDescription") && !b.get("wordBookDescription").isJsonNull()) {
                TextView descTv = new TextView(this);
                descTv.setText(b.get("wordBookDescription").getAsString());
                descTv.setTextSize(13);
                descTv.setTextColor(0xFF547bbc);
                descTv.setPadding(0, dp(8), 0, 0);
                card.addView(descTv);
            }

            TextView btn = new TextView(this);
            btn.setText("选择学习");
            btn.setTextSize(14);
            btn.setTextColor(0xFFFFFFFF);
            btn.setBackgroundColor(0xFF318af8);
            btn.setPadding(dp(24), dp(8), dp(24), dp(8));
            btn.setGravity(Gravity.CENTER);
            GradientDrawable btnBg = new GradientDrawable();
            btnBg.setColor(0xFF318af8);
            btnBg.setCornerRadius(dp(21));
            btn.setBackground(btnBg);
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            bp.gravity = Gravity.END;
            bp.topMargin = dp(8);
            btn.setLayoutParams(bp);

            String bookId = b.has("wordBookId") ? b.get("wordBookId").getAsString() : "";
            btn.setOnClickListener(v -> {
                Intent in = new Intent(this, BookDetailActivity.class);
                in.putExtra("bookId", bookId);
                startActivity(in);
            });
            card.addView(btn);
            bookList.addView(card);
        }
    }

    private void buildRecentWords(JsonArray arr) {
        recentWords.removeAllViews();
        if (arr == null || arr.size() == 0) {
            TextView tv = new TextView(this);
            tv.setText("暂无学习记录");
            tv.setTextSize(14);
            tv.setTextColor(0xFF8899aa);
            tv.setPadding(dp(16), dp(16), 0, 0);
            recentWords.addView(tv);
            return;
        }
        for (JsonElement e : arr) {
            JsonObject w = e.getAsJsonObject();
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(0xFFFFFFFF);
            card.setPadding(dp(13), dp(10), dp(13), dp(10));
            card.setGravity(Gravity.CENTER);
            GradientDrawable cd = new GradientDrawable();
            cd.setColor(0xFFFFFFFF);
            cd.setCornerRadius(dp(7));
            card.setBackground(cd);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            cp.setMargins(0, 0, dp(10), 0);
            card.setLayoutParams(cp);

            TextView wordTv = new TextView(this);
            wordTv.setText(w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "");
            wordTv.setTextSize(15);
            wordTv.setTextColor(0xFF318af8);
            wordTv.setTypeface(null, Typeface.BOLD);
            card.addView(wordTv);

            if (w.has("partOfSpeech") && !w.get("partOfSpeech").isJsonNull()) {
                TextView posTv = new TextView(this);
                posTv.setText(w.get("partOfSpeech").getAsString());
                posTv.setTextSize(13);
                posTv.setTextColor(0xFF47b1eb);
                card.addView(posTv);
            }

            TextView transTv = new TextView(this);
            transTv.setText(w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "");
            transTv.setTextSize(13);
            transTv.setTextColor(0xFF222222);
            card.addView(transTv);

            String wordId = getWordId(w);
            card.setOnClickListener(v -> {
                if (wordId != null && !wordId.isEmpty()) {
                    Intent in = new Intent(this, WordDetailActivity.class);
                    in.putExtra("wordId", wordId);
                    startActivity(in);
                }
            });
            recentWords.addView(card);
        }
    }

    private String getWordId(JsonObject w) {
        if (w.has("wordId")) return w.get("wordId").getAsString();
        if (w.has("id")) return w.get("id").getAsString();
        return "";
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
