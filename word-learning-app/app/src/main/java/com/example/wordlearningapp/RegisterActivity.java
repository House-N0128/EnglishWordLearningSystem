package com.example.wordlearningapp;

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
import com.google.gson.JsonObject;

public class RegisterActivity extends AppCompatActivity {

    private EditText etAccount, etPassword, etConfirmPw, etNickname, etPhone, etEmail;
    private TextView tvError, tvSuccess;
    private Button btnRegister, btnBack;
    private CheckBox cbAgreement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etAccount = findViewById(R.id.et_account);
        etPassword = findViewById(R.id.et_password);
        etConfirmPw = findViewById(R.id.et_confirm_pw);
        etNickname = findViewById(R.id.et_nickname);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        tvError = findViewById(R.id.tv_error);
        tvSuccess = findViewById(R.id.tv_success);
        btnRegister = findViewById(R.id.btn_register);
        btnBack = findViewById(R.id.btn_back);
        cbAgreement = findViewById(R.id.cb_agreement);

        btnRegister.setOnClickListener(v -> doRegister());
        btnBack.setOnClickListener(v -> finish());
    }

    private void doRegister() {
        String account = etAccount.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPw = etConfirmPw.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        tvError.setVisibility(View.GONE);
        tvSuccess.setVisibility(View.GONE);

        // 检查用户协议
        if (!cbAgreement.isChecked()) {
            showError("请先阅读并同意《用户服务协议》");
            return;
        }

        // 验证所有字段
        if (account.isEmpty() || password.isEmpty() || confirmPw.isEmpty()
                || nickname.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            showError("请填写所有必填字段");
            return;
        }

        // 验证密码一致性
        if (!password.equals(confirmPw)) {
            showError("两次输入的密码不一致");
            return;
        }

        // 验证密码强度
        if (password.length() < 6) {
            showError("密码长度至少6位");
            return;
        }

        // 验证手机号格式
        if (!phone.matches("\\d{11}")) {
            showError("请输入正确的11位手机号");
            return;
        }

        // 验证邮箱格式
        if (!email.contains("@")) {
            showError("请输入正确的邮箱地址");
            return;
        }

        // 禁用按钮，防止重复提交
        btnRegister.setEnabled(false);
        btnRegister.setText("注册中...");

        // 在子线程中执行网络请求
        new Thread(() -> {
            try {
                // 构建注册请求体
                JsonObject body = new JsonObject();
                body.addProperty("userId", account);
                body.addProperty("loginPassword", password);
                body.addProperty("userName", nickname);
                body.addProperty("phoneNumber", phone);
                body.addProperty("email", email);

                // 调用注册API
                JsonObject result = ApiClient.get().post("/api/user/register", body);
                int code = result.has("code") ? result.get("code").getAsInt() : -1;
                String message = result.has("message") ? result.get("message").getAsString() : "注册失败";

                // 在主线程中更新UI
                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("注册");

                    if (code == 200) {
                        tvSuccess.setText("注册成功！即将返回登录...");
                        tvSuccess.setVisibility(View.VISIBLE);
                        Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();

                        // 延迟1.5秒后返回登录页面
                        btnRegister.postDelayed(() -> {
                            finish();
                        }, 1500);
                    } else {
                        showError(message);
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
