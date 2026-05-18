package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class BookDetailActivity extends AppCompatActivity {

    private LinearLayout contentArea;
    private String bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        bookId = getIntent().getStringExtra("bookId");
        ((TextView) findViewById(R.id.toolbar_title)).setText("词书详情");
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());
        contentArea = findViewById(R.id.content_area);

        contentArea.addView(new ProgressBar(this));
        if (bookId == null) {
            showText("缺少词书参数");
            return;
        }
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
                runOnUiThread(() -> showText("加载失败"));
            }
        }).start();
    }

    private void buildUI(JsonObject book, JsonArray words) {
        contentArea.removeAllViews();

        if (book != null) {
            addLabelValue("词书名称", book.has("wordBookName") ? book.get("wordBookName").getAsString() : "");
            addLabelValue("难度", book.has("difficultyLevel") ? book.get("difficultyLevel").getAsString() : "");
            int wc = words != null ? words.size() : (book.has("wordCount") ? book.get("wordCount").getAsInt() : 0);
            addLabelValue("单词数量", String.valueOf(wc));
            addLabelValue("创建时间", book.has("createTime") ? trimDate(book.get("createTime").getAsString()) : "");
            addLabelValue("简介", book.has("wordBookDescription") && !book.get("wordBookDescription").isJsonNull()
                    ? book.get("wordBookDescription").getAsString() : "暂无");

            TextView wordsTitle = new TextView(this);
            wordsTitle.setText("单词列表（" + wc + "个）");
            wordsTitle.setTextSize(17);
            wordsTitle.setTextColor(0xFF318af8);
            wordsTitle.setPadding(0, 24, 0, 12);
            contentArea.addView(wordsTitle);

            if (words != null && words.size() > 0) {
                for (int i = 0; i < words.size(); i++) {
                    JsonObject w = words.get(i).getAsJsonObject();
                    String eng = w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "";
                    String chn = w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "";
                    TextView wt = new TextView(this);
                    wt.setText(eng + "  " + chn);
                    wt.setTextSize(15);
                    wt.setTextColor(0xFF273245);
                    wt.setPadding(12, 10, 12, 10);
                    wt.setBackgroundColor(0xFFFFFFFF);
                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    p.setMargins(0, 0, 0, 8);
                    wt.setLayoutParams(p);
                    contentArea.addView(wt);
                }
            } else {
                showText("暂无单词");
            }
        }

        Button btn = new Button(this);
        btn.setText("开始学习");
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackgroundColor(0xFF318af8);
        btn.setTextSize(17);
        btn.setPadding(13, 13, 13, 13);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0, 28, 0, 0);
        btn.setLayoutParams(bp);
        btn.setOnClickListener(v -> {
            Intent intent = new Intent(this, WordStudyActivity.class);
            intent.putExtra("bookId", bookId);
            startActivity(intent);
        });
        contentArea.addView(btn);
    }

    private void addLabelValue(String label, String value) {
        TextView lv = new TextView(this);
        lv.setText(label + ": " + value);
        lv.setTextSize(15);
        lv.setTextColor(0xFF273245);
        lv.setPadding(0, 8, 0, 4);
        contentArea.addView(lv);
    }

    private void showText(String msg) {
        contentArea.removeAllViews();
        TextView tv = new TextView(this);
        tv.setText(msg);
        tv.setTextSize(16);
        tv.setTextColor(0xFF8899aa);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, 40, 0, 40);
        contentArea.addView(tv);
    }

    private String trimDate(String dt) {
        return dt != null && dt.length() >= 10 ? dt.substring(0, 10) : dt;
    }
}
