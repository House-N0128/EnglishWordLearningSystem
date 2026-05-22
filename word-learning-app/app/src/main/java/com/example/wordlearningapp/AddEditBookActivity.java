package com.example.wordlearningapp;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonObject;

public class AddEditBookActivity extends AppCompatActivity {

    private EditText etName, etDesc;
    private Spinner spDifficulty, spStatus;
    private Button btnSubmit;
    private String bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bookId = getIntent().getStringExtra("bookId");
        boolean isEdit = bookId != null;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFf2f8fc);

        // ===== TOP BAR =====
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
        barTitle.setText(isEdit ? "编辑词书" : "新增词书");
        barTitle.setTextSize(18);
        barTitle.setTextColor(0xFFFFFFFF);
        barTitle.setTypeface(null, Typeface.BOLD);
        topBar.addView(barTitle);
        root.addView(topBar);

        // ===== FORM =====
        ScrollView scroll = new ScrollView(this);
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(16), dp(16), dp(16), dp(20));

        TextView formTitle = new TextView(this);
        formTitle.setText(isEdit ? "修改词书信息" : "新建词书");
        formTitle.setTextSize(19);
        formTitle.setTextColor(0xFF318af8);
        formTitle.setTypeface(null, Typeface.BOLD);
        formTitle.setPadding(0, 0, 0, dp(14));
        form.addView(formTitle);

        // Name
        addLabel(form, "词书名称");
        etName = addInput(form, "请输入词书名称");

        // Description
        addLabel(form, "词书描述");
        etDesc = addInput(form, "请输入词书描述");
        etDesc.setLines(3);
        etDesc.setMinHeight(dp(70));
        etDesc.setGravity(android.view.Gravity.TOP);

        // Difficulty
        addLabel(form, "难度等级");
        spDifficulty = new Spinner(this);
        spDifficulty.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"请选择", "初级", "中级", "高级"}));
        styleSpinner(spDifficulty);
        form.addView(spDifficulty);

        // Status
        addLabel(form, "状态");
        spStatus = new Spinner(this);
        spStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"请选择", "已上架", "未上架"}));
        styleSpinner(spStatus);
        form.addView(spStatus);

        // Save button
        btnSubmit = new Button(this);
        btnSubmit.setText(isEdit ? "保存修改" : "创建词书");
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

        if (isEdit) loadBookData();
    }

    private void addLabel(LinearLayout parent, String text) {
        TextView lbl = new TextView(this);
        lbl.setText(text);
        lbl.setTextSize(14);
        lbl.setTextColor(0xFF318af8);
        lbl.setTypeface(null, Typeface.BOLD);
        lbl.setPadding(0, dp(12), 0, dp(4));
        parent.addView(lbl);
    }

    private EditText addInput(LinearLayout parent, String hint) {
        EditText et = new EditText(this);
        et.setHint(hint);
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

    private void styleSpinner(Spinner sp) {
        sp.setPadding(dp(10), dp(12), dp(10), dp(12));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFFf6f8fc);
        bg.setCornerRadius(dp(8));
        bg.setStroke(1, 0xFFc7d9ee);
        sp.setBackground(bg);
        LinearLayout.LayoutParams spl = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        spl.setMargins(0, 0, 0, dp(12));
        sp.setLayoutParams(spl);
    }

    private void loadBookData() {
        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().get("/api/admin/wordbooks");
                if (res.get("code").getAsInt() == 200) {
                    var books = res.getAsJsonArray("data");
                    for (int i = 0; i < books.size(); i++) {
                        JsonObject b = books.get(i).getAsJsonObject();
                        if (bookId.equals(b.has("wordBookId") ? b.get("wordBookId").getAsString() : "")) {
                            runOnUiThread(() -> {
                                etName.setText(getStr(b, "wordBookName"));
                                etDesc.setText(getStr(b, "wordBookDescription"));
                                select(spDifficulty, getStr(b, "difficultyLevel"));
                                select(spStatus, getStr(b, "wordBookStatus"));
                            });
                            return;
                        }
                    }
                }
            } catch (Exception e) {}
        }).start();
    }

    private String getStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    private void select(Spinner sp, String value) {
        if (value.isEmpty()) return;
        for (int i = 0; i < sp.getCount(); i++) {
            if (sp.getItemAtPosition(i).toString().equals(value)) {
                sp.setSelection(i);
                return;
            }
        }
    }

    private void submit() {
        String name = etName.getText().toString().trim();
        String diff = spDifficulty.getSelectedItem() != null ? spDifficulty.getSelectedItem().toString() : "";
        String status = spStatus.getSelectedItem() != null ? spStatus.getSelectedItem().toString() : "";
        String desc = etDesc.getText().toString().trim();

        if (name.isEmpty()) { Toast.makeText(this, "请填写词书名称", Toast.LENGTH_SHORT).show(); return; }
        if ("请选择".equals(diff)) { Toast.makeText(this, "请选择难度等级", Toast.LENGTH_SHORT).show(); return; }
        if ("请选择".equals(status)) { Toast.makeText(this, "请选择状态", Toast.LENGTH_SHORT).show(); return; }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("提交中...");

        JsonObject body = new JsonObject();
        body.addProperty("wordBookName", name);
        body.addProperty("difficultyLevel", diff);
        body.addProperty("wordBookDescription", desc);
        body.addProperty("wordBookStatus", status);

        boolean isEdit = bookId != null;
        new Thread(() -> {
            try {
                JsonObject res;
                if (isEdit) {
                    res = ApiClient.get().put("/api/wordbooks/" + bookId, body);
                } else {
                    res = ApiClient.get().post("/api/wordbooks", body);
                }
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText(isEdit ? "保存修改" : "创建词书");
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

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
