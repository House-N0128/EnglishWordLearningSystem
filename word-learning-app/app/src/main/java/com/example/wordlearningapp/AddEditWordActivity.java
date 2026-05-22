package com.example.wordlearningapp;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonObject;

public class AddEditWordActivity extends AppCompatActivity {

    private EditText etSpelling, etPhonetic, etPos, etChinese, etExample;
    private Button btnSubmit;
    private TextView tvTitle;
    private String wordId, bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wordId = getIntent().getStringExtra("wordId");
        bookId = getIntent().getStringExtra("bookId");
        boolean isEdit = wordId != null;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFf2f8fc);

        // Top bar
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
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

        tvTitle = new TextView(this);
        tvTitle.setText(isEdit ? "编辑单词" : "新增单词");
        tvTitle.setTextSize(18);
        tvTitle.setTextColor(0xFFFFFFFF);
        tvTitle.setTypeface(null, Typeface.BOLD);
        topBar.addView(tvTitle);
        root.addView(topBar);

        // Form
        ScrollView scroll = new ScrollView(this);
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(16), dp(16), dp(16), dp(20));

        etSpelling = addField(form, "英文拼写 *");
        etPhonetic = addField(form, "音标");
        etPos = addField(form, "词性 (如: n. / v. / adj.)");
        etChinese = addField(form, "中文释义 *");
        etExample = addField(form, "例句");
        etExample.setLines(2);
        etExample.setMinHeight(dp(60));
        etExample.setGravity(android.view.Gravity.TOP);

        btnSubmit = new Button(this);
        btnSubmit.setText(isEdit ? "保存修改" : "添加单词");
        btnSubmit.setTextColor(0xFFFFFFFF);
        btnSubmit.setTextSize(17);
        btnSubmit.setPadding(0, dp(12), 0, dp(12));
        GradientDrawable sbg = new GradientDrawable();
        sbg.setColors(new int[]{0xFF3577ef, 0xFF6cc3ff});
        sbg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        sbg.setCornerRadius(dp(25));
        btnSubmit.setBackground(sbg);
        LinearLayout.LayoutParams sbp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sbp.setMargins(0, dp(16), 0, 0);
        btnSubmit.setLayoutParams(sbp);
        btnSubmit.setOnClickListener(v -> submit());
        form.addView(btnSubmit);

        scroll.addView(form);
        root.addView(scroll);
        setContentView(root);

        if (isEdit) loadWordData();
    }

    private EditText addField(LinearLayout parent, String label) {
        TextView lbl = new TextView(this);
        lbl.setText(label);
        lbl.setTextSize(14);
        lbl.setTextColor(0xFF318af8);
        lbl.setTypeface(null, Typeface.BOLD);
        lbl.setPadding(0, dp(12), 0, dp(4));
        parent.addView(lbl);

        EditText et = new EditText(this);
        et.setPadding(dp(10), dp(10), dp(10), dp(10));
        et.setTextSize(15);
        et.setSingleLine(true);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFFf6f8fc);
        bg.setCornerRadius(dp(8));
        bg.setStroke(1, 0xFFc7d9ee);
        et.setBackground(bg);
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ep.setMargins(0, 0, 0, dp(12));
        et.setLayoutParams(ep);
        parent.addView(et);
        return et;
    }

    private void loadWordData() {
        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().get("/api/words/" + wordId);
                if (res.get("code").getAsInt() == 200) {
                    JsonObject w = res.getAsJsonObject("data");
                    runOnUiThread(() -> {
                        setText(etSpelling, w, "englishSpelling");
                        setText(etPhonetic, w, "phoneticSymbol");
                        setText(etPos, w, "partOfSpeech");
                        setText(etChinese, w, "chineseDefinition");
                        setText(etExample, w, "exampleSentence");
                    });
                }
            } catch (Exception e) {}
        }).start();
    }

    private void setText(EditText et, JsonObject w, String key) {
        if (w.has(key) && !w.get(key).isJsonNull()) et.setText(w.get(key).getAsString());
    }

    private void submit() {
        String spelling = etSpelling.getText().toString().trim();
        String chinese = etChinese.getText().toString().trim();
        if (spelling.isEmpty() || chinese.isEmpty()) {
            Toast.makeText(this, "请填写英文拼写和中文释义", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("提交中...");

        boolean isEdit = wordId != null;
        new Thread(() -> {
            try {
                if (!isEdit && bookId == null) {
                    // Only check duplicate when not adding to a specific book
                    JsonObject check = ApiClient.get().get("/api/words/search?keyword=" + spelling);
                    if (check.get("code").getAsInt() == 200 && check.getAsJsonArray("data").size() > 0) {
                        var arr = check.getAsJsonArray("data");
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject w = arr.get(i).getAsJsonObject();
                            if (spelling.equalsIgnoreCase(w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "")) {
                                runOnUiThread(() -> {
                                    btnSubmit.setEnabled(true);
                                    btnSubmit.setText("添加单词");
                                    Toast.makeText(this, "单词已存在: " + spelling, Toast.LENGTH_SHORT).show();
                                });
                                return;
                            }
                        }
                    }
                }

                JsonObject body = new JsonObject();
                body.addProperty("englishSpelling", spelling);
                body.addProperty("phoneticSymbol", etPhonetic.getText().toString().trim());
                body.addProperty("partOfSpeech", etPos.getText().toString().trim());
                body.addProperty("chineseDefinition", chinese);
                body.addProperty("exampleSentence", etExample.getText().toString().trim());
                if (bookId != null) body.addProperty("wordBookId", bookId);

                JsonObject res;
                if (isEdit) {
                    res = ApiClient.get().put("/api/words/" + wordId, body);
                } else {
                    res = ApiClient.get().post("/api/words", body);
                }

                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText(isEdit ? "保存修改" : "添加单词");
                    if (res.get("code").getAsInt() == 200) {
                        Toast.makeText(this, res.get("message").getAsString(), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, res.has("message") ? res.get("message").getAsString() : "操作失败", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> { btnSubmit.setEnabled(true); btnSubmit.setText(isEdit ? "保存修改" : "添加单词"); Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show(); });
            }
        }).start();
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
