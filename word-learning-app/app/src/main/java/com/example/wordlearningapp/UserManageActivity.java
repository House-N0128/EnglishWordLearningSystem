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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
        tvEmpty.setVisibility(View.VISIBLE);
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

            TextView contactTv = new TextView(parent.getContext());
            contactTv.setId(View.generateViewId());
            contactTv.setTextSize(12);
            contactTv.setTextColor(0xFF8899aa);
            contactTv.setPadding(0, 0, 0, 10);
            card.addView(contactTv);

            // Buttons row
            LinearLayout btns = new LinearLayout(parent.getContext());
            btns.setOrientation(LinearLayout.HORIZONTAL);
            btns.setId(View.generateViewId());
            card.addView(btns);

            return new VH(card, nameTv, infoTv, contactTv, btns);
        }

        @Override public void onBindViewHolder(VH holder, int pos) {
            JsonObject u = data.get(pos).getAsJsonObject();
            String name = u.has("userName") ? u.get("userName").getAsString() : "未知";
            String userId = u.has("userId") ? u.get("userId").getAsString() : "";
            String status = u.has("accountStatus") ? u.get("accountStatus").getAsString() : "正常";
            String phone = u.has("phoneNumber") && !u.get("phoneNumber").isJsonNull() ? u.get("phoneNumber").getAsString() : "";
            String email = u.has("email") && !u.get("email").isJsonNull() ? u.get("email").getAsString() : "";

            holder.name.setText(name + " (" + userId + ")");
            holder.info.setText("状态: " + status);
            holder.contact.setText(phone + "  |  " + email);

            holder.btns.removeAllViews();

            // Freeze/Unfreeze button
            TextView toggleBtn = new TextView(holder.btns.getContext());
            String newStatus = "冻结".equals(status) ? "正常" : "冻结";
            String action = "冻结".equals(newStatus) ? "冻结" : "解冻";
            int btnColor = "冻结".equals(newStatus) ? 0xFFd93025 : 0xFF52e8bc;
            toggleBtn.setText(action);
            toggleBtn.setTextColor(0xFFFFFFFF);
            toggleBtn.setBackgroundColor(btnColor);
            toggleBtn.setTextSize(14);
            toggleBtn.setPadding(24, 10, 24, 10);
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tp.setMarginEnd(16);
            toggleBtn.setLayoutParams(tp);
            toggleBtn.setOnClickListener(v -> {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("用户操作")
                        .setMessage("确定" + action + "该账号？")
                        .setPositiveButton(action, (d, w) -> toggleStatus(userId, newStatus))
                        .setNegativeButton("取消", null)
                        .show();
            });
            holder.btns.addView(toggleBtn);

            // View records button
            TextView recordBtn = new TextView(holder.btns.getContext());
            recordBtn.setText("学习记录");
            recordBtn.setTextColor(0xFFFFFFFF);
            recordBtn.setBackgroundColor(0xFF318af8);
            recordBtn.setTextSize(14);
            recordBtn.setPadding(24, 10, 24, 10);
            recordBtn.setOnClickListener(v -> {
                Intent intent = new Intent(UserManageActivity.this, UserRecordActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("userName", name);
                startActivity(intent);
            });
            holder.btns.addView(recordBtn);
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView name, info, contact;
            LinearLayout btns;
            VH(View v, TextView name, TextView info, TextView contact, LinearLayout btns) {
                super(v);
                this.name = name;
                this.info = info;
                this.contact = contact;
                this.btns = btns;
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
