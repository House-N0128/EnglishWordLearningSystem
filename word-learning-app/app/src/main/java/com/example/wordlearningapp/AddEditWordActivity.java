package com.example.wordlearningapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
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

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.InputStream;

public class AddEditWordActivity extends AppCompatActivity {

    private static final int PICK_EXCEL_FILE = 1001;

    private EditText etSpelling, etPhonetic, etPos, etChinese, etExample;
    private Button btnSubmit;
    private TextView tvTitle;
    private String wordId, bookId;

    private LinearLayout toggleRow, singleModeWrap, batchModeWrap;
    private TextView tvSelectedFile, tvBatchResult;
    private Uri selectedFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wordId = getIntent().getStringExtra("wordId");
        bookId = getIntent().getStringExtra("bookId");
        boolean isEdit = wordId != null;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFf2f8fc);

        // ===== TOP BAR =====
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
        tvTitle.setText(isEdit ? "编辑单词" : "添加单词");
        tvTitle.setTextSize(18);
        tvTitle.setTextColor(0xFFFFFFFF);
        tvTitle.setTypeface(null, Typeface.BOLD);
        topBar.addView(tvTitle);
        root.addView(topBar);

        ScrollView scroll = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        // ===== TOGGLE (only when adding from book management, not word management) =====
        if (!isEdit && bookId != null) {
            toggleRow = new LinearLayout(this);
            toggleRow.setOrientation(LinearLayout.HORIZONTAL);
            toggleRow.setPadding(dp(12), dp(12), dp(12), dp(4));

            TextView btnSingle = makeToggleBtn("单个添加单词", true);
            TextView btnBatch = makeToggleBtn("批量导入", false);

            toggleRow.addView(btnSingle);
            toggleRow.addView(btnBatch);
            content.addView(toggleRow);

            // Book info hint
            if (bookId != null) {
                TextView hint = new TextView(this);
                hint.setText("目标词书: " + bookId);
                hint.setTextSize(13);
                hint.setTextColor(0xFF318af8);
                hint.setPadding(dp(14), dp(4), dp(14), dp(6));
                content.addView(hint);
            }

            // ---- SINGLE MODE WRAPPER ----
            singleModeWrap = new LinearLayout(this);
            singleModeWrap.setOrientation(LinearLayout.VERTICAL);
            singleModeWrap.setPadding(dp(16), dp(8), dp(16), dp(20));

            buildSingleMode(singleModeWrap);
            content.addView(singleModeWrap);

            // ---- BATCH MODE WRAPPER ----
            batchModeWrap = new LinearLayout(this);
            batchModeWrap.setOrientation(LinearLayout.VERTICAL);
            batchModeWrap.setPadding(dp(16), dp(8), dp(16), dp(20));
            batchModeWrap.setVisibility(View.GONE);

            buildBatchMode(batchModeWrap);
            content.addView(batchModeWrap);

            // Toggle listeners
            btnSingle.setOnClickListener(v -> {
                singleModeWrap.setVisibility(View.VISIBLE);
                batchModeWrap.setVisibility(View.GONE);
                btnSingle.setTextColor(0xFFFFFFFF);
                btnSingle.setBackgroundColor(0xFF318af8);
                btnBatch.setTextColor(0xFF318af8);
                btnBatch.setBackgroundColor(0xFFe8ecf1);
            });
            btnBatch.setOnClickListener(v -> {
                singleModeWrap.setVisibility(View.GONE);
                batchModeWrap.setVisibility(View.VISIBLE);
                btnBatch.setTextColor(0xFFFFFFFF);
                btnBatch.setBackgroundColor(0xFF318af8);
                btnSingle.setTextColor(0xFF318af8);
                btnSingle.setBackgroundColor(0xFFe8ecf1);
            });
        } else {
            // Edit mode or add from word management (no bookId): just the form
            LinearLayout formWrap = new LinearLayout(this);
            formWrap.setOrientation(LinearLayout.VERTICAL);
            formWrap.setPadding(dp(16), dp(8), dp(16), dp(20));
            buildSingleMode(formWrap);
            if (isEdit) {
                btnSubmit.setText("保存修改");
            }
            content.addView(formWrap);
        }

        scroll.addView(content);
        root.addView(scroll);
        setContentView(root);

        if (isEdit) loadWordData();
    }

    private TextView makeToggleBtn(String text, boolean active) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextSize(14);
        btn.setTextColor(active ? 0xFFFFFFFF : 0xFF318af8);
        btn.setBackgroundColor(active ? 0xFF318af8 : 0xFFe8ecf1);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(0, dp(10), 0, dp(10));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(active ? 0xFF318af8 : 0xFFe8ecf1);
        bg.setCornerRadius(dp(20));
        btn.setBackground(bg);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        bp.setMargins(0, 0, dp(6), 0);
        btn.setLayoutParams(bp);
        return btn;
    }

    private void buildSingleMode(LinearLayout parent) {
        etSpelling = addField(parent, "英文拼写 *");
        etPhonetic = addField(parent, "音标");
        etPos = addField(parent, "词性 (如: n. / v. / adj.)");
        etChinese = addField(parent, "中文释义 *");
        etExample = addField(parent, "例句");
        etExample.setLines(2);
        etExample.setMinHeight(dp(60));
        etExample.setGravity(Gravity.TOP);

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
        sbp.setMargins(0, dp(16), 0, 0);
        btnSubmit.setLayoutParams(sbp);
        btnSubmit.setOnClickListener(v -> submit());
        parent.addView(btnSubmit);
    }

    private void buildBatchMode(LinearLayout parent) {
        // Template hint
        TextView templateHint = new TextView(this);
        templateHint.setText("Excel模板（第一行为表头，至少需要spelling和definition两列）:\nspelling, definition, example_sentence, phonetic, pronunciation_url, image_url");
        templateHint.setTextSize(12);
        templateHint.setTextColor(0xFF8899aa);
        templateHint.setPadding(0, 0, 0, dp(12));
        parent.addView(templateHint);

        // File picker
        TextView btnPickFile = new TextView(this);
        btnPickFile.setText("选择Excel文件（.xlsx / .xls）");
        btnPickFile.setTextSize(15);
        btnPickFile.setTextColor(0xFFFFFFFF);
        btnPickFile.setGravity(Gravity.CENTER);
        btnPickFile.setPadding(0, dp(13), 0, dp(13));
        GradientDrawable pickBg = new GradientDrawable();
        pickBg.setColor(0xFF318af8);
        pickBg.setCornerRadius(dp(22));
        btnPickFile.setBackground(pickBg);
        LinearLayout.LayoutParams pfp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pfp.setMargins(0, 0, 0, dp(10));
        btnPickFile.setLayoutParams(pfp);
        btnPickFile.setOnClickListener(v -> openFilePicker());
        parent.addView(btnPickFile);

        tvSelectedFile = new TextView(this);
        tvSelectedFile.setText("未选择文件");
        tvSelectedFile.setTextSize(13);
        tvSelectedFile.setTextColor(0xFF8899aa);
        tvSelectedFile.setPadding(0, 0, 0, dp(14));
        parent.addView(tvSelectedFile);

        // Upload button
        TextView btnUpload = new TextView(this);
        btnUpload.setText("开始导入");
        btnUpload.setTextSize(16);
        btnUpload.setTextColor(0xFFFFFFFF);
        btnUpload.setGravity(Gravity.CENTER);
        btnUpload.setPadding(0, dp(14), 0, dp(14));
        GradientDrawable upBg = new GradientDrawable();
        upBg.setColors(new int[]{0xFF3577ef, 0xFF6cc3ff});
        upBg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        upBg.setCornerRadius(dp(25));
        btnUpload.setBackground(upBg);
        LinearLayout.LayoutParams ubp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ubp.setMargins(0, 0, 0, dp(12));
        btnUpload.setLayoutParams(ubp);
        btnUpload.setOnClickListener(v -> doBatchImport());
        parent.addView(btnUpload);

        // Result area
        tvBatchResult = new TextView(this);
        tvBatchResult.setTextSize(13);
        tvBatchResult.setTextColor(0xFF273245);
        tvBatchResult.setPadding(dp(12), dp(12), dp(12), dp(12));
        tvBatchResult.setBackgroundColor(0xFFFFFFFF);
        tvBatchResult.setVisibility(View.GONE);
        parent.addView(tvBatchResult);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-excel"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, PICK_EXCEL_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_EXCEL_FILE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                String name = selectedFileUri.getLastPathSegment();
                if (name == null) name = "已选择文件";
                tvSelectedFile.setText("已选择: " + name);
                tvSelectedFile.setTextColor(0xFF25cb75);
                tvBatchResult.setVisibility(View.GONE);
            }
        }
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

    private void doBatchImport() {
        if (selectedFileUri == null) {
            Toast.makeText(this, "请先选择Excel文件", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("正在导入...");
        pd.setCancelable(false);
        pd.show();

        new Thread(() -> {
            try {
                InputStream is = getContentResolver().openInputStream(selectedFileUri);
                String fileName = "import.xlsx";

                java.util.Map<String, String> fields = new java.util.HashMap<>();
                if (bookId != null) fields.put("wordBookId", bookId);

                JsonObject result = ApiClient.get().uploadFile(
                        "/api/words/batch/import", "file", fileName, is,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        fields);
                is.close();

                runOnUiThread(() -> {
                    pd.dismiss();
                    int code = result.has("code") ? result.get("code").getAsInt() : -1;
                    if (code == 200) {
                        JsonObject data = result.getAsJsonObject("data");
                        int newCount = data.has("newCount") ? data.get("newCount").getAsInt() : 0;
                        int linkedCount = data.has("linkedCount") ? data.get("linkedCount").getAsInt() : 0;
                        int skippedCount = data.has("skippedCount") ? data.get("skippedCount").getAsInt() : 0;
                        int failCount = data.has("failCount") ? data.get("failCount").getAsInt() : 0;

                        StringBuilder sb = new StringBuilder();
                        sb.append("导入完成！\n");
                        sb.append("新增单词: ").append(newCount).append("个\n");
                        if (bookId != null) {
                            sb.append("关联已有单词: ").append(linkedCount).append("个\n");
                        }
                        if (skippedCount > 0) {
                            sb.append("跳过(已存在): ").append(skippedCount).append("个\n");
                            if (data.has("skippedWords")) {
                                JsonArray sw = data.getAsJsonArray("skippedWords");
                                if (sw.size() > 0) {
                                    sb.append("跳过的单词: ");
                                    for (int i = 0; i < Math.min(sw.size(), 10); i++) {
                                        if (i > 0) sb.append(", ");
                                        sb.append(sw.get(i).getAsString());
                                    }
                                    if (sw.size() > 10) sb.append("...等");
                                    sb.append("\n");
                                }
                            }
                        }
                        if (failCount > 0) sb.append("失败: ").append(failCount).append("个");

                        tvBatchResult.setText(sb.toString());
                        tvBatchResult.setVisibility(View.VISIBLE);
                        tvBatchResult.setTextColor(0xFF273245);
                        Toast.makeText(this, "导入完成", Toast.LENGTH_SHORT).show();
                    } else {
                        String msg = result.has("message") ? result.get("message").getAsString() : "导入失败";
                        tvBatchResult.setText("导入失败: " + msg);
                        tvBatchResult.setVisibility(View.VISIBLE);
                        tvBatchResult.setTextColor(0xFFe37b4b);
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    pd.dismiss();
                    tvBatchResult.setText("导入失败: " + e.getMessage());
                    tvBatchResult.setVisibility(View.VISIBLE);
                    tvBatchResult.setTextColor(0xFFe37b4b);
                    Toast.makeText(this, "文件读取失败", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
