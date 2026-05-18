package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonObject;

public class ProfileActivity extends AppCompatActivity {

    private LinearLayout contentArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ((TextView) findViewById(R.id.toolbar_title)).setText("个人中心");
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());
        contentArea = findViewById(R.id.content_area);
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().get("/api/user/profile");
                if (res.get("code").getAsInt() == 200) {
                    JsonObject u = res.getAsJsonObject("data");
                    runOnUiThread(() -> buildUI(u));
                } else {
                    runOnUiThread(() -> showText("加载失败"));
                }
            } catch (Exception e) {
                runOnUiThread(() -> showText("加载失败"));
            }
        }).start();
    }

    private void buildUI(JsonObject u) {
        contentArea.removeAllViews();

        String[][] fields = {
                {"用户昵称", u.has("userName") ? getStr(u, "userName") : "-"},
                {"登录账号", u.has("userId") ? getStr(u, "userId") : "-"},
                {"联系电话", u.has("phoneNumber") ? getStr(u, "phoneNumber") : "-"},
                {"邮箱地址", u.has("email") ? getStr(u, "email") : "-"},
                {"注册时间", u.has("registerTime") ? trimDate(getStr(u, "registerTime")) : "-"},
                {"最后登录", u.has("lastLoginTime") ? trimDate(getStr(u, "lastLoginTime")) : "-"},
                {"账号状态", u.has("accountStatus") ? getStr(u, "accountStatus") : "-"},
        };

        for (String[] f : fields) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setBackgroundColor(0xFFFFFFFF);
            row.setPadding(16, 14, 16, 14);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rp.setMargins(0, 0, 0, 2);
            row.setLayoutParams(rp);

            TextView label = new TextView(this);
            label.setText(f[0]);
            label.setTextSize(16);
            label.setTextColor(0xFF318af8);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            label.setLayoutParams(lp);
            row.addView(label);

            TextView value = new TextView(this);
            value.setText(f[1]);
            value.setTextSize(16);
            value.setTextColor(0xFF273245);
            value.setGravity(Gravity.END);
            row.addView(value);

            contentArea.addView(row);
        }

        // Buttons
        Button btnLogout = new Button(this);
        btnLogout.setText("退出登录");
        btnLogout.setTextColor(0xFF318af8);
        btnLogout.setBackgroundColor(0xFFe0e6f2);
        btnLogout.setTextSize(17);
        btnLogout.setPadding(13, 13, 13, 13);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0, 32, 0, 0);
        btnLogout.setLayoutParams(bp);
        btnLogout.setOnClickListener(v -> {
            AuthManager.get().clearAuth();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        contentArea.addView(btnLogout);
    }

    private String getStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "-";
    }

    private String trimDate(String dt) {
        return dt != null && dt.length() >= 10 ? dt.substring(0, 10) : dt;
    }

    private void showText(String msg) {
        contentArea.removeAllViews();
        TextView tv = new TextView(this);
        tv.setText(msg);
        tv.setTextSize(16);
        tv.setTextColor(0xFF8899aa);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, 40, 0, 40);
        contentArea.addView(tv);
    }
}
