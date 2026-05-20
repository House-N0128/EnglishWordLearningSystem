package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class BookDetailActivity extends AppCompatActivity {

    private TextView tvBookName;
    private TextView tvDifficultyCount;
    private TextView tvCreateTime;
    private TextView tvDescription;
    private TextView tvWordsTitle;
    private LinearLayout wordsContainer;
    private Button btnStartStudy;
    private String bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        bookId = getIntent().getStringExtra("bookId");

        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        tvBookName = findViewById(R.id.tv_book_name);
        tvDifficultyCount = findViewById(R.id.tv_difficulty_count);
        tvCreateTime = findViewById(R.id.tv_create_time);
        tvDescription = findViewById(R.id.tv_description);
        tvWordsTitle = findViewById(R.id.tv_words_title);
        wordsContainer = findViewById(R.id.words_container);
        btnStartStudy = findViewById(R.id.btn_start_study);

        if (bookId == null) {
            tvBookName.setText("缺少词书参数");
            tvWordsTitle.setVisibility(View.GONE);
            wordsContainer.setVisibility(View.GONE);
            btnStartStudy.setVisibility(View.GONE);
            return;
        }

        btnStartStudy.setOnClickListener(v -> {
            Intent intent = new Intent(this, WordStudyActivity.class);
            intent.putExtra("bookId", bookId);
            startActivity(intent);
        });

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            try {
                JsonObject booksRes = ApiClient.get().get("/api/wordbooks");
                JsonObject wordsRes = ApiClient.get().get("/api/words?wordBookId=" + bookId);

                JsonObject book = null;
                if (booksRes.get("code").getAsInt() == 200) {
                    JsonArray books = booksRes.getAsJsonArray("data");
                    for (int i = 0; i < books.size(); i++) {
                        JsonObject b = books.get(i).getAsJsonObject();
                        if (bookId.equals(b.has("wordBookId") ? b.get("wordBookId").getAsString() : "")) {
                            book = b;
                            break;
                        }
                    }
                }

                JsonArray words = null;
                if (wordsRes.get("code").getAsInt() == 200) {
                    words = wordsRes.getAsJsonArray("data");
                }

                JsonObject finalBook = book;
                JsonArray finalWords = words;
                runOnUiThread(() -> buildUI(finalBook, finalWords));
            } catch (Exception e) {
                runOnUiThread(() -> tvBookName.setText("加载失败"));
            }
        }).start();
    }

    private void buildUI(JsonObject book, JsonArray words) {
        if (book != null) {
            tvBookName.setText(book.has("wordBookName") ? book.get("wordBookName").getAsString() : "");

            String difficulty = book.has("difficultyLevel") ? book.get("difficultyLevel").getAsString() : "";
            int wordCount = words != null ? words.size() : 0;
            tvDifficultyCount.setText("难度: " + difficulty + " | 共 " + wordCount + " 词");

            tvCreateTime.setText("创建时间: " + (book.has("createTime") ? trimDate(book.get("createTime").getAsString()) : ""));

            tvDescription.setText(book.has("wordBookDescription") && !book.get("wordBookDescription").isJsonNull()
                    ? book.get("wordBookDescription").getAsString() : "暂无简介");

            // 显示单词预览（前10个单词）
            if (words != null && words.size() > 0) {
                int previewCount = Math.min(words.size(), 10);
                tvWordsTitle.setText("单词预览（前" + previewCount + "个，共" + words.size() + "个）");
                wordsContainer.removeAllViews();

                for (int i = 0; i < previewCount; i++) {
                    JsonObject w = words.get(i).getAsJsonObject();
                    View wordView = LayoutInflater.from(this).inflate(R.layout.item_word, wordsContainer, false);

                    TextView tvEnglish = wordView.findViewById(R.id.tv_word_english);
                    TextView tvChinese = wordView.findViewById(R.id.tv_word_chinese);

                    tvEnglish.setText(w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "");
                    tvChinese.setText(w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "");

                    wordsContainer.addView(wordView);
                }
            } else {
                tvWordsTitle.setText("暂无单词");
                wordsContainer.removeAllViews();
            }
        }
    }

    private String trimDate(String dt) {
        return dt != null && dt.length() >= 10 ? dt.substring(0, 10) : dt;
    }
}
