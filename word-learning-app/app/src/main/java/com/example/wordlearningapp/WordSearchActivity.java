package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
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
        setContentView(R.layout.activity_search);

        ((TextView) findViewById(R.id.toolbar_title)).setText("单词管理");
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        etSearch = findViewById(R.id.et_search);
        tvEmpty = findViewById(R.id.tv_empty);
        progress = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { doSearch(); return true; }
            return false;
        });

        // Load all words initially
        etSearch.setText("");
        doSearch();
    }

    private void doSearch() {
        String keyword = etSearch.getText().toString().trim();
        if (keyword.isEmpty()) {
            tvEmpty.setText("输入关键词搜索单词");
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
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                            recyclerView.setAdapter(new WordAdapter(data));
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> { progress.setVisibility(View.GONE); tvEmpty.setText("搜索失败"); tvEmpty.setVisibility(View.VISIBLE); });
            }
        }).start();
    }

    private class WordAdapter extends RecyclerView.Adapter<WordAdapter.VH> {
        private final JsonArray data;
        WordAdapter(JsonArray data) { this.data = data; }

        @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout card = new LinearLayout(parent.getContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(0xFFFFFFFF);
            card.setPadding(24, 20, 24, 20);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 18);
            card.setLayoutParams(lp);
            card.setElevation(4);

            TextView nameTv = new TextView(parent.getContext());
            nameTv.setId(View.generateViewId());
            nameTv.setTextSize(18);
            nameTv.setTextColor(0xFF318af8);
            card.addView(nameTv);

            TextView infoTv = new TextView(parent.getContext());
            infoTv.setId(View.generateViewId());
            infoTv.setTextSize(14);
            infoTv.setTextColor(0xFF47b1eb);
            infoTv.setPadding(0, 6, 0, 6);
            card.addView(infoTv);

            TextView chnTv = new TextView(parent.getContext());
            chnTv.setId(View.generateViewId());
            chnTv.setTextSize(15);
            chnTv.setTextColor(0xFF273245);
            card.addView(chnTv);

            return new VH(card, nameTv, infoTv, chnTv);
        }

        @Override public void onBindViewHolder(VH holder, int pos) {
            JsonObject w = data.get(pos).getAsJsonObject();
            holder.name.setText(w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "");
            holder.info.setText("音标: " + (w.has("phoneticSymbol") && !w.get("phoneticSymbol").isJsonNull() ? w.get("phoneticSymbol").getAsString() : "无"));
            holder.chn.setText(w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "");

            String wordId = w.has("wordId") ? w.get("wordId").getAsString() : "";
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(WordSearchActivity.this, WordDetailActivity.class);
                intent.putExtra("wordId", wordId);
                startActivity(intent);
            });
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView name, info, chn;
            VH(View v, TextView name, TextView info, TextView chn) {
                super(v);
                this.name = name;
                this.info = info;
                this.chn = chn;
            }
        }
    }
}
