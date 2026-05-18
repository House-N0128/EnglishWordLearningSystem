package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MainActivity extends AppCompatActivity {

    private TextView tvUsername, tvToday, tvTotal, tvBook;
    private LinearLayout bookList, recentWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!AuthManager.get().isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        tvUsername = findViewById(R.id.tv_username);
        tvToday = findViewById(R.id.tv_today);
        tvTotal = findViewById(R.id.tv_total);
        tvBook = findViewById(R.id.tv_book);
        bookList = findViewById(R.id.book_list);
        recentWords = findViewById(R.id.recent_words);

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            AuthManager.get().clearAuth();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        setupNavBar();
        loadData();
    }

    private void setupNavBar() {
        findViewById(R.id.nav_home).setOnClickListener(v -> {});
        findViewById(R.id.nav_books).setOnClickListener(v ->
                startActivity(new Intent(this, WordBooksActivity.class)));
        findViewById(R.id.nav_search).setOnClickListener(v ->
                startActivity(new Intent(this, WordSearchActivity.class)));
        findViewById(R.id.nav_collect).setOnClickListener(v ->
                startActivity(new Intent(this, CollectionsActivity.class)));
        findViewById(R.id.nav_records).setOnClickListener(v ->
                startActivity(new Intent(this, StudyRecordsActivity.class)));
        findViewById(R.id.nav_profile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void loadData() {
        new Thread(() -> {
            try {
                // Load profile
                JsonObject profileRes = ApiClient.get().get("/api/user/profile");
                if (profileRes.get("code").getAsInt() == 200) {
                    JsonObject user = profileRes.getAsJsonObject("data");
                    String name = user.has("userName") && !user.get("userName").isJsonNull()
                            ? user.get("userName").getAsString() : user.get("userId").getAsString();
                    runOnUiThread(() -> tvUsername.setText(name));
                }

                // Load stats
                JsonObject statsRes = ApiClient.get().get("/api/records/stats");
                if (statsRes.get("code").getAsInt() == 200) {
                    JsonObject s = statsRes.getAsJsonObject("data");
                    int today = s.has("todayCount") ? s.get("todayCount").getAsInt() : 0;
                    int total = s.has("totalCount") ? s.get("totalCount").getAsInt() : 0;
                    String bookName = s.has("currentBookName") && !s.get("currentBookName").isJsonNull()
                            ? s.get("currentBookName").getAsString() : "无";
                    runOnUiThread(() -> {
                        tvToday.setText(String.valueOf(today));
                        tvTotal.setText(String.valueOf(total));
                        tvBook.setText(bookName);
                    });
                }

                // Load recent words
                JsonObject recentRes = ApiClient.get().get("/api/records/recent");
                if (recentRes.get("code").getAsInt() == 200) {
                    JsonArray recentArr = recentRes.getAsJsonArray("data");
                    runOnUiThread(() -> buildRecentWords(recentArr));
                }

                // Load word books
                JsonObject booksRes = ApiClient.get().get("/api/wordbooks");
                if (booksRes.get("code").getAsInt() == 200) {
                    JsonArray booksArr = booksRes.getAsJsonArray("data");
                    runOnUiThread(() -> buildBookList(booksArr));
                }
            } catch (Exception e) {
                runOnUiThread(() -> tvUsername.setText("加载失败"));
            }
        }).start();
    }

    private void buildRecentWords(JsonArray arr) {
        recentWords.removeAllViews();
        if (arr == null || arr.size() == 0) {
            TextView tv = new TextView(this);
            tv.setText("暂无学习记录");
            tv.setTextSize(14);
            tv.setTextColor(0xFF8899aa);
            tv.setPadding(16, 16, 16, 16);
            recentWords.addView(tv);
            return;
        }
        for (JsonElement e : arr) {
            JsonObject w = e.getAsJsonObject();
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(0xFFFFFFFF);
            card.setPadding(13, 10, 13, 10);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            params.setMargins(0, 0, 10, 0);
            card.setLayoutParams(params);

            TextView wordTv = new TextView(this);
            wordTv.setText(w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "");
            wordTv.setTextSize(15);
            wordTv.setTextColor(0xFF318af8);
            card.addView(wordTv);

            TextView transTv = new TextView(this);
            transTv.setText(w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "");
            transTv.setTextSize(13);
            transTv.setTextColor(0xFF273245);
            card.addView(transTv);

            recentWords.addView(card);
        }
    }

    private void buildBookList(JsonArray arr) {
        bookList.removeAllViews();
        if (arr == null || arr.size() == 0) {
            TextView tv = new TextView(this);
            tv.setText("暂无词书");
            tv.setTextSize(14);
            tv.setTextColor(0xFF8899aa);
            tv.setPadding(16, 16, 16, 16);
            bookList.addView(tv);
            return;
        }
        for (JsonElement e : arr) {
            JsonObject b = e.getAsJsonObject();
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(0xFFf6f8fc);
            card.setPadding(14, 13, 14, 13);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 14);
            card.setLayoutParams(params);

            TextView infoTv = new TextView(this);
            String info = (b.has("wordBookName") ? b.get("wordBookName").getAsString() : "")
                    + "｜" + (b.has("difficultyLevel") ? b.get("difficultyLevel").getAsString() : "")
                    + "｜" + (b.has("wordCount") ? b.get("wordCount").getAsInt() : 0) + "词";
            infoTv.setText(info);
            infoTv.setTextSize(15);
            infoTv.setTextColor(0xFF333333);
            card.addView(infoTv);

            if (b.has("wordBookDescription") && !b.get("wordBookDescription").isJsonNull()) {
                TextView descTv = new TextView(this);
                descTv.setText(b.get("wordBookDescription").getAsString());
                descTv.setTextSize(13);
                descTv.setTextColor(0xFF547bbc);
                descTv.setPadding(0, 7, 0, 7);
                card.addView(descTv);
            }

            TextView btn = new TextView(this);
            btn.setText("选择学习");
            btn.setTextSize(14);
            btn.setTextColor(0xFFFFFFFF);
            btn.setBackgroundColor(0xFF318af8);
            btn.setPadding(24, 8, 24, 8);
            btn.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            btnParams.gravity = android.view.Gravity.END;
            btnParams.topMargin = 8;
            btn.setLayoutParams(btnParams);

            String bookId = b.has("wordBookId") ? b.get("wordBookId").getAsString() : "";
            btn.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
                intent.putExtra("bookId", bookId);
                startActivity(intent);
            });

            card.addView(btn);
            bookList.addView(card);
        }
    }
}
