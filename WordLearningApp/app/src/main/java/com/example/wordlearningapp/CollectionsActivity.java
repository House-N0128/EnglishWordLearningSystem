package com.example.wordlearningapp;

import android.content.Intent;
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

public class CollectionsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ((TextView) findViewById(R.id.toolbar_title)).setText("我的收藏");
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());
        tvEmpty = findViewById(R.id.tv_empty);
        progress = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().get("/api/collections");
                if (res.get("code").getAsInt() == 200) {
                    JsonArray data = res.getAsJsonArray("data");
                    runOnUiThread(() -> {
                        progress.setVisibility(View.GONE);
                        if (data.size() == 0) {
                            tvEmpty.setText("暂无收藏");
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setAdapter(new CollectionAdapter(data));
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> { progress.setVisibility(View.GONE); tvEmpty.setText("加载失败"); tvEmpty.setVisibility(View.VISIBLE); });
            }
        }).start();
    }

    private class CollectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final JsonArray data;
        CollectionAdapter(JsonArray data) { this.data = data; }

        @Override public RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
            return new RecyclerView.ViewHolder(v) {};
        }

        @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int pos) {
            JsonObject c = data.get(pos).getAsJsonObject();
            String spelling = c.has("englishSpelling") ? c.get("englishSpelling").getAsString() : "";
            String phonetic = c.has("phoneticSymbol") && !c.get("phoneticSymbol").isJsonNull()
                    ? c.get("phoneticSymbol").getAsString() : "";
            String date = c.has("collectionTime") ? c.get("collectionTime").getAsString().substring(0, 10) : "";
            ((TextView) holder.itemView.findViewById(android.R.id.text1)).setText(spelling + "  " + phonetic);
            ((TextView) holder.itemView.findViewById(android.R.id.text2)).setText("收藏时间: " + date);
            String wordId = c.has("wordId") ? c.get("wordId").getAsString() : "";
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(CollectionsActivity.this, WordDetailActivity.class);
                intent.putExtra("wordId", wordId);
                startActivity(intent);
            });
        }

        @Override public int getItemCount() { return data.size(); }
    }
}
