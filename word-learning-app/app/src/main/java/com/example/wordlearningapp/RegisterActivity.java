package com.example.wordlearningapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordlearningapp.api.ApiClient;
import com.example.wordlearningapp.model.LoginRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class RegisterActivity extends AppCompatActivity {

    private EditText etPassword, etConfirmPw, etNickname, etPhone, etEmail;
    private TextView tvAutoAccount, tvError, tvSuccess, tvEmailStatus;
    private Button btnRegister, btnBack, btnVerifyEmail;
    private CheckBox cbAgreement;
    private String generatedAccount;
    private boolean isEmailVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tvAutoAccount = findViewById(R.id.tv_auto_account);
        etPassword = findViewById(R.id.et_password);
        etConfirmPw = findViewById(R.id.et_confirm_pw);
        etNickname = findViewById(R.id.et_nickname);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        tvError = findViewById(R.id.tv_error);
        tvSuccess = findViewById(R.id.tv_success);
        tvEmailStatus = findViewById(R.id.tv_email_status);
        btnRegister = findViewById(R.id.btn_register);
        btnBack = findViewById(R.id.btn_back);
        btnVerifyEmail = findViewById(R.id.btn_verify_email);
        cbAgreement = findViewById(R.id.cb_agreement);

        generateAccount();

        btnVerifyEmail.setOnClickListener(v -> verifyEmailAvailability());

        etEmail.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isEmailVerified = false;
                tvEmailStatus.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        btnRegister.setOnClickListener(v -> doRegister());
        btnBack.setOnClickListener(v -> finish());
    }

    private void generateAccount() {
        etPhone.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateGeneratedAccount();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void updateGeneratedAccount() {
        String phone = etPhone.getText().toString().trim();
        if (phone.length() == 11) {
            // 使用完整手机号 + 时间戳后4位
            long timestamp = System.currentTimeMillis();
            String timeSuffix = String.valueOf(timestamp % 10000);
            // 补齐4位
            while (timeSuffix.length() < 4) {
                timeSuffix = "0" + timeSuffix;
            }
            generatedAccount = phone + timeSuffix;

            tvAutoAccount.setText("系统分配账号：" + generatedAccount);
            tvAutoAccount.setTextColor(0xFF3577ef);
        } else {
            generatedAccount = null;
            tvAutoAccount.setText("请输入完整的11位手机号");
            tvAutoAccount.setTextColor(0xFF8eaac4);
        }
    }

    private void verifyEmailAvailability() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "请输入邮箱地址", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.contains("@")) {
            Toast.makeText(this, "请输入正确的邮箱地址", Toast.LENGTH_SHORT).show();
            return;
        }

        btnVerifyEmail.setEnabled(false);
        btnVerifyEmail.setText("验证中...");
        tvEmailStatus.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("contact", email);

                JsonObject result = ApiClient.get().post("/api/user/send-verification-code", body);
                int code = result.has("code") ? result.get("code").getAsInt() : -1;
                String message = result.has("message") ? result.get("message").getAsString() : "验证失败";

                runOnUiThread(() -> {
                    btnVerifyEmail.setEnabled(true);
                    btnVerifyEmail.setText("验证邮箱");

                    if (code == 200) {
                        tvEmailStatus.setText("❌ 该邮箱已被注册，请更换");
                        tvEmailStatus.setTextColor(0xFFd93025);
                        tvEmailStatus.setVisibility(View.VISIBLE);
                        isEmailVerified = false;
                    } else if (code == 404 || code == 400) {
                        if (message.contains("未注册") || message.contains("不存在")) {
                            tvEmailStatus.setText("✅ 邮箱可用，可以注册");
                            tvEmailStatus.setTextColor(0xFF1b8a2e);
                            tvEmailStatus.setVisibility(View.VISIBLE);
                            isEmailVerified = true;
                        } else {
                            tvEmailStatus.setText("❌ " + message);
                            tvEmailStatus.setTextColor(0xFFd93025);
                            tvEmailStatus.setVisibility(View.VISIBLE);
                            isEmailVerified = false;
                        }
                    } else {
                        tvEmailStatus.setText("❌ 验证失败: " + message);
                        tvEmailStatus.setTextColor(0xFFd93025);
                        tvEmailStatus.setVisibility(View.VISIBLE);
                        isEmailVerified = false;
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnVerifyEmail.setEnabled(true);
                    btnVerifyEmail.setText("验证邮箱");
                    tvEmailStatus.setText(" 网络连接失败");
                    tvEmailStatus.setTextColor(0xFFd93025);
                    tvEmailStatus.setVisibility(View.VISIBLE);
                    isEmailVerified = false;
                });
            }
        }).start();
    }

    private void doRegister() {
        String password = etPassword.getText().toString().trim();
        String confirmPw = etConfirmPw.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        tvError.setVisibility(View.GONE);
        tvSuccess.setVisibility(View.GONE);

        if (!cbAgreement.isChecked()) {
            showError("请先阅读并同意《用户服务协议》");
            return;
        }

        if (password.isEmpty() || confirmPw.isEmpty()
                || nickname.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            showError("请填写所有必填字段");
            return;
        }

        if (!isEmailVerified) {
            showError("请先点击'验证邮箱'按钮检查邮箱是否可用");
            return;
        }

        if (generatedAccount == null || generatedAccount.isEmpty()) {
            showError("请输入有效的手机号以生成账号");
            return;
        }

        if (!password.equals(confirmPw)) {
            showError("两次输入的密码不一致");
            return;
        }

        if (password.length() < 6) {
            showError("密码长度至少6位");
            return;
        }

        if (!phone.matches("\\d{11}")) {
            showError("请输入正确的11位手机号");
            return;
        }

        if (!email.contains("@")) {
            showError("请输入正确的邮箱地址");
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("注册中...");

        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("userId", generatedAccount);
                body.addProperty("loginPassword", password);
                body.addProperty("userName", nickname);
                body.addProperty("phoneNumber", phone);
                body.addProperty("email", email);

                JsonObject result = ApiClient.get().post("/api/user/register", body);
                int code = result.has("code") ? result.get("code").getAsInt() : -1;
                String message = result.has("message") ? result.get("message").getAsString() : "注册失败";

                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("注册");

                    if (code == 200) {
                        tvSuccess.setText("注册成功！\n您的账号是：" + generatedAccount);
                        tvSuccess.setVisibility(View.VISIBLE);
                        Toast.makeText(RegisterActivity.this, "注册成功，账号：" + generatedAccount, Toast.LENGTH_LONG).show();

                        btnRegister.postDelayed(() -> {
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.putExtra("auto_account", generatedAccount);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }, 2000);
                    } else {
                        if (message.contains("账号已存在") || message.contains("重复") || code == 409) {
                            Toast.makeText(RegisterActivity.this, "账号已存在，重新生成...", Toast.LENGTH_SHORT).show();
                            String prefix = phone.substring(0, 7);
                            int random = (int) (Math.random() * 9000) + 1000;
                            generatedAccount = prefix + random;
                            tvAutoAccount.setText("系统分配账号：" + generatedAccount);
                            doRegister();
                        } else {
                            showError(message);
                        }
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("注册");
                    showError("网络连接失败，请检查网络");
                });
            }
        }).start();
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}
