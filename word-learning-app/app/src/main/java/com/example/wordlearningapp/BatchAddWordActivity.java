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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.InputStream;
public class BatchAddWordActivity extends AppCompatActivity {

    private static final int PICK_EXCEL_FILE = 1001;

    private EditText etBatchInput;
    private Button btnSubmit;

    private LinearLayout textModeLayout, excelModeLayout;
    private TextView tvSelectedFile, tvExcelResult;
    private Uri selectedFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        topBar.setPadding(dp(16), statusBarH + dp(8), dp(16), dp(8));
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48) + statusBarH));

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
        barTitle.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        topBar.addView(barTitle);

        TextView placeholder = new TextView(this);
        placeholder.setLayoutParams(new LinearLayout.LayoutParams(dp(48), 1));
        topBar.addView(placeholder);
        root.addView(topBar);

        // ===== TOGGLE BUTTONS =====
        LinearLayout toggleRow = new LinearLayout(this);
        toggleRow.setOrientation(LinearLayout.HORIZONTAL);
        toggleRow.setPadding(dp(16), dp(12), dp(16), dp(4));

        TextView btnText = makeToggleBtn("文本输入", true);
        TextView btnExcel = makeToggleBtn("Excel导入", false);
        toggleRow.addView(btnText);
        toggleRow.addView(btnExcel);
        root.addView(toggleRow);

        // ===== TEXT MODE =====
        textModeLayout = new LinearLayout(this);
        textModeLayout.setOrientation(LinearLayout.VERTICAL);
        textModeLayout.setPadding(dp(16), dp(8), dp(16), dp(16));

        TextView hint = new TextView(this);
        hint.setText("每行一个单词，格式：拼写,词性,释义,音标,例句,音频链接,图片链接\n逗号分隔，词性及以后可选，单词ID自动生成");
        hint.setTextSize(13);
        hint.setTextColor(0xFF8899aa);
        hint.setPadding(0, 0, 0, dp(8));
        textModeLayout.addView(hint);

        etBatchInput = new EditText(this);
        etBatchInput.setHint("hello,v.,你好,/həˈloʊ/,Hello World!,http://audio.mp3,http://img.png");
        etBatchInput.setTextSize(14);
        etBatchInput.setPadding(dp(16), dp(12), dp(16), dp(12));
        etBatchInput.setBackgroundColor(0xFFf6f8fc);
        etBatchInput.setMinLines(8);
        etBatchInput.setGravity(Gravity.TOP);
        textModeLayout.addView(etBatchInput);

        btnSubmit = new Button(this);
        btnSubmit.setText("批量导入");
        btnSubmit.setTextColor(0xFFFFFFFF);
        btnSubmit.setTextSize(16);
        btnSubmit.setPadding(0, dp(14), 0, dp(14));
        btnSubmit.setBackgroundColor(0xFF318af8);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0, dp(16), 0, 0);
        btnSubmit.setLayoutParams(bp);
        btnSubmit.setOnClickListener(v -> doTextBatchAdd());
        textModeLayout.addView(btnSubmit);

        root.addView(textModeLayout);

        // ===== EXCEL MODE =====
        excelModeLayout = new LinearLayout(this);
        excelModeLayout.setOrientation(LinearLayout.VERTICAL);
        excelModeLayout.setPadding(dp(16), dp(8), dp(16), dp(16));
        excelModeLayout.setVisibility(View.GONE);

        TextView excelHint = new TextView(this);
        excelHint.setText("Excel模板（第一行为表头）:\nspelling, part_of_speech, definition, example_sentence, phonetic, pronunciation_url, image_url\n至少需要 spelling 和 definition 两列");
        excelHint.setTextSize(12);
        excelHint.setTextColor(0xFF8899aa);
        excelHint.setPadding(0, 0, 0, dp(12));
        excelModeLayout.addView(excelHint);

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
        btnPickFile.setOnClickListener(v -> openFilePicker());
        excelModeLayout.addView(btnPickFile);

        tvSelectedFile = new TextView(this);
        tvSelectedFile.setText("未选择文件");
        tvSelectedFile.setTextSize(13);
        tvSelectedFile.setTextColor(0xFF8899aa);
        tvSelectedFile.setPadding(0, dp(10), 0, dp(14));
        excelModeLayout.addView(tvSelectedFile);

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
        btnUpload.setOnClickListener(v -> doExcelImport());
        excelModeLayout.addView(btnUpload);

        tvExcelResult = new TextView(this);
        tvExcelResult.setTextSize(13);
        tvExcelResult.setTextColor(0xFF273245);
        tvExcelResult.setPadding(dp(12), dp(12), dp(12), dp(12));
        tvExcelResult.setBackgroundColor(0xFFFFFFFF);
        tvExcelResult.setVisibility(View.GONE);
        excelModeLayout.addView(tvExcelResult);

        root.addView(excelModeLayout);

        setContentView(root);

        // Toggle listeners
        btnText.setOnClickListener(v -> {
            textModeLayout.setVisibility(View.VISIBLE);
            excelModeLayout.setVisibility(View.GONE);
            btnText.setTextColor(0xFFFFFFFF);
            btnText.setBackgroundColor(0xFF318af8);
            btnExcel.setTextColor(0xFF318af8);
            btnExcel.setBackgroundColor(0xFFe8ecf1);
        });
        btnExcel.setOnClickListener(v -> {
            textModeLayout.setVisibility(View.GONE);
            excelModeLayout.setVisibility(View.VISIBLE);
            btnExcel.setTextColor(0xFFFFFFFF);
            btnExcel.setBackgroundColor(0xFF318af8);
            btnText.setTextColor(0xFF318af8);
            btnText.setBackgroundColor(0xFFe8ecf1);
        });

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
                tvExcelResult.setVisibility(View.GONE);
            }
        }
    }

    // ===== 文本模式批量导入 =====
    private void doTextBatchAdd() {
        String input = etBatchInput.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "请输入单词数据", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] lines = input.split("\n");
        JsonArray wordsArr = new JsonArray();
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",");
            if (parts.length < 3) continue;
            JsonObject word = new JsonObject();
            word.addProperty("englishSpelling", parts[0].trim());
            if (parts.length > 1) word.addProperty("partOfSpeech", parts[1].trim());
            if (parts.length > 2) word.addProperty("chineseDefinition", parts[2].trim());
            if (parts.length > 3) word.addProperty("exampleSentence", parts[3].trim());
            if (parts.length > 4) word.addProperty("phoneticSymbol", parts[4].trim());
            if (parts.length > 5) word.addProperty("wordPronunciation", parts[5].trim());
            if (parts.length > 6) word.addProperty("wordImage", parts[6].trim());
            wordsArr.add(word);
        }

        if (wordsArr.size() == 0) {
            Toast.makeText(this, "未解析到有效单词数据", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("导入中...");

        JsonObject body = new JsonObject();
        body.add("words", wordsArr);

        new Thread(() -> {
            try {
                JsonObject result = ApiClient.get().post("/api/words/batch", body);
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("批量导入");
                    if (result.get("code").getAsInt() == 200) {
                        JsonObject data = result.getAsJsonObject("data");
                        int nc = data.has("newCount") ? data.get("newCount").getAsInt() : 0;
                        int sc = data.has("skippedCount") ? data.get("skippedCount").getAsInt() : 0;
                        StringBuilder sb = new StringBuilder();
                        sb.append("新增 ").append(nc).append(" 个单词\n");
                        if (sc > 0) {
                            sb.append("跳过 ").append(sc).append(" 个已存在: ");
                            JsonArray sw = data.getAsJsonArray("skippedWords");
                            for (int i = 0; i < Math.min(sw.size(), 8); i++) {
                                if (i > 0) sb.append(", ");
                                sb.append(sw.get(i).getAsString());
                            }
                            if (sw.size() > 8) sb.append("...等");
                        }
                        Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, result.has("message") ? result.get("message").getAsString() : "导入失败", Toast.LENGTH_SHORT).show();
                    }
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

    // ===== Excel批量导入 =====
    private void doExcelImport() {
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

                java.util.Map<String, String> fields = new java.util.HashMap<>();

                JsonObject result = ApiClient.get().uploadFile(
                        "/api/words/batch/import", "file", "import.xlsx", is,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        fields);
                is.close();

                runOnUiThread(() -> {
                    pd.dismiss();
                    int code = result.has("code") ? result.get("code").getAsInt() : -1;
                    if (code == 200) {
                        JsonObject data = result.getAsJsonObject("data");
                        int newCount = data.has("newCount") ? data.get("newCount").getAsInt() : 0;
                        int skippedCount = data.has("skippedCount") ? data.get("skippedCount").getAsInt() : 0;
                        int failCount = data.has("failCount") ? data.get("failCount").getAsInt() : 0;

                        StringBuilder sb = new StringBuilder();
                        sb.append("导入完成！\n");
                        sb.append("新增单词: ").append(newCount).append("个\n");
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

                        tvExcelResult.setText(sb.toString());
                        tvExcelResult.setVisibility(View.VISIBLE);
                        tvExcelResult.setTextColor(0xFF273245);
                        Toast.makeText(this, "导入完成", Toast.LENGTH_SHORT).show();
                    } else {
                        String msg = result.has("message") ? result.get("message").getAsString() : "导入失败";
                        tvExcelResult.setText("导入失败: " + msg);
                        tvExcelResult.setVisibility(View.VISIBLE);
                        tvExcelResult.setTextColor(0xFFe37b4b);
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    pd.dismiss();
                    tvExcelResult.setText("导入失败: " + e.getMessage());
                    tvExcelResult.setVisibility(View.VISIBLE);
                    tvExcelResult.setTextColor(0xFFe37b4b);
                    Toast.makeText(this, "文件读取失败", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
