package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TodayWordsActivity extends AppCompatActivity {

    private static final String TAG = "TodayWordsActivity";

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ((TextView) findViewById(R.id.toolbar_title)).setText("今日学习单词");
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());
        tvEmpty = findViewById(R.id.tv_empty);
        progress = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadData();
    }

    private void loadData() {
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                Log.d(TAG, "=== 开始加载今日学习单词 ===");
                Log.d(TAG, "调用接口: /api/records/today");

                JsonObject res = ApiClient.get().get("/api/records/today");
                Log.d(TAG, "=== API响应 ===");
                Log.d(TAG, "完整响应: " + res.toString());
                Log.d(TAG, "是否有code字段: " + res.has("code"));

                if (res.has("code") && res.get("code").getAsInt() == 200) {
                    JsonElement dataElement = res.get("data");
                    Log.d(TAG, "data字段类型: " + dataElement.getClass().getSimpleName());
                    Log.d(TAG, "data字段内容: " + dataElement.toString());

                    JsonArray data;
                    if (dataElement.isJsonArray()) {
                        data = dataElement.getAsJsonArray();
                    } else {
                        Log.e(TAG, "data不是数组类型");
                        runOnUiThread(() -> {
                            progress.setVisibility(View.GONE);
                            tvEmpty.setText("数据格式异常");
                            tvEmpty.setVisibility(View.VISIBLE);
                        });
                        return;
                    }

                    Log.d(TAG, "今日学习单词数量: " + data.size());
                    runOnUiThread(() -> {
                        progress.setVisibility(View.GONE);
                        if (data.size() == 0) {
                            tvEmpty.setText("今日暂无学习记录");
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setAdapter(new WordAdapter(data));
                        }
                    });
                } else {
                    final String errorMsg;
                    if (res.has("message")) {
                        errorMsg = res.get("message").getAsString();
                    } else if (res.has("code")) {
                        errorMsg = "加载失败 (code: " + res.get("code").getAsInt() + ")";
                    } else {
                        errorMsg = "加载失败";
                    }
                    Log.e(TAG, errorMsg);
                    Log.e(TAG, "完整响应: " + res.toString());
                    runOnUiThread(() -> {
                        progress.setVisibility(View.GONE);
                        tvEmpty.setText(errorMsg);
                        tvEmpty.setVisibility(View.VISIBLE);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "=== 加载异常 ===");
                Log.e(TAG, "异常类型: " + e.getClass().getSimpleName());
                Log.e(TAG, "异常消息: " + e.getMessage());
                Log.e(TAG, "异常堆栈:", e);
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    tvEmpty.setText("加载失败: " + e.getMessage());
                    tvEmpty.setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }

    private class WordAdapter extends RecyclerView.Adapter<WordAdapter.VH> {
        private JsonArray data;

        WordAdapter(JsonArray data) {
            this.data = data;
        }

        @Override
        public WordAdapter.VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_word, parent, false);
            return new WordAdapter.VH(view);
        }

        @Override
        public void onBindViewHolder(WordAdapter.VH holder, int pos) {
            JsonElement element = data.get(pos);
            if (!element.isJsonObject()) return;

            JsonObject w = element.getAsJsonObject();
            String spelling = w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "";
            String definition = w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "";
            String wordId = w.has("wordId") ? w.get("wordId").getAsString() : "";

            holder.tvWord.setText(spelling);
            holder.tvDefinition.setText(definition);

            holder.itemView.setOnClickListener(v -> {
                if (wordId != null && !wordId.isEmpty()) {
                    Intent intent = new Intent(TodayWordsActivity.this, WordDetailActivity.class);
                    intent.putExtra("wordId", wordId);
                    intent.putExtra("fromCollection", false);
                    startActivity(intent);
                } else {
                    Toast.makeText(TodayWordsActivity.this, "单词ID无效", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvWord;
            TextView tvDefinition;

            VH(View v) {
                super(v);
                tvWord = v.findViewById(R.id.tv_word);
                tvDefinition = v.findViewById(R.id.tv_definition);
            }
        }
    }
}
