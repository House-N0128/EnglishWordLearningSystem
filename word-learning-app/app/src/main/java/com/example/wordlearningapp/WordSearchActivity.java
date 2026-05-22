package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class WordSearchActivity extends AppCompatActivity {

    private LinearLayout wordListContainer;
    private EditText etSearch;
    private Button btnSearch;
    private JsonArray allWords;
    private Map<String, String> bookIdToName = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 初始化视图
        wordListContainer = findViewById(R.id.word_list_container);
        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_search);

        // 搜索按钮点击事件
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterWords();
            }
        });

        // 搜索框回车事件
        etSearch.setOnEditorActionListener(new android.widget.TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(android.widget.TextView v, int actionId, android.view.KeyEvent event) {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                    filterWords();
                    return true;
                }
                return false;
            }
        });

        // 搜索框输入监听
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterWords(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // 底部导航栏点击事件
        setupBottomNavigation();

        // 加载单词数据
        loadWords();
    }

    private void setupBottomNavigation() {
        TextView navHome = findViewById(R.id.nav_home);
        TextView navBooks = findViewById(R.id.nav_books);
        TextView navSearch = findViewById(R.id.nav_search);
        TextView navCollection = findViewById(R.id.nav_collection);
        TextView navRecords = findViewById(R.id.nav_records);
        TextView navProfile = findViewById(R.id.nav_profile);

        View.OnClickListener navClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(WordSearchActivity.this, MainActivity.class));
                } else if (id == R.id.nav_books) {
                    startActivity(new Intent(WordSearchActivity.this, WordBooksActivity.class));
                } else if (id == R.id.nav_collection) {
                    startActivity(new Intent(WordSearchActivity.this, CollectionsActivity.class));
                } else if (id == R.id.nav_records) {
                    startActivity(new Intent(WordSearchActivity.this, StudyRecordsActivity.class));
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(WordSearchActivity.this, ProfileActivity.class));
                }
            }
        };

        navHome.setOnClickListener(navClickListener);
        navBooks.setOnClickListener(navClickListener);
        navCollection.setOnClickListener(navClickListener);
        navRecords.setOnClickListener(navClickListener);
        navProfile.setOnClickListener(navClickListener);

        // 当前页面不设置点击事件
    }

    private void loadWords() {
        new Thread(() -> {
            try {
                // 加载所有单词
                JsonObject r = ApiClient.get().get("/api/words/all");
                if (r.get("code").getAsInt() == 200) {
                    allWords = r.getAsJsonArray("data");
                    runOnUiThread(() -> filterWords());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "加载单词失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void filterWords() {
        String kw = etSearch.getText().toString().trim().toLowerCase();

        wordListContainer.removeAllViews();
        if (allWords == null) return;

        for (int i = 0; i < allWords.size(); i++) {
            JsonObject w = allWords.get(i).getAsJsonObject();
            String spelling = w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "";
            String definition = w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "";
            String wordId = w.has("wordId") ? w.get("wordId").getAsString() : "";
            String pos = w.has("partOfSpeech") && !w.get("partOfSpeech").isJsonNull() ? w.get("partOfSpeech").getAsString() : "";

            // 筛选条件：英文拼写或中文释义匹配
            if (!kw.isEmpty() && !spelling.toLowerCase().contains(kw) && !definition.toLowerCase().contains(kw)) continue;

            // 创建单词卡片（水平布局）
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

            // 第一行：单词拼写
            TextView wordSpelling = new TextView(this);
            wordSpelling.setText(spelling);
            wordSpelling.setTextSize(18);
            wordSpelling.setTextColor(0xFF318af8);
            wordSpelling.setTypeface(null, android.graphics.Typeface.BOLD);
            wordSpelling.setPadding(0, 0, 0, dp(8));
            leftInfo.addView(wordSpelling);

            // 第二行：词性 + 释义
            LinearLayout posDefRow = new LinearLayout(this);
            posDefRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            posDefRow.setLayoutParams(rowParams);

            if (!pos.isEmpty()) {
                TextView wordPos = new TextView(this);
                wordPos.setText(pos);
                wordPos.setTextSize(14);
                wordPos.setTextColor(0xFF666666);
                wordPos.setPadding(0, 0, dp(12), 0);
                posDefRow.addView(wordPos);
            }

            TextView wordDefinition = new TextView(this);
            wordDefinition.setText(definition);
            wordDefinition.setTextSize(14);
            wordDefinition.setTextColor(0xFF333333);
            posDefRow.addView(wordDefinition);

            leftInfo.addView(posDefRow);

            card.addView(leftInfo);

            // 右侧查看详情按钮
            Button detailBtn = new Button(this);
            detailBtn.setText("查看详情");
            detailBtn.setTextSize(14);
            detailBtn.setTextColor(0xFFFFFFFF);
            detailBtn.setPadding(dp(20), dp(8), dp(20), dp(8));
            detailBtn.setMinHeight(0);
            detailBtn.setMinimumHeight(0);
            detailBtn.setBackground(getDrawable(R.drawable.bg_btn_primary));
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            btnParams.setMargins(dp(16), 0, 0, 0);
            detailBtn.setLayoutParams(btnParams);

            final String finalWordId = wordId;
            detailBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(WordSearchActivity.this, WordDetailActivity.class);
                    intent.putExtra("wordId", finalWordId);
                    startActivity(intent);
                }
            });
            card.addView(detailBtn);

            wordListContainer.addView(card);
        }

        // 如果没有匹配的单词
        if (wordListContainer.getChildCount() == 0) {
            TextView emptyText = new TextView(this);
            emptyText.setText(kw.isEmpty() ? "输入关键词开始搜索" : "未找到相关单词");
            emptyText.setTextSize(16);
            emptyText.setTextColor(0xFF999999);
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(0, dp(40), 0, dp(40));
            wordListContainer.addView(emptyText);
        }
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
