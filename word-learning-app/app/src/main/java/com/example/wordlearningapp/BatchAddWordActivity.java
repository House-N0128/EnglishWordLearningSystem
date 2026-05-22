package com.example.wordlearningapp;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class BatchAddWordActivity extends AppCompatActivity {

    private Spinner spBook;
    private EditText etBatchInput;
    private Button btnSubmit;
    private List<String> bookIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFf2f8fc);

        // Top bar
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(0xFF318af8);
        topBar.setPadding(dp(16), 0, dp(16), 0);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));

        TextView btnBack = new TextView(this);
        btnBack.setText("← 返回");
        btnBack.setTextSize(16);
        btnBack.setTextColor(0xFFFFFFFF);
        btnBack.setTypeface(null, Typeface.BOLD);
        btnBack.setOnClickListener(v -> finish());
        topBar.addView(btnBack);

        TextView barTitle = new TextView(this);
        barTitle.setText("批量导入单词");
        barTitle.setTextSize(16);
        barTitle.setTextColor(0xFFFFFFFF);
        barTitle.setTypeface(null, Typeface.BOLD);
        barTitle.setGravity(Gravity.CENTER);
        barTitle.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        topBar.addView(barTitle);

        TextView placeholder = new TextView(this);
        placeholder.setLayoutParams(new LinearLayout.LayoutParams(dp(48), 1));
        topBar.addView(placeholder);
        root.addView(topBar);

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(32, 24, 32, 24);

        TextView title = new TextView(this);
        title.setText("批量导入单词");
        title.setTextSize(20);
        title.setTextColor(0xFF318af8);
        title.setPadding(0, 8, 0, 20);
        form.addView(title);

        // Book selector
        spBook = new Spinner(this);
        spBook.setPadding(16, 12, 16, 12);
        spBook.setBackgroundColor(0xFFf6f8fc);
        form.addView(spBook);

        // Instructions
        TextView hint = new TextView(this);
        hint.setText("每行一个单词，格式：英文拼写,中文释义,音标(可选),例句(可选)\n单词ID由系统自动生成，已存在的单词自动跳过");
        hint.setTextSize(13);
        hint.setTextColor(0xFF8899aa);
        hint.setPadding(0, 16, 0, 8);
        form.addView(hint);

        // Batch input area
        etBatchInput = new EditText(this);
        etBatchInput.setHint("hello,你好,/həˈloʊ/,Hello World!\nworld,世界,/wɜːld/,Hello World!");
        etBatchInput.setTextSize(14);
        etBatchInput.setPadding(16, 12, 16, 12);
        etBatchInput.setBackgroundColor(0xFFf6f8fc);
        etBatchInput.setMinLines(8);
        etBatchInput.setGravity(android.view.Gravity.TOP);
        form.addView(etBatchInput);

        // Submit
        btnSubmit = new Button(this);
        btnSubmit.setText("批量导入");
        btnSubmit.setTextColor(0xFFFFFFFF);
        btnSubmit.setTextSize(16);
        btnSubmit.setPadding(0, 14, 0, 14);
        btnSubmit.setBackgroundColor(0xFF318af8);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0, 16, 0, 0);
        btnSubmit.setLayoutParams(bp);
        btnSubmit.setOnClickListener(v -> doBatchAdd());
        form.addView(btnSubmit);

        root.addView(form);
        setContentView(root);
        loadBooks();
    }

    private void loadBooks() {
        new Thread(() -> {
            try {
                JsonObject r = ApiClient.get().get("/api/wordbooks");
                if (r.get("code").getAsInt() == 200) {
                    JsonArray books = r.getAsJsonArray("data");
                    List<String> names = new ArrayList<>();
                    for (int i = 0; i < books.size(); i++) {
                        JsonObject b = books.get(i).getAsJsonObject();
                        bookIds.add(b.get("wordBookId").getAsString());
                        names.add(b.get("wordBookName").getAsString());
                    }
                    runOnUiThread(() -> spBook.setAdapter(new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, names)));
                }
            } catch (Exception e) {}
        }).start();
    }

    private void doBatchAdd() {
        String input = etBatchInput.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "请输入单词数据", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spBook.getSelectedItemPosition() < 0 || bookIds.isEmpty()) {
            Toast.makeText(this, "请选择词书", Toast.LENGTH_SHORT).show();
            return;
        }

        String bookId = bookIds.get(spBook.getSelectedItemPosition());
        String[] lines = input.split("\n");
        JsonArray wordsArr = new JsonArray();
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",");
            if (parts.length < 2) continue;
            JsonObject word = new JsonObject();
            word.addProperty("englishSpelling", parts[0].trim());
            word.addProperty("chineseDefinition", parts[1].trim());
            if (parts.length > 2) word.addProperty("phoneticSymbol", parts[2].trim());
            if (parts.length > 3) word.addProperty("exampleSentence", parts[3].trim());
            wordsArr.add(word);
        }

        if (wordsArr.size() == 0) {
            Toast.makeText(this, "未解析到有效单词数据", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("导入中...");

        JsonArray finalWords = wordsArr;
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("wordBookId", bookId);
                body.add("words", finalWords);
                JsonObject result = ApiClient.get().post("/api/words/batch", body);
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("批量导入");
                    String msg = result.has("message") ? result.get("message").getAsString() : "导入完成";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    if (result.get("code").getAsInt() == 200) finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("批量导入");
                    Toast.makeText(this, "网络连接失败", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
