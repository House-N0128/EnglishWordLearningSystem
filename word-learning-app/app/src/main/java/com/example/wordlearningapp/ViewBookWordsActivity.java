package com.example.wordlearningapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

        // === TOP BAR ===
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(0xFF318af8);
        topBar.setPadding(dp(6), 0, dp(18), 0);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        GradientDrawable tbBg = new GradientDrawable();
        tbBg.setColor(0xFF318af8);
        tbBg.setCornerRadii(new float[]{0, 0, 0, 0, dp(18), dp(18), dp(18), dp(18)});
        topBar.setBackground(tbBg);

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

        // === CONTENT ===
        ScrollView scroll = new ScrollView(this);
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(16), dp(16), dp(16), dp(20));

        TextView info = new TextView(this);
        info.setText(bookName + " (" + bookId + ")");
        info.setTextSize(16);
        info.setTextColor(0xFF318af8);
        info.setTypeface(null, Typeface.BOLD);
        info.setPadding(0, 0, 0, dp(16));
        main.addView(info);

        listArea = new LinearLayout(this);
        listArea.setOrientation(LinearLayout.VERTICAL);
        main.addView(listArea);

        scroll.addView(main);
        root.addView(scroll);
        setContentView(root);

        loadWords();
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
                            emp.setText("该词书暂无单词");
                            emp.setTextSize(14);
                            emp.setTextColor(0xFF8899aa);
                            emp.setPadding(0, dp(20), 0, 0);
                            listArea.addView(emp);
                            return;
                        }
                        for (int i = 0; i < data.size(); i++) {
                            JsonObject w = data.get(i).getAsJsonObject();
                            String spelling = w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "";
                            String definition = w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "";
                            String wordId = w.has("wordId") ? w.get("wordId").getAsString() : "";
                            String phonetic = w.has("phoneticSymbol") && !w.get("phoneticSymbol").isJsonNull() ? w.get("phoneticSymbol").getAsString() : "";

                            LinearLayout card = new LinearLayout(this);
                            card.setOrientation(LinearLayout.VERTICAL);
                            card.setBackgroundColor(0xFFFFFFFF);
                            card.setPadding(dp(14), dp(12), dp(14), dp(12));
                            GradientDrawable cb = new GradientDrawable();
                            cb.setColor(0xFFFFFFFF);
                            cb.setCornerRadius(dp(10));
                            card.setBackground(cb);
                            card.setElevation(dp(1));
                            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            cp.setMargins(0, 0, 0, dp(8));
                            card.setLayoutParams(cp);

                            TextView sp = new TextView(this);
                            sp.setText(spelling + (phonetic.isEmpty() ? "" : "  " + phonetic));
                            sp.setTextSize(15);
                            sp.setTextColor(0xFF318af8);
                            sp.setTypeface(null, Typeface.BOLD);
                            card.addView(sp);

                            TextView def = new TextView(this);
                            def.setText(definition);
                            def.setTextSize(14);
                            def.setTextColor(0xFF4a5568);
                            def.setPadding(0, dp(4), 0, dp(8));
                            card.addView(def);

                            LinearLayout btns = new LinearLayout(this);
                            btns.setOrientation(LinearLayout.HORIZONTAL);

                            Button editBtn = new Button(this);
                            editBtn.setText("编辑");
                            editBtn.setTextColor(0xFFFFFFFF);
                            editBtn.setTextSize(12);
                            editBtn.setPadding(dp(10), dp(4), dp(10), dp(4));
                            GradientDrawable eb = new GradientDrawable();
                            eb.setColor(0xFF318af8);
                            eb.setCornerRadius(dp(5));
                            editBtn.setBackground(eb);
                            LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            elp.setMargins(0, 0, dp(8), 0);
                            editBtn.setLayoutParams(elp);
                            editBtn.setOnClickListener(v -> {
                                Intent in = new Intent(this, AddEditWordActivity.class);
                                in.putExtra("wordId", wordId);
                                startActivity(in);
                            });
                            btns.addView(editBtn);

                            Button delBtn = new Button(this);
                            delBtn.setText("移除");
                            delBtn.setTextColor(0xFF5a6b80);
                            delBtn.setTextSize(12);
                            delBtn.setPadding(dp(10), dp(4), dp(10), dp(4));
                            GradientDrawable db = new GradientDrawable();
                            db.setColor(0xFFe8ecf1);
                            db.setCornerRadius(dp(5));
                            delBtn.setBackground(db);
                            String fWordId = wordId;
                            delBtn.setOnClickListener(v -> new AlertDialog.Builder(this)
                                    .setTitle("移除单词")
                                    .setMessage("确定从词书移除 \"" + spelling + "\"？")
                                    .setPositiveButton("移除", (d, w2) -> new Thread(() -> {
                                        try {
                                            JsonObject rr = ApiClient.get().delete("/api/words/" + fWordId + "?wordBookId=" + bookId, null);
                                            runOnUiThread(() -> {
                                                Toast.makeText(this, rr.has("message") ? rr.get("message").getAsString() : "已移除", Toast.LENGTH_SHORT).show();
                                                loadWords();
                                            });
                                        } catch (Exception e) {}
                                    }).start())
                                    .setNegativeButton("取消", null)
                                    .show());
                            btns.addView(delBtn);

                            card.addView(btns);
                            listArea.addView(card);
                        }
                    }
                });
            } catch (Exception e) {}
        }).start();
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
