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
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class WordBooksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty, toolbarTitle;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("词书浏览");
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
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    tvEmpty.setText("加载失败");
                    tvEmpty.setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }

    private class BookAdapter extends RecyclerView.Adapter<BookAdapter.VH> {
        private final JsonArray data;

        BookAdapter(JsonArray data) { this.data = data; }

        @Override public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(VH holder, int pos) {
            JsonObject b = data.get(pos).getAsJsonObject();
            String name = b.has("wordBookName") ? b.get("wordBookName").getAsString() : "";
            String info = (b.has("difficultyLevel") ? b.get("difficultyLevel").getAsString() : "")
                    + " | " + (b.has("wordCount") ? b.get("wordCount").getAsInt() : 0) + "词";
            holder.title.setText(name);
            holder.subtitle.setText(info);
            String bookId = b.has("wordBookId") ? b.get("wordBookId").getAsString() : "";
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(WordBooksActivity.this, BookDetailActivity.class);
                intent.putExtra("bookId", bookId);
                startActivity(intent);
            });
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView title, subtitle;
            VH(View v) {
                super(v);
                title = v.findViewById(android.R.id.text1);
                subtitle = v.findViewById(android.R.id.text2);
            }
        }
    }
}
