package com.example.wordlearningapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class UserManageActivity extends AppCompatActivity {

    private LinearLayout listArea;
    private TextView tvPager;
    private EditText etSearch;
    private Spinner spStatus;
    private List<JsonObject> allUsersData = new ArrayList<>();
    private List<JsonObject> filteredData = new ArrayList<>();
    private int pageSize = 8, currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFf2f8fc);

        // ===== TOP BAR: 48dp, #318af8 (same as AdminMainActivity) =====
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(0xFF318af8);
        topBar.setPadding(dp(16), 0, dp(16), 0);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));

        TextView adminUser = new TextView(this);
        adminUser.setText("管理员：" + AuthManager.get().getUserId());
        adminUser.setTextSize(16);
        adminUser.setTextColor(0xFFFFFFFF);
        adminUser.setTypeface(null, Typeface.BOLD);
        adminUser.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        topBar.addView(adminUser);

        TextView btnRefresh = new TextView(this);
        btnRefresh.setText("刷新");
        btnRefresh.setTextSize(15);
        btnRefresh.setTextColor(0xFF318af8);
        btnRefresh.setBackgroundColor(0xFFFFFFFF);
        btnRefresh.setPadding(dp(16), dp(7), dp(16), dp(7));
        btnRefresh.setGravity(Gravity.CENTER);
        GradientDrawable rfBg = new GradientDrawable();
        rfBg.setColor(0xFFFFFFFF);
        rfBg.setCornerRadius(dp(16));
        btnRefresh.setBackground(rfBg);
        btnRefresh.setOnClickListener(v -> loadAllUsers());
        topBar.addView(btnRefresh);

        TextView btnLogout = new TextView(this);
        btnLogout.setText("退出登录");
        btnLogout.setTextSize(15);
        btnLogout.setTextColor(0xFF318af8);
        btnLogout.setBackgroundColor(0xFFFFFFFF);
        btnLogout.setPadding(dp(16), dp(7), dp(16), dp(7));
        btnLogout.setGravity(Gravity.CENTER);
        GradientDrawable lgBg = new GradientDrawable();
        lgBg.setColor(0xFFFFFFFF);
        lgBg.setCornerRadius(dp(16));
        btnLogout.setBackground(lgBg);
        btnLogout.setOnClickListener(v -> {
            AuthManager.get().clearAuth();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        topBar.addView(btnLogout);
        root.addView(topBar);

        // ===== SCROLLABLE MAIN =====
        ScrollView scroll = new ScrollView(this);
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(12), dp(8), dp(12), dp(80));

        // Page title
        TextView title = new TextView(this);
        title.setText("用户管理");
        title.setTextSize(19);
        title.setTextColor(0xFF318af8);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, dp(15), 0, dp(15));
        main.addView(title);

        // Search row: input + status dropdown
        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        searchRow.setPadding(0, 0, 0, dp(14));

        etSearch = new EditText(this);
        etSearch.setHint("用户ID/昵称/邮箱");
        etSearch.setTextSize(14);
        etSearch.setSingleLine(true);
        etSearch.setPadding(dp(10), 0, dp(10), 0);
        bg(etSearch, 0xFFf6f8fc, dp(7), 1, 0xFFc7d9ee);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { doSearch(); }
            @Override public void afterTextChanged(android.text.Editable e) {}
        });
        LinearLayout.LayoutParams edp = new LinearLayout.LayoutParams(0, dp(42), 1);
        edp.gravity = Gravity.CENTER_VERTICAL;
        etSearch.setLayoutParams(edp);
        searchRow.addView(etSearch);

        spStatus = new Spinner(this);
        spStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"账号状态", "全部", "正常", "冻结"}));
        styleSpinner(spStatus);
        LinearLayout.LayoutParams spp = new LinearLayout.LayoutParams(0, dp(42), 1);
        spp.setMargins(dp(8), 0, 0, 0);
        spp.gravity = Gravity.CENTER_VERTICAL;
        spStatus.setLayoutParams(spp);
        spStatus.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) { doSearch(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        searchRow.addView(spStatus);

        main.addView(searchRow);

        // User card list
        listArea = new LinearLayout(this);
        listArea.setOrientation(LinearLayout.VERTICAL);
        main.addView(listArea);

        // Pagination
        LinearLayout pager = new LinearLayout(this);
        pager.setOrientation(LinearLayout.HORIZONTAL);
        pager.setGravity(Gravity.CENTER);
        pager.setPadding(0, dp(8), 0, 0);

        Button btnPrev = new Button(this);
        btnPrev.setText("上一页");
        btnPrev.setTextColor(0xFFFFFFFF);
        btnPrev.setTextSize(13);
        btnPrev.setPadding(dp(12), dp(6), dp(12), dp(6));
        GradientDrawable prevBg = new GradientDrawable();
        prevBg.setColor(0xFF318af8);
        prevBg.setCornerRadius(dp(14));
        btnPrev.setBackground(prevBg);
        btnPrev.setOnClickListener(v -> { if (currentPage > 0) { currentPage--; renderPage(); } });
        pager.addView(btnPrev);

        tvPager = new TextView(this);
        tvPager.setTextSize(13);
        tvPager.setTextColor(0xFF8899aa);
        tvPager.setPadding(dp(16), 0, dp(16), 0);
        pager.addView(tvPager);

        Button btnNext = new Button(this);
        btnNext.setText("下一页");
        btnNext.setTextColor(0xFFFFFFFF);
        btnNext.setTextSize(13);
        btnNext.setPadding(dp(12), dp(6), dp(12), dp(6));
        GradientDrawable nextBg = new GradientDrawable();
        nextBg.setColor(0xFF318af8);
        nextBg.setCornerRadius(dp(14));
        btnNext.setBackground(nextBg);
        btnNext.setOnClickListener(v -> { int total = (filteredData.size() + pageSize - 1) / pageSize; if (currentPage < total - 1) { currentPage++; renderPage(); } });
        pager.addView(btnNext);

        main.addView(pager);

        scroll.addView(main);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        // ===== BOTTOM NAVBAR =====
        root.addView(makeNavbar());

        setContentView(root);
        loadAllUsers();
    }

    private void styleSpinner(Spinner sp) {
        sp.setPadding(dp(8), 0, dp(4), 0);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFFf6f8fc);
        bg.setCornerRadius(dp(7));
        bg.setStroke(1, 0xFFc7d9ee);
        sp.setBackground(bg);
    }

    private LinearLayout makeNavbar() {
        LinearLayout navbar = new LinearLayout(this);
        navbar.setOrientation(LinearLayout.HORIZONTAL);
        navbar.setBackgroundColor(0xFFFFFFFF);
        navbar.setPadding(0, dp(8), 0, dp(12));
        navbar.setElevation(dp(8));
        navbar.setWeightSum(5f);
        navbar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(61)));
        navbar.setGravity(Gravity.CENTER);
        navItem(navbar, "", "主页", false, () -> startActivity(new Intent(this, AdminMainActivity.class)));
        navItem(navbar, "", "用户管理", true, () -> {});
        navItem(navbar, "", "词书管理", false, () -> startActivity(new Intent(this, WordBooksActivity.class)));
        navItem(navbar, "", "单词管理", false, () -> startActivity(new Intent(this, WordSearchActivity.class)));
        navItem(navbar, "", "个人信息", false, () -> startActivity(new Intent(this, AdminProfileActivity.class)));
        return navbar;
    }

    private void navItem(LinearLayout parent, String icon, String label, boolean active, Runnable action) {
        TextView item = new TextView(this);
        item.setText(icon.isEmpty() ? label : icon + "\n" + label);
        item.setTextSize(13);
        item.setTextColor(active ? 0xFF17c2ae : 0xFF8899aa);
        item.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
        item.setGravity(Gravity.CENTER);
        item.setMaxLines(1);
        item.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        item.setOnClickListener(v -> action.run());
        parent.addView(item);
    }

    private void bg(View v, int color, int radius, int borderW, int borderC) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radius);
        if (borderW > 0) g.setStroke(borderW, borderC);
        v.setBackground(g);
    }

    private void loadAllUsers() {
        new Thread(() -> {
            try {
                JsonObject r = ApiClient.get().get("/api/admin/users?keyword=");
                if (r.get("code").getAsInt() == 200) {
                    JsonArray data = r.getAsJsonArray("data");
                    allUsersData.clear();
                    for (int i = 0; i < data.size(); i++) {
                        allUsersData.add(data.get(i).getAsJsonObject());
                    }
                    runOnUiThread(() -> doSearch());
                }
            } catch (Exception e) {}
        }).start();
    }

    private void doSearch() {
        String kw = etSearch.getText().toString().trim().toLowerCase();
        String statusFilter = spStatus.getSelectedItem() != null ? spStatus.getSelectedItem().toString() : "账号状态";

        // Filter
        List<JsonObject> filtered = new ArrayList<>();
        for (JsonObject u : allUsersData) {
            String uid = u.has("userId") ? u.get("userId").getAsString() : "";
            String name = u.has("userName") && !u.get("userName").isJsonNull() ? u.get("userName").getAsString() : "";
            String email = u.has("email") && !u.get("email").isJsonNull() ? u.get("email").getAsString() : "";
            String status = u.has("accountStatus") ? u.get("accountStatus").getAsString() : "正常";

            if (!kw.isEmpty() && !uid.toLowerCase().contains(kw) && !name.toLowerCase().contains(kw) && !email.toLowerCase().contains(kw))
                continue;
            if (!"账号状态".equals(statusFilter) && !"全部".equals(statusFilter) && !status.equals(statusFilter))
                continue;

            filtered.add(u);
        }

        filteredData = filtered;
        currentPage = 0;
        renderPage();
    }

    private void renderPage() {
        listArea.removeAllViews();
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, filteredData.size());
        int total = (filteredData.size() + pageSize - 1) / pageSize;
        if (total == 0) total = 1;
        tvPager.setText("第" + (currentPage + 1) + "页 / 共" + total + "页");

        for (int i = start; i < end; i++) {
            JsonObject u = filteredData.get(i);
            String uid = u.has("userId") ? u.get("userId").getAsString() : "";
            String name = u.has("userName") && !u.get("userName").isJsonNull() ? u.get("userName").getAsString() : "";
            String phone = u.has("phoneNumber") && !u.get("phoneNumber").isJsonNull() ? u.get("phoneNumber").getAsString() : "";
            String email = u.has("email") && !u.get("email").isJsonNull() ? u.get("email").getAsString() : "";
            String status = u.has("accountStatus") ? u.get("accountStatus").getAsString() : "正常";
            String regTime = u.has("registerTime") && !u.get("registerTime").isJsonNull() ? u.get("registerTime").getAsString().substring(0, 10) : "";

            // Card
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(13), dp(13), dp(13), dp(13));
            GradientDrawable cBg = new GradientDrawable();
            cBg.setColor(0xFFFFFFFF);
            cBg.setCornerRadius(dp(12));
            card.setBackground(cBg);
            card.setElevation(dp(2));
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cp.setMargins(0, 0, 0, dp(13));
            card.setLayoutParams(cp);

            addRow(card, "用户ID: " + uid + " | 昵称: " + (name.isEmpty() ? "-" : name), 14, 0xFF273245, Typeface.BOLD);
            addRow(card, "电话: " + (phone.isEmpty() ? "-" : phone) + " | 邮箱: " + (email.isEmpty() ? "-" : email), 13, 0xFF4a5568, Typeface.NORMAL);
            if (!regTime.isEmpty()) addRow(card, "注册时间: " + regTime, 12, 0xFF8899aa, Typeface.NORMAL);

            // Status
            boolean isNormal = "正常".equals(status);
            TextView stv = new TextView(this);
            stv.setText("账号状态: " + status);
            stv.setTextSize(13);
            stv.setTextColor(isNormal ? 0xFF25cb75 : 0xFFe37b4b);
            stv.setTypeface(null, Typeface.BOLD);
            stv.setPadding(0, dp(4), 0, dp(8));
            card.addView(stv);

            // Action buttons
            LinearLayout btns = new LinearLayout(this);
            btns.setOrientation(LinearLayout.HORIZONTAL);

            // 修改信息
            Button editBtn = cardBtn(btns, "修改信息", 0xFF318af8, 0xFFFFFFFF);
            editBtn.setOnClickListener(v -> showEditDialog(uid, name, phone, email));

            // 禁用/启用
            boolean willFreeze = isNormal;
            Button toggleBtn = cardBtn(btns, willFreeze ? "禁用账号" : "启用账号",
                    willFreeze ? 0xFFe8ecf1 : 0xFFe8ecf1,
                    willFreeze ? 0xFF5a6b80 : 0xFF38a169);
            toggleBtn.setOnClickListener(v -> toggleStatus(uid, name, isNormal ? "冻结" : "正常"));

            // 查看学习记录
            Button recBtn = cardBtn(btns, "查看学习记录", 0xFFff9f00, 0xFFFFFFFF);
            recBtn.setOnClickListener(v -> {
                Intent in = new Intent(this, UserRecordActivity.class);
                in.putExtra("userId", uid);
                in.putExtra("userName", name);
                startActivity(in);
            });

            btns.addView(editBtn);
            btns.addView(toggleBtn);
            btns.addView(recBtn);
            card.addView(btns);

            listArea.addView(card);
        }
    }

    private void addRow(LinearLayout card, String text, int size, int color, int style) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(size);
        tv.setTextColor(color);
        tv.setTypeface(null, style);
        tv.setPadding(0, 0, 0, dp(4));
        card.addView(tv);
    }

    private Button cardBtn(LinearLayout parent, String text, int bgColor, int textColor) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(textColor);
        b.setTextSize(12);
        b.setPadding(dp(8), dp(4), dp(8), dp(4));
        b.setMinWidth(0);
        b.setMinHeight(0);
        b.setMinimumWidth(0);
        b.setMinimumHeight(0);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(bgColor);
        bg.setCornerRadius(dp(5));
        b.setBackground(bg);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0, 0, dp(6), 0);
        b.setLayoutParams(bp);
        return b;
    }

    private void showEditDialog(String userId, String oldName, String oldPhone, String oldEmail) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(16), dp(12), dp(16), 0);

        EditText etName = new EditText(this);
        etName.setHint("昵称");
        etName.setText(oldName);
        etName.setTextSize(14);
        etName.setPadding(dp(12), dp(10), dp(12), dp(10));
        etName.setBackgroundColor(0xFFf6f8fc);
        form.addView(etName);

        EditText etPhone = new EditText(this);
        etPhone.setHint("电话");
        etPhone.setText(oldPhone);
        etPhone.setTextSize(14);
        etPhone.setPadding(dp(12), dp(10), dp(12), dp(10));
        etPhone.setBackgroundColor(0xFFf6f8fc);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pp.setMargins(0, dp(8), 0, 0);
        etPhone.setLayoutParams(pp);
        form.addView(etPhone);

        EditText etEmail = new EditText(this);
        etEmail.setHint("邮箱");
        etEmail.setText(oldEmail);
        etEmail.setTextSize(14);
        etEmail.setPadding(dp(12), dp(10), dp(12), dp(10));
        etEmail.setBackgroundColor(0xFFf6f8fc);
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ep.setMargins(0, dp(8), 0, 0);
        etEmail.setLayoutParams(ep);
        form.addView(etEmail);

        new AlertDialog.Builder(this)
                .setTitle("修改用户信息 - " + userId)
                .setView(form)
                .setPositiveButton("保存", (d, w) -> {
                    new Thread(() -> {
                        try {
                            JsonObject body = new JsonObject();
                            body.addProperty("userName", etName.getText().toString().trim());
                            body.addProperty("phoneNumber", etPhone.getText().toString().trim());
                            body.addProperty("email", etEmail.getText().toString().trim());
                            JsonObject r = ApiClient.get().put("/api/admin/users/" + userId, body);
                            runOnUiThread(() -> {
                                Toast.makeText(this, r.has("message") ? r.get("message").getAsString() : "已更新", Toast.LENGTH_SHORT).show();
                                loadAllUsers();
                            });
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(this, "修改失败", Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void toggleStatus(String userId, String userName, String newStatus) {
        String action = "冻结".equals(newStatus) ? "禁用" : "启用";
        new AlertDialog.Builder(this)
                .setTitle(action + "用户")
                .setMessage("确定" + action + " \"" + userName + "\" 吗？")
                .setPositiveButton(action, (d, w) -> {
                    new Thread(() -> {
                        try {
                            JsonObject body = new JsonObject();
                            body.addProperty("accountStatus", newStatus);
                            JsonObject r = ApiClient.get().put("/api/admin/users/" + userId, body);
                            runOnUiThread(() -> {
                                Toast.makeText(this, r.has("message") ? r.get("message").getAsString() : "完成", Toast.LENGTH_SHORT).show();
                                loadAllUsers();
                            });
                        } catch (Exception e) {}
                    }).start();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
