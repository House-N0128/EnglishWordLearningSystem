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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

        recordListContainer = findViewById(R.id.record_list_container);
        tvStats = findViewById(R.id.tv_stats);
        btnStartDate = findViewById(R.id.btn_start_date);
        btnEndDate = findViewById(R.id.btn_end_date);
        btnFilter = findViewById(R.id.btn_filter);

        startDate = "";
        endDate = "";
        btnStartDate.setText("开始日期 ▼");
        btnEndDate.setText("结束日期 ▼");

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

        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });

        setupBottomNavigation();

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

        try {
            String currentDate = isStartDate ? startDate : endDate;
            if (!currentDate.isEmpty()) {
                String[] parts = currentDate.split("/");
                calendar.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
            }
        } catch (Exception e) {
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
    }

    private void loadData() {
        new Thread(() -> {
            try {
                Log.d(TAG, "开始加载学习记录数据");

                JsonObject res = ApiClient.get().get("/api/records/list");
                if (res.get("code").getAsInt() == 200) {
                    JsonArray data = res.getAsJsonArray("data");
                    allRecords = data;
                    Log.d(TAG, "学习记录数量: " + data.size());

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

                    List<JsonObject> filteredData = new ArrayList<>();
                    for (int i = 0; i < data.size(); i++) {
                        JsonObject r = data.get(i).getAsJsonObject();
                        String date = r.has("learningDate") ? r.get("learningDate").getAsString() : "";

                        if (!startDate.isEmpty() && date.compareTo(startDate.replace("/", "-")) < 0) {
                            continue;
                        }
                        if (!endDate.isEmpty() && date.compareTo(endDate.replace("/", "-")) > 0) {
                            continue;
                        }

                        filteredData.add(r);
                    }

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
                    java.util.Collections.sort(list, (a, b) ->
                            b.get("date").getAsString().compareTo(a.get("date").getAsString()));

                    int totalRecords = filteredData.size();

                    Set<String> uniqueWordIds = new HashSet<>();
                    for (JsonObject record : filteredData) {
                        String wordId = "";
                        if (record.has("wordId") && !record.get("wordId").isJsonNull()) {
                            wordId = record.get("wordId").getAsString();
                        } else if (record.has("learnedWordId") && !record.get("learnedWordId").isJsonNull()) {
                            wordId = record.get("learnedWordId").getAsString();
                        } else if (record.has("id") && !record.get("id").isJsonNull()) {
                            wordId = record.get("id").getAsString();
                        }

                        if (!wordId.isEmpty()) {
                            uniqueWordIds.add(wordId);
                        }
                    }

                    int totalWords = uniqueWordIds.size();

                    int uniqueDays = (int) filteredData.stream()
                            .map(e -> e.get("learningDate").getAsString())
                            .distinct()
                            .count();

                    int avgDaily = uniqueDays > 0 ? (int) Math.round((double) totalWords / uniqueDays) : 0;

                    final int finalTotalWords = totalWords;
                    final int finalAvgDaily = avgDaily;
                    final List<JsonObject> finalList = list;

                    Log.d(TAG, "=== 统计信息 ===");
                    Log.d(TAG, "总记录数: " + totalRecords);
                    Log.d(TAG, "不重复单词数: " + totalWords);
                    Log.d(TAG, "唯一学习天数: " + uniqueDays);
                    Log.d(TAG, "平均每日学习: " + avgDaily);

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

        TableLayout table = new TableLayout(this);
        table.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        table.setColumnStretchable(0, true);
        table.setColumnStretchable(1, true);

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

        for (int i = 0; i < records.size(); i++) {
            JsonObject record = records.get(i);

            final String date = record.has("date") ? record.get("date").getAsString() : "";
            final String fullDate = date;

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
            dataRow.addView(createDataCell((count * 2) + "min", 0.8f));
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
