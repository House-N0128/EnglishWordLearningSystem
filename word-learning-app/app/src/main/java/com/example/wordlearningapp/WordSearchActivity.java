package com.example.wordlearningapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextWatcher;
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

import java.util.HashMap;
import java.util.Map;

public class WordSearchActivity extends AppCompatActivity {

    private LinearLayout wordListContainer;
    private EditText etSearch;
    private Spinner spBook, spStatus;
    private boolean isAdmin;
    private JsonArray allWords;
    private Map<String, String> bookIdToName = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isAdmin = "admin".equals(AuthManager.get().getRole());

        if (isAdmin) {
            buildAdminUI();
        } else {
            setContentView(R.layout.activity_search);
            wordListContainer = findViewById(R.id.word_list_container);
            etSearch = findViewById(R.id.et_search);
            findViewById(R.id.btn_search).setOnClickListener(v -> filterWords());
            etSearch.setOnEditorActionListener((v, actionId, event) -> { if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) { filterWords(); return true; } return false; });
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) { filterWords(); }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
            setupBottomNavigation();
            loadWords();
        }
    }

    private void buildAdminUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFf2f8fc);

        // Top bar (same as AdminMainActivity)
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(0xFF318af8);
        topBar.setPadding(dp(16), 0, dp(16), 0);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));

        TextView adminUser = new TextView(this);
        adminUser.setText("管理员：" + AuthManager.get().getUserId());
        adminUser.setTextSize(16);
        adminUser.setTextColor(0xFFFFFFFF);
        adminUser.setTypeface(null, Typeface.BOLD);
        adminUser.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        topBar.addView(adminUser);

        TextView btnRefresh = new TextView(this);
        btnRefresh.setText("刷新");
        btnRefresh.setTextSize(15);
        btnRefresh.setTextColor(0xFF318af8);
        btnRefresh.setBackgroundColor(0xFFFFFFFF);
        btnRefresh.setPadding(dp(16), dp(7), dp(16), dp(7));
        btnRefresh.setGravity(Gravity.CENTER);
        GradientDrawable rfBg = new GradientDrawable();
        rfBg.setColor(0xFFFFFFFF);
        rfBg.setCornerRadius(dp(16));
        btnRefresh.setBackground(rfBg);
        btnRefresh.setOnClickListener(v -> loadWords());
        topBar.addView(btnRefresh);

        TextView btnLogout = new TextView(this);
        btnLogout.setText("退出登录");
        btnLogout.setTextSize(15);
        btnLogout.setTextColor(0xFF318af8);
        btnLogout.setBackgroundColor(0xFFFFFFFF);
        btnLogout.setPadding(dp(16), dp(7), dp(16), dp(7));
        btnLogout.setGravity(Gravity.CENTER);
        GradientDrawable lgBg = new GradientDrawable();
        lgBg.setColor(0xFFFFFFFF);
        lgBg.setCornerRadius(dp(16));
        btnLogout.setBackground(lgBg);
        btnLogout.setOnClickListener(v -> { AuthManager.get().clearAuth(); startActivity(new Intent(this, LoginActivity.class)); finish(); });
        topBar.addView(btnLogout);
        root.addView(topBar);

        ScrollView scroll = new ScrollView(this);
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(12), dp(8), dp(12), dp(70));

        TextView title = new TextView(this);
        title.setText("单词管理"); title.setTextSize(19); title.setTextColor(0xFF318af8); title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, dp(15), 0, dp(15));
        main.addView(title);

        // Search area
        LinearLayout searchArea = new LinearLayout(this);
        searchArea.setOrientation(LinearLayout.VERTICAL);
        searchArea.setPadding(0, 0, 0, dp(12));

        LinearLayout row1 = new LinearLayout(this); row1.setOrientation(LinearLayout.HORIZONTAL); row1.setPadding(0, 0, 0, dp(8));
        etSearch = new EditText(this);
        etSearch.setHint("英文拼写/单词ID"); etSearch.setTextSize(14); etSearch.setSingleLine(true);
        etSearch.setPadding(dp(10), 0, dp(10), 0);
        GradientDrawable edBg = new GradientDrawable(); edBg.setColor(0xFFf6f8fc); edBg.setCornerRadius(dp(7)); edBg.setStroke(1, 0xFFc7d9ee);
        etSearch.setBackground(edBg);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { filterWords(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        etSearch.setLayoutParams(new LinearLayout.LayoutParams(0, dp(42), 1));
        row1.addView(etSearch);

        Button searchBtn = new Button(this);
        searchBtn.setText("查询"); searchBtn.setTextColor(0xFFFFFFFF); searchBtn.setTextSize(13);
        searchBtn.setPadding(dp(8), 0, dp(8), 0); searchBtn.setMinWidth(0); searchBtn.setMinimumWidth(0);
        GradientDrawable sbBg = new GradientDrawable(); sbBg.setColor(0xFF318af8); sbBg.setCornerRadius(dp(8));
        searchBtn.setBackground(sbBg);
        LinearLayout.LayoutParams sbp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(42));
        sbp.setMargins(dp(8), 0, 0, 0); searchBtn.setLayoutParams(sbp);
        searchBtn.setOnClickListener(v -> filterWords());
        row1.addView(searchBtn);

        LinearLayout row2 = new LinearLayout(this); row2.setOrientation(LinearLayout.HORIZONTAL);
        spBook = new Spinner(this);
        spBook.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"所属词书"}));
        styleSpinner(spBook);
        spBook.setLayoutParams(new LinearLayout.LayoutParams(0, dp(42), 1));
        spBook.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) { filterWords(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        row2.addView(spBook);
        View spacer = new View(this); spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1)); row2.addView(spacer);

        searchArea.addView(row1); searchArea.addView(row2);
        main.addView(searchArea);

        // Buttons
        LinearLayout btnGroup = new LinearLayout(this); btnGroup.setOrientation(LinearLayout.HORIZONTAL); btnGroup.setPadding(0, 0, 0, dp(11));
        Button addBtn = new Button(this); addBtn.setText("新增单词"); addBtn.setTextColor(0xFFFFFFFF); addBtn.setTextSize(14);
        addBtn.setPadding(0, dp(10), 0, dp(10));
        GradientDrawable abBg = new GradientDrawable(); abBg.setColor(0xFF318af8); abBg.setCornerRadius(dp(8));
        addBtn.setBackground(abBg);
        addBtn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1) {{ setMargins(0, 0, dp(8), 0); }});
        addBtn.setOnClickListener(v -> startActivity(new Intent(this, AddEditWordActivity.class)));
        btnGroup.addView(addBtn);

        Button batchBtn = new Button(this); batchBtn.setText("批量导入"); batchBtn.setTextColor(0xFFFFFFFF); batchBtn.setTextSize(14);
        batchBtn.setPadding(0, dp(10), 0, dp(10));
        GradientDrawable bbBg = new GradientDrawable(); bbBg.setColor(0xFF318af8); bbBg.setCornerRadius(dp(8));
        batchBtn.setBackground(bbBg);
        batchBtn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1) {{ setMargins(dp(8), 0, 0, 0); }});
        batchBtn.setOnClickListener(v -> startActivity(new Intent(this, BatchAddWordActivity.class)));
        btnGroup.addView(batchBtn);
        main.addView(btnGroup);

        wordListContainer = new LinearLayout(this); wordListContainer.setOrientation(LinearLayout.VERTICAL);
        main.addView(wordListContainer);
        scroll.addView(main);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        // Admin navbar
        LinearLayout navbar = new LinearLayout(this);
        navbar.setOrientation(LinearLayout.HORIZONTAL); navbar.setBackgroundColor(0xFFFFFFFF);
        navbar.setPadding(0, dp(8), 0, dp(12)); navbar.setElevation(dp(8));
        navbar.setWeightSum(5f);
        navbar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(61)));
        navbar.setGravity(Gravity.CENTER);
        String[] tabs = {"主页", "用户管理", "词书管理", "单词管理", "个人信息"};
        Class<?>[] targets = {AdminMainActivity.class, UserManageActivity.class, WordBooksActivity.class, WordSearchActivity.class, AdminProfileActivity.class};
        for (int i = 0; i < 5; i++) {
            TextView tv = new TextView(this);
            tv.setText(tabs[i]); tv.setTextSize(13);
            tv.setTextColor(i == 3 ? 0xFF17c2ae : 0xFF8899aa);
            tv.setTypeface(null, i == 3 ? Typeface.BOLD : Typeface.NORMAL);
            tv.setGravity(Gravity.CENTER);
            tv.setMaxLines(1);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            int idx = i; tv.setOnClickListener(v -> startActivity(new Intent(this, targets[idx])));
            navbar.addView(tv);
        }
        root.addView(navbar);
        setContentView(root);
        loadWords();
    }

    private void styleSpinner(Spinner sp) {
        sp.setPadding(dp(8), 0, dp(4), 0);
        GradientDrawable bg = new GradientDrawable(); bg.setColor(0xFFf6f8fc); bg.setCornerRadius(dp(7)); bg.setStroke(1, 0xFFc7d9ee);
        sp.setBackground(bg);
    }

    private void setupBottomNavigation() {
        TextView navHome = findViewById(R.id.nav_home);
        TextView navBooks = findViewById(R.id.nav_books);
        TextView navSearch = findViewById(R.id.nav_search);
        TextView navCollection = findViewById(R.id.nav_collection);
        TextView navRecords = findViewById(R.id.nav_records);
        TextView navProfile = findViewById(R.id.nav_profile);
        View.OnClickListener navListener = v -> {
            int id = v.getId();
            if (id == R.id.nav_home) startActivity(new Intent(this, MainActivity.class));
            else if (id == R.id.nav_books) startActivity(new Intent(this, WordBooksActivity.class));
            else if (id == R.id.nav_collection) startActivity(new Intent(this, CollectionsActivity.class));
            else if (id == R.id.nav_records) startActivity(new Intent(this, StudyRecordsActivity.class));
            else if (id == R.id.nav_profile) startActivity(new Intent(this, ProfileActivity.class));
        };
        navHome.setOnClickListener(navListener); navBooks.setOnClickListener(navListener);
        navCollection.setOnClickListener(navListener); navRecords.setOnClickListener(navListener);
        navProfile.setOnClickListener(navListener);
    }

    private void loadWords() {
        new Thread(() -> {
            try {
                JsonObject r = ApiClient.get().get("/api/words/all");
                if (r.get("code").getAsInt() == 200) { allWords = r.getAsJsonArray("data"); runOnUiThread(() -> filterWords()); }
                if (isAdmin) {
                    JsonObject rb = ApiClient.get().get("/api/admin/wordbooks");
                    if (rb.get("code").getAsInt() == 200) {
                        JsonArray books = rb.getAsJsonArray("data");
                        for (int i = 0; i < books.size(); i++) {
                            JsonObject b = books.get(i).getAsJsonObject();
                            bookIdToName.put(b.get("wordBookId").getAsString(), b.get("wordBookName").getAsString());
                        }
                        runOnUiThread(() -> {
                            java.util.List<String> items = new java.util.ArrayList<>(); items.add("所属词书"); items.add("全部");
                            for (String n : bookIdToName.values()) items.add(n);
                            spBook.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items.toArray(new String[0])));
                        });
                    }
                }
            } catch (Exception e) {}
        }).start();
    }

    @Override
    protected void onResume() { super.onResume(); loadWords(); }

    private void filterWords() {
        String kw = etSearch.getText().toString().trim().toLowerCase();
        wordListContainer.removeAllViews();
        if (allWords == null) return;

        for (int i = 0; i < allWords.size(); i++) {
            JsonObject w = allWords.get(i).getAsJsonObject();
            String spelling = w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "";
            String definition = w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "";
            String wordId = w.has("wordId") ? w.get("wordId").getAsString() : "";
            if (!kw.isEmpty() && !spelling.toLowerCase().contains(kw) && !definition.toLowerCase().contains(kw)) continue;

            if (isAdmin) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL); row.setPadding(dp(10), dp(5), dp(10), dp(5));
                row.setGravity(Gravity.CENTER_VERTICAL);
                GradientDrawable rBg = new GradientDrawable(); rBg.setColor(0xFFFFFFFF); rBg.setCornerRadius(dp(6));
                row.setBackground(rBg);
                row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{ setMargins(0, 0, 0, dp(2)); }});

                String pos = w.has("partOfSpeech") && !w.get("partOfSpeech").isJsonNull() ? w.get("partOfSpeech").getAsString() : "";
                String display = spelling + (pos.isEmpty() ? "" : " " + pos) + "  " + definition;

                TextView spTv = new TextView(this);
                spTv.setText(display); spTv.setTextSize(13); spTv.setTextColor(0xFF318af8);
                spTv.setTypeface(null, Typeface.BOLD);
                spTv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                row.addView(spTv);
                String fId = wordId, fSp = spelling;
                row.addView(tinyBtn("编辑", 0xFF318af8, 0xFFFFFFFF, v -> { Intent in = new Intent(this, AddEditWordActivity.class); in.putExtra("wordId", fId); startActivity(in); }));
                row.addView(tinyBtn("删除", 0xFFe8ecf1, 0xFF5a6b80, v -> new AlertDialog.Builder(this).setTitle("确认删除").setMessage("确定删除\"" + fSp + "\"？")
                        .setPositiveButton("确定", (d, w2) -> new Thread(() -> { try { ApiClient.get().delete("/api/words/" + fId, null); runOnUiThread(() -> loadWords()); } catch (Exception e) {} }).start()).setNegativeButton("取消", null).show()));
                row.addView(tinyBtn("详情", 0xFF318af8, 0xFFFFFFFF, v -> { Intent in = new Intent(this, WordDetailActivity.class); in.putExtra("wordId", fId); startActivity(in); }));
                wordListContainer.addView(row);
            } else {
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.HORIZONTAL); card.setPadding(dp(16), dp(16), dp(16), dp(16));
                card.setBackground(getDrawable(R.drawable.bg_white_card)); card.setElevation(dp(2)); card.setGravity(Gravity.CENTER_VERTICAL);
                card.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{ setMargins(0, 0, 0, dp(12)); }});
                LinearLayout left = new LinearLayout(this); left.setOrientation(LinearLayout.VERTICAL);
                left.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                TextView ws = new TextView(this); ws.setText(spelling); ws.setTextSize(18); ws.setTextColor(0xFF318af8); ws.setTypeface(null, Typeface.BOLD); ws.setPadding(0, 0, 0, dp(8)); left.addView(ws);
                TextView wd = new TextView(this); wd.setText(definition); wd.setTextSize(14); wd.setTextColor(0xFF333333); left.addView(wd);
                card.addView(left);
                Button detailBtn = new Button(this); detailBtn.setText("查看详情"); detailBtn.setTextSize(14); detailBtn.setTextColor(0xFFFFFFFF);
                detailBtn.setPadding(dp(20), dp(8), dp(20), dp(8)); detailBtn.setMinHeight(0); detailBtn.setMinimumHeight(0);
                detailBtn.setBackground(getDrawable(R.drawable.bg_btn_primary));
                detailBtn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{ setMargins(dp(16), 0, 0, 0); }});
                String fId = wordId;
                detailBtn.setOnClickListener(v -> { Intent in = new Intent(this, WordDetailActivity.class); in.putExtra("wordId", fId); startActivity(in); });
                card.addView(detailBtn);
                wordListContainer.addView(card);
            }
        }
        if (wordListContainer.getChildCount() == 0) {
            TextView emp = new TextView(this); emp.setText(kw.isEmpty() ? "输入关键词搜索" : "未找到相关单词");
            emp.setTextSize(16); emp.setTextColor(0xFF999999); emp.setGravity(Gravity.CENTER); emp.setPadding(0, dp(40), 0, dp(40));
            wordListContainer.addView(emp);
        }
    }

    private Button tinyBtn(String text, int bg, int fg, View.OnClickListener listener) {
        Button b = new Button(this); b.setText(text); b.setTextColor(fg); b.setTextSize(11);
        b.setPadding(dp(6), dp(2), dp(6), dp(2)); b.setMinWidth(0); b.setMinHeight(0); b.setMinimumWidth(0); b.setMinimumHeight(0);
        GradientDrawable g = new GradientDrawable(); g.setColor(bg); g.setCornerRadius(dp(4)); b.setBackground(g);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.setMargins(dp(4), 0, 0, 0); b.setLayoutParams(bp); b.setOnClickListener(listener);
        return b;
    }

    private int dp(int val) { return (int)(val * getResources().getDisplayMetrics().density + 0.5f); }
}
