package com.example.wordlearningapp;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordlearningapp.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserRecordActivity extends AppCompatActivity {

    private TextView tvTitle, tvStats;
    private RecyclerView recyclerView;
    private LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String userId = getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("userName");

        // Build UI programmatically
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFf2f8fc);

        // Toolbar
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setBackgroundColor(0xFF318af8);
        toolbar.setPadding(28, 28, 28, 28);

        TextView backBtn = new TextView(this);
        backBtn.setText("← 返回");
        backBtn.setTextSize(16);
        backBtn.setTextColor(0xFFFFFFFF);
        backBtn.setPadding(0, 0, 16, 0);
        backBtn.setOnClickListener(v -> finish());
        toolbar.addView(backBtn);

        tvTitle = new TextView(this);
        tvTitle.setText("学习记录");
        tvTitle.setTextSize(18);
        tvTitle.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        tvTitle.setLayoutParams(tlp);
        tvTitle.setGravity(Gravity.CENTER);
        toolbar.addView(tvTitle);

        ViewGroup spacer = new ViewGroup(this) {
            @Override protected void onLayout(boolean c, int l, int t, int r, int b) {}
        };
        spacer.setLayoutParams(new LinearLayout.LayoutParams(48, 1));
        toolbar.addView(spacer);
        root.addView(toolbar);

        // User info
        TextView userInfo = new TextView(this);
        userInfo.setText(userName + " (" + userId + ")");
        userInfo.setTextSize(16);
        userInfo.setTextColor(0xFF318af8);
        userInfo.setPadding(28, 20, 28, 0);
        root.addView(userInfo);

        // Stats
        tvStats = new TextView(this);
        tvStats.setText("加载中...");
        tvStats.setTextSize(14);
        tvStats.setTextColor(0xFF8899aa);
        tvStats.setPadding(28, 8, 28, 16);
        root.addView(tvStats);

        // RecyclerView
        recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setPadding(28, 0, 28, 28);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
        recyclerView.setLayoutParams(rlp);
        root.addView(recyclerView);

        setContentView(root);

        if (userId != null) loadData(userId);
    }

    private void loadData(String userId) {
        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().get("/api/admin/records?userId=" + userId);
                if (res.get("code").getAsInt() == 200) {
                    JsonArray data = res.getAsJsonArray("data");
                    Map<String, JsonObject> agg = new LinkedHashMap<>();
                    for (int i = 0; i < data.size(); i++) {
                        JsonObject r = data.get(i).getAsJsonObject();
                        String date = r.has("learningDate") ? r.get("learningDate").getAsString() : "";
                        String book = r.has("learnedWordBookId") ? r.get("learnedWordBookId").getAsString() : "unknown";
                        String key = date + "|" + book;
                        if (!agg.containsKey(key)) {
                            JsonObject e = new JsonObject();
                            e.addProperty("date", date);
                            e.addProperty("book", book);
                            e.addProperty("count", 1);
                            agg.put(key, e);
                        } else {
                            JsonObject e = agg.get(key);
                            e.addProperty("count", e.get("count").getAsInt() + 1);
                        }
                    }
                    List<JsonObject> list = new ArrayList<>(agg.values());
                    list.sort((a, b) -> b.get("date").getAsString().compareTo(a.get("date").getAsString()));

                    runOnUiThread(() -> {
                        tvStats.setText("总记录: " + data.size() + " 条");
                        recyclerView.setAdapter(new RecordAdapter(list));
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> tvStats.setText("加载失败"));
            }
        }).start();
    }

    private class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.VH> {
        private final List<JsonObject> data;
        RecordAdapter(List<JsonObject> data) { this.data = data; }

        @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout card = new LinearLayout(parent.getContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(0xFFFFFFFF);
            card.setPadding(24, 16, 24, 16);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 12);
            card.setLayoutParams(lp);
            card.setElevation(2);

            TextView dateTv = new TextView(parent.getContext());
            dateTv.setId(View.generateViewId());
            dateTv.setTextSize(16);
            dateTv.setTextColor(0xFF318af8);
            card.addView(dateTv);

            TextView detailTv = new TextView(parent.getContext());
            detailTv.setId(ViewGroup.generateViewId());
            detailTv.setTextSize(14);
            detailTv.setTextColor(0xFF6578a0);
            detailTv.setPadding(0, 4, 0, 0);
            card.addView(detailTv);

            return new VH(card, dateTv, detailTv);
        }

        @Override public void onBindViewHolder(VH holder, int pos) {
            JsonObject r = data.get(pos);
            holder.date.setText(r.has("date") ? r.get("date").getAsString() : "");
            int count = r.has("count") ? r.get("count").getAsInt() : 0;
            String book = r.has("book") ? r.get("book").getAsString() : "";
            holder.detail.setText("词书: " + book + "  |  学习 " + count + " 词");
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView date, detail;
            VH(View v, TextView date, TextView detail) {
                super(v);
                this.date = date;
                this.detail = detail;
            }
        }
    }
}
