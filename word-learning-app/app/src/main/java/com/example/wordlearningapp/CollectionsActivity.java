package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CollectionsActivity extends AppCompatActivity {

    private static final String TAG = "CollectionsActivity";

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProgressBar progress;
    private EditText etSearch;
    private Button btnSearch;
    private TextView tvBack;
    private JsonArray allCollections;
    private CollectionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collections);

        tvBack = findViewById(R.id.tv_back);
        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_search);
        tvEmpty = findViewById(R.id.tv_empty);
        progress = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tvBack.setOnClickListener(v -> finish());

        btnSearch.setOnClickListener(v -> filterCollections());

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filterCollections();
                return true;
            }
            return false;
        });

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "页面恢复，刷新收藏数据");
        loadData();
    }

    private void loadData() {
        if (progress != null) progress.setVisibility(View.VISIBLE);
        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                Log.d(TAG, "开始加载收藏数据");
                JsonObject res = ApiClient.get().get("/api/collections");
                Log.d(TAG, "API响应: " + res.toString());

                if (res.has("code") && res.get("code").getAsInt() == 200) {
                    if (res.has("data")) {
                        allCollections = res.getAsJsonArray("data");
                        Log.d(TAG, "收藏数据数量: " + allCollections.size());
                        runOnUiThread(() -> {
                            if (progress != null) progress.setVisibility(View.GONE);
                            filterCollections();
                        });
                    } else {
                        runOnUiThread(() -> {
                            if (progress != null) progress.setVisibility(View.GONE);
                            allCollections = new JsonArray();
                            filterCollections();
                        });
                    }
                } else {
                    String errorMsg = res.has("message") ? res.get("message").getAsString() : "加载失败";
                    Log.e(TAG, "加载失败: " + errorMsg);
                    runOnUiThread(() -> {
                        if (progress != null) progress.setVisibility(View.GONE);
                        if (tvEmpty != null) {
                            tvEmpty.setText(errorMsg);
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "加载异常: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    if (progress != null) progress.setVisibility(View.GONE);
                    if (tvEmpty != null) {
                        tvEmpty.setText("加载失败: " + e.getMessage());
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();
    }

    private void filterCollections() {
        if (allCollections == null) {
            allCollections = new JsonArray();
        }

        String searchText = etSearch != null ? etSearch.getText().toString().trim().toLowerCase() : "";
        JsonArray filteredData = new JsonArray();

        for (int i = 0; i < allCollections.size(); i++) {
            JsonElement element = allCollections.get(i);
            if (!element.isJsonObject()) continue;

            JsonObject c = element.getAsJsonObject();
            String spelling = c.has("englishSpelling") ? c.get("englishSpelling").getAsString().toLowerCase() : "";

            if (searchText.isEmpty() || spelling.contains(searchText)) {
                filteredData.add(c);
            }
        }

        runOnUiThread(() -> {
            if (filteredData.size() == 0) {
                if (recyclerView != null) recyclerView.setVisibility(View.GONE);
                if (tvEmpty != null) {
                    tvEmpty.setText(searchText.isEmpty() ? "暂无收藏" : "未找到相关单词");
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            } else {
                if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
                if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);

                if (adapter == null) {
                    adapter = new CollectionAdapter(filteredData);
                    if (recyclerView != null) recyclerView.setAdapter(adapter);
                } else {
                    adapter.updateData(filteredData);
                }
            }
        });
    }

    private void cancelCollection(String wordId, int pos) {
        Log.d(TAG, "开始取消收藏, wordId: " + wordId + ", pos: " + pos);

        if (wordId == null || wordId.isEmpty()) {
            Toast.makeText(this, "无效的单词ID", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                // 正确的API调用：DELETE /api/collections/remove (body: {wordId: "xxx"})
                JsonObject body = new JsonObject();
                body.addProperty("wordId", wordId);

                String url = "/api/collections/remove";
                Log.d(TAG, "DELETE请求: " + url + ", body: " + body.toString());

                JsonObject res = ApiClient.get().delete(url, body);
                Log.d(TAG, "响应: " + res.toString());

                runOnUiThread(() -> {
                    if (res != null && res.has("code") && res.get("code").getAsInt() == 200) {
                        Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "取消收藏成功，重新加载数据");
                        loadData();
                    } else {
                        String message = "取消失败";
                        if (res != null && res.has("message")) {
                            message = res.get("message").getAsString();
                        } else if (res != null && res.has("error")) {
                            message = res.get("error").getAsString();
                        }
                        Log.e(TAG, "取消收藏失败: " + message);
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "取消收藏异常: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "网络错误: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.VH> {
        JsonArray data;

        CollectionAdapter(JsonArray data) {
            this.data = data != null ? data : new JsonArray();
        }

        void updateData(JsonArray newData) {
            this.data = newData != null ? newData : new JsonArray();
            notifyDataSetChanged();
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_collection, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(VH holder, int pos) {
            if (data == null || pos < 0 || pos >= data.size()) {
                Log.e(TAG, "无效的位置: " + pos + ", 数据大小: " + (data != null ? data.size() : 0));
                return;
            }

            JsonElement element = data.get(pos);
            if (!element.isJsonObject()) {
                Log.e(TAG, "数据项不是JsonObject");
                return;
            }

            JsonObject c = element.getAsJsonObject();

            String spelling = c.has("englishSpelling") ? c.get("englishSpelling").getAsString() : "";
            String phonetic = "";
            if (c.has("phoneticSymbol") && !c.get("phoneticSymbol").isJsonNull()) {
                phonetic = c.get("phoneticSymbol").getAsString();
            }

            String date = "";
            if (c.has("collectionTime") && !c.get("collectionTime").isJsonNull()) {
                String fullDate = c.get("collectionTime").getAsString();
                date = fullDate.length() >= 10 ? fullDate.substring(0, 10) : fullDate;
            }

            String collectionId = c.has("collectionId") ? c.get("collectionId").getAsString() : "";
            String wordId = c.has("wordId") ? c.get("wordId").getAsString() : "";

            Log.d(TAG, "绑定数据项 " + pos + ": spelling=" + spelling + ", wordId=" + wordId);

            holder.tvWord.setText(spelling);
            holder.tvPhonetic.setText(phonetic.isEmpty() ? "" : "[" + phonetic + "]");
            holder.tvTime.setText("收藏时间：" + date);

            holder.btnDetail.setOnClickListener(v -> {
                if (wordId != null && !wordId.isEmpty()) {
                    Intent intent = new Intent(CollectionsActivity.this, WordDetailActivity.class);
                    intent.putExtra("wordId", wordId);
                    intent.putExtra("fromCollection", true);
                    startActivity(intent);
                } else {
                    Toast.makeText(CollectionsActivity.this, "无效的单词ID", Toast.LENGTH_SHORT).show();
                }
            });

            holder.btnCancel.setOnClickListener(v -> {
                if (wordId == null || wordId.isEmpty()) {
                    Toast.makeText(CollectionsActivity.this, "无效的单词ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                new AlertDialog.Builder(CollectionsActivity.this)
                        .setTitle("确认取消")
                        .setMessage("确定取消收藏单词\"" + spelling + "\"？")
                        .setPositiveButton("确定", (d, w) -> {
                            Log.d(TAG, "用户确认取消收藏");
                            cancelCollection(wordId, pos);
                        })
                        .setNegativeButton("取消", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return data != null ? data.size() : 0;
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvWord;
            TextView tvPhonetic;
            TextView tvTime;
            Button btnDetail;
            Button btnCancel;

            VH(View v) {
                super(v);
                tvWord = v.findViewById(R.id.tv_word);
                tvPhonetic = v.findViewById(R.id.tv_phonetic);
                tvTime = v.findViewById(R.id.tv_time);
                btnDetail = v.findViewById(R.id.btn_detail);
                btnCancel = v.findViewById(R.id.btn_cancel);
            }
        }
    }
}
