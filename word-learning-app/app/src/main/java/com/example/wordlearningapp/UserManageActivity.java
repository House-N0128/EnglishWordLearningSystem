package com.example.wordlearningapp;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xFFf2f8fc);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        // Header
        LinearLayout h = new LinearLayout(this);
        h.setBackgroundColor(0xFF318af8);
        h.setPadding(32, 24, 32, 24);
        h.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = new TextView(this);
        back.setText("← 返回"); back.setTextSize(15); back.setTextColor(0xFFFFFFFF);
        back.setOnClickListener(v -> finish());
        h.addView(back);
        TextView t = new TextView(this);
        t.setText("用户管理"); t.setTextSize(18); t.setTextColor(0xFFFFFFFF); t.setGravity(Gravity.CENTER);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        h.addView(t);
        h.addView(new View(this) {{ setLayoutParams(new LinearLayout.LayoutParams(48, 1)); }});
        root.addView(h);

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(24, 16, 24, 80);

        // Search row: input + button, same style as WordSearchActivity
        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);

        etSearch = new EditText(this);
        etSearch.setHint("输入用户ID/昵称搜索");
        etSearch.setTextSize(15);
        etSearch.setPadding(20, 16, 20, 16);
        etSearch.setGravity(Gravity.CENTER_VERTICAL);
        etSearch.setSingleLine(true);
        GradientDrawable sd = new GradientDrawable();
        sd.setColor(0xFFf6f8fc); sd.setCornerRadius(12); sd.setStroke(1, 0xFFc7d9ee);
        etSearch.setBackground(sd);
        LinearLayout.LayoutParams edp = new LinearLayout.LayoutParams(0, dp(48), 1);
        edp.gravity = Gravity.CENTER_VERTICAL;
        etSearch.setLayoutParams(edp);
        searchRow.addView(etSearch);

        Button searchBtn = new Button(this);
        searchBtn.setText("搜索");
        searchBtn.setTextColor(0xFFFFFFFF);
        searchBtn.setTextSize(14);
        searchBtn.setPadding(16, 0, 16, 0);
        searchBtn.setGravity(Gravity.CENTER);
        GradientDrawable sbb = new GradientDrawable();
        sbb.setColor(0xFF318af8); sbb.setCornerRadius(12);
        searchBtn.setBackground(sbb);
        LinearLayout.LayoutParams sbp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(48));
        sbp.setMargins(8, 0, 0, 0);
        searchBtn.setLayoutParams(sbp);
        searchBtn.setOnClickListener(v -> doSearch());
        searchRow.addView(searchBtn);

        // Enter key triggers search
        etSearch.setOnEditorActionListener((v, actionId, event) -> { if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) { doSearch(); return true; } return false; });

        main.addView(searchRow);

        listArea = new LinearLayout(this);
        listArea.setOrientation(LinearLayout.VERTICAL);
        main.addView(listArea);

        root.addView(main);
        scroll.addView(root);
        setContentView(scroll);
    }

    private void doSearch() {
        String kw = etSearch.getText().toString().trim();
        if (kw.isEmpty()) return;

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
                            String phone = u.has("phoneNumber") && !u.get("phoneNumber").isJsonNull() ? u.get("phoneNumber").getAsString() : "";
                            String email = u.has("email") && !u.get("email").isJsonNull() ? u.get("email").getAsString() : "";

                            LinearLayout card = new LinearLayout(this);
                            card.setOrientation(LinearLayout.VERTICAL);
                            card.setBackgroundColor(0xFFFFFFFF);
                            card.setPadding(24, 18, 24, 18);
                            GradientDrawable cd = new GradientDrawable();
                            cd.setColor(0xFFFFFFFF); cd.setCornerRadius(16);
                            card.setBackground(cd);
                            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            cp.setMargins(0, 0, 0, 14);
                            card.setLayoutParams(cp);

                            TextView nm = new TextView(this);
                            nm.setText(name + " (" + uid + ")");
                            nm.setTextSize(16); nm.setTextColor(0xFF318af8);
                            card.addView(nm);

                            TextView info = new TextView(this);
                            info.setText("状态: " + status + " | " + phone + " | " + email);
                            info.setTextSize(13); info.setTextColor(0xFF8899aa);
                            info.setPadding(0, 4, 0, 8);
                            card.addView(info);

                            if (u.has("registerTime") && !u.get("registerTime").isJsonNull()) {
                                TextView reg = new TextView(this);
                                reg.setText("注册: " + u.get("registerTime").getAsString().substring(0, 10));
                                reg.setTextSize(12); reg.setTextColor(0xFF8899aa);
                                reg.setPadding(0, 0, 0, 8);
                                card.addView(reg);
                            }

                            LinearLayout btns = new LinearLayout(this);
                            btns.setOrientation(LinearLayout.HORIZONTAL);

                            String newStatus = "冻结".equals(status) ? "正常" : "冻结";
                            String action = "冻结".equals(newStatus) ? "冻结" : "解冻";
                            int btnColor = "冻结".equals(newStatus) ? 0xFFe53e3e : 0xFF38a169;

                            Button toggle = new Button(this);
                            toggle.setText(action); toggle.setTextColor(0xFFFFFFFF); toggle.setTextSize(13); toggle.setPadding(20, 8, 20, 8);
                            GradientDrawable tb = new GradientDrawable();
                            tb.setColor(btnColor); tb.setCornerRadius(20);
                            toggle.setBackground(tb);
                            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            tp.setMargins(0, 0, 12, 0);
                            toggle.setLayoutParams(tp);
                            String fid = uid;
                            toggle.setOnClickListener(v -> {
                                new AlertDialog.Builder(this).setTitle(action + "用户").setMessage("确定" + action + "\"" + name + "\"？")
                                        .setPositiveButton(action, (d, w) -> {
                                            new Thread(() -> {
                                                try {
                                                    JsonObject body = new JsonObject();
                                                    body.addProperty("accountStatus", newStatus);
                                                    JsonObject rr = ApiClient.get().put("/api/admin/users/" + fid, body);
                                                    runOnUiThread(() -> { Toast.makeText(this, rr.has("message") ? rr.get("message").getAsString() : "完成", Toast.LENGTH_SHORT).show(); doSearch(); });
                                                } catch (Exception e) {}
                                            }).start();
                                        }).setNegativeButton("取消", null).show();
                            });
                            btns.addView(toggle);

                            Button rec = new Button(this);
                            rec.setText("学习记录"); rec.setTextColor(0xFFFFFFFF); rec.setTextSize(13); rec.setPadding(20, 8, 20, 8);
                            GradientDrawable rb = new GradientDrawable();
                            rb.setColor(0xFF318af8); rb.setCornerRadius(20);
                            rec.setBackground(rb);
                            String rid = uid;
                            rec.setOnClickListener(v -> {
                                Intent in = new Intent(this, UserRecordActivity.class);
                                in.putExtra("userId", rid); in.putExtra("userName", name); startActivity(in);
                            });
                            btns.addView(rec);

                            card.addView(btns);
                            listArea.addView(card);
                        }
                    } else {
                        TextView emp = new TextView(this);
                        emp.setText("未找到匹配用户");
                        emp.setTextSize(14); emp.setTextColor(0xFF8899aa);
                        emp.setGravity(Gravity.CENTER); emp.setPadding(0, 40, 0, 40);
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
