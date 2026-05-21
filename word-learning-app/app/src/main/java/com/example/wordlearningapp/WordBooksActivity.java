package com.example.wordlearningapp;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
    private boolean isAdmin;
    private JsonArray allBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isAdmin = "admin".equals(AuthManager.get().getRole());

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xFFf2f8fc);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        // Header
        addHeader(root, isAdmin ? "词书管理" : "词书浏览");

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(24, 16, 24, 80);

        // Search row: input + button, same as WordSearchActivity
        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);

        etSearch = new EditText(this);
        etSearch.setHint("词书名称搜索");
        etSearch.setTextSize(15);
        etSearch.setPadding(20, 16, 20, 16);
        etSearch.setGravity(Gravity.CENTER_VERTICAL);
        etSearch.setSingleLine(true);
        bg(etSearch, 0xFFf6f8fc, 12, 1, 0xFFc7d9ee);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, dp(48), 1);
        sp.gravity = Gravity.CENTER_VERTICAL;
        etSearch.setLayoutParams(sp);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { filterBooks(); }
            @Override public void afterTextChanged(android.text.Editable e) {}
        });
        searchRow.addView(etSearch);

        Button searchBtn = new Button(this);
        searchBtn.setText("搜索");
        searchBtn.setTextColor(0xFFFFFFFF);
        searchBtn.setTextSize(14);
        searchBtn.setPadding(16, 0, 16, 0);
        searchBtn.setGravity(Gravity.CENTER);
        GradientDrawable sbb = new GradientDrawable();
        sbb.setColor(0xFF318af8); sbb.setCornerRadius(12);
        searchBtn.setBackground(sbb);
        LinearLayout.LayoutParams sbp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(48));
        sbp.setMargins(8, 0, 0, 14);
        searchBtn.setLayoutParams(sbp);
        searchBtn.setOnClickListener(v -> filterBooks());
        searchRow.addView(searchBtn);

        etSearch.setOnEditorActionListener((v, actionId, event) -> { if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) { filterBooks(); return true; } return false; });

        main.addView(searchRow);

        if (isAdmin) {
            Button addBtn = new Button(this);
            addBtn.setText("+ 新增词书");
            addBtn.setTextColor(0xFFFFFFFF);
            addBtn.setBackgroundColor(0xFF318af8);
            addBtn.setTextSize(15);
            addBtn.setPadding(14, 12, 14, 12);
            bg(addBtn, 0xFF318af8, 24, 0, 0);
            LinearLayout.LayoutParams abp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            abp.setMargins(0, 0, 0, 14);
            addBtn.setLayoutParams(abp);
            addBtn.setOnClickListener(v -> startActivity(new Intent(this, AddEditBookActivity.class)));
            main.addView(addBtn);
        }

        listArea = new LinearLayout(this);
        listArea.setOrientation(LinearLayout.VERTICAL);
        main.addView(listArea);

        root.addView(main);
        scroll.addView(root);
        setContentView(scroll);
        loadBooks();
    }

    private void addHeader(LinearLayout root, String title) {
        LinearLayout h = new LinearLayout(this);
        h.setBackgroundColor(0xFF318af8);
        h.setPadding(32, 24, 32, 24);
        h.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = new TextView(this);
        back.setText("← 返回");
        back.setTextSize(15);
        back.setTextColor(0xFFFFFFFF);
        back.setOnClickListener(v -> finish());
        h.addView(back);

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextSize(18);
        t.setTextColor(0xFFFFFFFF);
        t.setGravity(Gravity.CENTER);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        h.addView(t);

        h.addView(new View(this) {{ setLayoutParams(new LinearLayout.LayoutParams(48, 1)); }});
        root.addView(h);
    }

    private void bg(View v, int color, int radius, int borderW, int borderC) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radius);
        if (borderW > 0) g.setStroke(borderW, borderC);
        v.setBackground(g);
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void loadBooks() {
        new Thread(() -> {
            try {
                JsonObject r = ApiClient.get().get("/api/wordbooks");
                if (r.get("code").getAsInt() == 200) {
                    allBooks = r.getAsJsonArray("data");
                    runOnUiThread(() -> filterBooks());
                }
            } catch (Exception e) {}
        }).start();
    }

    private void filterBooks() {
        String kw = etSearch.getText().toString().trim().toLowerCase();
        listArea.removeAllViews();
        if (allBooks == null) return;

        for (int i = 0; i < allBooks.size(); i++) {
            JsonObject b = allBooks.get(i).getAsJsonObject();
            String name = b.has("wordBookName") ? b.get("wordBookName").getAsString() : "";
            if (!kw.isEmpty() && !name.toLowerCase().contains(kw)) continue;

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(0xFFFFFFFF);
            card.setPadding(24, 18, 24, 18);
            bg(card, 0xFFFFFFFF, 16, 0, 0);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cp.setMargins(0, 0, 0, 14);
            card.setLayoutParams(cp);

            TextView nm = new TextView(this);
            nm.setText(name);
            nm.setTextSize(16);
            nm.setTextColor(0xFF318af8);
            card.addView(nm);

            TextView info = new TextView(this);
            info.setText((b.has("difficultyLevel") ? b.get("difficultyLevel").getAsString() : "") + " | " + (b.has("wordCount") ? b.get("wordCount").getAsInt() : 0) + "词");
            info.setTextSize(13);
            info.setTextColor(0xFF8899aa);
            info.setPadding(0, 4, 0, 8);
            card.addView(info);

            String bid = b.has("wordBookId") ? b.get("wordBookId").getAsString() : "";

            if (isAdmin) {
                LinearLayout btns = new LinearLayout(this);
                btns.setOrientation(LinearLayout.HORIZONTAL);

                Button edit = new Button(this);
                edit.setText("编辑");
                edit.setTextColor(0xFFFFFFFF);
                edit.setTextSize(13);
                edit.setPadding(20, 8, 20, 8);
                bg(edit, 0xFF318af8, 20, 0, 0);
                LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ep.setMargins(0, 0, 12, 0);
                edit.setLayoutParams(ep);
                edit.setOnClickListener(v -> { Intent in = new Intent(this, AddEditBookActivity.class); in.putExtra("bookId", bid); startActivity(in); });
                btns.addView(edit);

                Button del = new Button(this);
                del.setText("下架");
                del.setTextColor(0xFF318af8);
                del.setTextSize(13);
                del.setPadding(20, 8, 20, 8);
                bg(del, 0xFFe0e6f2, 20, 0, 0);
                del.setOnClickListener(v -> {
                    new AlertDialog.Builder(this).setTitle("确认下架").setMessage("确定下架\"" + name + "\"？")
                            .setPositiveButton("确定", (d, w) -> {
                                new Thread(() -> {
                                    try {
                                        JsonObject rr = ApiClient.get().delete("/api/wordbooks/" + bid, null);
                                        runOnUiThread(() -> { Toast.makeText(this, rr.has("message") ? rr.get("message").getAsString() : "已下架", Toast.LENGTH_SHORT).show(); loadBooks(); });
                                    } catch (Exception e) {}
                                }).start();
                            }).setNegativeButton("取消", null).show();
                });
                btns.addView(del);

                card.addView(btns);
            } else {
                card.setOnClickListener(v -> { Intent in = new Intent(this, BookDetailActivity.class); in.putExtra("bookId", bid); startActivity(in); });
            }

            listArea.addView(card);
        }
    }
}
