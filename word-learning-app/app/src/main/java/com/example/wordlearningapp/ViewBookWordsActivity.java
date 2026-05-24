package com.example.wordlearningapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ViewBookWordsActivity extends AppCompatActivity {

    private LinearLayout listArea;
    private EditText etAddWord;
    private String bookId, bookName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bookId = getIntent().getStringExtra("bookId");
        bookName = getIntent().getStringExtra("bookName");
        if (bookId == null) { finish(); return; }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFf2f8fc);

        // Top bar
        getWindow().setStatusBarColor(0xFF318af8);
        int statusBarH = 0;
        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) statusBarH = getResources().getDimensionPixelSize(resId);

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(0xFF318af8);
        topBar.setPadding(dp(6), statusBarH + dp(8), dp(18), dp(8));
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48) + statusBarH));

        TextView btnBack = new TextView(this);
        btnBack.setText("←");
        btnBack.setTextSize(22);
        btnBack.setTextColor(0xFFFFFFFF);
        btnBack.setTypeface(null, Typeface.BOLD);
        btnBack.setPadding(dp(5), 0, dp(11), 0);
        btnBack.setOnClickListener(v -> finish());
        topBar.addView(btnBack);

        TextView barTitle = new TextView(this);
        barTitle.setText("词书单词");
        barTitle.setTextSize(18);
        barTitle.setTextColor(0xFFFFFFFF);
        barTitle.setTypeface(null, Typeface.BOLD);
        topBar.addView(barTitle);
        root.addView(topBar);

        // Content
        ScrollView scroll = new ScrollView(this);
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(12), dp(12), dp(12), dp(20));

        TextView info = new TextView(this);
        info.setText(bookName + " (" + bookId + ")");
        info.setTextSize(15);
        info.setTextColor(0xFF318af8);
        info.setTypeface(null, Typeface.BOLD);
        info.setPadding(0, 0, 0, dp(12));
        main.addView(info);

        // Add word row
        LinearLayout addRow = new LinearLayout(this);
        addRow.setOrientation(LinearLayout.HORIZONTAL);
        addRow.setPadding(0, 0, 0, dp(12));
        addRow.setGravity(Gravity.CENTER_VERTICAL);

        etAddWord = new EditText(this);
        etAddWord.setHint("英文拼写");
        etAddWord.setTextSize(14);
        etAddWord.setSingleLine(true);
        etAddWord.setPadding(dp(10), dp(8), dp(10), dp(8));
        bg(etAddWord, 0xFFf6f8fc, dp(7), 1, 0xFFc7d9ee);
        LinearLayout.LayoutParams edp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        edp.gravity = Gravity.CENTER_VERTICAL;
        etAddWord.setLayoutParams(edp);
        addRow.addView(etAddWord);

        Button btnAdd = new Button(this);
        btnAdd.setText("添加");
        btnAdd.setTextColor(0xFFFFFFFF);
        btnAdd.setTextSize(13);
        btnAdd.setPadding(dp(12), dp(8), dp(12), dp(8));
        GradientDrawable abBg = new GradientDrawable();
        abBg.setColor(0xFF318af8);
        abBg.setCornerRadius(dp(6));
        btnAdd.setBackground(abBg);
        LinearLayout.LayoutParams abp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        abp.setMargins(dp(8), 0, 0, 0);
        btnAdd.setLayoutParams(abp);
        btnAdd.setOnClickListener(v -> addWordToBook());
        addRow.addView(btnAdd);
        main.addView(addRow);

        listArea = new LinearLayout(this);
        listArea.setOrientation(LinearLayout.VERTICAL);
        main.addView(listArea);

        scroll.addView(main);
        root.addView(scroll);
        setContentView(root);

        loadWords();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWords();
    }

    private void addWordToBook() {
        String spelling = etAddWord.getText().toString().trim();
        if (spelling.isEmpty()) { Toast.makeText(this, "请输入拼写", Toast.LENGTH_SHORT).show(); return; }

        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("englishSpelling", spelling);
                body.addProperty("chineseDefinition", spelling);
                body.addProperty("wordBookId", bookId);
                JsonObject r = ApiClient.get().post("/api/words", body);
                runOnUiThread(() -> {
                    Toast.makeText(this, r.has("message") ? r.get("message").getAsString() : "完成", Toast.LENGTH_SHORT).show();
                    if (r.get("code").getAsInt() == 200) { etAddWord.setText(""); loadWords(); }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void loadWords() {
        new Thread(() -> {
            try {
                JsonObject r = ApiClient.get().get("/api/words?wordBookId=" + bookId);
                runOnUiThread(() -> {
                    listArea.removeAllViews();
                    if (r.get("code").getAsInt() == 200) {
                        JsonArray data = r.getAsJsonArray("data");
                        if (data.size() == 0) {
                            TextView emp = new TextView(this);
                            emp.setText("暂无单词");
                            emp.setTextSize(13);
                            emp.setTextColor(0xFF8899aa);
                            emp.setPadding(0, dp(16), 0, 0);
                            listArea.addView(emp);
                            return;
                        }
                        for (int i = 0; i < data.size(); i++) {
                            JsonObject w = data.get(i).getAsJsonObject();
                            String spelling = w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "";
                            String definition = w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "";
                            String wordId = w.has("wordId") ? w.get("wordId").getAsString() : "";
                            String phonetic = w.has("phoneticSymbol") && !w.get("phoneticSymbol").isJsonNull() ? w.get("phoneticSymbol").getAsString() : "";
                            String partOfSpeech = w.has("partOfSpeech") && !w.get("partOfSpeech").isJsonNull() ? w.get("partOfSpeech").getAsString() : "";

                            // Compact row: spelling + partOfSpeech + definition | edit + remove
                            LinearLayout row = new LinearLayout(this);
                            row.setOrientation(LinearLayout.HORIZONTAL);
                            row.setBackgroundColor(0xFFFFFFFF);
                            row.setPadding(dp(10), dp(6), dp(10), dp(6));
                            row.setGravity(Gravity.CENTER_VERTICAL);
                            GradientDrawable rb = new GradientDrawable();
                            rb.setColor(0xFFFFFFFF);
                            rb.setCornerRadius(dp(6));
                            row.setBackground(rb);
                            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            rp.setMargins(0, 0, 0, dp(2));
                            row.setLayoutParams(rp);

                            StringBuilder displayText = new StringBuilder(spelling);
                            if (!phonetic.isEmpty()) displayText.append(" ").append(phonetic);
                            if (!partOfSpeech.isEmpty()) displayText.append(" ").append(partOfSpeech);
                            displayText.append("  ").append(definition);

                            TextView sp = new TextView(this);
                            sp.setText(displayText.toString());
                            sp.setTextSize(13);
                            sp.setTextColor(0xFF318af8);
                            sp.setTypeface(null, Typeface.BOLD);
                            sp.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                            row.addView(sp);

                            Button editBtn = tinyBtn("编辑", 0xFF318af8, 0xFFFFFFFF);
                            editBtn.setOnClickListener(v -> {
                                Intent in = new Intent(this, AddEditWordActivity.class);
                                in.putExtra("wordId", wordId);
                                startActivity(in);
                            });
                            row.addView(editBtn);

                            Button delBtn = tinyBtn("移除", 0xFFe8ecf1, 0xFF5a6b80);
                            delBtn.setOnClickListener(v -> new AlertDialog.Builder(this)
                                    .setTitle("移除单词")
                                    .setMessage("确定从词书移除 \"" + spelling + "\"？")
                                    .setPositiveButton("移除", (d, w2) -> new Thread(() -> {
                                        try {
                                            ApiClient.get().delete("/api/words/" + wordId + "?wordBookId=" + bookId, null);
                                            runOnUiThread(this::loadWords);
                                        } catch (Exception e) {}
                                    }).start())
                                    .setNegativeButton("取消", null)
                                    .show());
                            row.addView(delBtn);

                            Button detailBtn = tinyBtn("详情", 0xFF17c2ae, 0xFFFFFFFF);
                            detailBtn.setOnClickListener(v -> {
                                Intent in = new Intent(this, WordDetailActivity.class);
                                in.putExtra("wordId", wordId);
                                startActivity(in);
                            });
                            row.addView(detailBtn);

                            listArea.addView(row);
                        }
                    }
                });
            } catch (Exception e) {}
        }).start();
    }

    private Button tinyBtn(String text, int bg, int fg) {
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
        return b;
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
}
