package com.example.wordlearningapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class WordBooksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ((TextView) findViewById(R.id.toolbar_title)).setText("词书浏览");
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());
        tvEmpty = findViewById(R.id.tv_empty);
        progress = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().get("/api/wordbooks");
                if (res.get("code").getAsInt() == 200) {
                    JsonArray data = res.getAsJsonArray("data");
                    runOnUiThread(() -> {
                        progress.setVisibility(View.GONE);
                        if (data.size() == 0) {
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setAdapter(new BookAdapter(data));
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> { progress.setVisibility(View.GONE); tvEmpty.setText("加载失败"); tvEmpty.setVisibility(View.VISIBLE); });
            }
        }).start();
    }

    private class BookAdapter extends RecyclerView.Adapter<BookAdapter.VH> {
        private final JsonArray data;
        BookAdapter(JsonArray data) { this.data = data; }

        @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            // Create card-style layout
            LinearLayout card = new LinearLayout(parent.getContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(0xFFFFFFFF);
            card.setPadding(24, 20, 24, 20);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 24);
            card.setLayoutParams(lp);
            card.setElevation(4);

            TextView nameTv = new TextView(parent.getContext());
            nameTv.setId(View.generateViewId());
            nameTv.setTextSize(18);
            nameTv.setTextColor(0xFF318af8);

            TextView infoTv = new TextView(parent.getContext());
            infoTv.setId(View.generateViewId());
            infoTv.setTextSize(14);
            infoTv.setTextColor(0xFF47b1eb);
            infoTv.setPadding(0, 8, 0, 8);

            TextView descTv = new TextView(parent.getContext());
            descTv.setId(View.generateViewId());
            descTv.setTextSize(14);
            descTv.setTextColor(0xFF6578a0);

            card.addView(nameTv);
            card.addView(infoTv);
            card.addView(descTv);

            return new VH(card, nameTv, infoTv, descTv);
        }

        @Override public void onBindViewHolder(VH holder, int pos) {
            JsonObject b = data.get(pos).getAsJsonObject();
            holder.name.setText(b.has("wordBookName") ? b.get("wordBookName").getAsString() : "");
            holder.info.setText((b.has("difficultyLevel") ? b.get("difficultyLevel").getAsString() : "")
                    + "  |  " + (b.has("wordCount") ? b.get("wordCount").getAsInt() : 0) + "词");
            holder.desc.setText(b.has("wordBookDescription") && !b.get("wordBookDescription").isJsonNull()
                    ? b.get("wordBookDescription").getAsString() : "");
            String bookId = b.has("wordBookId") ? b.get("wordBookId").getAsString() : "";
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(WordBooksActivity.this, BookDetailActivity.class);
                intent.putExtra("bookId", bookId);
                startActivity(intent);
            });
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView name, info, desc;
            VH(View v, TextView name, TextView info, TextView desc) {
                super(v);
                this.name = name;
                this.info = info;
                this.desc = desc;
            }
        }
    }
}
