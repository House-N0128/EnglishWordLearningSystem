package com.example.wordlearningapp;

import android.os.Bundle;
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
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonObject;

public class AddEditBookActivity extends AppCompatActivity {

    private EditText etId, etName, etDesc;
    private Spinner spDifficulty;
    private Button btnSubmit;
    private TextView tvTitle;
    private String bookId; // null = add mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bookId = getIntent().getStringExtra("bookId");

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
        tvTitle.setText(bookId != null ? "编辑词书" : "添加词书");
        tvTitle.setTextSize(18); tvTitle.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tvTitle.setGravity(android.view.Gravity.CENTER);
        tvTitle.setLayoutParams(tp);
        toolbar.addView(tvTitle);
        toolbar.addView(new View(this) {{ setLayoutParams(new LinearLayout.LayoutParams(48, 1)); }});
        root.addView(toolbar);

        // Form
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(28, 24, 28, 24);

        if (bookId == null) {
            // Add mode: ID auto-generated, show hint only
            TextView idLabel = new TextView(this);
            idLabel.setText("词书ID（系统自动生成）");
            idLabel.setTextSize(14); idLabel.setTextColor(0xFF4a5568);
            idLabel.setPadding(0, 12, 0, 6);
            form.addView(idLabel);

            TextView idHint = new TextView(this);
            idHint.setText("系统将自动分配 WB001 格式ID");
            idHint.setTextSize(13); idHint.setTextColor(0xFF8899aa);
            idHint.setPadding(0, 0, 0, 12);
            form.addView(idHint);
        } else {
            // Edit mode: show ID as read-only
            etId = addField(form, "词书ID（不可修改）");
            etId.setText(bookId);
            etId.setEnabled(false);
            etId.setBackgroundColor(0xFFF0F4F8);
        }

        etName = addField(form, "词书名称");

        TextView diffLabel = new TextView(this);
        diffLabel.setText("难度等级");
        diffLabel.setTextSize(14); diffLabel.setTextColor(0xFF4a5568);
        diffLabel.setPadding(0, 12, 0, 6);
        form.addView(diffLabel);
        spDifficulty = new Spinner(this);
        spDifficulty.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"初级", "中级", "高级"}));
        spDifficulty.setPadding(24, 14, 24, 14);
        android.graphics.drawable.GradientDrawable sbg = new android.graphics.drawable.GradientDrawable();
        sbg.setColor(0xFFFFFFFF);
        sbg.setStroke(2, 0xFFe2e8f0);
        sbg.setCornerRadius(24);
        spDifficulty.setBackground(sbg);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        slp.setMargins(0, 0, 0, 12);
        spDifficulty.setLayoutParams(slp);
        form.addView(spDifficulty);

        etDesc = addField(form, "词书简介");
        etDesc.setLines(3);

        btnSubmit = new Button(this);
        btnSubmit.setText(bookId != null ? "保存修改" : "创建词书");
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

        // If editing, load existing data
        if (bookId != null) loadBookData();
    }

    private EditText addField(LinearLayout parent, String label) {
        TextView lbl = new TextView(this);
        lbl.setText(label);
        lbl.setTextSize(14); lbl.setTextColor(0xFF4a5568);
        lbl.setPadding(0, 12, 0, 6);
        parent.addView(lbl);

        EditText et = new EditText(this);
        et.setPadding(24, 14, 24, 14);
        et.setTextSize(15);
        et.setSingleLine(true);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(0xFFFFFFFF);
        bg.setStroke(2, 0xFFe2e8f0);
        bg.setCornerRadius(24);
        et.setBackground(bg);
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ep.setMargins(0, 0, 0, 12);
        et.setLayoutParams(ep);
        parent.addView(et);
        return et;
    }

    private void loadBookData() {
        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().get("/api/wordbooks");
                if (res.get("code").getAsInt() == 200) {
                    var books = res.getAsJsonArray("data");
                    for (int i = 0; i < books.size(); i++) {
                        JsonObject b = books.get(i).getAsJsonObject();
                        if (bookId.equals(b.has("wordBookId") ? b.get("wordBookId").getAsString() : "")) {
                            runOnUiThread(() -> {
                                etName.setText(b.has("wordBookName") && !b.get("wordBookName").isJsonNull() ? b.get("wordBookName").getAsString() : "");
                                String diff = b.has("difficultyLevel") && !b.get("difficultyLevel").isJsonNull() ? b.get("difficultyLevel").getAsString() : "初级";
                                for (int j = 0; j < spDifficulty.getCount(); j++) {
                                    if (spDifficulty.getItemAtPosition(j).toString().equals(diff)) { spDifficulty.setSelection(j); break; }
                                }
                                etDesc.setText(b.has("wordBookDescription") && !b.get("wordBookDescription").isJsonNull() ? b.get("wordBookDescription").getAsString() : "");
                            });
                            return;
                        }
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void submit() {
        String name = etName.getText().toString().trim();
        String diff = spDifficulty.getSelectedItem().toString();
        String desc = etDesc.getText().toString().trim();

        if (name.isEmpty()) { Toast.makeText(this, "请填写词书名称", Toast.LENGTH_SHORT).show(); return; }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("提交中...");

        JsonObject body = new JsonObject();
        body.addProperty("wordBookName", name);
        body.addProperty("difficultyLevel", diff);
        body.addProperty("wordBookDescription", desc);

        new Thread(() -> {
            try {
                JsonObject res;
                if (bookId != null) {
                    res = ApiClient.get().put("/api/wordbooks/" + bookId, body);
                } else {
                    res = ApiClient.get().post("/api/wordbooks", body);
                }
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText(bookId != null ? "保存修改" : "创建词书");
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
