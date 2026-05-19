package com.example.wordlearningapp;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
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

public class UserManageActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (!AuthManager.get().isLoggedIn() || !"admin".equals(AuthManager.get().getRole())) {
            finish();
            return;
        }

        ((TextView) findViewById(R.id.toolbar_title)).setText("用户管理");
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        etSearch = findViewById(R.id.et_search);
        etSearch.setHint("输入用户ID/昵称搜索");
        tvEmpty = findViewById(R.id.tv_empty);
        tvEmpty.setText("输入关键词搜索用户");
        progress = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { doSearch(); return true; }
            return false;
        });
    }

    private void doSearch() {
        String keyword = etSearch.getText().toString().trim();
        if (keyword.isEmpty()) return;
        tvEmpty.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().get("/api/admin/users?keyword=" + keyword);
                if (res.get("code").getAsInt() == 200) {
                    JsonArray data = res.getAsJsonArray("data");
                    runOnUiThread(() -> {
                        progress.setVisibility(View.GONE);
                        if (data.size() == 0) {
                            tvEmpty.setText("未找到匹配用户");
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setAdapter(new UserAdapter(data));
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> { progress.setVisibility(View.GONE); tvEmpty.setText("搜索失败"); tvEmpty.setVisibility(View.VISIBLE); });
            }
        }).start();
    }

    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {
        private final JsonArray data;
        UserAdapter(JsonArray data) { this.data = data; }

        @Override public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(VH holder, int pos) {
            JsonObject u = data.get(pos).getAsJsonObject();
            String name = u.has("userName") ? u.get("userName").getAsString() : "";
            String userId = u.has("userId") ? u.get("userId").getAsString() : "";
            String status = u.has("accountStatus") ? u.get("accountStatus").getAsString() : "正常";
            holder.title.setText(name + " (" + userId + ")");
            holder.subtitle.setText("状态: " + status);

            holder.itemView.setOnClickListener(v -> {
                String newStatus = "冻结".equals(status) ? "正常" : "冻结";
                String action = "冻结".equals(newStatus) ? "冻结" : "解冻";
                new android.app.AlertDialog.Builder(v.getContext())
                        .setTitle("用户操作")
                        .setMessage("确定" + action + "该账号？")
                        .setPositiveButton(action, (d, w) -> toggleStatus(userId, newStatus))
                        .setNegativeButton("取消", null)
                        .show();
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

    private void toggleStatus(String userId, String newStatus) {
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("accountStatus", newStatus);
                JsonObject res = ApiClient.get().put("/api/admin/users/" + userId, body);
                runOnUiThread(() -> {
                    Toast.makeText(this, res.has("message") ? res.get("message").getAsString() : "操作完成", Toast.LENGTH_SHORT).show();
                    doSearch();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
