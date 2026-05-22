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

public class AddWordToBookActivity extends AppCompatActivity {

    private EditText etWordId, etSpelling, etPhonetic, etChinese, etExample;
    private Button btnSubmit;
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
        barTitle.setText("添加单词");
        barTitle.setTextSize(18);
        barTitle.setTextColor(0xFFFFFFFF);
        barTitle.setTypeface(null, Typeface.BOLD);
        topBar.addView(barTitle);
        root.addView(topBar);

        // === FORM ===
        ScrollView scroll = new ScrollView(this);
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(16), dp(16), dp(16), dp(20));

        TextView info = new TextView(this);
        info.setText("词书: " + bookName + " (" + bookId + ")");
        info.setTextSize(15);
        info.setTextColor(0xFF318af8);
        info.setPadding(0, 0, 0, dp(16));
        form.addView(info);

        etWordId = addField(form, "单词ID");
        etSpelling = addField(form, "英文拼写");
        etPhonetic = addField(form, "音标");
        etChinese = addField(form, "中文释义");
        etExample = addField(form, "例句");

        btnSubmit = new Button(this);
        btnSubmit.setText("添加单词");
        btnSubmit.setTextColor(0xFFFFFFFF);
        btnSubmit.setTextSize(17);
        btnSubmit.setPadding(0, dp(12), 0, dp(12));
        GradientDrawable sbg = new GradientDrawable();
        sbg.setColors(new int[]{0xFF3577ef, 0xFF6cc3ff});
        sbg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        sbg.setCornerRadius(dp(25));
        btnSubmit.setBackground(sbg);
        LinearLayout.LayoutParams sbp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sbp.setMargins(0, dp(10), 0, 0);
        btnSubmit.setLayoutParams(sbp);
        btnSubmit.setOnClickListener(v -> submit());
        form.addView(btnSubmit);

        scroll.addView(form);
        root.addView(scroll);
        setContentView(root);
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
        et.setPadding(dp(10), dp(8), dp(10), dp(8));
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

    private void submit() {
        String wordId = etWordId.getText().toString().trim();
        String spelling = etSpelling.getText().toString().trim();
        String chinese = etChinese.getText().toString().trim();

        if (wordId.isEmpty() || spelling.isEmpty() || chinese.isEmpty()) {
            Toast.makeText(this, "请填写单词ID、拼写和释义", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("提交中...");

        JsonObject body = new JsonObject();
        body.addProperty("wordId", wordId);
        body.addProperty("wordBookId", bookId);
        body.addProperty("englishSpelling", spelling);
        body.addProperty("chineseDefinition", chinese);
        body.addProperty("phoneticSymbol", etPhonetic.getText().toString().trim());
        body.addProperty("exampleSentence", etExample.getText().toString().trim());

        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().post("/api/words", body);
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("添加单词");
                    if (res.get("code").getAsInt() == 200) {
                        Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                        etWordId.setText("");
                        etSpelling.setText("");
                        etPhonetic.setText("");
                        etChinese.setText("");
                        etExample.setText("");
                    } else {
                        Toast.makeText(this, res.has("message") ? res.get("message").getAsString() : "添加失败", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> { btnSubmit.setEnabled(true); btnSubmit.setText("添加单词"); Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show(); });
            }
        }).start();
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
