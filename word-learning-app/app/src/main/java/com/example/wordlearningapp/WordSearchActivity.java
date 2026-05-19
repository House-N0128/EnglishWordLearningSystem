package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class WordSearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProgressBar progress;
    private boolean isAdmin;
    private String lastKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        isAdmin = "admin".equals(AuthManager.get().getRole());
        ((TextView) findViewById(R.id.toolbar_title)).setText(isAdmin ? "单词管理" : "单词查询");
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        if (isAdmin) {
            etSearch.setHint("输入关键词搜索，不输入则显示全部");
            // Add button
            LinearLayout root = (LinearLayout) recyclerView.getParent().getParent();
            Button addBtn = new Button(this);
            addBtn.setText("+ 新增单词");
            addBtn.setTextColor(0xFFFFFFFF);
            addBtn.setBackgroundColor(0xFF52e8bc);
            addBtn.setTextSize(15);
            addBtn.setPadding(14, 12, 14, 12);
            LinearLayout.LayoutParams abp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            abp.setMargins(28, 0, 28, 14);
            addBtn.setLayoutParams(abp);
            addBtn.setOnClickListener(v -> startActivity(new Intent(this, AddEditWordActivity.class)));
            root.addView(addBtn, 0);
        }

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

            LinearLayout btnRow = new LinearLayout(parent.getContext());
            btnRow.setOrientation(LinearLayout.HORIZONTAL);
            btnRow.setVisibility(View.GONE);
            card.addView(btnRow);

            return new VH(card, nameTv, infoTv, chnTv, btnRow);
        }

        @Override public void onBindViewHolder(VH holder, int pos) {
            JsonObject w = data.get(pos).getAsJsonObject();
            holder.name.setText(w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "");
            holder.info.setText("音标: " + (w.has("phoneticSymbol") && !w.get("phoneticSymbol").isJsonNull() ? w.get("phoneticSymbol").getAsString() : "无"));
            holder.chn.setText(w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "");

            String wordId = w.has("wordId") ? w.get("wordId").getAsString() : "";
            holder.itemView.setOnClickListener(v -> {
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

            // Admin buttons
            if (isAdmin && holder.btnRow.getChildCount() == 0) {
                TextView delBtn = new TextView(holder.itemView.getContext());
                delBtn.setText("删除");
                delBtn.setTextColor(0xFFFFFFFF);
                delBtn.setBackgroundColor(0xFFd93025);
                delBtn.setTextSize(12);
                delBtn.setPadding(16, 6, 16, 6);
                LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dp.setMarginEnd(16);
                delBtn.setLayoutParams(dp);
                delBtn.setOnClickListener(dv -> {
                    new android.app.AlertDialog.Builder(dv.getContext())
                            .setTitle("确认删除")
                            .setMessage("确定删除单词\"" + holder.name.getText() + "\"？")
                            .setPositiveButton("确定", (d, w2) -> {
                                new Thread(() -> {
                                    try {
                                        JsonObject res = ApiClient.get().delete("/api/words/" + wordId, null);
                                        runOnUiThread(() -> {
                                            Toast.makeText(WordSearchActivity.this, res.has("message") ? res.get("message").getAsString() : "已删除", Toast.LENGTH_SHORT).show();
                                            doSearch();
                                        });
                                    } catch (Exception e) {
                                        runOnUiThread(() -> Toast.makeText(WordSearchActivity.this, "网络错误", Toast.LENGTH_SHORT).show());
                                    }
                                }).start();
                            })
                            .setNegativeButton("取消", null).show();
                });
                holder.btnRow.addView(delBtn);
            }
            holder.btnRow.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView name, info, chn;
            LinearLayout btnRow;
            VH(View v, TextView name, TextView info, TextView chn, LinearLayout btnRow) {
                super(v);
                this.name = name;
                this.info = info;
                this.chn = chn;
                this.btnRow = btnRow;
            }
        }
    }
}
