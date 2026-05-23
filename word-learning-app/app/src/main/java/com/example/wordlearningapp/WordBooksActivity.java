package com.example.wordlearningapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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
    private boolean isAdmin;
    private JsonArray allBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isAdmin = "admin".equals(AuthManager.get().getRole());

        if (isAdmin) {
            buildAdminUI();
        } else {
            setContentView(R.layout.activity_word_books);
            bookListContainer = findViewById(R.id.book_list_container);
            etSearchBook = findViewById(R.id.et_search_book);
            spinnerDifficulty = findViewById(R.id.spinner_difficulty);

            // 设置难度筛选下拉框的适配器
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    new String[]{"难度等级", "全部", "初级", "中级", "高级"}
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDifficulty.setAdapter(adapter);

            findViewById(R.id.btn_search_book).setOnClickListener(v -> filterBooks());
            spinnerDifficulty.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) { filterBooks(); }
                @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
            });
            findViewById(R.id.btn_my_books).setOnClickListener(v -> startActivity(new Intent(this, MyBooksActivity.class)));
            setupBottomNavigation();
            loadBooks();
        }
    }


    private void buildAdminUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFf2f8fc);

        // Top bar
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setPadding(dp(12), 0, dp(16), 0);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        GradientDrawable tbBg = new GradientDrawable();
        tbBg.setColor(0xFF318af8);
        tbBg.setCornerRadii(new float[]{0, 0, 0, 0, dp(18), dp(18), dp(18), dp(18)});
        topBar.setBackground(tbBg);
        TextView adminUser = new TextView(this);
        adminUser.setText("管理员：" + AuthManager.get().getUserId());
        adminUser.setTextSize(16); adminUser.setTextColor(0xFFFFFFFF); adminUser.setTypeface(null, Typeface.BOLD);
        adminUser.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        topBar.addView(adminUser);
        TextView btnLogout = new TextView(this);
        btnLogout.setText("退出登录"); btnLogout.setTextSize(14); btnLogout.setTextColor(0xFF318af8); btnLogout.setGravity(Gravity.CENTER);
        btnLogout.setPadding(dp(16), dp(6), dp(16), dp(6));
        GradientDrawable lgBg = new GradientDrawable(); lgBg.setColor(0xFFFFFFFF); lgBg.setCornerRadius(dp(14));
        btnLogout.setBackground(lgBg);
        btnLogout.setOnClickListener(v -> { AuthManager.get().clearAuth(); startActivity(new Intent(this, LoginActivity.class)); finish(); });
        topBar.addView(btnLogout);
        root.addView(topBar);

        ScrollView scroll = new ScrollView(this);
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(12), dp(8), dp(12), dp(70));

        TextView title = new TextView(this);
        title.setText("词书管理"); title.setTextSize(19); title.setTextColor(0xFF318af8); title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, dp(15), 0, dp(15));
        main.addView(title);

        // Search area
        LinearLayout searchArea = new LinearLayout(this);
        searchArea.setOrientation(LinearLayout.VERTICAL);
        searchArea.setPadding(0, 0, 0, dp(14));

        LinearLayout row1 = new LinearLayout(this); row1.setOrientation(LinearLayout.HORIZONTAL); row1.setPadding(0, 0, 0, dp(8));
        etSearchBook = new EditText(this);
        etSearchBook.setHint("词书名称 / 词书ID"); etSearchBook.setTextSize(14); etSearchBook.setSingleLine(true);
        etSearchBook.setPadding(dp(10), 0, dp(10), 0);
        GradientDrawable edBg = new GradientDrawable(); edBg.setColor(0xFFf6f8fc); edBg.setCornerRadius(dp(7)); edBg.setStroke(1, 0xFFc7d9ee);
        etSearchBook.setBackground(edBg);
        etSearchBook.setLayoutParams(new LinearLayout.LayoutParams(0, dp(42), 1));
        row1.addView(etSearchBook);

        Button searchBtn = new Button(this);
        searchBtn.setText("查询"); searchBtn.setTextColor(0xFFFFFFFF); searchBtn.setTextSize(13);
        searchBtn.setPadding(dp(8), 0, dp(8), 0); searchBtn.setMinWidth(0); searchBtn.setMinimumWidth(0);
        GradientDrawable sbBg = new GradientDrawable(); sbBg.setColor(0xFF318af8); sbBg.setCornerRadius(dp(8));
        searchBtn.setBackground(sbBg);
        LinearLayout.LayoutParams sbp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(42));
        sbp.setMargins(dp(8), 0, 0, 0); searchBtn.setLayoutParams(sbp);
        searchBtn.setOnClickListener(v -> filterBooks());
        row1.addView(searchBtn);

        LinearLayout row2 = new LinearLayout(this); row2.setOrientation(LinearLayout.HORIZONTAL);
        spinnerDifficulty = new Spinner(this);
        spinnerDifficulty.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"难度等级", "全部", "初级", "中级", "高级"}));
        styleSpinner(spinnerDifficulty);
        spinnerDifficulty.setLayoutParams(new LinearLayout.LayoutParams(0, dp(42), 1));
        spinnerDifficulty.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) { filterBooks(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        row2.addView(spinnerDifficulty);
        View spacer = new View(this); spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1)); row2.addView(spacer);

        searchArea.addView(row1); searchArea.addView(row2);
        main.addView(searchArea);

        // Add book button
        Button addBtn = new Button(this);
        addBtn.setText("新增词书"); addBtn.setTextColor(0xFFFFFFFF); addBtn.setTextSize(15);
        addBtn.setPadding(0, dp(11), 0, dp(11));
        GradientDrawable abBg = new GradientDrawable(); abBg.setColor(0xFF318af8); abBg.setCornerRadius(dp(8));
        addBtn.setBackground(abBg);
        addBtn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ((LinearLayout.LayoutParams)addBtn.getLayoutParams()).setMargins(0, 0, 0, dp(11));
        addBtn.setOnClickListener(v -> startActivity(new Intent(this, AddEditBookActivity.class)));
        main.addView(addBtn);

        bookListContainer = new LinearLayout(this); bookListContainer.setOrientation(LinearLayout.VERTICAL);
        main.addView(bookListContainer);
        scroll.addView(main);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        // Admin bottom nav
        LinearLayout navbar = new LinearLayout(this);
        navbar.setOrientation(LinearLayout.HORIZONTAL); navbar.setBackgroundColor(0xFFFFFFFF);
        navbar.setPadding(0, dp(8), 0, dp(12)); navbar.setElevation(dp(8));
        navbar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(61)));
        navbar.setGravity(Gravity.CENTER);
        String[][] tabs = {{"主页", "用户管理", "词书管理", "单词管理"}};
        Class<?>[] targets = {AdminMainActivity.class, UserManageActivity.class, WordBooksActivity.class, WordSearchActivity.class};
        for (int i = 0; i < 4; i++) {
            TextView tv = new TextView(this);
            tv.setText(tabs[0][i]); tv.setTextSize(15);
            tv.setTextColor(i == 2 ? 0xFF17c2ae : 0xFF8899aa);
            tv.setTypeface(null, i == 2 ? Typeface.BOLD : Typeface.NORMAL);
            tv.setGravity(Gravity.CENTER);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            int idx = i; tv.setOnClickListener(v -> startActivity(new Intent(this, targets[idx])));
            navbar.addView(tv);
        }
        root.addView(navbar);

        setContentView(root);
        loadBooks();
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
        View.OnClickListener navClickListener = v -> {
            int id = v.getId();
            if (id == R.id.nav_home) startActivity(new Intent(this, MainActivity.class));
            else if (id == R.id.nav_search) startActivity(new Intent(this, WordSearchActivity.class));
            else if (id == R.id.nav_collection) startActivity(new Intent(this, CollectionsActivity.class));
            else if (id == R.id.nav_records) startActivity(new Intent(this, StudyRecordsActivity.class));
            else if (id == R.id.nav_profile) startActivity(new Intent(this, ProfileActivity.class));
        };
        navHome.setOnClickListener(navClickListener); navSearch.setOnClickListener(navClickListener);
        navCollection.setOnClickListener(navClickListener); navRecords.setOnClickListener(navClickListener);
        navProfile.setOnClickListener(navClickListener);
    }

    private void loadBooks() {
        new Thread(() -> {
            try {
                String url = isAdmin ? "/api/admin/wordbooks" : "/api/wordbooks";
                JsonObject r = ApiClient.get().get(url);
                if (r.get("code").getAsInt() == 200) {
                    allBooks = r.getAsJsonArray("data");
                    runOnUiThread(() -> filterBooks());
                }
            } catch (Exception e) {}
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBooks();
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
            String status = b.has("wordBookStatus") ? b.get("wordBookStatus").getAsString() : "已上架";
            int wc = b.has("wordCount") ? b.get("wordCount").getAsInt() : 0;
            String bid = b.has("wordBookId") ? b.get("wordBookId").getAsString() : "";
            String cTime = b.has("createTime") ? b.get("createTime").getAsString().substring(0, 10) : "";
            String uTime = b.has("updateTime") && !b.get("updateTime").isJsonNull() ? b.get("updateTime").getAsString().substring(0, 10) : "";

            if (!kw.isEmpty() && !name.toLowerCase().contains(kw) && !bid.toLowerCase().contains(kw)) continue;
            if (!"难度等级".equals(diff) && !"全部".equals(diff) && !diff.equals(bookDiff)) continue;

            if (isAdmin) {
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(10), dp(10), dp(10), dp(10));
                GradientDrawable cBg = new GradientDrawable(); cBg.setColor(0xFFFFFFFF); cBg.setCornerRadius(dp(12));
                card.setBackground(cBg); card.setElevation(dp(2));
                LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                cp.setMargins(0, 0, 0, dp(10)); card.setLayoutParams(cp);

                addRow(card, "ID: " + bid + " | 名称: " + name + " | 难度: " + bookDiff, 12, 0xFF4a5568);
                addRow(card, "词数: " + wc + " | 创建: " + cTime + " | 更新: " + uTime, 12, 0xFF4a5568);
                TextView stv = new TextView(this);
                stv.setText("状态: " + status); stv.setTextSize(12);
                stv.setTextColor("未上架".equals(status) ? 0xFFe37b4b : 0xFF25cb75);
                stv.setPadding(0, 0, 0, dp(4)); card.addView(stv);

                LinearLayout btns = new LinearLayout(this); btns.setOrientation(LinearLayout.HORIZONTAL);
                btns.addView(cardBtn("编辑", 0xFF318af8, 0xFFFFFFFF, v -> { Intent in = new Intent(this, AddEditBookActivity.class); in.putExtra("bookId", bid); startActivity(in); }));
                btns.addView(cardBtn("删除", 0xFFe8ecf1, 0xFF5a6b80, v -> new AlertDialog.Builder(this).setTitle("确认删除").setMessage("确定删除\"" + name + "\"？")
                        .setPositiveButton("确定", (d, w) -> new Thread(() -> { try { ApiClient.get().delete("/api/wordbooks/" + bid, null); runOnUiThread(() -> loadBooks()); } catch (Exception e) {} }).start()).setNegativeButton("取消", null).show()));
                btns.addView(cardBtn("添加单词", 0xFF318af8, 0xFFFFFFFF, v -> { Intent in = new Intent(this, AddEditWordActivity.class); in.putExtra("bookId", bid); startActivity(in); }));
                btns.addView(cardBtn("查看单词", 0xFF318af8, 0xFFFFFFFF, v -> { Intent in = new Intent(this, ViewBookWordsActivity.class); in.putExtra("bookId", bid); in.putExtra("bookName", name); startActivity(in); }));
                card.addView(btns);
                bookListContainer.addView(card);
            } else {
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.HORIZONTAL); card.setPadding(dp(16), dp(16), dp(16), dp(16));
                card.setBackground(getDrawable(R.drawable.bg_white_card)); card.setElevation(dp(2)); card.setGravity(Gravity.CENTER_VERTICAL);
                card.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{ setMargins(0, 0, 0, dp(12)); }});
                LinearLayout leftInfo = new LinearLayout(this); leftInfo.setOrientation(LinearLayout.VERTICAL);
                leftInfo.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                TextView bnm = new TextView(this); bnm.setText(name); bnm.setTextSize(18); bnm.setTextColor(0xFF318af8); bnm.setTypeface(null, Typeface.BOLD); bnm.setPadding(0, 0, 0, dp(8)); leftInfo.addView(bnm);
                TextView binfo = new TextView(this); binfo.setText(bookDiff + " | " + wc + "词"); binfo.setTextSize(14); binfo.setTextColor(0xFF666666); binfo.setPadding(0, 0, 0, dp(8)); leftInfo.addView(binfo);
                TextView btime = new TextView(this); btime.setText("创建时间: " + cTime); btime.setTextSize(14); btime.setTextColor(0xFF666666); leftInfo.addView(btime);
                card.addView(leftInfo);
                Button studyBtn = new Button(this); studyBtn.setText("开始学习"); studyBtn.setTextSize(14); studyBtn.setTextColor(0xFFFFFFFF); studyBtn.setPadding(dp(20), dp(8), dp(20), dp(8));
                studyBtn.setMinHeight(0); studyBtn.setMinimumHeight(0); studyBtn.setBackground(getDrawable(R.drawable.bg_btn_primary));
                studyBtn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{ setMargins(dp(16), 0, 0, 0); }});
                studyBtn.setOnClickListener(v -> { Intent in = new Intent(this, BookDetailActivity.class); in.putExtra("bookId", bid); startActivity(in); });
                card.addView(studyBtn);
                bookListContainer.addView(card);
            }
        }
        if (bookListContainer.getChildCount() == 0) {
            TextView noR = new TextView(this); noR.setText("没有找到匹配的词书"); noR.setTextSize(16); noR.setTextColor(0xFF999999);
            noR.setGravity(Gravity.CENTER); noR.setPadding(0, dp(40), 0, dp(40)); bookListContainer.addView(noR);
        }
    }

    private void addRow(LinearLayout parent, String text, int size, int color) {
        TextView tv = new TextView(this); tv.setText(text); tv.setTextSize(size); tv.setTextColor(color); tv.setPadding(0, 0, 0, dp(3)); parent.addView(tv);
    }

    private Button cardBtn(String text, int bg, int fg, View.OnClickListener listener) {
        Button b = new Button(this); b.setText(text); b.setTextColor(fg); b.setTextSize(11);
        b.setPadding(dp(6), dp(3), dp(6), dp(3)); b.setMinWidth(0); b.setMinHeight(0); b.setMinimumWidth(0); b.setMinimumHeight(0);
        GradientDrawable g = new GradientDrawable(); g.setColor(bg); g.setCornerRadius(dp(5)); b.setBackground(g);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0, 0, dp(4), 0); b.setLayoutParams(bp); b.setOnClickListener(listener);
        return b;
    }

    private int dp(int val) { return (int)(val * getResources().getDisplayMetrics().density + 0.5f); }
}
