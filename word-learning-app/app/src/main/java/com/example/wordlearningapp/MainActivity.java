package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView tvUsername, tvToday, tvTotal, tvBook;
    private LinearLayout cardCurrentBook, bookList, recentWords;
    private String currentBookId = "";

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
        cardCurrentBook = findViewById(R.id.card_current_book);
        bookList = findViewById(R.id.book_list);
        recentWords = findViewById(R.id.recent_words);

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            AuthManager.get().clearAuth();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        setupNavBar();
        setupClickListeners();
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    private void setupClickListeners() {
        findViewById(R.id.card_today).setOnClickListener(v -> {
            Intent intent = new Intent(this, TodayWordsActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.card_total).setOnClickListener(v -> {
            startActivity(new Intent(this, StudyRecordsActivity.class));
        });

        // 当前词书卡片点击跳转至词书详情页
        cardCurrentBook.setOnClickListener(v -> {
            if (currentBookId != null && !currentBookId.isEmpty()) {
                Log.d(TAG, "跳转到词书详情页，bookId: " + currentBookId);
                Intent intent = new Intent(this, BookDetailActivity.class);
                intent.putExtra("bookId", currentBookId);
                startActivity(intent);
            } else {
                Log.d(TAG, "当前没有选择词书");
                Toast.makeText(this, "当前没有选择词书，请先选择词书开始学习", Toast.LENGTH_SHORT).show();
            }
        });
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

                    // 获取当前词书ID
                    String bookId = "";
                    if (s.has("currentBookId") && !s.get("currentBookId").isJsonNull()) {
                        bookId = s.get("currentBookId").getAsString();
                    }

                    final String finalBookId = bookId;

                    runOnUiThread(() -> {
                        tvToday.setText(String.valueOf(today));
                        tvTotal.setText(String.valueOf(total));
                        tvBook.setText(bookName);
                        currentBookId = finalBookId;

                        // 更新卡片视觉效果
                        if (!currentBookId.isEmpty()) {
                            tvBook.setTextColor(0xFF318af8);
                            tvBook.setPaintFlags(tvBook.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
                        } else {
                            tvBook.setTextColor(0xFF8899aa);
                            tvBook.setPaintFlags(tvBook.getPaintFlags() & (~android.graphics.Paint.UNDERLINE_TEXT_FLAG));
                        }
                    });
                }

                JsonObject recentRes = ApiClient.get().get("/api/records/recent");
                Log.d(TAG, "=== 最近学习API响应 ===");
                Log.d(TAG, recentRes.toString());
                if (recentRes.get("code").getAsInt() == 200) {
                    JsonArray recentArr = recentRes.getAsJsonArray("data");
                    Log.d(TAG, "=== 最近学习数据 ===");
                    Log.d(TAG, "数据数量: " + recentArr.size());
                    for (int i = 0; i < recentArr.size(); i++) {
                        Log.d(TAG, "数据项[" + i + "]: " + recentArr.get(i).toString());
                    }
                    runOnUiThread(() -> buildRecentWords(recentArr));
                }

                JsonObject booksRes = ApiClient.get().get("/api/wordbooks");
                if (booksRes.get("code").getAsInt() == 200) {
                    JsonArray booksArr = booksRes.getAsJsonArray("data");
                    runOnUiThread(() -> buildBookList(booksArr));
                }
            } catch (Exception e) {
                Log.e(TAG, "加载数据异常: " + e.getMessage(), e);
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
            card.setBackground(getDrawable(R.drawable.bg_white_card_small));
            card.setPadding(16, 12, 16, 12);
            card.setElevation(dp(2));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            params.setMargins(0, 0, 10, 0);
            card.setLayoutParams(params);
            card.setClickable(true);
            card.setFocusable(true);

            TextView wordTv = new TextView(this);
            String spelling = w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "";
            wordTv.setText(spelling);
            wordTv.setTextSize(15);
            wordTv.setTextColor(0xFF318af8);
            wordTv.setTypeface(null, android.graphics.Typeface.BOLD);
            card.addView(wordTv);

            TextView transTv = new TextView(this);
            String definition = w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "";
            transTv.setText(definition);
            transTv.setTextSize(13);
            transTv.setTextColor(0xFF273245);
            transTv.setPadding(0, dp(4), 0, 0);
            card.addView(transTv);

            String wordId = "";
            if (w.has("wordId")) {
                wordId = w.get("wordId").getAsString();
            } else if (w.has("id")) {
                wordId = w.get("id").getAsString();
            }

            Log.d(TAG, "单词: " + spelling + ", wordId: " + (wordId.isEmpty() ? "(空)" : wordId));

            String finalWordId = wordId;
            String finalSpelling = spelling;
            String finalDefinition = definition;

            card.setOnClickListener(v -> {
                if (finalWordId != null && !finalWordId.isEmpty()) {
                    Log.d(TAG, "直接跳转，wordId: " + finalWordId);
                    Intent intent = new Intent(MainActivity.this, WordDetailActivity.class);
                    intent.putExtra("wordId", finalWordId);
                    intent.putExtra("fromCollection", false);
                    startActivity(intent);
                } else if (!finalSpelling.isEmpty()) {
                    Log.d(TAG, "使用搜索接口查询: " + finalSpelling);
                    new Thread(() -> {
                        try {
                            String searchUrl = "/api/words/search?keyword=" + finalSpelling;
                            Log.d(TAG, "=== 使用搜索接口 ===");
                            Log.d(TAG, "查询URL: " + searchUrl);

                            JsonObject res = ApiClient.get().get(searchUrl);
                            Log.d(TAG, "搜索响应: " + res.toString());

                            if (res.has("code") && res.get("code").getAsInt() == 200) {
                                JsonArray searchResults = res.getAsJsonArray("data");
                                Log.d(TAG, "搜索结果数量: " + searchResults.size());

                                if (searchResults.size() > 0) {
                                    JsonObject firstWord = searchResults.get(0).getAsJsonObject();
                                    String foundWordId = firstWord.has("wordId")
                                            ? firstWord.get("wordId").getAsString()
                                            : "";

                                    Log.d(TAG, "找到单词ID: " + foundWordId);
                                    Log.d(TAG, "完整单词对象: " + firstWord.toString());

                                    if (!foundWordId.isEmpty()) {
                                        String finalFoundWordId = foundWordId;
                                        runOnUiThread(() -> {
                                            Log.d(TAG, "跳转到详情页，wordId: " + finalFoundWordId);
                                            Intent intent = new Intent(MainActivity.this, WordDetailActivity.class);
                                            intent.putExtra("wordId", finalFoundWordId);
                                            intent.putExtra("fromCollection", false);
                                            startActivity(intent);
                                        });
                                    } else {
                                        runOnUiThread(() -> {
                                            Log.e(TAG, "搜索结果中没有wordId");
                                            Toast.makeText(MainActivity.this, "单词信息不完整", Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                } else {
                                    runOnUiThread(() -> {
                                        Log.e(TAG, "未找到匹配的单词: " + finalSpelling);
                                        Toast.makeText(MainActivity.this, "未找到单词详情", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            } else {
                                final String errorMsg;
                                if (res.has("message")) {
                                    errorMsg = res.get("message").getAsString();
                                } else {
                                    errorMsg = "查询失败";
                                }
                                Log.e(TAG, errorMsg);
                                runOnUiThread(() -> Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show());
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, "查询异常: " + ex.getMessage(), ex);
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "加载失败: " + ex.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                } else {
                    Toast.makeText(MainActivity.this, "单词信息无效", Toast.LENGTH_SHORT).show();
                }
            });

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

        // 只展示前三个词书
        int displayCount = Math.min(arr.size(), 3);

        for (int i = 0; i < displayCount; i++) {
            JsonObject b = arr.get(i).getAsJsonObject();

            // 创建词书卡片（水平布局，与词书浏览界面一致）
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setPadding(dp(16), dp(16), dp(16), dp(16));
            card.setBackground(getDrawable(R.drawable.bg_white_card));
            card.setElevation(dp(2));
            card.setGravity(android.view.Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 0, dp(12));
            card.setLayoutParams(cardParams);

            // 左侧信息区域
            LinearLayout leftInfo = new LinearLayout(this);
            leftInfo.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            leftInfo.setLayoutParams(leftParams);

            // 词书名称
            TextView bookName = new TextView(this);
            String name = b.has("wordBookName") ? b.get("wordBookName").getAsString() : "";
            bookName.setText(name);
            bookName.setTextSize(18);
            bookName.setTextColor(0xFF318af8);
            bookName.setTypeface(null, android.graphics.Typeface.BOLD);
            bookName.setPadding(0, 0, 0, dp(8));
            leftInfo.addView(bookName);

            // 难度和词数
            TextView bookInfo = new TextView(this);
            String info = (b.has("difficultyLevel") ? b.get("difficultyLevel").getAsString() : "")
                    + " | " + (b.has("wordCount") ? b.get("wordCount").getAsInt() : 0) + "词";
            bookInfo.setText(info);
            bookInfo.setTextSize(14);
            bookInfo.setTextColor(0xFF666666);
            bookInfo.setPadding(0, 0, 0, dp(8));
            leftInfo.addView(bookInfo);

            // 词书描述
            if (b.has("wordBookDescription") && !b.get("wordBookDescription").isJsonNull()) {
                TextView descTv = new TextView(this);
                descTv.setText(b.get("wordBookDescription").getAsString());
                descTv.setTextSize(13);
                descTv.setTextColor(0xFF547bbc);
                descTv.setPadding(0, 0, 0, 0);
                leftInfo.addView(descTv);
            }

            card.addView(leftInfo);

            // 右侧开始学习按钮（使用渐变背景）
            Button studyBtn = new Button(this);
            studyBtn.setText("开始学习");
            studyBtn.setTextSize(14);
            studyBtn.setTextColor(0xFFFFFFFF);
            studyBtn.setPadding(dp(20), dp(8), dp(20), dp(8));
            studyBtn.setMinHeight(0);
            studyBtn.setMinimumHeight(0);
            studyBtn.setBackground(getDrawable(R.drawable.bg_btn_primary));
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            btnParams.setMargins(dp(16), 0, 0, 0);
            studyBtn.setLayoutParams(btnParams);
            studyBtn.setClickable(true);
            studyBtn.setFocusable(true);

            String bookId = b.has("wordBookId") ? b.get("wordBookId").getAsString() : "";
            studyBtn.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
                intent.putExtra("bookId", bookId);
                startActivity(intent);
            });

            card.addView(studyBtn);
            bookList.addView(card);
        }

    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
