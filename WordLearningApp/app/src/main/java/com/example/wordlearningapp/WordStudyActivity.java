package com.example.wordlearningapp;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class WordStudyActivity extends AppCompatActivity {

    private LinearLayout contentArea;
    private String bookId;
    private JsonArray words;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        bookId = getIntent().getStringExtra("bookId");
        ((TextView) findViewById(R.id.toolbar_title)).setText("单词学习");
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());
        contentArea = findViewById(R.id.content_area);

        if (bookId == null) {
            showText("缺少词书参数");
            return;
        }
        loadWords();
    }

    private void loadWords() {
        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().get("/api/words?wordBookId=" + bookId);
                if (res.get("code").getAsInt() == 200) {
                    words = res.getAsJsonArray("data");
                    runOnUiThread(() -> {
                        if (words.size() == 0) {
                            showText("该词书暂无单词");
                        } else {
                            showWord(0);
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> showText("加载失败"));
            }
        }).start();
    }

    private void showWord(int index) {
        if (words == null || words.size() == 0) return;
        if (index < 0) index = 0;
        if (index >= words.size()) index = words.size() - 1;
        currentIndex = index;

        JsonObject w = words.get(index).getAsJsonObject();
        contentArea.removeAllViews();

        // Progress
        TextView progress = new TextView(this);
        progress.setText("进度 " + (index + 1) + "/" + words.size());
        progress.setTextSize(16);
        progress.setTextColor(0xFF318af8);
        progress.setGravity(Gravity.CENTER);
        progress.setPadding(0, 8, 0, 24);
        contentArea.addView(progress);

        // Word card container
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFFFFFFFF);
        card.setPadding(26, 26, 26, 26);
        card.setElevation(4);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cp.setMargins(0, 0, 0, 24);
        card.setLayoutParams(cp);

        // Spelling
        TextView spelling = new TextView(this);
        spelling.setText(w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "");
        spelling.setTextSize(22);
        spelling.setTextColor(0xFF318af8);
        spelling.setGravity(Gravity.CENTER);
        card.addView(spelling);

        // Phonetic
        TextView phonetic = new TextView(this);
        phonetic.setText(w.has("phoneticSymbol") ? w.get("phoneticSymbol").getAsString() : "");
        phonetic.setTextSize(15);
        phonetic.setTextColor(0xFF547bbc);
        phonetic.setGravity(Gravity.CENTER);
        phonetic.setPadding(0, 6, 0, 6);
        card.addView(phonetic);

        // Chinese
        TextView chinese = new TextView(this);
        chinese.setText(w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "");
        chinese.setTextSize(15);
        chinese.setTextColor(0xFF273245);
        chinese.setGravity(Gravity.CENTER);
        chinese.setPadding(0, 6, 0, 6);
        card.addView(chinese);

        // Example
        if (w.has("exampleSentence") && !w.get("exampleSentence").isJsonNull()) {
            TextView example = new TextView(this);
            example.setText("例句：" + w.get("exampleSentence").getAsString());
            example.setTextSize(14);
            example.setTextColor(0xFF6578a0);
            example.setPadding(0, 6, 0, 6);
            card.addView(example);
        }

        contentArea.addView(card);

        // Buttons row
        LinearLayout btns = new LinearLayout(this);
        btns.setOrientation(LinearLayout.HORIZONTAL);
        btns.setGravity(Gravity.CENTER);

        Button btnCollect = new Button(this);
        btnCollect.setText("⭐ 收藏");
        btnCollect.setTextColor(0xFFFFFFFF);
        btnCollect.setBackgroundColor(0xFF318af8);
        String wordId = w.has("wordId") ? w.get("wordId").getAsString() : "";
        btnCollect.setOnClickListener(v -> collectWord(wordId));
        btns.addView(btnCollect);

        Button btnPrev = new Button(this);
        btnPrev.setText("上一个");
        btnPrev.setTextColor(0xFFFFFFFF);
        btnPrev.setBackgroundColor(0xFF318af8);
        btnPrev.setOnClickListener(v -> showWord(currentIndex - 1));
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        pp.setMargins(10, 0, 10, 0);
        btnPrev.setLayoutParams(pp);
        btns.addView(btnPrev);

        Button btnNext = new Button(this);
        btnNext.setText("下一个");
        btnNext.setTextColor(0xFFFFFFFF);
        btnNext.setBackgroundColor(0xFF318af8);
        btnNext.setOnClickListener(v -> showWord(currentIndex + 1));
        btns.addView(btnNext);

        contentArea.addView(btns);

        // Mark learned
        Button btnLearned = new Button(this);
        btnLearned.setText("标记已掌握");
        btnLearned.setTextColor(0xFFFFFFFF);
        btnLearned.setBackgroundColor(0xFF52e8bc);
        btnLearned.setTextSize(16);
        btnLearned.setPadding(13, 13, 13, 13);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 24, 0, 0);
        btnLearned.setLayoutParams(lp);
        btnLearned.setOnClickListener(v -> markLearned(wordId));
        contentArea.addView(btnLearned);
    }

    private void collectWord(String wordId) {
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("wordId", wordId);
                JsonObject res = ApiClient.get().post("/api/collections/add", body);
                String msg = res.has("message") ? res.get("message").getAsString() : "操作失败";
                runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void markLearned(String wordId) {
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("wordId", wordId);
                body.addProperty("learnedWordBookId", bookId);
                JsonObject res = ApiClient.get().post("/api/records/add", body);
                String msg = res.has("message") ? res.get("message").getAsString() : "操作失败";
                runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show());
            }
        }).start();
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
}
