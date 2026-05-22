package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public class CollectionsActivity extends AppCompatActivity {

    private static final String TAG = "CollectionsActivity";

    private LinearLayout collectionListContainer;
    private EditText etSearch;
    private Button btnSearch;
    private JsonArray allCollections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collections);

        // 初始化视图
        collectionListContainer = findViewById(R.id.collection_list_container);
        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_search);

        // 搜索按钮点击事件
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterCollections();
            }
        });

        // 搜索框回车事件
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                filterCollections();
                return true;
            }
            return false;
        });

        // 搜索框输入监听
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterCollections(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // 底部导航栏点击事件
        setupBottomNavigation();

        // 加载收藏数据
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "页面恢复，刷新收藏数据");
        loadData();
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
                    startActivity(new Intent(CollectionsActivity.this, MainActivity.class));
                } else if (id == R.id.nav_books) {
                    startActivity(new Intent(CollectionsActivity.this, WordBooksActivity.class));
                } else if (id == R.id.nav_search) {
                    startActivity(new Intent(CollectionsActivity.this, WordSearchActivity.class));
                } else if (id == R.id.nav_records) {
                    startActivity(new Intent(CollectionsActivity.this, StudyRecordsActivity.class));
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(CollectionsActivity.this, ProfileActivity.class));
                }
            }
        };

        navHome.setOnClickListener(navClickListener);
        navBooks.setOnClickListener(navClickListener);
        navSearch.setOnClickListener(navClickListener);
        navRecords.setOnClickListener(navClickListener);
        navProfile.setOnClickListener(navClickListener);

        // 当前页面不设置点击事件
    }

    private void loadData() {
        new Thread(() -> {
            try {
                Log.d(TAG, "开始加载收藏数据");
                JsonObject res = ApiClient.get().get("/api/collections");
                Log.d(TAG, "API响应: " + res.toString());

                if (res.has("code") && res.get("code").getAsInt() == 200) {
                    if (res.has("data")) {
                        allCollections = res.getAsJsonArray("data");
                        Log.d(TAG, "收藏数据数量: " + allCollections.size());
                        runOnUiThread(() -> filterCollections());
                    } else {
                        runOnUiThread(() -> {
                            allCollections = new JsonArray();
                            filterCollections();
                        });
                    }
                } else {
                    String errorMsg = res.has("message") ? res.get("message").getAsString() : "加载失败";
                    Log.e(TAG, "加载失败: " + errorMsg);
                    runOnUiThread(() -> Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e(TAG, "加载异常: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "加载失败: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void filterCollections() {
        if (allCollections == null) {
            allCollections = new JsonArray();
        }

        String searchText = etSearch != null ? etSearch.getText().toString().trim().toLowerCase() : "";
        JsonArray filteredData = new JsonArray();

        for (int i = 0; i < allCollections.size(); i++) {
            JsonElement element = allCollections.get(i);
            if (!element.isJsonObject()) continue;

            JsonObject c = element.getAsJsonObject();
            String spelling = c.has("englishSpelling") ? c.get("englishSpelling").getAsString().toLowerCase() : "";

            if (searchText.isEmpty() || spelling.contains(searchText)) {
                filteredData.add(c);
            }
        }

        runOnUiThread(() -> displayCollections(filteredData));
    }

    private void displayCollections(JsonArray filteredData) {
        collectionListContainer.removeAllViews();

        if (filteredData.size() == 0) {
            TextView emptyText = new TextView(this);
            String searchText = etSearch != null ? etSearch.getText().toString().trim() : "";
            emptyText.setText(searchText.isEmpty() ? "暂无收藏" : "未找到相关单词");
            emptyText.setTextSize(16);
            emptyText.setTextColor(0xFF999999);
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(0, dp(40), 0, dp(40));
            collectionListContainer.addView(emptyText);
            return;
        }

        for (int i = 0; i < filteredData.size(); i++) {
            JsonElement element = filteredData.get(i);
            if (!element.isJsonObject()) continue;

            JsonObject c = element.getAsJsonObject();

            String spelling = c.has("englishSpelling") ? c.get("englishSpelling").getAsString() : "";
            String phonetic = "";
            if (c.has("phoneticSymbol") && !c.get("phoneticSymbol").isJsonNull()) {
                phonetic = c.get("phoneticSymbol").getAsString();
            }
            String pos = c.has("partOfSpeech") && !c.get("partOfSpeech").isJsonNull()
                    ? c.get("partOfSpeech").getAsString() : "";

            String date = "";
            if (c.has("collectionTime") && !c.get("collectionTime").isJsonNull()) {
                String fullDate = c.get("collectionTime").getAsString();
                date = fullDate.length() >= 10 ? fullDate.substring(0, 10) : fullDate;
            }

            String wordId = c.has("wordId") ? c.get("wordId").getAsString() : "";

            // 创建卡片（垂直布局）
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(16), dp(16), dp(16), dp(16));
            card.setBackground(getDrawable(R.drawable.bg_white_card));
            card.setElevation(dp(2));

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 0, dp(12));
            card.setLayoutParams(cardParams);

            // 第一部分：单词信息
            LinearLayout wordInfoArea = new LinearLayout(this);
            wordInfoArea.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams wordInfoParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            wordInfoArea.setLayoutParams(wordInfoParams);

            // 第一行：单词 + 音标
            LinearLayout wordRow = new LinearLayout(this);
            wordRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams wordRowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            wordRow.setLayoutParams(wordRowParams);

            TextView tvWord = new TextView(this);
            tvWord.setText(spelling);
            tvWord.setTextSize(20);
            tvWord.setTextColor(0xFF318af8);
            tvWord.setTypeface(null, android.graphics.Typeface.BOLD);
            wordRow.addView(tvWord);

            if (!phonetic.isEmpty()) {
                TextView tvPhonetic = new TextView(this);
                tvPhonetic.setText("  [" + phonetic + "]");
                tvPhonetic.setTextSize(14);
                tvPhonetic.setTextColor(0xFF666666);
                wordRow.addView(tvPhonetic);
            }

            wordInfoArea.addView(wordRow);

            // 第二行：词性（如果有）
            if (!pos.isEmpty()) {
                TextView tvPos = new TextView(this);
                tvPos.setText(pos);
                tvPos.setTextSize(14);
                tvPos.setTextColor(0xFF318af8);
                tvPos.setPadding(0, dp(8), 0, 0);
                wordInfoArea.addView(tvPos);
            }

            // 第三行：收藏时间
            TextView tvTime = new TextView(this);
            tvTime.setText("收藏时间：" + date);
            tvTime.setTextSize(13);
            tvTime.setTextColor(0xFF999999);
            tvTime.setPadding(0, dp(12), 0, 0);
            wordInfoArea.addView(tvTime);

            card.addView(wordInfoArea);

            // 第二部分：按钮区域（单独一行）
            LinearLayout buttonArea = new LinearLayout(this);
            buttonArea.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams buttonAreaParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            buttonAreaParams.setMargins(0, dp(16), 0, 0);
            buttonArea.setLayoutParams(buttonAreaParams);
            buttonArea.setGravity(Gravity.END);

            // 详情按钮
            Button btnDetail = new Button(this);
            btnDetail.setText("详情");
            btnDetail.setTextSize(14);
            btnDetail.setTextColor(0xFFFFFFFF);
            btnDetail.setPadding(dp(24), dp(8), dp(24), dp(8));
            btnDetail.setMinHeight(0);
            btnDetail.setMinimumHeight(0);
            btnDetail.setBackground(getDrawable(R.drawable.bg_btn_blue_round));
            LinearLayout.LayoutParams detailBtnParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    dp(36));
            detailBtnParams.setMargins(0, 0, dp(12), 0);
            btnDetail.setLayoutParams(detailBtnParams);

            final String finalWordId = wordId;
            btnDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (finalWordId != null && !finalWordId.isEmpty()) {
                        Intent intent = new Intent(CollectionsActivity.this, WordDetailActivity.class);
                        intent.putExtra("wordId", finalWordId);
                        intent.putExtra("fromCollection", true);
                        startActivity(intent);
                    } else {
                        Toast.makeText(CollectionsActivity.this, "无效的单词ID", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            buttonArea.addView(btnDetail);

            // 取消收藏按钮
            Button btnCancel = new Button(this);
            btnCancel.setText("取消收藏");
            btnCancel.setTextSize(14);
            btnCancel.setTextColor(0xFF318af8);
            btnCancel.setPadding(dp(24), dp(8), dp(24), dp(8));
            btnCancel.setMinHeight(0);
            btnCancel.setMinimumHeight(0);
            btnCancel.setBackground(getDrawable(R.drawable.bg_input));
            LinearLayout.LayoutParams cancelBtnParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    dp(36));
            btnCancel.setLayoutParams(cancelBtnParams);

            final String finalSpelling = spelling;
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (finalWordId == null || finalWordId.isEmpty()) {
                        Toast.makeText(CollectionsActivity.this, "无效的单词ID", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new AlertDialog.Builder(CollectionsActivity.this)
                            .setTitle("确认取消")
                            .setMessage("确定取消收藏单词\"" + finalSpelling + "\"？")
                            .setPositiveButton("确定", (d, w) -> cancelCollection(finalWordId))
                            .setNegativeButton("取消", null)
                            .show();
                }
            });
            buttonArea.addView(btnCancel);

            card.addView(buttonArea);
            collectionListContainer.addView(card);
        }
    }

    private void cancelCollection(String wordId) {
        Log.d(TAG, "开始取消收藏, wordId: " + wordId);

        if (wordId == null || wordId.isEmpty()) {
            Toast.makeText(this, "无效的单词ID", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("wordId", wordId);

                String url = "/api/collections/remove";
                Log.d(TAG, "DELETE请求: " + url + ", body: " + body.toString());

                JsonObject res = ApiClient.get().delete(url, body);
                Log.d(TAG, "响应: " + res.toString());

                runOnUiThread(() -> {
                    if (res != null && res.has("code") && res.get("code").getAsInt() == 200) {
                        Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "取消收藏成功，重新加载数据");
                        loadData();
                    } else {
                        String message = "取消失败";
                        if (res != null && res.has("message")) {
                            message = res.get("message").getAsString();
                        } else if (res != null && res.has("error")) {
                            message = res.get("error").getAsString();
                        }
                        Log.e(TAG, "取消收藏失败: " + message);
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "取消收藏异常: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "网络错误: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
