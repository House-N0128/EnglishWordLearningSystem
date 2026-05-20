package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class WordSearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private Button btnSearch;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProgressBar progress;
    private TextView tvBack;
    private boolean isAdmin;
    private JsonArray currentData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        isAdmin = "admin".equals(AuthManager.get().getRole());

        tvBack = findViewById(R.id.tv_back);
        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_search);
        tvEmpty = findViewById(R.id.tv_empty);
        progress = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tvBack.setOnClickListener(v -> finish());

        btnSearch.setOnClickListener(v -> doSearch());

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch();
                return true;
            }
            return false;
        });

        if (isAdmin) {
            etSearch.setHint("输入关键词搜索");
            Button addBtn = findViewById(R.id.btn_add);
            if (addBtn != null) {
                addBtn.setVisibility(View.VISIBLE);
                addBtn.setOnClickListener(v -> startActivity(new Intent(this, AddEditWordActivity.class)));
            }
        }
    }

    private void doSearch() {
        String keyword = etSearch.getText().toString().trim();
        if (keyword.isEmpty()) {
            tvEmpty.setText("输入关键词搜索单词");
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(null);
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
                        currentData = data;
                        if (data.size() == 0) {
                            recyclerView.setAdapter(null);
                            tvEmpty.setText("未找到相关单词");
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                            recyclerView.setAdapter(new WordAdapter(data));
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

    private void deleteWord(String wordId, int pos) {
        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().delete("/api/words/" + wordId, null);
                runOnUiThread(() -> {
                    if (res.get("code").getAsInt() == 200) {
                        Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
                        if (currentData != null) {
                            currentData.remove(pos);
                            recyclerView.getAdapter().notifyItemRemoved(pos);
                        }
                    } else {
                        Toast.makeText(this, res.has("message") ? res.get("message").getAsString() : "删除失败", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private class WordAdapter extends RecyclerView.Adapter<WordAdapter.VH> {
        private final JsonArray data;
        WordAdapter(JsonArray data) {
            this.data = data;
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_search_word, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(VH holder, int pos) {
            JsonObject w = data.get(pos).getAsJsonObject();
            String spelling = w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "";
            String phonetic = (w.has("phoneticSymbol") && !w.get("phoneticSymbol").isJsonNull())
                    ? w.get("phoneticSymbol").getAsString() : "";
            String chinese = w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "";
            String wordId = w.has("wordId") ? w.get("wordId").getAsString() : "";

            String wordWithType = spelling;
            if (!phonetic.isEmpty()) {
                wordWithType += " " + phonetic;
            }
            holder.tvEnglish.setText(wordWithType);
            holder.tvChinese.setText(chinese);

            holder.btnViewDetail.setOnClickListener(v -> {
                if (isAdmin) {
                    Intent intent = new Intent(WordSearchActivity.this, AddEditWordActivity.class);
                    intent.putExtra("wordId", wordId);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(WordSearchActivity.this, WordDetailActivity.class);
                    intent.putExtra("wordId", wordId);
                    startActivity(intent);
                }
            });

            if (isAdmin) {
                holder.btnDelete.setVisibility(View.VISIBLE);
                int finalPos = pos;
                holder.btnDelete.setOnClickListener(v -> {
                    new AlertDialog.Builder(WordSearchActivity.this)
                            .setTitle("确认删除")
                            .setMessage("确定删除单词\"" + spelling + "\"？")
                            .setPositiveButton("确定", (d, w2) -> deleteWord(wordId, finalPos))
                            .setNegativeButton("取消", null)
                            .show();
                });
            } else {
                holder.btnDelete.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvEnglish;
            TextView tvChinese;
            Button btnViewDetail;
            Button btnDelete;

            VH(View v) {
                super(v);
                tvEnglish = v.findViewById(R.id.tv_word_english);
                tvChinese = v.findViewById(R.id.tv_word_chinese);
                btnViewDetail = v.findViewById(R.id.btn_view_detail);
                btnDelete = v.findViewById(R.id.btn_delete);
            }
        }
    }
}
