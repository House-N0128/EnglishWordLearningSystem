package com.example.wordlearningapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
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

public class UserManageActivity extends AppCompatActivity {

    private LinearLayout listArea;
    private EditText etSearch;
    private Spinner spStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFf2f8fc);

        // ===== TOP BAR: 48dp, #318af8 =====
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

        TextView btnLogout = new TextView(this);
        btnLogout.setText("退出登录");
        btnLogout.setTextSize(15);
        btnLogout.setTextColor(0xFF318af8);
        btnLogout.setGravity(Gravity.CENTER);
        btnLogout.setPadding(dp(20), dp(7), dp(20), dp(7));
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
        main.setPadding(dp(12), dp(8), dp(12), dp(24));

        // Page title
        TextView title = new TextView(this);
        title.setText("用户管理");
        title.setTextSize(19);
        title.setTextColor(0xFF318af8);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, dp(15), 0, dp(15));
        main.addView(title);

        // Search row: input + dropdown + button
        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        searchRow.setPadding(0, 0, 0, dp(14));

        etSearch = new EditText(this);
        etSearch.setHint("用户ID/账号/昵称");
        etSearch.setTextSize(14);
        etSearch.setSingleLine(true);
        etSearch.setPadding(dp(8), 0, dp(8), 0);
        etSearch.setBackgroundColor(0xFFf6f8fc);
        GradientDrawable edBg = new GradientDrawable();
        edBg.setColor(0xFFf6f8fc); edBg.setCornerRadius(dp(7)); edBg.setStroke(1, 0xFFc7d9ee);
        etSearch.setBackground(edBg);
        LinearLayout.LayoutParams edp = new LinearLayout.LayoutParams(0, dp(48), 3);
        edp.gravity = Gravity.CENTER_VERTICAL;
        etSearch.setLayoutParams(edp);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { doSearch(); }
            @Override public void afterTextChanged(android.text.Editable e) {}
        });
        etSearch.setOnEditorActionListener((v, a, e) -> { if (a == EditorInfo.IME_ACTION_SEARCH || a == EditorInfo.IME_ACTION_DONE) { doSearch(); return true; } return false; });
        searchRow.addView(etSearch);

        spStatus = new Spinner(this);
        spStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"账号状态", "全部", "正常", "冻结"}));
        spStatus.setPadding(dp(8), 0, dp(8), 0);
        spStatus.setBackgroundColor(0xFFf6f8fc);
        GradientDrawable spBg = new GradientDrawable();
        spBg.setColor(0xFFf6f8fc); spBg.setCornerRadius(dp(7)); spBg.setStroke(1, 0xFFc7d9ee);
        spStatus.setBackground(spBg);
        LinearLayout.LayoutParams spp = new LinearLayout.LayoutParams(0, dp(48), 1.5f);
        spp.setMargins(dp(8), 0, 0, 0);
        spp.gravity = Gravity.CENTER_VERTICAL;
        spStatus.setLayoutParams(spp);
        spStatus.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) { doSearch(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        searchRow.addView(spStatus);

        main.addView(searchRow);

        // Result list
        listArea = new LinearLayout(this);
        listArea.setOrientation(LinearLayout.VERTICAL);
        main.addView(listArea);

        scroll.addView(main);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        // ===== BOTTOM NAVBAR =====
        LinearLayout navbar = new LinearLayout(this);
        navbar.setOrientation(LinearLayout.HORIZONTAL);
        navbar.setBackgroundColor(0xFFFFFFFF);
        navbar.setPadding(0, dp(8), 0, dp(12));
        GradientDrawable nbBg = new GradientDrawable();
        nbBg.setColor(0xFFFFFFFF);
        nbBg.setCornerRadii(new float[]{dp(14), dp(14), dp(14), dp(14), 0, 0, 0, 0});
        navbar.setBackground(nbBg);
        navbar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(61)));
        navbar.setGravity(Gravity.CENTER);

        addNav(navbar, "🏠", "主页", false, () -> startActivity(new Intent(this, AdminMainActivity.class)));
        addNav(navbar, "👥", "用户管理", true, () -> {});
        addNav(navbar, "📚", "词书管理", false, () -> startActivity(new Intent(this, WordBooksActivity.class)));
        addNav(navbar, "🗃️", "单词管理", false, () -> startActivity(new Intent(this, WordSearchActivity.class)));
        root.addView(navbar);

        setContentView(root);
    }

    private void addNav(LinearLayout parent, String icon, String label, boolean active, Runnable action) {
        TextView item = new TextView(this);
        item.setText(icon + "\n" + label);
        item.setTextSize(15);
        item.setTextColor(active ? 0xFF17c2ae : 0xFF318af8);
        item.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
        item.setGravity(Gravity.CENTER);
        item.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        item.setOnClickListener(v -> action.run());
        parent.addView(item);
    }

    private void doSearch() {
        String kw = etSearch.getText().toString().trim();
        if (kw.isEmpty()) return;
        String statusFilter = spStatus.getSelectedItem() != null ? spStatus.getSelectedItem().toString() : "账号状态";

        new Thread(() -> {
            try {
                JsonObject r = ApiClient.get().get("/api/admin/users?keyword=" + kw);
                runOnUiThread(() -> {
                    listArea.removeAllViews();
                    if (r.get("code").getAsInt() == 200 && r.getAsJsonArray("data").size() > 0) {
                        JsonArray data = r.getAsJsonArray("data");
                        for (int i = 0; i < data.size(); i++) {
                            JsonObject u = data.get(i).getAsJsonObject();
                            String name = u.has("userName") ? u.get("userName").getAsString() : "";
                            String uid = u.has("userId") ? u.get("userId").getAsString() : "";
                            String status = u.has("accountStatus") ? u.get("accountStatus").getAsString() : "正常";

                            // Filter by status
                            if (!"账号状态".equals(statusFilter) && !"全部".equals(statusFilter) && !status.equals(statusFilter))
                                continue;

                            String phone = u.has("phoneNumber") && !u.get("phoneNumber").isJsonNull() ? u.get("phoneNumber").getAsString() : "";
                            String email = u.has("email") && !u.get("email").isJsonNull() ? u.get("email").getAsString() : "";

                            LinearLayout card = new LinearLayout(this);
                            card.setOrientation(LinearLayout.VERTICAL);
                            card.setBackgroundColor(0xFFFFFFFF);
                            card.setPadding(dp(16), dp(14), dp(16), dp(14));
                            GradientDrawable cBg = new GradientDrawable();
                            cBg.setColor(0xFFFFFFFF); cBg.setCornerRadius(dp(13));
                            card.setBackground(cBg);
                            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            cp.setMargins(0, 0, 0, dp(13));
                            card.setLayoutParams(cp);

                            TextView nm = new TextView(this);
                            nm.setText(name + " (" + uid + ")");
                            nm.setTextSize(15);
                            nm.setTextColor(0xFF318af8);
                            nm.setTypeface(null, Typeface.BOLD);
                            nm.setPadding(0, 0, 0, dp(4));
                            card.addView(nm);

                            TextView info = new TextView(this);
                            info.setText("状态: " + status + " | " + phone + " | " + email);
                            info.setTextSize(13);
                            info.setTextColor(0xFF8899aa);
                            info.setPadding(0, 0, 0, dp(8));
                            card.addView(info);

                            if (u.has("registerTime") && !u.get("registerTime").isJsonNull()) {
                                TextView reg = new TextView(this);
                                reg.setText("注册: " + u.get("registerTime").getAsString().substring(0, 10));
                                reg.setTextSize(12);
                                reg.setTextColor(0xFF8899aa);
                                reg.setPadding(0, 0, 0, dp(8));
                                card.addView(reg);
                            }

                            LinearLayout btns = new LinearLayout(this);
                            btns.setOrientation(LinearLayout.HORIZONTAL);

                            String newStatus = "冻结".equals(status) ? "正常" : "冻结";
                            String action = "冻结".equals(newStatus) ? "冻结" : "解冻";
                            int btnColor = "冻结".equals(newStatus) ? 0xFFe53e3e : 0xFF38a169;

                            Button toggle = new Button(this);
                            toggle.setText(action); toggle.setTextColor(0xFFFFFFFF); toggle.setTextSize(13); toggle.setPadding(dp(20), dp(8), dp(20), dp(8));
                            GradientDrawable tg = new GradientDrawable(); tg.setColor(btnColor); tg.setCornerRadius(dp(14));
                            toggle.setBackground(tg);
                            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            tp.setMargins(0, 0, dp(8), 0); toggle.setLayoutParams(tp);
                            String fUid = uid;
                            toggle.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle(action + "用户")
                                    .setMessage("确定" + action + "\"" + name + "\"？")
                                    .setPositiveButton(action, (d, w) -> {
                                        new Thread(() -> {
                                            try {
                                                JsonObject body = new JsonObject();
                                                body.addProperty("accountStatus", newStatus);
                                                JsonObject rr = ApiClient.get().put("/api/admin/users/" + fUid, body);
                                                runOnUiThread(() -> { Toast.makeText(this, rr.has("message") ? rr.get("message").getAsString() : "完成", Toast.LENGTH_SHORT).show(); doSearch(); });
                                            } catch (Exception e) {}
                                        }).start();
                                    }).setNegativeButton("取消", null).show());
                            btns.addView(toggle);

                            Button rec = new Button(this);
                            rec.setText("学习记录"); rec.setTextColor(0xFFFFFFFF); rec.setTextSize(13); rec.setPadding(dp(20), dp(8), dp(20), dp(8));
                            GradientDrawable rg = new GradientDrawable(); rg.setColor(0xFF318af8); rg.setCornerRadius(dp(14));
                            rec.setBackground(rg);
                            String rUid = uid;
                            rec.setOnClickListener(v -> { Intent in = new Intent(this, UserRecordActivity.class); in.putExtra("userId", rUid); in.putExtra("userName", name); startActivity(in); });
                            btns.addView(rec);

                            card.addView(btns);
                            listArea.addView(card);
                        }
                        if (listArea.getChildCount() == 0) {
                            TextView emp = new TextView(this);
                            emp.setText("未找到匹配用户");
                            emp.setTextSize(14); emp.setTextColor(0xFF8899aa);
                            emp.setGravity(Gravity.CENTER); emp.setPadding(0, dp(40), 0, dp(40));
                            listArea.addView(emp);
                        }
                    } else {
                        TextView emp = new TextView(this);
                        emp.setText("未找到匹配用户");
                        emp.setTextSize(14); emp.setTextColor(0xFF8899aa);
                        emp.setGravity(Gravity.CENTER); emp.setPadding(0, dp(40), 0, dp(40));
                        listArea.addView(emp);
                    }
                });
            } catch (Exception e) {}
        }).start();
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
