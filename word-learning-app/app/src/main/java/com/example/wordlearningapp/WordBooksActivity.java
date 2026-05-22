package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class WordBooksActivity extends AppCompatActivity {

    private LinearLayout bookListContainer;
    private EditText etSearchBook;
    private Spinner spinnerDifficulty;
    private Button btnSearchBook;
    private boolean isAdmin;
    private JsonArray allBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_books);

        isAdmin = "admin".equals(AuthManager.get().getRole());

        // 初始化视图
        bookListContainer = findViewById(R.id.book_list_container);
        etSearchBook = findViewById(R.id.et_search_book);
        spinnerDifficulty = findViewById(R.id.spinner_difficulty);
        btnSearchBook = findViewById(R.id.btn_search_book);

        // 设置难度等级下拉框
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"难度等级", "全部", "初级", "中级", "高级"});
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(difficultyAdapter);

        // 搜索按钮点击事件
        btnSearchBook.setOnClickListener(v -> {
            filterBooks();
        });

        // 难度筛选
        spinnerDifficulty.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) { filterBooks(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // 我的词书按钮
        findViewById(R.id.btn_my_books).setOnClickListener(v -> {
            startActivity(new Intent(this, MyBooksActivity.class));
        });

        // 底部导航栏点击事件
        setupBottomNavigation();

        // 加载词书数据
        loadBooks();
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
                    startActivity(new Intent(WordBooksActivity.this, MainActivity.class));
                } else if (id == R.id.nav_search) {
                    startActivity(new Intent(WordBooksActivity.this, WordSearchActivity.class));
                } else if (id == R.id.nav_collection) {
                    startActivity(new Intent(WordBooksActivity.this, CollectionsActivity.class));
                } else if (id == R.id.nav_records) {
                    startActivity(new Intent(WordBooksActivity.this, StudyRecordsActivity.class));
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(WordBooksActivity.this, ProfileActivity.class));
                }
            }
        };

        navHome.setOnClickListener(navClickListener);
        navSearch.setOnClickListener(navClickListener);
        navCollection.setOnClickListener(navClickListener);
        navRecords.setOnClickListener(navClickListener);
        navProfile.setOnClickListener(navClickListener);

        // 当前页面不设置点击事件
    }

    private void loadBooks() {
        new Thread(() -> {
            try {
                JsonObject r = ApiClient.get().get("/api/wordbooks");
                if (r.get("code").getAsInt() == 200) {
                    allBooks = r.getAsJsonArray("data");
                    runOnUiThread(() -> filterBooks());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "加载词书失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void filterBooks() {
        String kw = etSearchBook.getText().toString().trim().toLowerCase();
        String diff = spinnerDifficulty.getSelectedItem() != null ? spinnerDifficulty.getSelectedItem().toString() : "难度等级";

        bookListContainer.removeAllViews();
        if (allBooks == null) return;

        for (int i = 0; i < allBooks.size(); i++) {
            JsonObject b = allBooks.get(i).getAsJsonObject();
            String name = b.has("wordBookName") ? b.get("wordBookName").getAsString() : "";
            String bookDiff = b.has("difficultyLevel") ? b.get("difficultyLevel").getAsString() : "";
            int wc = b.has("wordCount") ? b.get("wordCount").getAsInt() : 0;
            String cTime = b.has("createTime") ? b.get("createTime").getAsString().substring(0, 10) : "";
            String bid = b.has("wordBookId") ? b.get("wordBookId").getAsString() : "";

            // 筛选条件
            if (!kw.isEmpty() && !name.toLowerCase().contains(kw)) continue;
            if (!"难度等级".equals(diff) && !"全部".equals(diff) && !diff.equals(bookDiff)) continue;

            // 创建词书卡片（水平布局）
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
            bookName.setText(name);
            bookName.setTextSize(18);
            bookName.setTextColor(0xFF318af8);
            bookName.setTypeface(null, android.graphics.Typeface.BOLD);
            bookName.setPadding(0, 0, 0, dp(8));
            leftInfo.addView(bookName);

            // 难度和词数
            TextView bookInfo = new TextView(this);
            bookInfo.setText(bookDiff + " | " + wc + "词");
            bookInfo.setTextSize(14);
            bookInfo.setTextColor(0xFF666666);
            bookInfo.setPadding(0, 0, 0, dp(8));
            leftInfo.addView(bookInfo);

            // 创建时间
            TextView bookTime = new TextView(this);
            bookTime.setText("创建时间: " + cTime);
            bookTime.setTextSize(14);
            bookTime.setTextColor(0xFF666666);
            leftInfo.addView(bookTime);

            card.addView(leftInfo);

            // 右侧开始学习按钮
            Button studyBtn = new Button(this);
            studyBtn.setText("开始学习");
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

            studyBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, BookDetailActivity.class);
                intent.putExtra("bookId", bid);
                startActivity(intent);
            });
            card.addView(studyBtn);

            bookListContainer.addView(card);
        }

        // 如果没有匹配的词书
        if (bookListContainer.getChildCount() == 0) {
            TextView noResult = new TextView(this);
            noResult.setText("没有找到匹配的词书");
            noResult.setTextSize(16);
            noResult.setTextColor(0xFF999999);
            noResult.setGravity(Gravity.CENTER);
            noResult.setPadding(0, dp(40), 0, dp(40));
            bookListContainer.addView(noResult);
        }
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
