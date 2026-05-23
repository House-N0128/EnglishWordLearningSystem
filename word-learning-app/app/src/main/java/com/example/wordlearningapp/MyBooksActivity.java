package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MyBooksActivity extends AppCompatActivity {

    private static final String TAG = "MyBooksActivity";
    private LinearLayout myBookListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_books);

        myBookListContainer = findViewById(R.id.my_book_list_container);

        // 返回按钮
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        // 底部导航栏
        setupBottomNavigation();

        // 加载数据
        loadMyBooks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "页面恢复，刷新我的词书数据");
        loadMyBooks();
    }

    private void setupBottomNavigation() {
        findViewById(R.id.nav_home).setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.nav_books).setOnClickListener(v ->
                startActivity(new Intent(this, WordBooksActivity.class)));
        findViewById(R.id.nav_search).setOnClickListener(v ->
                startActivity(new Intent(this, WordSearchActivity.class)));
        findViewById(R.id.nav_collection).setOnClickListener(v ->
                startActivity(new Intent(this, CollectionsActivity.class)));
        findViewById(R.id.nav_records).setOnClickListener(v ->
                startActivity(new Intent(this, StudyRecordsActivity.class)));
        findViewById(R.id.nav_profile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void loadMyBooks() {
        new Thread(() -> {
            try {
                Log.d(TAG, "开始加载我的词书数据");

                // 获取学习记录
                JsonObject recordsRes = ApiClient.get().get("/api/records/list");

                if (recordsRes.get("code").getAsInt() == 200) {
                    JsonArray records = recordsRes.getAsJsonArray("data");
                    Log.d(TAG, "学习记录数量: " + records.size());

                    // 打印第一条记录的字段用于调试
                    if (records.size() > 0) {
                        JsonObject firstRecord = records.get(0).getAsJsonObject();
                        Log.d(TAG, "学习记录字段: " + firstRecord.toString());
                    }

                    // 获取词书列表，建立词书ID到信息的映射
                    JsonObject booksRes = ApiClient.get().get("/api/wordbooks");
                    Map<String, JsonObject> bookMap = new HashMap<>();
                    if (booksRes.get("code").getAsInt() == 200) {
                        JsonArray books = booksRes.getAsJsonArray("data");
                        for (int i = 0; i < books.size(); i++) {
                            JsonObject book = books.get(i).getAsJsonObject();
                            String bookId = book.has("wordBookId") ? book.get("wordBookId").getAsString() : "";
                            if (!bookId.isEmpty()) {
                                bookMap.put(bookId, book);
                            }
                        }
                    }

                    // 从学习记录中提取所有有学习记录的词书
                    // 每条学习记录代表一个单词的学习，使用Set去重
                    Map<String, JsonObject> learnedBooksMap = new HashMap<>();
                    Map<String, Set<String>> learnedWordsMap = new HashMap<>(); // 词书ID -> 单词ID集合（去重）
                    Map<String, String> lastStudyTimeMap = new HashMap<>();

                    for (int i = 0; i < records.size(); i++) {
                        JsonObject record = records.get(i).getAsJsonObject();

                        // 尝试多个可能的字段名
                        String bookId = "";
                        if (record.has("learnedWordBookId")) {
                            bookId = record.get("learnedWordBookId").getAsString();
                        } else if (record.has("wordBookId")) {
                            bookId = record.get("wordBookId").getAsString();
                        }

                        String wordId = "";
                        if (record.has("learnedWordId")) {
                            wordId = record.get("learnedWordId").getAsString();
                        } else if (record.has("wordId")) {
                            wordId = record.get("wordId").getAsString();
                        } else if (record.has("id")) {
                            wordId = record.get("id").getAsString();
                        }

                        String studyTime = "";
                        if (record.has("learningDate")) {
                            studyTime = record.get("learningDate").getAsString();
                        } else if (record.has("createTime")) {
                            studyTime = record.get("createTime").getAsString();
                        }

                        if (!bookId.isEmpty()) {
                            // 使用Set去重统计每个词书的学习单词数
                            if (!learnedWordsMap.containsKey(bookId)) {
                                learnedWordsMap.put(bookId, new HashSet<>());
                            }

                            // 如果有wordId则使用wordId去重，否则记录索引作为唯一标识
                            String uniqueKey = !wordId.isEmpty() ? wordId : ("record_" + i);
                            learnedWordsMap.get(bookId).add(uniqueKey);

                            Log.d(TAG, "词书ID: " + bookId + ", 单词ID: " + uniqueKey + ", 去重后数量: " + learnedWordsMap.get(bookId).size());

                            // 记录最后学习时间（取最新的）
                            if (!lastStudyTimeMap.containsKey(bookId) || studyTime.compareTo(lastStudyTimeMap.get(bookId)) > 0) {
                                lastStudyTimeMap.put(bookId, studyTime);
                            }

                            // 保存词书信息
                            if (bookMap.containsKey(bookId)) {
                                learnedBooksMap.put(bookId, bookMap.get(bookId));
                            }
                        }
                    }

                    Log.d(TAG, "统计到的词书数量: " + learnedBooksMap.size());
                    for (Map.Entry<String, Set<String>> entry : learnedWordsMap.entrySet()) {
                        Log.d(TAG, "词书 " + entry.getKey() + " 已学单词数: " + entry.getValue().size());
                    }

                    // 构建返回数据
                    JsonArray resultBooks = new JsonArray();
                    for (Map.Entry<String, JsonObject> entry : learnedBooksMap.entrySet()) {
                        String bookId = entry.getKey();
                        JsonObject book = entry.getValue();

                        // 创建词书信息对象
                        JsonObject bookInfo = new JsonObject();
                        bookInfo.addProperty("wordBookId", bookId);
                        bookInfo.addProperty("wordBookName", book.has("wordBookName") ? book.get("wordBookName").getAsString() : "未知词书");
                        bookInfo.addProperty("wordCount", book.has("wordCount") ? book.get("wordCount").getAsInt() : 0);
                        // 使用Set的大小作为去重后的单词数
                        int uniqueWordCount = learnedWordsMap.containsKey(bookId) ? learnedWordsMap.get(bookId).size() : 0;
                        bookInfo.addProperty("learnedCount", uniqueWordCount);

                        String lastTime = lastStudyTimeMap.get(bookId);
                        if (lastTime != null && !lastTime.isEmpty()) {
                            bookInfo.addProperty("lastStudyTime", lastTime);
                        }

                        resultBooks.add(bookInfo);
                    }

                    JsonArray finalResultBooks = resultBooks;
                    runOnUiThread(() -> buildMyBooksList(finalResultBooks));
                } else {
                    runOnUiThread(() -> {
                        TextView emptyText = new TextView(this);
                        emptyText.setText("暂无学习记录");
                        emptyText.setTextSize(16);
                        emptyText.setTextColor(0xFF999999);
                        emptyText.setGravity(Gravity.CENTER);
                        emptyText.setPadding(0, dp(40), 0, dp(40));
                        myBookListContainer.addView(emptyText);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "加载异常: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void buildMyBooksList(JsonArray books) {
        myBookListContainer.removeAllViews();

        if (books == null || books.size() == 0) {
            TextView emptyText = new TextView(this);
            emptyText.setText("暂无已学词书");
            emptyText.setTextSize(16);
            emptyText.setTextColor(0xFF999999);
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(0, dp(40), 0, dp(40));
            myBookListContainer.addView(emptyText);
            return;
        }

        // 按最后学习时间降序排序
        List<JsonObject> sortedBooks = new ArrayList<>();
        for (JsonElement e : books) {
            sortedBooks.add(e.getAsJsonObject());
        }
        sortedBooks.sort((a, b) -> {
            String timeA = a.has("lastStudyTime") ? a.get("lastStudyTime").getAsString() : "";
            String timeB = b.has("lastStudyTime") ? b.get("lastStudyTime").getAsString() : "";
            return timeB.compareTo(timeA); // 降序
        });

        for (JsonObject b : sortedBooks) {
            // 创建词书卡片
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setPadding(dp(16), dp(16), dp(16), dp(16));
            card.setBackground(getDrawable(R.drawable.bg_white_card));
            card.setElevation(dp(2));
            card.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 0, dp(12));
            card.setLayoutParams(cardParams);

            // 左侧信息区域
            LinearLayout leftInfo = new LinearLayout(this);
            leftInfo.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1);
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

            // 学习进度（去重后的单词数）
            TextView progressInfo = new TextView(this);
            int learned = b.has("learnedCount") ? b.get("learnedCount").getAsInt() : 0;
            int total = b.has("wordCount") ? b.get("wordCount").getAsInt() : 0;
            progressInfo.setText("已学: " + learned + " / " + total + " 词");
            progressInfo.setTextSize(14);
            progressInfo.setTextColor(0xFF666666);
            progressInfo.setPadding(0, 0, 0, dp(8));
            leftInfo.addView(progressInfo);

            // 最后学习时间
            if (b.has("lastStudyTime") && !b.get("lastStudyTime").isJsonNull()) {
                TextView lastTime = new TextView(this);
                String timeStr = b.get("lastStudyTime").getAsString();
                // 格式化日期显示
                if (timeStr.length() >= 10) {
                    lastTime.setText("最后学习: " + timeStr.substring(0, 10));
                } else {
                    lastTime.setText("最后学习: " + timeStr);
                }
                lastTime.setTextSize(14);
                lastTime.setTextColor(0xFF666666);
                leftInfo.addView(lastTime);
            }

            card.addView(leftInfo);

            // 右侧继续学习按钮
            Button studyBtn = new Button(this);
            studyBtn.setText("继续学习");
            studyBtn.setTextSize(14);
            studyBtn.setTextColor(0xFFFFFFFF);
            studyBtn.setPadding(dp(20), dp(8), dp(20), dp(8));
            studyBtn.setMinHeight(0);
            studyBtn.setMinimumHeight(0);
            studyBtn.setBackground(getDrawable(R.drawable.bg_btn_primary));
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            btnParams.setMargins(dp(16), 0, 0, 0);
            studyBtn.setLayoutParams(btnParams);

            String bookId = b.has("wordBookId") ? b.get("wordBookId").getAsString() : "";
            studyBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, BookDetailActivity.class);
                intent.putExtra("bookId", bookId);
                intent.putExtra("studyMode", "continue");
                startActivity(intent);
            });
            card.addView(studyBtn);

            myBookListContainer.addView(card);
        }
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
