package com.example.wordlearningapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StudyRecordsActivity extends AppCompatActivity {

    private static final String TAG = "StudyRecordsActivity";

    private LinearLayout recordListContainer;
    private TextView tvStats;
    private Button btnStartDate;
    private Button btnEndDate;
    private Button btnFilter;
    private JsonArray allRecords;
    private Map<String, String> bookNameMap = new HashMap<>();
    private String startDate = "";
    private String endDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_records);

        // 初始化视图
        recordListContainer = findViewById(R.id.record_list_container);
        tvStats = findViewById(R.id.tv_stats);
        btnStartDate = findViewById(R.id.btn_start_date);
        btnEndDate = findViewById(R.id.btn_end_date);
        btnFilter = findViewById(R.id.btn_filter);

        // 初始化日期为默认值（不筛选）
        startDate = "";
        endDate = "";
        btnStartDate.setText("开始日期 ▼");
        btnEndDate.setText("结束日期 ▼");

        // 设置日期选择器点击事件
        btnStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(true);
            }
        });

        btnEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(false);
            }
        });

        // 设置筛选按钮点击事件
        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击筛选按钮后才加载筛选后的数据
                loadData();
            }
        });

        // 底部导航栏点击事件
        setupBottomNavigation();

        // 加载数据（默认显示所有记录）
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "页面恢复，刷新学习记录数据");
        loadData();
    }

    private void showDatePickerDialog(final boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();

        // 如果已经有日期，使用当前选择的日期；否则使用当前日期
        try {
            String currentDate = isStartDate ? startDate : endDate;
            if (!currentDate.isEmpty()) {
                String[] parts = currentDate.split("/");
                calendar.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
            }
        } catch (Exception e) {
            // 使用当前日期
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.CHINA, "%04d/%02d/%02d", year, month + 1, dayOfMonth);
                    if (isStartDate) {
                        startDate = selectedDate;
                        btnStartDate.setText(startDate);
                    } else {
                        endDate = selectedDate;
                        btnEndDate.setText(endDate);
                    }
                    // 不自动加载数据，等待用户点击筛选按钮
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void setupBottomNavigation() {
        TextView navHome = findViewById(R.id.nav_home);
        TextView navBooks = findViewById(R.id.nav_books);
        TextView navSearch = findViewById(R.id.nav_search);
        TextView navCollection = findViewById(R.id.nav_collection);
        TextView navRecords = findViewById(R.id.nav_records);
        TextView navProfile = findViewById(R.id.nav_profile);

        View.OnClickListener navClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(StudyRecordsActivity.this, MainActivity.class));
                } else if (id == R.id.nav_books) {
                    startActivity(new Intent(StudyRecordsActivity.this, WordBooksActivity.class));
                } else if (id == R.id.nav_search) {
                    startActivity(new Intent(StudyRecordsActivity.this, WordSearchActivity.class));
                } else if (id == R.id.nav_collection) {
                    startActivity(new Intent(StudyRecordsActivity.this, CollectionsActivity.class));
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(StudyRecordsActivity.this, ProfileActivity.class));
                }
            }
        };

        navHome.setOnClickListener(navClickListener);
        navBooks.setOnClickListener(navClickListener);
        navSearch.setOnClickListener(navClickListener);
        navCollection.setOnClickListener(navClickListener);
        navProfile.setOnClickListener(navClickListener);

        // 当前页面不设置点击事件
    }

    private void loadData() {
        new Thread(() -> {
            try {
                Log.d(TAG, "开始加载学习记录数据");

                // 获取学习记录
                JsonObject res = ApiClient.get().get("/api/records/list");
                if (res.get("code").getAsInt() == 200) {
                    JsonArray data = res.getAsJsonArray("data");
                    allRecords = data;
                    Log.d(TAG, "学习记录数量: " + data.size());

                    // 获取词书列表，建立词书ID到名称的映射
                    JsonObject booksRes = ApiClient.get().get("/api/wordbooks");
                    if (booksRes.get("code").getAsInt() == 200) {
                        JsonArray books = booksRes.getAsJsonArray("data");
                        for (int i = 0; i < books.size(); i++) {
                            JsonObject book = books.get(i).getAsJsonObject();
                            String bookId = book.has("wordBookId") ? book.get("wordBookId").getAsString() : "";
                            String bookName = book.has("wordBookName") ? book.get("wordBookName").getAsString() : "";
                            if (!bookId.isEmpty()) {
                                bookNameMap.put(bookId, bookName);
                            }
                        }
                    }

                    // 先过滤原始数据（应用日期筛选）
                    List<JsonObject> filteredData = new ArrayList<>();
                    for (int i = 0; i < data.size(); i++) {
                        JsonObject r = data.get(i).getAsJsonObject();
                        String date = r.has("learningDate") ? r.get("learningDate").getAsString() : "";

                        // 应用日期筛选（只在日期不为空时才筛选）
                        if (!startDate.isEmpty() && date.compareTo(startDate.replace("/", "-")) < 0) {
                            continue;
                        }
                        if (!endDate.isEmpty() && date.compareTo(endDate.replace("/", "-")) > 0) {
                            continue;
                        }

                        filteredData.add(r);
                    }

                    // 按日期 + 词书ID聚合
                    Map<String, JsonObject> agg = new java.util.LinkedHashMap<>();
                    for (JsonObject r : filteredData) {
                        String date = r.has("learningDate") ? r.get("learningDate").getAsString() : "";
                        String bookId = r.has("learnedWordBookId") ? r.get("learnedWordBookId").getAsString() : "unknown";
                        String bookName = r.has("learnedWordBookName") ? r.get("learnedWordBookName").getAsString() : "未知词书";

                        String key = date + "|" + bookId;

                        if (!agg.containsKey(key)) {
                            JsonObject entry = new JsonObject();
                            entry.addProperty("date", date);
                            entry.addProperty("bookId", bookId);
                            entry.addProperty("bookName", bookNameMap.getOrDefault(bookId, bookName));
                            entry.addProperty("count", 1);
                            agg.put(key, entry);
                        } else {
                            JsonObject entry = agg.get(key);
                            entry.addProperty("count", entry.get("count").getAsInt() + 1);
                        }
                    }

                    List<JsonObject> list = new ArrayList<>(agg.values());
                    // 按日期降序排序
                    java.util.Collections.sort(list, (a, b) ->
                            b.get("date").getAsString().compareTo(a.get("date").getAsString()));

                    // 计算统计信息（基于筛选后的数据）
                    int totalWords = filteredData.size(); // 筛选后的总单词数

                    // 从筛选后的数据中计算唯一学习天数
                    int uniqueDays = (int) filteredData.stream()
                            .map(e -> e.get("learningDate").getAsString())
                            .distinct()
                            .count();

                    // 使用四舍五入计算平均值
                    int avgDaily = uniqueDays > 0 ? (int) Math.round((double) totalWords / uniqueDays) : 0;

                    // 使用final变量
                    final int finalTotalWords = totalWords;
                    final int finalAvgDaily = avgDaily;
                    final List<JsonObject> finalList = list;

                    runOnUiThread(() -> {
                        tvStats.setText("总学习单词数：" + finalTotalWords + " ｜ 平均每日学习数：" + finalAvgDaily);
                        displayRecords(finalList);
                    });
                } else {
                    runOnUiThread(() -> {
                        tvStats.setText("总学习单词数：0 ｜ 平均每日学习数：0");
                        displayRecords(new ArrayList<>());
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "加载异常: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    tvStats.setText("总学习单词数：0 ｜ 平均每日学习数：0");
                    displayRecords(new ArrayList<>());
                    Toast.makeText(this, "加载失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void displayRecords(List<JsonObject> records) {
        recordListContainer.removeAllViews();

        if (records.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("暂无学习记录");
            emptyText.setTextSize(16);
            emptyText.setTextColor(0xFF999999);
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(0, dp(40), 0, dp(40));
            recordListContainer.addView(emptyText);
            return;
        }

        // 使用TableLayout确保严格对齐
        TableLayout table = new TableLayout(this);
        table.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // 设置列宽权重：日期1.0、词书1.4（增加）、数量0.8、时长0.8、占位0.1（最小化）
        table.setColumnStretchable(0, true);  // 日期列
        table.setColumnStretchable(1, true);  // 词书列

        // 表头行
        TableRow headerRow = new TableRow(this);
        headerRow.setPadding(dp(8), dp(12), dp(8), dp(12));
        headerRow.setBackground(getDrawable(R.drawable.bg_white_card));
        headerRow.setGravity(Gravity.CENTER);

        headerRow.addView(createHeaderCell("日期", 1.0f));
        headerRow.addView(createHeaderCell("词书", 1.4f));
        headerRow.addView(createHeaderCell("数量", 0.8f));
        headerRow.addView(createHeaderCell("时长", 0.8f));
        headerRow.addView(createSpacerView(0.1f));

        table.addView(headerRow);

        // 数据行
        for (int i = 0; i < records.size(); i++) {
            JsonObject record = records.get(i);

            final String date = record.has("date") ? record.get("date").getAsString() : "";
            // 显示完整日期：YYYY-MM-DD（年-月-日）
            final String fullDate = date;

            // 完整显示词书名称
            String bookName = record.has("bookName") ? record.get("bookName").getAsString() : "未知词书";

            int count = record.has("count") ? record.get("count").getAsInt() : 0;

            TableRow dataRow = new TableRow(this);
            dataRow.setPadding(dp(8), dp(12), dp(8), dp(12));
            dataRow.setBackground(getDrawable(R.drawable.bg_white_card));
            dataRow.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, dp(6), 0, 0);
            dataRow.setLayoutParams(rowParams);

            dataRow.addView(createDataCell(fullDate, 1.0f));
            dataRow.addView(createDataCell(bookName, 1.4f));
            dataRow.addView(createDataCell(String.valueOf(count), 0.8f));
            dataRow.addView(createDataCell((count * 3) + "min", 0.8f));
            dataRow.addView(createSpacerView(0.1f));

            table.addView(dataRow);
        }

        recordListContainer.addView(table);
    }

    private TextView createHeaderCell(String text, float weight) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(14);
        tv.setTextColor(0xFF318af8);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(4), dp(10), dp(4), dp(10));
        tv.setSingleLine(true);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, weight);
        tv.setLayoutParams(params);
        return tv;
    }

    private TextView createDataCell(String text, float weight) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(14);
        tv.setTextColor(0xFF318af8);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(4), dp(10), dp(4), dp(10));
        tv.setSingleLine(true);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, weight);
        tv.setLayoutParams(params);
        return tv;
    }

    // 权重占位View
    private View createSpacerView(float weight) {
        View spacer = new View(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, 1, weight);
        spacer.setLayoutParams(params);
        return spacer;
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
