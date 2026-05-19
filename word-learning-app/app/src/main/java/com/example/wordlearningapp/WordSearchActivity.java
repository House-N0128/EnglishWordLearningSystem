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
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProgressBar progress;
    private boolean isAdmin;
    private JsonArray currentData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        isAdmin = "admin".equals(AuthManager.get().getRole());
        ((TextView) findViewById(R.id.toolbar_title)).setText(isAdmin ? "单词管理" : "单词查询");
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
                            tvEmpty.setText("未找到相关单词");
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setAdapter(new WordAdapter(data));
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> { progress.setVisibility(View.GONE); tvEmpty.setText("搜索失败"); tvEmpty.setVisibility(View.VISIBLE); });
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

            TextView nameTv = new TextView(parent.getContext());
            nameTv.setTextSize(18);
            nameTv.setTextColor(0xFF318af8);
            card.addView(nameTv);

            TextView infoTv = new TextView(parent.getContext());
            infoTv.setTextSize(14);
            infoTv.setTextColor(0xFF47b1eb);
            infoTv.setPadding(0, 6, 0, 6);
            card.addView(infoTv);

            TextView chnTv = new TextView(parent.getContext());
            chnTv.setTextSize(15);
            chnTv.setTextColor(0xFF273245);
            card.addView(chnTv);

            VH vh = new VH(card, nameTv, infoTv, chnTv);
            return vh;
        }

        @Override public void onBindViewHolder(VH holder, int pos) {
            JsonObject w = data.get(pos).getAsJsonObject();
            String spelling = w.has("englishSpelling") ? w.get("englishSpelling").getAsString() : "";
            String phonetic = (w.has("phoneticSymbol") && !w.get("phoneticSymbol").isJsonNull()) ? w.get("phoneticSymbol").getAsString() : "";
            String chinese = w.has("chineseDefinition") ? w.get("chineseDefinition").getAsString() : "";
            String wordId = w.has("wordId") ? w.get("wordId").getAsString() : "";

            holder.name.setText(spelling);
            holder.info.setText("音标: " + phonetic);
            holder.chn.setText(chinese);

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

            // Admin delete button
            holder.btnRow.removeAllViews();
            if (isAdmin) {
                Button delBtn = new Button(holder.itemView.getContext());
                delBtn.setText("删除");
                delBtn.setTextColor(0xFFFFFFFF);
                delBtn.setBackgroundColor(0xFFd93025);
                delBtn.setTextSize(13);
                delBtn.setPadding(20, 8, 20, 8);
                LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dp.topMargin = 12;
                delBtn.setLayoutParams(dp);
                int finalPos = pos;
                delBtn.setOnClickListener(dv -> {
                    new AlertDialog.Builder(WordSearchActivity.this)
                            .setTitle("确认删除")
                            .setMessage("确定删除单词\"" + spelling + "\"？")
                            .setPositiveButton("确定", (d, w2) -> deleteWord(wordId, finalPos))
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
            VH(View v, TextView name, TextView info, TextView chn) {
                super(v);
                this.name = name;
                this.info = info;
                this.chn = chn;
                this.btnRow = new LinearLayout(v.getContext());
                this.btnRow.setOrientation(LinearLayout.HORIZONTAL);
                ((LinearLayout) v).addView(this.btnRow);
            }
        }
    }
}
