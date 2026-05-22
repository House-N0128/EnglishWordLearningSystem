package com.example.wordlearningapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.JsonObject;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvNickname, tvUserId, tvPhone, tvEmail, tvRegTime, tvLastLogin, tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFf2f8fc);

        // ===== TOP BAR =====
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(0xFF318af8);
        topBar.setGravity(Gravity.CENTER);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        GradientDrawable tbBg = new GradientDrawable();
        tbBg.setColor(0xFF318af8);
        tbBg.setCornerRadii(new float[]{dp(18), dp(18), dp(18), dp(18), 0, 0, 0, 0});
        topBar.setBackground(tbBg);

        TextView barTitle = new TextView(this);
        barTitle.setText("个人中心");
        barTitle.setTextSize(18);
        barTitle.setTextColor(0xFFFFFFFF);
        barTitle.setTypeface(null, Typeface.BOLD);
        topBar.addView(barTitle);
        root.addView(topBar);

        // ===== SCROLLABLE MAIN =====
        ScrollView scroll = new ScrollView(this);
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(14), dp(32), dp(14), dp(82));

        // Info card
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFFFFFFFF);
        card.setPadding(dp(20), dp(32), dp(20), dp(20));
        GradientDrawable cd = new GradientDrawable();
        cd.setColor(0xFFFFFFFF);
        cd.setCornerRadius(dp(20));
        card.setBackground(cd);
        card.setElevation(dp(2));

        tvNickname = addInfoRow(card, "用户昵称", "加载中...");
        tvUserId = addInfoRow(card, "登录账号", "");
        tvPhone = addInfoRow(card, "联系电话", "");
        tvEmail = addInfoRow(card, "邮箱地址", "");
        tvRegTime = addInfoRow(card, "注册时间", "");
        tvLastLogin = addInfoRow(card, "最后登录时间", "");
        tvStatus = addInfoRow(card, "账号状态", "");

        // Buttons
        LinearLayout btns = new LinearLayout(this);
        btns.setOrientation(LinearLayout.VERTICAL);
        btns.setPadding(0, dp(9), 0, 0);

        addBtn(btns, "修改信息", 0xFF318af8, 0xFFFFFFFF, v -> startActivity(new Intent(this, EditProfileActivity.class)));
        addBtn(btns, "修改密码", 0xFF318af8, 0xFFFFFFFF, v -> {
            Intent in = new Intent(this, UserChangePassword1Activity.class);
            in.putExtra("mode", "change");
            startActivity(in);
        });
        addBtn(btns, "注销账号", 0xFF318af8, 0xFFFFFFFF, v -> startActivity(new Intent(this, UserDeleteAccountActivity.class)));
        addBtn(btns, "退出登录", 0xFFe0e6f2, 0xFF318af8, v -> {
            AuthManager.get().clearAuth();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        card.addView(btns);
        main.addView(card);
        scroll.addView(main);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        // ===== BOTTOM NAVBAR =====
        root.addView(makeUserNavbar(5));

        setContentView(root);
        loadData();
    }

    private TextView addInfoRow(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 0, 0, dp(19));

        TextView lbl = new TextView(this);
        lbl.setText(label);
        lbl.setTextSize(17);
        lbl.setTextColor(0xFF318af8);
        lbl.setTypeface(null, Typeface.BOLD);
        row.addView(lbl);

        TextView val = new TextView(this);
        val.setText(value);
        val.setTextSize(17);
        val.setTextColor(0xFF223344);
        val.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        val.setGravity(Gravity.END);
        row.addView(val);

        parent.addView(row);
        return val;
    }

    private void addBtn(LinearLayout parent, String text, int bgColor, int textColor, android.view.View.OnClickListener listener) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextSize(17);
        btn.setTextColor(textColor);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(0, dp(13), 0, dp(13));
        GradientDrawable bd = new GradientDrawable();
        bd.setColor(bgColor);
        bd.setCornerRadius(dp(23));
        btn.setBackground(bd);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0, 0, 0, dp(18));
        btn.setLayoutParams(bp);
        btn.setOnClickListener(listener);
        parent.addView(btn);
    }

    private LinearLayout makeUserNavbar(int activeIndex) {
        LinearLayout navbar = new LinearLayout(this);
        navbar.setOrientation(LinearLayout.HORIZONTAL);
        navbar.setBackgroundColor(0xFFFFFFFF);
        navbar.setPadding(0, dp(8), 0, dp(12));
        navbar.setElevation(dp(8));
        GradientDrawable nbBg = new GradientDrawable();
        nbBg.setColor(0xFFFFFFFF);
        nbBg.setCornerRadii(new float[]{dp(16), dp(16), dp(16), dp(16), 0, 0, 0, 0});
        navbar.setBackground(nbBg);
        navbar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(62)));
        navbar.setGravity(Gravity.CENTER);

        String[][] tabs = {{"🏠","首页"},{"📚","词书浏览"},{"🔍","单词查询"},{"⭐","我的收藏"},{"📝","学习记录"},{"👤","个人中心"}};
        Class<?>[] targets = {MainActivity.class, WordBooksActivity.class, WordSearchActivity.class,
                CollectionsActivity.class, StudyRecordsActivity.class, ProfileActivity.class};

        for (int i = 0; i < tabs.length; i++) {
            boolean active = (i == activeIndex);
            TextView tv = new TextView(this);
            tv.setText(tabs[i][0] + "\n" + tabs[i][1]);
            tv.setTextSize(15);
            tv.setTextColor(active ? 0xFF17c2ae : 0xFF318af8);
            tv.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
            tv.setGravity(Gravity.CENTER);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            int idx = i;
            tv.setOnClickListener(v -> startActivity(new Intent(this, targets[idx])));
            navbar.addView(tv);
        }
        return navbar;
    }

    private void loadData() {
        new Thread(() -> {
            try {
                JsonObject res = ApiClient.get().get("/api/user/profile");
                if (res.get("code").getAsInt() == 200) {
                    JsonObject u = res.getAsJsonObject("data");
                    runOnUiThread(() -> {
                        tvNickname.setText(getStr(u, "userName"));
                        tvUserId.setText(getStr(u, "userId"));
                        tvPhone.setText(getStr(u, "phoneNumber"));
                        tvEmail.setText(getStr(u, "email"));
                        tvRegTime.setText(trimDate(getStr(u, "registerTime")));
                        tvLastLogin.setText(trimDate(getStr(u, "lastLoginTime")));
                        tvStatus.setText(getStr(u, "accountStatus"));
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "加载失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private String getStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "-";
    }

    private String trimDate(String dt) {
        return dt != null && dt.length() >= 10 ? dt.substring(0, 10) : (dt != null ? dt : "-");
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}
