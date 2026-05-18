package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class WordSearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ((TextView) findViewById(R.id.toolbar_title)).setText("单词查询");
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());
        tvEmpty = findViewById(R.id.tv_empty);
        tvEmpty.setText("输入关键词开始搜索");
        tvEmpty.setVisibility(View.VISIBLE);
        progress = findViewById(R.id.progress);
        progress.setVisibility(View.GONE);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Add search bar as header in RecyclerView
        etSearch = new EditText(this);
        etSearch.setHint("输入英文/中文查找单词");
        etSearch.setTextSize(16);
        etSearch.setPadding(14, 14, 14, 14);
        etSearch.setBackgroundColor(0xFFf6f8fc);
        etSearch.setSingleLine(true);
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch();
                return true;
            }
            return false;
        });

        // Use a simple adapter approach with header
        findViewById(R.id.toolbar_back).post(() -> doSearch());
    }

    private void doSearch() {
        String keyword = etSearch.getText().toString().trim();
        if (keyword.isEmpty()) {
            recyclerView.setAdapter(null);
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().get("/api/words/search?keyword=" + keyword);
                if (res.get("code").getAsInt() == 200) {
                    JsonArray data = res.getAsJsonArray("data");
                    runOnUiThread(() -> {
                        progress.setVisibility(View.GONE);
                        if (data.size() == 0) {
                            tvEmpty.setText("未找到相关单词");
                            tvEmpty.setVisibility(View.VISIBLE);
                            recyclerView.setAdapter(null);
                        } else {
                            recyclerView.setAdapter(new WordAdapter(data, etSearch));
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    tvEmpty.setText("搜索失败");
                    tvEmpty.setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }

    private static class WordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;

        private final JsonArray data;
        private final EditText searchBox;

        WordAdapter(JsonArray data, EditText searchBox) {
            this.data = data;
            this.searchBox = searchBox;
        }

        @Override public int getItemViewType(int position) {
            return position == 0 ? TYPE_HEADER : TYPE_ITEM;
        }

        @Override public RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                return new RecyclerView.ViewHolder(searchBox) {};
            }
            View v = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new RecyclerView.ViewHolder(v) {};
        }

        @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int pos) {
            if (pos == 0) return; // header

            JsonObject w = data.get(pos - 1).getAsJsonObject();
            String eng = w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "";
            String chn = w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "";
            ((TextView) holder.itemView.findViewById(android.R.id.text1)).setText(eng + " - " + chn);

            String wordId = w.has("wordId") ? w.get("wordId").getAsString() : "";
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), WordDetailActivity.class);
                intent.putExtra("wordId", wordId);
                v.getContext().startActivity(intent);
            });
        }

        @Override public int getItemCount() { return data.size() + 1; }
    }
}
