package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.model.LoginRequest;
import com.example.wordlearningapp.util.AuthManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextView tabUser, tabAdmin, loginTitle, tvError, tvForgotPassword, tvHelpLinks;
    private EditText etAccount, etPassword;
    private Button btnLogin;
    private TextView btnRegister;
    private String currentRole = "user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AuthManager.init(this);

        if (AuthManager.get().isLoggedIn()) {
            navigateToHome();
            return;
        }

        initViews();
        initListeners();

        checkAutoFillAccount();
    }

    private void checkAutoFillAccount() {
        String autoAccount = getIntent().getStringExtra("auto_account");
        if (autoAccount != null && !autoAccount.isEmpty()) {
            etAccount.setText(autoAccount);
            etAccount.setSelection(autoAccount.length());
            etPassword.requestFocus();

            Toast.makeText(this, "已自动填入账号：" + autoAccount, Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        tabUser = findViewById(R.id.tab_user);
        tabAdmin = findViewById(R.id.tab_admin);
        loginTitle = findViewById(R.id.login_title);
        tvError = findViewById(R.id.tv_error);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvHelpLinks = findViewById(R.id.tv_help_links);
        etAccount = findViewById(R.id.et_account);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
    }

    private void initListeners() {
        tabUser.setOnClickListener(v -> switchRole("user"));
        tabAdmin.setOnClickListener(v -> switchRole("admin"));

        btnLogin.setOnClickListener(v -> doLogin());
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserChangePassword1Activity.class);
            intent.putExtra("mode", "reset");
            startActivity(intent);
        });

        tvHelpLinks.setOnClickListener(v -> {
            Toast.makeText(this, "请联系客服：support@wordlearning.com", Toast.LENGTH_LONG).show();
        });
    }

    private void switchRole(String role) {
        currentRole = role;
        if ("admin".equals(role)) {
            tabAdmin.setBackgroundResource(R.drawable.tab_selected_bg);
            tabAdmin.setTextColor(0xFF3577ef);
            tabUser.setBackgroundResource(android.R.color.transparent);
            tabUser.setTextColor(0xFF8899aa);
            loginTitle.setText("管理员登录");
        } else {
            tabUser.setBackgroundResource(R.drawable.tab_selected_bg);
            tabUser.setTextColor(0xFF3577ef);
            tabAdmin.setBackgroundResource(android.R.color.transparent);
            tabAdmin.setTextColor(0xFF8899aa);
            loginTitle.setText("普通用户登录");
        }
        tvError.setVisibility(View.GONE);
    }

    private void doLogin() {
        String account = etAccount.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (account.isEmpty() || password.isEmpty()) {
            showError("请输入账号和密码");
            return;
        }

        tvError.setVisibility(View.GONE);
        btnLogin.setEnabled(false);
        btnLogin.setText("登录中...");

        new Thread(() -> {
            try {
                String endpoint = "admin".equals(currentRole) ? "/api/admin/login" : "/api/user/login";
                LoginRequest req = new LoginRequest(account, password);
                JsonObject body = new Gson().toJsonTree(req).getAsJsonObject();
                JsonObject result = ApiClient.get().post(endpoint, body);

                int code = result.has("code") ? result.get("code").getAsInt() : -1;
                String message = result.has("message") ? result.get("message").getAsString() : "网络错误";

                Log.d(TAG, "登录响应 - code: " + code + ", message: " + message);
                Log.d(TAG, "完整响应: " + result.toString());

                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("登录");
                    if (code == 200 && result.has("data") && !result.get("data").isJsonNull()) {
                        JsonObject data = result.getAsJsonObject("data");
                        String userId = data.has("userid") ? data.get("userid").getAsString() : account;
                        AuthManager.get().setAuth(userId, currentRole);
                        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                        navigateToHome();
                    } else {
                        handleLoginError(code, message);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "登录异常: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("登录");
                    showError("网络连接失败，请检查网络");
                });
            }
        }).start();
    }

    private void handleLoginError(int code, String message) {
        Log.d(TAG, "处理登录错误 - code: " + code + ", message: " + message);

        // 检查消息中是否包含账号被禁用/冻结的信息
        if (message != null && (message.contains("禁用") || message.contains("冻结") ||
                message.contains("封禁") || message.contains("disabled") || message.contains("freeze"))) {
            Log.d(TAG, "检测到账号被禁用/冻结: " + message);
            showDisabledAccountDialog(message);
            return;
        }

        // 其他错误使用常规提示
        String enhancedMessage = enhanceErrorMessage(code, message);
        showError(enhancedMessage);
    }

    private void showDisabledAccountDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚠️ 账号已被禁用");

        builder.setMessage("您的账号已被禁用，无法登录\n\n" +
                "后端返回信息：" + message + "\n\n" +
                "可能的原因：\n" +
                "• 违反平台使用规定\n" +
                "• 异常登录行为\n" +
                "• 被管理员手动禁用\n\n" +
                "如需解封，请联系客服：\n" +
                "📧 support@wordlearning.com\n" +
                "📞 400-xxx-xxxx");

        builder.setPositiveButton("我知道了", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.setNegativeButton("联系客服", (dialog, which) -> {
            Toast.makeText(this, "客服邮箱：support@wordlearning.com", Toast.LENGTH_LONG).show();
        });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        // 设置按钮颜色
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFF3577ef);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(0xFFd93025);
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }

    private String enhanceErrorMessage(int code, String originalMessage) {
        Log.d(TAG, "错误码: " + code + ", 原始消息: " + originalMessage);

        if (code == 404) {
            return "此账号暂不存在，请先注册";
        }

        if (code == 401) {
            return "账号或密码错误\n如未注册请先注册";
        }

        if (code == 403) {
            return "账号已被禁用，请联系客服";
        }

        return originalMessage;
    }

    private void navigateToHome() {
        Intent intent;
        if ("admin".equals(AuthManager.get().getRole())) {
            intent = new Intent(this, AdminMainActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
