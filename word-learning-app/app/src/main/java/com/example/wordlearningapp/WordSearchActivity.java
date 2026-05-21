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

public class WordSearchActivity extends AppCompatActivity {

    private LinearLayout listArea;
    private EditText etSearch;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isAdmin = "admin".equals(AuthManager.get().getRole());

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xFFf2f8fc);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        // Header
        LinearLayout h = new LinearLayout(this);
        h.setBackgroundColor(0xFF318af8);
        h.setPadding(32, 24, 32, 24);
        h.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = new TextView(this);
        back.setText("← 返回"); back.setTextSize(15); back.setTextColor(0xFFFFFFFF);
        back.setOnClickListener(v -> finish());
        h.addView(back);
        TextView t = new TextView(this);
        t.setText(isAdmin ? "单词管理" : "单词查询");
        t.setTextSize(18); t.setTextColor(0xFFFFFFFF); t.setGravity(Gravity.CENTER);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        h.addView(t);
        h.addView(new View(this) {{ setLayoutParams(new LinearLayout.LayoutParams(48, 1)); }});
        root.addView(h);

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(24, 16, 24, 80);

        // Search row
        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);

        etSearch = new EditText(this);
        etSearch.setHint("输入英文搜索单词");
        etSearch.setTextSize(15);
        etSearch.setPadding(20, 16, 20, 16);
        etSearch.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable sd = new GradientDrawable();
        sd.setColor(0xFFf6f8fc); sd.setCornerRadius(12); sd.setStroke(1, 0xFFc7d9ee);
        etSearch.setBackground(sd);
        etSearch.setMinHeight(0);
        LinearLayout.LayoutParams edp = new LinearLayout.LayoutParams(0, dp(48), 1);
        edp.gravity = Gravity.CENTER_VERTICAL;
        etSearch.setLayoutParams(edp);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { doSearch(); }
            @Override public void afterTextChanged(android.text.Editable e) {}
        });
        etSearch.setOnEditorActionListener((v, actionId, event) -> { if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) { doSearch(); return true; } return false; });
        searchRow.addView(etSearch);

        main.addView(searchRow);

        if (isAdmin) {
            Button addBtn = new Button(this);
            addBtn.setText("+ 新增单词");
            addBtn.setTextColor(0xFFFFFFFF);
            addBtn.setTextSize(15); addBtn.setPadding(14, 12, 14, 12);
            GradientDrawable ab = new GradientDrawable();
            ab.setColor(0xFF318af8); ab.setCornerRadius(24);
            addBtn.setBackground(ab);
            LinearLayout.LayoutParams abp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            abp.setMargins(0, 12, 0, 12);
            addBtn.setLayoutParams(abp);
            addBtn.setOnClickListener(v -> startActivity(new Intent(this, AddEditWordActivity.class)));
            main.addView(addBtn);
        }

        listArea = new LinearLayout(this);
        listArea.setOrientation(LinearLayout.VERTICAL);
        main.addView(listArea);

        root.addView(main);
        scroll.addView(root);
        setContentView(scroll);
    }

    private void doSearch() {
        String kw = etSearch.getText().toString().trim();
        if (kw.isEmpty()) return;

        new Thread(() -> {
            try {
                JsonObject r = ApiClient.get().get("/api/words/search?keyword=" + kw);
                runOnUiThread(() -> {
                    listArea.removeAllViews();
                    if (r.get("code").getAsInt() == 200 && r.getAsJsonArray("data").size() > 0) {
                        JsonArray data = r.getAsJsonArray("data");
                        for (int i = 0; i < data.size(); i++) {
                            JsonObject w = data.get(i).getAsJsonObject();
                            LinearLayout card = new LinearLayout(this);
                            card.setOrientation(LinearLayout.VERTICAL);
                            card.setBackgroundColor(0xFFFFFFFF);
                            card.setPadding(24, 18, 24, 18);
                            GradientDrawable cd = new GradientDrawable();
                            cd.setColor(0xFFFFFFFF); cd.setCornerRadius(16);
                            card.setBackground(cd);
                            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            cp.setMargins(0, 0, 0, 14);
                            card.setLayoutParams(cp);

                            String spelling = w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "";
                            String phonetic = w.has("phoneticSymbol") && !w.get("phoneticSymbol").isJsonNull() ? w.get("phoneticSymbol").getAsString() : "";
                            String chinese = w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "";
                            String wordId = w.has("wordId") ? w.get("wordId").getAsString() : "";

                            TextView nm = new TextView(this);
                            nm.setText(spelling + "  " + phonetic);
                            nm.setTextSize(16); nm.setTextColor(0xFF318af8);
                            card.addView(nm);

                            TextView def = new TextView(this);
                            def.setText(chinese);
                            def.setTextSize(14); def.setTextColor(0xFF273245);
                            def.setPadding(0, 4, 0, 4);
                            card.addView(def);

                            if (w.has("exampleSentence") && !w.get("exampleSentence").isJsonNull()) {
                                TextView ex = new TextView(this);
                                ex.setText("例句：" + w.get("exampleSentence").getAsString());
                                ex.setTextSize(12); ex.setTextColor(0xFF8899aa);
                                ex.setPadding(0, 0, 0, 6);
                                card.addView(ex);
                            }

                            if (isAdmin) {
                                LinearLayout btns = new LinearLayout(this);
                                btns.setOrientation(LinearLayout.HORIZONTAL);

                                Button edit = new Button(this);
                                edit.setText("编辑"); edit.setTextColor(0xFFFFFFFF); edit.setTextSize(13); edit.setPadding(20, 8, 20, 8);
                                GradientDrawable eb = new GradientDrawable();
                                eb.setColor(0xFF318af8); eb.setCornerRadius(20);
                                edit.setBackground(eb);
                                LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                ep.setMargins(0, 0, 12, 0);
                                edit.setLayoutParams(ep);
                                String fid = wordId;
                                edit.setOnClickListener(v -> { Intent in = new Intent(this, AddEditWordActivity.class); in.putExtra("wordId", fid); startActivity(in); });
                                btns.addView(edit);

                                Button del = new Button(this);
                                del.setText("删除"); del.setTextColor(0xFF318af8); del.setTextSize(13); del.setPadding(20, 8, 20, 8);
                                GradientDrawable db = new GradientDrawable();
                                db.setColor(0xFFe0e6f2); db.setCornerRadius(20);
                                del.setBackground(db);
                                String did = wordId;
                                del.setOnClickListener(v -> {
                                    new AlertDialog.Builder(this).setTitle("确认删除").setMessage("确定删除\"" + spelling + "\"？")
                                            .setPositiveButton("确定", (d, w2) -> {
                                                new Thread(() -> {
                                                    try {
                                                        JsonObject rr = ApiClient.get().delete("/api/words/" + did, null);
                                                        runOnUiThread(() -> { Toast.makeText(this, rr.has("message") ? rr.get("message").getAsString() : "已删除", Toast.LENGTH_SHORT).show(); doSearch(); });
                                                    } catch (Exception e) {}
                                                }).start();
                                            }).setNegativeButton("取消", null).show();
                                });
                                btns.addView(del);

                                card.addView(btns);
                            } else {
                                card.setOnClickListener(v -> { Intent in = new Intent(this, WordDetailActivity.class); in.putExtra("wordId", wordId); startActivity(in); });
                            }

                            listArea.addView(card);
                        }
                    } else {
                        TextView emp = new TextView(this);
                        emp.setText("未找到相关单词");
                        emp.setTextSize(14); emp.setTextColor(0xFF8899aa);
                        emp.setGravity(Gravity.CENTER); emp.setPadding(0, 40, 0, 40);
                        listArea.addView(emp);
                    }
                });
            } catch (Exception e) {}
        }).start();
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
