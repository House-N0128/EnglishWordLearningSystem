package com.example.wordlearningapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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

    private LinearLayout listArea;
    private EditText etSearch;
    private Spinner spDifficulty, spStatus;
    private boolean isAdmin;
    private JsonArray allBooks;

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

        TextView btnLogout = new TextView(this);
        btnLogout.setText("退出登录");
        btnLogout.setTextSize(15);
        btnLogout.setTextColor(0xFF318af8);
        btnLogout.setGravity(Gravity.CENTER);
        btnLogout.setPadding(dp(20), dp(7), dp(20), dp(7));
        GradientDrawable lgBg = new GradientDrawable();
        lgBg.setColor(0xFFFFFFFF);
        lgBg.setCornerRadius(dp(16));
        btnLogout.setBackground(lgBg);
        btnLogout.setOnClickListener(v -> { AuthManager.get().clearAuth(); startActivity(new Intent(this, LoginActivity.class)); finish(); });
        topBar.addView(btnLogout);
        root.addView(topBar);

        // ===== SCROLLABLE MAIN =====
        ScrollView scroll = new ScrollView(this);
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(12), dp(8), dp(12), dp(70));

        // Title
        TextView title = new TextView(this);
        title.setText("词书管理");
        title.setTextSize(19);
        title.setTextColor(0xFF318af8);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, dp(15), 0, dp(15));
        main.addView(title);

        // Search row: input + difficulty dropdown + status dropdown + button
        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        searchRow.setPadding(0, 0, 0, dp(14));

        etSearch = new EditText(this);
        etSearch.setHint("词书名称 / 词书ID");
        etSearch.setTextSize(14);
        etSearch.setSingleLine(true);
        etSearch.setPadding(dp(6), 0, dp(6), 0);
        bg(etSearch, 0xFFf6f8fc, dp(7), 1, 0xFFc7d9ee);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { filterBooks(); }
            @Override public void afterTextChanged(android.text.Editable e) {}
        });
        LinearLayout.LayoutParams edp = new LinearLayout.LayoutParams(0, dp(48), 2.5f);
        edp.gravity = Gravity.CENTER_VERTICAL;
        etSearch.setLayoutParams(edp);
        searchRow.addView(etSearch);

        spDifficulty = new Spinner(this);
        spDifficulty.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"难度等级", "全部", "初级", "中级", "高级"}));
        styleSpinner(spDifficulty);
        LinearLayout.LayoutParams dpp = new LinearLayout.LayoutParams(0, dp(48), 1.2f);
        dpp.setMargins(dp(8), 0, 0, 0); dpp.gravity = Gravity.CENTER_VERTICAL;
        spDifficulty.setLayoutParams(dpp);
        spDifficulty.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) { filterBooks(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        searchRow.addView(spDifficulty);

        spStatus = new Spinner(this);
        spStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"词书状态", "全部", "已上架", "未上架"}));
        styleSpinner(spStatus);
        LinearLayout.LayoutParams stp = new LinearLayout.LayoutParams(0, dp(48), 1.2f);
        stp.setMargins(dp(8), 0, 0, 0); stp.gravity = Gravity.CENTER_VERTICAL;
        spStatus.setLayoutParams(stp);
        spStatus.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) { filterBooks(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        searchRow.addView(spStatus);

        Button searchBtn = new Button(this);
        searchBtn.setText("查询");
        searchBtn.setTextColor(0xFFFFFFFF);
        searchBtn.setTextSize(14);
        searchBtn.setPadding(dp(14), 0, dp(14), 0);
        searchBtn.setGravity(Gravity.CENTER);
        GradientDrawable sbBg = new GradientDrawable();
        sbBg.setColor(0xFF318af8); sbBg.setCornerRadius(dp(12));
        searchBtn.setBackground(sbBg);
        LinearLayout.LayoutParams sbp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(48));
        searchBtn.setLayoutParams(sbp);
        searchBtn.setOnClickListener(v -> filterBooks());
        searchRow.addView(searchBtn);

        main.addView(searchRow);

        // Add button
        if (isAdmin) {
            Button addBtn = new Button(this);
            addBtn.setText("新增词书");
            addBtn.setTextColor(0xFFFFFFFF);
            addBtn.setTextSize(15);
            addBtn.setPadding(0, dp(9), 0, dp(9));
            GradientDrawable abBg = new GradientDrawable();
            abBg.setColor(0xFF318af8); abBg.setCornerRadius(dp(12));
            addBtn.setBackground(abBg);
            LinearLayout.LayoutParams abp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            abp.setMargins(0, 0, 0, dp(11));
            addBtn.setLayoutParams(abp);
            addBtn.setOnClickListener(v -> startActivity(new Intent(this, AddEditBookActivity.class)));
            main.addView(addBtn);
        }

        listArea = new LinearLayout(this);
        listArea.setOrientation(LinearLayout.VERTICAL);
        main.addView(listArea);

        scroll.addView(main);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        // ===== BOTTOM NAVBAR =====
        root.addView(makeNavbar());

        setContentView(root);
        loadBooks();
    }

    private void styleSpinner(Spinner sp) {
        sp.setPadding(dp(6), 0, dp(6), 0);
        sp.setBackgroundColor(0xFFf6f8fc);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFFf6f8fc); bg.setCornerRadius(dp(7)); bg.setStroke(1, 0xFFc7d9ee);
        sp.setBackground(bg);
    }

    private LinearLayout makeNavbar() {
        LinearLayout navbar = new LinearLayout(this);
        navbar.setOrientation(LinearLayout.HORIZONTAL);
        navbar.setBackgroundColor(0xFFFFFFFF);
        navbar.setPadding(0, dp(8), 0, dp(12));
        GradientDrawable nbBg = new GradientDrawable();
        nbBg.setColor(0xFFFFFFFF);
        nbBg.setCornerRadii(new float[]{dp(14), dp(14), dp(14), dp(14), 0, 0, 0, 0});
        navbar.setBackground(nbBg);
        navbar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(61)));
        navbar.setGravity(Gravity.CENTER);
        navItem(navbar, "🏠", "主页", false, () -> startActivity(new Intent(this, AdminMainActivity.class)));
        navItem(navbar, "👥", "用户管理", false, () -> startActivity(new Intent(this, UserManageActivity.class)));
        navItem(navbar, "📚", "词书管理", true, () -> {});
        navItem(navbar, "🗃️", "单词管理", false, () -> startActivity(new Intent(this, WordSearchActivity.class)));
        return navbar;
    }

    private void navItem(LinearLayout parent, String icon, String label, boolean active, Runnable action) {
        TextView item = new TextView(this);
        item.setText(icon + "\n" + label);
        item.setTextSize(15);
        item.setTextColor(active ? 0xFF17c2ae : 0xFF318af8);
        item.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
        item.setGravity(Gravity.CENTER);
        item.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        item.setOnClickListener(v -> action.run());
        parent.addView(item);
    }

    private void bg(View v, int color, int radius, int borderW, int borderC) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color); g.setCornerRadius(radius);
        if (borderW > 0) g.setStroke(borderW, borderC);
        v.setBackground(g);
    }

    private void loadBooks() {
        new Thread(() -> {
            try {
                JsonObject r = ApiClient.get().get("/api/wordbooks");
                if (r.get("code").getAsInt() == 200) { allBooks = r.getAsJsonArray("data"); runOnUiThread(() -> filterBooks()); }
            } catch (Exception e) {}
        }).start();
    }

    private void filterBooks() {
        String kw = etSearch.getText().toString().trim().toLowerCase();
        String diff = spDifficulty.getSelectedItem() != null ? spDifficulty.getSelectedItem().toString() : "难度等级";
        String st = spStatus.getSelectedItem() != null ? spStatus.getSelectedItem().toString() : "词书状态";

        listArea.removeAllViews();
        if (allBooks == null) return;

        for (int i = 0; i < allBooks.size(); i++) {
            JsonObject b = allBooks.get(i).getAsJsonObject();
            String name = b.has("wordBookName") ? b.get("wordBookName").getAsString() : "";
            String bookDiff = b.has("difficultyLevel") ? b.get("difficultyLevel").getAsString() : "";
            String status = b.has("wordBookStatus") ? b.get("wordBookStatus").getAsString() : "已上架";
            String bid = b.has("wordBookId") ? b.get("wordBookId").getAsString() : "";
            int wc = b.has("wordCount") ? b.get("wordCount").getAsInt() : 0;
            String cTime = b.has("createTime") ? b.get("createTime").getAsString().substring(0, 10) : "";
            String uTime = b.has("updateTime") && !b.get("updateTime").isJsonNull() ? b.get("updateTime").getAsString().substring(0, 10) : "";

            if (!kw.isEmpty() && !name.toLowerCase().contains(kw) && !bid.toLowerCase().contains(kw)) continue;
            if (!"难度等级".equals(diff) && !"全部".equals(diff) && !diff.equals(bookDiff)) continue;
            if (!"词书状态".equals(st) && !"全部".equals(st) && !st.equals(status)) continue;

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(0xFFFFFFFF);
            card.setPadding(dp(13), dp(13), dp(13), dp(13));
            GradientDrawable cBg = new GradientDrawable();
            cBg.setColor(0xFFFFFFFF); cBg.setCornerRadius(dp(12));
            card.setBackground(cBg);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cp.setMargins(0, 0, 0, dp(13));
            card.setLayoutParams(cp);

            addCardRow(card, "ID: " + bid + " | 名称: " + name + " | 难度: " + bookDiff);
            addCardRow(card, "词数: " + wc + " | 创建管理员: " + AuthManager.get().getUserId());
            addCardRow(card, "创建: " + cTime + " | 更新: " + uTime + " | 状态: " + status);

            if (isAdmin) {
                LinearLayout btns = new LinearLayout(this);
                btns.setOrientation(LinearLayout.HORIZONTAL);
                btns.setPadding(0, dp(4), 0, 0);

                Button editBtn = btn(btns, "编辑", 0xFF318af8, 0xFFFFFFFF);
                editBtn.setOnClickListener(v -> { Intent in = new Intent(this, AddEditBookActivity.class); in.putExtra("bookId", bid); startActivity(in); });

                Button delBtn = btn(btns, "删除", 0xFFe0e6f2, 0xFF318af8);
                String fBid = bid, fName = name;
                delBtn.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("确认删除")
                        .setMessage("确定删除\"" + fName + "\"？")
                        .setPositiveButton("确定", (d, w) -> new Thread(() -> {
                            try { JsonObject rr = ApiClient.get().delete("/api/wordbooks/" + fBid, null);
                                runOnUiThread(() -> { Toast.makeText(this, rr.has("message")?rr.get("message").getAsString():"已下架", Toast.LENGTH_SHORT).show(); loadBooks(); });
                            } catch (Exception e) {}
                        }).start()).setNegativeButton("取消", null).show());

                Button addWordBtn = btn(btns, "添加单词", 0xFF318af8, 0xFFFFFFFF);
                addWordBtn.setOnClickListener(v -> { Intent in = new Intent(this, AddEditBookActivity.class); in.putExtra("bookId", bid); startActivity(in); });

                Button viewWordBtn = btn(btns, "查看单词", 0xFF318af8, 0xFFFFFFFF);
                viewWordBtn.setOnClickListener(v -> startActivity(new Intent(this, WordSearchActivity.class)));

                btns.addView(editBtn);
                btns.addView(delBtn);
                btns.addView(addWordBtn);
                btns.addView(viewWordBtn);
                card.addView(btns);
            }

            listArea.addView(card);
        }
    }

    private void addCardRow(LinearLayout card, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(14);
        tv.setTextColor(0xFF273245);
        tv.setPadding(0, 0, 0, dp(4));
        card.addView(tv);
    }

    private Button btn(LinearLayout parent, String text, int bgColor, int textColor) {
        Button b = new Button(this);
        b.setText(text); b.setTextColor(textColor); b.setTextSize(12); b.setPadding(dp(5), dp(5), dp(9), dp(5));
        GradientDrawable bg = new GradientDrawable(); bg.setColor(bgColor); bg.setCornerRadius(dp(11));
        b.setBackground(bg);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0, 0, dp(2), 0);
        b.setLayoutParams(bp);
        return b;
    }

    private int dp(int val) { return (int)(val * getResources().getDisplayMetrics().density + 0.5f); }
}
