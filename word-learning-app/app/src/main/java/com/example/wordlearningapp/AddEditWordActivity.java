package com.example.wordlearningapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonObject;

public class AddEditWordActivity extends AppCompatActivity {

    private EditText etBookId, etSpelling, etPhonetic, etChinese, etExample, etAudio, etImage;
    private Button btnSubmit;
    private TextView tvTitle;
    private String wordId; // null = add mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wordId = getIntent().getStringExtra("wordId");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFf2f8fc);

        // Toolbar
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setBackgroundColor(0xFF318af8);
        toolbar.setPadding(28, 28, 28, 28);
        TextView back = new TextView(this);
        back.setText("← 返回"); back.setTextSize(16); back.setTextColor(0xFFFFFFFF);
        back.setOnClickListener(v -> finish());
        toolbar.addView(back);
        tvTitle = new TextView(this);
        tvTitle.setText(wordId != null ? "编辑单词" : "添加单词");
        tvTitle.setTextSize(18); tvTitle.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tvTitle.setGravity(android.view.Gravity.CENTER);
        tvTitle.setLayoutParams(tp);
        toolbar.addView(tvTitle);
        toolbar.addView(new View(this) {{ setLayoutParams(new LinearLayout.LayoutParams(48, 1)); }});
        root.addView(toolbar);

        // Form in ScrollView
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(28, 20, 28, 24);

        etBookId = addField(form, "所属词书ID");
        etSpelling = addField(form, "英文拼写");
        etPhonetic = addField(form, "音标");
        etChinese = addField(form, "中文释义");
        etExample = addField(form, "例句");
        etExample.setLines(2);
        etAudio = addField(form, "音频URL");
        etImage = addField(form, "图片URL");

        btnSubmit = new Button(this);
        btnSubmit.setText(wordId != null ? "保存修改" : "添加单词");
        btnSubmit.setTextColor(0xFFFFFFFF); btnSubmit.setBackgroundColor(0xFF318af8);
        btnSubmit.setTextSize(16); btnSubmit.setPadding(14, 14, 14, 14);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0, 20, 0, 0);
        btnSubmit.setLayoutParams(bp);
        form.addView(btnSubmit);

        root.addView(form);
        setContentView(root);

        btnSubmit.setOnClickListener(v -> submit());

        if (wordId != null) loadWordData();
    }

    private EditText addField(LinearLayout parent, String label) {
        TextView lbl = new TextView(this);
        lbl.setText(label); lbl.setTextSize(14); lbl.setTextColor(0xFF318af8);
        lbl.setPadding(0, 10, 0, 4);
        parent.addView(lbl);
        EditText et = new EditText(this);
        et.setPadding(28, 12, 28, 12); et.setBackgroundColor(0xFFf6f8fc);
        et.setTextSize(15); et.setSingleLine(true);
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ep.setMargins(0, 0, 0, 6);
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
                        if (w.has("wordBookId") && !w.get("wordBookId").isJsonNull()) etBookId.setText(w.get("wordBookId").getAsString());
                        if (w.has("englishSpelling") && !w.get("englishSpelling").isJsonNull()) etSpelling.setText(w.get("englishSpelling").getAsString());
                        if (w.has("phoneticSymbol") && !w.get("phoneticSymbol").isJsonNull()) etPhonetic.setText(w.get("phoneticSymbol").getAsString());
                        if (w.has("chineseDefinition") && !w.get("chineseDefinition").isJsonNull()) etChinese.setText(w.get("chineseDefinition").getAsString());
                        if (w.has("exampleSentence") && !w.get("exampleSentence").isJsonNull()) etExample.setText(w.get("exampleSentence").getAsString());
                        if (w.has("wordPronunciation") && !w.get("wordPronunciation").isJsonNull()) etAudio.setText(w.get("wordPronunciation").getAsString());
                        if (w.has("wordImage") && !w.get("wordImage").isJsonNull()) etImage.setText(w.get("wordImage").getAsString());
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void submit() {
        String bookId = etBookId.getText().toString().trim();
        String spelling = etSpelling.getText().toString().trim();
        String chinese = etChinese.getText().toString().trim();
        if (spelling.isEmpty() || chinese.isEmpty() || bookId.isEmpty()) {
            Toast.makeText(this, "请填写词书ID、拼写和释义", Toast.LENGTH_SHORT).show(); return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("提交中...");

        JsonObject body = new JsonObject();
        if (wordId == null) body.addProperty("wordId", "WD" + System.currentTimeMillis() % 100000);
        body.addProperty("wordBookId", bookId);
        body.addProperty("englishSpelling", spelling);
        body.addProperty("phoneticSymbol", etPhonetic.getText().toString().trim());
        body.addProperty("chineseDefinition", chinese);
        body.addProperty("exampleSentence", etExample.getText().toString().trim());
        body.addProperty("wordPronunciation", etAudio.getText().toString().trim());
        body.addProperty("wordImage", etImage.getText().toString().trim());

        new Thread(() -> {
            try {
                JsonObject res;
                if (wordId != null) {
                    res = ApiClient.get().put("/api/words/" + wordId, body);
                } else {
                    res = ApiClient.get().post("/api/words", body);
                }
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText(wordId != null ? "保存修改" : "添加单词");
                    if (res.get("code").getAsInt() == 200) {
                        Toast.makeText(this, res.get("message").getAsString(), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, res.has("message") ? res.get("message").getAsString() : "操作失败", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> { btnSubmit.setEnabled(true); Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show(); });
            }
        }).start();
    }
}
