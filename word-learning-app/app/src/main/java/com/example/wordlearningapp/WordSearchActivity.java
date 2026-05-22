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

    private LinearLayout listArea;
    private EditText etSearch;
    private Spinner spBook, spStatus;
    private boolean isAdmin;
    private JsonArray allWords;
    private Map<String, String> bookIdToName = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isAdmin = "admin".equals(AuthManager.get().getRole());

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFf2f8fc);

        // ===== TOP BAR: 48dp, #318af8 =====
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setPadding(dp(12), 0, dp(16), 0);
        GradientDrawable tbBg = new GradientDrawable();
        tbBg.setColor(0xFF318af8);
        tbBg.setCornerRadii(new float[]{0, 0, 0, 0, dp(18), dp(18), dp(18), dp(18)});
        topBar.setBackground(tbBg);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));

        if (isAdmin) {
            TextView adminUser = new TextView(this);
            adminUser.setText("管理员：" + AuthManager.get().getUserId());
            adminUser.setTextSize(16);
            adminUser.setTextColor(0xFFFFFFFF);
            adminUser.setTypeface(null, Typeface.BOLD);
            adminUser.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            topBar.addView(adminUser);

            TextView btnLogout = new TextView(this);
            btnLogout.setText("退出登录");
            btnLogout.setTextSize(14);
            btnLogout.setTextColor(0xFF318af8);
            btnLogout.setGravity(Gravity.CENTER);
            btnLogout.setPadding(dp(16), dp(6), dp(16), dp(6));
            GradientDrawable lgBg = new GradientDrawable();
            lgBg.setColor(0xFFFFFFFF);
            lgBg.setCornerRadius(dp(14));
            btnLogout.setBackground(lgBg);
            btnLogout.setOnClickListener(v -> {
                AuthManager.get().clearAuth();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
            topBar.addView(btnLogout);
        } else {
            TextView btnBack = new TextView(this);
            btnBack.setText("←");
            btnBack.setTextSize(22);
            btnBack.setTextColor(0xFFFFFFFF);
            btnBack.setTypeface(null, Typeface.BOLD);
            btnBack.setPadding(0, 0, dp(12), 0);
            btnBack.setOnClickListener(v -> finish());
            topBar.addView(btnBack);

            TextView userTitle = new TextView(this);
            userTitle.setText("单词查询");
            userTitle.setTextSize(18);
            userTitle.setTextColor(0xFFFFFFFF);
            userTitle.setTypeface(null, Typeface.BOLD);
            userTitle.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            topBar.addView(userTitle);

            TextView place = new TextView(this);
            place.setLayoutParams(new LinearLayout.LayoutParams(dp(48), 1));
            topBar.addView(place);
        }
        root.addView(topBar);

        // ===== SCROLLABLE MAIN =====
        ScrollView scroll = new ScrollView(this);
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(12), dp(8), dp(12), isAdmin ? dp(70) : dp(16));

        // Title
        TextView title = new TextView(this);
        title.setText(isAdmin ? "单词管理" : "单词查询");
        title.setTextSize(19);
        title.setTextColor(0xFF318af8);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, dp(15), 0, dp(15));
        main.addView(title);

        // Search area: two rows
        LinearLayout searchArea = new LinearLayout(this);
        searchArea.setOrientation(LinearLayout.VERTICAL);
        searchArea.setPadding(0, 0, 0, dp(12));

        // Row 1: input + search button
        LinearLayout searchRow1 = new LinearLayout(this);
        searchRow1.setOrientation(LinearLayout.HORIZONTAL);
        searchRow1.setPadding(0, 0, 0, dp(8));

        etSearch = new EditText(this);
        etSearch.setHint("英文拼写/单词ID");
        etSearch.setTextSize(14);
        etSearch.setSingleLine(true);
        etSearch.setPadding(dp(10), 0, dp(10), 0);
        bg(etSearch, 0xFFf6f8fc, dp(7), 1, 0xFFc7d9ee);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { filterWords(); }
            @Override public void afterTextChanged(android.text.Editable e) {}
        });
        LinearLayout.LayoutParams edp = new LinearLayout.LayoutParams(0, dp(42), 1);
        edp.gravity = Gravity.CENTER_VERTICAL;
        etSearch.setLayoutParams(edp);
        searchRow1.addView(etSearch);

        Button searchBtn = new Button(this);
        searchBtn.setText("查询");
        searchBtn.setTextColor(0xFFFFFFFF);
        searchBtn.setTextSize(13);
        searchBtn.setPadding(dp(8), 0, dp(8), 0);
        searchBtn.setMinWidth(0);
        searchBtn.setMinimumWidth(0);
        searchBtn.setGravity(Gravity.CENTER);
        GradientDrawable sbtnBg = new GradientDrawable();
        sbtnBg.setColor(0xFF318af8);
        sbtnBg.setCornerRadius(dp(8));
        searchBtn.setBackground(sbtnBg);
        LinearLayout.LayoutParams sbtnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(42));
        sbtnLp.setMargins(dp(8), 0, 0, 0);
        searchBtn.setLayoutParams(sbtnLp);
        searchBtn.setOnClickListener(v -> filterWords());
        searchRow1.addView(searchBtn);

        // Row 2: book spinner + status spinner
        LinearLayout searchRow2 = new LinearLayout(this);
        searchRow2.setOrientation(LinearLayout.HORIZONTAL);

        spBook = new Spinner(this);
        spBook.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"所属词书"}));
        styleSpinner(spBook);
        LinearLayout.LayoutParams bkp = new LinearLayout.LayoutParams(0, dp(42), 1);
        bkp.gravity = Gravity.CENTER_VERTICAL;
        spBook.setLayoutParams(bkp);
        spBook.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) { filterWords(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        searchRow2.addView(spBook);

        spStatus = new Spinner(this);
        spStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"单词状态", "全部", "已上架", "未上架"}));
        styleSpinner(spStatus);
        LinearLayout.LayoutParams stp = new LinearLayout.LayoutParams(0, dp(42), 1);
        stp.setMargins(dp(8), 0, 0, 0);
        stp.gravity = Gravity.CENTER_VERTICAL;
        spStatus.setLayoutParams(stp);
        spStatus.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) { filterWords(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        searchRow2.addView(spStatus);

        searchArea.addView(searchRow1);
        searchArea.addView(searchRow2);
        main.addView(searchArea);

        // Two buttons: 新增单词 + 批量导入
        if (isAdmin) {
            LinearLayout btnGroup = new LinearLayout(this);
            btnGroup.setOrientation(LinearLayout.HORIZONTAL);
            btnGroup.setPadding(0, 0, 0, dp(11));

            Button addBtn = new Button(this);
            addBtn.setText("新增单词");
            addBtn.setTextColor(0xFFFFFFFF);
            addBtn.setTextSize(14);
            addBtn.setPadding(0, dp(10), 0, dp(10));
            GradientDrawable abBg = new GradientDrawable();
            abBg.setColor(0xFF318af8);
            abBg.setCornerRadius(dp(8));
            addBtn.setBackground(abBg);
            LinearLayout.LayoutParams abp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            abp.setMargins(0, 0, dp(8), 0);
            addBtn.setLayoutParams(abp);
            addBtn.setOnClickListener(v -> startActivity(new Intent(this, AddEditWordActivity.class)));
            btnGroup.addView(addBtn);

            Button batchBtn = new Button(this);
            batchBtn.setText("批量导入");
            batchBtn.setTextColor(0xFFFFFFFF);
            batchBtn.setTextSize(14);
            batchBtn.setPadding(0, dp(10), 0, dp(10));
            GradientDrawable bbBg = new GradientDrawable();
            bbBg.setColor(0xFF318af8);
            bbBg.setCornerRadius(dp(8));
            batchBtn.setBackground(bbBg);
            LinearLayout.LayoutParams bbp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            bbp.setMargins(dp(8), 0, 0, 0);
            batchBtn.setLayoutParams(bbp);
            batchBtn.setOnClickListener(v -> startActivity(new Intent(this, BatchAddWordActivity.class)));
            btnGroup.addView(batchBtn);

            main.addView(btnGroup);
        }

        listArea = new LinearLayout(this);
        listArea.setOrientation(LinearLayout.VERTICAL);
        main.addView(listArea);

        scroll.addView(main);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        // ===== BOTTOM NAVBAR (admin only) =====
        if (isAdmin) {
            root.addView(makeNavbar());
        }

        setContentView(root);
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void styleSpinner(Spinner sp) {
        sp.setPadding(dp(6), 0, dp(2), 0);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFFf6f8fc);
        bg.setCornerRadius(dp(7));
        bg.setStroke(1, 0xFFc7d9ee);
        sp.setBackground(bg);
    }

    private LinearLayout makeNavbar() {
        LinearLayout navbar = new LinearLayout(this);
        navbar.setOrientation(LinearLayout.HORIZONTAL);
        navbar.setBackgroundColor(0xFFFFFFFF);
        navbar.setPadding(0, dp(8), 0, dp(12));
        navbar.setElevation(dp(8));
        navbar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(61)));
        navbar.setGravity(Gravity.CENTER);
        navItem(navbar, "", "主页", false, () -> startActivity(new Intent(this, AdminMainActivity.class)));
        navItem(navbar, "", "用户管理", false, () -> startActivity(new Intent(this, UserManageActivity.class)));
        navItem(navbar, "", "词书管理", false, () -> startActivity(new Intent(this, WordBooksActivity.class)));
        navItem(navbar, "", "单词管理", true, () -> {});
        return navbar;
    }

    private void navItem(LinearLayout parent, String icon, String label, boolean active, Runnable action) {
        TextView item = new TextView(this);
        item.setText(icon.isEmpty() ? label : icon + "\n" + label);
        item.setTextSize(15);
        item.setTextColor(active ? 0xFF17c2ae : 0xFF8899aa);
        item.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
        item.setGravity(Gravity.CENTER);
        item.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        item.setOnClickListener(v -> action.run());
        parent.addView(item);
    }

    private void bg(View v, int color, int radius, int borderW, int borderC) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radius);
        if (borderW > 0) g.setStroke(borderW, borderC);
        v.setBackground(g);
    }

    private void loadData() {
        new Thread(() -> {
            try {
                // Load books for name mapping and spinner
                JsonObject rb = ApiClient.get().get("/api/wordbooks");
                if (rb.get("code").getAsInt() == 200) {
                    JsonArray books = rb.getAsJsonArray("data");
                    java.util.List<String> bookItems = new java.util.ArrayList<>();
                    bookItems.add("所属词书");
                    for (int i = 0; i < books.size(); i++) {
                        JsonObject b = books.get(i).getAsJsonObject();
                        String bid = b.has("wordBookId") ? b.get("wordBookId").getAsString() : "";
                        String bname = b.has("wordBookName") ? b.get("wordBookName").getAsString() : "";
                        if (!bid.isEmpty() && !bname.isEmpty()) {
                            bookIdToName.put(bid, bname);
                            bookItems.add(bname);
                        }
                    }
                    runOnUiThread(() -> spBook.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                            bookItems.toArray(new String[0]))));
                }

                // Load all words
                JsonObject r = ApiClient.get().get("/api/words/all");
                if (r.get("code").getAsInt() == 200) {
                    allWords = r.getAsJsonArray("data");
                    runOnUiThread(() -> filterWords());
                }
            } catch (Exception e) {}
        }).start();
    }

    private void filterWords() {
        String kw = etSearch.getText().toString().trim().toLowerCase();
        String bookName = spBook.getSelectedItem() != null ? spBook.getSelectedItem().toString() : "所属词书";
        String status = spStatus.getSelectedItem() != null ? spStatus.getSelectedItem().toString() : "单词状态";

        listArea.removeAllViews();
        if (allWords == null) return;

        for (int i = 0; i < allWords.size(); i++) {
            JsonObject w = allWords.get(i).getAsJsonObject();
            String spelling = w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "";
            String definition = w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "";
            String wordId = w.has("wordId") ? w.get("wordId").getAsString() : "";
            String phonetic = w.has("phoneticSymbol") && !w.get("phoneticSymbol").isJsonNull() ? w.get("phoneticSymbol").getAsString() : "";
            String pos = w.has("partOfSpeech") && !w.get("partOfSpeech").isJsonNull() ? w.get("partOfSpeech").getAsString() : "";
            String createTime = w.has("createTime") && !w.get("createTime").isJsonNull() ? w.get("createTime").getAsString().substring(0, 10) : "";
            String wBookId = w.has("wordBookId") && !w.get("wordBookId").isJsonNull() ? w.get("wordBookId").getAsString() : "";
            String wBookName = bookIdToName.getOrDefault(wBookId, "");

            if (!kw.isEmpty() && !spelling.toLowerCase().contains(kw) && !wordId.toLowerCase().contains(kw)) continue;
            if (!"所属词书".equals(bookName) && !bookName.isEmpty() && !bookName.equals(wBookName)) continue;
            if (!"单词状态".equals(status) && !"全部".equals(status)) {
                continue; // Words don't have real status yet, skip filter for non-全部
            }

            if (isAdmin) {
                // Compact single-line: spelling+def | edit | delete | detail
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setBackgroundColor(0xFFFFFFFF);
                row.setPadding(dp(10), dp(5), dp(10), dp(5));
                row.setGravity(Gravity.CENTER_VERTICAL);
                GradientDrawable rBg = new GradientDrawable();
                rBg.setColor(0xFFFFFFFF);
                rBg.setCornerRadius(dp(6));
                row.setBackground(rBg);
                LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rp.setMargins(0, 0, 0, dp(2));
                row.setLayoutParams(rp);

                TextView spTv = new TextView(this);
                spTv.setText(spelling + "  " + definition);
                spTv.setTextSize(13);
                spTv.setTextColor(0xFF318af8);
                spTv.setTypeface(null, Typeface.BOLD);
                spTv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                row.addView(spTv);

                String fWordId = wordId, fSpelling = spelling;
                row.addView(tinyBtn("编辑", 0xFF318af8, 0xFFFFFFFF, v -> {
                    Intent in = new Intent(this, AddEditWordActivity.class);
                    in.putExtra("wordId", fWordId);
                    startActivity(in);
                }));
                row.addView(tinyBtn("删除", 0xFFe8ecf1, 0xFF5a6b80, v -> new AlertDialog.Builder(this)
                        .setTitle("确认删除")
                        .setMessage("确定删除\"" + fSpelling + "\"？")
                        .setPositiveButton("确定", (d, w2) -> new Thread(() -> {
                            try { ApiClient.get().delete("/api/words/" + fWordId, null); runOnUiThread(() -> loadData()); } catch (Exception e) {}
                        }).start())
                        .setNegativeButton("取消", null)
                        .show()));
                row.addView(tinyBtn("详情", 0xFF318af8, 0xFFFFFFFF, v -> {
                    Intent in = new Intent(this, WordDetailActivity.class);
                    in.putExtra("wordId", fWordId);
                    startActivity(in);
                }));

                listArea.addView(row);
            }

        }
    }

    private void addCardRow(LinearLayout card, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(12);
        tv.setTextColor(0xFF4a5568);
        tv.setPadding(0, 0, 0, dp(3));
        card.addView(tv);
    }

    private Button tinyBtn(String text, int bg, int fg, android.view.View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(fg);
        b.setTextSize(11);
        b.setPadding(dp(6), dp(2), dp(6), dp(2));
        b.setMinWidth(0); b.setMinHeight(0);
        b.setMinimumWidth(0); b.setMinimumHeight(0);
        GradientDrawable g = new GradientDrawable();
        g.setColor(bg);
        g.setCornerRadius(dp(4));
        b.setBackground(g);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.setMargins(dp(4), 0, 0, 0);
        b.setLayoutParams(bp);
        b.setOnClickListener(listener);
        return b;
    }

    private Button cardBtn(LinearLayout parent, String text, int bgColor, int textColor) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(textColor);
        b.setTextSize(12);
        b.setPadding(dp(8), dp(4), dp(8), dp(4));
        b.setMinWidth(0);
        b.setMinHeight(0);
        b.setMinimumWidth(0);
        b.setMinimumHeight(0);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(bgColor);
        bg.setCornerRadius(dp(5));
        b.setBackground(bg);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0, 0, dp(6), 0);
        b.setLayoutParams(bp);
        return b;
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
