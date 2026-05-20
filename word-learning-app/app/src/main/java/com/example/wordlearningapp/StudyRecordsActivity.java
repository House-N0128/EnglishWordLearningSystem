package com.example.wordlearningapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudyRecordsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty, tvStats;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ((TextView) findViewById(R.id.toolbar_title)).setText("学习记录");
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());
        tvEmpty = findViewById(R.id.tv_empty);
        progress = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tvStats = new TextView(this);
        tvStats.setTextSize(15);
        tvStats.setTextColor(0xFF318af8);
        tvStats.setPadding(14, 14, 14, 14);
        ((android.view.ViewGroup) findViewById(android.R.id.content)).addView(tvStats, 1);

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            try {
                // 获取学习记录
                JsonObject res = ApiClient.get().get("/api/records/list");
                if (res.get("code").getAsInt() == 200) {
                    JsonArray data = res.getAsJsonArray("data");

                    // 获取词书列表，建立词书ID到名称的映射
                    JsonObject booksRes = ApiClient.get().get("/api/wordbooks");
                    Map<String, String> bookNameMap = new HashMap<>();
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

                    // 按日期 + 词书ID聚合
                    Map<String, JsonObject> agg = new java.util.LinkedHashMap<>();
                    for (int i = 0; i < data.size(); i++) {
                        JsonObject r = data.get(i).getAsJsonObject();
                        String date = r.has("learningDate") ? r.get("learningDate").getAsString() : "";
                        String bookId = r.has("learnedWordBookId") ? r.get("learnedWordBookId").getAsString() : "unknown";
                        String key = date + "|" + bookId;

                        if (!agg.containsKey(key)) {
                            JsonObject entry = new JsonObject();
                            entry.addProperty("date", date);
                            entry.addProperty("bookId", bookId);
                            entry.addProperty("bookName", bookNameMap.getOrDefault(bookId, "未知词书"));
                            entry.addProperty("count", 1);
                            agg.put(key, entry);
                        } else {
                            JsonObject entry = agg.get(key);
                            entry.addProperty("count", entry.get("count").getAsInt() + 1);
                        }
                    }

                    List<JsonObject> list = new ArrayList<>(agg.values());
                    // Sort descending by date
                    java.util.Collections.sort(list, (a, b) ->
                            b.get("date").getAsString().compareTo(a.get("date").getAsString()));

                    runOnUiThread(() -> {
                        progress.setVisibility(View.GONE);
                        tvStats.setText("总学习记录: " + data.size() + " 条");
                        if (list.isEmpty()) {
                            tvEmpty.setText("暂无学习记录");
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            JsonArray arr = new JsonArray();
                            list.forEach(arr::add);
                            recyclerView.setAdapter(new RecordAdapter(arr));
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> { progress.setVisibility(View.GONE); tvEmpty.setText("加载失败"); tvEmpty.setVisibility(View.VISIBLE); });
            }
        }).start();
    }

    private class RecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final JsonArray data;
        RecordAdapter(JsonArray data) { this.data = data; }

        @Override public RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
            return new RecyclerView.ViewHolder(v) {};
        }

        @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int pos) {
            JsonObject r = data.get(pos).getAsJsonObject();
            String date = r.has("date") ? r.get("date").getAsString() : "";
            String bookName = r.has("bookName") ? r.get("bookName").getAsString() : "未知词书";
            int count = r.has("count") ? r.get("count").getAsInt() : 0;

            ((TextView) holder.itemView.findViewById(android.R.id.text1)).setText(date + "  ｜  " + count + "词");
            ((TextView) holder.itemView.findViewById(android.R.id.text2)).setText(bookName);
        }

        @Override public int getItemCount() { return data.size(); }
    }
}
